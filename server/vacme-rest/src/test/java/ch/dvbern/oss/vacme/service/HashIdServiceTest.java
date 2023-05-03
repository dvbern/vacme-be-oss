package ch.dvbern.oss.vacme.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashIdServiceTest {

	@Test
	void getHashFromNumber() {

		HashIdService hashIdService = new HashIdService();
		hashIdService.alphabet = "123456789ABCDEFGHIKLMNPQRSTUVWXYZ";
		hashIdService.salt = "mysaltstri";
		hashIdService.minLength = 6;
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
