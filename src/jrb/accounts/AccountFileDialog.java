/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;

class AccountFileDialog extends JDialog implements ActionListener {
    private static final String OPEN = "Open";
    private static final String SAVE = "Save";
    private static final String CANCEL = "Cancel";
    private static final String SET_PASSWORD = "SetPassword";

    private static final int PASSWORD_COLUMNS = 24;

    public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
    public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
    public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

    private JFileChooser fileChooser;
    private JPasswordField passwordText;

    private File selectedFile;
    private char[] selectedPassword;

    private static final InputVerifier PASSWORD_ENFORCER =
        new InputVerifier() {
            public boolean verify(JComponent c) {
                char[] password = ((JPasswordField) c).getPassword();
                return (password != null && password.length != 0);
            }
        };

    private JFileChooser makeFileChooser() {
	JFileChooser aChooser = new JFileChooser();
	aChooser.setMultiSelectionEnabled(false);
	aChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	aChooser.addActionListener(this);
	return aChooser;
    }

    private JComponent makePasswordPanel() {
	JPanel pwdPanel = new JPanel();
	JLabel pwdLabel = new JLabel("File password");
	pwdPanel.add(pwdLabel);

	passwordText = new JPasswordField(PASSWORD_COLUMNS);
	passwordText.setActionCommand(SET_PASSWORD);
	passwordText.addActionListener(this);
	pwdLabel.setLabelFor(passwordText);
	pwdPanel.add(passwordText);
	return pwdPanel;
    }

    AccountFileDialog(JFrame parent) {
	super(parent, "Choose a file and password");
	fileChooser = makeFileChooser();
	add(fileChooser, BorderLayout.CENTER);
	add(makePasswordPanel(), BorderLayout.NORTH);
	pack();
    }

    public char[] getPassword() {
	return selectedPassword;
    }

    public File getSelectedFile() {
	return fileChooser.getSelectedFile();
    }

    public void setSelectedFile(char[] password, File f) {
	selectedPassword = password;
	fileChooser.setSelectedFile(f);
    }

    private void selectFile() {
	char[] password = passwordText.getPassword();
	if (password == null || password.length == 0) {
	    passwordText.requestFocusInWindow();
	    return;
	}

	File f = fileChooser.getSelectedFile();
	if (f == null || f.isDirectory()) {
	    fileChooser.requestFocusInWindow();
	    return;
	}

	selectedPassword = password;
	setVisible(false);
    }

    private int showDialog(int fileDialogType) {
	fileChooser.rescanCurrentDirectory();
	fileChooser.setDialogType(fileDialogType);
	setModalityType(ModalityType.APPLICATION_MODAL);
	passwordText.requestFocusInWindow();
	setVisible(true);
	if (selectedPassword != null) {
	    return APPROVE_OPTION;
	} else {
	    return CANCEL_OPTION;
	}
    }

    public int showOpenDialog() {
	passwordText.setInputVerifier(null);
	return showDialog(JFileChooser.OPEN_DIALOG);
    }

    public int showSaveDialog() {
	passwordText.setInputVerifier(PASSWORD_ENFORCER);
	return showDialog(JFileChooser.SAVE_DIALOG);
    }

    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	if (command.equals(JFileChooser.APPROVE_SELECTION)
		|| command.equals(SET_PASSWORD)) {
	    selectFile();
	} else if (command.equals(JFileChooser.CANCEL_SELECTION)) {
	    selectedPassword = null;
	    setVisible(false);
	}
    }
}
