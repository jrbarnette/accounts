/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.File;
import java.io.IOException;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.*;

/**
 */
class AccountStoreUI extends JFrame {
    private static final String NEW = "New";
    private static final String OPEN = "Load";
    private static final String SAVE = "Save";
    private static final String SAVE_AS = "Save As ...";
    private static final String EXIT = "Exit";

    private JFileChooser		fileChooser;
    private Action			saveAction;
    private File			saveFile;

    /**
     * Initialize all the UI elements in the application.
     */
    public AccountStoreUI() {
        super("Account Manager");

	//setJMenuBar(new JMenuBar());
	//getJMenuBar().add(makeFileMenu());
	//saveFile = null;
	add(new AccountStorePanel());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	pack();
    }

    /**
     * Create and start an instance of the GUI.
     */
    public static void main(String[] argv) {
        JFrame.setDefaultLookAndFeelDecorated(true);
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		new AccountStoreUI().setVisible(true);
	    }
	});
    }
}
