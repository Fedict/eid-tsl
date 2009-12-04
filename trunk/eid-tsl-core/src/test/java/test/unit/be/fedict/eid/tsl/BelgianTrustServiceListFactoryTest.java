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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.etsi.uri._02231.v2_.PostalAddressType;
import org.joda.time.DateTime;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.fedict.eid.tsl.BelgianTrustServiceListFactory;
import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;
import be.fedict.eid.tsl.TrustServiceProvider;
import be.fedict.eid.tsl.BelgianTrustServiceListFactory.Semester;

public class BelgianTrustServiceListFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(BelgianTrustServiceListFactoryTest.class);

	@Test
	public void testBelgianTrustList() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2010, Semester.FIRST);

		assertNotNull(trustServiceList.getType());
		
		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.save(unsignedTslFile);

		// sign trust list
		KeyPair keyPair = TrustTestUtils.generateKeyPair(2048);
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair,
						"C=BE, CN=Belgium Trust List Scheme Operator",
						notBefore, notAfter);
		trustServiceList.sign(privateKey, certificate);

		// operate
		File tmpTslFile = File.createTempFile("tsl-be-", ".xml");
		// tmpTslFile.deleteOnExit();
		trustServiceList.save(tmpTslFile);

		// --------------- VERIFY TRUST LIST --------------------
		LOG.debug("TSL: " + FileUtils.readFileToString(tmpTslFile));
		Document document = TrustTestUtils.loadDocument(tmpTslFile);

		// signature
		trustServiceList = TrustServiceListFactory.newInstance(tmpTslFile);
		X509Certificate resultCertificate = trustServiceList.verifySignature();
		assertEquals(certificate, resultCertificate);

		File pdfExportFile = File.createTempFile("tsl-be-", ".pdf");
		trustServiceList.humanReadableExport(pdfExportFile);

		// scheme operator name
		String schemeOperatorNameEn = trustServiceList
				.getSchemeOperatorName(Locale.ENGLISH);
		assertEquals("Fedict", schemeOperatorNameEn);
		LOG.debug("Locale.ENGLISH: " + Locale.ENGLISH.getLanguage());
		assertEquals("Fedict", trustServiceList
				.getSchemeOperatorName(Locale.FRENCH));

		Node schemeOperatorNameEnNode = XPathAPI
				.selectSingleNode(
						document,
						"tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:SchemeOperatorName/tsl:Name[@xml:lang='en']");
		assertNotNull(schemeOperatorNameEnNode);
		assertEquals("Fedict", schemeOperatorNameEnNode.getTextContent());

		// scheme operator postal address
		PostalAddressType resultPostalAddress = trustServiceList
				.getSchemeOperatorPostalAddress(Locale.ENGLISH);
		assertNotNull(resultPostalAddress);
		assertEquals("Maria-Theresiastraat 1/3", resultPostalAddress
				.getStreetAddress());
		assertEquals("Brussels", resultPostalAddress.getLocality());
		assertEquals("Brussel", trustServiceList
				.getSchemeOperatorPostalAddress(new Locale("nl")).getLocality());

		// scheme operator electronic address
		assertEquals(2, trustServiceList.getSchemeOperatorElectronicAddresses()
				.size());
		LOG.debug("electronic addresses: "
				+ trustServiceList.getSchemeOperatorElectronicAddresses());

		// scheme name
		assertTrue(trustServiceList.getSchemeName(Locale.ENGLISH).startsWith(
				"BE:"));

		// scheme information uri
		List<String> schemeInformationUris = trustServiceList
				.getSchemeInformationUris();
		assertNotNull(schemeInformationUris);
		assertEquals(3, schemeInformationUris.size());
		assertEquals("http://tsl.belgium.be/", schemeInformationUris.get(0));

		// status determination approach
		assertEquals(
				"http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/StatusDetn/appropriate",
				trustServiceList.getStatusDeterminationApproach());

		// scheme types
		List<String> schemeTypes = trustServiceList.getSchemeTypes();
		assertNotNull(schemeTypes);
		assertEquals(2, schemeTypes.size());

		// scheme territory
		assertEquals("BE", trustServiceList.getSchemeTerritory());

		// legal notice
		String resultLegalNotice = trustServiceList
				.getLegalNotice(Locale.ENGLISH);
		assertNotNull(resultLegalNotice);
		assertTrue(resultLegalNotice.indexOf("1999/93/EC") != -1);
		assertTrue(resultLegalNotice.indexOf("Belgium") != -1);

		// historical information period
		assertEquals(new Integer(3653 * 3), trustServiceList
				.getHistoricalInformationPeriod());

		// list issue date time
		DateTime resultListIssueDateTime = trustServiceList
				.getListIssueDateTime();
		assertNotNull(resultListIssueDateTime);

		// next update
		DateTime resultNextUpdateDateTime = trustServiceList.getNextUpdate();
		assertNotNull(resultNextUpdateDateTime);

		// trust service provider list
		List<TrustServiceProvider> trustServiceProviders = trustServiceList
				.getTrustServiceProviders();
		assertEquals(1, trustServiceProviders.size());
		TrustServiceProvider certipostTrustServiceProvider = trustServiceProviders
				.get(0);
		assertEquals("Certipost", certipostTrustServiceProvider
				.getName(Locale.ENGLISH));

		// postal address
		PostalAddressType certipostPostalAddress = certipostTrustServiceProvider
				.getPostalAddress(Locale.ENGLISH);
		assertNotNull(certipostPostalAddress);
		assertEquals("Ninovesteenweg 196", certipostPostalAddress
				.getStreetAddress());
		assertEquals("BE", certipostPostalAddress.getCountryName());

		// electronic address
		List<String> resultElectronicAddress = certipostTrustServiceProvider
				.getElectronicAddress();
		assertEquals(2, resultElectronicAddress.size());

		// information uri
		String resultInformationUri = certipostTrustServiceProvider
				.getInformationUri(Locale.ENGLISH);
		assertEquals("http://www.certipost.be", resultInformationUri);

		LOG.debug("unsigned TSL: " + unsignedTslFile.getAbsolutePath());
		LOG.debug("TSL: " + tmpTslFile.getAbsolutePath());
		LOG.debug("PDF: " + pdfExportFile.getAbsolutePath());
	}

}
