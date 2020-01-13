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
    static private final String REVERT = "Revert";
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
	// XXX Usually, clearAccounts() triggers field changes that
	// invoke validateAccountFields().  But the call here doesn't.
	// I'm not sure why; best guess is because the fields are
	// already all empty.  So, we call to validate the fields
	// manually.
	validateAccountFields();
    }

    /**
     * Validate the input fields, and set button states based on the
     * result.
     *<p>
     * The various buttons depend on three boolean input conditions:
     *<dl>
     *<dt>S - "selected"</dt>
     *<dd> True if we are editing an account from the account list.  If
     *     false, it means we're editing an account to be created.
     *<dt>C - "changed"</dt>
     *<dd> True if one or more fields has been edited.
     *<dt>V - "valid"</dt>
     *<dd> True if the account fields are valid to be saved to an
     *     account.  Currently, this means all fields are non-empty.
     *</dl>
     *<p>
     * These inputs determine button states as follows:
     *<dl>
     *<dt>DELETE and COPY buttons</dt>
     *<dd> Enabled only if selected, and there are no changes.
     *<dt>CLEAR/REVERT button</dt>
     *<dd> If selected, and there are no changes, the text is CLEAR, and
     *     the button is enabled.  Otherwise, the text is REVERT, and the
     *     button is enabled if there are any changes.
     *<dt>UPDATE/CREATE button</dt>
     *<dd> If selected, the text is UPDATE; otherwise, the text is
     *     CREATE.  The button is enabled if there are changes, and
     *     all fields are valid.
     *</dl>
     */
    private void validateAccountFields() {
	String description = descriptionText.getText();
	String url = urlText.getText();
	String username = usernameText.getText();
	String password = new String(passwordText.getPassword());

	clearButton.setText(REVERT);

	boolean changed;
	if (!accountList.isSelectionEmpty()) {
	    Account selectedAccount = accountList.getSelectedValue();
	    changed = !description.equals(selectedAccount.getDescription())
		    || !url.equals(selectedAccount.getUrl())
		    || !username.equals(selectedAccount.getUsername())
		    || !password.equals(selectedAccount.getPassword());
	    deleteButton.setEnabled(!changed);
	    if (!changed) {
		clearButton.setText(CLEAR);
	    }
	    clearButton.setEnabled(true);
	    updateButton.setText(UPDATE);
	} else {
	    changed = !description.isEmpty()
		    || !url.isEmpty()
		    || !username.isEmpty()
		    || !password.isEmpty();
	    deleteButton.setEnabled(false);
	    clearButton.setEnabled(changed);
	    updateButton.setText(CREATE);
	}
	if (changed) {
	    boolean valid = !description.isEmpty()
			&& !url.isEmpty()
			&& !username.isEmpty()
			&& !password.isEmpty();
	    updateButton.setEnabled(valid);
	} else {
	    updateButton.setEnabled(false);
	}
	copyButton.setEnabled(deleteButton.isEnabled());
	accountList.setEnabled(!changed);
    }

    private void clearAccountFields() {
	// These changes will trigger DocumentEvent notifications
	// that will update the button states.
	descriptionText.setText("");
	urlText.setText("");
	usernameText.setText("");
	passwordText.setText("");
    }

    private void fillAccountFields(Account selectedAccount) {
	// These changes will trigger DocumentEvent notifications
	// that will update the button states.
	descriptionText.setText(selectedAccount.getDescription());
	urlText.setText(selectedAccount.getUrl());
	usernameText.setText(selectedAccount.getUsername());
	passwordText.setText(selectedAccount.getPassword());
    }

    private void refreshAccountFields() {
	Account selectedAccount = accountList.getSelectedValue();
	if (selectedAccount == null) {
	    clearAccountFields();
	} else {
	    fillAccountFields(selectedAccount);
	}
    }

    public void clearAccounts() {
	myAccountStore = new AccountStore();
	accountsChanged = false;
	refillAccountList();
	clearAccountFields();
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
	Account account;
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
	    // Our state machine guarantees that there's an account
	    // selection in this case.
	    Account selectedAccount = accountList.getSelectedValue();
	    myAccountStore.deleteAccount(selectedAccount);
	    accountsChanged = true;
	    refillAccountList();
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
	    descriptionText.requestFocusInWindow();
	} else if (actionName.equals(REVERT)) {
	    refreshAccountFields();
	    descriptionText.requestFocusInWindow();
	} else if (actionName.equals(COPY)) {
	    copyPasswordToClipboard();
	} else if (actionName.equals(GENERATE)) {
	    passwordText.setText(
		    new String(passwordPanel.generatePassword()));
	    passwordText.requestFocusInWindow();
	} else if (updateButton.isEnabled()
		   && actionName.equals(updateButton.getText())) {
	    updateAccountData();
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting())
	    return;
	refreshAccountFields();
	accountList.requestFocusInWindow();
    }

    public void focusGained(FocusEvent e) {
	((JTextField) e.getSource()).selectAll();
    }

    public void focusLost(FocusEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
	validateAccountFields();
    }

    public void removeUpdate(DocumentEvent e) {
	validateAccountFields();
    }

    public void changedUpdate(DocumentEvent e) {
	// I _think_ this is for non-text property changes
	// validateAccountFields();
    }
}
