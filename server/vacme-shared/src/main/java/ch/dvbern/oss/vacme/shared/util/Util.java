/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.shared.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public final class Util {
	private Util() {
	}

	/**
	 * Hilfsmethode fuer toString(): Wenn beim Debugging eine JPA-Referenz schon detached ist,
	 * kann nicht mehr auf den Wert zugegriffen werden und es kommt eine Exception.
	 * Diese Methode faengt die Exception ab und gibt einen fixen Text zurueck.
	 * <pre>
	 * {@code
	 * 	public String toString() {
	 * 		return MoreObjects.toStringHelper(this)
	 * 			.add("id", getId())
	 * 			.add("kontaktperson", getSilent(() -> kontaktperson))
	 * 			.add("kind", getSilent(() -> kind))
	 * 			.toString();
	 *    }
	 * }
	 * </pre>
	 */
	public static <T> String getSilent(Supplier<T> supplier) {
		try {
			return String.valueOf(supplier.get());
		} catch (RuntimeException ignored) {
			return "<unknown>";
		}
	}

	/**
	 * @see #getSilent(Supplier)
	 */
	public static <T, S> String getMapSilent(
			Supplier<Map<T, S>> supplier) {

		try {
			return String.valueOf(supplier.get());
		} catch (RuntimeException ignored) {
			return "<unknown>";
		}
	}

	public static URL parseURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Not a valid URL: " + url, e);
		}
	}

	public static URI parseURI(String uri) {
		return URI.create(uri);
	}

	/**
	 * Find an enum entry by the given predicate.
	 */
	public static <E extends Enum<?>> Optional<E> findEnumBy(Class<E> enumClass, Predicate<E> predicate) {
		return Arrays.stream(requireNonNull(enumClass.getEnumConstants()))
				.filter(predicate)
				.findFirst();
	}

	/**
	 * See {@link #findEnumBy(Class, Predicate)} but throws {@link IllegalArgumentException} if no entry was found.
	 */
	public static <E extends Enum<?>> E getEnum(Class<E> enumClass, Predicate<E> predicate) {
		return findEnumBy(enumClass, predicate)
				.orElseThrow(() -> new IllegalArgumentException(
						"unable to find enum " + enumClass.getName() + " for predicate"));
	}

	/**
	 * Return the first Non-Null parameter or {@link Optional#empty()}.
	 */
	@SuppressWarnings("varargs")
	@SafeVarargs
	public static <T> Optional<@Nullable T> coalesce(T... values) {
		return Arrays.stream(values)
				.filter(Objects::nonNull)
				.findFirst();
	}

	/**
	 * Nicer replacement for the ternary operator.
	 * <p>
	 * Please note: eager evalation! For a lazy version, see {@link #ifNull(Object, Supplier)}.
	 * </p>
	 * <p>
	 * Example:
	 * <pre>
	 * {@code
	 * // Old style:
	 * Object foo = (bar != null ? bar : new OtherObject())
	 *
	 * // New style:
	 * Object foo = ifNull(bar, new OtherObject());
	 * }
	 * </pre>
	 */
	public static <T> T ifNull(@Nullable T value, T valueIfNull) {
		return value != null ? value : valueIfNull;
	}

	/**
	 * Nicer replacement for the ternary operator.
	 * <p>
	 * Example:
	 * <pre>
	 * {@code
	 * // Old style:
	 * Object foo = (bar != null ? bar : new OtherObject())
	 *
	 * // New style:
	 * Object foo = ifNull(bar, () -> new OtherObject());
	 * }
	 * </pre>
	 */
	public static <T> T ifNull(@Nullable T value, Supplier<T> valueIfNull) {
		return value != null ? value : valueIfNull.get();
	}

	public static <T, R> @Nullable R ifValue(@Nullable T value, Function<T, @Nullable R> resolver) {
		if (value == null) {
			return null;
		}

		return resolver.apply(value);
	}

	public static <T, R> R ifValue(@Nullable T value, Function<T, R> resolver, Supplier<R> fallback) {
		if (value == null) {
			return fallback.get();
		}

		return resolver.apply(value);
	}

	/**
	 * Return the forst Non-Null/Non-Blank String or {@link Optional#empty()}.
	 */
	public static Optional<String> coalesceString(@Nullable String... values) {
		return Arrays.stream(values)
				.map(StringUtils::trimToNull)
				.filter(Objects::nonNull)
				.findFirst();
	}

	/**
	 * Format a collection of data in the following way:
	 * If there is only one entry: return the value.
	 * If there is more than one entry: print the first entry, followed by the prefix, all remaining entries joined
	 * by the infix and finalle the suffix.
	 * <p>
	 * Example: "first (foo, bar, baz)"
	 */
	public static <T> String formatFirstWithOptionalOthers(
			Collection<T> data,
			Function<T, ?> valueExtractor,
			String surplusPrefix,
			String surplusInfix,
			String surplusSuffix
	) {
		if (data.isEmpty()) {
			return "";
		}

		List<String> distinct = data.stream()
				.map(valueExtractor)
				.map(String::valueOf)
				.distinct()
				.collect(Collectors.toList());

		String first = distinct.get(0);

		if (distinct.size() == 1) {
			return first;
		}

		String others = distinct.stream()
				.skip(1)
				.collect(Collectors.joining(surplusInfix));

		return first + surplusPrefix + others + surplusSuffix;
	}

	/**
	 * Convenience: calls {@link #formatFirstWithOptionalOthers(Collection, Function, String, String, String)}
	 * with prefix: "(", infix: "," suffix: ")".
	 */
	public static <T> String formatFirstWithOptionalOthers(
			Collection<T> data,
			Function<T, ?> valueExtractor
	) {
		return formatFirstWithOptionalOthers(data, valueExtractor, " (", ", ", ")");
	}

	public static <T> Optional<T> findDuplicate(Iterable<T> all) {
		Set<T> set = new HashSet<>();

		for (T each : all) {
			if (!set.add(each)) {
				return Optional.of(each);
			}
		}
		return Optional.empty();
	}

	/**
	 * Convenience: get the current ContextClassLoader.
	 */
	public static ClassLoader contextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	@SafeVarargs
	public static <@Nullable T> boolean isIn(@Nullable T needle, T... haystack) {
		for (T t : haystack) {
			if (Objects.equals(needle, t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Turn a collection, which will be filtered before into a Map.
	 */
	public static <Key, T> Map<Key, T> toMap(
			Collection<T> collection,
			Function<T, Key> keyExtractor,
			Predicate<T> filter
	) {
		var map = collection.stream()
				.filter(filter)
				.collect(Collectors.toMap(keyExtractor, identity()));

		return map;
	}

	/**
	 * Turn a collection into a map.
	 */
	public static <Key, T> Map<Key, T> toMap(Collection<T> collection, Function<T, Key> keyExtractor) {
		var map = collection.stream()
				.collect(Collectors.toMap(keyExtractor, identity()));

		return map;
	}

	/**
	 * Turn a collection into a map using the objects identity as map-key.
	 */
	public static <V> Map<V, V> toMap(Collection<V> collection) {
		return toMap(collection, identity());
	}

}
