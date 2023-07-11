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

package ch.dvbern.oss.vacme.repo;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.onboarding.Onboarding;
import ch.dvbern.oss.vacme.entities.onboarding.QOnboarding;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingHashIdService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequestScoped
@Transactional
public class OnboardingRepo {

	private final Db db;
	private final OnboardingHashIdService onboardingHashIdService;

	@Inject
	public OnboardingRepo(Db db, OnboardingHashIdService onboardingHashIdService) {
		this.db = db;
		this.onboardingHashIdService = onboardingHashIdService;
	}

	/**
	 * Speichert einen Onboarding Code in der DB
	 */
	public void create(@NonNull Onboarding onboarding) {
		db.persist(onboarding);
	}

	@NonNull
	public Optional<Onboarding> findOnboardingObjectByCode(@NonNull String code) {
		var result = db.select(QOnboarding.onboarding)
			.from(QOnboarding.onboarding)
			.where(QOnboarding.onboarding.code.eq(code))
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	@NonNull
	public Optional<Onboarding> findOnboardingObjectByToken(@NonNull String token) {
		var result = db.select(QOnboarding.onboarding)
			.from(QOnboarding.onboarding)
			.where(
				QOnboarding.onboarding.onboardingTempToken.eq(token)
			)
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	public List<Onboarding> findByRegistrierung(@NonNull Registrierung registrierung) {
		return db.selectFrom(QOnboarding.onboarding)
			.where(QOnboarding.onboarding.registrierung.eq(registrierung))
			.fetch();
	}

	@NonNull
	@Transactional(Transactional.TxType.SUPPORTS)
	/* use transaction if there is one in the context (keep in mind sequences can't be rolled back)
	otherwise run without */
	public String getNextOnboardingCode() {
		BigInteger nextValue =
			(BigInteger) this.db.getEntityManager().createNativeQuery("SELECT NEXT VALUE FOR onboarding_sequence;").getSingleResult();

		SecureRandom random = new SecureRandom();
		String randomDigits = String.valueOf(random.nextInt(10) + String.valueOf(random.nextInt(10)));
		return onboardingHashIdService.getOnboardingHash(nextValue.longValue(), randomDigits);
	}

	public void update(@NonNull Onboarding onboarding) {
		db.merge(onboarding);
		db.flush();
	}

	public boolean isValidOnboardingCode(String onboardingCode) {
		return this.onboardingHashIdService.isValidOnboardingCode(onboardingCode);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public Onboarding increaseNumOfTries(Onboarding onboarding) {
		Optional<Onboarding> onboardingOptional = db.get(onboarding.toId());
		Onboarding onboardingToIncrement = onboardingOptional
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, onboarding.getId().toString()));
		onboardingToIncrement.increaseNumOfTries();

		return db.merge(onboardingToIncrement);

	}

	@Transactional(TxType.REQUIRES_NEW)
	@Nullable
	public String generateOnboardingTempToken(Onboarding loadedOnboarding) {
		loadedOnboarding.generateOnboardingTempToken();
		db.merge(loadedOnboarding);
		return loadedOnboarding.getOnboardingTempToken();
	}

	public void delete(@NonNull Onboarding onboarding) {
		db.remove(onboarding);
	}
}
