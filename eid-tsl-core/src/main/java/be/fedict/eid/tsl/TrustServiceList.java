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

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.TSLSchemeInformationType;
import org.etsi.uri._02231.v2_.TSPType;
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

	private List<TrustServiceProvider> trustServiceProviders;

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
		String name = TrustServiceListUtils.getValue(i18nSchemeName, locale);
		return name;
	}

	public String getSchemeOperatorName() {
		Locale locale = Locale.getDefault();
		return getSchemeOperatorName(locale);
	}

	public String getSchemeOperatorName(Locale locale) {
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		InternationalNamesType i18nSchemeOperatorName = tslSchemeInformation
				.getSchemeOperatorName();
		String name = TrustServiceListUtils.getValue(i18nSchemeOperatorName,
				locale);
		return name;
	}

	public List<TrustServiceProvider> getTrustServiceProviders() {
		if (null != this.trustServiceProviders) {
			// only load once
			return this.trustServiceProviders;
		}
		this.trustServiceProviders = new LinkedList<TrustServiceProvider>();
		List<TSPType> tsps = this.trustStatusList.getTrustServiceProviderList()
				.getTrustServiceProvider();
		for (TSPType tsp : tsps) {
			TrustServiceProvider trustServiceProvider = new TrustServiceProvider(
					tsp);
			this.trustServiceProviders.add(trustServiceProvider);
		}
		return this.trustServiceProviders;
	}

	public String getType() {
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		String type = tslSchemeInformation.getTSLType();
		return type;
	}

	public BigInteger getSequenceNumber() {
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		BigInteger sequenceNumber = tslSchemeInformation.getTSLSequenceNumber();
		return sequenceNumber;
	}

	public Date getIssueDate() {
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		XMLGregorianCalendar xmlGregorianCalendar = tslSchemeInformation
				.getListIssueDateTime();
		return xmlGregorianCalendar.toGregorianCalendar().getTime();
	}
}
