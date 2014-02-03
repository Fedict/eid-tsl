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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.joda.time.DateTime;

import be.fedict.eid.tsl.jaxb.ecc.CriteriaListType;
import be.fedict.eid.tsl.jaxb.ecc.PoliciesListType;
import be.fedict.eid.tsl.jaxb.ecc.QualificationElementType;
import be.fedict.eid.tsl.jaxb.ecc.QualificationsType;
import be.fedict.eid.tsl.jaxb.ecc.QualifierType;
import be.fedict.eid.tsl.jaxb.ecc.QualifiersType;
import be.fedict.eid.tsl.jaxb.tsl.AdditionalServiceInformationType;
import be.fedict.eid.tsl.jaxb.tsl.DigitalIdentityListType;
import be.fedict.eid.tsl.jaxb.tsl.DigitalIdentityType;
import be.fedict.eid.tsl.jaxb.tsl.ExtensionType;
import be.fedict.eid.tsl.jaxb.tsl.ExtensionsListType;
import be.fedict.eid.tsl.jaxb.tsl.InternationalNamesType;
import be.fedict.eid.tsl.jaxb.tsl.MultiLangNormStringType;
import be.fedict.eid.tsl.jaxb.tsl.NonEmptyMultiLangURIType;
import be.fedict.eid.tsl.jaxb.tsl.ObjectFactory;
import be.fedict.eid.tsl.jaxb.tsl.ServiceHistoryInstanceType;
import be.fedict.eid.tsl.jaxb.tsl.ServiceHistoryType;
import be.fedict.eid.tsl.jaxb.tsl.TSPServiceInformationType;
import be.fedict.eid.tsl.jaxb.tsl.TSPServiceType;
import be.fedict.eid.tsl.jaxb.xades.IdentifierType;
import be.fedict.eid.tsl.jaxb.xades.ObjectIdentifierType;

public class TrustService {

	private static final Log LOG = LogFactory.getLog(TrustService.class);

	private final TSPServiceType tspService;

	private final ObjectFactory objectFactory;

	private final DatatypeFactory datatypeFactory;

	private final be.fedict.eid.tsl.jaxb.ecc.ObjectFactory eccObjectFactory;

	private final be.fedict.eid.tsl.jaxb.xades.ObjectFactory xadesObjectFactory;

	public static final String QC_NO_SSCD_QUALIFIER_URI = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/QCNoSSCD";

	public static final String QC_SSCD_STATUS_AS_IN_CERT_QUALIFIER_URI = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/QCSSCDStatusAsInCert";

	public static final String QC_FOR_LEGAL_PERSON_QUALIFIER_URI = "http://uri.etsi.org/TrstSvc/TrustedList//SvcInfoExt/QCForLegalPerson";
	
	public static final String SERVICE_TYPE_IDENTIFIER_CA_QC_URI = "http://uri.etsi.org/TrstSvc/Svctype/CA/QC";
	
	public static final String SERVICE_TYPE_IDENTIFIER_ROOTCA_QC_URI ="http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/RootCA-QC";
	
	public static final String SERVICE_TYPE_IDENTIFIER_NATIONALROOTCA_QC_URI ="http://uri.etsi.org/TrstSvc/Svctype/NationalRootCA-QC";
	
	public static final String SERVICE_STATUS_UNDER_SUPERVISION = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/undersupervision";
	
	public static final String SERVICE_STATUS_UNDER_SUPERVISION_IN_CESSATION = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/supervisionincessation";
	
	public static final String SERVICE_STATUS_CEASED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/supervisionceased";
	
	public static final String SERVICE_STATUS_SUPERVISION_REVOKED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/supervisionrevoked"; 
	
	public static final String SERVICE_STATUS_ACCREDITED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/accredited";
	
	public static final String SERVICE_STATUS_ACCREDITATION_CEASED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/accreditationceased";
	
	public static final String SERVICE_STATUS_ACCREDITATION_REVOKED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/accreditationrevoked";
	
	public static final String SERVICE_STATUS_SET_BY_NATIONAL_LAW = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/setbynationallaw";
	
