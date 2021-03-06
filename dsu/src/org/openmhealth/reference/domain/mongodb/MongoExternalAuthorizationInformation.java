package org.openmhealth.reference.domain.mongodb;

import java.net.URL;
import java.util.Map;

import org.mongojack.ObjectId;
import org.openmhealth.reference.domain.ExternalAuthorizationInformation;
import org.openmhealth.reference.exception.OmhException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB externsion of the {@link ExternalAuthorizationInformation} type.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoExternalAuthorizationInformation
	extends ExternalAuthorizationInformation
	implements MongoDbObject {

	/**
	 * The ID for this class which is used for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Recreates an existing set of external authorization information. This
	 * should be used when an existing set of information is being restored.
	 *
	 * @param username
	 *        The user-name of the Open mHealth user that may be making the
	 *        request.
	 *
	 * @param domain
	 *        The domain to which the request will be made.
	 *
	 * @param authorizeId
	 *        The unique identifier for this information.
	 *
	 * @param url
	 *        The URL to which the request should be made for the domain.
     *
     * @param clientRedirectUrl
     *        The URL to use to redirect the user back to the client after the
     *        authorization flow has completed.
	 *
	 * @param preAuthState
	 *        Values that may be set before the authorization is presented to
	 *        the user. These will be retained-server side and never sent.
	 *
	 * @param creationDate
	 *        The time-stamp of when this information was generated.
	 *
	 * @throws OmhException
	 *         A parameter was invalid.
	 */
	@JsonCreator
	public MongoExternalAuthorizationInformation(
		@JsonProperty(DATABASE_FIELD_ID) final String dbId,
		@JsonProperty(JSON_KEY_USERNAME) final String username,
		@JsonProperty(JSON_KEY_DOMAIN) final String domain,
		@JsonProperty(JSON_KEY_AUTHORIZE_ID) final String authorizeId,
		@JsonProperty(JSON_KEY_URL) final URL url,
		@JsonProperty(JSON_KEY_CLIENT_REDIRECT_URL)
		    final URL clientRedirectUrl,
		@JsonProperty(JSON_KEY_PRE_AUTH_STATE)
			final Map<String, Object> preAuthState,
		@JsonProperty(JSON_KEY_CREATION_DATE) final long creationDate)
		throws OmhException {

		super(
			username,
			domain,
			authorizeId,
			url,
			clientRedirectUrl,
			preAuthState,
			creationDate);

		// Store the MongoDB ID.
		if(dbId == null) {
			throw new OmhException("The MongoDB ID is missing.");
		}
		else {
			this.dbId = dbId;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmhealth.reference.data.mongodb.MongoDbObject#getDatabaseId()
	 */
	@Override
	public String getDatabaseId() {
		return dbId;
	}
}