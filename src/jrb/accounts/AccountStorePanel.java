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

    private DefaultListModel<String> descriptionsList;

    private AccountStore myAccountStore;

    static private final String SEARCH = "Search";
    static private final String UPDATE = "Update";

    private JComponent createAccountDataPanel() {
	JPanel dataPanel = new JPanel(new GridLayout(0, 3));

	// 1st row - Description
	dataPanel.add(new JLabel("Description"));
	descriptionText = new JTextField();
	dataPanel.add(descriptionText);
	dataPanel.add(new JLabel(" "));

	// 2nd row - URL
	dataPanel.add(new JLabel("URL"));
	urlText = new JTextField();
	dataPanel.add(urlText);
	JButton searchButton = new JButton(SEARCH);
	dataPanel.add(searchButton);

	// 3rd row - User name
	dataPanel.add(new JLabel("User Name"));
	usernameText = new JTextField();
	dataPanel.add(usernameText);
	dataPanel.add(new JLabel(" "));

	// 4th row - Password
	dataPanel.add(new JLabel("Password"));
	passwordText = new JPasswordField();
	dataPanel.add(passwordText);
	dataPanel.add(new JButton("Copy"));

	// 5th row - Buttons
	dataPanel.add(new JButton(UPDATE));
	dataPanel.add(new JButton("Generate ..."));

	return dataPanel;
    }

    /**
     */
    public AccountStorePanel() {
	super();

	myAccountStore = new AccountStore();

	descriptionsList = new DefaultListModel<String>();
	JList<String> accountList = new JList<String>(descriptionsList);
	JScrollPane aScrollPane = new JScrollPane();
	aScrollPane.setViewportView(accountList);
	add(aScrollPane);

	add(createAccountDataPanel());
    }
}
