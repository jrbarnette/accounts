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
    private static final String NEW = "New ...";
    private static final String OPEN = "Open ...";
    private static final String MERGE = "Merge ...";
    private static final String SAVE = "Save ...";
    private static final String EXIT = "Exit";

    private Action saveFileAction;
    private Action mergeFileAction;

    private AccountFileDialog		fileChooser;
    private AccountStorePanel		accountsPanel;

    /**
     * Initialize all the UI elements in the application.
     */
    public AccountStoreUI() {
        super("Account Manager");

	setJMenuBar(new JMenuBar());
	getJMenuBar().add(makeFileMenu());
	accountsPanel = new AccountStorePanel();
	add(accountsPanel);

	fileChooser = new AccountFileDialog(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	pack();
    }

    /**
     * Make the GUI's <i>File</i> menu.
     *
     * @return The application's <i>File</i> menu.
     */
    private JMenu makeFileMenu() {
	JMenu fileMenu = new JMenu("File"); {
	    AbstractAction act;

	    act = new AbstractAction(NEW) {
		public void actionPerformed(ActionEvent ae) {
		    newAccountsDialog();
		}
	    };
	    fileMenu.add(new JMenuItem(act));

	    act = new AbstractAction(OPEN) {
		public void actionPerformed(ActionEvent ae) {
		    openAccountsDialog();
		}
	    };
	    fileMenu.add(act);

	    mergeFileAction = new AbstractAction(MERGE) {
		public void actionPerformed(ActionEvent ae) {
		    mergeAccountsDialog();
		}
	    };
	    mergeFileAction.setEnabled(false);
	    fileMenu.add(mergeFileAction);

	    saveFileAction = new AbstractAction(SAVE) {
		public void actionPerformed(ActionEvent ae) {
		    saveAccountsDialog();
		}
	    };
	    saveFileAction.setEnabled(false);
	    fileMenu.add(saveFileAction);

	    fileMenu.addSeparator();

	    act = new AbstractAction(EXIT) {
		public void actionPerformed(ActionEvent ae) {
		    System.exit(0);
		}
	    };
	    fileMenu.add(act);
	}
	return fileMenu;
    }

    private void openAccounts(File newFile, char[] password) {
	try {
	    accountsPanel.openAccountStore(newFile, password);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(
		this,
		"Unable to open '"
		    + newFile.getName() + "': "
		    + e.getMessage(),
		"Unable to Open Accounts",
		JOptionPane.WARNING_MESSAGE);
	}
	saveFileAction.setEnabled(true);
	mergeFileAction.setEnabled(true);
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
     * file-menu choice.  Give the user a chance to save the current
     * file if there are changes.
     */
    private void openAccountsDialog() {
	int option = fileChooser.showOpenDialog();
	if (option != AccountFileDialog.APPROVE_OPTION)
	    return;

	File newFile = fileChooser.getSelectedFile();
	char[] password = fileChooser.getPassword();

	openAccounts(newFile, password);
    }

    /**
     * Merge accounts from a user selected file for the <i>Merge</i>
     * file-menu choice.
     */
    private void mergeAccountsDialog() {
	int option = fileChooser.showOpenDialog();
	if (option != AccountFileDialog.APPROVE_OPTION)
	    return;

	File newFile = fileChooser.getSelectedFile();
	char[] password = fileChooser.getPassword();

	try {
	    accountsPanel.mergeAccountStore(newFile, password);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(
		this,
		"Unable to merge from '"
		    + newFile.getName() + "': "
		    + e.getMessage(),
		"Unable to Merge Accounts",
		JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Return whether we're allowed to overwrite a given file with our
     * <code>AccountStore</code>. If the file exists, present a modal
     * dialog asking the user for permission to overwrite the file.
     *
     * @param file The file that will be created or overwritten.
     * @return True if the file doesn't exist or the user has approved
     *     overwriting it.
     */
    private boolean canSaveAs(File file) {
	if (file.exists()) {
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
	    return choice == 0;
	}
	return true;
    }

    /**
     * Create a new <code>AccountStore</code> and its backing file
     * in support of the <i>New</i> file-menu choice.  If we're
     * requested to overwrite a file, confirm it with the user.
     */
    private void newAccountsDialog() {
	int option = fileChooser.showSaveDialog();
	if (option != AccountFileDialog.APPROVE_OPTION)
	    return;

	File file = fileChooser.getSelectedFile();
	char[] password = fileChooser.getPassword();
	if (canSaveAs(file)) {
	    try {
		accountsPanel.createAccountStore(file, password);
	    } catch (Exception e) {
		JOptionPane.showMessageDialog(
		    this,
		    "Unable to create '"
			+ file.getName() +"': "
			+ e.getMessage(),
		    "Unable to Create Accounts",
		    JOptionPane.ERROR_MESSAGE);
	    }
	}
	saveFileAction.setEnabled(true);
	mergeFileAction.setEnabled(true);
    }

    /**
     * Save accounts to a user-selected file for the <i>Save As</i>
     * file-menu choice. If we're requested to overwrite a file, confirm
     * it with the user.
     */
    private void saveAccountsDialog() {
	int option = fileChooser.showSaveDialog();
	if (option != AccountFileDialog.APPROVE_OPTION)
	    return;

	File file = fileChooser.getSelectedFile();
	char[] password = fileChooser.getPassword();
	if (canSaveAs(file)) {
	    try {
		accountsPanel.saveAccountStore(file, password);
	    } catch (Exception e) {
		JOptionPane.showMessageDialog(
		    this,
		    "Unable to save '"
			+ file.getName() +"': "
			+ e.getMessage(),
		    "Unable to Save Accounts",
		    JOptionPane.ERROR_MESSAGE);
	    }
	}
    }

    /**
     * Create and start an instance of the GUI.
     *
     * @param argv The command line arguments.
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
