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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bouncycastle.asn1.x509.X509Extensions;
import org.etsi.uri._01903.v1_3.IdentifierType;
import org.etsi.uri._01903.v1_3.ObjectIdentifierType;
import org.etsi.uri._02231.v2_.DigitalIdentityListType;
import org.etsi.uri._02231.v2_.DigitalIdentityType;
import org.etsi.uri._02231.v2_.ExtensionType;
import org.etsi.uri._02231.v2_.ExtensionsListType;
import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.MultiLangNormStringType;
import org.etsi.uri._02231.v2_.ObjectFactory;
import org.etsi.uri._02231.v2_.TSPServiceInformationType;
import org.etsi.uri._02231.v2_.TSPServiceType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.CriteriaListType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.KeyUsageBitType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.KeyUsageType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.PoliciesListType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.QualificationElementType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.QualificationsType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.QualifierType;
import org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.QualifiersType;
import org.joda.time.DateTime;

public class TrustService {

	private final TSPServiceType tspService;

	private final ObjectFactory objectFactory;

	private final DatatypeFactory datatypeFactory;

	private final org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.ObjectFactory eccObjectFactory;

	private final org.etsi.uri._01903.v1_3.ObjectFactory xadesObjectFactory;

	private final List<String> oids;

	TrustService(TSPServiceType tspService) {
		this.tspService = tspService;
		this.objectFactory = new ObjectFactory();
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("datatype config error: "
					+ e.getMessage(), e);
		}
		this.oids = new LinkedList<String>();
		this.eccObjectFactory = new org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.ObjectFactory();
		this.xadesObjectFactory = new org.etsi.uri._01903.v1_3.ObjectFactory();
	}

	public TrustService(X509Certificate certificate) {
		this(certificate, new String[] {});
	}

	public TrustService(X509Certificate certificate, String... oids) {
		this.oids = new LinkedList<String>();
		for (String oid : oids) {
			this.oids.add(oid);
		}

		this.objectFactory = new ObjectFactory();
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("datatype config error: "
					+ e.getMessage(), e);
		}
		this.eccObjectFactory = new org.etsi.uri.trstsvc.svcinfoext.esigdir_1999_93_ec_trustedlist.ObjectFactory();
		this.xadesObjectFactory = new org.etsi.uri._01903.v1_3.ObjectFactory();

		this.tspService = this.objectFactory.createTSPServiceType();
		TSPServiceInformationType tspServiceInformation = this.objectFactory
				.createTSPServiceInformationType();
		this.tspService.setServiceInformation(tspServiceInformation);
		tspServiceInformation
				.setServiceTypeIdentifier("http://uri.etsi.org/TrstSvc/Svctype/CA/QC");
		InternationalNamesType i18nServiceName = this.objectFactory
				.createInternationalNamesType();
		List<MultiLangNormStringType> serviceNames = i18nServiceName.getName();
		MultiLangNormStringType serviceName = this.objectFactory
				.createMultiLangNormStringType();
		serviceNames.add(serviceName);
		serviceName.setLang("EN");
		serviceName.setValue(certificate.getSubjectX500Principal().toString());
		tspServiceInformation.setServiceName(i18nServiceName);

		DigitalIdentityListType digitalIdentityList = this.objectFactory
				.createDigitalIdentityListType();
		List<DigitalIdentityType> digitalIdentities = digitalIdentityList
				.getDigitalId();
		DigitalIdentityType digitalIdentity = this.objectFactory
				.createDigitalIdentityType();
		try {
			digitalIdentity.setX509Certificate(certificate.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new RuntimeException(
					"X509 encoding error: " + e.getMessage(), e);
		}
		digitalIdentity.setX509SubjectName(certificate
				.getSubjectX500Principal().getName());
		byte[] skiValue = certificate
				.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
		digitalIdentity.setX509SKI(skiValue);
		digitalIdentities.add(digitalIdentity);
		tspServiceInformation.setServiceDigitalIdentity(digitalIdentityList);

		tspServiceInformation
				.setServiceStatus("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/Svcstatus/undersupervision");

		GregorianCalendar statusStartingCalendar = new DateTime(certificate
				.getNotBefore()).toGregorianCalendar();
		statusStartingCalendar.setTimeZone(TimeZone.getTimeZone("Z"));
		XMLGregorianCalendar statusStartingTime = this.datatypeFactory
				.newXMLGregorianCalendar(statusStartingCalendar);
		tspServiceInformation.setStatusStartingTime(statusStartingTime);

		if (false == this.oids.isEmpty()) {
			ExtensionsListType extensionsList = this.objectFactory
					.createExtensionsListType();
			tspServiceInformation
					.setServiceInformationExtensions(extensionsList);
			List<ExtensionType> extensions = extensionsList.getExtension();
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
			QualifierType qcWithSscdqualifier = this.eccObjectFactory
					.createQualifierType();
			qualifierList.add(qcWithSscdqualifier);
			qcWithSscdqualifier
					.setUri("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/SvcInfoExt/QCWithSSCD");
			QualifierType qcForLegalPersonQualifier = this.eccObjectFactory
					.createQualifierType();
			qualifierList.add(qcForLegalPersonQualifier);
			qcForLegalPersonQualifier
					.setUri("http://uri.etsi.org/TrstSvc/eSigDir-1999-93-EC-TrustedList/SvcInfoExt/QCForLegalPerson");
			qualificationElement.setQualifiers(qualifiers);

			CriteriaListType criteriaList = this.eccObjectFactory
					.createCriteriaListType();
			qualificationElement.setCriteriaList(criteriaList);
			criteriaList.setAssert("all");
			List<KeyUsageType> keyUsageList = criteriaList.getKeyUsage();
			KeyUsageType keyUsage = this.eccObjectFactory.createKeyUsageType();
			keyUsageList.add(keyUsage);
			KeyUsageBitType digitalSignatureKeyUsageBit = this.eccObjectFactory
					.createKeyUsageBitType();
			digitalSignatureKeyUsageBit.setValue(false);
			digitalSignatureKeyUsageBit.setName("digitalSignature");
			keyUsage.getKeyUsageBit().add(digitalSignatureKeyUsageBit);
			KeyUsageBitType nonRepudiationKeyUsageBit = this.eccObjectFactory
					.createKeyUsageBitType();
			nonRepudiationKeyUsageBit.setValue(true);
			nonRepudiationKeyUsageBit.setName("nonRepudiation");
			keyUsage.getKeyUsageBit().add(nonRepudiationKeyUsageBit);

			CriteriaListType oidCriteriaList = this.eccObjectFactory
					.createCriteriaListType();
			criteriaList.setCriteriaList(oidCriteriaList);
			oidCriteriaList.setAssert("atLeastOne");
			List<PoliciesListType> policySet = oidCriteriaList.getPolicySet();
			PoliciesListType policiesList = this.eccObjectFactory
					.createPoliciesListType();
			policySet.add(policiesList);
			for (String oid : this.oids) {
				ObjectIdentifierType objectIdentifier = this.xadesObjectFactory
						.createObjectIdentifierType();
				IdentifierType identifier = this.xadesObjectFactory
						.createIdentifierType();
				identifier.setValue(oid);
				objectIdentifier.setIdentifier(identifier);
				policiesList.getPolicyIdentifier().add(objectIdentifier);
			}
		}
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

	public String getStatus() {
		TSPServiceInformationType tspServiceInformation = this.tspService
				.getServiceInformation();
		String status = tspServiceInformation.getServiceStatus();
		return status;
	}

	@Override
	public String toString() {
		return getName();
	}
}
