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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
