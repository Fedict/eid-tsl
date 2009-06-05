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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.TSPInformationType;
import org.etsi.uri._02231.v2_.TSPServiceType;
import org.etsi.uri._02231.v2_.TSPServicesListType;
import org.etsi.uri._02231.v2_.TSPType;

public class TrustServiceProvider {

	private final TSPType tsp;

	private List<TrustService> trustServices;

	TrustServiceProvider(TSPType tsp) {
		this.tsp = tsp;
	}

	public String getName(Locale locale) {
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		InternationalNamesType i18nTspName = tspInformation.getTSPName();
		String tspName = TrustServiceListUtils.getValue(i18nTspName, locale);
		return tspName;
	}

	public String getName() {
		Locale locale = Locale.getDefault();
		return getName(locale);
	}

	public List<TrustService> getTrustServices() {
		if (null != this.trustServices) {
			return this.trustServices;
		}
		this.trustServices = new LinkedList<TrustService>();
		TSPServicesListType tspServices = this.tsp.getTSPServices();
		List<TSPServiceType> tspServiceList = tspServices.getTSPService();
		for (TSPServiceType tspService : tspServiceList) {
			TrustService trustService = new TrustService(tspService);
			this.trustServices.add(trustService);
		}
		return this.trustServices;
	}
}
