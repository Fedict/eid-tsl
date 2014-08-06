/*
 * eID TSL Project.
 * Copyright (C) 2009-2012 FedICT.
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

import static org.junit.Assert.*;

import java.security.cert.X509Certificate;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.w3c.dom.Document;

import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;

public class FingerprintTest {

	private static final Log LOG = LogFactory.getLog(FingerprintTest.class);

	@Test
	public void testECSSLFingerprint() throws Exception {
		// setup
		X509Certificate sslCert = TrustTestUtils
				.loadCertificateFromResource("eu/ec.europa.eu.der");

		// operate
		LOG.debug("EC SSL SHA-1 fingerprint: "
				+ DigestUtils.shaHex(sslCert.getEncoded()));
		LOG.debug("EC SSL SHA-256 fingerprint: "
				+ DigestUtils.sha256Hex(sslCert.getEncoded()));
	}

	@Test
	public void testECFingerprint() throws Exception {
		// setup
		Document euTSLDocument = TrustTestUtils
				.loadDocumentFromResource("eu/tl-mp-2.xml");
		TrustServiceList euTSL = TrustServiceListFactory
				.newInstance(euTSLDocument);
		X509Certificate euCertificate = euTSL.verifySignature();

		// operate
		LOG.debug("EC SHA-1 fingerprint: "
				+ DigestUtils.shaHex(euCertificate.getEncoded()));
		LOG.debug("EC SHA-256 fingerprint: "
				+ DigestUtils.sha256Hex(euCertificate.getEncoded()));
	}

	@Test
	public void testNewECFingerprint() throws Exception {
		// setup
		Document euTSLDocument = TrustTestUtils
				.loadDocumentFromResource("eu/tl-mp-33.xml");
		TrustServiceList euTSL = TrustServiceListFactory
				.newInstance(euTSLDocument);
		X509Certificate euCertificate = euTSL.verifySignature();

		// operate
		LOG.debug("EC SHA-1 fingerprint: "
				+ DigestUtils.shaHex(euCertificate.getEncoded()));
		LOG.debug("EC SHA-256 fingerprint: "
				+ DigestUtils.sha256Hex(euCertificate.getEncoded()));
	}

	@Test
	public void testNewCertipostCAs() throws Exception {
		X509Certificate caQS_VG = TrustTestUtils
				.loadCertificateFromResource("eu/be/certipost/Certipost Public CA for Qualified Signatures - VG root signed.cer");
		assertNotNull(caQS_VG);
		LOG.debug("CA subject: " + caQS_VG.getSubjectX500Principal());
		LOG.debug("CA issuer: " + caQS_VG.getIssuerX500Principal());
		LOG.debug("CA not before: " + caQS_VG.getNotBefore());
		LOG.debug("CA not after: " + caQS_VG.getNotAfter());
		
		X509Certificate caQS_BCT = TrustTestUtils
				.loadCertificateFromResource("eu/be/certipost/Certipost Public CA for Qualified Signatures - BCT root signed.cer");
		assertNotNull(caQS_BCT);
		LOG.debug("CA subject: " + caQS_BCT.getSubjectX500Principal());
		LOG.debug("CA issuer: " + caQS_BCT.getIssuerX500Principal());
		LOG.debug("CA not before: " + caQS_BCT.getNotBefore());
		LOG.debug("CA not after: " + caQS_BCT.getNotAfter());
		
	}
}
