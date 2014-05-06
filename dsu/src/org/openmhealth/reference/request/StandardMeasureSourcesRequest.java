package org.openmhealth.reference.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmhealth.reference.domain.MultiValueResult;
import org.openmhealth.reference.domain.MultiValueResultAggregator;
import org.openmhealth.reference.exception.OmhException;
import org.openmhealth.shim.ShimRegistry;

/**
 * Retrieves all known domains that provide a given standard measure.
 */
public class StandardMeasureSourcesRequest extends ListRequest<String> {
    private final String schemaId;
    private final Long version;

	public StandardMeasureSourcesRequest(
        final String schemaId,
        final Long version,
		final Long numToSkip,
		final Long numToReturn) {
		super(numToSkip, numToReturn);

        this.schemaId = schemaId;
        this.version = version;
	}

	@Override
	public void service() throws OmhException {
        List<String> domains = 
            ShimRegistry.getDomainsForStandardMeasure(schemaId, version);

		Map<String, Object> metaData = new HashMap<String, Object>();
		metaData.put(METADATA_KEY_COUNT, domains.size());
		setMetaData(metaData);

		MultiValueResult<String> result =
			(new MultiValueResultAggregator<String>(domains)).build(); 
		setData(result);
    }

	@Override
	public Map<String, String> getPreviousNextParameters() {
		return Collections.emptyMap();
	}
}
