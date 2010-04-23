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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

import be.fedict.eid.tsl.BelgianTrustServiceListFactory;
import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;
import be.fedict.eid.tsl.BelgianTrustServiceListFactory.Trimester;

/**
 * Trusted Service List Tool.
 * 
 * @author fcorneli
 * 
 */
public class TslTool extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(TslTool.class);

	private static final String EXIT_ACTION_COMMAND = "exit";

	private static final String OPEN_ACTION_COMMAND = "open";

	private static final String CLOSE_ACTION_COMMAND = "close";

	private static final String EXPORT_ACTION_COMMAND = "export";

	private static final String SIGN_ACTION_COMMAND = "sign";

	private static final String SAVE_ACTION_COMMAND = "save";

	private static final String SAVE_AS_ACTION_COMMAND = "save-as";

	private static final String ABOUT_ACTION_COMMAND = "about";

	private final JDesktopPane desktopPane;

	private JMenuItem closeMenuItem;

	private JMenuItem exportMenuItem;

	private JMenuItem signMenuItem;

	private JMenuItem saveMenuItem;

	private JMenuItem saveAsMenuItem;

	private TslInternalFrame activeTslInternalFrame;

	private TslTool() {
		super("eID TSL Tool");

		initMenuBar();

		this.desktopPane = new JDesktopPane();
		setContentPane(this.desktopPane);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);
		setVisible(true);
	}

	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		initFileMenu(menuBar);
		menuBar.add(Box.createHorizontalGlue());
		initHelpMenu(menuBar);
		this.setJMenuBar(menuBar);
	}

	private void initHelpMenu(JMenuBar menuBar) {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);

		addActionMenuItem("About", KeyEvent.VK_A, ABOUT_ACTION_COMMAND,
				helpMenu);
	}

	private void initFileMenu(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		initNewMenu(fileMenu);

		addActionMenuItem("Open", KeyEvent.VK_O, OPEN_ACTION_COMMAND, fileMenu);
		fileMenu.addSeparator();
		this.signMenuItem = addActionMenuItem("Sign", KeyEvent.VK_S,
				SIGN_ACTION_COMMAND, fileMenu, false);
		this.saveMenuItem = addActionMenuItem("Save", KeyEvent.VK_A,
				SAVE_ACTION_COMMAND, fileMenu, false);
		this.saveAsMenuItem = addActionMenuItem("Save As", KeyEvent.VK_V,
				SAVE_AS_ACTION_COMMAND, fileMenu, false);
		this.exportMenuItem = addActionMenuItem("Export", KeyEvent.VK_E,
				EXPORT_ACTION_COMMAND, fileMenu, false);
		this.closeMenuItem = addActionMenuItem("Close", KeyEvent.VK_C,
				CLOSE_ACTION_COMMAND, fileMenu, false);
		fileMenu.addSeparator();
		addActionMenuItem("Exit", KeyEvent.VK_X, EXIT_ACTION_COMMAND, fileMenu);
	}

	private void initNewMenu(JMenu fileMenu) {
		JMenu newMenu = new JMenu("New");
		newMenu.setMnemonic(KeyEvent.VK_N);
		fileMenu.add(newMenu);

		JMenu belgiumMenu = new JMenu("Belgium");
		newMenu.add(belgiumMenu);

		JMenu _2010BelgiumMenu = new JMenu("2010");
		belgiumMenu.add(_2010BelgiumMenu);

		JMenuItem _2010_T1_BelgiumMenuItem = addActionMenuItem("Trimester 1",
				KeyEvent.VK_1, "TSL-BE-2010-T1", _2010BelgiumMenu, true);
		_2010BelgiumMenu.add(_2010_T1_BelgiumMenuItem);
		JMenuItem _2010_T2_BelgiumMenuItem = addActionMenuItem("Trimester 2",
				KeyEvent.VK_1, "TSL-BE-2010-T2", _2010BelgiumMenu, true);
		_2010BelgiumMenu.add(_2010_T2_BelgiumMenuItem);
	}

	private JMenuItem addActionMenuItem(String text, int mnemonic,
			String actionCommand, JMenu menu) {
		return addActionMenuItem(text, mnemonic, actionCommand, menu, true);
	}

	private JMenuItem addActionMenuItem(String text, int mnemonic,
			String actionCommand, JMenu menu, boolean enabled) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.setMnemonic(mnemonic);
		menuItem.setActionCommand(actionCommand);
		menuItem.addActionListener(this);
		menuItem.setEnabled(enabled);
		menu.add(menuItem);
		return menuItem;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (EXIT_ACTION_COMMAND.equals(command)) {
			System.exit(0);
		} else if (OPEN_ACTION_COMMAND.equals(command)) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Open TSL");
			int returnValue = fileChooser.showOpenDialog(this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				displayTsl(fileChooser.getSelectedFile());
			}
		} else if (ABOUT_ACTION_COMMAND.equals(command)) {
			JOptionPane.showMessageDialog(this, "eID TSL Tool\n"
					+ "Copyright (C) 2009 FedICT\n"
					+ "http://code.google.com/p/eid-tsl/", "About",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (CLOSE_ACTION_COMMAND.equals(command)) {
			if (this.activeTslInternalFrame.getTrustServiceList().hasChanged()) {
				int result = JOptionPane.showConfirmDialog(this,
						"TSL has been changed.\n" + "Save the TSL?", "Save",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (JOptionPane.CANCEL_OPTION == result) {
					return;
				}
				if (JOptionPane.YES_OPTION == result) {
					try {
						this.activeTslInternalFrame.save();
					} catch (IOException e) {
						LOG.error("IO error: " + e.getMessage(), e);
					}
				}
			}
			try {
				this.activeTslInternalFrame.setClosed(true);
			} catch (PropertyVetoException e) {
				LOG.warn("property veto error: " + e.getMessage(), e);
			}
		} else if (SIGN_ACTION_COMMAND.equals(command)) {
			LOG.debug("sign");
			TrustServiceList trustServiceList = this.activeTslInternalFrame
					.getTrustServiceList();
			if (trustServiceList.hasSignature()) {
				int confirmResult = JOptionPane.showConfirmDialog(this,
						"TSL is already signed.\n" + "Resign the TSL?",
						"Resign", JOptionPane.OK_CANCEL_OPTION);
				if (JOptionPane.CANCEL_OPTION == confirmResult) {
					return;
				}
			}
			SignSelectPkcs11FinishablePanel pkcs11Panel = new SignSelectPkcs11FinishablePanel();
			WizardDescriptor wizardDescriptor = new WizardDescriptor(
					new WizardDescriptor.Panel[] {
							new SignInitFinishablePanel(),
							pkcs11Panel,
							new SignSelectCertificatePanel(pkcs11Panel,
									trustServiceList),
							new SignFinishFinishablePanel() });
			wizardDescriptor.setTitle("Sign TSL");
			wizardDescriptor.putProperty("WizardPanel_autoWizardStyle",
					Boolean.TRUE);
			DialogDisplayer dialogDisplayer = DialogDisplayer.getDefault();
			Dialog wizardDialog = dialogDisplayer
					.createDialog(wizardDescriptor);
			wizardDialog.setVisible(true);
		} else if (SAVE_ACTION_COMMAND.equals(command)) {
			LOG.debug("save");
			try {
				this.activeTslInternalFrame.save();
				this.saveMenuItem.setEnabled(false);
			} catch (IOException e) {
				LOG.debug("IO error: " + e.getMessage(), e);
			}
		} else if (SAVE_AS_ACTION_COMMAND.equals(command)) {
			LOG.debug("save as");
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save As");
			int result = fileChooser.showSaveDialog(this);
			if (JFileChooser.APPROVE_OPTION == result) {
				File tslFile = fileChooser.getSelectedFile();
				if (tslFile.exists()) {
					int confirmResult = JOptionPane.showConfirmDialog(this,
							"File already exists.\n"
									+ tslFile.getAbsolutePath() + "\n"
									+ "Overwrite file?", "Overwrite",
							JOptionPane.OK_CANCEL_OPTION);
					if (JOptionPane.CANCEL_OPTION == confirmResult) {
						return;
					}
				}
				try {
					this.activeTslInternalFrame.saveAs(tslFile);
				} catch (IOException e) {
					LOG.debug("IO error: " + e.getMessage(), e);
				}
				this.saveMenuItem.setEnabled(false);
			}
		} else if (EXPORT_ACTION_COMMAND.equals(command)) {
			LOG.debug("export");
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Export to PDF");
			int result = fileChooser.showSaveDialog(this);
			if (JFileChooser.APPROVE_OPTION == result) {
				File pdfFile = fileChooser.getSelectedFile();
				if (pdfFile.exists()) {
					int confirmResult = JOptionPane.showConfirmDialog(this,
							"File already exists.\n"
									+ pdfFile.getAbsolutePath() + "\n"
									+ "Overwrite file?", "Overwrite",
							JOptionPane.OK_CANCEL_OPTION);
					if (JOptionPane.CANCEL_OPTION == confirmResult) {
						return;
					}
				}
				try {
					this.activeTslInternalFrame.export(pdfFile);
				} catch (IOException e) {
					LOG.debug("IO error: " + e.getMessage(), e);
				}
			}
		} else if ("TSL-BE-2010-T1".equals(command)) {
			TrustServiceList trustServiceList = BelgianTrustServiceListFactory
					.newInstance(2010, Trimester.FIRST);
			displayTsl("*TSL-BE-2010-T1.xml", trustServiceList);
			this.saveMenuItem.setEnabled(false);
		} else if ("TSL-BE-2010-T2".equals(command)) {
			TrustServiceList trustServiceList = BelgianTrustServiceListFactory
					.newInstance(2010, Trimester.SECOND);
			displayTsl("*TSL-BE-2010-T2.xml", trustServiceList);
			this.saveMenuItem.setEnabled(false);
		}
	}

	private void displayTsl(File tslFile) {
		LOG.debug("display TSL: " + tslFile.getAbsolutePath());
		TrustServiceList trustServiceList;
		try {
			trustServiceList = TrustServiceListFactory.newInstance(tslFile);
		} catch (IOException e) {
			LOG.debug("IO exception: " + e.getMessage(), e);
			JOptionPane.showMessageDialog(this, "Error loading TSL file.",
					"Load Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		LOG.debug("SHA1 fingerprint: " + trustServiceList.getSha1Fingerprint());
		displayTsl(tslFile, trustServiceList);
	}

	private void displayTsl(File tslFile, TrustServiceList trustServiceList) {
		JInternalFrame internalFrame = new TslInternalFrame(tslFile,
				trustServiceList, this);
		this.desktopPane.add(internalFrame);

		/*
		 * Bring new internal frame to top and focus on it.
		 */
		this.desktopPane.getDesktopManager().activateFrame(internalFrame);
		try {
			internalFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			LOG.error("veto exception");
		}
	}

	private void displayTsl(String tslName, TrustServiceList trustServiceList) {
		JInternalFrame internalFrame = new TslInternalFrame(tslName,
				trustServiceList, this);
		this.desktopPane.add(internalFrame);

		/*
		 * Bring new internal frame to top and focus on it.
		 */
		this.desktopPane.getDesktopManager().activateFrame(internalFrame);
		try {
			internalFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			LOG.error("veto exception");
		}
	}

	void setActiveTslInternalFrame(TslInternalFrame tslInternalFrame) {
		if (null == tslInternalFrame) {
			this.signMenuItem.setEnabled(false);
			this.closeMenuItem.setEnabled(false);
			this.exportMenuItem.setEnabled(false);
			this.saveMenuItem.setEnabled(false);
			this.saveAsMenuItem.setEnabled(false);
		} else {
			this.signMenuItem.setEnabled(true);
			this.closeMenuItem.setEnabled(true);
			this.exportMenuItem.setEnabled(true);
			boolean changed = tslInternalFrame.getTrustServiceList()
					.hasChanged();
			this.saveMenuItem.setEnabled(changed);
			this.saveAsMenuItem.setEnabled(true);
		}
		this.activeTslInternalFrame = tslInternalFrame;
	}

	public static void main(String[] args) {
		new TslTool();
	}

	public void setChanged(boolean changed) {
		this.saveMenuItem.setEnabled(changed);
	}
}
