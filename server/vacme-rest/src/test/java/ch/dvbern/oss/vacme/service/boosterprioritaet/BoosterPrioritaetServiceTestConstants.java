/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.time.LocalDate;

public class BoosterPrioritaetServiceTestConstants {

	public static final int FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG = 11;
	public static final int FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG = 6;
	public static final int FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG = 6;
	public static final int FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A = 9;

	public static final int FREIGABE_MONTHS_NACH_IMPFUNG = 6;
	public static final int FREIGABE_MONTHS_NACH_KRANKHEIT = 6;
	public static final LocalDate CUTOFF_SELBSTZAHLER = LocalDate.of(2022, 10, 10);


	public static final BoosterPrioritaetServiceTestConfig TEST_MAERZ_2023_BE_CONFIG = new BoosterPrioritaetServiceTestConfig(
		FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG,
		FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG,
		FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG,
		FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A,
		FREIGABE_MONTHS_NACH_IMPFUNG,
		FREIGABE_MONTHS_NACH_KRANKHEIT,
		CUTOFF_SELBSTZAHLER
	);

	public static final BoosterPrioritaetServiceTestConfig TEST_HERBST_2022_BE_CONFIG = new BoosterPrioritaetServiceTestConfig(
		FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG,
		FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG,
		FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG,
		FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A,
		4,
		4,
		CUTOFF_SELBSTZAHLER
	);

}
