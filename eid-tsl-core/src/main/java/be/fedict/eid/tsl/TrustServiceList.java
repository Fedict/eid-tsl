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
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.ObjectFactory;
import org.etsi.uri._02231.v2_.TSLSchemeInformationType;
import org.etsi.uri._02231.v2_.TSPType;
import org.etsi.uri._02231.v2_.TrustServiceProviderListType;
import org.etsi.uri._02231.v2_.TrustStatusListType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Trust Service List.
 * 
 * @author fcorneli
 * 
 */
public class TrustServiceList {

	private static final Log LOG = LogFactory.getLog(TrustServiceList.class);

	private TrustStatusListType trustStatusList;

	private Document tslDocument;

	private List<TrustServiceProvider> trustServiceProviders;

	protected TrustServiceList() {
		super();
	}

	protected TrustServiceList(TrustStatusListType trustStatusList,
			Document tslDocument) {
		this.trustStatusList = trustStatusList;
		this.tslDocument = tslDocument;
	}

	public String getSchemeName() {
		Locale locale = Locale.getDefault();
		return getSchemeName(locale);
	}

	/**
	 * Sets the scheme name according to the default locale.
	 * 
	 * @param schemeName
	 */
	public void setSchemeName(String schemeName) {
		Locale locale = Locale.getDefault();
		setSchemeName(schemeName, locale);
	}

	public void setSchemeName(String schemeName, Locale locale) {
		/*
		 * The XML signature should be regenerated anyway, so clear the TSL DOM
		 * object.
		 */
		this.tslDocument = null;
		ObjectFactory objectFactory = new ObjectFactory();
		if (null == this.trustStatusList) {
			this.trustStatusList = objectFactory.createTrustStatusListType();
		}
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		if (null == tslSchemeInformation) {
			tslSchemeInformation = objectFactory
					.createTSLSchemeInformationType();
			this.trustStatusList.setSchemeInformation(tslSchemeInformation);
		}
		InternationalNamesType i18nSchemeName = tslSchemeInformation
				.getSchemeName();
		if (null == i18nSchemeName) {
			i18nSchemeName = objectFactory.createInternationalNamesType();
			tslSchemeInformation.setSchemeName(i18nSchemeName);
		}
		TrustServiceListUtils.setValue(schemeName, locale, i18nSchemeName);
	}

	public String getSchemeName(Locale locale) {
		if (null == this.trustStatusList) {
			return null;
		}
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		InternationalNamesType i18nSchemeName = tslSchemeInformation
				.getSchemeName();
		String name = TrustServiceListUtils.getValue(i18nSchemeName, locale);
		return name;
	}

	public String getSchemeOperatorName() {
		Locale locale = Locale.getDefault();
		return getSchemeOperatorName(locale);
	}

	public String getSchemeOperatorName(Locale locale) {
		if (null == this.trustStatusList) {
			return null;
		}
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		InternationalNamesType i18nSchemeOperatorName = tslSchemeInformation
				.getSchemeOperatorName();
		String name = TrustServiceListUtils.getValue(i18nSchemeOperatorName,
				locale);
		return name;
	}

	public List<TrustServiceProvider> getTrustServiceProviders() {
		if (null != this.trustServiceProviders) {
			// only load once
			return this.trustServiceProviders;
		}
		this.trustServiceProviders = new LinkedList<TrustServiceProvider>();
		if (null == this.trustStatusList) {
			return this.trustServiceProviders;
		}
		TrustServiceProviderListType trustServiceProviderList = this.trustStatusList
				.getTrustServiceProviderList();
		if (null == trustServiceProviderList) {
			return this.trustServiceProviders;
		}
		List<TSPType> tsps = trustServiceProviderList.getTrustServiceProvider();
		for (TSPType tsp : tsps) {
			TrustServiceProvider trustServiceProvider = new TrustServiceProvider(
					tsp);
			this.trustServiceProviders.add(trustServiceProvider);
		}
		return this.trustServiceProviders;
	}

	public String getType() {
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		String type = tslSchemeInformation.getTSLType();
		return type;
	}

	public BigInteger getSequenceNumber() {
		if (null == this.trustStatusList) {
			return null;
		}
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		BigInteger sequenceNumber = tslSchemeInformation.getTSLSequenceNumber();
		return sequenceNumber;
	}

	public Date getIssueDate() {
		if (null == this.trustStatusList) {
			return null;
		}
		TSLSchemeInformationType tslSchemeInformation = this.trustStatusList
				.getSchemeInformation();
		XMLGregorianCalendar xmlGregorianCalendar = tslSchemeInformation
				.getListIssueDateTime();
		return xmlGregorianCalendar.toGregorianCalendar().getTime();
	}

	public boolean hasSignature() {
		if (null == this.tslDocument) {
			/*
			 * Even if the JAXB TSL still has a signature, it's probably already
			 * invalid.
			 */
			return false;
		}
		Node signatureNode = getSignatureNode();
		if (null == signatureNode) {
			return false;
		}
		return true;
	}

