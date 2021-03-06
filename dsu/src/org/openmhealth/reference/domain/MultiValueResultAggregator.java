package org.openmhealth.reference.domain;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class MultiValueResultAggregator<T> {
	/**
	 * <p>
	 * A MultiValueResult generated by aggregating existing MultiValueResults and
	 * collections.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class MultiValueResultAggregation<T>
		implements MultiValueResult<T> {
	
		/**
		 * The list of results in the order they were given.
		 */
		private final List<T> results;
		/**
		 * The number of results before paging, which is why this must be
		 * tracked independently of the actual results.
		 */
		private int count;
		
		/**
		 * Creates a MultiValueResultAggregation object with an initially empty
		 * set of data.
		 */
		private MultiValueResultAggregation() {
			results = new LinkedList<T>();
			count = 0;
		}
		
		/**
		 * A copy constructor for creating new instance of the object that will
		 * not be influenced by its builder.
		 * 
		 * @param original
		 *        The original MultiValueResultAggregation that should be used
		 *        to build this.
		 */
		private MultiValueResultAggregation(
			final MultiValueResultAggregation<T> original) {
			
			results = original.results;
			count = original.count;
		}
	
		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<T> iterator() {
			return results.iterator();
		}
	
		/*
		 * (non-Javadoc)
		 * @see org.openmhealth.reference.domain.MultiValueResult#count()
		 */
		@Override
		public int count() {
			return count;
		}
	
		/*
		 * (non-Javadoc)
		 * @see org.openmhealth.reference.domain.MultiValueResult#size()
		 */
		@Override
		public int size() {
			return results.size();
		}
		
		/**
		 * Adds the values from the given MultiValueResult to this object.
		 * 
		 * @param multiValueResult The MultiValueResul whose results should be
		 * aggregated here.
		 */
		protected void add(final MultiValueResult<T> multiValueResult) {
			// Validate the input.
			if(multiValueResult == null) {
				return;
			}
			
			// Add each of the elements.
			Iterator<T> iter = multiValueResult.iterator();
			while(iter.hasNext()) {
				results.add(iter.next());
			}
			
			// Increase the count.
			count += multiValueResult.count();
		}
		
		protected void add(final Collection<T> collection) {
			if(collection == null) {
				return;
			}
			
			// Add the elements.
			results.addAll(collection);
			
			// Increase the count.
			count += collection.size();
		}
	}
	/**
	 * The aggregator that will be built and returned.
	 */
	private final MultiValueResultAggregation<T> aggregation;
	
	/**
	 * Initializes this aggregator with no data.
	 */
	public MultiValueResultAggregator() {
		aggregation = new MultiValueResultAggregation<T>();
	}
	
	/**
	 * Initializes this aggregator with the given data as the initial data. If
	 * the parameter is null, it is the same as sending an empty multi-value
	 * result.
	 * 
	 * @param initial
	 *        This initial data for this aggregator.
	 */
	public MultiValueResultAggregator(final MultiValueResult<T> initial) {
		this();
		
		add(initial);
	}

	/**
	 * Initializes this aggregator with the given data as the initial data. If
	 * the parameter is null, it is the same as sending an empty collection.
	 * 
	 * @param initial
	 *        This initial data for this aggregator.
	 */
	public MultiValueResultAggregator(final Collection<T> initial) {
		this();
		
		add(initial);
	}
	
	/**
	 * Adds the data to this aggregator and returns itself to facilitate
	 * chaining. If the parameter is null, it is the same as sending an empty
	 * multi-value result.
	 * 
	 * @param value
	 *        The value to add to this aggregator.
	 * 
	 * @return This aggregator to facilitate chaining.
	 */
	public MultiValueResultAggregator<T> add(final MultiValueResult<T> value) {
		aggregation.add(value);
		return this;
	}
	
	/**
	 * Adds the data to this aggregator and returns itself to facilitate
	 * chaining. If the parameter is null, it is the same as sending an empty
	 * collection.
	 * 
	 * @param value
	 *        The value to add to this aggregator.
	 * 
	 * @return This aggregator to facilitate chaining.
	 */
	public MultiValueResultAggregator<T> add(final Collection<T> value) {
		aggregation.add(value);
		return this;
	}
	
	/**
	 * Builds the multi-value result and returns it.
	 * 
	 * @return The MultiValueResult object that captures all of the results
	 *         that were added.
	 */
	public MultiValueResult<T> build() {
		return aggregation;
	}
}