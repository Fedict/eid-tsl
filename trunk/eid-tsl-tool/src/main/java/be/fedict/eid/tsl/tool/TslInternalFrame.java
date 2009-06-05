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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceProvider;

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
		JTabbedPane tabbedPane = new JTabbedPane();
		Container contentPane = this.getContentPane();
		contentPane.add(tabbedPane);

		addGenericTab(tabbedPane);
		addServiceProviderTab(tabbedPane);
		addSignatureTab(tabbedPane);
	}

	private void addSignatureTab(JTabbedPane tabbedPane) {
		JPanel panel = new JPanel();
		tabbedPane.add("Signature", panel);
	}

	private void addServiceProviderTab(JTabbedPane tabbedPane) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		tabbedPane.add("Service Providers", splitPane);

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		JTree tree = new JTree(rootNode);
		for (TrustServiceProvider trustServiceProvider : this.trustServiceList
				.getTrustServiceProviders()) {
			MutableTreeNode node = new DefaultMutableTreeNode(
					trustServiceProvider.getName());
			rootNode.add(node);
		}
		tree.expandRow(0);

		JScrollPane treeScrollPane = new JScrollPane(tree);
		JPanel detailsPanel = new JPanel();
		splitPane.setLeftComponent(treeScrollPane);
		splitPane.setRightComponent(detailsPanel);

		detailsPanel.setBorder(new TitledBorder("Details"));
	}

	private void addGenericTab(JTabbedPane tabbedPane) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel dataPanel = new JPanel(gridBagLayout);
		JPanel genericPanel = new JPanel();
		tabbedPane.add("Generic", new JScrollPane(genericPanel));
		genericPanel.add(dataPanel);

		GridBagConstraints constraints = new GridBagConstraints();

		JLabel schemeNameLabel = new JLabel("Scheme Name");
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 10;
		dataPanel.add(schemeNameLabel, constraints);
		JLabel schemeName = new JLabel(this.trustServiceList.getSchemeName());
		constraints.gridx++;
		dataPanel.add(schemeName, constraints);

		JLabel schemeOperatorNameLabel = new JLabel("Scheme Operator Name");
		constraints.gridy++;
		constraints.gridx = 0;
		dataPanel.add(schemeOperatorNameLabel, constraints);
		JLabel schemeOperatorName = new JLabel(this.trustServiceList
				.getSchemeOperatorName());
		constraints.gridx++;
		dataPanel.add(schemeOperatorName, constraints);
	}
}
