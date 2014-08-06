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

import java.awt.Component;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Callback handler implementation for PKCS#11 keystore.
 * 
 * @author fcorneli
 * 
 */
public class Pkcs11CallbackHandler implements CallbackHandler {

	private static final Log LOG = LogFactory
			.getLog(Pkcs11CallbackHandler.class);

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		LOG.debug("handle");
		for (Callback callback : callbacks) {
			if (callback instanceof PasswordCallback) {
				PasswordCallback passwordCallback = (PasswordCallback) callback;
				char[] pin = getPin();
				passwordCallback.setPassword(pin);
			}
		}
	}

	private char[] getPin() {
		Box mainPanel = Box.createVerticalBox();

		Box passwordPanel = Box.createHorizontalBox();
		JLabel promptLabel = new JLabel("eID PIN:");
		passwordPanel.add(promptLabel);
		passwordPanel.add(Box.createHorizontalStrut(5));
		JPasswordField passwordField = new JPasswordField(8);
		passwordPanel.add(passwordField);
		mainPanel.add(passwordPanel);

		Component parentComponent = null;
		int result = JOptionPane.showOptionDialog(parentComponent, mainPanel,
				"eID PIN?", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			char[] pin = passwordField.getPassword();
			return pin;
		}
		throw new RuntimeException("operation canceled.");
	}
}
