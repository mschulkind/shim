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
package org.openmhealth.reference.domain;

import java.util.List;

import org.joda.time.DateTime;
import org.openmhealth.reference.data.DataSet;
import org.openmhealth.reference.data.ExternalAuthorizationTokenBin;
import org.openmhealth.reference.data.Registry;
import org.openmhealth.reference.exception.NoSuchSchemaException;
import org.openmhealth.reference.exception.OmhException;
import org.openmhealth.reference.request.Request;
import org.openmhealth.shim.Shim;
import org.openmhealth.shim.ShimRegistry;

/**
 * Reads data from shims or the database.
 */
public class DataReader {
    public static boolean canReadData(
        final String schemaId,
        final long version,
        final String username) {
        if (isSchemaIdKnown(schemaId, version)) {
            String domain = Request.parseDomain(schemaId);

            if (ShimRegistry.hasDomain(domain)) {
				return (
                    ExternalAuthorizationTokenBin
                        .getInstance()
                        .getToken(username, domain) != null);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static MultiValueResult<Data> readData(
        final String schemaId,
        final long version,
        final String username,
        final DateTime startDate,
        final DateTime endDate,
        final ColumnList columnList,
        final Long numToSkip,
        final Long numToReturn) {
		// Get the domain.
		String domain = Request.parseDomain(schemaId);

		// Check to be sure the schema is known.
        if (!isSchemaIdKnown(schemaId, version)) {
			throw
				new NoSuchSchemaException(
					"The schema ID, '" +
						schemaId +
						"', and version, '" +
						version +
						"', pair is unknown.");
        }
		
		// Get the data.
		MultiValueResult<Data> result;
		// Check if a shim should handle the request.
		if(ShimRegistry.getAllSchemaIds().contains(schemaId)) {
            String sourceDomain = null;
            ExternalAuthorizationToken token = null;

            if (domain.equals(StandardMeasure.DOMAIN)) {
                List<String> sourceDomains = 
                    ShimRegistry.getDomainsForStandardMeasure(
                        schemaId, version);

                // Find an authorized domain from the source domains.
                for (String d : sourceDomains) {
                    sourceDomain = d;
                    token =
                        ExternalAuthorizationTokenBin
                        .getInstance()
                        .getToken(username, d);

                    if (token != null) {
                        break;
                    }
                }

                if (token == null) {
                    throw
                        new OmhException(
                            "No authorized source domains found for measure.");
                }
            } else {
                sourceDomain = domain;

                // Lookup the user's authorization code.
                token =
                    ExternalAuthorizationTokenBin
                    .getInstance()
                    .getToken(username, domain);

                // If the token does not exist, return an error. Clients should
                // first check to be sure that the user has already authorized
                // this domain.
                if(token == null) {
                    throw
                        new OmhException(
                            "The user has not yet authorized this domain.");
                }
            }

			// Get the shim.
			Shim shim = ShimRegistry.getShim(sourceDomain);
			
			// Get the data from the shim.
			List<Data> resultList =
				shim
					.getData(
						schemaId,
						version,
						token,
						startDate,
						endDate,
						columnList,
						numToSkip,
						numToReturn);
			
			// Convert the List object into a MultiValueResult object.
			result =
				(new MultiValueResultAggregator<Data>(resultList)).build();
        } else {
            // Otherwise, handle the request ourselves.
			result =
				DataSet
					.getInstance()
					.getData(
						username, 
						schemaId, 
						version,
						startDate,
						endDate,
						columnList, 
						numToSkip, 
						numToReturn);
		}

        return result;
    }

    private static boolean isSchemaIdKnown(
        final String schemaId, 
        final long version) {
        return (
            ShimRegistry.getAllSchemaIds().contains(schemaId)
            || (Registry
                .getInstance()
                .getSchemas(schemaId, version, 0, 1).count() != 0));
    }
}
