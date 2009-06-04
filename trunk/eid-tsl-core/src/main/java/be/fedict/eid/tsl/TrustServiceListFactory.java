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

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.uri._02231.v2_.ObjectFactory;
import org.etsi.uri._02231.v2_.TrustStatusListType;
import org.w3c.dom.Document;

/**
 * Factory for Trust Service Lists.
 * 
 * @author fcorneli
 * 
 */
public class TrustServiceListFactory {

	private static final Log LOG = LogFactory
			.getLog(TrustServiceListFactory.class);

	private TrustServiceListFactory() {
		super();
	}

	public static TrustServiceList newInstance(File tslFile) throws IOException {
		if (null == tslFile) {
			throw new IllegalArgumentException();
		}
		TrustStatusListType trustServiceStatusList;
		try {
			trustServiceStatusList = parseTslDocument(tslFile);
		} catch (JAXBException e) {
			throw new IOException("TSL parse error: " + e.getMessage(), e);
		}
		return new TrustServiceList(trustServiceStatusList);
	}

	public static TrustServiceList newInstance(Document tslDocument)
			throws IOException {
		if (null == tslDocument) {
			throw new IllegalArgumentException();
		}
		TrustStatusListType trustServiceStatusList;
		try {
			trustServiceStatusList = parseTslDocument(tslDocument);
		} catch (JAXBException e) {
			throw new IOException("TSL parse error: " + e.getMessage(), e);
		}
		return new TrustServiceList(trustServiceStatusList);
	}

	private static TrustStatusListType parseTslDocument(File tslFile)
			throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller();
		JAXBElement<TrustStatusListType> jaxbElement = (JAXBElement<TrustStatusListType>) unmarshaller
				.unmarshal(tslFile);
		TrustStatusListType trustServiceStatusList = jaxbElement.getValue();
		return trustServiceStatusList;
	}

	private static TrustStatusListType parseTslDocument(Document tslDocument)
			throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller();
		JAXBElement<TrustStatusListType> jaxbElement = (JAXBElement<TrustStatusListType>) unmarshaller
				.unmarshal(tslDocument);
		TrustStatusListType trustServiceStatusList = jaxbElement.getValue();
		return trustServiceStatusList;
	}

	private static Unmarshaller getUnmarshaller() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller;
	}
}
