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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import be.fedict.eid.tsl.BelgianTrustServiceListFactory;
import be.fedict.eid.tsl.BelgianTrustServiceListFactory.Trimester;
import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;
import be.fedict.eid.tsl.TrustServiceProvider;
import be.fedict.eid.tsl.jaxb.tsl.PostalAddressType;

public class BelgianTrustServiceListFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(BelgianTrustServiceListFactoryTest.class);

	@Before
	public void setUp() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Test
	public void testBelgianTrustList() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2010, Trimester.FIRST);

		assertNotNull(trustServiceList.getType());

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);

		// sign trust list
		KeyPair keyPair = TrustTestUtils.generateKeyPair(2048);
		PrivateKey privateKey = keyPair.getPrivate();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(5);
		X509Certificate certificate = TrustTestUtils
				.generateSelfSignedCertificate(keyPair,
						"C=BE, CN=Belgium Trust List Scheme Operator",
						notBefore, notAfter);
		trustServiceList.sign(privateKey, certificate);

		// operate
		File tmpTslFile = File.createTempFile("tsl-be-", ".xml");
		// tmpTslFile.deleteOnExit();
		trustServiceList.saveAs(tmpTslFile);

		// --------------- VERIFY TRUST LIST --------------------
		LOG.debug("TSL: " + FileUtils.readFileToString(tmpTslFile));
		Document document = TrustTestUtils.loadDocument(tmpTslFile);

		// XML schema validation
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		LSResourceResolver resourceResolver = new TSLLSResourceResolver();
		factory.setResourceResolver(resourceResolver);
		InputStream tslSchemaInputStream = BelgianTrustServiceListFactoryTest.class
				.getResourceAsStream("/ts_102231v030102_xsd.xsd");
		Source tslSchemaSource = new StreamSource(tslSchemaInputStream);
		Schema tslSchema = factory.newSchema(tslSchemaSource);
		Validator tslValidator = tslSchema.newValidator();
		tslValidator.validate(new DOMSource(document));

		Validator eccValidator = factory.newSchema(
				BelgianTrustServiceListFactoryTest.class
						.getResource("/ts_102231v030102_sie_xsd.xsd"))
				.newValidator();
		NodeList eccQualificationsNodeList = document
				.getElementsByTagNameNS(
						"http://uri.etsi.org/TrstSvc/SvcInfoExt/eSigDir-1999-93-EC-TrustedList/#",
						"Qualifications");
		for (int idx = 0; idx < eccQualificationsNodeList.getLength(); idx++) {
			Node eccQualificationsNode = eccQualificationsNodeList.item(idx);
			eccValidator.validate(new DOMSource(eccQualificationsNode));
		}

		Validator xadesValidator = factory.newSchema(
				BelgianTrustServiceListFactoryTest.class
						.getResource("/XAdES.xsd")).newValidator();
		NodeList xadesQualifyingPropertiesNodeList = document
				.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.3.2#",
						"QualifyingProperties");
		for (int idx = 0; idx < xadesQualifyingPropertiesNodeList.getLength(); idx++) {
			Node xadesQualifyingPropertiesNode = xadesQualifyingPropertiesNodeList
					.item(idx);
			xadesValidator
					.validate(new DOMSource(xadesQualifyingPropertiesNode));
		}

		// signature
		trustServiceList = TrustServiceListFactory.newInstance(tmpTslFile);
		X509Certificate resultCertificate = trustServiceList.verifySignature();
		assertEquals(certificate, resultCertificate);

		File pdfExportFile = File.createTempFile("tsl-be-", ".pdf");
		trustServiceList.humanReadableExport(pdfExportFile);

		// scheme operator name
		String schemeOperatorNameEn = trustServiceList
				.getSchemeOperatorName(Locale.ENGLISH);
		assertEquals(
				"FPS Economy, SMEs, Self-employed and Energy - Quality and Security - Information Management",
				schemeOperatorNameEn);
		LOG.debug("Locale.ENGLISH: " + Locale.ENGLISH.getLanguage());
		assertEquals(
				"SPF Economie, PME, Classes moyennes et Energie - Qualité et Sécurité - Information Management",
				trustServiceList.getSchemeOperatorName(Locale.FRENCH));

		Node schemeOperatorNameEnNode = XPathAPI
				.selectSingleNode(
						document,
						"tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:SchemeOperatorName/tsl:Name[@xml:lang='en']");
		assertNotNull(schemeOperatorNameEnNode);
		assertEquals(
				"FPS Economy, SMEs, Self-employed and Energy - Quality and Security - Information Management",
				schemeOperatorNameEnNode.getTextContent());

		// scheme operator postal address
		PostalAddressType resultPostalAddress = trustServiceList
				.getSchemeOperatorPostalAddress(Locale.ENGLISH);
		assertNotNull(resultPostalAddress);
		assertEquals("NG III - Koning Albert II-laan 16",
				resultPostalAddress.getStreetAddress());
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
		// assertEquals(3, schemeInformationUris.size());
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
		assertEquals(new Integer(3653 * 3),
				trustServiceList.getHistoricalInformationPeriod());

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
		assertEquals("Certipost NV/SA",
				certipostTrustServiceProvider.getName(Locale.ENGLISH));

		// postal address
		PostalAddressType certipostPostalAddress = certipostTrustServiceProvider
				.getPostalAddress(Locale.ENGLISH);
		assertNotNull(certipostPostalAddress);
		assertEquals("Muntcentrum", certipostPostalAddress.getStreetAddress());
		assertEquals("BE", certipostPostalAddress.getCountryName());

		// electronic address
		List<String> resultElectronicAddress = certipostTrustServiceProvider
				.getElectronicAddress();
		assertEquals(2, resultElectronicAddress.size());

		// information uri
		List<String> resultInformationUris = certipostTrustServiceProvider
				.getInformationUris(Locale.ENGLISH);
		assertEquals(2, resultInformationUris.size());
		assertEquals("http://repository.eid.belgium.be/EN/Index.htm",
				resultInformationUris.get(0));

		LOG.debug("unsigned TSL: " + unsignedTslFile.getAbsolutePath());
		LOG.debug("TSL: " + tmpTslFile.getAbsolutePath());
		LOG.debug("PDF: " + pdfExportFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester2_2010() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2010, Trimester.SECOND);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester3_2010() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2010, Trimester.THIRD);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester1_2011() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2011, Trimester.FIRST);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester1_2012() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2012, Trimester.FIRST);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester2_2012() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2012, Trimester.SECOND);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester3_2012() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2012, Trimester.THIRD);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester1_2013() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2013, Trimester.FIRST);

		File unsignedTslFile = File.createTempFile("TSL-BE-2013-T1-candidate-",
				".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	@Test
	public void testBelgianTrustListTrimester2_2011() throws Exception {
		// setup
		TrustServiceList trustServiceList = BelgianTrustServiceListFactory
				.newInstance(2011, Trimester.SECOND);

		File unsignedTslFile = File.createTempFile("tsl-be-unsigned-", ".xml");
		trustServiceList.saveAs(unsignedTslFile);
		LOG.debug("unsigned TSL file: " + unsignedTslFile.getAbsolutePath());
	}

	private static class TSLLSResourceResolver implements LSResourceResolver {

		private static final Log LOG = LogFactory
				.getLog(TSLLSResourceResolver.class);

		public LSInput resolveResource(String type, String namespaceURI,
				String publicId, String systemId, String baseURI) {
			LOG.debug("resolve resource");
			LOG.debug("type: " + type);
			LOG.debug("namespace URI: " + namespaceURI);
			LOG.debug("publicId: " + publicId);
			LOG.debug("systemId: " + systemId);
			LOG.debug("base URI: " + baseURI);
			if ("http://www.w3.org/2001/xml.xsd".equals(systemId)) {
				return new LocalLSInput(publicId, systemId, baseURI, "/xml.xsd");
			}
			if ("http://www.w3.org/TR/2002/REC-xmldsig-core-20020212/xmldsig-core-schema.xsd"
					.equals(systemId)) {
				return new LocalLSInput(publicId, systemId, baseURI,
						"/xmldsig-core-schema.xsd");
			}
			if ("http://uri.etsi.org/01903/v1.3.2/XAdES.xsd".equals(systemId)) {
				return new LocalLSInput(publicId, systemId, baseURI,
						"/XAdES.xsd");
			}
			return null;
		}
	}

	private static class LocalLSInput implements LSInput {

		private String publicId;

		private String systemId;

		private String baseURI;

		private final String schemaResourceName;

		public LocalLSInput(String publicId, String systemId, String baseURI,
				String schemaResourceName) {
			this.publicId = publicId;
			this.systemId = systemId;
			this.baseURI = baseURI;
			this.schemaResourceName = schemaResourceName;
		}

		public String getBaseURI() {
			return this.baseURI;
		}

		public InputStream getByteStream() {
			InputStream inputStream = BelgianTrustServiceListFactoryTest.class
					.getResourceAsStream(this.schemaResourceName);
			return inputStream;
		}

		public boolean getCertifiedText() {
			return true;
		}

		public Reader getCharacterStream() {
			InputStream inputStream = getByteStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			return reader;
		}

		public String getEncoding() {
			return "UTF-8";
		}

		public String getPublicId() {
			return this.publicId;
		}

		public String getStringData() {
			InputStream inputStream = getByteStream();
			String stringData;
			try {
				stringData = IOUtils.toString(inputStream);
			} catch (IOException e) {
				throw new RuntimeException("I/O error: " + e.getMessage(), e);
			}
			return stringData;
		}

		public String getSystemId() {
			return this.systemId;
		}

		public void setBaseURI(String baseURI) {
			this.baseURI = baseURI;
		}

		public void setByteStream(InputStream byteStream) {
			throw new UnsupportedOperationException();
		}

		public void setCertifiedText(boolean certifiedText) {
			throw new UnsupportedOperationException();
		}

		public void setCharacterStream(Reader characterStream) {
			throw new UnsupportedOperationException();
		}

		public void setEncoding(String encoding) {
			throw new UnsupportedOperationException();
		}

		public void setPublicId(String publicId) {
			this.publicId = publicId;
		}

		public void setStringData(String stringData) {
			throw new UnsupportedOperationException();
		}

		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}
	}
}