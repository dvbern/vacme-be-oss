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

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;

import ch.dvbern.oss.vacme.entities.base.AbstractEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.IDL;
import ch.dvbern.oss.vacme.entities.base.IDS;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "unused", "OverloadedVarargsMethod" })
public class Db {
	private final SmartEntityManager em;
	private final JPAQueryFactory queryFactory;

	public Db(EntityManager em) {
		this.em = SmartEntityManager.wrap(em);
		queryFactory = new JPAQueryFactory(em);
	}

	/**
	 * @deprecated Use {@link #get(ID)} instead.
	 */
	@Deprecated
	public <Id, Entity extends AbstractEntity<Id>> Optional<Entity> find(Class<Entity> clazz, Id id) {
		return em.find(clazz, id);
	}

	public <Entity extends AbstractEntity<UUID>> Optional<Entity> get(ID<Entity> id) {
		return em.find(id.getEntityClazz(), id.getId());
	}

	public <Entity extends AbstractEntity<String>> Optional<Entity> get(IDS<Entity> id) {
		return em.find(id.getEntityClazz(), id.getId());
	}

	public <Entity extends AbstractEntity<Long>> Optional<Entity> get(IDL<Entity> id) {
		return em.find(id.getEntityClazz(), id.getId());
	}

	public <Entity extends AbstractEntity<?>> Entity merge(Entity entity) {
		return em.merge(entity);
	}

	public <Entity extends AbstractEntity<?>> void persist(Entity entity) {
		em.persist(entity);
	}

	public <Entity extends AbstractEntity<?>> void refresh(Entity entity) {
		em.refresh(entity);
	}

	public void flush() {
		em.flush();
	}

	public BigInteger nextSequenceValue(String sequenceName) {
		var result = (BigInteger) em.createStoredProcedureQuery("nextval")
				.registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
				.setParameter(1, sequenceName)
				.getSingleResult();

		return requireNonNull(result, "No sequence value returned for sequence: " + sequenceName);
	}

	/**
	 * <strong>Remember: references will always be != null but might fail when the actual SQL query is run!</strong>
	 */
	public <Entity extends AbstractEntity<UUID>> Entity getReference(ID<Entity> id) {
		return em.getReference(id.getEntityClazz(), id.getId());
	}

	public <Entity extends AbstractEntity<?>> void remove(Entity entity) {
		em.remove(entity);
	}

	public <Entity extends AbstractEntity<UUID>> void remove(ID<Entity> id) {
		var entity = getReference(id);
		em.remove(entity);
	}

	public <Q, EPath extends EntityPathBase<Q>> List<Q> findAll(EPath path) {
		return selectFrom(path)
				.fetch();
	}

	public <TargetEntity, Q, EPath extends EntityPathBase<Q>> List<TargetEntity> findAll(
			EPath path,
			ConstructorExpression<TargetEntity> projection
	) {
		return select(projection)
				.from(path)
				.fetch();
	}

	public JPADeleteClause delete(EntityPath<?> path) {
		return queryFactory.delete(path);
	}

	public <T> ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<T> select(Expression<T> expr) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.select(expr));
	}

	public ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<Tuple> select(Expression<?>... exprs) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.select(exprs));
	}

	public <T> ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<T> selectDistinct(Expression<T> expr) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.selectDistinct(expr));
	}

	public ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<Tuple> selectDistinct(Expression<?>... exprs) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.selectDistinct(exprs));
	}

	public ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<Integer> selectOne() {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.selectOne());
	}

	public ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<Integer> selectZero() {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.selectZero());
	}

	public <T> ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<T> selectFrom(EntityPath<T> from) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.selectFrom(from));
	}

	public ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<?> from(EntityPath<?> from) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.from(from));
	}

	public ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<?> from(EntityPath<?>... from) {
		return new ch.dvbern.oss.vacme.smartdb.SmartJPAQuery<>(queryFactory.from(from));
	}

	public JPAUpdateClause update(EntityPath<?> path) {
		return queryFactory.update(path);
	}

	public SmartEntityManager getEntityManager() {
		return em;
	}
}
