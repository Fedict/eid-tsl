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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.WizardValidationException;
import org.openide.WizardDescriptor.ValidatingPanel;
import org.openide.util.HelpCtx;

public class SignSelectPkcs11FinishablePanel implements
		ValidatingPanel<Object>, ActionListener {

	private static final Log LOG = LogFactory
			.getLog(SignSelectPkcs11FinishablePanel.class);

	private JTextField pkcs11TextField;

	private JSpinner slotIdxSpinner;

	private Component component;

	@Override
	public Component getComponent() {
		LOG.debug("get component");
		if (null == this.component) {
			/*
			 * We need to return the same component each time, else the
			 * validate() logic doesn't work as expected.
			 */
			JPanel panel = new JPanel();
			BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
			panel.setLayout(boxLayout);
			JPanel infoPanel = new JPanel();
			infoPanel.add(new JLabel("Please select a PKCS#11 library."));
			panel.add(infoPanel);

			JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(browsePanel);
			browsePanel.add(new JLabel("PKCS#11 library:"));
			this.pkcs11TextField = new JTextField(30);
			browsePanel.add(this.pkcs11TextField);
			JButton browseButton = new JButton("Browse...");
			browseButton.addActionListener(this);
			browsePanel.add(browseButton);

			JPanel slotIdxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(slotIdxPanel);
			slotIdxPanel.add(new JLabel("Slot index:"));
			SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 10, 1);
			this.slotIdxSpinner = new JSpinner(spinnerModel);
			slotIdxPanel.add(this.slotIdxSpinner);

			this.component = panel;
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
	public void readSettings(Object data) {
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
	}

	@Override
	public void storeSettings(Object data) {
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
	}

	@Override
	public void validate() throws WizardValidationException {
		String pkcs11Library = this.pkcs11TextField.getText();
		LOG.debug("PKCS#11 library: " + pkcs11Library);
		File pkcs11File = new File(pkcs11Library);
		if (false == pkcs11File.exists()) {
			throw new WizardValidationException(null,
					"PKCS#11 library not found", null);
		}
		if (true == pkcs11File.isDirectory()) {
			throw new WizardValidationException(null,
					"PKCS#11 library is a directory", null);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select PKCS#11 library");
		int returnValue = fileChooser.showOpenDialog(null);
		if (JFileChooser.APPROVE_OPTION == returnValue) {
			String pkcs11Library = fileChooser.getSelectedFile()
					.getAbsolutePath();
			this.pkcs11TextField.setText(pkcs11Library);
		}
	}

	public String getPkcs11Library() {
		return this.pkcs11TextField.getText();
	}

	public int getSlotIdx() {
		return (Integer) this.slotIdxSpinner.getValue();
	}
}
