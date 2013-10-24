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
package org.openmhealth.reference.data.mongodb;

import java.util.List;

import org.joda.time.DateTime;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.openmhealth.reference.data.DataSet;
import org.openmhealth.reference.domain.ColumnList;
import org.openmhealth.reference.domain.Data;
import org.openmhealth.reference.domain.MetaData;
import org.openmhealth.reference.domain.MultiValueResult;
import org.openmhealth.reference.domain.Schema;
import org.openmhealth.reference.domain.mongodb.MongoData;
import org.openmhealth.reference.domain.mongodb.MongoMultiValueResultCursor;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The interface to the database-backed set of data.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoDataSet extends DataSet {
	/**
	 * Default constructor.
	 */
	protected MongoDataSet() {
		// Get the collection to add indexes to.
		DBCollection collection =
			MongoDao.getInstance().getDb().getCollection(DB_NAME);
		
		// Ensure that there is an index on the token.
		collection
			.ensureIndex(
				new BasicDBObject(Data.JSON_KEY_OWNER, 1),
				DB_NAME + "_" + Data.JSON_KEY_OWNER + "_index",
				false);
		
		// Ensure that there is an index on the schema's ID.
		collection
			.ensureIndex(
				new BasicDBObject(Schema.JSON_KEY_ID, 1),
				DB_NAME + "_" + Schema.JSON_KEY_ID + "_index",
				false);
		// Ensure that there is an index on the schema's version.
		collection
			.ensureIndex(
				new BasicDBObject(Schema.JSON_KEY_VERSION, 1),
				DB_NAME + "_" + Schema.JSON_KEY_VERSION + "_index",
				false);
		
		// Build the index for sorting.
		String sortingFields = 
			Data.JSON_KEY_METADATA + 
				ColumnList.COLUMN_SEPARATOR + 
				MetaData.JSON_KEY_TIMESTAMP;
		collection.ensureIndex(
			new BasicDBObject(sortingFields, 1),
			DB_NAME + "_" + sortingFields + "_index",
			false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openmhealth.reference.data.DataSet#setData(java.util.List)
	 */
	@Override
	public void storeData(final List<Data> data) {
		// Get the connection to the database.
		DB db = MongoDao.getInstance().getDb();
		
		// Get the connection to the data with the Jackson wrapper.
		JacksonDBCollection<Data, Object> collection =
			JacksonDBCollection
				.wrap(db.getCollection(DB_NAME), Data.class);
		
		// Insert the data.
		collection.insert(data);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmhealth.reference.data.DataSet#getData(java.lang.String, java.lang.String, long, org.joda.time.DateTime, org.joda.time.DateTime, org.openmhealth.reference.domain.ColumnList, long, long)
	 */
	@Override
	public MultiValueResult<Data> getData(
		final String owner,
		final String schemaId,
		final long version,
		final DateTime startDate,
		final DateTime endDate,
		final ColumnList columnList,
		final long numToSkip,
		final long numToReturn) {
		
		// Get the connection to the database.
		DB db = MongoDao.getInstance().getDb();
		
		// Get the connection to the data with the Jackson wrapper.
		JacksonDBCollection<MongoData, Object> collection =
			JacksonDBCollection
				.wrap(db.getCollection(DB_NAME), MongoData.class);
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Only select data for a single user.
		queryBuilder.and(Data.JSON_KEY_OWNER).is(owner);
		
		// Only select data for a given schema.
		queryBuilder.and(Schema.JSON_KEY_ID).is(schemaId);
		
		// Only select data for a given version of the the given schema.
		queryBuilder.and(Schema.JSON_KEY_VERSION).is(version);
		
		// Only select data on or after the start date.
		if(startDate != null) {
			queryBuilder
				.and(
					Data.JSON_KEY_METADATA +
						"." +
						MetaData.JSON_KEY_TIMESTAMP)
				.greaterThan(startDate.toString());
		}
		
		// Only select data on or before the end date.
		if(endDate != null) {
			queryBuilder
				.and(
					Data.JSON_KEY_METADATA +
						"." +
						MetaData.JSON_KEY_TIMESTAMP)
				.greaterThan(endDate.toString());
		}
		
		// Create the projection.
		DBObject projection = new BasicDBObject();
		// Add the owner field.
		projection.put(Data.JSON_KEY_OWNER, 1);
		// Add the schema ID field.
		projection.put(Schema.JSON_KEY_ID, 1);
		// Add the schema version.
		projection.put(Schema.JSON_KEY_VERSION, 1);
		// Add the meta-data field.
		projection.put(Data.JSON_KEY_METADATA, 1);
		// Add all of the data or add only the specified columns if given.
		if(columnList.size() == 0) {
			projection.put(Data.JSON_KEY_DATA, 1);
		}
		else {
			if((columnList != null) && (columnList.size() > 0)) {
				for(String column : columnList.toList()) {
					projection
						.put(
							Data.JSON_KEY_DATA +
								ColumnList.COLUMN_SEPARATOR +
								column,
							1);
				}
			}
		}
		
		// Build the query.
		DBCursor<MongoData> dbResult =
			collection.find(queryBuilder.get(), projection);
		
		// Build the sort field by sorting in reverse chronological order.
		DBObject sort = new BasicDBObject();
		sort.put(
			Data.JSON_KEY_METADATA + 
				ColumnList.COLUMN_SEPARATOR + 
				MetaData.JSON_KEY_TIMESTAMP,
			-1);
		dbResult.sort(sort);
		
		// Page the results and return the multi-value result.
		return
			new MongoMultiValueResultCursor<Data>(
				dbResult
					.skip((new Long(numToSkip)).intValue())
					.limit((new Long(numToReturn)).intValue()));
	}
}