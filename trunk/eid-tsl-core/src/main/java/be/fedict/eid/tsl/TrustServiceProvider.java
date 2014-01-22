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

import be.fedict.eid.tsl.jaxb.tsl.AddressType;
import be.fedict.eid.tsl.jaxb.tsl.ElectronicAddressType;
import be.fedict.eid.tsl.jaxb.tsl.InternationalNamesType;
import be.fedict.eid.tsl.jaxb.tsl.NonEmptyMultiLangURIListType;
import be.fedict.eid.tsl.jaxb.tsl.NonEmptyMultiLangURIType;
import be.fedict.eid.tsl.jaxb.tsl.ObjectFactory;
import be.fedict.eid.tsl.jaxb.tsl.PostalAddressListType;
import be.fedict.eid.tsl.jaxb.tsl.PostalAddressType;
import be.fedict.eid.tsl.jaxb.tsl.TSPInformationType;
import be.fedict.eid.tsl.jaxb.tsl.TSPServiceType;
import be.fedict.eid.tsl.jaxb.tsl.TSPServicesListType;
import be.fedict.eid.tsl.jaxb.tsl.TSPType;

public class TrustServiceProvider {

	private final TSPType tsp;

	private List<TrustService> trustServices;

	private final ObjectFactory objectFactory;

	TrustServiceProvider(TSPType tsp) {
		this.tsp = tsp;
		this.objectFactory = new ObjectFactory();
	}

	public TrustServiceProvider(String name, String tradeName) {
		this.objectFactory = new ObjectFactory();
		this.tsp = this.objectFactory.createTSPType();
		TSPInformationType tspInformation = this.objectFactory
				.createTSPInformationType();
		this.tsp.setTSPInformation(tspInformation);

		InternationalNamesType tspNames = this.objectFactory
				.createInternationalNamesType();
		TrustServiceListUtils.setValue(name, Locale.ENGLISH, tspNames);
		tspInformation.setTSPName(tspNames);

		InternationalNamesType tspTradeNames = this.objectFactory
				.createInternationalNamesType();
		TrustServiceListUtils
				.setValue(tradeName, Locale.ENGLISH, tspTradeNames);
		tspInformation.setTSPTradeName(tspTradeNames);
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

		postalAddress.setLang(locale.getLanguage());
		postalAddress.setStreetAddress(streetAddress);
		postalAddress.setLocality(locality);
		postalAddress.setStateOrProvince(stateOrProvince);
		postalAddress.setPostalCode(postalCode);
		postalAddress.setCountryName(countryName);
	}

	public PostalAddressType getPostalAddress() {
		return getPostalAddress(Locale.ENGLISH);
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

	public void addElectronicAddress(Locale local, String electronicAddres) {
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
		List<NonEmptyMultiLangURIType> uris = electronicAddress.getURI();
		NonEmptyMultiLangURIType uri = this.objectFactory.
				createNonEmptyMultiLangURIType();
		uri.setLang(local.getLanguage());
		uri.setValue(electronicAddres);
		uris.add(uri);
	}

	public NonEmptyMultiLangURIListType getElectronicAddress() {
		NonEmptyMultiLangURIListType resultElectronicAddress = this.objectFactory
				.createNonEmptyMultiLangURIListType();
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
		List<NonEmptyMultiLangURIType> uris = electronicAddress.getURI();
		for (NonEmptyMultiLangURIType uri : uris) {
			resultElectronicAddress.getURI().add(uri);
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
		uri.setLang(locale.getLanguage());
		uri.setValue(informationUri);
		uris.add(uri);
	}

	public List<String> getInformationUris() {
		return getInformationUris(Locale.ENGLISH);
	}
	
	public List<String> getInformationUris(Locale locale) {
		List<String> results = new LinkedList<String>();
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		if (null == tspInformation) {
			return results;
		}
		NonEmptyMultiLangURIListType tspInformationURI = tspInformation
				.getTSPInformationURI();
		if (null == tspInformationURI) {
			return results;
		}
		List<NonEmptyMultiLangURIType> uris = tspInformationURI.getURI();
		for (NonEmptyMultiLangURIType uri : uris) {
			String lang = uri.getLang();
			if (0 != locale.getLanguage().compareToIgnoreCase(lang)) {
				continue;
			}
			results.add(uri.getValue());
		}
		return results;
	}

	public String getName(Locale locale) {
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		InternationalNamesType i18nTspName = tspInformation.getTSPName();
		String tspName = TrustServiceListUtils.getValue(i18nTspName, locale);
		return tspName;
	}

	public String getTradeName(Locale locale) {
		TSPInformationType tspInformation = this.tsp.getTSPInformation();
		InternationalNamesType i18nTspTradeName = tspInformation
				.getTSPTradeName();
		String tspTradeName = TrustServiceListUtils.getValue(i18nTspTradeName,
				locale);
		return tspTradeName;
	}

	public String getTradeName() {
		return getTradeName(Locale.ENGLISH);
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
		if (null == tspServices) {
			return this.trustServices;
		}
		List<TSPServiceType> tspServiceList = tspServices.getTSPService();
		for (TSPServiceType tspService : tspServiceList) {
			TrustService trustService = new TrustService(tspService);
			this.trustServices.add(trustService);
		}
		return this.trustServices;
	}

	public void addTrustService(TrustService trustService) {
		TSPServicesListType tspServicesList = this.tsp.getTSPServices();
		if (null == tspServicesList) {
			tspServicesList = this.objectFactory.createTSPServicesListType();
			this.tsp.setTSPServices(tspServicesList);
		}
		List<TSPServiceType> tspServices = tspServicesList.getTSPService();
		tspServices.add(trustService.getTSPService());
		// reset java model cache
		this.trustServices = null;
	}
}
