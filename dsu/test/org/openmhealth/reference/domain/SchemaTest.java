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

import java.util.Collections;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.schema.ObjectSchema;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.openmhealth.reference.concordia.OmhValidationController;
import org.openmhealth.reference.exception.OmhException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>
 * Responsible for testing everything about the {@link Schema} class.
 * </p>
 *
 * @author John Jenkins
 */
public class SchemaTest {
	/**
	 * A valid ID to use for testing.
	 */
	public static final String ID = "omh:abc_123";
	/**
	 * A valid version to use for testing.
	 */
	public static final long VERSION = 1;
	/**
	 * The {@link Concordia} to use for testing.
	 */
	public static final Concordia CONCORDIA;
	static {
		// Build the schema as an object type with no fields.
		ObjectNode schemaRoot = new ObjectNode(JsonNodeFactory.instance);
		schemaRoot
			.put(
				name.jenkins.paul.john.concordia.schema.Schema.JSON_KEY_TYPE,
				ObjectSchema.TYPE_ID);
		schemaRoot
			.put(
				ObjectSchema.JSON_KEY_FIELDS,
				new ArrayNode(JsonNodeFactory.instance));
		// Create the schema.
		try {
			CONCORDIA =
				new Concordia(
					new ObjectSchema(
						null, 
						false, 
						null, 
						Collections.
							<name.jenkins.paul.john.concordia.schema.Schema>
							emptyList()),
					OmhValidationController.VALIDATION_CONTROLLER);
		}
		catch(OmhException | IllegalArgumentException | ConcordiaException e) {
			throw new IllegalStateException(
				"Couldn't create the default Concordia to use for the tests.",
				e);
		}
	}
	/**
	 * The owner username to use for testing.
	 */
	public static final String OWNER = "Test.User";
	/**
	 * The {@link MetaData} to use for testing.
	 */
	public static final MetaData META_DATA;
	static {
		MetaData.Builder builder = new MetaData.Builder();
		builder.setId("metaDataId");
		builder.setTimestamp(new DateTime(0, DateTimeZone.UTC));
		META_DATA = builder.build();
	}
	/**
	 * The data that conforms to the {@link #CONCORDIA} to use for testing.
	 */
	public static final JsonNode DATA =
		new ObjectNode(JsonNodeFactory.instance);

	/**
	 * Test that an exception is thrown when the ID is null.
	 */
	@Test(expected = OmhException.class)
	public void testSchemaIdNull() {
		new Schema(null, VERSION, CONCORDIA);
	}

	/**
	 * Test that an exception is thrown when the ID is an empty string.
	 */
	@Test(expected = OmhException.class)
	public void testSchemaIdEmpty() {
		new Schema("", VERSION, CONCORDIA);
	}

	/**
	 * Test that an exception is thrown when the ID is only whitespace.
	 */
	@Test(expected = OmhException.class)
	public void testSchemaIdWhitespace() {
		new Schema("\t", VERSION, CONCORDIA);
	}

	/**
	 * Test that an exception is thrown when the schema is null.
	 */
	@Test(expected = OmhException.class)
	public void testSchemaSchemaNull() {
		new Schema(ID, VERSION, null);
	}

	/**
	 * Test that a {@link Schema} object can be created when the parameters are
	 * valid.
	 */
	@Test
	public void testSchema() {
		new Schema(ID, VERSION, CONCORDIA);
	}

	/**
	 * Test that the given ID is the same as the one stored in the object.
	 */
	@Test
	public void testGetId() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		Assert.assertEquals(ID, schema.getId());
	}

	/**
	 * Test that the given version is the same as the one stored in the object.
	 */
	@Test
	public void testGetVersion() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		Assert.assertEquals(VERSION, schema.getVersion());
	}

	/**
	 * Test that validating data with a null owner throws an exception.
	 */
	@Test(expected = OmhException.class)
	public void testValidateDataOwnerNull() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		schema.validateData(null, META_DATA, DATA);
	}

	/**
	 * Test that validating data when the meta-data is null is valid.
	 */
	@Test
	public void testValidateDataMetaDataNull() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		schema.validateData(OWNER, null, DATA);
	}

	/**
	 * Test that validating data when the data is null throws an exception.
	 */
	@Test(expected = OmhException.class)
	public void testValidateDataDataNull() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		schema.validateData(OWNER, META_DATA, null);
	}

	/**
	 * Test that validating data when the data is invalid throws an exception.
	 */
	@Test(expected = OmhException.class)
	public void testValidateDataDataInvalid() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		schema.validateData(OWNER, META_DATA, BooleanNode.TRUE);
	}

	/**
	 * Test that validating valid data does not throw an exception.
	 */
	@Test
	public void testValidateData() {
		Schema schema = new Schema(ID, VERSION, CONCORDIA);
		schema.validateData(OWNER, META_DATA, DATA);
	}

	/**
	 * Test that null is not a valid ID.
	 */
	@Test(expected = OmhException.class)
	public void testValidateIdNull() {
		Schema.validateId(null);
	}

	/**
	 * Test that an empty string is not a valid ID.
	 */
	@Test(expected = OmhException.class)
	public void testValidateIdEmpty() {
		Schema.validateId("");
	}

	/**
	 * Test that whitespace is not a valid ID.
	 */
	@Test(expected = OmhException.class)
	public void testValidateIdWhitespace() {
		Schema.validateId("\t");
	}

	/**
	 * Test that a valid ID is valid.
	 */
	@Test
	public void testValidateId() {
		Schema.validateId(ID);
	}

	/**
	 * Test that a negative number is not a valid version.
	 */
	@Test(expected = OmhException.class)
	public void testValidateVersionNegative() {
		Schema.validateVersion(-1);
	}

	/**
	 * Test that zero is not a valid version.
	 */
	@Test(expected = OmhException.class)
	public void testValidateVersionZero() {
		Schema.validateVersion(0);
	}

	/**
	 * Test that a positive number is not a valid version.
	 */
	@Test
	public void testValidateVersionPositive() {
		Schema.validateVersion(1);
	}

	/**
	 * Test that a valid version is a valid version.
	 */
	@Test
	public void testValidateVersion() {
		Schema.validateVersion(VERSION);
	}
}