package org.openmhealth.reference.request;

import java.util.Map;

import org.openmhealth.reference.domain.MultiValueResult;
import org.openmhealth.reference.exception.OmhException;

/**
 * <p>
 * The root class for all requests that may return more than one entity.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class ListRequest<T> extends Request<MultiValueResult<T>> {
	/**
	 * The number must be a String to be used in the annotations. When
	 * referencing this value, always use its decoded form
	 * {@link #DEFAULT_NUMBER_TO_SKIP}.
	 */
	public static final String DEFAULT_NUMBER_TO_SKIP_STRING = "0";
	/**
	 * For paging, the default number of items to skip if not given.
	 */
	public static final long DEFAULT_NUMBER_TO_SKIP =
		Long.decode(DEFAULT_NUMBER_TO_SKIP_STRING);

	/**
	 * The number must be a String to be used in the annotations. When
	 * referencing this value, always use its decoded form
	 * {@link #DEFAULT_NUMBER_TO_RETURN};
	 */
	public static final String DEFAULT_NUMBER_TO_RETURN_STRING = "100";
	/**
	 * For paging, the default number of items to skip if not given.
	 */
	public static final long DEFAULT_NUMBER_TO_RETURN =
		Long.decode(DEFAULT_NUMBER_TO_RETURN_STRING);

    /**
     * The meta-data key that indicates the number of data points that are
     * being returned.
     */
    public static final String METADATA_KEY_COUNT = "Count";
    /**
     * The meta-data key that indicates the total number of data points that
     * matched the query before paging was applied.
     */
    public static final String METADATA_KEY_TOTAL_COUNT = "Total-Count";

	/**
	 * The number of elements to skip.
	 */
	private final long numToSkip;
	/**
	 * The number of elements to return.
	 */
	private final long numToReturn;

	/**
	 * Creates the base part of the request with paging.
	 *
	 * @param numToSkip
	 *        The number of elements to skip while processing this request. If
	 *        this is null, the {@link #DEFAULT_NUMBER_TO_SKIP default} is
	 *        used.
	 *
	 * @param numToReturn
	 *        The number of elements to return from this request. If this is
	 *        null, the {@link #DEFAULT_NUMBER_TO_RETURN default} is used.
	 *
	 * @throws OmhException
	 *         A parameter was invalid.
	 */
	public ListRequest(
		final Long numToSkip,
		final Long numToReturn)
		throws OmhException {

		// Validate the number of elements to skip.
		if(numToSkip == null) {
			this.numToSkip = DEFAULT_NUMBER_TO_SKIP;
		}
		else if(numToSkip < 0) {
			throw new OmhException(
				"The number to skip must be 0 or positive: " + numToSkip);
		}
		else {
			this.numToSkip = numToSkip;
		}

		// Validate the number of elements to return.
		if(numToReturn == null) {
			this.numToReturn = DEFAULT_NUMBER_TO_RETURN;
		}
		else if(numToReturn <= 0) {
			throw new OmhException(
				"The number to return must be positive: " + numToReturn);
		}
		else if(numToReturn > DEFAULT_NUMBER_TO_RETURN) {
			throw new OmhException(
				"The number to return is greater than the allowed " +
					"default (" +
					DEFAULT_NUMBER_TO_RETURN +
					"): " +
					numToReturn);
		}
		else {
			this.numToReturn = numToReturn;
		}
	}

	/**
	 * Returns the number of elements to skip.
	 *
	 * @return The number of elements to skip.
	 */
	public long getNumToSkip() {
		return numToSkip;
	}

	/**
	 * Returns the number of elements to return.
	 *
	 * @return The number of elements to return.
	 */
	public long getNumToReturn() {
		return numToReturn;
	}

	/**
	 * Returns the parameters used to build a previous or next URLs. The
	 * resulting map must not include the paging parameters.
	 *
	 * @return A map of parameter keys to their value for previous or next URLs
	 *         excluding the paging parameters.
	 */
	public abstract Map<String, String> getPreviousNextParameters();
}