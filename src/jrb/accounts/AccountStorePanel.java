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
    /**
     * Text for the button used to move earlier in account update
     * history.
     */
    static private final String EARLIER = "<";

    /**
     * Text for the button used to move later in account update
     * history.
     */
    static private final String LATER = ">";

    /**
     * Text for the button used to delete the currently selected
     * account.
     */
    static private final String DELETE = "Delete";

    /**
     * Text for the button used to clear fields to begin entering data
     * for a new account.
     */
    static private final String CLEAR = "New";

    /**
     * Text for the button used to revert changes made to the current
     * account data.
     */
    static private final String REVERT = "Revert";

    /**
     * Text for the button used to create a new account from account
     * data and add it to the account store.
     */
    static private final String CREATE = "Add";

    /**
     * Text for the button used to apply edited account data as an
     * update to the currently selected account.
     */
    static private final String UPDATE = "Update";

    /**
     * Text for the button used to generate a new password.
     */
    static private final String GENERATE = "Generate";

    /**
     * Text for the button used to copy password text into the system
     * clipboard.
     */
    static private final String COPY = "Copy";

    private AccountDataPanel accountDataPanel;
    private PasswordGenPanel passwordPanel;

    private JButton earlierButton = new JButton(EARLIER);
    private JButton laterButton = new JButton(LATER);
    private JButton deleteButton = new JButton(DELETE);
    private JButton clearButton = new JButton(CLEAR);
    private JButton updateButton = new JButton(CREATE);
    private JButton generateButton = new JButton(GENERATE);
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
	earlierButton.setEnabled(false);
	earlierButton.addActionListener(this);
	laterButton.setEnabled(false);
	laterButton.addActionListener(this);

	JPanel combinedPanel = new JPanel(new GridLayout(0, 2));
	JPanel tPanel;
	tPanel = new JPanel(new GridLayout(1, 0));
	tPanel.add(new JPanel());
	tPanel.add(new JPanel());
	tPanel.add(earlierButton);
	combinedPanel.add(tPanel);
	tPanel = new JPanel(new GridLayout(1, 0));
	tPanel.add(laterButton);
	tPanel.add(new JPanel());
	tPanel.add(new JPanel());
	combinedPanel.add(tPanel);

	combinedPanel.add(new JPanel());
	JButton[] accountButtons = {
	    deleteButton, clearButton, updateButton
	};
	combinedPanel.add(createButtonPanel(accountButtons));

	combinedPanel.add(new JPanel());
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

    /**
     * Validate the input fields, and set button states based on the
     * result.
     *<p>
     * The various buttons depend on three boolean input conditions:
     *<dl style="text-indent: 3em">
     *<dt>E - "existing"</dt>
     *<dd> True if we are editing an existing account to be updated in
     *     the account store.  If false, it means we're editing an
     *     account to be created.
     *<dt>C - "changed"</dt>
     *<dd> True if one or more fields has been edited.
     *<dt>V - "valid"</dt>
     *<dd> True if the account fields are valid to be saved to an
     *     account.  Currently, this means all fields are non-empty.
     *</dl>
     *<p>
     * These inputs determine button states as follows:
     *<dl style="text-indent: 3em">
     *<dt>DELETE and COPY buttons</dt>
     *<dd> Enabled only if it's an existing account with no changes.
     *<dt>CLEAR/REVERT button</dt>
     *<dd> For an existing account with no changes, the text is
     *     <code>CLEAR</code>, and the button is enabled.  Otherwise,
     *     the text is <code>REVERT</code>, and the button is enabled if
     *     there are any changes.
     *<dt>UPDATE/CREATE button</dt>
     *<dd> For an existing account, the text is <code>UPDATE</code>;
     *     otherwise, the text is <code>CREATE</code>.  The button is
     *     enabled if there are changes and all fields are valid.
     *<dt>Account list</dt>
     *<dd> Selecting a new account is disabled if there are changes.
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
	laterButton.setEnabled(!changed
			       && !accountDataPanel.isLatestUpdate());
	earlierButton.setEnabled(!changed
				 && !accountDataPanel.isEarliestUpdate());
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

    /**
     * Re-initialize the contents of {@link #accountList}.  This method
     * must be called after any change that adds, deletes, or changes an
     * account in the account store.  After the call, the current
     * selection will have been cleared.
     */
    private void refillAccountList() {
	Vector<Account> v = new Vector<Account>();
	for (Account acct : myAccountStore) {
	    v.add(acct);
	}
	// This call leaves the current selection cleared.  If there was
	// a selection prior to the call, this will also trigger a call
	// to `valueChanged()`.
	accountList.setListData(v);
    }

    /**
     * Delete the currently selected account.  After the call, the
     * current selection will have been cleared.
     */
    private void deleteAccount() {
	// validateAccountFields() guarantees that there's an account
	// selection in this case.
	myAccountStore.deleteAccount(accountList.getSelectedValue());
	accountsChanged = true;
	refillAccountList();
    }

    /**
     * Update the account store with current data from the account
     * panel.  If there is an account currently selected, the data is
     * used to update the selected account.  Otherwise, a new account is
     * created from the data.  After the call, the new or updated
     * account will be the current selection.
     */
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

    /**
     * Copy the password from the account panel into the system
     * clipboard, so that it can be pasted into some other application.
     */
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
	    deleteAccount();
	} else if (actionName.equals(CLEAR)) {
	    // validateAccountFields() guarantees that there's a selection
	    // to be cleared.  Clearing the selection will trigger the
	    // event that updates the account panel.
	    accountList.clearSelection();
	} else if (actionName.equals(REVERT)) {
	    accountDataPanel.restoreSelectedAccount();
	} else if (actionName.equals(COPY)) {
	    copyPasswordToClipboard();
	} else if (actionName.equals(GENERATE)) {
	    accountDataPanel.setPassword(
		    passwordPanel.generatePassword());
	} else if (actionName.equals(LATER)) {
	    accountDataPanel.selectLaterUpdate();
	    laterButton.setEnabled(!accountDataPanel.isLatestUpdate());
	    earlierButton.setEnabled(true);
	    generateButton.setEnabled(accountDataPanel.isEditable());
	} else if (actionName.equals(EARLIER)) {
	    accountDataPanel.selectEarlierUpdate();
	    laterButton.setEnabled(true);
	    earlierButton.setEnabled(!accountDataPanel.isEarliestUpdate());
	    generateButton.setEnabled(accountDataPanel.isEditable());
	} else if (actionName.equals(updateButton.getText())) {
	    updateAccountStore();
	}
    }

    public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting())
	    return;
	accountDataPanel.setSelectedAccount(
		accountList.getSelectedValue());
	laterButton.setEnabled(!accountDataPanel.isLatestUpdate());
	earlierButton.setEnabled(!accountDataPanel.isEarliestUpdate());
	generateButton.setEnabled(accountDataPanel.isEditable());
	accountList.requestFocusInWindow();
    }

    /**
     * Clear our account data as for the "File-&gt;New" menu option.
     */
    void clearAccounts() {
	myAccountStore = new AccountStore();
	accountsChanged = false;
	refillAccountList();
    }

    /**
     * Read account data as for the "File-&gt;Open" menu option.
     *
     * @param accountsFile File from which to read our new account
     *     data.
     * @param password Password for decrypting the account data in the
     *     file.
     * @throws GeneralSecurityException Indicates a failure relating to
     *     file decryption.
     * @throws IOException Indicates a failure reading from the file.
     */
    void openAccounts(File accountsFile, char[] password)
	    throws IOException, GeneralSecurityException {
	myAccountStore.readAccounts(
	    new FileInputStream(accountsFile), password);
	accountsChanged = false;
	refillAccountList();
    }

    /**
     * Save account data as for the "File-&gt;Save As" menu option.
     *
     * @param accountsFile File from to which to write our new account
     *     data.
     * @param password Password for encrypting the account data in the
     *     file.
     * @throws GeneralSecurityException Indicates a failure relating to
     *     file encryption.
     * @throws IOException Indicates a failure writing to the file.
     */
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

    /**
     * Return whether the account store has changes that have not been
     * written out.
     *
     * @return True if any changes have not been written out by
     *     saveAccounts().
     */
    boolean needSave() {
	return accountsChanged;
    }
}
