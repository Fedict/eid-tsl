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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.etsi.uri._02231.v2_.PostalAddressType;
import org.joda.time.DateTime;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;
import be.fedict.eid.tsl.TrustServiceProvider;

public class TrustServiceListFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(TrustServiceListFactoryTest.class);

	@Test
	public void testNewInstanceRequiresArgument() throws Exception {
		try {
			TrustServiceListFactory.newInstance((Document) null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testParseTsl() throws Exception {
		// setup
		Document tslDocument = loadDocumentFromResource("tsl-unsigned-1.xml");

		// operate
		TrustServiceList result = TrustServiceListFactory
				.newInstance(tslDocument);

		// verify
		assertNotNull(result);
		assertFalse(result.hasChanged());
		assertEquals("BE:Belgium Trust-service Status List - TEST VERSION",
				result.getSchemeName());
		assertEquals("FedICT", result.getSchemeOperatorName());
		assertNotNull(result.getTrustServiceProviders());
		assertEquals(1, result.getTrustServiceProviders().size());
		TrustServiceProvider trustServiceProvider = result
				.getTrustServiceProviders().get(0);
		assertEquals("Certipost", trustServiceProvider.getName());
		assertNull(result.verifySignature());
	}

	@Test
	public void testVerifySignature() throws Exception {
		// setup
		Document tslDocument = loadDocumentFromResource("tsl-signed-1.xml");

		// operate
		TrustServiceList result = TrustServiceListFactory
				.newInstance(tslDocument);

		// verify
		assertNotNull(result);
		assertNotNull(result.verifySignature());
		assertFalse(result.hasChanged());
		LOG.debug("signer: "
				+ result.verifySignature().getSubjectX500Principal());
	}

	@Test
	public void testNewEmptyTsl() throws Exception {
		// operate
		TrustServiceList result = TrustServiceListFactory.newInstance();

		// verify
		assertNotNull(result);

		assertTrue(result.hasChanged());
		assertNull(result.getSchemeName());
		assertNull(result.getIssueDate());
		assertNull(result.getSchemeOperatorName());
		assertNull(result.getSequenceNumber());
		List<TrustServiceProvider> trustServiceProviders = result
				.getTrustServiceProviders();
		assertNotNull(trustServiceProviders);
		assertTrue(trustServiceProviders.isEmpty());
	}

	@Test
	public void testSetSchemeNameOnNewTsl() throws Exception {
		// setup
		String schemeName = "test-scheme-name";
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();

		// operate
		assertTrue(trustServiceList.hasChanged());
		trustServiceList.setSchemeName(schemeName);

		// verify
		assertEquals(schemeName, trustServiceList.getSchemeName());
		assertTrue(trustServiceList.hasChanged());
	}

	@Test
	public void testSetLocaleSchemeNames() throws Exception {
		// setup
		String schemeNameEn = "test-scheme-name";
		String schemeNameNl = "test-schema-naam";
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();

		// operate
		trustServiceList.setSchemeName(schemeNameEn, Locale.ENGLISH);
		trustServiceList.setSchemeName(schemeNameNl, new Locale("nl"));

		// verify
		assertEquals(schemeNameEn, trustServiceList
				.getSchemeName(Locale.ENGLISH));
		assertEquals(schemeNameNl, trustServiceList.getSchemeName(new Locale(
				"nl")));
	}

	@Test
	public void testSaveExistingTsl() throws Exception {
		Document tslDocument = loadDocumentFromResource("tsl-signed-1.xml");
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance(tslDocument);
		File tmpTslFile = File.createTempFile("tsl-", ".xml");
		tmpTslFile.deleteOnExit();

		// operate
		assertFalse(trustServiceList.hasChanged());
		trustServiceList.save(tmpTslFile);

		// verify
		assertFalse(trustServiceList.hasChanged());
		trustServiceList = TrustServiceListFactory.newInstance(tmpTslFile);
		assertEquals("BE:Belgium Trust-service Status List - TEST VERSION",
				trustServiceList.getSchemeName());
		assertNotNull(trustServiceList.verifySignature());
	}

	/**
	 * This unit test saves an empty trust list and verifies whether all
	 * required elements are present.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaveNewTsl() throws Exception {
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();
		File tmpTslFile = File.createTempFile("tsl-", ".xml");
		tmpTslFile.deleteOnExit();

		// operate
		assertTrue(trustServiceList.hasChanged());
		trustServiceList.save(tmpTslFile);

		// verify
		assertFalse(trustServiceList.hasChanged());
		trustServiceList = TrustServiceListFactory.newInstance(tmpTslFile);
		LOG.debug(FileUtils.readFileToString(tmpTslFile));
		Document document = loadDocument(tmpTslFile);

		// verify: TSLTag
		Node tslTagNode = XPathAPI.selectSingleNode(document,
				"tsl:TrustServiceStatusList/@TSLTag");
		assertNotNull(tslTagNode);
		LOG.debug("tsl tag node: " + tslTagNode.getNodeValue());
		assertEquals("http://uri.etsi.org/02231/TSLtag", tslTagNode
				.getNodeValue());

		// verify: version
		Node versionNode = XPathAPI
				.selectSingleNode(document,
						"tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:TSLVersionIdentifier");
		assertNotNull(versionNode);
		assertEquals("3", versionNode.getTextContent());

		// verify: sequence number
		Node sequenceNumberNode = XPathAPI
				.selectSingleNode(document,
						"tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:TSLSequenceNumber");
		assertNotNull(sequenceNumberNode);
		new BigInteger(sequenceNumberNode.getTextContent());
		LOG
				.debug("TSL sequence number: "
						+ sequenceNumberNode.getTextContent());

		// verify: TSL type
		Node typeNode = XPathAPI.selectSingleNode(document,
				"tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:TSLType");
		assertNotNull(typeNode);
		assertEquals(
				"http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/TSLtype/generic",
				typeNode.getTextContent());
	}

	@Test
	public void testSaveChangedTsl() throws Exception {
		Document tslDocument = loadDocumentFromResource("tsl-signed-1.xml");
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance(tslDocument);
		String schemeName = "test-scheme-name";
		File tmpTslFile = File.createTempFile("tsl-", ".xml");
		tmpTslFile.deleteOnExit();

		// operate
		assertFalse(trustServiceList.hasChanged());
		trustServiceList.setSchemeName(schemeName);
		assertTrue(trustServiceList.hasChanged());
		trustServiceList.save(tmpTslFile);
		assertFalse(trustServiceList.hasChanged());

		// verify
		trustServiceList = TrustServiceListFactory.newInstance(tmpTslFile);
		assertEquals(schemeName, trustServiceList.getSchemeName());
		assertNull(trustServiceList.verifySignature());
	}

	@Test
	public void testSetSchemeNameOnExistingTsl() throws Exception {
		// setup
		Document tslDocument = loadDocumentFromResource("tsl-signed-1.xml");
		String schemeName = "test-scheme-name";
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance(tslDocument);

		// operate
		trustServiceList.setSchemeName(schemeName);

		// verify
		assertEquals(schemeName, trustServiceList.getSchemeName());
		assertNull(trustServiceList.verifySignature());
	}

	@Test
	public void testResignExistingTsl() throws Exception {
		// setup
		Document tslDocument = loadDocumentFromResource("tsl-signed-1.xml");
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance(tslDocument);

		KeyPair keyPair = TrustTestUtils.generateKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test", notBefore,
						notAfter);

		// operate
		assertFalse(trustServiceList.hasChanged());
		trustServiceList.sign(privateKey, certificate);

		// verify
		assertEquals(certificate, trustServiceList.verifySignature());
		assertTrue(trustServiceList.hasChanged());
	}

	@Test
	public void testResignChangedTsl() throws Exception {
		// setup
		Document tslDocument = loadDocumentFromResource("tsl-signed-1.xml");
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance(tslDocument);
		String schemeName = "test-scheme-name";

		KeyPair keyPair = TrustTestUtils.generateKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test", notBefore,
						notAfter);

		// operate
		assertFalse(trustServiceList.hasChanged());
		trustServiceList.setSchemeName(schemeName);
		assertTrue(trustServiceList.hasChanged());
		trustServiceList.sign(privateKey, certificate);
		assertTrue(trustServiceList.hasChanged());

		// verify
		assertEquals(certificate, trustServiceList.verifySignature());
		assertEquals(schemeName, trustServiceList.getSchemeName());
	}

	@Test
	public void testBelgianTrustList() throws Exception {
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
		trustServiceList.addSchemeInformationUri(
				"http://tsl.fedict.belgium.be/", Locale.ENGLISH);

		// status determination approach
		trustServiceList
				.setStatusDeterminationApproach("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/StatusDetn/appropriate ");

		// scheme type
		trustServiceList
				.addSchemeType("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/schemerules/common");
		trustServiceList
				.addSchemeType("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/schemerules/BE");

		// scheme territory
		trustServiceList.setSchemeTerritory("BE");

		// legal notice
		trustServiceList
				.addLegalNotice(
						"The applicable legal framework for the present TSL implementation of the Trusted List of supervised/accredited Certification Service Providers for Belgium is the Directive 1999/93/EC of the European Parliament and of the Council of 13 December 1999 on a Community framework for electronic signatures and its implementation in Belgium laws.",
						Locale.ENGLISH);

		// historical information period
		trustServiceList.setHistoricalInformationPeriod(3653);

		// list issue date time
		DateTime listIssueDateTime = new DateTime();
		trustServiceList.setListIssueDateTime(listIssueDateTime);

		// next update
		DateTime nextUpdateDateTime = listIssueDateTime.plusMonths(6);
		trustServiceList.setNextUpdate(nextUpdateDateTime);

		// trust service provider list
		TrustServiceProvider certipostTrustServiceProvider = TrustServiceListFactory
				.createTrustServiceProvider("Certipost");
		trustServiceList.addTrustServiceProvider(certipostTrustServiceProvider);
		certipostTrustServiceProvider.addPostalAddress(Locale.ENGLISH,
				"Ninovesteenweg 196", "EREMBODEGEM", "Oost-Vlaanderen", "9320",
				"BE");

		// operate
		File tmpTslFile = File.createTempFile("tsl-be-", ".xml");
		// tmpTslFile.deleteOnExit();
		trustServiceList.save(tmpTslFile);

		// --------------- VERIFY TRUST LIST --------------------
		LOG.debug("TSL: " + FileUtils.readFileToString(tmpTslFile));
		Document document = loadDocument(tmpTslFile);

		// scheme operator name
		trustServiceList = TrustServiceListFactory.newInstance(tmpTslFile);
		String schemeOperatorNameEn = trustServiceList
				.getSchemeOperatorName(Locale.ENGLISH);
		assertEquals("Fedict", schemeOperatorNameEn);
		LOG.debug("Locale.ENGLISH: " + Locale.ENGLISH.getLanguage());
		assertEquals("Fedict", trustServiceList
				.getSchemeOperatorName(Locale.FRENCH));

		Node schemeOperatorNameEnNode = XPathAPI
				.selectSingleNode(
						document,
						"tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:SchemeOperatorName/tsl:Name[@xml:lang='EN']");
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
		assertEquals(1, schemeInformationUris.size());
		assertEquals("http://tsl.fedict.belgium.be/", schemeInformationUris
				.get(0));

		// status determination approach
		assertEquals(
				"http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/StatusDetn/appropriate ",
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
		assertEquals(new Integer(3653), trustServiceList
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
		certipostTrustServiceProvider = trustServiceProviders.get(0);
		assertEquals("Certipost", certipostTrustServiceProvider
				.getName(Locale.ENGLISH));

		// postal address
		PostalAddressType certipostPostalAddress = certipostTrustServiceProvider
				.getPostalAddress(Locale.ENGLISH);
		assertNotNull(certipostPostalAddress);
		assertEquals("Ninovesteenweg 196", certipostPostalAddress
				.getStreetAddress());
		assertEquals("BE", certipostPostalAddress.getCountryName());

		LOG.debug("TSL: " + tmpTslFile.getAbsolutePath());
	}

	@Test
	public void testSignNewTsl() throws Exception {
		// setup
		KeyPair keyPair = TrustTestUtils.generateKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test", notBefore,
						notAfter);

		String schemeName = "test-scheme-name";
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance();
		trustServiceList.setSchemeName(schemeName);

		assertFalse(trustServiceList.hasSignature());

		// operate
		trustServiceList.sign(privateKey, certificate);

		// verify
		assertTrue(trustServiceList.hasSignature());
		assertEquals(certificate, trustServiceList.verifySignature());
	}

	private Document loadDocumentFromResource(String resourceName)
			throws ParserConfigurationException, SAXException, IOException {
		Thread currentThread = Thread.currentThread();
		ClassLoader classLoader = currentThread.getContextClassLoader();
		InputStream documentInputStream = classLoader
				.getResourceAsStream(resourceName);
		if (null == documentInputStream) {
			throw new IllegalArgumentException("resource not found: "
					+ resourceName);
		}
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document tslDocument = documentBuilder.parse(documentInputStream);
		return tslDocument;
	}

	private Document loadDocument(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.parse(file);
		return document;
	}

	private String toString(Node dom) throws TransformerException {
		Source source = new DOMSource(dom);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		/*
		 * We have to omit the ?xml declaration if we want to embed the
		 * document.
		 */
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, result);
		return stringWriter.getBuffer().toString();
	}
}
