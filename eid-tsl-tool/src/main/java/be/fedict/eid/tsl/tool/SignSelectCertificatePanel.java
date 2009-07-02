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

package be.fedict.eid.tsl.tool;

import java.awt.Component;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.WizardValidationException;
import org.openide.WizardDescriptor.ValidatingPanel;
import org.openide.util.HelpCtx;

import be.fedict.eid.tsl.Pkcs11Token;

public class SignSelectCertificatePanel implements ValidatingPanel<Object> {

	private static final Log LOG = LogFactory
			.getLog(SignSelectCertificatePanel.class);

	private final JPanel component;

	private final JList certificateList;

	private final SignSelectPkcs11FinishablePanel pkcs11Panel;

	private Pkcs11Token pkcs11Token;

	public SignSelectCertificatePanel(
			SignSelectPkcs11FinishablePanel pkcs11Panel) {
		this.pkcs11Panel = pkcs11Panel;

		this.component = new JPanel();
		BoxLayout boxLayout = new BoxLayout(this.component, BoxLayout.PAGE_AXIS);
		this.component.setLayout(boxLayout);

		this.component.add(new JLabel(
				"Please select a certificate from the token: "));

		DefaultListModel listModel = new DefaultListModel();
		this.certificateList = new JList(listModel);
		this.component.add(new JScrollPane(this.certificateList));
	}

	@Override
	public void validate() throws WizardValidationException {
		if (null == this.certificateList.getSelectedValue()) {
			throw new WizardValidationException(null,
					"No certificate selected.", null);
		}
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
	}

	@Override
	public Component getComponent() {
		LOG.debug("get component");
		if (null == this.pkcs11Token) {
			String pkcs11Library = this.pkcs11Panel.getPkcs11Library();
			LOG.debug("PKCS#11 library: " + pkcs11Library);
			try {
				this.pkcs11Token = new Pkcs11Token(pkcs11Library);
				List<String> aliases = this.pkcs11Token.getAliases();
				DefaultListModel listModel = (DefaultListModel) this.certificateList
						.getModel();
				for (String alias : aliases) {
					listModel.addElement(alias);
				}
			} catch (Exception e) {
				LOG.debug("error: " + e.getMessage(), e);
			}
		}
		return this.component;
	}

	@Override
	public HelpCtx getHelp() {
		return null;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void readSettings(Object object) {
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
	}

	@Override
	public void storeSettings(Object object) {
	}
}