	public static final String SERVICE_STATUS_DEPRECATED_BY_NATIONAL_LAW = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/deprecatedbynationallaw";
	
	
	
	private String serviceName;

	private DateTime statusStartingDate;

	TrustService(TSPServiceType tspService) {
		this.tspService = tspService;
		this.objectFactory = new ObjectFactory();
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("datatype config error: "
					+ e.getMessage(), e);
		}
		this.eccObjectFactory = new be.fedict.eid.tsl.jaxb.ecc.ObjectFactory();
		this.xadesObjectFactory = new be.fedict.eid.tsl.jaxb.xades.ObjectFactory();
	}

	public TrustService(String serviceName, String serviceTypeIdentifier, String serviceStatus, DateTime statusStartingDate,
			 X509Certificate... certificates) {
		this.serviceName = serviceName;
		this.statusStartingDate = statusStartingDate;
		this.objectFactory = new ObjectFactory();
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("datatype config error: "
					+ e.getMessage(), e);
		}
		this.eccObjectFactory = new be.fedict.eid.tsl.jaxb.ecc.ObjectFactory();
		this.xadesObjectFactory = new be.fedict.eid.tsl.jaxb.xades.ObjectFactory();

		this.tspService = this.objectFactory.createTSPServiceType();
		TSPServiceInformationType tspServiceInformation = this.objectFactory
				.createTSPServiceInformationType();
		this.tspService.setServiceInformation(tspServiceInformation);
		tspServiceInformation
				.setServiceTypeIdentifier(serviceTypeIdentifier);
		InternationalNamesType i18nServiceName = this.objectFactory
				.createInternationalNamesType();
		List<MultiLangNormStringType> serviceNames = i18nServiceName.getName();
		MultiLangNormStringType serviceNameJaxb = this.objectFactory
				.createMultiLangNormStringType();
		serviceNames.add(serviceNameJaxb);
		serviceNameJaxb.setLang(Locale.ENGLISH.getLanguage());
		X509Certificate certificate = certificates[0];
		if (null == this.serviceName) {
			serviceNameJaxb.setValue(certificate.getSubjectX500Principal()
					.toString());
		} else {
			serviceNameJaxb.setValue(this.serviceName);
		}
		tspServiceInformation.setServiceName(i18nServiceName);

		DigitalIdentityListType digitalIdentityList = createDigitalIdentityList(certificates);
		tspServiceInformation.setServiceDigitalIdentity(digitalIdentityList);

		tspServiceInformation
				.setServiceStatus(serviceStatus);

		GregorianCalendar statusStartingCalendar;
		if (null == this.statusStartingDate) {
			statusStartingCalendar = new DateTime(certificate.getNotBefore())
					.toGregorianCalendar();
		} else {
			statusStartingCalendar = this.statusStartingDate
					.toGregorianCalendar();
		}
		statusStartingCalendar.setTimeZone(TimeZone.getTimeZone("Z"));
		XMLGregorianCalendar statusStartingTime = this.datatypeFactory
				.newXMLGregorianCalendar(statusStartingCalendar);
		tspServiceInformation.setStatusStartingTime(statusStartingTime);
		/*
		if (null != serviceHistoryStatus){
			this.tspService.setServiceHistory(serviceHistoryStatus);
		}
		*/
	}
	public void addServiceHistory(String serviceTypeIdentifier, String serviceName, String servicePreviousStatus, DateTime statusPreviousStartingDate,
			 X509Certificate... certificates){
		
		ServiceHistoryType serviceHistoryType;
		ServiceHistoryInstanceType serviceHistoryInstanceType;
		
		if (this.tspService.getServiceHistory() == null){
			serviceHistoryType = this.objectFactory
					.createServiceHistoryType();
			this.tspService.setServiceHistory(serviceHistoryType);
		}else{
			serviceHistoryType = this.tspService.getServiceHistory();
		}
		
		serviceHistoryInstanceType = this.objectFactory
				.createServiceHistoryInstanceType();
		
		InternationalNamesType i18nServiceName = this.objectFactory
				.createInternationalNamesType();
		List<MultiLangNormStringType> serviceNames = i18nServiceName.getName();
		MultiLangNormStringType serviceNameJaxb = this.objectFactory
				.createMultiLangNormStringType();
		serviceNames.add(serviceNameJaxb);
		serviceNameJaxb.setLang(Locale.ENGLISH.getLanguage());
		X509Certificate certificate = certificates[0];
		if (null == serviceName) {
			serviceNameJaxb.setValue(certificate.getSubjectX500Principal()
					.toString());
		} else {
			serviceNameJaxb.setValue(serviceName);
		}
		serviceHistoryInstanceType.setServiceName(i18nServiceName);
		
		DigitalIdentityListType digitalIdentityList = createDigitalIdentityList(certificates);
		serviceHistoryInstanceType.setServiceDigitalIdentity(digitalIdentityList);
		
		serviceHistoryInstanceType.setServiceStatus(servicePreviousStatus);
		
		GregorianCalendar statusStartingCalendar;
		if (null == this.statusStartingDate) {
			statusStartingCalendar = new DateTime(certificate.getNotBefore())
					.toGregorianCalendar();
		} else {
			statusStartingCalendar = this.statusStartingDate
					.toGregorianCalendar();
		}
		statusStartingCalendar.setTimeZone(TimeZone.getTimeZone("Z"));
		XMLGregorianCalendar statusStartingTime = this.datatypeFactory
				.newXMLGregorianCalendar(statusStartingCalendar);
		serviceHistoryInstanceType.setStatusStartingTime(statusStartingTime);
		
		serviceHistoryType.getServiceHistoryInstance().add(serviceHistoryInstanceType);	
		
	}
	/*
	public TrustService(X509Certificate... certificates) {
		this(null, null, null, null,certificates);
	}
	*/
	
	private DigitalIdentityListType createDigitalIdentityList(
			X509Certificate... certificates) {
		DigitalIdentityListType digitalIdentityList = this.objectFactory
				.createDigitalIdentityListType();
		List<DigitalIdentityType> digitalIdentities = digitalIdentityList
				.getDigitalId();

		for (X509Certificate certificate : certificates) {
			DigitalIdentityType digitalIdentity = this.objectFactory
					.createDigitalIdentityType();
			try {
				digitalIdentity.setX509Certificate(certificate.getEncoded());
			} catch (CertificateEncodingException e) {
				throw new RuntimeException("X509 encoding error: "
						+ e.getMessage(), e);
			}
			digitalIdentities.add(digitalIdentity);
		}

		DigitalIdentityType digitalIdentity = this.objectFactory
				.createDigitalIdentityType();
		digitalIdentity.setX509SubjectName(certificates[0]
				.getSubjectX500Principal().getName());
		digitalIdentities.add(digitalIdentity);

		digitalIdentity = this.objectFactory.createDigitalIdentityType();
		byte[] skiValue = certificates[0]
				.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
		SubjectKeyIdentifierStructure subjectKeyIdentifierStructure;
		try {
			subjectKeyIdentifierStructure = new SubjectKeyIdentifierStructure(
					skiValue);
		} catch (IOException e) {
			throw new RuntimeException("X509 SKI decoding error: "
					+ e.getMessage(), e);
		}
		digitalIdentity.setX509SKI(subjectKeyIdentifierStructure
				.getKeyIdentifier());
		digitalIdentities.add(digitalIdentity);

		return digitalIdentityList;
	}

	TSPServiceType getTSPService() {
		return this.tspService;
	}

	public String getName(Locale locale) {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		InternationalNamesType i18nServiceName = tspServiceInformation
				.getServiceName();
		String serviceName = TrustServiceListUtils.getValue(i18nServiceName,
				locale);
		return serviceName;
	}

	public String getName() {
		Locale locale = Locale.getDefault();
		return getName(locale);
	}

	public String getType() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		String serviceTypeIdentifier = tspServiceInformation
				.getServiceTypeIdentifier();
		return serviceTypeIdentifier;
	}

	public List<ExtensionType> getExtensions() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		ExtensionsListType extensionsList = tspServiceInformation
				.getServiceInformationExtensions();
		if (null == extensionsList) {
			return new LinkedList<ExtensionType>();
		}
		List<ExtensionType> extensions = extensionsList.getExtension();
		return extensions;
	}

	public String getStatus() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		String status = tspServiceInformation.getServiceStatus();
		return status;
	}

	public DateTime getStatusStartingTime() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		XMLGregorianCalendar statusStartingTimeXmlCalendar = tspServiceInformation
				.getStatusStartingTime();
		DateTime statusStartingTimeDateTime = new DateTime(
				statusStartingTimeXmlCalendar.toGregorianCalendar());
		return statusStartingTimeDateTime;
	}

	public X509Certificate getServiceDigitalIdentity() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		DigitalIdentityListType digitalIdentityList = tspServiceInformation
				.getServiceDigitalIdentity();
		try {
			final CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			for (final DigitalIdentityType digitalIdentity : digitalIdentityList
					.getDigitalId()) {
				byte[] x509CertificateData = digitalIdentity
						.getX509Certificate();
				if (x509CertificateData != null) {
					try {
						X509Certificate certificate = (X509Certificate) certificateFactory
								.generateCertificate(new ByteArrayInputStream(
										x509CertificateData));
						return certificate;
					} catch (CertificateException e) {
						throw new RuntimeException("X509 error: "
								+ e.getMessage(), e);
					}
				}
			}
			throw new RuntimeException("No X509Certificate identity specified");
		} catch (CertificateException e) {
			throw new RuntimeException("X509 error: " + e.getMessage(), e);
		}
	}

	public byte[] getServiceDigitalIdentityData() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		DigitalIdentityListType digitalIdentityList = tspServiceInformation
				.getServiceDigitalIdentity();
		try {
			final CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			for (final DigitalIdentityType digitalIdentity : digitalIdentityList
					.getDigitalId()) {
				byte[] x509CertificateData = digitalIdentity
						.getX509Certificate();
				if (x509CertificateData != null) {
					try {
						X509Certificate certificate = (X509Certificate) certificateFactory
								.generateCertificate(new ByteArrayInputStream(
										x509CertificateData));
						return x509CertificateData;
					} catch (CertificateException e) {
						throw new RuntimeException("X509 error: "
								+ e.getMessage(), e);
					}
				}
			}
			throw new RuntimeException("No X509Certificate identity specified");
		} catch (CertificateException e) {
			throw new RuntimeException("X509 error: " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	private static final QName qualifiersName = new QName(
			"http://uri.etsi.org/TrstSvc/SvcInfoExt/eSigDir-1999-93-EC-TrustedList/#",
			"Qualifications");

	public void addOIDForQCSSCDStatusAsInCert(String oid) {
		addOIDForQCSSCDStatusAsInCert(oid, null);
	}

	public void addOIDForQCSSCDStatusAsInCert(String oid, String description) {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		ExtensionsListType extensionsList = tspServiceInformation
				.getServiceInformationExtensions();
		if (null == extensionsList) {
			extensionsList = this.objectFactory.createExtensionsListType();
			tspServiceInformation
					.setServiceInformationExtensions(extensionsList);
		}
		List<ExtensionType> extensions = extensionsList.getExtension();
		for (ExtensionType extension : extensions) {
			if (false == extension.isCritical()) {
				continue;
			}
			List<Object> extensionContent = extension.getContent();
			for (Object extensionObject : extensionContent) {
				JAXBElement<?> extensionElement = (JAXBElement<?>) extensionObject;
				QName extensionName = extensionElement.getName();
				LOG.debug("extension name: " + extensionName);
				if (qualifiersName.equals(extensionName)) {
					LOG.debug("extension found");
					QualificationsType qualifications = (QualificationsType) extensionElement
							.getValue();
					List<QualificationElementType> qualificationElements = qualifications
							.getQualificationElement();
					for (QualificationElementType qualificationElement : qualificationElements) {
						QualifiersType qualifiers = qualificationElement
								.getQualifiers();
						List<QualifierType> qualifierList = qualifiers
								.getQualifier();
						boolean qcSscdStatusAsInCertQualifier = false;
						for (QualifierType qualifier : qualifierList) {
							if (QC_SSCD_STATUS_AS_IN_CERT_QUALIFIER_URI
									.equals(qualifier.getUri())) {
								qcSscdStatusAsInCertQualifier = true;
							}
						}
						if (qcSscdStatusAsInCertQualifier) {
							CriteriaListType criteriaList = qualificationElement
									.getCriteriaList();
							List<PoliciesListType> policySet = criteriaList
									.getPolicySet();

							PoliciesListType policiesList = this.eccObjectFactory
									.createPoliciesListType();
							policySet.add(policiesList);

							ObjectIdentifierType objectIdentifier = this.xadesObjectFactory
									.createObjectIdentifierType();
							IdentifierType identifier = this.xadesObjectFactory
									.createIdentifierType();
							identifier.setValue(oid);
							objectIdentifier.setIdentifier(identifier);
							if (null != description) {
								objectIdentifier.setDescription(description);
							}
							policiesList.getPolicyIdentifier().add(
									objectIdentifier);
							return;
						}
					}
				}
			}
		}
		ExtensionType extension = this.objectFactory.createExtensionType();
		extension.setCritical(true);
		extensions.add(extension);

		QualificationsType qualifications = this.eccObjectFactory
				.createQualificationsType();
		extension.getContent().add(
				this.eccObjectFactory.createQualifications(qualifications));
		List<QualificationElementType> qualificationElements = qualifications
				.getQualificationElement();

		QualificationElementType qualificationElement = this.eccObjectFactory
				.createQualificationElementType();
		qualificationElements.add(qualificationElement);

		QualifiersType qualifiers = this.eccObjectFactory
				.createQualifiersType();
		List<QualifierType> qualifierList = qualifiers.getQualifier();
		QualifierType qcSscdStatusInCertqualifier = this.eccObjectFactory
				.createQualifierType();
		qualifierList.add(qcSscdStatusInCertqualifier);
		qcSscdStatusInCertqualifier
				.setUri(QC_SSCD_STATUS_AS_IN_CERT_QUALIFIER_URI);
		qualificationElement.setQualifiers(qualifiers);

		CriteriaListType criteriaList = this.eccObjectFactory
				.createCriteriaListType();
		qualificationElement.setCriteriaList(criteriaList);
		criteriaList.setAssert("atLeastOne");

		List<PoliciesListType> policySet = criteriaList.getPolicySet();
		PoliciesListType policiesList = this.eccObjectFactory
				.createPoliciesListType();
		policySet.add(policiesList);
		ObjectIdentifierType objectIdentifier = this.xadesObjectFactory
				.createObjectIdentifierType();
		IdentifierType identifier = this.xadesObjectFactory
				.createIdentifierType();
		identifier.setValue(oid);
		objectIdentifier.setDescription(description);
		objectIdentifier.setIdentifier(identifier);
		policiesList.getPolicyIdentifier().add(objectIdentifier);

		AdditionalServiceInformationType additionalServiceInformation = this.objectFactory
				.createAdditionalServiceInformationType();
		NonEmptyMultiLangURIType additionalServiceInformationUri = this.objectFactory
				.createNonEmptyMultiLangURIType();
		additionalServiceInformationUri.setLang("en");
		additionalServiceInformationUri
				.setValue("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/SvcInfoExt/RootCA-QC");
		additionalServiceInformation.setURI(additionalServiceInformationUri);
		extension
				.getContent()
				.add(this.objectFactory
						.createAdditionalServiceInformation(additionalServiceInformation));

	}

	public void addOIDForQCForLegalPerson(String oid) {
		addOIDForQCForLegalPerson(oid, false);
	}

	public void addOIDForQCForLegalPerson(String oid, boolean noRoot) {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		ExtensionsListType extensionsList = tspServiceInformation
				.getServiceInformationExtensions();
		if (null == extensionsList) {
			extensionsList = this.objectFactory.createExtensionsListType();
			tspServiceInformation
					.setServiceInformationExtensions(extensionsList);
		}
		List<ExtensionType> extensions = extensionsList.getExtension();
		for (ExtensionType extension : extensions) {
			if (false == extension.isCritical()) {
				continue;
			}
			List<Object> extensionContent = extension.getContent();
			for (Object extensionObject : extensionContent) {
				JAXBElement<?> extensionElement = (JAXBElement<?>) extensionObject;
				QName extensionName = extensionElement.getName();
				LOG.debug("extension name: " + extensionName);
				if (qualifiersName.equals(extensionName)) {
					LOG.debug("extension found");
					QualificationsType qualifications = (QualificationsType) extensionElement
							.getValue();
					List<QualificationElementType> qualificationElements = qualifications
							.getQualificationElement();
					for (QualificationElementType qualificationElement : qualificationElements) {
						QualifiersType qualifiers = qualificationElement
								.getQualifiers();
						List<QualifierType> qualifierList = qualifiers
								.getQualifier();
						for (QualifierType qualifier : qualifierList) {
							if (QC_FOR_LEGAL_PERSON_QUALIFIER_URI
									.equals(qualifier.getUri())) {
								CriteriaListType criteriaList = qualificationElement
										.getCriteriaList();
								List<PoliciesListType> policySet = criteriaList
										.getPolicySet();

								PoliciesListType policiesList = this.eccObjectFactory
										.createPoliciesListType();
								policySet.add(policiesList);

								ObjectIdentifierType objectIdentifier = this.xadesObjectFactory
										.createObjectIdentifierType();
								IdentifierType identifier = this.xadesObjectFactory
										.createIdentifierType();
								identifier.setValue(oid);
								objectIdentifier.setIdentifier(identifier);
								policiesList.getPolicyIdentifier().add(
										objectIdentifier);
								return;
							}
						}
					}
				}
			}
		}
		ExtensionType extension = this.objectFactory.createExtensionType();
		extension.setCritical(true);
		extensions.add(extension);

		QualificationsType qualifications = this.eccObjectFactory
				.createQualificationsType();
		extension.getContent().add(
				this.eccObjectFactory.createQualifications(qualifications));
		List<QualificationElementType> qualificationElements = qualifications
				.getQualificationElement();

		QualificationElementType qualificationElement = this.eccObjectFactory
				.createQualificationElementType();
		qualificationElements.add(qualificationElement);

		QualifiersType qualifiers = this.eccObjectFactory
				.createQualifiersType();
		List<QualifierType> qualifierList = qualifiers.getQualifier();
		QualifierType qcForLegalPersonqualifier = this.eccObjectFactory
				.createQualifierType();
		qualifierList.add(qcForLegalPersonqualifier);
		qcForLegalPersonqualifier.setUri(QC_FOR_LEGAL_PERSON_QUALIFIER_URI);
		qualificationElement.setQualifiers(qualifiers);

		CriteriaListType criteriaList = this.eccObjectFactory
				.createCriteriaListType();
		qualificationElement.setCriteriaList(criteriaList);
		criteriaList.setAssert("atLeastOne");

		List<PoliciesListType> policySet = criteriaList.getPolicySet();
		PoliciesListType policiesList = this.eccObjectFactory
				.createPoliciesListType();
		policySet.add(policiesList);
		ObjectIdentifierType objectIdentifier = this.xadesObjectFactory
				.createObjectIdentifierType();
		IdentifierType identifier = this.xadesObjectFactory
				.createIdentifierType();
		identifier.setValue(oid);
		objectIdentifier.setIdentifier(identifier);
		policiesList.getPolicyIdentifier().add(objectIdentifier);

		if (noRoot == false) {
			AdditionalServiceInformationType additionalServiceInformation = this.objectFactory
					.createAdditionalServiceInformationType();
			NonEmptyMultiLangURIType additionalServiceInformationUri = this.objectFactory
					.createNonEmptyMultiLangURIType();
			additionalServiceInformationUri.setLang("en");
			additionalServiceInformationUri
					.setValue("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/SvcInfoExt/RootCA-QC");
			additionalServiceInformation
					.setURI(additionalServiceInformationUri);
			extension
					.getContent()
					.add(this.objectFactory
							.createAdditionalServiceInformation(additionalServiceInformation));
		}
	}
}
