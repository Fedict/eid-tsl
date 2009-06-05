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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	private static final String LOAD_ACTION_COMMAND = "load";

	private static final String ABOUT_ACTION_COMMAND = "about";

	private final JDesktopPane desktopPane;

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
		menuBar.add(helpMenu);

		addActionMenuItem("About", ABOUT_ACTION_COMMAND, helpMenu);
	}

	private void initFileMenu(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		addActionMenuItem("Load", LOAD_ACTION_COMMAND, fileMenu);
		addActionMenuItem("Exit", EXIT_ACTION_COMMAND, fileMenu);
	}

	private void addActionMenuItem(String text, String actionCommand, JMenu menu) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.setActionCommand(actionCommand);
		menuItem.addActionListener(this);
		menu.add(menuItem);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (EXIT_ACTION_COMMAND.equals(command)) {
			System.exit(0);
		} else if (LOAD_ACTION_COMMAND.equals(command)) {
			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showOpenDialog(this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				displayTsl(fileChooser.getSelectedFile());
			}
		} else if (ABOUT_ACTION_COMMAND.equals(command)) {
			JOptionPane.showMessageDialog(this, "eID TSL Tool\n"
					+ "Copyright (C) 2009 FedICT\n"
					+ "http://code.google.com/p/eid-tsl/", "About",
					JOptionPane.INFORMATION_MESSAGE);
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
				trustServiceList);
		this.desktopPane.add(internalFrame);
	}

	public static void main(String[] args) {
		new TslTool();
	}
}
