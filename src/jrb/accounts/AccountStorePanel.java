/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.awt.GridLayout;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 */
class AccountStorePanel extends JPanel {
    private JTextField descriptionText;
    private JTextField urlText;
    private JTextField usernameText;
    private JPasswordField passwordText;

    /**
     */
    public AccountStorePanel() {
	super();

	JScrollPane aScrollPane = new JScrollPane();
	JList<String> accountList = new JList<>();
	accountList.setModel(new javax.swing.AbstractListModel<String>() {
	    String[] strings = {
		"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"
	    };
	    public int getSize() { return strings.length; }
	    public String getElementAt(int i) { return strings[i]; }
	});
	aScrollPane.setViewportView(accountList);
	add(aScrollPane);

	JPanel accountPanel = new JPanel(new GridLayout(0, 3)); {
	    JPanel subPanel;
	    // 1st row - Description
	    accountPanel.add(new JLabel("Description"));
	    descriptionText = new JTextField();
	    accountPanel.add(descriptionText);
	    accountPanel.add(new JButton("Search"));

	    // 2nd row - URL
	    accountPanel.add(new JLabel("URL"));
	    urlText = new JTextField();
	    accountPanel.add(urlText);
	    accountPanel.add(new JButton("Search"));

	    // 3rd row - User name
	    accountPanel.add(new JLabel("User Name"));
	    usernameText = new JTextField();
	    accountPanel.add(usernameText);
	    accountPanel.add(new JLabel(" "));

	    // 4th row - Password
	    accountPanel.add(new JLabel("Password"));
	    passwordText = new JPasswordField();
	    accountPanel.add(passwordText);
	    accountPanel.add(new JButton("Copy"));

	    // 5th row - Buttons
	    accountPanel.add(new JButton("Update"));
	    accountPanel.add(new JButton("Generate ..."));
	}
	add(accountPanel);
    }
}
