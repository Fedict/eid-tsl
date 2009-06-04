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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;

public class TrustServiceListFactoryTest {

	@Test
	public void testNewInstanceRequiresArgument() throws Exception {
		try {
			TrustServiceListFactory.newInstance(null);
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
