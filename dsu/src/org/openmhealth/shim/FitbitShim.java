package org.openmhealth.shim;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.schema.ObjectSchema;
import name.jenkins.paul.john.concordia.validator.ValidationController;

import org.joda.time.DateTime;
import org.openmhealth.reference.domain.ColumnList;
import org.openmhealth.reference.domain.Data;
import org.openmhealth.reference.domain.ExternalAuthorizationToken;
import org.openmhealth.reference.domain.MetaData;
import org.openmhealth.reference.domain.Schema;
import org.openmhealth.shim.authorization.ShimAuthorization;
import org.openmhealth.shim.authorization.oauth1.OAuth1Authorization;
import org.openmhealth.shim.exception.ShimDataException;
import org.openmhealth.shim.exception.ShimSchemaException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.FitbitAPIEntityCache;
import com.fitbit.api.client.FitbitApiClientAgent;
import com.fitbit.api.client.FitbitApiCredentialsCache;
import com.fitbit.api.client.FitbitApiCredentialsCacheMapImpl;
import com.fitbit.api.client.FitbitApiEntityCacheMapImpl;
import com.fitbit.api.client.FitbitApiSubscriptionStorage;
import com.fitbit.api.client.FitbitApiSubscriptionStorageInMemoryImpl;
import com.fitbit.api.client.LocalUserDetail;
import com.fitbit.api.client.service.FitbitAPIClientService;
import com.fitbit.api.common.model.activities.ActivitiesSummary;
import com.fitbit.api.common.model.activities.ActivityDistance;
import com.fitbit.api.common.model.sleep.SleepSummary;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.FitbitUser;

public class FitbitShim implements Shim {
    private static final String DOMAIN = "fitbit";

    /**
     * The prefix for all schemas used in this shim.
     */
    private static final String SCHEMA_PREFIX = "omh:" + DOMAIN + ":";

    private FitbitAPIEntityCache entityCache =
        new FitbitApiEntityCacheMapImpl();

    private FitbitApiCredentialsCache credentialsCache = 
        new FitbitApiCredentialsCacheMapImpl();

    private FitbitApiSubscriptionStorage subscriptionStore = 
        new FitbitApiSubscriptionStorageInMemoryImpl();

    private FitbitAPIClientService<FitbitApiClientAgent> apiClientService;

    /**
     * Interface for the data fetchers used in the dataFetcherMap below. One
     * DataFetcher will be defined for each supported domain.
     */
    private interface DataFetcher {
        public Map<String, Object> dataForDay(
            FitbitAPIClientService<FitbitApiClientAgent> client,
            LocalUserDetail localUserDetail, DateTime date);
    }

    /**
     * Maps schemaID to DataFetcher for each supported domain.
     */
    private static Map<String, DataFetcher> dataFetcherMap = 
        new HashMap<String, DataFetcher>();
    static {
        dataFetcherMap.put(
            "activity", 
            new DataFetcher() {
                public Map<String, Object> dataForDay(
                    FitbitAPIClientService<FitbitApiClientAgent> client,
                    LocalUserDetail localUserDetail, DateTime date) {
                    return activityForDay(client, localUserDetail, date);
                }
            });

        dataFetcherMap.put(
            "sleep", 
            new DataFetcher() {
                public Map<String, Object> dataForDay(
                    FitbitAPIClientService<FitbitApiClientAgent> client,
                    LocalUserDetail localUserDetail, DateTime date) {
                    return sleepForDay(client, localUserDetail, date);
                }
            });
    }

    public FitbitShim() {
         apiClientService = 
             new FitbitAPIClientService<FitbitApiClientAgent>(
                 new FitbitApiClientAgent(
                     "api.fitbit.com", "http://www.fitbit.com", 
                     credentialsCache),
                 ShimUtil.getShimProperty(DOMAIN, "clientId"),
                 ShimUtil.getShimProperty(DOMAIN, "clientSecret"),
                 credentialsCache,
                 entityCache,
                 subscriptionStore);
    }

    public FitbitAPIClientService<FitbitApiClientAgent> getApiClientService() {
        return apiClientService;
    }

    public FitbitApiCredentialsCache getCredentialsCache() {
        return credentialsCache;
    }

    public String getDomain() {
        return DOMAIN;
    }

