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

    static private final String PROTOTYPE_TEXT =
	    "12345678901234567890123456789012";
    static JLabel prototypeLabel = new JLabel(PROTOTYPE_TEXT);

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

    // private DefaultListModel<Account> accountListModel;
    JList<Account> accountList;

    private AccountStore myAccountStore;

    private boolean selected;
    private boolean valid;
    private boolean changed;

    private JLabel createFieldLabel(String text) {
	JLabel label = new JLabel(text);
	label.setPreferredSize(prototypeLabel.getPreferredSize());
	label.setMinimumSize(prototypeLabel.getMinimumSize());
	label.setMaximumSize(prototypeLabel.getMaximumSize());
	return label;
    }

    private JComponent createAccountDataPanel() {
	JPanel fieldsPanel = new JPanel();
	JPanel columnPanel;

	columnPanel = new JPanel(new GridLayout(0, 1));
	String[] fieldNames = {
	    "Description", "URL", "User Name", "Password",
	};
	for (String name : fieldNames) {
	    JPanel panel = new JPanel();
	    panel.add(new JLabel(name));
	    columnPanel.add(panel);
	}
	fieldsPanel.add(columnPanel);

	descriptionText = new JTextField();
	descriptionText.setColumns(32);
	urlText = new JTextField();
	urlText.setColumns(32);
	usernameText = new JTextField();
	usernameText.setColumns(32);
	passwordText = new JPasswordField();
	passwordText.setColumns(32);

	columnPanel = new JPanel(new GridLayout(0, 1));
	columnPanel.add(descriptionText);
	columnPanel.add(urlText);
	columnPanel.add(usernameText);
	columnPanel.add(passwordText);
	fieldsPanel.add(columnPanel);

	columnPanel = new JPanel(new GridLayout(0, 1));
	columnPanel.add(new JLabel(" "));
	JButton searchButton = new JButton(SEARCH);
	searchButton.addActionListener(this);
	columnPanel.add(searchButton);
	columnPanel.add(new JLabel(" "));
	columnPanel.add(new JButton("Copy"));
	fieldsPanel.add(columnPanel);

	JPanel buttonPanel = new JPanel();
	JPanel aPanel = new JPanel();

	aPanel.getInsets().set(0, 0, 0, 0);
	aPanel.add(new JButton(UPDATE));
	buttonPanel.add(aPanel);
	aPanel = new JPanel();
	aPanel.getInsets().set(0, 0, 0, 0);
	aPanel.add(new JButton("Generate ..."));
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
	JScrollPane aScrollPane = new JScrollPane();
	aScrollPane.setViewportView(accountList);
	add(aScrollPane);

	add(createAccountDataPanel());
	clearAccountData();
    }

    public void actionPerformed(ActionEvent e) {
	String buttonName = e.getActionCommand();
	if (buttonName.equals(UPDATE)) {
	    updateAccountData();
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	Account selectedAccount = accountList.getSelectedValue();
	descriptionText.setText(selectedAccount.getDescription());
	urlText.setText(selectedAccount.getUrl());
	usernameText.setText(selectedAccount.getUsername());
	passwordText.setText(selectedAccount.getPassword());
    }

    private void clearAccountData() {
	selected = false;
	valid = false;
	changed = false;

	descriptionText.setText("");
	urlText.setText("");
	usernameText.setText("");
	passwordText.setText("");

	accountList.clearSelection();
    }

    private void updateAccountData() {
	myAccountStore.updateAccount(
		descriptionText.getText(),
		urlText.getText(),
		usernameText.getText(),
		new String(passwordText.getPassword()));
    }
}
