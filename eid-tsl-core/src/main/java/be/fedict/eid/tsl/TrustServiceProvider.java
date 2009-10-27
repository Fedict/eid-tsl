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

import org.etsi.uri._02231.v2_.AddressType;
import org.etsi.uri._02231.v2_.ElectronicAddressType;
import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.NonEmptyMultiLangURIListType;
import org.etsi.uri._02231.v2_.NonEmptyMultiLangURIType;
import org.etsi.uri._02231.v2_.ObjectFactory;
import org.etsi.uri._02231.v2_.PostalAddressListType;
import org.etsi.uri._02231.v2_.PostalAddressType;
import org.etsi.uri._02231.v2_.TSPInformationType;
import org.etsi.uri._02231.v2_.TSPServiceType;
import org.etsi.uri._02231.v2_.TSPServicesListType;
import org.etsi.uri._02231.v2_.TSPType;

public class TrustServiceProvider {

	private final TSPType tsp;

	private List<TrustService> trustServices;

	private final ObjectFactory objectFactory;

	TrustServiceProvider(TSPType tsp) {
		this.tsp = tsp;
		this.objectFactory = new ObjectFactory();
	}

	public TrustServiceProvider(String name) {
		this.objectFactory = new ObjectFactory();
		this.tsp = this.objectFactory.createTSPType();
		TSPInformationType tspInformation = this.objectFactory
				.createTSPInformationType();
		InternationalNamesType tspNames = this.objectFactory
				.createInternationalNamesType();
		TrustServiceListUtils.setValue(name, Locale.ENGLISH, tspNames);
		tspInformation.setTSPName(tspNames);
		this.tsp.setTSPInformation(tspInformation);
	}

	TSPType getTSP() {
		return this.tsp;
	}

	public void addPostalAddress(Locale locale, String streetAddress,
			String locality, String stateOrProvince, String postalCode,
			String countryName) {
		TSPInformationType tspInformation = getTSPInformation();
		AddressType address = tspInformation.getTSPAddress();
		if (null == address) {
			address = this.objectFactory.createAddressType();
			tspInformation.setTSPAddress(address);
		}
		PostalAddressListType postalAddresses = address.getPostalAddresses();
		if (null == postalAddresses) {
			postalAddresses = this.objectFactory.createPostalAddressListType();
			address.setPostalAddresses(postalAddresses);
		}
		List<PostalAddressType> postalAddressList = postalAddresses
				.getPostalAddress();
		PostalAddressType postalAddress = this.objectFactory
				.createPostalAddressType();
		postalAddressList.add(postalAddress);

		postalAddress.setLang(locale.getLanguage().toUpperCase());
		postalAddress.setStreetAddress(streetAddress);
		postalAddress.setLocality(locality);
		postalAddress.setStateOrProvince(stateOrProvince);
		postalAddress.setPostalCode(postalCode);
		postalAddress.setCountryName(countryName);
	}

	public PostalAddressType getPostalAddress(Locale locale) {
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		if (null == tspInformation) {
			return null;
		}
		AddressType address = tspInformation.getTSPAddress();
		if (null == address) {
			return null;
		}
		PostalAddressListType postalAddresses = address.getPostalAddresses();
		if (null == postalAddresses) {
			return null;
		}
		List<PostalAddressType> postalAddressList = postalAddresses
				.getPostalAddress();
		for (PostalAddressType postalAddress : postalAddressList) {
			String lang = postalAddress.getLang();
			if (0 != locale.getLanguage().compareToIgnoreCase(lang)) {
				continue;
			}
			return postalAddress;
		}
		return null;
	}

	public void addElectronicAddress(String... electronicAddressUris) {
		TSPInformationType tspInformation = getTSPInformation();
		AddressType address = tspInformation.getTSPAddress();
		if (null == address) {
			address = this.objectFactory.createAddressType();
			tspInformation.setTSPAddress(address);
		}
		ElectronicAddressType electronicAddress = address
				.getElectronicAddress();
		if (null == electronicAddress) {
			electronicAddress = this.objectFactory
					.createElectronicAddressType();
			address.setElectronicAddress(electronicAddress);
		}
		List<String> uris = electronicAddress.getURI();
		for (String electronicAddressUri : electronicAddressUris) {
			uris.add(electronicAddressUri);
		}
	}

	public List<String> getElectronicAddress() {
		List<String> resultElectronicAddress = new LinkedList<String>();
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		if (null == tspInformation) {
			return resultElectronicAddress;
		}
		AddressType address = tspInformation.getTSPAddress();
		if (null == address) {
			return resultElectronicAddress;
		}
		ElectronicAddressType electronicAddress = address
				.getElectronicAddress();
		if (null == electronicAddress) {
			return resultElectronicAddress;
		}
		List<String> uris = electronicAddress.getURI();
		for (String uri : uris) {
			resultElectronicAddress.add(uri);
		}
		return resultElectronicAddress;
	}

	private TSPInformationType getTSPInformation() {
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		if (null == tspInformation) {
			tspInformation = this.objectFactory.createTSPInformationType();
			this.tsp.setTSPInformation(tspInformation);
		}
		return tspInformation;
	}

	public void addInformationUri(Locale locale, String informationUri) {
		TSPInformationType tspInformation = getTSPInformation();
		NonEmptyMultiLangURIListType tspInformationURI = tspInformation
				.getTSPInformationURI();
		if (null == tspInformationURI) {
			tspInformationURI = this.objectFactory
					.createNonEmptyMultiLangURIListType();
			tspInformation.setTSPInformationURI(tspInformationURI);
		}
		List<NonEmptyMultiLangURIType> uris = tspInformationURI.getURI();
		NonEmptyMultiLangURIType uri = this.objectFactory
				.createNonEmptyMultiLangURIType();
		uri.setLang(locale.getLanguage().toUpperCase());
		uri.setValue(informationUri);
		uris.add(uri);
	}

	public String getInformationUri(Locale locale) {
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		if (null == tspInformation) {
			return null;
		}
		NonEmptyMultiLangURIListType tspInformationURI = tspInformation
				.getTSPInformationURI();
		if (null == tspInformationURI) {
			return null;
		}
		List<NonEmptyMultiLangURIType> uris = tspInformationURI.getURI();
		for (NonEmptyMultiLangURIType uri : uris) {
			String lang = uri.getLang();
			if (0 != locale.getLanguage().compareToIgnoreCase(lang)) {
				continue;
			}
			return uri.getValue();
		}
		return null;
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
