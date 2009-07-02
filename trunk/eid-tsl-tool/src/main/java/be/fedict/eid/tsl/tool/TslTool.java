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

import be.fedict.eid.tsl.TrustServiceList;
import be.fedict.eid.tsl.TrustServiceListFactory;

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

	private static final String SIGN_ACTION_COMMAND = "sign";

	private static final String ABOUT_ACTION_COMMAND = "about";

	private final JDesktopPane desktopPane;

	private JMenuItem closeMenuItem;

	private JMenuItem signMenuItem;

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

		addActionMenuItem("Open", KeyEvent.VK_O, OPEN_ACTION_COMMAND, fileMenu);
		fileMenu.addSeparator();
		this.signMenuItem = addActionMenuItem("Sign", KeyEvent.VK_S,
				SIGN_ACTION_COMMAND, fileMenu, false);
		this.closeMenuItem = addActionMenuItem("Close", KeyEvent.VK_C,
				CLOSE_ACTION_COMMAND, fileMenu, false);
		fileMenu.addSeparator();
		addActionMenuItem("Exit", KeyEvent.VK_X, EXIT_ACTION_COMMAND, fileMenu);
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
							new SignInitFinishablePanel(), pkcs11Panel,
							new SignSelectCertificatePanel(pkcs11Panel),
							new SignFinishFinishablePanel() });
			wizardDescriptor.setTitle("Sign TSL");
			wizardDescriptor.putProperty("WizardPanel_autoWizardStyle",
					Boolean.TRUE);
			DialogDisplayer dialogDisplayer = DialogDisplayer.getDefault();
			Dialog wizardDialog = dialogDisplayer
					.createDialog(wizardDescriptor);
			wizardDialog.setVisible(true);
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

	void setActiveTslInternalFrame(TslInternalFrame tslInternalFrame) {
		if (null == tslInternalFrame) {
			this.signMenuItem.setEnabled(false);
			this.closeMenuItem.setEnabled(false);
		} else {
			this.signMenuItem.setEnabled(true);
			this.closeMenuItem.setEnabled(true);
		}
		this.activeTslInternalFrame = tslInternalFrame;
	}

	public static void main(String[] args) {
		new TslTool();
	}
}
