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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.w3c.dom.Document;
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
		LOG.debug("signer: "
				+ result.verifySignature().getSubjectX500Principal());
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
}
