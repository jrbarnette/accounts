/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 */
class AccountStorePanel extends JPanel
	implements ActionListener, ListSelectionListener {
    static private final String SEARCH = "Search";
    static private final String CLEAR = "Clear";
    static private final String UPDATE = "Update";
    static private final String CREATE = "Create";
    static private final String GENERATE = "Generate ...";

    static private final Account PROTOTYPE_ACCOUNT =
	new Account("url", "12345678901234567890123456789012",
		    "user", "pw");

    static Account[] stubAccountData = {
	new Account("url1", "desc1", "u1", "p1"),
	new Account("url2", "desc2", "u2", "p2"),
	new Account("url3", "desc3", "u3", "p3"),
	new Account("url4", "desc4", "u4", "p4"),
    };

    private JTextField descriptionText;
    private JTextField urlText;
    private JTextField usernameText;
    private JPasswordField passwordText;

    private Account selectedAccount;
    private boolean valid;
    private boolean changed;

    // private DefaultListModel<Account> accountListModel;
    JList<Account> accountList;

    private AccountStore myAccountStore;

    private void createTextFields() {
	descriptionText = new JTextField();
	descriptionText.setColumns(32);
	urlText = new JTextField();
	urlText.setColumns(32);
	usernameText = new JTextField();
	usernameText.setColumns(32);
	passwordText = new JPasswordField();
	passwordText.setColumns(32);
    }

    private JComponent createFieldPanels() {
	JPanel parentPanel = new JPanel();
	JPanel namesColumn = new JPanel(new GridLayout(0, 1));
	parentPanel.add(namesColumn);
	JPanel textBoxColumn = new JPanel(new GridLayout(0, 1));
	parentPanel.add(textBoxColumn);
	JPanel buttonColumn = new JPanel(new GridLayout(0, 1));
	parentPanel.add(buttonColumn);

	// Description
	namesColumn.add((new JPanel()).add(new JLabel("Description")));
	textBoxColumn.add(descriptionText);
	buttonColumn.add(new JLabel(" "));

	// URL
	namesColumn.add((new JPanel()).add(new JLabel("URL")));
	textBoxColumn.add(urlText);
	JButton searchButton = new JButton(SEARCH);
	searchButton.addActionListener(this);
	buttonColumn.add(searchButton);

	// User Name
	namesColumn.add((new JPanel()).add(new JLabel("User Name")));
	textBoxColumn.add(usernameText);
	buttonColumn.add(new JLabel(" "));

	// Password
	namesColumn.add((new JPanel()).add(new JLabel("Password")));
	textBoxColumn.add(passwordText);
	buttonColumn.add(new JButton("Copy"));

	return parentPanel;
    }

    private JComponent createAccountDataPanel() {
	createTextFields();
	JComponent fieldsPanel = createFieldPanels();

	JPanel buttonPanel = new JPanel();
	JPanel aPanel;

	aPanel = new JPanel();
	aPanel.add(new JButton(UPDATE));
	buttonPanel.add(aPanel);
	aPanel = new JPanel();
	aPanel.add(new JButton(GENERATE));
	buttonPanel.add(aPanel);

	JPanel fullPanel = new JPanel(new BorderLayout());
	fullPanel.add(fieldsPanel, BorderLayout.CENTER);
	fullPanel.add(buttonPanel, BorderLayout.SOUTH);

	return fullPanel;
    }

    /**
     */
    public AccountStorePanel() {
	super();

	myAccountStore = new AccountStore();

	// accountListModel = new DefaultListModel<Account>(stubAccountData);
	accountList = new JList<Account>(stubAccountData);
	accountList.addListSelectionListener(this);
	accountList.setPrototypeCellValue(PROTOTYPE_ACCOUNT);
	JScrollPane aScrollPane = new JScrollPane();
	aScrollPane.setViewportView(accountList);
	add(aScrollPane);

	add(createAccountDataPanel());
	clearAccountData();
    }

    private void clearAccountData() {
	selectedAccount = null;
	descriptionText.setText("");
	urlText.setText("");
	usernameText.setText("");
	passwordText.setText("");
	// update/create -> "Create"

	accountList.clearSelection();

	valid = false;
	changed = false;
    }

    private void updateAccountData() {
	myAccountStore.updateAccount(
		descriptionText.getText(),
		urlText.getText(),
		usernameText.getText(),
		new String(passwordText.getPassword()));
    }

    public void actionPerformed(ActionEvent e) {
	String buttonName = e.getActionCommand();
	if (buttonName.equals(UPDATE)) {
	    updateAccountData();
	} else if (buttonName.equals(CREATE)) {
	} else if (buttonName.equals(CLEAR)) {
	    clearAccountData();
	}
    }

    private void validateFields() {
	valid = !descriptionText.getText().isEmpty()
		&& !urlText.getText().isEmpty()
		&& !usernameText.getText().isEmpty()
		&& passwordText.getPassword().length > 0;
	if (valid) {
	    // enable update/create button
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	selectedAccount = accountList.getSelectedValue();
	descriptionText.setText(selectedAccount.getDescription());
	urlText.setText(selectedAccount.getUrl());
	usernameText.setText(selectedAccount.getUsername());
	passwordText.setText(selectedAccount.getPassword());
	// update/create -> "Update"

	validateFields();
	changed = false;
    }
}
