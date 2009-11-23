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

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.etsi.uri._02231.v2_.PostalAddressType;
import org.joda.time.DateTime;

/**
 * Factory for the Belgian Trust Service List.
 * 
 * @author fcorneli
 * 
 */
public class BelgianTrustServiceListFactory {

	private BelgianTrustServiceListFactory() {
		super();
	}

	public static TrustServiceList newInstance() {
		// setup
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();

		// scheme operator name
		trustServiceList.setSchemeOperatorName("Fedict", Locale.ENGLISH);
		trustServiceList.setSchemeOperatorName("Fedict", new Locale("nl"));
		trustServiceList.setSchemeOperatorName("Fedict", Locale.FRENCH);
		trustServiceList.setSchemeOperatorName("Fedict", Locale.GERMAN);

		// scheme operator postal address
		PostalAddressType schemeOperatorPostalAddress = new PostalAddressType();
		schemeOperatorPostalAddress
				.setStreetAddress("Maria-Theresiastraat 1/3");
		schemeOperatorPostalAddress.setLocality("Brussels");
		schemeOperatorPostalAddress.setStateOrProvince("Brussels");
		schemeOperatorPostalAddress.setPostalCode("1000");
		schemeOperatorPostalAddress.setCountryName("Belgium");
		trustServiceList.setSchemeOperatorPostalAddress(
				schemeOperatorPostalAddress, Locale.ENGLISH);

		schemeOperatorPostalAddress
				.setStreetAddress("Maria-Theresiastraat 1/3");
		schemeOperatorPostalAddress.setLocality("Brussel");
		schemeOperatorPostalAddress.setStateOrProvince("Brussel");
		schemeOperatorPostalAddress.setPostalCode("1000");
		schemeOperatorPostalAddress.setCountryName("België");
		trustServiceList.setSchemeOperatorPostalAddress(
				schemeOperatorPostalAddress, new Locale("nl"));

		// scheme operator electronic address
		List<String> electronicAddresses = new LinkedList<String>();
		electronicAddresses.add("http://www.fedict.belgium.be/");
		electronicAddresses.add("mailto://eid@belgium.be");
		trustServiceList
				.setSchemeOperatorElectronicAddresses(electronicAddresses);

		// scheme name
		trustServiceList
				.setSchemeName(
						"BE:Supervision/Accreditation Status List of certification services from Certification Service Providers, which are supervised/accredited by the referenced Scheme Operator Member State for compliance with the relevant provisions laid down in  Directive 1999/93/EC of the European Parliament and of the Council of 13 December 1999 on a Community framework for electronic signatures",
						Locale.ENGLISH);

		// scheme information URIs
		trustServiceList.addSchemeInformationUri("http://tsl.belgium.be/",
				Locale.ENGLISH);
		trustServiceList.addSchemeInformationUri("http://tsl.belgium.be/nl/",
				new Locale("nl"));
		trustServiceList.addSchemeInformationUri("http://tsl.belgium.be/fr/",
				Locale.FRENCH);

		// status determination approach
		trustServiceList
				.setStatusDeterminationApproach("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/StatusDetn/appropriate ");

		// scheme type
		/*
		 * The Scheme Type URIs can actually be visited. We should provide some
		 * information to ETSI for the BE schemerules.
		 */
		trustServiceList
				.addSchemeType("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/schemerules/common");
		/*
		 * The BE schemerules MUSH be provided. We can add extra paths for
		 * language. For example: http://
		 * uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/schemerules/BE/nl
		 */
		trustServiceList
				.addSchemeType("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/schemerules/BE");

		// scheme territory
		trustServiceList.setSchemeTerritory("BE");

		// legal notice
		trustServiceList
				.addLegalNotice(
						"The applicable legal framework for the present TSL implementation of the Trusted List of supervised/accredited Certification Service Providers for Belgium is the Directive 1999/93/EC of the European Parliament and of the Council of 13 December 1999 on a Community framework for electronic signatures and its implementation in Belgian laws. The applicable legal national framework is the Wet van 9 juli 2001 houdende vaststelling van bepaalde regels in verband met het juridisch kader voor elektronische handtekeningen en certificatiediensten.",
						Locale.ENGLISH);

		// historical information period
		/*
		 * Volgens de wet van 9 JULI 2001. — Wet houdende vaststelling van
		 * bepaalde regels in verband met het juridisch kader voor elektronische
		 * handtekeningen en certificatiediensten: Bijlage II - punt i) alle
		 * relevante informatie over een gekwalificeerd certificaat te
		 * registreren gedurende de nuttige termijn van dertig jaar, in het
		 * bijzonder om een certificatiebewijs te kunnen voorleggen bij
		 * gerechtelijke procedures.
		 */
		trustServiceList.setHistoricalInformationPeriod(3653 * 3);

		// list issue date time
		DateTime listIssueDateTime = new DateTime();
		trustServiceList.setListIssueDateTime(listIssueDateTime);

		// next update
		DateTime nextUpdateDateTime = listIssueDateTime.plusMonths(6);
		trustServiceList.setNextUpdate(nextUpdateDateTime);

		trustServiceList
				.addDistributionPoint("http://tsl.belgium.be/tsl-be.xml");

		// trust service provider list: certipost
		TrustServiceProvider certipostTrustServiceProvider = TrustServiceListFactory
				.createTrustServiceProvider("Certipost");
		trustServiceList.addTrustServiceProvider(certipostTrustServiceProvider);
		certipostTrustServiceProvider.addPostalAddress(Locale.ENGLISH,
				"Ninovesteenweg 196", "EREMBODEGEM", "Oost-Vlaanderen", "9320",
				"BE");
		certipostTrustServiceProvider
				.addElectronicAddress("http://www.certipost.be/",
						"mailto:eid.csp@staff.certipost.be");

		certipostTrustServiceProvider.addInformationUri(Locale.ENGLISH,
				"http://www.certipost.be");

		// certipost trust services: Root CA and Root CA2
		X509Certificate rootCaCertificate = loadCertificateFromResource("./eu/be/belgiumrca.crt");
		TrustService rootCaTrustService = TrustServiceListFactory
				.createTrustService(rootCaCertificate);
		rootCaTrustService.addOIDForQCNoSSCD("2.16.56.1.1.1.2.1", "Citizen");
		rootCaTrustService.addOIDForQCNoSSCD("2.16.56.1.1.1.7.1", "Foreigner");
		certipostTrustServiceProvider.addTrustService(rootCaTrustService);

		X509Certificate rootCa2Certificate = loadCertificateFromResource("./eu/be/belgiumrca2.crt");
		TrustService rootCa2TrustService = TrustServiceListFactory
				.createTrustService(rootCa2Certificate);
		rootCa2TrustService.addOIDForQCNoSSCD("2.16.56.9.1.1.2.1", "Citizen");
		rootCa2TrustService.addOIDForQCNoSSCD("2.16.56.9.1.1.7.1", "Foreigner");
		certipostTrustServiceProvider.addTrustService(rootCa2TrustService);

		return trustServiceList;
	}

	private static X509Certificate loadCertificateFromResource(
			String resourceName) {
		Thread currentThread = Thread.currentThread();
		ClassLoader classLoader = currentThread.getContextClassLoader();
		InputStream certificateInputStream = classLoader
				.getResourceAsStream(resourceName);
		if (null == certificateInputStream) {
			throw new IllegalArgumentException(
					"could not load certificate resource: " + resourceName);
		}
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) certificateFactory
					.generateCertificate(certificateInputStream);
			return certificate;
		} catch (CertificateException e) {
			throw new RuntimeException("certificate factory error: "
					+ e.getMessage(), e);
		}
	}
}
