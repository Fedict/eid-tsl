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
import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fedict.eid.tsl.ChangeListener;
import be.fedict.eid.tsl.TrustService;
import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceProvider;

class TslInternalFrame extends JInternalFrame implements TreeSelectionListener,
		InternalFrameListener, ChangeListener {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(TslInternalFrame.class);

	private final TrustServiceList trustServiceList;

	private JTree tree;

	private JLabel serviceName;

	private JLabel serviceType;

	private JLabel serviceStatus;

	private final TslTool tslTool;

	private JLabel signer;

	private File tslFile;

	TslInternalFrame(File tslFile, TrustServiceList trustServiceList,
			TslTool tslTool) {
		super(tslFile.getName(), true, true, true);
		this.tslFile = tslFile;
		this.trustServiceList = trustServiceList;
		this.tslTool = tslTool;

		initUI();

		/*
		 * Keep us up-to-date on the changes on the TSL document.
		 */
		this.trustServiceList.addChangeListener(this);

		addInternalFrameListener(this);
		setSize(500, 300);
		setVisible(true);
	}

	private void initUI() {
		JTabbedPane tabbedPane = new JTabbedPane();
		Container contentPane = this.getContentPane();
		contentPane.add(tabbedPane);

		addGenericTab(tabbedPane);
		addServiceProviderTab(tabbedPane);
		addSignatureTab(tabbedPane);
	}

	private void addSignatureTab(JTabbedPane tabbedPane) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel dataPanel = new JPanel(gridBagLayout);
		JPanel signaturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tabbedPane.add("Signature", new JScrollPane(signaturePanel));
		signaturePanel.add(dataPanel);

		GridBagConstraints constraints = new GridBagConstraints();

		JLabel signerLabel = new JLabel("Signer");
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 10;
		dataPanel.add(signerLabel, constraints);
		this.signer = new JLabel();
		constraints.gridx++;
		dataPanel.add(this.signer, constraints);

		updateView();
	}

	private void updateView() {
		X509Certificate signerCertificate = this.trustServiceList
				.verifySignature();
		if (null != signerCertificate) {
			this.signer.setText(signerCertificate.getSubjectX500Principal()
					.toString());
		} else {
			this.signer.setText("[TSL is not signed]");
		}
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

	public TrustServiceList getTrustServiceList() {
		return this.trustServiceList;
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

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		LOG.debug("activated: " + e.getInternalFrame().getTitle());
		this.tslTool.setActiveTslInternalFrame(this);
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		LOG.debug("closed");
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		LOG.debug("closing");
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		LOG.debug("deactivated: " + e.getInternalFrame().getTitle());
		this.tslTool.setActiveTslInternalFrame(null);
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		LOG.debug("deiconified");
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		LOG.debug("iconified");
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		LOG.debug("opened");
	}

	@Override
	public void changed() {
		LOG.debug("TSL changed");
		setTitle("*" + this.tslFile.getAbsolutePath());
		this.tslTool.setChanged(true);
		updateView();
	}

	public File getFile() {
		return this.tslFile;
	}

	public void save() throws IOException {
		this.trustServiceList.save(this.tslFile);
		setTitle(this.tslFile.getAbsolutePath());
	}
}
