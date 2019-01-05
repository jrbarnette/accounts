/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Vector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 */
class AccountStorePanel extends JPanel
	implements ActionListener, ListSelectionListener, FocusListener {
    static private final String SEARCH = "Search";
    static private final String COPY = "Copy";
    static private final String CLEAR = "Clear";
    static private final String UPDATE = "Update";
    static private final String CREATE = "Add";
    static private final String GENERATE = "Generate ...";

    static private final Account PROTOTYPE_ACCOUNT =
	new Account("12345678901234567890123456789012",
		    "http://example.com", "user", "pw");

    private JTextField descriptionText;
    private JTextField urlText;
    private JTextField usernameText;
    private JPasswordField passwordText;

    private JButton clearButton;
    private JButton updateButton;

    private Account selectedAccount;
    boolean accountsChanged;


    // private DefaultListModel<Account> accountListModel;
    JList<Account> accountList;

    private AccountStore myAccountStore;

    private static void addInPanel(JComponent p, JComponent c) {
	JPanel aPanel = new JPanel();
	aPanel.add(c);
	p.add(aPanel);
    }

    private void createTextFields() {
	int nColumns = PROTOTYPE_ACCOUNT.getDescription().length();
	descriptionText = new JTextField();
	descriptionText.setColumns(nColumns);
	descriptionText.addActionListener(this);
	descriptionText.addFocusListener(this);

	urlText = new JTextField();
	urlText.setColumns(nColumns);
	urlText.addActionListener(this);
	urlText.addFocusListener(this);

	usernameText = new JTextField();
	usernameText.setColumns(nColumns);
	usernameText.addActionListener(this);
	usernameText.addFocusListener(this);

	passwordText = new JPasswordField();
	passwordText.setColumns(nColumns);
	passwordText.addActionListener(this);
	passwordText.addFocusListener(this);
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
	addInPanel(namesColumn, new JLabel("Description"));
	textBoxColumn.add(descriptionText);
	buttonColumn.add(new JLabel(" "));

	// URL
	addInPanel(namesColumn, new JLabel("URL"));
	textBoxColumn.add(urlText);
	JButton searchButton = new JButton(SEARCH);
	searchButton.addActionListener(this);
	buttonColumn.add(searchButton);

	// User Name
	addInPanel(namesColumn, new JLabel("User Name"));
	textBoxColumn.add(usernameText);
	buttonColumn.add(new JLabel(" "));

	// Password
	addInPanel(namesColumn, new JLabel("Password"));
	textBoxColumn.add(passwordText);
	JButton copyButton = new JButton(COPY);
	copyButton.addActionListener(this);
	buttonColumn.add(copyButton);

	return parentPanel;
    }

    private JComponent createAccountDataPanel() {
	createTextFields();
	JComponent fieldsPanel = createFieldPanels();

	JPanel buttonPanel = new JPanel();
	String[] buttonNames = { CLEAR, CREATE, GENERATE };
	for (String name : buttonNames) {
	    JButton button = new JButton(name);
	    button.addActionListener(this);
	    buttonPanel.add(button);
	}
	clearButton = (JButton) buttonPanel.getComponent(0);
	updateButton = (JButton) buttonPanel.getComponent(1);
	JPanel fullPanel = new JPanel(new BorderLayout());
	fullPanel.add(fieldsPanel, BorderLayout.CENTER);
	fullPanel.add(buttonPanel, BorderLayout.SOUTH);

	return fullPanel;
    }

    /**
     */
    public AccountStorePanel() {
	super();

	accountList = new JList<Account>();
	accountList.addListSelectionListener(this);
	accountList.setPrototypeCellValue(PROTOTYPE_ACCOUNT);
	JScrollPane aScrollPane = new JScrollPane();
	aScrollPane.setViewportView(accountList);
	add(aScrollPane);

	add(createAccountDataPanel());
	clearAccounts();
    }

    private void clearAccountData() {
	descriptionText.setText("");
	urlText.setText("");
	usernameText.setText("");
	passwordText.setText("");

	clearButton.setEnabled(false);
	updateButton.setText(CREATE);
	updateButton.setEnabled(false);
    }

    public void clearAccounts() {
	myAccountStore = new AccountStore();
	accountsChanged = false;
	clearAccountData();
    }

    boolean needSave() {
        return accountsChanged;
    }

    void openAccounts(File accountsFile) throws IOException {
	FileInputStream fis = new FileInputStream(accountsFile);
	DataInputStream in = new DataInputStream(fis);
	myAccountStore.readAccounts(in);
    }

    void saveAccounts(File accountsFile) throws IOException {
	FileOutputStream fis = new FileOutputStream(accountsFile);
	DataOutputStream out = new DataOutputStream(fis);
	myAccountStore.writeAccounts(out);
    }

    private void fillAccountData() {
	descriptionText.setText(selectedAccount.getDescription());
	urlText.setText(selectedAccount.getUrl());
	usernameText.setText(selectedAccount.getUsername());
	passwordText.setText(selectedAccount.getPassword());

	clearButton.setEnabled(true);
	updateButton.setText(UPDATE);
	updateButton.setEnabled(false);
    }

    private void refillAccountList() {
	Vector<Account> v = new Vector<Account>();
	for (Account acct : myAccountStore.allAccounts()) {
	    v.add(acct);
	}
	// This call implicitly clears the selection, forcing a call to
	// `valueChanged()`.
	accountList.setListData(v);
    }

    private void updateAccount() {
	// error check: creating a duplicate.
	myAccountStore.updateAccount(
	    selectedAccount,
	    descriptionText.getText(),
	    urlText.getText(),
	    usernameText.getText(),
	    new String(passwordText.getPassword()));
	refillAccountList();
    }

    private void createAccount() {
	// error check: creating a duplicate.
	myAccountStore.createAccount(
	    descriptionText.getText(),
	    urlText.getText(),
	    usernameText.getText(),
	    new String(passwordText.getPassword()));
	refillAccountList();
    }

    private void validateFields() {
	String description = descriptionText.getText();
	String url = urlText.getText();
	String username = usernameText.getText();
	String password = new String(passwordText.getPassword());
	boolean valid = !description.isEmpty()
			&& !url.isEmpty()
			&& !username.isEmpty()
			&& !password.isEmpty();
	if (valid && selectedAccount != null) {
	    updateButton.setEnabled(
		    !description.equals(selectedAccount.getDescription())
		    || !url.equals(selectedAccount.getUrl())
		    || !username.equals(selectedAccount.getUsername())
		    || !password.equals(selectedAccount.getPassword()));
	} else {
	    updateButton.setEnabled(valid);
	}
    }

    private void copyPasswordToClipboard() {
	StringSelection selection =
	    new StringSelection(new String(passwordText.getPassword()));
	Clipboard clipboard =
	    Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(selection, selection);
    }

    public void actionPerformed(ActionEvent e) {
	String buttonName = e.getActionCommand();
	validateFields();
	if (buttonName.equals(CLEAR)) {
	    // If the selection goes from "something" to "null", it will
	    // trigger "valueChanged()", which will then call
	    // "clearAccountData()".
	    if (accountList.isSelectionEmpty()) {
		clearAccountData();
	    } else {
		accountList.clearSelection();
	    }
	} else if (buttonName.equals(COPY)) {
	    copyPasswordToClipboard();
	} else if (updateButton.isEnabled()) {
	    if (buttonName.equals(UPDATE)) {
		updateAccount();
	    } else if (buttonName.equals(CREATE)) {
		createAccount();
	    }
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting())
	    return;
	selectedAccount = accountList.getSelectedValue();
	if (selectedAccount != null) {
	    fillAccountData();
	} else {
	    clearAccountData();
	}
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
	validateFields();
    }
}
