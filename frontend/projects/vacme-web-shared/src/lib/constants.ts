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

export const DB_DEFAULT_MAX_LENGTH = 255;
export const DB_VMDL_SCHNITTSTELLE_LENGTH = 32;
export const EMAIL_PATTERN = '[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}';
export const REGEX_TELEFON = '^(0|\\+41|0041)[ ]*([\\d]{1,2})[ ]*[\\d]{3}[ ]*[\\d]{2}[ ]*[\\d]{2}$';
export const MOBILE_VORWAHLEN = ['75', '76', '77', '78', '79'];
export const REGEX_NUMBER_INT = '^[0-9]*$';
export const TEL_REGEX_NUMBER_INT = '^[0-9\\+]{1}[0-9\\ ]*$';
export const REGEX_GLN_NUM = '^[^a-zA-Z]*$';
export const REGEX_NUMBER = '^-?[0-9]+(\\.[0-9]*){0,1}$';
export const REGEX_IMPFMENGE = /^([0-1](\.\d{0,2})?|2)$/;
export const DATE_PATTERN = '^\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*$';
export const DATE_TIME_PATTERN = '^\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\,\\s(2[0-3]|[1][0-9]|0?[1-9])\\:([0-5][0-9]|0?[1-9])$';
export const DATE_FORMAT = 'DD.MM.YYYY';
export const DATE_TIME_FORMAT = 'DD.MM.YYYY, HH:mm';
export const MAX_LENGTH_TEXTAREA = 2000;
export const KRANKENKASSE_KARTENNUMMER_PRAEFIX = '80756';
export const KRANKENKASSENNUMMERN_OHNE_PREFIX = '00000000000000000000';
export const API_URL = '/api/v1';
export const COLOR_PROGRESSBAR = '#707070'; // siehe Variable $color-vacme-grey !!
export const TERMINSLOTS_MAX_PER_DAY = 500;
export const MAX_ODI_STATS_ON_STARTPAGE = 4;
export const NBR_HOURS_TO_SHOW_IMPFDETAILS = 24;
export const NBR_IMPFSLOT_PER_DAY = 32;
export const HTTP_UNAUTHORIZED = 401;
export const MIN_ALTER_IMPFLING = 5;
export const MAX_ALTER_IMPFLING = 120;
export const MIN_ALTER_BERUF = 12;
export const MIN_DATE_FOR_IMPFUNGEN_COVID = '20.12.2020';
export const MIN_DATE_FOR_IMPFUNGEN_AFFENPOCKEN = '01.11.2022';
export const MIN_DATE_FOR_EXTERNE_IMPFUNGEN_COVID = '01.01.2020';
export const MIN_DATE_FOR_EXTERNE_IMPFUNGEN = '01.01.1900';
export const MAX_EXTERNES_ZERTIFIKAT_IMPFUNGEN = 8; // weil wir hoechstens ein 9/9-Zertifikat ausstellen koennen
export const MIN_DATE_FOR_POSITIV_GETESTET = '01.01.2020';
export const BASE_DECIMAL_MAX_LENGTH = 17;
export const API_TOKEN_MAX_LENGTH = 2000;
export const ONBOARDING_ACTIVE_KEY = 'onboarding-in-progress';
export const ONBOARDING_TOKEN_TTL = 15 * 60 * 1000; // 15 minutes (in milliseconds)
export const JUMP_TO_KC_REGISTRAION = 'jumptokcregistration';
export const DAILY_SELBSTZAHLER_POPUP  = 'dailySelbstzahlerPopup';
export const KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP  = 'forcedWellLogoutTimestamp';
export const KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP_GRACE_SECONDS  = 20;
export const JUMP_TO_KC_REGISTRAION_TTL = 1 * 60 * 1000; // 1 minute (in milliseconds)
export const IMPFSTOFF_ID_KINDERIMPFUNG = '4ebb48c1-cc96-4a8e-9832-77092bb968db';
export const REGISTRIERUNGSNUMMER_LENGTH = 6;
export const EARTH_RADIUS = 6378137;
export const MAX_ODI_DISTANCE = 800; // km inside Switzerland
