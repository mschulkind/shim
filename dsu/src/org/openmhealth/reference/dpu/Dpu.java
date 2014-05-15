/*******************************************************************************
 * Copyright 2014 Open mHealth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.openmhealth.reference.dpu;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.jenkins.paul.john.concordia.Concordia;

import org.joda.time.DateTime;
import org.openmhealth.reference.data.DataSet;

import org.openmhealth.reference.data.Registry;
import org.openmhealth.reference.data.UserBin;
import org.openmhealth.reference.domain.Data;
import org.openmhealth.reference.domain.DataReader;
import org.openmhealth.reference.domain.MultiValueResult;
import org.openmhealth.reference.domain.MultiValueResultAggregator;
import org.openmhealth.reference.domain.Schema;
import org.openmhealth.reference.domain.User;
import org.openmhealth.reference.exception.OmhException;
import org.openmhealth.reference.util.HttpUtil;
import org.openmhealth.reference.util.OmhObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Dpu {
    private final String id;
    private final String baseUrl;
    private final long version;
    private Schema schema;
    
    public Dpu(
        final String id,
        final String baseUrl,
        final long version) {
        if (id == null || baseUrl == null) {
            throw new OmhException("A required parameter is missing");
        }

        this.id = id;
        this.baseUrl = baseUrl;
        this.version = version;
    }

    public String getId() { return id; }
    public String getBaseUrl() { return baseUrl; }
    public long getVersion() { return version; }

    public Schema getSchema() {
        if (schema == null) {
            URL url = buildUrl(null, null, null);
            Concordia concordia = null;
            try {
                concordia = new Concordia(HttpUtil.fetchUrl(url));
            }
            catch(Exception e) {
                throw new OmhException("Error fetching schema.", e);
            }
                    
            schema = new Schema(id, version, concordia);
        }

        return schema;
    }

    public void run(
        final DateTime startDate,
        final DateTime endDate) {
        ensureOutputSchemaExists();

        JsonNode requirements = fetchRequirements(startDate, endDate);

        // Loop through all users.
        for (User user : UserBin.getInstance().getUsers()) {
            // Run read->transform->write cycle for each user that can fulfill
            // the DPU requirements.
            if (canReadAllRequirements(user.getUsername(), requirements)) {
                // First delete any existing output data so we don't get
                // duplicates from repeat runs.
                DataSet.getInstance().deleteData(
                    user.getUsername(), id, version, startDate, endDate);
                
                Map<String, MultiValueResult<Data>> inputData = 
                    readInputData(user.getUsername(), requirements);
                List<Data> outputData = 
                    transformData(
                        startDate, endDate, user.getUsername(), inputData);
                writeOutputData(outputData);
            }
        }
    }

    public static Map<String, MultiValueResult<Data>> readInputData(
        String username,
        JsonNode requirements) {
        if (!requirements.isArray()) {
            throw new OmhException("Malformed DPU requirements");
        }

        Map<String, MultiValueResult<Data>> dataMap = 
            new HashMap<String, MultiValueResult<Data>>();

        for (int i = 0; i < requirements.size(); ++i) {
            JsonNode requirement = requirements.get(i);

            // Grab the schema ID.
            JsonNode schemaIdJson = requirement.get("schema_id");
            if (schemaIdJson == null || !schemaIdJson.isTextual()) {
                throw new OmhException("Missing schema ID");
            }
            String schemaId = schemaIdJson.asText();

            // Grab the version.
            JsonNode versionJson = requirement.get("version");
            if (versionJson == null || !versionJson.isIntegralNumber()) {
                throw new OmhException("Missing version");
            }
            Long version = new Long(versionJson.longValue());

            // Grab the start date.
            JsonNode startJson = requirement.get("t_start");
            DateTime start = null;
            if (startJson != null && startJson.isTextual()) {
                start = DateTime.parse(startJson.asText());
            }

            // Grab the end date.
            JsonNode endJson = requirement.get("t_end");
            DateTime end = null;
            if (endJson != null && endJson.isTextual()) {
                end = DateTime.parse(endJson.asText());
            }

            // Grab the num to return.
            JsonNode numToReturnJson = requirement.get("num_to_return");
            long numToReturn = Long.MAX_VALUE;
            if (numToReturnJson != null && numToReturnJson.isIntegralNumber()) {
                numToReturn = numToReturnJson.asLong();
            }
            
            // Grab the minimum num to return.
            JsonNode includeOnePreviousJson = 
                requirement.get("include_one_previous");
            Boolean includeOnePrevious = false;
            if (includeOnePreviousJson != null 
                && includeOnePreviousJson.isBoolean()) {
                includeOnePrevious = includeOnePreviousJson.asBoolean();
            }

			MultiValueResultAggregator<Data> result = 
                new MultiValueResultAggregator<Data>();

            // Add a previous point if needed.
            if (includeOnePrevious) {
                result.add(
                    DataReader.readData(
                        schemaId, version, username, null, start, null,
                        new Long(0), 
                        new Long(1)));
            }

            result.add(
                DataReader.readData(
                    schemaId, version, username, start, end, null,
                    new Long(0), 
                    new Long(numToReturn)));

            dataMap.put(schemaId, result.build());
        }

        return dataMap;
    }

    private JsonNode fetchRequirements(
        final DateTime startDate,
        final DateTime endDate) {
        URL url = buildUrl("requirements", startDate, endDate);
        ObjectMapper objectMapper = new OmhObjectMapper();
        JsonNode requirements = null;
        try {
            requirements = objectMapper.readTree(HttpUtil.fetchUrl(url));
        }
        catch(IOException e) {
            throw new OmhException("JSON decoding error", e);
        }

        return requirements;
    }

    private List<Data> transformData(
        final DateTime startDate,
        final DateTime endDate,
        final String username,
        final Map<String, MultiValueResult<Data>> inputData) {
        ObjectMapper objectMapper = new OmhObjectMapper();

        // Encode the input data into JSON.
        String inputDataEncoded = null;
        try {
            inputDataEncoded = objectMapper.writeValueAsString(inputData);
        } catch(JsonProcessingException e) {
            throw new OmhException("JSON encoding error", e);
        }

        // Run the data through the remote DPU.
        JsonNode resultsJson = null;
        URL url = buildUrl("process", startDate, endDate);
        InputStream outputDataStream =
            HttpUtil.fetchUrl(url, null, inputDataEncoded);

		// Parse the data into builders.
		List<Data.Builder> outputDataBuilders;
		try {
			outputDataBuilders =
				objectMapper.readValue(
                    outputDataStream,
                    new TypeReference<List<Data.Builder>>(){});
		} catch(IOException e) {
			throw new OmhException("JSON decoding error", e);
		}

        List<Data> outputData = new ArrayList<Data>();
        for (Data.Builder builder : outputDataBuilders) {
            builder.setOwner(username);
            outputData.add(builder.build(getSchema()));
        }

        return outputData;
    }

    private void writeOutputData(final List<Data> data) {
        DataSet.getInstance().storeData(data);
    }

    private URL buildUrl(
        final String endpoint,
        final DateTime startDate,
        final DateTime endDate) {
        StringBuilder urlBuilder = 
            new StringBuilder(baseUrl + "/omh/v1/" + id + "/" + version);

        if (endpoint != null) {
            urlBuilder.append("/" + endpoint);
        }

        if (startDate != null && endDate != null) {
            urlBuilder.append(
                "?t_start=" + startDate.toString()
                + "&t_end=" + endDate.toString());
        }

        URL url = null;
        try {
            url = new URL(urlBuilder.toString());
        }
        catch(MalformedURLException e) {
            throw new OmhException("URL construction error", e);
        }

        return url;
    }

    private boolean canReadAllRequirements(
        final String username,
        final JsonNode requirements) {
        boolean result = true;

        for (int i = 0; i < requirements.size(); ++i) {
            JsonNode requirement = requirements.get(i);

            // Grab the schema ID.
            JsonNode schemaIdJson = requirement.get("schema_id");
            if (schemaIdJson == null || !schemaIdJson.isTextual()) {
                throw new OmhException("Missing schema ID");
            }
            String schemaId = schemaIdJson.asText();

            // Grab the version.
            JsonNode versionJson = requirement.get("version");
            if (versionJson == null || !versionJson.isIntegralNumber()) {
                throw new OmhException("Missing version");
            }
            long version = versionJson.longValue();

            if (!DataReader.canReadData(schemaId, version, username)) {
                result = false;
                break;
            }
        }

        return result;
    }

    private void ensureOutputSchemaExists() {
        Registry registry = Registry.getInstance();
        if (registry.getSchema(id, version) == null) {
            registry.createSchema(getSchema());
        }
    }
}
