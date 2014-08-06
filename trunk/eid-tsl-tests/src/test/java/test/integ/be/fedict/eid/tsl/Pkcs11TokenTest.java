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

package test.integ.be.fedict.eid.tsl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import be.fedict.eid.tsl.Pkcs11Token;

public class Pkcs11TokenTest {

	private static final Log LOG = LogFactory.getLog(Pkcs11TokenTest.class);

	@Test
	public void testEIDAliases() throws Exception {
		File eidPkcs11File = new File("/usr/local/lib/libbeidpkcs11.so");
		listAliases(eidPkcs11File, 2);

		File openscPkcs11File = new File("/usr/lib/opensc-pkcs11.so");
		listAliases(openscPkcs11File, 2);
	}

	@Test
	public void testETokenAliases() throws Exception {
		File eidPkcs11File = new File("/usr/lib/libeTPkcs11.so");
		//listAliases(eidPkcs11File, 1);

		Pkcs11Token pkcs11Token = new Pkcs11Token(eidPkcs11File
				.getAbsolutePath());
		try {
			List<String> aliases = pkcs11Token.getAliases();
			String alias = aliases.get(0);
			LOG.debug("alias: " + alias);
			PrivateKeyEntry privateKeyEntry = pkcs11Token.getPrivateKeyEntry(alias);
			
		} finally {
			pkcs11Token.close();
		}
	}

	private void listAliases(File pkcs11File, int aliasCount)
			throws IOException, KeyStoreException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableEntryException {
		LOG.debug("pkcs11 file: " + pkcs11File.getAbsolutePath());
		assertTrue(pkcs11File.exists());

		Pkcs11Token pkcs11Token = new Pkcs11Token(pkcs11File.getAbsolutePath());
		try {
			List<String> aliases = pkcs11Token.getAliases();
			assertNotNull(aliases);
			for (String alias : aliases) {
				LOG.debug("alias: " + alias);
			}
			assertEquals(aliasCount, aliases.size());
		} finally {
			pkcs11Token.close();
		}
	}
}