	public X509Certificate verifySignature() {
		if (null == this.tslDocument) {
			LOG.debug("first save the document");
			return null;
		}

		Node signatureNode = getSignatureNode();
		if (null == signatureNode) {
			LOG.debug("no ds:Signature element present");
			return null;
		}

		KeyInfoKeySelector keyInfoKeySelector = new KeyInfoKeySelector();
		DOMValidateContext valContext = new DOMValidateContext(
				keyInfoKeySelector, signatureNode);
		XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory
				.getInstance("DOM");
		XMLSignature signature;
		try {
			signature = xmlSignatureFactory.unmarshalXMLSignature(valContext);
		} catch (MarshalException e) {
			throw new RuntimeException("XML signature parse error: "
					+ e.getMessage(), e);
		}
		boolean coreValidity;
		try {
			coreValidity = signature.validate(valContext);
		} catch (XMLSignatureException e) {
			throw new RuntimeException(
					"XML signature error: " + e.getMessage(), e);
		}

		// TODO: check what has been signed

		if (coreValidity) {
			LOG.debug("signature valid");
			return keyInfoKeySelector.getCertificate();
		}
		LOG.debug("signature invalid");

		return null;
	}

	private Node getSignatureNode() {
		Element nsElement = this.tslDocument.createElement("ns");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds",
				XMLSignature.XMLNS);
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tsl",
				"http://uri.etsi.org/02231/v2#");

		Node signatureNode;
		try {
			signatureNode = XPathAPI.selectSingleNode(this.tslDocument,
					"tsl:TrustServiceStatusList/ds:Signature", nsElement);
		} catch (TransformerException e) {
			throw new RuntimeException("XPath error: " + e.getMessage(), e);
		}
		return signatureNode;
	}

	private void marshall() throws JAXBException, ParserConfigurationException {
		/*
		 * Assign a unique XML Id to the TSL for signing purposes.
		 */
		String tslId = "tsl-" + UUID.randomUUID().toString();
		this.trustStatusList.setId(tslId);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<TrustStatusListType> trustStatusListElement = objectFactory
				.createTrustServiceStatusList(this.trustStatusList);
		marshaller.marshal(trustStatusListElement, document);

		this.tslDocument = document;
	}

	public void sign(PrivateKey privateKey, X509Certificate certificate)
			throws IOException {
		LOG.debug("sign with: " + certificate.getSubjectX500Principal());
		if (null == this.tslDocument) {
			/*
			 * Marshall to DOM.
			 */
			try {
				marshall();
			} catch (Exception e) {
				throw new IOException("marshaller error: " + e.getMessage(), e);
			}
		}

		/*
		 * Remove existing XML signature from DOM.
		 */
		Node signatureNode = getSignatureNode();
		if (null != signatureNode) {
			signatureNode.getParentNode().removeChild(signatureNode);
		}

		String tslId = this.trustStatusList.getId();

		/*
		 * Create new XML signature.
		 */
		try {
			xmlSign(privateKey, certificate, tslId);
		} catch (Exception e) {
			throw new IOException("XML sign error: " + e.getMessage(), e);
		}
	}

	private void xmlSign(PrivateKey privateKey, X509Certificate certificate,
			String tslId) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, MarshalException,
			XMLSignatureException {
		XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance(
				"DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());
		XMLSignContext signContext = new DOMSignContext(privateKey,
				this.tslDocument.getDocumentElement());
		signContext.putNamespacePrefix(XMLSignature.XMLNS, "ds");

		DigestMethod digestMethod = signatureFactory.newDigestMethod(
				DigestMethod.SHA1, null);
		List<Reference> references = new LinkedList<Reference>();
		List<Transform> transforms = new LinkedList<Transform>();
		transforms.add(signatureFactory.newTransform(Transform.ENVELOPED,
				(TransformParameterSpec) null));

		Reference reference = signatureFactory.newReference("#" + tslId,
				digestMethod, transforms, null, null);
		references.add(reference);

		SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(
				SignatureMethod.RSA_SHA1, null);
		CanonicalizationMethod canonicalizationMethod = signatureFactory
				.newCanonicalizationMethod(
						CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
						(C14NMethodParameterSpec) null);
		SignedInfo signedInfo = signatureFactory.newSignedInfo(
				canonicalizationMethod, signatureMethod, references);

		KeyInfoFactory keyInfoFactory = KeyInfoFactory.getInstance();
		X509Data x509Data = keyInfoFactory.newX509Data(Collections
				.singletonList(certificate));
		KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections
				.singletonList(x509Data));

		XMLSignature xmlSignature = signatureFactory.newXMLSignature(
				signedInfo, keyInfo);
		xmlSignature.sign(signContext);
	}

	public void save(File tslFile) throws IOException {
		LOG.debug("save to: " + tslFile.getAbsolutePath());
		if (null == this.tslDocument) {
			try {
				marshall();
			} catch (Exception e) {
				throw new IOException("marshall error: " + e.getMessage(), e);
			}
			/*
			 * Only remove existing XML signature from new (or changed) DOM
			 * documents.
			 */
			Node signatureNode = getSignatureNode();
			if (null != signatureNode) {
				signatureNode.getParentNode().removeChild(signatureNode);
			}
		}
		try {
			toFile(tslFile);
		} catch (Exception e) {
			throw new IOException(
					"DOM transformation error: " + e.getMessage(), e);
		}
	}

	private void toFile(File tslFile)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {
		Source source = new DOMSource(this.tslDocument);
		Result result = new StreamResult(tslFile);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		/*
		 * We have to omit the ?xml declaration if we want to embed the
		 * document.
		 */
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, result);
	}
}
