/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Vector;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.DocumentListener;

/**
 * A component for displaying and editing the data associated with a
 * single account.
 */
class AccountDataPanel extends JPanel implements FocusListener {
    static final Account PROTOTYPE_ACCOUNT
	    = new Account("123456789 123456789 123456789 12",
			  "http://example.com", "user", "pw");
    static private final int FIELD_COLUMNS
	    = PROTOTYPE_ACCOUNT.getDescription().length();

    static final int EXISTING = 1;
    static final int CHANGED = 2;
    static final int VALID = 4;

    private JLabel uuidLabel = new JLabel();
    private JLabel timestampLabel = new JLabel();

    private JTextField descriptionText = new JTextField(FIELD_COLUMNS);
    private JTextField urlText = new JTextField(FIELD_COLUMNS);
    private JTextField usernameText = new JTextField(FIELD_COLUMNS);
    private JPasswordField passwordText
	    = new JPasswordField(FIELD_COLUMNS);

    private Account savedAccount;

    private void initializeTextFields(DocumentListener listener) {
	JTextField[] fields = {
	    descriptionText, urlText, usernameText, passwordText,
	};

	for (JTextField f : fields) {
	    f.addFocusListener(this);
	    f.getDocument().addDocumentListener(listener);
	}
    }

    public AccountDataPanel(DocumentListener listener) {
	initializeTextFields(listener);

	JPanel textBoxColumn = new JPanel(new GridLayout(0, 1));
	JPanel namesColumn = new JPanel(new GridLayout(0, 1));

	// Wrap each JLabel in a panel so that its margin aligns with the
	// text fields.
	JPanel uuidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	uuidPanel.add(uuidLabel);
	JPanel timestampPanel
		= new JPanel(new FlowLayout(FlowLayout.LEFT));
	timestampPanel.add(timestampLabel);

	JComponent[] accountData = {
	    uuidPanel,
	    descriptionText, urlText, usernameText, passwordText,
	    timestampPanel,
	};

	String[] names = {
	    "Account UUID",
	    "Description", "URL", "User Name", "Password",
	    "Timestamp",
	};

	for (int i = 0; i < names.length; i++) {
	    JPanel aPanel = new JPanel(
		    new FlowLayout(FlowLayout.RIGHT, 0, 5));
	    JLabel aLabel = new JLabel(names[i]);
	    aLabel.setLabelFor(accountData[i]);
	    aPanel.add(aLabel);
	    namesColumn.add(aPanel);

	    textBoxColumn.add(accountData[i]);
	}

	add(namesColumn);
	add(textBoxColumn);
    }

    private void updateAccountData() {
	if (savedAccount != null) {
	    uuidLabel.setText(savedAccount.getUUID());
	    timestampLabel.setText(savedAccount.getTimestamp());

	    // These changes will trigger DocumentEvent notifications
	    // that will update the button states.
	    descriptionText.setText(savedAccount.getDescription());
	    urlText.setText(savedAccount.getUrl());
	    usernameText.setText(savedAccount.getUsername());
	    passwordText.setText(savedAccount.getPassword());
	} else {
	    uuidLabel.setText("");
	    timestampLabel.setText("");

	    // These changes will trigger DocumentEvent notifications
	    // that will update the button states.
	    descriptionText.setText("");
	    urlText.setText("");
	    usernameText.setText("");
	    passwordText.setText("");
	}
    }

    void setSelectedAccount(Account account) {
	savedAccount = account;
	updateAccountData();
	if (savedAccount == null) {
	    descriptionText.requestFocusInWindow();
	}
    }

    void restoreSelectedAccount() {
	updateAccountData();
	descriptionText.requestFocusInWindow();
    }

    int getFieldState() {
	String description = descriptionText.getText();
	String url = urlText.getText();
	String username = usernameText.getText();
	String password = new String(passwordText.getPassword());

	int rv = 0;
	boolean changed;
	if (savedAccount != null) {
	    rv |= EXISTING;
	    changed = !description.equals(savedAccount.getDescription())
		    || !url.equals(savedAccount.getUrl())
		    || !username.equals(savedAccount.getUsername())
		    || !password.equals(savedAccount.getPassword());
	} else {
	    changed = !description.isEmpty()
		    || !url.isEmpty()
		    || !username.isEmpty()
		    || !password.isEmpty();
	}
	if (changed) {
	    rv |= CHANGED;
	    boolean valid = !description.isEmpty()
			&& !url.isEmpty()
			&& !username.isEmpty()
			&& !password.isEmpty();
	    if (valid) {
		rv |= VALID;
	    }
	} else if (account != null) {
	    rv |= VALID;
	}
	return rv;
    }

    /**
     * Return this account's description property, as of the most recent
     * update.
     *
     * @return This account's current description.
     */
    public String getDescription() {
	return descriptionText.getText();
    }

    /**
     * Return this account's URL property, as of the most recent update.
     *
     * @return This account's current URL.
     */
    public String getUrl() {
	return urlText.getText();
    }

    /**
     * Return this account's user name property, as of the most recent
     * update.
     *
     * @return This account's current user name.
     */
    public String getUsername() {
	return usernameText.getText();
    }

    /**
     * Return this account's password property, as of the most recent
     * update.
     *
     * @return This account's current password.
     */
    public String getPassword() {
	return new String(passwordText.getPassword());
    }

    void setPassword(char[] text) {
	passwordText.setText(new String(text));
	passwordText.requestFocusInWindow();
    }

    public void focusGained(FocusEvent e) {
	((JTextField) e.getSource()).selectAll();
    }

    public void focusLost(FocusEvent e) {
    }
}
