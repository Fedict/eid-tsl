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

public enum EULanguage {

	bg("български", "Bulgarian"), es("español", "Spanish"), cs("čeština",
			"Czech"), da("dansk", "Danish"), de("Deutsch", "German"), et(
			"eesti keel", "Estonian"), el("ελληνικά", "Greek"), en("English",
			"English"), fr("français", "French"), ga("Gaeilge", "Irish"), it(
			"italiano", "Italian"), lv("latviešu valoda", "Latvian"), lt(
			"lietuvių kalba", "Lithuanian"), hu("magyar", "Hungarian"), mt(
			"Malti", "Maltese"), nl("Nederlands", "Dutch"), pl("polski",
			"Polish"), pt("português", "Portuguese"), ro("română", "Romanian"), sk(
			"slovenčina", "Slovak"), sl("slovenščina", "Slovenian"), fi(
			"suomi", "Finnish"), sv("svenska", "Swedish");

	private final String selfName;

	private final String englishName;

	private EULanguage(final String selfName, final String englishName) {
		this.selfName = selfName;
		this.englishName = englishName;
	}

	public String getEnglishName() {
		return this.englishName;
	}

	public String getSelfName() {
		return this.selfName;
	}
}
