package ch.dvbern.oss.vacme.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HashIdServiceTest {

	@Test
	void getHashFromNumber() {

		VacmeSettingsService vacmeSettingsService = Mockito.mock(VacmeSettingsService.class);
		Mockito.when(vacmeSettingsService.getHashidAlphabet()).thenReturn("123456789ABCDEFGHIKLMNPQRSTUVWXYZ");
		Mockito.when(vacmeSettingsService.getHashidSalt()).thenReturn("mysaltstri");
		Mockito.when(vacmeSettingsService.getHashidMinLength()).thenReturn(6);

		HashIdService hashIdService = new HashIdService(vacmeSettingsService);
		String regNum = "";

		int i = 200000;
		regNum =  hashIdService.getHashFromNumber(i);
		Assertions.assertEquals(6, regNum.length());
		i = 5153631; // maximum number of regs that will get 6 character code
		regNum =  hashIdService.getHashFromNumber(i);
		Assertions.assertEquals(6, regNum.length());
		i =  5153632;
		regNum =  hashIdService.getHashFromNumber(i);
		Assertions.assertEquals(7, regNum.length());


	}
}
