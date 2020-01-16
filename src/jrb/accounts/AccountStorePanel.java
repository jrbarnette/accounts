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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A subclass of JPanel for displaying and changing an account store.
 */
class AccountStorePanel extends JPanel
	implements ActionListener, ListSelectionListener,
		   DocumentListener {
    static private final String COPY = "Copy";
    static private final String CLEAR = "New";
    static private final String REVERT = "Revert";
    static private final String DELETE = "Delete";
    static private final String UPDATE = "Update";
    static private final String CREATE = "Add";
    static private final String GENERATE = "Generate";

    private AccountDataPanel accountDataPanel;
    private PasswordGenPanel passwordPanel;

    private JButton deleteButton = new JButton(DELETE);
    private JButton clearButton = new JButton(CLEAR);
    private JButton updateButton = new JButton(CREATE);
    private JButton copyButton = new JButton(COPY);

    private boolean accountsChanged;

    private JList<Account> accountList;

    private AccountStore myAccountStore;

    private JPanel createButtonPanel(JButton[] buttons) {
	JPanel buttonPanel = new JPanel(
		new FlowLayout(FlowLayout.RIGHT, 0, 0));
	for (JButton aButton : buttons) {
	    aButton.addActionListener(this);
	    buttonPanel.add(aButton);
	}
	return buttonPanel;
    }

    private JComponent createAccountButtons() {
	JPanel combinedPanel = new JPanel(new GridLayout(0, 1));

	JButton[] accountButtons = {
	    deleteButton, clearButton, updateButton
	};
	combinedPanel.add(createButtonPanel(accountButtons));

	JButton generateButton = new JButton(GENERATE);
	JButton[] passwordButtons = { generateButton, copyButton };
	combinedPanel.add(createButtonPanel(passwordButtons));

	return combinedPanel;
    }

    private JComponent createAccountPanel() {
	JPanel aPanel = new JPanel(new BorderLayout());
	aPanel.add(accountDataPanel, BorderLayout.CENTER);
	aPanel.add(createAccountButtons(), BorderLayout.SOUTH);
	return aPanel;
    }

    public AccountStorePanel() {
	super(new BorderLayout());

	accountList = new JList<Account>();
	accountList.addListSelectionListener(this);
	accountList.setPrototypeCellValue(
		AccountDataPanel.PROTOTYPE_ACCOUNT);
	accountList.setVisibleRowCount(35);
	JScrollPane aScrollPane = new JScrollPane(accountList);
	aScrollPane.setVerticalScrollBarPolicy(
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	add(aScrollPane, BorderLayout.WEST);

	accountDataPanel = new AccountDataPanel(this);
	passwordPanel = new PasswordGenPanel();

	JPanel outer = new JPanel(new BorderLayout());
	outer.add(createAccountPanel(), BorderLayout.NORTH);
	outer.add(passwordPanel, BorderLayout.SOUTH);
	add(outer, BorderLayout.CENTER);

	clearAccounts();
	// XXX Usually, clearAccounts() triggers field changes that
	// invoke validateAccountFields().  But the call here doesn't.
	// I'm not sure why; best guess is because the fields are
	// already all empty.  So, we call to validate the fields
	// manually.
	validateAccountFields();
    }

    public void clearAccounts() {
	myAccountStore = new AccountStore();
	accountsChanged = false;
	refillAccountList();
	accountDataPanel.setSelectedAccount(null);
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
	for (Account acct : myAccountStore) {
	    v.add(acct);
	}
	// If there's currently a selection, this call clears it,
	// forcing a call to `valueChanged()`.
	accountList.setListData(v);
    }

    private void updateAccountStore() {
	// error check: creating a duplicate.
	Account account;
	if (!accountList.isSelectionEmpty()) {
	    account = accountList.getSelectedValue();
	    myAccountStore.updateAccount(
		account,
		accountDataPanel.getDescription(),
		accountDataPanel.getUrl(),
		accountDataPanel.getUsername(),
		accountDataPanel.getPassword());
	} else {
	    account = myAccountStore.createAccount(
		accountDataPanel.getDescription(),
		accountDataPanel.getUrl(),
		accountDataPanel.getUsername(),
		accountDataPanel.getPassword());
	}
	accountsChanged = true;
	refillAccountList();
	accountList.setSelectedValue(account, true);
    }

    private void copyPasswordToClipboard() {
	StringSelection selection =
	    new StringSelection(accountDataPanel.getPassword());
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
	    // the selection triggers no events, so we must clear
	    // "accountDataPanel" here.  When we do have a selection,
	    // clearing it will trigger "valueChanged()", which clears
	    // the data panel for us.
	    if (accountList.isSelectionEmpty()) {
		accountDataPanel.setSelectedAccount(null);
	    } else {
		accountList.clearSelection();
	    }
	} else if (actionName.equals(REVERT)) {
	    accountDataPanel.restoreSelectedAccount();
	} else if (actionName.equals(COPY)) {
	    copyPasswordToClipboard();
	} else if (actionName.equals(GENERATE)) {
	    accountDataPanel.setPassword(
		    passwordPanel.generatePassword());
	} else if (actionName.equals(updateButton.getText())) {
	    updateAccountStore();
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting())
	    return;
	accountDataPanel.setSelectedAccount(
		accountList.getSelectedValue());
    }

    /**
     * Validate the input fields, and set button states based on the
     * result.
     *<p>
     * The various buttons depend on three boolean input conditions:
     *<dl>
     *<dt>E - "existing"</dt>
     *<dd> True if we are editing an existing account from the account
     *     list.  If false, it means we're editing an account to be
     *     created.
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
     *<dd> Enabled only if it's an existing account with no changes.
     *<dt>CLEAR/REVERT button</dt>
     *<dd> For an existing account with no changes, the text is CLEAR,
     *     and the button is enabled.  Otherwise, the text is REVERT,
     *     and the button is enabled if there are any changes.
     *<dt>UPDATE/CREATE button</dt>
     *<dd> For an existing account, the text is UPDATE; otherwise, the
     *     text is CREATE.  The button is enabled if there are changes,
     *     and all fields are valid.
     *<dt>Account list</dt>
     *<dd> Changing the selected account is disabled if there are
     *     changes.
     *</dl>
     */
    private void validateAccountFields() {
	int fieldState = accountDataPanel.getFieldState();

	boolean existing
		= ((fieldState & AccountDataPanel.EXISTING) != 0);
	boolean changed
		= ((fieldState & AccountDataPanel.CHANGED) != 0);
	boolean valid
		= ((fieldState & AccountDataPanel.VALID) != 0);

	clearButton.setText(REVERT);
	if (existing) {
	    if (!changed) {
		clearButton.setText(CLEAR);
	    }
	    updateButton.setText(UPDATE);
	} else {
	    updateButton.setText(CREATE);
	}

	deleteButton.setEnabled(existing && !changed);
	clearButton.setEnabled(existing || changed);
	updateButton.setEnabled(changed && valid);
	copyButton.setEnabled(deleteButton.isEnabled());
	accountList.setEnabled(!changed);
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
