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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bouncycastle.asn1.x509.X509Extensions;
import org.etsi.uri._02231.v2_.DigitalIdentityListType;
import org.etsi.uri._02231.v2_.DigitalIdentityType;
import org.etsi.uri._02231.v2_.InternationalNamesType;
import org.etsi.uri._02231.v2_.MultiLangNormStringType;
import org.etsi.uri._02231.v2_.ObjectFactory;
import org.etsi.uri._02231.v2_.TSPServiceInformationType;
import org.etsi.uri._02231.v2_.TSPServiceType;
import org.joda.time.DateTime;

public class TrustService {

	private final TSPServiceType tspService;

	private final ObjectFactory objectFactory;

	private final DatatypeFactory datatypeFactory;

	TrustService(TSPServiceType tspService) {
		this.tspService = tspService;
		this.objectFactory = new ObjectFactory();
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("datatype config error: "
					+ e.getMessage(), e);
		}
	}

	public TrustService(X509Certificate certificate) {
		this.objectFactory = new ObjectFactory();
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("datatype config error: "
					+ e.getMessage(), e);
		}
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
