/*******************************************************************************
 * Copyright 2013 Open mHealth
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

import java.io.InputStream;
import java.util.regex.Pattern;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.validator.ValidationController;

import org.openmhealth.reference.exception.OmhException;
import org.openmhealth.shim.exception.ShimSchemaException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>
 * A schema as defined by the Open mHealth specification.
 * </p>
 *
 * <p>
 * This class is immutable.
 * </p>
 *
 * @author John Jenkins
 */
public class Schema implements OmhObject {
	/**
	 * The version of this class for serialization purposes.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The pattern to use for validating schema IDs.
	 */
	private static final Pattern PATTERN_ID =
		Pattern.compile("omh(:[a-zA-Z0-9_-]+)+");

	/**
	 * The JSON key for the ID of a schema.
	 */
	public static final String JSON_KEY_ID = "schema_id";
	/**
	 * The JSON key for the version of the schema.
	 */
	public static final String JSON_KEY_VERSION = "schema_version";
	/**
	 * The JSON key for the Concordia schema.
	 */
	public static final String JSON_KEY_SCHEMA = "schema";
	/**
	 * The JSON key for the {@link ValidationController} that will be used to
	 * build the underlying {@link Concordia} object. If not set or not given,
	 * the default one, {@link ValidationController#BASIC_CONTROLLER} will be
	 * used.
	 */
	public static final String JSON_KEY_VALIDATION_CONTROLLER =
		"validation_controller";

	/**
	 * The schema's ID.
	 */
	@JsonProperty(JSON_KEY_ID)
	private final String id;
	/**
	 * The schema's version.
	 */
	@JsonProperty(JSON_KEY_VERSION)
	private final long version;
	/**
	 * The actual schema for this {@link Schema} object.
	 */
	@JsonProperty(JSON_KEY_SCHEMA)
	private final Concordia schema;

