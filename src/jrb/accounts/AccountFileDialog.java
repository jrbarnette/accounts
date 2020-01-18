/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;
import java.util.Arrays;

class AccountFileDialog extends JDialog implements ActionListener {
    private static final String OPEN = "Open";
    private static final String SAVE = "Save";
    private static final String CANCEL = "Cancel";
    private static final String SET_PASSWORD = "SetPassword";

    private static final int PASSWORD_COLUMNS = 24;

    static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
    static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
    static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

    private JFileChooser fileChooser;

    private JPanel labelColumn = new JPanel(new GridLayout(0, 1));
    private JComponent passwordLabel;
    private JComponent confirmLabel;

    private JPanel fieldColumn = new JPanel(new GridLayout(0, 1));
    private JPasswordField passwordText;
    private JPasswordField confirmText;

    private File selectedFile;
    private char[] selectedPassword;

    private JFileChooser createFileChooser() {
	JFileChooser aChooser = new JFileChooser();
	aChooser.setMultiSelectionEnabled(false);
	aChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	aChooser.addActionListener(this);
	return aChooser;
    }

    private JPasswordField createPasswordField() {
	JPasswordField field = new JPasswordField(PASSWORD_COLUMNS);
	field.setActionCommand(SET_PASSWORD);
	field.addActionListener(this);
	return field;
    }

    private JPanel createLabelPanel(String text, JComponent target) {
	JPanel aPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	JLabel aLabel = new JLabel(text);
	aLabel.setLabelFor(target);
	aPanel.add(aLabel);
	return aPanel;
    }

    AccountFileDialog(JFrame parent) {
	super(parent, "Choose a file and password");

	fileChooser = createFileChooser();
	passwordText = createPasswordField();
	confirmText = createPasswordField();
	passwordLabel = createLabelPanel("File Password",
					 passwordText);
	confirmLabel = createLabelPanel("Confirm File Password",
					confirmText);

	JPanel pwdPanel = new JPanel();
	pwdPanel.add(labelColumn);
	pwdPanel.add(fieldColumn);
	add(pwdPanel, BorderLayout.NORTH);

	add(fileChooser, BorderLayout.CENTER);

	pack();
    }

    char[] getPassword() {
	return selectedPassword;
    }

    File getSelectedFile() {
	return fileChooser.getSelectedFile();
    }

    void setSelectedFile(char[] password, File f) {
	selectedPassword = password;
	fileChooser.setSelectedFile(f);
    }

    private void selectFile() {
	char[] password = passwordText.getPassword();
	if (password == null || password.length == 0) {
	    passwordText.requestFocusInWindow();
	    return;
	}

	if (fileChooser.getDialogType() == JFileChooser.SAVE_DIALOG
		&& !Arrays.equals(password, confirmText.getPassword())) {
	    confirmText.requestFocusInWindow();
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

    private void addPasswordPanel() {
	labelColumn.removeAll();
	fieldColumn.removeAll();
	labelColumn.add(passwordLabel);
	fieldColumn.add(passwordText);
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

    int showOpenDialog() {
	addPasswordPanel();
	return showDialog(JFileChooser.OPEN_DIALOG);
    }

    int showSaveDialog() {
	addPasswordPanel();
	confirmText.setText(new String(passwordText.getPassword()));
	labelColumn.add(confirmLabel);
	fieldColumn.add(confirmText);
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
