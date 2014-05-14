package org.openmhealth.reference.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmhealth.reference.data.Registry;
import org.openmhealth.reference.domain.MultiValueResult;
import org.openmhealth.reference.domain.MultiValueResultAggregator;
import org.openmhealth.reference.exception.OmhException;
import org.openmhealth.shim.Shim;
import org.openmhealth.shim.ShimRegistry;

/**
 * <p>
 * Retrieves all of the known schema IDs.
 * </p>
 *
 * @author John Jenkins
 */
public class SchemaIdsRequest extends ListRequest<String> {
	/**
	 * Creates a request for the list of known schema IDs.
	 *
	 * @param numToSkip The number of schema IDs to skip.
	 *
	 * @param numToReturn The number of schema IDs to return.
	 *
	 * @throws OmhException A parameter was invalid.
	 */
	public SchemaIdsRequest(
		final Long numToSkip,
		final Long numToReturn)
		throws OmhException {

		super(numToSkip, numToReturn);
	}

	/**
	 * Retrieves the list of known schema IDs.
	 */
	@Override
	public void service() throws OmhException {
		// First, short-circuit if this request has already been serviced.
		if(isServiced()) {
			return;
		}
		else {
			setServiced();
		}

        Set<String> schemaIds = new HashSet<String>();

        schemaIds.addAll(ShimRegistry.getAllSchemaIds());
		
		// Get all internal schema IDs.
		MultiValueResult<String> internalSchemaIds =
			Registry.getInstance().getSchemaIds(0, Long.MAX_VALUE);
        Iterator<String> externalIdIterator = internalSchemaIds.iterator();
        while(externalIdIterator.hasNext()) {
            schemaIds.add(externalIdIterator.next());
        }

        // Sort, dedupe, and set the result.
        List<String> schemaIdsList = new ArrayList<String>();
        schemaIdsList.addAll(schemaIds);
		Collections.sort(schemaIdsList);
        int lowerBound = Math.max((int)getNumToSkip(), 0);
        int upperBound =
            Math.min(
                (int)(getNumToSkip() + getNumToReturn()), 
                schemaIdsList.size());
        schemaIdsList = 
            schemaIdsList.subList(lowerBound, upperBound);

        MultiValueResult<String> result =
            (new MultiValueResultAggregator<String>(schemaIdsList)).build();
		
		// Set the data.
		setData(result);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmhealth.reference.request.ListRequest#getPreviousNextParameters()
	 */
	@Override
	public Map<String, String> getPreviousNextParameters() {
		return Collections.emptyMap();
	}
}