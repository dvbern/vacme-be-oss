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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NextTerminCacheServiceTest {


	@Test
	public void testNextFreiCallCache(){

		System.setProperty("vacme.cache.nextfrei.ttl.sconds", "120"); // set to zero to disable cache
		LocalDateTime timeToReturn = LocalDateTime.now().plusDays(1).plusHours(12);
		OrtDerImpfungService ortDerImpfungServiceMock = mock(OrtDerImpfungService.class);
		Mockito.when(ortDerImpfungServiceMock.getNextFreierImpftermin(any(), eq(Impffolge.ERSTE_IMPFUNG), eq(null), eq(false), eq(KrankheitIdentifier.COVID)))
			.thenReturn(timeToReturn);

		NextTerminCacheService serviceUnderTest = new NextTerminCacheService(ortDerImpfungServiceMock);
		serviceUnderTest.init();

		ID<OrtDerImpfung> ortDerImpfungId = OrtDerImpfung.toId(UUID.randomUUID());

		// call multiple times and assert that method was only called once for each id
		List<LocalDateTime> allResults = callCacheServiceMultipleTimes(timeToReturn, serviceUnderTest,
			ortDerImpfungId);
		allResults.forEach(returnedDate -> Assertions.assertEquals(timeToReturn, returnedDate));

		ID<OrtDerImpfung> differentOdiId = OrtDerImpfung.toId(UUID.randomUUID());
		// call multiple times and assert that method was only called once for each id
		List<LocalDateTime> moreResults = callCacheServiceMultipleTimes(timeToReturn, serviceUnderTest,
			differentOdiId);
		moreResults.forEach(returnedDate -> Assertions.assertEquals(timeToReturn, returnedDate));

		verify(ortDerImpfungServiceMock, times(1)).getNextFreierImpftermin(ortDerImpfungId, Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
		verify(ortDerImpfungServiceMock, times(1)).getNextFreierImpftermin(differentOdiId, Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
	}



	@Test
	public void testCacheNull(){

		System.setProperty("vacme.cache.nextfrei.ttl.sconds", "120"); // set to zero to disable cache
		LocalDateTime timeToReturn = LocalDateTime.now().plusDays(1).plusHours(12);
		OrtDerImpfungService ortDerImpfungServiceMock = mock(OrtDerImpfungService.class);
		Mockito.when(ortDerImpfungServiceMock.getNextFreierImpftermin(any(), eq(Impffolge.ERSTE_IMPFUNG), eq(null), eq(false), eq(KrankheitIdentifier.COVID)))
			.thenReturn(null);

		NextTerminCacheService serviceUnderTest = new NextTerminCacheService(ortDerImpfungServiceMock);
		serviceUnderTest.init();

		ID<OrtDerImpfung> ortDerImpfungId = OrtDerImpfung.toId(UUID.randomUUID());

		// call multiple times and assert that method was only called once for each id
		List<LocalDateTime> allResults = callCacheServiceMultipleTimes(timeToReturn, serviceUnderTest,
			ortDerImpfungId);
		allResults.forEach(Assertions::assertNull);

		ID<OrtDerImpfung> differentOdiId = OrtDerImpfung.toId(UUID.randomUUID());
		// call multiple times and assert that method was only called once for each id
		List<LocalDateTime> moreResults = callCacheServiceMultipleTimes(timeToReturn, serviceUnderTest,
			differentOdiId);
		moreResults.forEach(Assertions::assertNull);

		verify(ortDerImpfungServiceMock, times(1)).getNextFreierImpftermin(ortDerImpfungId, Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
		verify(ortDerImpfungServiceMock, times(1)).getNextFreierImpftermin(differentOdiId, Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
	}

	@Test
	public void testNImpfungCache() {
		System.setProperty("vacme.cache.nextfrei.ttl.sconds", "120"); // set to zero to disable cache
		OrtDerImpfungService ortDerImpfungServiceMock = mock(OrtDerImpfungService.class);
		Mockito.when(ortDerImpfungServiceMock.getNextFreierImpftermin(any(), any(), eq(null), eq(false), any()))
			.thenReturn(null);

		NextTerminCacheService serviceUnderTest = new NextTerminCacheService(ortDerImpfungServiceMock);
		serviceUnderTest.init();

		ID<OrtDerImpfung> ortDerImpfungId = OrtDerImpfung.toId(UUID.randomUUID());

		// call multiple times and assert that method was only called once for each id, krankheit combination
		List<LocalDateTime> allResults = callCacheServiceMultipleTimes(serviceUnderTest,
			ortDerImpfungId, Impffolge.BOOSTER_IMPFUNG, KrankheitIdentifier.COVID);
		allResults.forEach(Assertions::assertNull);

		List<LocalDateTime> moreResults = callCacheServiceMultipleTimes(serviceUnderTest,
			ortDerImpfungId, Impffolge.BOOSTER_IMPFUNG, KrankheitIdentifier.AFFENPOCKEN);
		moreResults.forEach(Assertions::assertNull);

		verify(ortDerImpfungServiceMock, times(1)).getNextFreierImpftermin(ortDerImpfungId, Impffolge.BOOSTER_IMPFUNG, null, false, KrankheitIdentifier.COVID);
		verify(ortDerImpfungServiceMock, times(1)).getNextFreierImpftermin(ortDerImpfungId, Impffolge.BOOSTER_IMPFUNG, null, false, KrankheitIdentifier.AFFENPOCKEN);

	}

	private List<LocalDateTime> callCacheServiceMultipleTimes(LocalDateTime timeToReturn, NextTerminCacheService serviceUnderTest,
		ID<OrtDerImpfung> ortDerImpfungId) {
		return callCacheServiceMultipleTimes(serviceUnderTest, ortDerImpfungId, Impffolge.ERSTE_IMPFUNG, KrankheitIdentifier.COVID);
	}

	private List<LocalDateTime> callCacheServiceMultipleTimes(NextTerminCacheService serviceUnderTest, ID<OrtDerImpfung> ortDerImpfungId, Impffolge impffolge, KrankheitIdentifier krankheit) {
		return IntStream.range(0, 5)
			.mapToObj(value -> serviceUnderTest.getNextFreierImpfterminThroughCache(
				ortDerImpfungId,
				impffolge,
				null,
				false,
				krankheit))
			.collect(Collectors.toList());
	}

	@Test
	public void testCacheDisabled(){
		String originalaProp = System.getProperty("vacme.cache.nextfrei.ttl.sconds");
		System.setProperty("vacme.cache.nextfrei.ttl.sconds", "0"); // set to zero to disable cache


		LocalDateTime timeToReturn = LocalDateTime.now().plusDays(1).plusHours(12);
		OrtDerImpfungService ortDerImpfungServiceMock = mock(OrtDerImpfungService.class);
		Mockito.when(ortDerImpfungServiceMock.getNextFreierImpftermin(any(), eq(Impffolge.ERSTE_IMPFUNG), eq(null), eq(false), eq(KrankheitIdentifier.COVID)))
			.thenReturn(timeToReturn);

		NextTerminCacheService serviceUnderTest = new NextTerminCacheService(ortDerImpfungServiceMock);
		serviceUnderTest.init();

		ID<OrtDerImpfung> ortDerImpfungId = OrtDerImpfung.toId(UUID.randomUUID());

		// call multiple times and assert that method was only called once for each id
		List<LocalDateTime> allResults = callCacheServiceMultipleTimes(timeToReturn, serviceUnderTest,
			ortDerImpfungId);
		allResults.forEach(returnedDate -> Assertions.assertEquals(timeToReturn, returnedDate));

		ID<OrtDerImpfung> differentOdiId = OrtDerImpfung.toId(UUID.randomUUID());
		// call multiple times and assert that method was only called once for each id
		List<LocalDateTime> moreResults = callCacheServiceMultipleTimes(timeToReturn, serviceUnderTest,
			differentOdiId);
		moreResults.forEach(returnedDate -> Assertions.assertEquals(timeToReturn, returnedDate));

		// since cache is disabled this hould be called 5 times each
		verify(ortDerImpfungServiceMock, times(5)).getNextFreierImpftermin(ortDerImpfungId, Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
		verify(ortDerImpfungServiceMock, times(5)).getNextFreierImpftermin(differentOdiId, Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);

		if (originalaProp != null) {
			System.setProperty("vacme.cache.nextfrei.ttl.sconds", originalaProp);
		}
	}
}
