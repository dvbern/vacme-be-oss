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

package ch.dvbern.oss.vacme.service.sms;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ECallSmsProviderTest {


	@Test
	public void testUrlEncoding() throws URISyntaxException {
		ECallSmsProvider eCallSmsProvider = new ECallSmsProvider("https://testurl.ch/sms", "myUser", "mySecret",
			"myJobid", "myCallback", "myExtCallback");
		URI testUri = eCallSmsProvider.getUriWithParams("+41769991234", "This is a méssage für Testović");
		String url = testUri.toString();
		Assertions.assertEquals("https://testurl.ch/sms?username=myUser&password=mySecret&address=%2B41769991234&message=This+is+a+m%C3%A9ssage+f%C3%BCr+Testovi%C4%87&jobid=myJobid&callback=myCallback",
			url);
	}

	@Test
	public void testSimplifiedString() throws URISyntaxException {
		ECallSmsProvider eCallSmsProvider = new ECallSmsProvider("https://testurl.ch/sms", "myUser", "mySecret",
			"myJobid", "myCallback", "myExtCallback");
		URI testUri = eCallSmsProvider.getUriWithParams("+41769991234", "This is a méssage für Đinđić");
		String url = testUri.toString();
		Assertions.assertEquals("https://testurl.ch/sms?username=myUser&password=mySecret&address=%2B41769991234&message=This+is+a+m%C3%A9ssage+f%C3%BCr+%C4%90in%C4%91i%C4%87&jobid=myJobid&callback=myCallback",
			url);
	}

	@Test
	public void extCallbackForForeignNumber() throws URISyntaxException {
		ECallSmsProvider eCallSmsProvider = new ECallSmsProvider("https://testurl.ch/sms", "myUser", "mySecret",
			"myJobid", "myCallback", "myExtCallback");
		URI testUri = eCallSmsProvider.getUriWithParams("+260978365113", "message");
		String url = testUri.toString();
		Assertions.assertEquals("https://testurl.ch/sms?username=myUser&password=mySecret&address=%2B260978365113&message=message&jobid=myJobid&callback=myExtCallback",
			url);
	}
}
