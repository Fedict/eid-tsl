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

import java.util.List;
import java.util.Locale;

import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.MultiLangNormStringType;

class TrustServiceListUtils {

	private TrustServiceListUtils() {
		super();
	}

	static String getValue(InternationalNamesType i18nName, Locale locale) {
		List<MultiLangNormStringType> names = i18nName.getName();
		String enValue = null;
		for (MultiLangNormStringType name : names) {
			String lang = name.getLang().toLowerCase();
			if ("en".equals(lang)) {
				enValue = name.getValue();
			}
			if (locale.getLanguage().equals(lang)) {
				return name.getValue();
			}
		}
		if (null != enValue) {
			return enValue;
		}
		return names.get(0).getValue();
	}
}
