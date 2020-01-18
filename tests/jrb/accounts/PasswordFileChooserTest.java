/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.awt.EventQueue;
import javax.swing.*;

class PasswordFileChooserTest implements Runnable {
    JFrame frame;
    String key;

    PasswordFileChooserTest(JFrame aFrame, String aKey) {
	frame = aFrame;
	key = aKey;
    }

    public void run() {
	AccountFileDialog dialog = new AccountFileDialog(frame);

	for (char c : key.toCharArray()) {
	    boolean useOpenDialog;
	    if (c == 'o') {
		useOpenDialog = true;
	    } else if (c == 's') {
		useOpenDialog = false;
	    } else {
		continue;
	    }

	    int option;
	    if (useOpenDialog) {
		option = dialog.showOpenDialog();
	    } else {
		option = dialog.showSaveDialog();
	    }
	    if (option == JFileChooser.APPROVE_OPTION) {
		System.out.println(dialog.getSelectedFile().toString());
		System.out.println(new String(dialog.getPassword()));
	    } else if (option == JFileChooser.CANCEL_OPTION) {
		System.out.println("Canceled");
	    } else {
		System.out.println("Error");
	    }
	}
	System.exit(0);
    }

    public static void main(String[] argv) {
        JFrame.setDefaultLookAndFeelDecorated(true);

	String key;
	if (argv.length > 0) {
	    key = argv[0];
	} else {
	    key = "soos";
	}
	EventQueue.invokeLater(
		new PasswordFileChooserTest(new JFrame("Fubar"), key));

    }
}
