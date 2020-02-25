/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.*;

class PasswordFileChooserTest extends JFrame {
    AccountFileDialog dialog;
    JTextArea logText;

    private void createDialog() {
	dialog = new AccountFileDialog(this);
    }

    private void logDialogAction(int option, String operation) {
	if (option == JFileChooser.APPROVE_OPTION) {
	    logText.append(operation);
	    logText.append(": ");
	    logText.append(dialog.getSelectedFile().toString());
	    logText.append("\nPassword: ");
	    logText.append(new String(dialog.getPassword()));
	    logText.append("\n");
	} else if (option == JFileChooser.CANCEL_OPTION) {
	    logText.append("Canceled\n");
	} else {
	    logText.append("Error\n");
	}
    }

    PasswordFileChooserTest() {
	super("Test Password FileChooser");
	createDialog();

	JPanel buttons = new JPanel();

	buttons.add(new JButton(new AbstractAction("Open") {
	    public void actionPerformed(ActionEvent evt) {
		logDialogAction(dialog.showOpenDialog(), "Open");
	    }
	}));

	buttons.add(new JButton(new AbstractAction("Save") {
	    public void actionPerformed(ActionEvent evt) {
		logDialogAction(dialog.showSaveDialog(), "Save as");
	    }
	}));

	buttons.add(new JButton(new AbstractAction("New") {
	    public void actionPerformed(ActionEvent evt) {
		createDialog();
		logText.append("Reset dialog\n");
	    }
	}));

	add(buttons, BorderLayout.SOUTH);

	logText = new JTextArea(15, 45);
	logText.setEditable(false);
	JScrollPane scrollPane = new JScrollPane(logText);
	scrollPane.setHorizontalScrollBarPolicy(
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	pack();
    }

    public static void main(String[] argv) {
        JFrame.setDefaultLookAndFeelDecorated(true);

	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		JFrame app = new PasswordFileChooserTest();
		app.setVisible(true);
	    }
	});
    }
}
