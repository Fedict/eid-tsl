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

import java.util.Locale;

import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.TSPServiceInformationType;
import org.etsi.uri._02231.v2_.TSPServiceType;

public class TrustService {

	private final TSPServiceType tspService;

	TrustService(TSPServiceType tspService) {
		this.tspService = tspService;
	}

	public String getName(Locale locale) {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		InternationalNamesType i18nServiceName = tspServiceInformation
				.getServiceName();
		String serviceName = TrustServiceListUtils.getValue(i18nServiceName,
				locale);
		return serviceName;
	}

	public String getName() {
		Locale locale = Locale.getDefault();
		return getName(locale);
	}

	public String getType() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		String serviceTypeIdentifier = tspServiceInformation
				.getServiceTypeIdentifier();
		return serviceTypeIdentifier;
	}

	public String getStatus() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		String status = tspServiceInformation.getServiceStatus();
		return status;
	}

	@Override
	public String toString() {
		return getName();
	}
}
