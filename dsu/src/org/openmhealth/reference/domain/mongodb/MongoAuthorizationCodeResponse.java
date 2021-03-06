package org.openmhealth.reference.domain.mongodb;

import org.openmhealth.reference.domain.AuthorizationCodeResponse;
import org.openmhealth.reference.exception.OmhException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The MongoDB-specific variant of an {@link AuthorizationCodeResponse}
 * object.
 * </p>
 * 
 * <p>
 * This class is immutable.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoAuthorizationCodeResponse
	extends AuthorizationCodeResponse
	implements MongoDbObject {

	/**
	 * The ID for this class which is used for serialization. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The database ID for this object.
	 */
	@JsonIgnore
	private final String dbId;

	/**
	 * Used for deserializing a MongoDB-variant of an
	 * {@link AuthorizationCodeResponse} entity.
	 * 
	 * @param dbId
	 * 		  The MongoDB ID for this entity.
	 * 
	 * @param authorizationCode
	 *        The authorization code to which this response applies.
	 * 
	 * @param owner
	 *        The ID for the user that granted or rejected this authorization
	 *        code.
	 * 
	 * @param granted
	 *        Whether or not the authorization is granted.
	 * 
	 * @throws OmhException
	 *         A parameter is invalid.
	 */
	public MongoAuthorizationCodeResponse(
		@JsonProperty(DATABASE_FIELD_ID) final String dbId,
		@JsonProperty(JSON_KEY_AUTHORIZATION_CODE)
			final String authorizationCode,
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_GRANTED) final boolean granted)
		throws OmhException {
		
		super(authorizationCode, owner, granted);
		
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