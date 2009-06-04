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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import be.fedict.eid.tsl.TrustServiceList;

class TslInternalFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;

	private final TrustServiceList trustServiceList;

	TslInternalFrame(File tslFile, TrustServiceList trustServiceList) {
		super(tslFile.getName(), true, true, true);
		this.trustServiceList = trustServiceList;

		init();

		setSize(500, 300);
		setVisible(true);
	}

	private void init() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel panel = new JPanel(gridBagLayout);
		Container contentPane = this.getContentPane();
		contentPane.add(panel);

		GridBagConstraints constraints = new GridBagConstraints();

		JLabel schemeNameLabel = new JLabel("Scheme Name");
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.gridx = 0;
		constraints.gridy = 0;
		panel.add(schemeNameLabel, constraints);
		JLabel schemeName = new JLabel(this.trustServiceList.getSchemeName());
		constraints.gridx++;
		panel.add(schemeName, constraints);

		JLabel schemeOperatorNameLabel = new JLabel("Scheme Operator Name");
		constraints.gridy++;
		constraints.gridx = 0;
		panel.add(schemeOperatorNameLabel, constraints);
		JLabel schemeOperatorName = new JLabel(this.trustServiceList
				.getSchemeOperatorName());
		constraints.gridx++;
		panel.add(schemeOperatorName, constraints);
	}
}
