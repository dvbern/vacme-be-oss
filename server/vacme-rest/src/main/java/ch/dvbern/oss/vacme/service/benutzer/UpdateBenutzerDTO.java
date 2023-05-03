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

package ch.dvbern.oss.vacme.service.benutzer;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@AllArgsConstructor
public class UpdateBenutzerDTO {

	private String preferred_username;
	private String familyName;
	private String givenName;
	private String email;
	private String mobile;
	private String glnNum;
	private String issuer;
	private String wellId;

	public void applyTo(@NonNull Benutzer dbBenutzer) {
		dbBenutzer.setBenutzername(preferred_username);
		dbBenutzer.setName(familyName);
		dbBenutzer.setVorname(givenName);
		dbBenutzer.setEmail(email);
		dbBenutzer.setMobiltelefon(mobile);
		// eigentlich immer gesetzt aber es gibt testbenutzer bei denen es gar nicht erfasst wurde
		dbBenutzer.setMobiltelefonValidiert(!Strings.isNullOrEmpty(mobile));
		dbBenutzer.setGlnNummer(glnNum);
		dbBenutzer.setIssuer(issuer);
		dbBenutzer.setWellId(wellId);
	}
}
