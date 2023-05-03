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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.QueryResults;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MapExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.jpa.impl.JPAQuery;

import static java.util.Optional.ofNullable;

@SuppressWarnings({ "NonBooleanMethodNameMayNotStartWithQuestion", "unused", "OverloadedVarargsMethod" })
public class SmartJPAQuery<T> {
	private final JPAQuery<T> delegate;

	public SmartJPAQuery(JPAQuery<T> delegate) {
		this.delegate = delegate;
	}

	public SubQueryExpression<T> asSubQuery() {
		return delegate;
	}

	public <U> SmartJPAQuery<U> select(Expression<U> expr) {
		return new SmartJPAQuery<>(delegate.select(expr));
	}

	public SmartJPAQuery<Tuple> select(Expression<?>... exprs) {
		return new SmartJPAQuery<>(delegate.select(exprs));
	}

	public long fetchCount() {
		return delegate.fetchCount();
	}

	public CloseableIterator<T> iterate() {
		return delegate.iterate();
	}

	public List<T> fetch() {
		return delegate.fetch();
	}

	public QueryResults<T> fetchResults() {
		return delegate.fetchResults();
	}

	public Optional<T> fetchOne() throws NonUniqueResultException {
		return ofNullable(delegate.fetchOne());
	}

	public SmartJPAQuery<T> setLockMode(LockModeType lockMode) {
		return new SmartJPAQuery<>(delegate.setLockMode(lockMode));
	}

	public SmartJPAQuery<T> setFlushMode(FlushModeType flushMode) {
		return new SmartJPAQuery<>(delegate.setFlushMode(flushMode));
	}

	public SmartJPAQuery<T> setHint(String name, Object value) {
		return new SmartJPAQuery<>(delegate.setHint(name, value));
	}

	public SmartJPAQuery<T> fetchJoin() {
		return new SmartJPAQuery<>(delegate.fetchJoin());
	}

	public SmartJPAQuery<T> fetchAll() {
		return new SmartJPAQuery<>(delegate.fetchAll());
	}

	public SmartJPAQuery<T> from(EntityPath<?> arg) {
		return new SmartJPAQuery<>(delegate.from(arg));
	}

	public SmartJPAQuery<T> from(EntityPath<?>... args) {
		return new SmartJPAQuery<>(delegate.from(args));
	}

