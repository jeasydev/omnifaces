/*
 * Copyright 2013 OmniFaces.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.omnifaces.el.functions;

import static org.omnifaces.util.Utils.isEmpty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.model.DataModel;

import org.omnifaces.model.IterableDataModel;
import org.omnifaces.util.Json;
import org.omnifaces.util.Utils;

/**
 * Collection of EL functions for data conversion.
 *
 * @author Bauke Scholtz
 * @author Arjan Tijms
 * @author Radu Creanga <rdcrng@gmail.com>
 */
public final class Converters {

	// Constants ------------------------------------------------------------------------------------------------------

	private static final String ERROR_NOT_AN_ARRAY = "The given type '%' is not an array at all.";
    private static final String ERROR_INVALID_FRAGMENT_SIZE = "The given fragment size '%s' must be at least 1.";

	// Constructors ---------------------------------------------------------------------------------------------------

	private Converters() {
		// Hide constructor.
	}

	// Utility --------------------------------------------------------------------------------------------------------

	/**
	 * Converts a <code>Set&lt;E&gt;</code> to a <code>List&lt;E&gt;</code>. Useful when you want to iterate over a
	 * <code>Set</code> in for example <code>&lt;ui:repeat&gt;</code>.
	 * @param set The set to be converted to list of its entries.
	 * @return The converted list.
	 */
	public static <E> List<E> setToList(Set<E> set) {
		if (set == null) {
			return null;
		}

		return new ArrayList<E>(set);
	}

	/**
	 * Converts a <code>Map&lt;K, V&gt;</code> to a <code>List&lt;Map.Entry&lt;K, V&gt;&gt;</code>. Useful when you want
	 * to iterate over a <code>Map</code> in for example <code>&lt;ui:repeat&gt;</code>. Each of the entries has the
	 * usual <code>getKey()</code> and <code>getValue()</code> methods.
	 * @param map The map to be converted to list of its entries.
	 * @return The converted list.
	 */
	public static <K, V> List<Map.Entry<K, V>> mapToList(Map<K, V> map) {
		if (map == null) {
			return null;
		}

		return new ArrayList<Map.Entry<K, V>>(map.entrySet());
	}

	/**
	 * Converts a <code>Iterable&lt;E&gt;</code> to a <code>List&lt;E&gt;</code>. Useful when you want to iterate over an
	 * <code>Iterable</code>, which includes any type of <code>Collection</code> (which includes e.g. a <code>Set</code>)
	 * in for example <code>&lt;ui:repeat&gt;</code> and <code>&lt;h:dataTable&gt;</code>.
	 * <p>
	 * When iterating specifically over a Set using the above mentioned components {@link Converters#setToList(Set)} is
	 * an alternative to this.
	 *
	 * @param iterable The Iterable to be converted to a List.
	 * @return The converted List.
	 *
	 * @since 1.5
	 */
	public static <E> List<E> iterableToList(Iterable<E> iterable) {
		if (iterable == null) {
			return null;
		}

		return Utils.iterableToList(iterable);
	}

	/**
	 * Converts an <code>Iterable&lt;E&gt;</code> to a <code>DataModel&lt;E&gt;</code>. Useful when you want to iterate over an
	 * <code>Iterable</code>, which includes any type of <code>Collection</code> (which includes e.g. a <code>Set</code>)
	 * in for example <code>&lt;ui:repeat&gt;</code> and <code>&lt;h:dataTable&gt;</code>.
	 * <p>
	 * When iterating specifically over a Set using the above mentioned components {@link Converters#setToList(Set)} is
	 * an alternative to this. Use this for more general cases or when the exact collection type is unknown.
	 * <p>
	 * For those same components {@link Converters#iterableToList(Iterable)} is another alternative. Use this when
	 * a DataModel is specifically needed.
	 *
	 * @param iterable The Iterable to be converted to a DataModel.
	 * @return The converted DataModel.
	 *
	 * @since 1.5
	 */
	public static <E> DataModel<E> iterableToModel(Iterable<E> iterable) {
		if (iterable == null) {
			return null;
		}

		return new IterableDataModel<E>(iterable);
	}

	/**
	 * Joins all elements of the given array to a single string, separated by the given separator.
	 * @param array The array to be joined.
	 * @param separator The separator to be used. If null, then it defaults to empty string.
	 * @return All elements of the given array as a single string, separated by the given separator.
	 * @throws IllegalArgumentException When the given array is not an array at all.
	 * @since 1.3
	 */
	public static String joinArray(Object array, String separator) {
		if (array == null) {
			return null;
		}

		if (!array.getClass().isArray()) {
			throw new IllegalArgumentException(String.format(ERROR_NOT_AN_ARRAY, array.getClass()));
		}

		if (separator == null) {
			separator = "";
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < Array.getLength(array); i++) {
			if (i > 0) {
				builder.append(separator);
			}

			builder.append(Array.get(array, i));
		}

		return builder.toString();
	}

