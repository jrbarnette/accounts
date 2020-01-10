/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.security.GeneralSecurityException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

/**
 */
class AccountStorePanel extends JPanel
	implements ActionListener, ListSelectionListener,
		    FocusListener, DocumentListener {
    static private final String COPY = "Copy";
    static private final String CLEAR = "New";
    static private final String DELETE = "Delete";
    static private final String UPDATE = "Update";
    static private final String CREATE = "Add";
    static private final String GENERATE = "Generate";

    static private final Account PROTOTYPE_ACCOUNT =
	new Account("123456789 123456789 123456789 12",
		    "http://example.com", "user", "pw");

    private JTextField descriptionText;
    private JTextField urlText;
    private JTextField usernameText;
    private JPasswordField passwordText;

    private PasswordGenPanel passwordPanel;

    private JButton deleteButton;
    private JButton clearButton;
    private JButton updateButton;
    private JButton copyButton;

    boolean accountsChanged;


    JList<Account> accountList;

    private AccountStore myAccountStore;

    private JComponent createFieldPanels() {
	descriptionText = new JTextField();
	urlText = new JTextField();
	usernameText = new JTextField();
	passwordText = new JPasswordField();

	JPanel textBoxColumn = new JPanel(new GridLayout(0, 1));
	JPanel namesColumn = new JPanel(new GridLayout(0, 1));

	JTextField[] fields = {
	    descriptionText, urlText, usernameText, passwordText,
	};
	String[] names = {
	    "Description",   "URL",   "User Name",  "Password",
	};

	int textWidth = PROTOTYPE_ACCOUNT.getDescription().length();

	for (int i = 0; i < fields.length; i++) {
	    JPanel aPanel = new JPanel(
		    new FlowLayout(FlowLayout.RIGHT, 0, 5));
	    JLabel aLabel = new JLabel(names[i]);
	    aLabel.setLabelFor(fields[i]);
	    aPanel.add(aLabel);
	    namesColumn.add(aPanel);

	    fields[i].setColumns(textWidth);
	    fields[i].addFocusListener(this);
	    fields[i].getDocument().addDocumentListener(this);
	    textBoxColumn.add(fields[i]);
	}

	JPanel parentPanel = new JPanel();
	parentPanel.add(namesColumn);
	parentPanel.add(textBoxColumn);
	return parentPanel;
    }

    private JComponent createAccountButtons() {
	JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	String[] buttonNames = { DELETE, CLEAR, CREATE, COPY };
	JButton[] buttons = new JButton[buttonNames.length];
	int i = 0;
	for (String name : buttonNames) {
	    JPanel aPanel = new JPanel();
	    buttons[i] = new JButton(name);
	    buttons[i].addActionListener(this);
	    aPanel.add(buttons[i]);
	    buttonPanel.add(aPanel);
	    i++;
	}
	deleteButton = buttons[0];
	clearButton = buttons[1];
	updateButton = buttons[2];
	copyButton = buttons[3];
	return buttonPanel;
    }

    private JComponent createAccountDataPanel() {
	JPanel aPanel = new JPanel(new BorderLayout());
	aPanel.add(createFieldPanels(), BorderLayout.CENTER);
	aPanel.add(createAccountButtons(), BorderLayout.SOUTH);
	return aPanel;
    }

    /**
     */
    public AccountStorePanel() {
	super(new BorderLayout());

	accountList = new JList<Account>();
	accountList.addListSelectionListener(this);
	accountList.setPrototypeCellValue(PROTOTYPE_ACCOUNT);
	accountList.setVisibleRowCount(35);
	JScrollPane aScrollPane = new JScrollPane();
	aScrollPane.setViewportView(accountList);
	aScrollPane.setVerticalScrollBarPolicy(
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	add(aScrollPane, BorderLayout.WEST);

	JPanel outer = new JPanel(new BorderLayout());
	JPanel inner = new JPanel(new BorderLayout());

	passwordPanel = new PasswordGenPanel();
	inner.add(passwordPanel, BorderLayout.NORTH);
	JPanel tPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	JButton generateButton = new JButton(GENERATE);
	generateButton.addActionListener(this);
	tPanel.add(generateButton);
	inner.add(tPanel, BorderLayout.SOUTH);

	outer.add(createAccountDataPanel(), BorderLayout.NORTH);
	outer.add(inner, BorderLayout.SOUTH);
	add(outer, BorderLayout.CENTER);

	clearAccounts();
    }

    private void clearAccountFields() {
	descriptionText.setText("");
	urlText.setText("");
	usernameText.setText("");
	passwordText.setText("");

	deleteButton.setEnabled(false);
	clearButton.setEnabled(false);
	updateButton.setText(CREATE);
	updateButton.setEnabled(false);
	copyButton.setEnabled(false);
    }

    private void fillAccountFields(Account selectedAccount) {
	descriptionText.setText(selectedAccount.getDescription());
	urlText.setText(selectedAccount.getUrl());
	usernameText.setText(selectedAccount.getUsername());
	passwordText.setText(selectedAccount.getPassword());

	deleteButton.setEnabled(true);
	clearButton.setEnabled(true);
	updateButton.setText(UPDATE);
	updateButton.setEnabled(false);
	copyButton.setEnabled(true);
    }

    public void clearAccounts() {
	myAccountStore = new AccountStore();
	accountsChanged = false;
	clearAccountFields();
	refillAccountList();
    }

    boolean needSave() {
	return accountsChanged;
    }

    void openAccounts(File accountsFile, char[] password)
	    throws IOException, GeneralSecurityException {
	myAccountStore.readAccounts(
	    new FileInputStream(accountsFile), password);
	accountsChanged = false;
	refillAccountList();
    }

    void saveAccounts(File accountsFile, char[] password)
	    throws IOException, GeneralSecurityException {
	if (password != null)
	    myAccountStore.writeAccounts(
		new FileOutputStream(accountsFile), password);
	else
	    myAccountStore.writeAccounts(
		new FileOutputStream(accountsFile));
	accountsChanged = false;
    }

    private void refillAccountList() {
	Vector<Account> v = new Vector<Account>();
	for (Account acct : myAccountStore.allAccounts()) {
	    v.add(acct);
	}
	// If there's currently a selection, this call clears it,
	// forcing a call to `valueChanged()`.
	accountList.setListData(v);
    }

    private void updateAccountData() {
	// error check: creating a duplicate.
	Account account = null;
	if (!accountList.isSelectionEmpty()) {
	    account = accountList.getSelectedValue();
	    myAccountStore.updateAccount(
		account,
		descriptionText.getText(),
		urlText.getText(),
		usernameText.getText(),
		new String(passwordText.getPassword()));
	} else {
	    account = myAccountStore.createAccount(
		descriptionText.getText(),
		urlText.getText(),
		usernameText.getText(),
		new String(passwordText.getPassword()));
	}
	accountsChanged = true;
	refillAccountList();
	accountList.setSelectedValue(account, true);
    }

    /**
     * Validate the input fields, and enable or disable updates based
     * on the result.  The fields are "valid" if they're all non-empty.
     * When the fields are valid, we enable the update button if any
     * field has changed from its original value.
     */
    private void validateFields() {
	String description = descriptionText.getText();
	String url = urlText.getText();
	String username = usernameText.getText();
	String password = new String(passwordText.getPassword());
	boolean valid = !description.isEmpty()
			&& !url.isEmpty()
			&& !username.isEmpty()
			&& !password.isEmpty();
	if (valid && !accountList.isSelectionEmpty()) {
	    Account selectedAccount = accountList.getSelectedValue();
	    boolean changed =
		    !description.equals(selectedAccount.getDescription())
		    || !url.equals(selectedAccount.getUrl())
		    || !username.equals(selectedAccount.getUsername())
		    || !password.equals(selectedAccount.getPassword());
	    updateButton.setEnabled(changed);
	    deleteButton.setEnabled(!changed);
	} else {
	    updateButton.setEnabled(valid);
	    deleteButton.setEnabled(false);
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
	String actionName = e.getActionCommand();
	if (actionName.equals(DELETE)) {
	    if (!accountList.isSelectionEmpty()) {
		Account selectedAccount = accountList.getSelectedValue();
		myAccountStore.deleteAccount(selectedAccount);
		accountsChanged = true;
		refillAccountList();
	    }
	} else if (actionName.equals(CLEAR)) {
	    // When there's no selection, calling "accountList" to clear
	    // the selection does nothing.  When we have a selection,
	    // clearing the selection will trigger "valueChanged()",
	    // which will then call "clearAccountFields()".
	    if (accountList.isSelectionEmpty()) {
		clearAccountFields();
	    } else {
		accountList.clearSelection();
	    }
	} else if (actionName.equals(COPY)) {
	    copyPasswordToClipboard();
	} else if (actionName.equals(GENERATE)) {
	    String password = new String(passwordPanel.generatePassword());
	    passwordText.setText(password);
	} else if (updateButton.isEnabled()
		   && actionName.equals(updateButton.getText())) {
	    updateAccountData();
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting())
	    return;
	Account selectedAccount = accountList.getSelectedValue();
	if (selectedAccount == null) {
	    clearAccountFields();
	} else {
	    fillAccountFields(selectedAccount);
	}
    }

    public void focusGained(FocusEvent e) {
	((JTextField) e.getSource()).selectAll();
    }

    public void focusLost(FocusEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
	validateFields();
    }

    public void removeUpdate(DocumentEvent e) {
	validateFields();
    }

    public void changedUpdate(DocumentEvent e) {
	// I _think_ this is for non-text property changes
	// validateFields();
    }
}
