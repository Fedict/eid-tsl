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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.StyleSheet.BoxPainter;

import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.util.HelpCtx;

public class SignInitFinishablePanel implements FinishablePanel<Object> {

	@Override
	public boolean isFinishPanel() {
		return false;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
	}

	@Override
	public Component getComponent() {
		JPanel panel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(boxLayout);
		panel.add(new JLabel("Welcome to the TSL Signing Wizard."));
		panel
				.add(new JLabel(
						"This wizard will help with the creation of the XML signature."));
		return panel;
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
}
