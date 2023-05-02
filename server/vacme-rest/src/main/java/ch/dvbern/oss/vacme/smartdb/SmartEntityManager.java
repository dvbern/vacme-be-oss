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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class SmartEntityManager {
	private final EntityManager delegate;

	private SmartEntityManager(@NonNull EntityManager delegate) {
		this.delegate = requireNonNull(delegate);
	}

	public static SmartEntityManager wrap(EntityManager delegate) {
		return new SmartEntityManager(delegate);
	}

	public EntityManager em() {
		return delegate;
	}

	public void persist(Object entity) {
		delegate.persist(entity);
	}

	public <T> @NonNull T merge(@NonNull T entity) {
		return delegate.merge(entity);
	}

	/**
	 * Like {@link #merge(Object)} but calls {@link #flush()} thereafter.
	 */
	public <T> @NonNull T mergeSync(@NonNull T entity) {
		T result = merge(entity);
		flush();

		return result;
	}

	public void remove(Object entity) {
		delegate.remove(entity);
	}

	public <T> @NonNull Optional<@NonNull T> find(Class<T> entityClass, Object primaryKey) {
		return ofNullable(delegate.find(entityClass, primaryKey));
	}

	public <T> @NonNull Optional<@NonNull T> find(
			Class<T> entityClass, Object primaryKey, Map<String, Object>
			properties) {

		return ofNullable(delegate.find(entityClass, primaryKey, properties));
	}

	public <T> @NonNull Optional<@NonNull T> find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		return ofNullable(delegate.find(entityClass, primaryKey, lockMode));
	}

	public <T> @NonNull Optional<@NonNull T> find(
			Class<T> entityClass, Object primaryKey, LockModeType lockMode,
			Map<String, Object> properties) {

		return ofNullable(delegate.find(entityClass, primaryKey, lockMode, properties));
	}

	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		return delegate.getReference(entityClass, primaryKey);
	}

	public void flush() {
		delegate.flush();
	}

	public void setFlushMode(FlushModeType flushMode) {
		delegate.setFlushMode(flushMode);
	}

	public FlushModeType getFlushMode() {
		return delegate.getFlushMode();
	}

	public void lock(Object entity, LockModeType lockMode) {
		delegate.lock(entity, lockMode);
	}

	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		delegate.lock(entity, lockMode, properties);
	}

	public void refresh(Object entity) {
		delegate.refresh(entity);
	}

	public void refresh(Object entity, Map<String, Object> properties) {
		delegate.refresh(entity, properties);
	}

	public void refresh(Object entity, LockModeType lockMode) {
		delegate.refresh(entity, lockMode);
	}

	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		delegate.refresh(entity, lockMode, properties);
	}

	public void clear() {
		delegate.clear();
	}

	public void detach(Object entity) {
		delegate.detach(entity);
	}

	public boolean contains(Object entity) {
		return delegate.contains(entity);
	}

	public LockModeType getLockMode(Object entity) {
		return delegate.getLockMode(entity);
	}

	public void setProperty(String propertyName, Object value) {
		delegate.setProperty(propertyName, value);
	}

	public Map<String, Object> getProperties() {
		return delegate.getProperties();
	}

	public Query createQuery(String qlString) {
		return delegate.createQuery(qlString);
	}

	public <T> ch.dvbern.oss.vacme.smartdb.SmartTypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return new ch.dvbern.oss.vacme.smartdb.SmartTypedQuery<>(delegate.createQuery(criteriaQuery));
	}

	public Query createQuery(CriteriaUpdate<?> updateQuery) {
		return delegate.createQuery(updateQuery);
	}

	public Query createQuery(CriteriaDelete<?> deleteQuery) {
		return delegate.createQuery(deleteQuery);
	}

	public <T> ch.dvbern.oss.vacme.smartdb.SmartTypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return new ch.dvbern.oss.vacme.smartdb.SmartTypedQuery<>(delegate.createQuery(qlString, resultClass));
	}

	public Query createNamedQuery(String name) {
		return delegate.createNamedQuery(name);
	}

	public <T> ch.dvbern.oss.vacme.smartdb.SmartTypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return new ch.dvbern.oss.vacme.smartdb.SmartTypedQuery<>(delegate.createNamedQuery(name, resultClass));
	}

	public Query createNativeQuery(String sqlString) {
		return delegate.createNativeQuery(sqlString);
	}

	public Query createNativeQuery(String sqlString, Class<?> resultClass) {
		return delegate.createNativeQuery(sqlString, resultClass);
	}

	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return delegate.createNativeQuery(sqlString, resultSetMapping);
	}

	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		return delegate.createNamedStoredProcedureQuery(name);
	}

	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		return delegate.createStoredProcedureQuery(procedureName);
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class<?>... resultClasses) {
		return delegate.createStoredProcedureQuery(procedureName, resultClasses);
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		return delegate.createStoredProcedureQuery(procedureName, resultSetMappings);
	}

	public void joinTransaction() {
		delegate.joinTransaction();
	}

	public boolean isJoinedToTransaction() {
		return delegate.isJoinedToTransaction();
	}

	public <T> T unwrap(Class<T> cls) {
		return delegate.unwrap(cls);
	}

	public Object getDelegate() {
		return delegate.getDelegate();
	}

	public void close() {
		delegate.close();
	}

	public boolean isOpen() {
		return delegate.isOpen();
	}

	public EntityTransaction getTransaction() {
		return delegate.getTransaction();
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return delegate.getEntityManagerFactory();
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return delegate.getCriteriaBuilder();
	}

	public Metamodel getMetamodel() {
		return delegate.getMetamodel();
	}

	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		return delegate.createEntityGraph(rootType);
	}

	public EntityGraph<?> createEntityGraph(String graphName) {
		return delegate.createEntityGraph(graphName);
	}

	public EntityGraph<?> getEntityGraph(String graphName) {
		return delegate.getEntityGraph(graphName);
	}

	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		return delegate.getEntityGraphs(entityClass);
	}
}
