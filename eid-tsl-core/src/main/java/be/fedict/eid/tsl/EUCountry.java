/*
 * eID TSL Project.
 * Copyright (C) 2009 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.tsl;

public enum EUCountry {

	BE("BE", "Belgique/België", "Belgium", "Kingdom of Belgium"), BG("BG",
			"България (*)", "Bulgaria", "Republic of Bulgaria"), CZ("CZ",
			"Česká republika", "Czech Republic", "Czech Republic"), DK("DK",
			"Danmark", "Denmark", "Kingdom of Denmark"), DE("DE",
			"Deutschland", "Germany", "Federal Republic of Germany"), EE("EE",
			"Eesti", "Estonia", "Republic of Estonia"), IE("IE",
			"Éire/Ireland", "Ireland", "Ireland"), EL("GR", "Ελλάδα (*)",
			"Greece", "Hellenic Republic"), ES("ES", "España", "Spain",
			"Kingdom of Spain"), FR("FR", "France", "France", "French Republic"), IT(
			"IT", "Italia", "Italy", "Italian Republic"), CY("CY",
			"Κύπρος/Kıbrıs (*)", "Cyprus", "Republic of Cyprus"), LV("LV",
			"Latvija", "Latvia", "Republic of Latvia"), LT("LT", "Lietuva",
			"Lithuania", "Republic of Lithuania"), LU("LU", "Luxembourg",
			"Luxembourg", "Grand Duchy of Luxembourg"), HU("HU",
			"Magyarország", "Hungary", "Republic of Hungary"), MT("MT",
			"Malta", "Malta", "Republic of Malta"), NL("NL", "Nederland",
			"Netherlands", "Kingdom of the Netherlands"), AT("AT",
			"Österreich", "Austria", "Republic of Austria"), PL("PL", "Polska",
			"Poland", "Republic of Poland"), PT("PT", "Portugal", "Portugal",
			"Portuguese Republic"), RO("RO", "România", "Romania", "Romania"), SI(
			"SI", "Slovenija", "Slovenia", "Republic of Slovenia"), SK("SK",
			"Slovensko", "Slovakia", "Slovak Republic"), FI("FI",
			"Suomi/Finland", "Finland", "Republic of Finland"), SE("SE",
			"Sverige", "Sweden", "Kingdom of Sweden"), UK("GB",
			"United Kingdom", "United Kingdom",
			"United Kingdom of Great Britain and Northern Ireland");

	private final String isoCode;

	private final String shortSrcLangName;

	private final String shortEnglishName;

	private final String officialEnglishName;

	private EUCountry(final String isoCode, final String shortSrcLangName,
			final String shortEnglishName, final String officialEnglishName) {
		this.isoCode = isoCode;
		this.officialEnglishName = officialEnglishName;
		this.shortEnglishName = shortEnglishName;
		this.shortSrcLangName = shortSrcLangName;
	}

	public String getIsoCode() {
		return this.isoCode;
	}

	public String getShortSrcLangName() {
		return this.shortSrcLangName;
	}

	public String getShortEnglishName() {
		return this.shortEnglishName;
	}

	public String getOfficialEnglishName() {
		return this.officialEnglishName;
	}
}
