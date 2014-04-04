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
		if(
			(! ShimRegistry.hasDomain(domain)) &&
			(Registry
				.getInstance()
				.getSchemas(schemaId, version, 0, 1).count() == 0)) {
			
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
		if(ShimRegistry.hasDomain(domain)) {
			// Get the shim.
			Shim shim = ShimRegistry.getShim(domain);
			
			// Lookup the user's authorization code.
			ExternalAuthorizationToken token =
				ExternalAuthorizationTokenBin
					.getInstance()
					.getToken(username, domain);
			
			// If the token does not exist, return an error. Clients should
			// first check to be sure that the user has already authorized this
			// domain.
			if(token == null) {
				throw
					new OmhException(
						"The user has not yet authorized this domain.");
			}
			
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
		}
		// Otherwise, handle the request ourselves.
		else {
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
}