	/**
	 * Joins all elements of the given collection to a single string, separated by the given separator.
	 * @param collection The collection to be joined.
	 * @param separator The separator to be used. If null, then it defaults to empty string.
	 * @return All elements of the given collection as a single string, separated by the given separator.
	 * @since 1.3
	 */
	public static <E> String joinCollection(Collection<E> collection, String separator) {
		if (collection == null) {
			return null;
		}

		if (separator == null) {
			separator = "";
		}

		StringBuilder builder = new StringBuilder();
		int i = 0;

		for (E element : collection) {
			if (i++ > 0) {
				builder.append(separator);
			}

			builder.append(element);
		}

		return builder.toString();
	}

	/**
	 * Joins all elements of the given map to a single string, separated by the given key-value pair separator and
	 * entry separator.
	 * @param map The map to be joined.
	 * @param pairSeparator The key-value pair separator to be used. If null, then it defaults to empty string.
	 * @param entrySeparator The entry separator to be used. If null, then it defaults to empty string.
	 * @return All elements of the given map as a single string, separated by the given separators.
	 * @since 1.3
	 */
	public static <K, V> String joinMap(Map<K, V> map, String pairSeparator, String entrySeparator) {
		if (map == null) {
			return null;
		}

		if (pairSeparator == null) {
			pairSeparator = "";
		}

		if (entrySeparator == null) {
			entrySeparator = "";
		}

		StringBuilder builder = new StringBuilder();
		int i = 0;

		for (Entry<K, V> entry : map.entrySet()) {
			if (i++ > 0) {
				builder.append(entrySeparator);
			}

			builder.append(entry.getKey()).append(pairSeparator).append(entry.getValue());
		}

		return builder.toString();
	}

	/**
	 * Splits the given array into an array of subarrays of the given fragment size. This is useful for creating nested
	 * <code>&lt;ui:repeat&gt;</code> structures, for example, when positioning a list of items into a grid based
	 * layout system such as Twitter Bootstrap.
	 * @param array The array to be split.
	 * @param fragmentSize The size of each subarray.
	 * @return A new array consisting of subarrays of the given array.
	 * @throws IllegalArgumentException When the fragment size is less than 1.
	 * @since 1.6
	 */
	public static Object[][] splitArray(Object array, int fragmentSize) {
		if (isEmpty(array)) {
			return new Object[0][];
		}

		if (!array.getClass().isArray()) {
			throw new IllegalArgumentException(String.format(ERROR_NOT_AN_ARRAY, array.getClass()));
		}

		if (fragmentSize < 1) {
			throw new IllegalArgumentException(String.format(ERROR_INVALID_FRAGMENT_SIZE, fragmentSize));
		}

		int sourceSize = Array.getLength(array);
		Object[][] arrays = new Object[(sourceSize + fragmentSize - 1) / fragmentSize][];

		for (int i = 0, j = 0; i < sourceSize; i += fragmentSize, j++) {
			arrays[j] = new Object[Math.min(fragmentSize, sourceSize - i)];
			System.arraycopy(array, i, arrays[j], 0, arrays[j].length);
		}

		return arrays;
	}

	/**
	 * Splits the given list into a list of sublists of the given fragment size. This is useful for creating nested
	 * <code>&lt;ui:repeat&gt;</code> structures, for example, when positioning a list of items into a grid based
	 * layout system such as Twitter Bootstrap.
	 * @param list The list to be split.
	 * @param fragmentSize The size of each sublist.
	 * @return A new list consisting of sublists of the given list.
	 * @throws IllegalArgumentException When the fragment size is less than 1.
	 * @since 1.6
	 */
	public static <T> List<List<T>> splitList(List<T> list, int fragmentSize) {
		if (isEmpty(list)) {
			return Collections.emptyList();
		}

		if (fragmentSize < 1) {
			throw new IllegalArgumentException(String.format(ERROR_INVALID_FRAGMENT_SIZE, fragmentSize));
		}

		int sourceSize = list.size();
		List<List<T>> lists = new ArrayList<List<T>>((sourceSize + fragmentSize - 1) / fragmentSize);

		for (int i = 0; i < sourceSize; i += fragmentSize) {
			lists.add(list.subList(i, Math.min(i + fragmentSize, sourceSize)));
		}

		return lists;
	}

    /**
	 * Encode given object as JSON.
	 * Currently, this delegates directly to {@link Json#encode(Object)}.
	 * @param object Object to be encoded as JSON.
	 * @return The encoded JSON string.
	 * @see Json#encode(Object)
	 * @since 1.5
	 */
	public static String toJson(Object object) {
		return Json.encode(object);
	}

	/**
	 * Print the stack trace of the given exception.
	 * @param exception The exception to print the stack trace for.
	 * @return The printed stack trace.
	 */
	public static String printStackTrace(Throwable exception) {
		if (exception == null) {
			return null;
		}

		StringWriter stringWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stringWriter, true));
		return stringWriter.toString();
	}

}