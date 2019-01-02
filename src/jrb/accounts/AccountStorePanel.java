/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

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

    private JButton clearButton;
    private JButton updateButton;

    private Account selectedAccount;

    // private DefaultListModel<Account> accountListModel;
    JList<Account> accountList;

    private AccountStore myAccountStore;

    private static void addInPanel(JComponent p, JComponent c) {
	JPanel aPanel = new JPanel();
	aPanel.add(c);
	p.add(aPanel);
    }

    private void createTextFields() {
	descriptionText = new JTextField();
	descriptionText.setColumns(32);
	descriptionText.addActionListener(this);
	descriptionText.addFocusListener(this);

	urlText = new JTextField();
	urlText.setColumns(32);
	urlText.addActionListener(this);
	urlText.addFocusListener(this);

	usernameText = new JTextField();
	usernameText.setColumns(32);
	usernameText.addActionListener(this);
	usernameText.addFocusListener(this);

	passwordText = new JPasswordField();
	passwordText.setColumns(32);
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
	descriptionText.setText("");
	urlText.setText("");
	usernameText.setText("");
	passwordText.setText("");

	clearButton.setEnabled(false);
	updateButton.setText(CREATE);
	updateButton.setEnabled(false);
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

    private void updateAccount() {
	// error check: creating a duplicate.
	/*
	myAccountStore.updateAccount(
	    selectedAccount,
	    descriptionText.getText(),
	    urlText.getText(),
	    usernameText.getText(),
	    new String(passwordText.getPassword()));
	*/
	// update accountList UI widget
	//   get List of accounts, sorted by Description
	//   setListData(<List of accounts, sorted by Description>)
	accountList.setListData(stubAccountData);
    }

    private void createAccount() {
	// error check: creating a duplicate.
	/*
	myAccountStore.createAccount(
	    descriptionText.getText(),
	    urlText.getText(),
	    usernameText.getText(),
	    new String(passwordText.getPassword()));
	*/
	// update accountList UI widget
	//   get List of accounts, sorted by Description
	//   setListData(<List of accounts, sorted by Description>)
	accountList.setListData(stubAccountData);
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
	boolean changed = valid;
	if (selectedAccount != null) {
	    changed = description != selectedAccount.getDescription()
			|| url != selectedAccount.getUrl()
			|| username != selectedAccount.getUsername()
			|| password != selectedAccount.getPassword();
	}
	updateButton.setEnabled(valid && changed);
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
	    StringSelection selection =
                new StringSelection(new String(passwordText.getPassword()));
	    Clipboard clipboard =
		Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
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