	public <P> SmartJPAQuery<T> from(CollectionExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.from(target, alias));
	}

	public <P> SmartJPAQuery<T> innerJoin(CollectionExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.innerJoin(target));
	}

	public <P> SmartJPAQuery<T> innerJoin(CollectionExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.innerJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> innerJoin(EntityPath<P> target) {
		return new SmartJPAQuery<>(delegate.innerJoin(target));
	}

	public <P> SmartJPAQuery<T> innerJoin(EntityPath<P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.innerJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> innerJoin(MapExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.innerJoin(target));
	}

	public <P> SmartJPAQuery<T> innerJoin(MapExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.innerJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> join(CollectionExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.join(target));
	}

	public <P> SmartJPAQuery<T> join(CollectionExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.join(target, alias));
	}

	public <P> SmartJPAQuery<T> join(EntityPath<P> target) {
		return new SmartJPAQuery<>(delegate.join(target));
	}

	public <P> SmartJPAQuery<T> join(EntityPath<P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.join(target, alias));
	}

	public <P> SmartJPAQuery<T> join(MapExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.join(target));
	}

	public <P> SmartJPAQuery<T> join(MapExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.join(target, alias));
	}

	public <P> SmartJPAQuery<T> leftJoin(CollectionExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.leftJoin(target));
	}

	public <P> SmartJPAQuery<T> leftJoin(CollectionExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.leftJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> leftJoin(EntityPath<P> target) {
		return new SmartJPAQuery<>(delegate.leftJoin(target));
	}

	public <P> SmartJPAQuery<T> leftJoin(EntityPath<P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.leftJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> leftJoin(MapExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.leftJoin(target));
	}

	public <P> SmartJPAQuery<T> leftJoin(MapExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.leftJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> rightJoin(CollectionExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.rightJoin(target));
	}

	public <P> SmartJPAQuery<T> rightJoin(CollectionExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.rightJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> rightJoin(EntityPath<P> target) {
		return new SmartJPAQuery<>(delegate.rightJoin(target));
	}

	public <P> SmartJPAQuery<T> rightJoin(EntityPath<P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.rightJoin(target, alias));
	}

	public <P> SmartJPAQuery<T> rightJoin(MapExpression<?, P> target) {
		return new SmartJPAQuery<>(delegate.rightJoin(target));
	}

	public <P> SmartJPAQuery<T> rightJoin(MapExpression<?, P> target, Path<P> alias) {
		return new SmartJPAQuery<>(delegate.rightJoin(target, alias));
	}

	public SmartJPAQuery<T> on(Predicate condition) {
		return new SmartJPAQuery<>(delegate.on(condition));
	}

	public SmartJPAQuery<T> on(Predicate... conditions) {
		return new SmartJPAQuery<>(delegate.on(conditions));
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public BooleanExpression contains(Expression<? extends T> right) {
		return delegate.contains(right);
	}

	public BooleanExpression contains(T constant) {
		return delegate.contains(constant);
	}

	public BooleanExpression exists() {
		return delegate.exists();
	}

	public BooleanExpression eq(Expression<? extends T> expr) {
		return delegate.eq(expr);
	}

	public BooleanExpression eq(T constant) {
		return delegate.eq(constant);
	}

	public BooleanExpression ne(Expression<? extends T> expr) {
		return delegate.ne(expr);
	}

	public BooleanExpression ne(T constant) {
		return delegate.ne(constant);
	}

	public BooleanExpression notExists() {
		return delegate.notExists();
	}

	public BooleanExpression lt(Expression<? extends T> expr) {
		return delegate.lt(expr);
	}

	public BooleanExpression lt(T constant) {
		return delegate.lt(constant);
	}

	public BooleanExpression gt(Expression<? extends T> expr) {
		return delegate.gt(expr);
	}

	public BooleanExpression gt(T constant) {
		return delegate.gt(constant);
	}

	public BooleanExpression loe(Expression<? extends T> expr) {
		return delegate.loe(expr);
	}

	public BooleanExpression loe(T constant) {
		return delegate.loe(constant);
	}

	public BooleanExpression goe(Expression<? extends T> expr) {
		return delegate.goe(expr);
	}

	public BooleanExpression goe(T constant) {
		return delegate.goe(constant);
	}

	public BooleanOperation isNull() {
		return delegate.isNull();
	}

	public BooleanOperation isNotNull() {
		return delegate.isNotNull();
	}

	public Class<T> getType() {
		return delegate.getType();
	}

	public BooleanExpression in(Collection<? extends T> right) {
		return delegate.in(right);
	}

	@SuppressWarnings("varargs")
	@SafeVarargs
	public final BooleanExpression in(T... right) {
		return delegate.in(right);
	}

	public <T1> T1 transform(ResultTransformer<T1> transformer) {
		return delegate.transform(transformer);
	}

	public SmartJPAQuery<T> distinct() {
		return new SmartJPAQuery<>(delegate.distinct());
	}

	public SmartJPAQuery<T> groupBy(Expression<?> e) {
		return new SmartJPAQuery<>(delegate.groupBy(e));
	}

	public SmartJPAQuery<T> groupBy(Expression<?>... o) {
		return new SmartJPAQuery<>(delegate.groupBy(o));
	}

	public SmartJPAQuery<T> having(Predicate e) {
		return new SmartJPAQuery<>(delegate.having(e));
	}

	public SmartJPAQuery<T> having(Predicate... o) {
		return new SmartJPAQuery<>(delegate.having(o));
	}

	public SmartJPAQuery<T> orderBy(OrderSpecifier<?> o) {
		return new SmartJPAQuery<>(delegate.orderBy(o));
	}

	public SmartJPAQuery<T> orderBy(OrderSpecifier<?>... o) {
		return new SmartJPAQuery<>(delegate.orderBy(o));
	}

	public SmartJPAQuery<T> where(Predicate o) {
		return new SmartJPAQuery<>(delegate.where(o));
	}

	public SmartJPAQuery<T> where(Predicate... o) {
		return new SmartJPAQuery<>(delegate.where(o));
	}

	public SmartJPAQuery<T> limit(long limit) {
		return new SmartJPAQuery<>(delegate.limit(limit));
	}

	public SmartJPAQuery<T> offset(long offset) {
		return new SmartJPAQuery<>(delegate.offset(offset));
	}

	public SmartJPAQuery<T> restrict(QueryModifiers modifiers) {
		return new SmartJPAQuery<>(delegate.restrict(modifiers));
	}

	public <P> SmartJPAQuery<T> set(ParamExpression<P> param, P value) {
		return new SmartJPAQuery<>(delegate.set(param, value));
	}

	public T fetchFirst() {
		return delegate.fetchFirst();
	}

	public QueryMetadata getMetadata() {
		return delegate.getMetadata();
	}

}
