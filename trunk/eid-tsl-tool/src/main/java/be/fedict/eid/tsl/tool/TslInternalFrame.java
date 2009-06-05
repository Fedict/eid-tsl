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
import java.awt.FlowLayout;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import be.fedict.eid.tsl.TrustService;
import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceProvider;

class TslInternalFrame extends JInternalFrame implements TreeSelectionListener {

	private static final long serialVersionUID = 1L;

	private final TrustServiceList trustServiceList;

	private JTree tree;

	private JLabel serviceName;

	private JLabel serviceType;

	private JLabel serviceStatus;

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

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Service Providers");
		this.tree = new JTree(rootNode);
		this.tree.addTreeSelectionListener(this);
		for (TrustServiceProvider trustServiceProvider : this.trustServiceList
				.getTrustServiceProviders()) {
			DefaultMutableTreeNode trustServiceProviderNode = new DefaultMutableTreeNode(
					trustServiceProvider.getName());
			rootNode.add(trustServiceProviderNode);
			for (TrustService trustService : trustServiceProvider
					.getTrustServices()) {
				MutableTreeNode trustServiceNode = new DefaultMutableTreeNode(
						trustService);
				trustServiceProviderNode.add(trustServiceNode);
			}
		}
		this.tree.expandRow(0);

		JScrollPane treeScrollPane = new JScrollPane(this.tree);
		JPanel detailsPanel = new JPanel();
		splitPane.setLeftComponent(treeScrollPane);
		splitPane.setRightComponent(detailsPanel);

		initDetailsPanel(detailsPanel);
	}

	private void initDetailsPanel(JPanel detailsPanel) {
		detailsPanel.setBorder(new TitledBorder("Details"));
		detailsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel dataPanel = new JPanel(gridBagLayout);
		detailsPanel.add(dataPanel);

		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 10;
		dataPanel.add(new JLabel("Service Name"), constraints);
		this.serviceName = new JLabel();
		constraints.gridx++;
		dataPanel.add(this.serviceName, constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		dataPanel.add(new JLabel("Service Type"), constraints);

		constraints.gridx++;
		this.serviceType = new JLabel();
		dataPanel.add(this.serviceType, constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		dataPanel.add(new JLabel("Service Status"), constraints);

		constraints.gridx++;
		this.serviceStatus = new JLabel();
		dataPanel.add(this.serviceStatus, constraints);
	}

	private void addGenericTab(JTabbedPane tabbedPane) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel dataPanel = new JPanel(gridBagLayout);
		JPanel genericPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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

		constraints.gridy++;
		constraints.gridx = 0;
		dataPanel.add(new JLabel("Type"), constraints);
		constraints.gridx++;
		dataPanel.add(new JLabel(this.trustServiceList.getType()), constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		dataPanel.add(new JLabel("Sequence number"), constraints);
		constraints.gridx++;
		dataPanel.add(new JLabel(this.trustServiceList.getSequenceNumber()
				.toString()), constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		dataPanel.add(new JLabel("Issue date"), constraints);
		constraints.gridx++;
		dataPanel.add(new JLabel(this.trustServiceList.getIssueDate()
				.toString()), constraints);
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (treeNode.isLeaf()) {
			TrustService trustService = (TrustService) treeNode.getUserObject();
			this.serviceName.setText(trustService.getName());
			this.serviceType.setText(trustService.getType());
			this.serviceStatus.setText(trustService.getStatus());
		} else {
			this.serviceName.setText("");
			this.serviceType.setText("");
			this.serviceStatus.setText("");
		}
	}
}
