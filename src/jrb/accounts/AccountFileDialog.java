/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;

class AccountFileDialog extends JDialog
	implements ActionListener, PropertyChangeListener {
    private static final String OPEN = "Open";
    private static final String SAVE = "Save";
    private static final String CANCEL = "Cancel";
    private static final String PASSWORD = "SetPassword";

    private static final int PASSWORD_COLUMNS = 24;

    public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
    public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
    public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

    private JFileChooser fileChooser;
    private JButton openButton;
    private JButton cancelButton;
    private JPasswordField passwordText;

    private File selectedFile;
    private char[] selectedPassword;

    private JFileChooser makeFileChooser() {
	JFileChooser aChooser = new JFileChooser();
	aChooser.setControlButtonsAreShown(false);
	aChooser.setMultiSelectionEnabled(false);
	aChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	aChooser.addActionListener(this);
	aChooser.addPropertyChangeListener(
		JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, this);
	return aChooser;
    }

    private JComponent makePasswordPanel() {
	JPanel pwdPanel = new JPanel();
	JLabel pwdLabel = new JLabel("File password");
	pwdPanel.add(pwdLabel);

	passwordText = new JPasswordField(PASSWORD_COLUMNS);
	passwordText.setActionCommand(PASSWORD);
	passwordText.addActionListener(this);
	pwdLabel.setLabelFor(passwordText);
	pwdPanel.add(passwordText);

	cancelButton = new JButton(CANCEL);
	cancelButton.addActionListener(this);
	pwdPanel.add(cancelButton);

	openButton = new JButton(OPEN);
	openButton.addActionListener(this);
	openButton.setEnabled(false);
	pwdPanel.add(openButton);
	return pwdPanel;
    }

    AccountFileDialog(JFrame parent) {
	super(parent, "Choose a file and password");
	fileChooser = makeFileChooser();
	add(fileChooser, BorderLayout.CENTER);
	add(makePasswordPanel(), BorderLayout.SOUTH);
	pack();
    }

    public char[] getPassword() {
	return selectedPassword;
    }

    public File getSelectedFile() {
	return selectedFile;
    }

    private void openFile() {
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
	selectedFile = f;
	setVisible(false);
    }

    private int showDialog(String approvalName) {
	openButton.setText(approvalName);
	setModalityType(ModalityType.APPLICATION_MODAL);
	setVisible(true);
	if (selectedPassword != null && selectedFile != null) {
	    return APPROVE_OPTION;
	} else if (selectedPassword == null && selectedFile == null) {
	    return CANCEL_OPTION;
	} else {
	    return ERROR_OPTION;
	}
    }

    public int showOpenDialog() {
	return showDialog(OPEN);
    }

    public int showSaveDialog() {
	return showDialog(SAVE);
    }

    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	if (command == JFileChooser.APPROVE_SELECTION
		|| command == openButton.getText()
		|| command == PASSWORD) {
	    openFile();
	} else if (command == CANCEL) {
	    setVisible(false);
	}
    }

    public void propertyChange(PropertyChangeEvent e) {
	File f = (File) e.getNewValue();
	openButton.setEnabled(f != null && !f.isDirectory());
    }
}
