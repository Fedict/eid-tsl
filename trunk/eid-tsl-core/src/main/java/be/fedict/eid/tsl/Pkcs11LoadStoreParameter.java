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

import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.ProtectionParameter;

import javax.security.auth.callback.CallbackHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Keystore load/store parameter implementation for the PKCS#11 keystore.
 * 
 * @author fcorneli
 * 
 */
public class Pkcs11LoadStoreParameter implements LoadStoreParameter {

	private static final Log LOG = LogFactory
			.getLog(Pkcs11LoadStoreParameter.class);

	private final CallbackHandlerProtection callbackHandlerProtection;

	public Pkcs11LoadStoreParameter() {
		CallbackHandler callbackHandler = new Pkcs11CallbackHandler();
		this.callbackHandlerProtection = new CallbackHandlerProtection(
				callbackHandler);
	}

	public ProtectionParameter getProtectionParameter() {
		LOG.debug("getting protection parameter");
		return this.callbackHandlerProtection;
	}
}
