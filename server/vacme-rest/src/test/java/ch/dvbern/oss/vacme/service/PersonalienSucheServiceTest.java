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

package ch.dvbern.oss.vacme.service;

import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.jax.PersonalienSucheJax;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

public class PersonalienSucheServiceTest {
	private PersonalienSucheService sucheService;

	@BeforeEach
	void setUp() {
		sucheService = new PersonalienSucheService(
			Mockito.mock(RegistrierungRepo.class),
			Mockito.mock(ImpfdossierRepo.class),
			Mockito.mock(ZertifikatService.class),
			Mockito.mock(UserPrincipal.class),
			Mockito.mock(ImpfterminRepo.class)
		);
	}

	@ParameterizedTest
	@CsvSource({
		"Peterhans    , Josué Markus    , Peterhans     , Josué Markus          , true"  ,
		"Peterhans    , Josué Markus    , Peterhans     , Josué Markus Hans     , true"  ,
		"Peterhans    , Markus Josué    , Peterhans     , Josué Markus Hans     , false" ,
		"Peterhans    , Markus Hans     , Peterhans     , Josué Markus Hans     , false" ,
		"Peterhans    , Josue Markus    , Peterhans     , Josué Markus          , true"  ,
		"Peterhans    , Markus          , Peterhans     , Josué Markus          , true"  ,
		"Hans         , Josué Markus    , Peterhans     , Josué Markus          , false" ,
		"Hanspeter    , Josué Markus    , Peterhans     , Josué Markus          , false" ,
		"ter          , Mark            , Peterhans     , Josué Markus          , false" ,
		"Peterhans    , Mark            , Peterhans     , Josué Markus          , false" ,
		"Meier        , Fridolin        , Muster Meier  , Fridolin              , true"  ,
		"Muster       , Fridolin        , Muster Meier  , Fridolin              , true"  ,
		"Meier        , Fridolin        , Meier-Muster  , Fridolin              , true"  ,
		"Muster       , Fridolin        , Meier-Muster  , Fridolin              , true"  ,
		"Meier-Muster , Fridolin        , Meier-Muster  , Fridolin              , true"  ,
		"Weibel       , Xaver           , Weibel-Kohler , Xaver                 , true"  ,
		"Weibel-Kohle , Xaver           , Weibel-Kohler , Xaver                 , false" ,
		"Kohler       , Xaver           , Weibel-Kohler , Xaver                 , true"  ,
		"Weibel       , Xaver           , Weibel-Kohler , Xaver Raphael         , true"  ,
		"Weibel       , Raphael         , Weibel-Kohler , Xaver Raphael         , true"  ,
		"Weibel       , Raphael Xaver   , Weibel-Kohler , Xaver Raphael         , false"  ,
		"Müller       , Simon Friedrich , Müller        , Simon Friedrich Jakob , true"  ,
		"Müller       , Friedrich Jakob , Müller        , Simon Friedrich Jakob , false" ,
		"Dindic       , Granit          , Đinđić        , Granit                , false" , // Đ is its own char and does not match D
		"Đinđić       , Granit          , Dindic        , Granit                , false" ,
	})
	void testFilterMatchingJaxes2(String searchName, String searchVorname, String storedName, String storedVorname, Boolean booleanShouldMatch) {
		List<PersonalienSucheJax> persistedJax = List.of(new PersonalienSucheJax(
			UUID.randomUUID(), storedName, storedVorname));
		boolean match;
		String msg = booleanShouldMatch ? "should be a match" : "should not be a match";
		match = !sucheService.filterMatchingJaxes(searchVorname, searchName, persistedJax).isEmpty();
		Assertions.assertEquals(booleanShouldMatch, match, msg);
	}

}
