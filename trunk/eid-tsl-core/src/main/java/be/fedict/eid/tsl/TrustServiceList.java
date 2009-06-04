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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.MultiLangNormStringType;
import org.etsi.uri._02231.v2_.TSLSchemeInformationType;
import org.etsi.uri._02231.v2_.TrustStatusListType;

/**
 * Trust Service List.
 * 
 * @author fcorneli
 * 
 */
public class TrustServiceList {

	private static final Log LOG = LogFactory.getLog(TrustServiceList.class);

	private final TrustStatusListType trustStatusList;

	protected TrustServiceList(TrustStatusListType trustStatusList) {
		this.trustStatusList = trustStatusList;
	}

	public String getSchemeName() {
		Locale locale = Locale.getDefault();
		return getSchemeName(locale);
	}

	public String getSchemeName(Locale locale) {
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		InternationalNamesType i18nSchemeName = tslSchemeInformation
				.getSchemeName();
		List<MultiLangNormStringType> schemeNames = i18nSchemeName.getName();
		String enValue = null;
		for (MultiLangNormStringType schemeName : schemeNames) {
			String lang = schemeName.getLang().toUpperCase();
			if ("EN".equals(lang)) {
				enValue = schemeName.getValue();
			}
			if (locale.getLanguage().equals(lang)) {
				return schemeName.getValue();
			}
		}
		if (null != enValue) {
			return enValue;
		}
		return schemeNames.get(0).getValue();
	}
}
