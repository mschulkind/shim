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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.openmhealth.reference.exception.OmhException;

public class Runner {
	private static final Logger LOGGER =
		Logger.getLogger(Runner.class.getName());

    public static void run(
        DateTime startDate,
        DateTime endDate) {
        // Maps DPU index to a map of the properties.
        //
        // For example, for the following config:
        //
        // dpu.0.baseUrl=http://localhost:4567/
        // dpu.1.baseUrl=http://localhost:4568/
        //
        // You'll get a map with two top-level entries of '0' and '1'. Each
        // sub-map with one entry for baseUrl.
        Map<String, Map<String, String>> dpuPropertiesMap =
            new HashMap<String, Map<String, String>>();
        for (Map.Entry<Object, Object> entry 
             : System.getProperties().entrySet()) {  
            String key = (String)entry.getKey();  
            String value = (String)entry.getValue();  
            String[] keyParts = key.split("\\.");

            if (keyParts.length < 3 || !keyParts[0].equals("dpu")) {
                continue;
            }

            String dpuIndex = keyParts[1];

            // Allocate an inner map if one doesn't yet exist.
            if (dpuPropertiesMap.get(dpuIndex) == null) {
                dpuPropertiesMap.put(dpuIndex, new HashMap<String, String>());
            }

            dpuPropertiesMap.get(dpuIndex).put(keyParts[2], value);
        }

        // Construct all the DPUs from the properties.
        List<Dpu> dpus = new ArrayList<Dpu>();
        for (String dpuIndex : dpuPropertiesMap.keySet()) {
            Map<String, String> map = dpuPropertiesMap.get(dpuIndex);

            String id = map.get("id");
            if (id == null) {
                throw new OmhException(
                    "DPU with index " + dpuIndex + " is missing the id.");
            }

            String baseUrl = map.get("baseUrl");
            if (baseUrl == null) {
                throw new OmhException(
                    "DPU with index " + dpuIndex + " is missing the baseUrl.");
            }

            String versionString = map.get("version");
            if (versionString == null) {
                throw new OmhException(
                    "DPU with index " + dpuIndex + " is missing the version.");
            }
            long version;
            try {
                version = Long.parseLong(versionString);
            } catch (NumberFormatException e) {
                throw new OmhException(
                    "DPU with index " + dpuIndex + " has an invalid version.");
            }

            dpus.add(new Dpu(id, baseUrl, version));
        }

        for (Dpu dpu : dpus) {
            try {
                dpu.run(startDate, endDate);
            } catch (Exception e) {
                LOGGER.log(
                    Level.WARNING, 
                    "Error running DPU '" + dpu.getId() + "/"
                    + dpu.getVersion() + "', skipping.");
            }
        }
    }
}
