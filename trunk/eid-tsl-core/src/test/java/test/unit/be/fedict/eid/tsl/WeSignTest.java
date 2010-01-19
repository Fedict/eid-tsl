/*
 * eID TSL Project.
 * Copyright (C) 2009-2010 FedICT.
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

import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.w3c.dom.Document;

import be.fedict.eid.tsl.TrustService;
import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;
import be.fedict.eid.tsl.TrustServiceProvider;

public class WeSignTest {

	private static final Log LOG = LogFactory.getLog(WeSignTest.class);

	@Test
	public void testLoadWeSignTSL() throws Exception {
		// setup
		Document tslDocument = TrustTestUtils
				.loadDocumentFromResource("WESIGN_TSL_ID001.xml");

		// operate
		TrustServiceList trustServiceList = TrustServiceListFactory
				.newInstance(tslDocument);

		// verify
		assertNotNull(trustServiceList);
		LOG.debug("scheme name: " + trustServiceList.getSchemeName());
		assertEquals("WP3 - TSL TEST SCHEME", trustServiceList.getSchemeName());

		List<TrustServiceProvider> trustServiceProviders = trustServiceList
				.getTrustServiceProviders();
		for (TrustServiceProvider trustServiceProvider : trustServiceProviders) {
			LOG.debug("\tTSP name: " + trustServiceProvider.getName());
			if (false == "Certipost NV - E-Trust, Citizen CA, Foreigner CA"
					.equals(trustServiceProvider.getName())) {
				continue;
			}
			List<TrustService> trustServices = trustServiceProvider
					.getTrustServices();
			for (TrustService trustService : trustServices) {
				LOG.debug("\t\tTS name: " + trustService.getName());
				X509Certificate caCertificate = trustService
						.getServiceDigitalIdentity();
				LOG.debug("\t\tCA Subject: "
						+ caCertificate.getSubjectX500Principal());
				LOG.debug("\t\tCA Issuer: "
						+ caCertificate.getIssuerX500Principal());
			}
		}
	}

	@Test
	public void testCertipostCAs() throws Exception {
		X509Certificate certificate = TrustTestUtils
				.loadCertificateFromResource("eu/be/etrust/QCA_Self_Signed.crt");
		LOG.debug("eTrust QCSP: " + certificate);
	}
}
