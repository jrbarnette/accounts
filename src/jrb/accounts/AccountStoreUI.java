/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.File;
import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.*;

/**
 */
class AccountStoreUI extends JFrame {
    private static final String NEW = "New";
    private static final String OPEN = "Open";
    private static final String SAVE = "Save";
    private static final String SAVE_AS = "Save As ...";
    private static final String EXIT = "Exit";

    private AccountFileDialog		fileChooser;
    private Action			saveAction;
    private File			saveFile;
    private AccountStorePanel		accountsPanel;


    /**
     * Initialize all the UI elements in the application.
     */
    public AccountStoreUI() {
        super("Account Manager");

	setJMenuBar(new JMenuBar());
	getJMenuBar().add(makeFileMenu());
	saveFile = null;
	accountsPanel = new AccountStorePanel();
	add(accountsPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	pack();
    }

    /**
     * Make the GUI's <i>File</i> menu.
     */
    private JMenu makeFileMenu() {
	JMenu fileMenu = new JMenu("File"); {
	    AbstractAction act;

	    act = new AbstractAction(NEW) {
		public void actionPerformed(ActionEvent ae) {
		    if (!checkSaveAccounts())
			return;

		    accountsPanel.clearAccounts();
		    saveFile = null;
		    saveAction.setEnabled(false);
		}
	    };
	    fileMenu.add(new JMenuItem(act));

	    act = new AbstractAction(OPEN) {
		public void actionPerformed(ActionEvent ae) {
		    openAccountsDialog();
		}
	    };
	    fileMenu.add(act);

	    saveAction = new AbstractAction(SAVE) {
		public void actionPerformed(ActionEvent ae) {
		    if (saveFile != null)
			saveAccounts(saveFile, null);
		}
	    };
	    saveAction.setEnabled(false);
	    fileMenu.add(saveAction);

	    act = new AbstractAction(SAVE_AS) {
		public void actionPerformed(ActionEvent ae) {
		    saveAccountsDialog();
		}
	    };
	    fileMenu.add(act);

	    fileMenu.addSeparator();

	    act = new AbstractAction(EXIT) {
		public void actionPerformed(ActionEvent ae) {
		    if (checkSaveAccounts())
			System.exit(0);
		}
	    };
	    fileMenu.add(act);
	}
	return fileMenu;
    }

    /**
     * Ensure that our cached <code>JFileChooser</code> object
     * is initialized.
     */
    private void checkFileChooser() {
	if (fileChooser != null)
	    return;

	fileChooser = new AccountFileDialog(this);
    }

    /**
     * Allow the user an opportunity to save the current accounts.  Does
     * nothing if nothing has changed since the last save.  The user
     * is allowed to choose saving the accounts, continuing without
     * saving, or canceling the operation.
     *
     * @return <code>true</code> indicates the operation may
     *     continue.  <code>false</code> indicates that the user
     *     chose to cancel the operation.
     */
    private boolean checkSaveAccounts() {
	if (!accountsPanel.needSave())
	    return true;

	Object[] options = { "Save ...", "Don't Save", "Cancel" };
	int choice = JOptionPane.showOptionDialog(
	    this,
	    "Accounts file has not been saved.  Save it now?",
	    "Accounts Not Saved",
	    JOptionPane.DEFAULT_OPTION,
	    JOptionPane.QUESTION_MESSAGE,
	    null, options, options[0]);

	switch (choice) {
	case 0: // Save
	    saveAccountsDialog();
	    return !accountsPanel.needSave();
	case 1: // Don't Save
	    return true;
	default: // Cancel, closed dialog, or otherwise
	    return false;
	}
    }

    private void openAccounts(File newFile, char[] password) {
	try {
	    accountsPanel.openAccounts(newFile, password);
	    saveFile = newFile;
	    saveAction.setEnabled(true);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(
		this,
		"Unable to open '"
		    + newFile.getName() +"': "
		    + e.getMessage(),
		"Unable to Open Accounts",
		JOptionPane.WARNING_MESSAGE);
	}
    }

    private void openAccountsPasswordDialog(String filename) {
	File newFile = new File(filename);
	JDialog dialog = new JDialog(this, "Please enter password");
	dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
	((JComponent) dialog.getContentPane()).setBorder(
	    BorderFactory.createEmptyBorder(4, 4, 4, 4));

	JPanel leftColumn = new JPanel(new GridLayout(0, 1));
	leftColumn.add(new JLabel("File"));
	leftColumn.add(new JLabel("Password"));
	dialog.add(leftColumn, BorderLayout.WEST);

	JPanel rightColumn = new JPanel(new GridLayout(0, 1));
	rightColumn.add(new JLabel(filename));
	JPasswordField passwordField = new JPasswordField();
	passwordField.setColumns(24);
	passwordField.addActionListener(new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
		dialog.dispose();
	    }
	});
	rightColumn.add(passwordField);
	dialog.add(rightColumn, BorderLayout.EAST);

	JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.add(new JButton(new AbstractAction("Quit") {
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	}));
	buttonPanel.add(new JButton(new AbstractAction("Cancel") {
	    public void actionPerformed(ActionEvent e) {
		passwordField.setText("");
		dialog.dispose();
	    }
	}));
	buttonPanel.add(new JButton(new AbstractAction("Open") {
	    public void actionPerformed(ActionEvent e) {
		dialog.dispose();
	    }
	}));
	dialog.add(buttonPanel, BorderLayout.SOUTH);

	dialog.pack();
	dialog.setVisible(true);
	char[] password = passwordField.getPassword();
	if (password != null && password.length > 0) {
	    openAccounts(newFile, password);
	}
    }

    /**
     * Read accounts from a user-selected file for the <i>Open</i>
     * file-menu choice.
     */
    private void openAccountsDialog() {
	if (!checkSaveAccounts())
	    return;

	checkFileChooser();
	int option = fileChooser.showOpenDialog();
	if (option != AccountFileDialog.APPROVE_OPTION)
	    return;

	File newFile = fileChooser.getSelectedFile();
	char[] password = fileChooser.getPassword();

	openAccounts(newFile, password);
    }

    /**
     * Save accounts for the <i>Save</i> and <i>Save As</i> file-menu
     * choices.
     */
    private void saveAccounts(File newFile, char[] password) {
	try {
	    accountsPanel.saveAccounts(newFile, password);
	    saveFile = newFile;
	    saveAction.setEnabled(true);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(
		this,
		"Unable to save '"
		    + newFile.getName() +"': "
		    + e.getMessage(),
		"Unable to Save Accounts",
		JOptionPane.WARNING_MESSAGE);
	}
    }

    /**
     * Save accounts to a user-selected file for the <i>Save As</i>
     * file-menu choice.
     */
    private void saveAccountsDialog() {
	checkFileChooser();
	int option = fileChooser.showSaveDialog();
	if (option != AccountFileDialog.APPROVE_OPTION)
	    return;

	File file = fileChooser.getSelectedFile();
	char[] password = fileChooser.getPassword();
	if (!file.equals(saveFile) && file.exists()) {
	    Object[] options = { "OK", "Cancel" };
	    int choice = JOptionPane.showOptionDialog(
		this,
		"The file '"
		    + file.getName()
		    + "' already exists.  OK to overwrite?",
		"File Exists",
		JOptionPane.DEFAULT_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null, options, options[1]);
	    if (choice != 0)
		return;
	}

	saveAccounts(file, password);
    }

    /**
     * Handle "window closing" events so we can ask the user what to
     * do with unsaved accounts.  Forward all other events to our
     * superclass.
     */
    protected void processWindowEvent(WindowEvent we) {
	if (we.getID() == WindowEvent.WINDOW_CLOSING &&
		!checkSaveAccounts())
	    return;
	super.processWindowEvent(we);
    }

    /**
     * Create and start an instance of the GUI.
     */
    public static void main(String[] argv) {
        JFrame.setDefaultLookAndFeelDecorated(true);
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		AccountStoreUI ui = new AccountStoreUI();
		if (argv.length != 0) {
		    ui.openAccountsPasswordDialog(argv[0]);
		}
		ui.setVisible(true);
	    }
	});
    }
}