	public ShimAuthorization getAuthorizationImplementation() {
        return new FitbitShimAuthorization();
    }

	public List<String> getSchemaIds() {
        List<String> schemaIds = new ArrayList<String>();
        for (String key : dataFetcherMap.keySet()) {
            schemaIds.add(SCHEMA_PREFIX + key);
        }
        return schemaIds;
    }

	public List<Long> getSchemaVersions(
		final String id)
		throws ShimSchemaException {
        // This shim doesn't (yet) have more than one version of a schema for
        // anything, so we just hardcode the return here.
        List<Long> versionList = new ArrayList<Long>();
        versionList.add(1L);
        return versionList;
    }

	public Schema getSchema(
		final String id,
		final Long version)
		throws ShimSchemaException {
        return ShimUtil.getSchema(id, version);
    }

	public List<Data> getData(
		final String schemaId,
		final Long version,
		final ExternalAuthorizationToken token,
		final DateTime startDate,
		final DateTime endDate,
		final ColumnList columnList,
		final Long numToSkip,
		final Long numToReturn)
		throws ShimDataException {
        // We only have a version 1 for now, so return null early for anything
        // but 1.
        if (!version.equals(1L)) {
            return null;
        }

        LocalUserDetail localUserDetail =
            new LocalUserDetail(token.getUsername());

        // Store the user's access token in the cache.
        APIResourceCredentials credentials =
            new APIResourceCredentials(token.getUsername(), null, null);
        credentials.setAccessToken(token.getAccessToken());
        credentials.setAccessTokenSecret(
            token.<String>getExtra(OAuth1Authorization.KEY_EXTRAS_SECRET));
        credentialsCache.saveResourceCredentials(localUserDetail, credentials);

        // Extract the data type and find the associated DataFetcher.
        String dataType = null;
        try {
            dataType = ShimUtil.dataTypeFromSchemaId(schemaId);
        }
        catch(ShimSchemaException e) {
            throw new ShimDataException("Invalid schema id: " + schemaId, e);
        }
        DataFetcher dataFetcher = dataFetcherMap.get(dataType);
        if (dataFetcher == null) {
            throw new ShimDataException("Unknown schema id: " + schemaId);
        }

        // Fetch the data.
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataPoint = null;
        try {
            dataPoint = objectMapper.valueToTree(
                dataFetcher.dataForDay(
                    apiClientService, localUserDetail, startDate));
        }
        catch(Exception e) {
            throw new ShimDataException("JSON encoding error", e);
        }

        return Arrays.asList(
            new Data(
                token.getUsername(), schemaId, version,
                new MetaData(null, startDate),
                dataPoint));
    }

    private static Map<String, Object> activityForDay(
        FitbitAPIClientService<FitbitApiClientAgent> client,
        LocalUserDetail localUserDetail, DateTime date) {
        // Fetch the data.
        ActivitiesSummary summary = null;
        try {
            summary = client.getClient().getActivities(
                localUserDetail, FitbitUser.CURRENT_AUTHORIZED_USER, 
                date.toLocalDate()).getSummary();
        }
        catch(FitbitAPIException e) {
            throw new ShimDataException("Fitbit API error", e);
        }

        // Build the return data object.
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("steps", summary.getSteps());

        data.put("calories_out", summary.getCaloriesOut());

        double distance = 0;
        for(ActivityDistance d : summary.getDistances()) {
            if (d.getActivity().equals("total")) {
                distance = d.getDistance();
                break;
            }
        }
        data.put("distance", distance);

        if (summary.getFloors() != null) {
            data.put("floors", summary.getFloors());
        }

        return data;
    }

    private static Map<String, Object> sleepForDay(
        FitbitAPIClientService<FitbitApiClientAgent> client,
        LocalUserDetail localUserDetail, DateTime date) {
        // Fetch the data.
        SleepSummary summary = null;
        try {
            summary = client.getClient().getSleep(
                localUserDetail, FitbitUser.CURRENT_AUTHORIZED_USER, 
                date.toLocalDate()).getSummary();
        }
        catch(FitbitAPIException e) {
            throw new ShimDataException("Fitbit API error", e);
        }

        // Build the return data object.
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("minutes_asleep", summary.getTotalMinutesAsleep());
        data.put("time_in_bed", summary.getTotalTimeInBed());

        return data;
    }
}
