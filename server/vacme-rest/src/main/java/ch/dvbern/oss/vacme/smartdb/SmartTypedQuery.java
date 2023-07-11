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

package ch.dvbern.oss.vacme.smartdb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

public class SmartTypedQuery<X> implements TypedQuery<X> {

	private final TypedQuery<X> delegate;

	public SmartTypedQuery(@NonNull TypedQuery<X> delegate) {
		this.delegate = requireNonNull(delegate);
	}

	/**
	 * Liefert ein Non-Null (im SQL- *und* Java-Sinne) Result zurueck!
	 * <p>
	 * Ist also nicht geeignet, wenn das SQL-Query ein Ergebnis mit Value null liefert!
	 */
	public @NonNull
	Optional<@NonNull X> getSingleResultOpt() {
		try {
			return Optional.of(delegate.getSingleResult());
		} catch (NoResultException ignored) {
			return Optional.empty();
		}
	}

	@Override
	public @NonNull
	List<@NonNull X> getResultList() {
		return delegate.getResultList();
	}

	@Override
	public @Nullable
	X getSingleResult() {
		return delegate.getSingleResult();
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setMaxResults(int maxResult) {
		return new SmartTypedQuery<>(delegate.setMaxResults(maxResult));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setFirstResult(int startPosition) {
		return new SmartTypedQuery<>(delegate.setFirstResult(startPosition));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setHint(@NonNull String hintName, @Nullable Object value) {
		return new SmartTypedQuery<>(delegate.setHint(hintName, value));
	}

	@Override
	public <T> @NonNull SmartTypedQuery<X> setParameter(@NonNull Parameter<T> param, @Nullable T value) {
		return new SmartTypedQuery<>(delegate.setParameter(param, value));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(
			@NonNull Parameter<Calendar> param,
			@Nullable Calendar value,
			@NonNull TemporalType temporalType) {
		return new SmartTypedQuery<>(delegate.setParameter(param, value, temporalType));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(
			@NonNull Parameter<Date> param,
			@Nullable Date value,
			@NonNull TemporalType temporalType) {
		return new SmartTypedQuery<>(delegate.setParameter(param, value, temporalType));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(@NonNull String name, @Nullable Object value) {
		return new SmartTypedQuery<>(delegate.setParameter(name, value));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(
			@NonNull String name,
			@Nullable Calendar value,
			@NonNull TemporalType temporalType) {
		return new SmartTypedQuery<>(delegate.setParameter(name, value, temporalType));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(
			@NonNull String name,
			@Nullable Date value,
			@NonNull TemporalType temporalType) {
		return new SmartTypedQuery<>(delegate.setParameter(name, value, temporalType));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(int position, @Nullable Object value) {
		return new SmartTypedQuery<>(delegate.setParameter(position, value));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(
			int position,
			@Nullable Calendar value,
			@NonNull TemporalType temporalType) {
		return new SmartTypedQuery<>(delegate.setParameter(position, value, temporalType));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setParameter(
			int position,
			@Nullable Date value,
			@NonNull TemporalType temporalType) {
		return new SmartTypedQuery<>(delegate.setParameter(position, value, temporalType));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setFlushMode(@NonNull FlushModeType flushMode) {
		return new SmartTypedQuery<>(delegate.setFlushMode(flushMode));
	}

	@Override
	public @NonNull
	SmartTypedQuery<X> setLockMode(@NonNull LockModeType lockMode) {
		return new SmartTypedQuery<>(delegate.setLockMode(lockMode));
	}

	@Override
	public int executeUpdate() {
		return delegate.executeUpdate();
	}

	@Override
	public int getMaxResults() {
		return delegate.getMaxResults();
	}

	@Override
	public int getFirstResult() {
		return delegate.getFirstResult();
	}

	@Override
	public @NonNull
	Map<@NonNull String, @Nullable Object> getHints() {
		return delegate.getHints();
	}

	@Override
	public @NonNull
	Set<@NonNull Parameter<?>> getParameters() {
		return delegate.getParameters();
	}

	@Override
	public @NonNull
	Parameter<?> getParameter(@NonNull String name) {
		return delegate.getParameter(name);
	}

	@Override
	public <T> @NonNull Parameter<T> getParameter(@NonNull String name, @NonNull Class<T> type) {
		return delegate.getParameter(name, type);
	}

	@Override
	public @NonNull
	Parameter<?> getParameter(int position) {
		return delegate.getParameter(position);
	}

	@Override
	public <T> @NonNull Parameter<T> getParameter(int position, @NonNull Class<T> type) {
		return delegate.getParameter(position, type);
	}

	@Override
	public boolean isBound(@NonNull Parameter<?> param) {
		return delegate.isBound(param);
	}

	@Override
	public <T> T getParameterValue(@NonNull Parameter<T> param) {
		return delegate.getParameterValue(param);
	}

	@Override
	public @Nullable
	Object getParameterValue(@NonNull String name) {
		return delegate.getParameterValue(name);
	}

	@Override
	public @Nullable
	Object getParameterValue(int position) {
		return delegate.getParameterValue(position);
	}

	@Override
	public @NonNull
	FlushModeType getFlushMode() {
		return delegate.getFlushMode();
	}

	@Override
	public @NonNull
	LockModeType getLockMode() {
		return delegate.getLockMode();
	}

	@Override
	public <T> @NonNull T unwrap(@NonNull Class<T> cls) {
		return delegate.unwrap(cls);
	}
}
