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

package test.unit.be.fedict.eid.tsl;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.uri._02231.v2_.PostalAddressType;
import org.joda.time.DateTime;
import org.junit.Test;

import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;

/**
 * ETSI TSL Lifecycle tests.
 * 
 * @author fcorneli
 * @see http://xades-portal.etsi.org/protected/TSLlifecycle/
 */
public class EtsiTslLifecycleTest {

	private static final Log LOG = LogFactory
			.getLog(EtsiTslLifecycleTest.class);

	@Test
	public void testLC001() throws Exception {
		// setup
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();

		// scheme operator name
		trustServiceList.setSchemeOperatorName("BE:Fedict", Locale.ENGLISH);
		trustServiceList.setSchemeOperatorName("BE:Fedict", new Locale("nl"));
		trustServiceList.setSchemeOperatorName("BE:Fedict", Locale.FRENCH);
		trustServiceList.setSchemeOperatorName("BE:Fedict", Locale.GERMAN);

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
		trustServiceList.addSchemeInformationUri(
				"https://www.e-contract.be/tsl/", Locale.ENGLISH);

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
						"The applicable legal framework for the present TSL implementation of the Trusted List of supervised/accredited Certification Service Providers for Belgium is the Directive 1999/93/EC of the European Parliament and of the Council of 13 December 1999 on a Community framework for electronic signatures and its implementation in Belgian laws.",
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

		// distribution point
		File tslFile = File.createTempFile("tsl-LC001-", ".xml");
		trustServiceList.addDistributionPoint("https://www.e-contract.be/tsl/"
				+ tslFile.getName());

		// sign TSL
		KeyPair keyPair = TrustTestUtils.generateKeyPair(2048);
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair,
						"C=BE, CN=Belgium Trust List Scheme Operator",
						notBefore, notAfter);
		trustServiceList.sign(privateKey, certificate);

		// save TSL
		trustServiceList.save(tslFile);
		LOG.debug("TSL file: " + tslFile.getAbsolutePath());
	}

	@Test
	public void testLC002() throws Exception {
		// setup
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();

		// scheme operator name
		trustServiceList.setSchemeOperatorName("BE:Fedict", Locale.ENGLISH);
		trustServiceList.setSchemeOperatorName("BE:Fedict", new Locale("nl"));
		trustServiceList.setSchemeOperatorName("BE:Fedict", Locale.FRENCH);
		trustServiceList.setSchemeOperatorName("BE:Fedict", Locale.GERMAN);

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
		trustServiceList.addSchemeInformationUri(
				"https://www.e-contract.be/tsl/", Locale.ENGLISH);

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
						"The applicable legal framework for the present TSL implementation of the Trusted List of supervised/accredited Certification Service Providers for Belgium is the Directive 1999/93/EC of the European Parliament and of the Council of 13 December 1999 on a Community framework for electronic signatures and its implementation in Belgian laws.",
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

		// distribution point
		File tslFile = File.createTempFile("tsl-LC002-", ".xml");
		trustServiceList.addDistributionPoint("https://www.e-contract.be/tsl/"
				+ tslFile.getName());

		// sign TSL
		KeyPair keyPair = TrustTestUtils.generateKeyPair(2048);
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair,
						"C=BE, CN=Belgium Trust List Scheme Operator",
						notBefore, notAfter);
		trustServiceList.sign(privateKey, certificate);

		// save TSL
		trustServiceList.save(tslFile);
		LOG.debug("TSL file: " + tslFile.getAbsolutePath());
	}
}
