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
import org.joda.time.DateTimeZone;

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

	public static enum Trimester {
		FIRST, SECOND, THIRD
	}

	/**
	 * Creates a new instance of a trust service list for Belgium according to
	 * the given time frame.
	 * 
	 * @param year
	 *            the year for which the TSL should be valid.
	 * @param trimester
	 *            the trimester for which the TSL should be valid.
	 * @return the trust service list object.
	 */
	public static TrustServiceList newInstance(int year, Trimester trimester) {
		if (2010 != year || Trimester.FIRST != trimester) {
			throw new IllegalArgumentException("cannot create a TSL for year: "
					+ year + " trimester " + trimester);
		}

		// setup
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();

		// scheme operator name
		trustServiceList
				.setSchemeOperatorName(
						"FPS Economy, SMEs, Self-employed and Energy - Quality and Security - Information Management",
						Locale.ENGLISH);
		trustServiceList
				.setSchemeOperatorName(
						"FOD Economie, KMO, Middenstand en Energie - Kwaliteit en Veiligheid - Information Management",
						new Locale("nl"));
		trustServiceList
				.setSchemeOperatorName(
						"SPF Economie, PME, Classes moyennes et Energie - Qualité et Sécurité - Information Management",
						Locale.FRENCH);
		trustServiceList
				.setSchemeOperatorName(
						"FÖD Wirtschaft, KMU, Mittelstand und Energie - Qualität und Sicherheit - Informationsmanagement",
						Locale.GERMAN);

		// scheme operator postal address
		PostalAddressType schemeOperatorPostalAddress = new PostalAddressType();
		schemeOperatorPostalAddress
				.setStreetAddress("NG III - Koning Albert II-laan 16");
		schemeOperatorPostalAddress.setLocality("Brussels");
		schemeOperatorPostalAddress.setStateOrProvince("Brussels");
		schemeOperatorPostalAddress.setPostalCode("1000");
		schemeOperatorPostalAddress.setCountryName("BE"); // this one containing
		// an EU country
		// code
		trustServiceList.setSchemeOperatorPostalAddress(
				schemeOperatorPostalAddress, Locale.ENGLISH);

		schemeOperatorPostalAddress
				.setStreetAddress("NG III - Koning Albert II-laan 16");
		schemeOperatorPostalAddress.setLocality("Brussel");
		schemeOperatorPostalAddress.setStateOrProvince("Brussel");
		schemeOperatorPostalAddress.setPostalCode("1000");
		schemeOperatorPostalAddress.setCountryName("BE"); // this one containing
		// an EU country
		// code
		trustServiceList.setSchemeOperatorPostalAddress(
				schemeOperatorPostalAddress, new Locale("nl"));

		// scheme operator electronic address
		List<String> electronicAddresses = new LinkedList<String>();
		electronicAddresses.add("http://economie.fgov.be");
		electronicAddresses.add("mailto://be.sign@economie.fgov.be");
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
				.setStatusDeterminationApproach(TrustServiceList.STATUS_DETERMINATION_APPROPRIATE);

		// scheme type
		/*
		 * The Scheme Type URIs can actually be visited. We should provide some
		 * information to ETSI for the BE schemerules.
		 */
		trustServiceList.addSchemeType(TrustServiceList.SCHEME_RULE_COMMON);
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
						"The applicable legal framework for the present TSL implementation of the Trusted List of supervised/accredited Certification Service Providers for Belgium is the Directive 1999/93/EC of the European Parliament and of the Council of 13 December 1999 on a Community framework for electronic signatures and its implementation in Belgian laws. The applicable legal national framework is the Belgian CSP act of 9 July 2001 to create a legal framework for the usage of electronic signatures and certification services.",
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
		DateTime listIssueDateTime = new DateTime(2010, 1, 1, 0, 0, 0, 0,
				DateTimeZone.UTC);
		trustServiceList.setListIssueDateTime(listIssueDateTime);

		// next update
		int operationalOverlapWeeks = 2;
		DateTime nextUpdateDateTime = listIssueDateTime.plusMonths(12 / 3)
				.plusWeeks(operationalOverlapWeeks);
		trustServiceList.setNextUpdate(nextUpdateDateTime);

		trustServiceList
				.addDistributionPoint("http://tsl.belgium.be/tsl-be.xml");

		// trust service provider list: certipost
		TrustServiceProvider certipostTrustServiceProvider = TrustServiceListFactory
				.createTrustServiceProvider("Certipost", "Certipost België");
		trustServiceList.addTrustServiceProvider(certipostTrustServiceProvider);
		certipostTrustServiceProvider.addPostalAddress(Locale.ENGLISH,
				"Ninovesteenweg 196", "EREMBODEGEM", "Oost-Vlaanderen", "9320",
				"BE");
		certipostTrustServiceProvider
				.addElectronicAddress("http://www.certipost.be/",
						"mailto:eid.csp@staff.certipost.be");

		certipostTrustServiceProvider.addInformationUri(Locale.ENGLISH,
				"http://www.certipost.be");

		// Certipost trust services: Root CA and Root CA2
		X509Certificate rootCaCertificate = loadCertificateFromResource("eu/be/belgiumrca.crt");
		TrustService rootCaTrustService = TrustServiceListFactory
				.createTrustService(rootCaCertificate);
		rootCaTrustService.addOIDForQCSSCDStatusAsInCert("2.16.56.1.1.1.2.1",
				"Citizen");
		rootCaTrustService.addOIDForQCSSCDStatusAsInCert("2.16.56.1.1.1.7.1",
				"Foreigner");
		certipostTrustServiceProvider.addTrustService(rootCaTrustService);

		X509Certificate rootCa2Certificate = loadCertificateFromResource("eu/be/belgiumrca2.crt");
		TrustService rootCa2TrustService = TrustServiceListFactory
				.createTrustService(rootCa2Certificate);
		rootCa2TrustService.addOIDForQCSSCDStatusAsInCert("2.16.56.9.1.1.2.1",
				"Citizen");
		rootCa2TrustService.addOIDForQCSSCDStatusAsInCert("2.16.56.9.1.1.7.1",
				"Foreigner");
		certipostTrustServiceProvider.addTrustService(rootCa2TrustService);

		// Certipost eTrust trust services
		X509Certificate eTrustQCaCertificate = loadCertificateFromResource("eu/be/etrust/QCA_Self_Signed.crt");
		TrustService eTrustQCaTrustService = TrustServiceListFactory
				.createTrustService(eTrustQCaCertificate);
		eTrustQCaTrustService.addOIDForQCForLegalPerson("0.3.2062.7.1.1.112.1");
		eTrustQCaTrustService.addOIDForQCForLegalPerson("0.3.2062.7.1.1.140.1");
		eTrustQCaTrustService.addOIDForQCForLegalPerson("0.3.2062.7.1.1.111.1");
		certipostTrustServiceProvider.addTrustService(eTrustQCaTrustService);

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
