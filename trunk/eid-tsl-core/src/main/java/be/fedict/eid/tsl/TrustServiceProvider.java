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
import org.etsi.uri._02231.v2_.TSPInformationType;
import org.etsi.uri._02231.v2_.TSPType;

public class TrustServiceProvider {

	private final TSPType tsp;

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
}
