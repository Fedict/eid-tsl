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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.KeyStore.Entry;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.FailedLoginException;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.security.pkcs11.SunPKCS11;

public class Pkcs11Token {

	private static final Log LOG = LogFactory.getLog(Pkcs11Token.class);

	private final String pkcs11Library;

	private final SunPKCS11 pkcs11Provider;

	private KeyStore keyStore;

	public Pkcs11Token(String pkcs11Library) throws IOException {
		this.pkcs11Library = pkcs11Library;
		LOG.debug("PKCS#11 library: " + this.pkcs11Library);
		String pkcs11ConfigFile = createPkcs11ProviderConfigFile();
		this.pkcs11Provider = new SunPKCS11(pkcs11ConfigFile);
		if (-1 == Security.addProvider(this.pkcs11Provider)) {
			throw new RuntimeException("could not add security provider");
		}
	}

	public void close() {
		LOG.debug("close");
		Security.removeProvider(this.pkcs11Provider.getName());
	}

	public List<String> getAliases() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableEntryException {
		List<String> aliases = new LinkedList<String>();
		try {
			this.keyStore = KeyStore.getInstance("PKCS11", this.pkcs11Provider);
		} catch (KeyStoreException e) {
			JOptionPane.showMessageDialog(null, "No keystore present: "
					+ e.getMessage(), "PKCS#11 error",
					JOptionPane.ERROR_MESSAGE);
			throw e;
		}
		LoadStoreParameter loadStoreParameter = new Pkcs11LoadStoreParameter();
		try {
			this.keyStore.load(loadStoreParameter);
		} catch (IOException e) {
			LOG.debug("I/O error: " + e.getMessage(), e);
			Throwable cause = e.getCause();
			if (null != cause) {
				if (cause instanceof FailedLoginException) {
					JOptionPane.showMessageDialog(null, "PIN incorrect",
							"Login failed", JOptionPane.ERROR_MESSAGE);
				}
			}
			return aliases;
		}
		Enumeration<String> aliasesEnum = this.keyStore.aliases();
		while (aliasesEnum.hasMoreElements()) {
			String alias = aliasesEnum.nextElement();
			LOG.debug("keystore alias: " + alias);
			if (false == this.keyStore.isKeyEntry(alias)) {
				continue;
			}
			aliases.add(alias);
		}
		return aliases;
	}

	public PrivateKeyEntry getPrivateKeyEntry(String alias)
			throws NoSuchAlgorithmException, UnrecoverableEntryException,
			KeyStoreException {
		if (null == this.keyStore) {
			throw new IllegalStateException("first call getAliases()");
		}
		if (false == this.keyStore.isKeyEntry(alias)) {
			throw new RuntimeException("not a key entry: " + alias);
		}
		Entry keyEntry = this.keyStore.getEntry(alias, null);
		if (false == keyEntry instanceof PrivateKeyEntry) {
			throw new RuntimeException("not a private key entry: " + alias);
		}
		PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) keyEntry;
		return privateKeyEntry;
	}

	private String createPkcs11ProviderConfigFile() throws IOException {
		File tmpConfigFile = File.createTempFile("pkcs11-", "conf");
		tmpConfigFile.deleteOnExit();
		PrintWriter configWriter = new PrintWriter(new FileOutputStream(
				tmpConfigFile), true);
		configWriter.println("name=SmartCard");
		configWriter.println("library=" + this.pkcs11Library);
		configWriter.println("slotListIndex= " + 0);
		// configWriter.println("disabledMechanisms = {");
		// configWriter.println("\tCKM_SHA1_RSA_PKCS");
		// configWriter.println("}");
		return tmpConfigFile.getAbsolutePath();
	}
}