	/**
	 * Creates a new schema (registry entry).
	 *
	 * @param id
	 *        The ID for this schema.
	 *
	 * @param version
	 *        The version of this schema.
	 *
	 * @param schema
	 *        The Concordia schema that defines this Schema object.
	 *
	 * @throws OmhException
	 *         A parameter was invalid.
	 */
	@JsonCreator
	public Schema(
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_VERSION) final long version,
		@JsonProperty(JSON_KEY_SCHEMA) final Concordia schema)
		throws OmhException {

		// Validate the ID.
		if(id == null) {
			throw new OmhException("The ID is null.");
		}
		else if(id.trim().length() == 0) {
			throw new OmhException("The ID is empty.");
		}
		else {
			this.id = validateId(id);
		}

		// Validate the version.
		this.version = validateVersion(version);

		// Make sure the schema is not null.
		if(schema == null) {
			throw new OmhException("The schema is null.");
		}
		else {
			this.schema = schema;
		}
	}

	/**
	 * Returns the unique identifier for this schema.
	 *
	 * @return The unique identifier for this schema.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the version of this schema.
	 *
	 * @return The version of this schema.
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Returns the schema.
	 *
	 * @return The schema.
	 */
	public Concordia getSchema() {
		return schema;
	}

	/**
	 * Validates some data.
	 *
	 * @param owner
	 *        The owner of the data that is being validated. This is needed to
	 *        build the {@link Data} object.
	 *
	 * @param metaData
	 *        The meta-data for the data that is being validated. This is
	 *        needed to build the {@link Data} object.
	 *
	 * @param data
	 *        The data to be validated.
	 *
	 * @return The validated data as a {@link Data} object.
	 *
	 * @throws OmhException
	 *         The data was null or invalid.
	 */
	public Data validateData(
		final String owner,
		final MetaData metaData,
		final JsonNode data)
		throws OmhException {

		// Ensure the data is not null.
		if(data == null) {
			throw new OmhException("The data field is null.");
		}

		// Validate the data.
		try {
			schema.validateData(data);
		}
		catch(ConcordiaException e) {
			throw new OmhException("The data is invalid.", e);
		}

		// Return the result.
		return new Data(owner, id, version, metaData, data);
	}

	/**
	 * Validates that the ID follows our rules.
	 *
	 * @param id
	 *        The ID to be validated.
	 *
	 * @return The validated and, potentially, simplified schema, e.g. trimmed.
	 *
	 * @throws OmhException
	 *         The ID is invalid.
	 */
	public static String validateId(final String id) throws OmhException {
		// Validate that the ID is not null.
		if(id == null) {
			throw new OmhException("The ID is null.");
		}

		// Remove surrounding whitespace.
		String idTrimmed = id.trim();

		// Validate that the ID is not empty or only whitespace.
		if(idTrimmed.length() == 0) {
			throw new OmhException("The ID is empty or only whitespace.");
		}

		// Validate that the trimmed ID matches the pattern.
		if(! PATTERN_ID.matcher(idTrimmed).matches()) {
			throw
				new OmhException(
					"The schema ID is invalid. It must be colon " +
						"deliminated, alphanumeric sections, with or " +
						"without underscores, where the first section is " +
						"\"omh\": " +
						idTrimmed);
		}

		// Return the trimmed ID.
		return idTrimmed;
	}

	/**
	 * Validates that the version follows our rules.
	 *
	 * @param version
	 *        The version to be validated.
	 *
	 * @return The version as it was given.
	 *
	 * @throws OmhException
	 *         The version is invalid.
	 */
	public static long validateVersion(
		final long version)
		throws OmhException {

		// The version must be positive.
		if(version <= 0) {
			throw new OmhException("The version must be positive.");
		}

		return version;
	}

	/**
	 * Validates that the chunk size follows our rules.
	 *
	 * @param chunkSize
	 *        The chunk size to validate.
	 *
	 * @return The chunk size as it was given.
	 *
	 * @throws OmhException
	 *         The chunk size is invalid.
	 */
	public static long validateChunkSize(
		final long chunkSize)
		throws OmhException {

		// The chunk size must be positive.
		if(chunkSize <= 0) {
			throw new OmhException("The chunk size must be positive.");
		}

		return chunkSize;
	}

    /**
     * Splits a schema ID into an array of strings.
     */
    private static String[] splitSchemaId(final String id) {
        if (id == null) {
            throw new OmhException("id is null");
        }

        String[] parts = id.split(":");

        if (parts.length != 3) {
            throw new OmhException("Invalid schema id: " + id);
        }

        return parts;
    }

    /**
     * Given a schema ID, returns the data type. For example, given
     * 'omh:fitbit:activity', will return 'activity'.
     *
     * @param id
     *        The schema ID.
     *
     * @return The data type.
     */
    public static String dataTypeFromSchemaId(final String id) {
        return splitSchemaId(id)[2];
    }

    /**
     * Given a schema ID, returns the domain. For example, given
     * 'omh:fitbit:activity', will return 'fitbit'.
     *
     * @param id
     *        The schema ID.
     *
     * @return The domain.
     */
    public static String domainFromSchemaId(final String id) {
        return splitSchemaId(id)[1];
    }

    /**
     * Loads a Schema object for a schema ID. Schemas are read from JSON
     * files found on the classpath. For example, the schema for version 1 of
     * 'omh:fitbit:activity' would be found in a file called
     * 'schema/fitbit/1/activity.json'.
     *
	 * @param id
	 *        The schema ID.
	 * 
	 * @param version
	 *        The schema version.
	 * 
	 * @return The schema for the given schema-ID version pair or null if the
	 *         pair is unknown.
	 * 
	 * @throws ShimSchemaException
	 *         The ID and/or version are null.
     */
    public static Schema loadFromFile(
        final String id,
        final Long version) {
        if (id == null) {
            throw new OmhException("The given schema ID is null.");
        }
        if (version == null) {
            throw new OmhException("The given schema version is null.");
        }

        String schemaResourcePath =
            "schema/" + domainFromSchemaId(id) + "/" + version + "/"
            + dataTypeFromSchemaId(id) + ".json";

        // Load and parse the schema from the schema file.
        InputStream schemaStream =
            Schema.class.getClassLoader()
                .getResourceAsStream(schemaResourcePath);
        Concordia concordia = null;
        try {
            concordia = new Concordia(schemaStream);
        }
        catch(Exception e) {
            throw new OmhException("Error reading schema.", e);
        }
                
        return new Schema(id, version.longValue(), concordia);
    }
}