/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 */
class AccountStore {
    private TreeMap<String, Account> myAccounts;

    public AccountStore() {
	myAccounts = new TreeMap<String, Account>();
    }

    public AccountStore(InputStream rawInput) throws IOException {
	this();
	readAccounts(rawInput);
    }

    public void addAccount(Account newAccount) {
	assert !myAccounts.containsKey(
		    newAccount.getDescription());

	myAccounts.put(newAccount.getDescription(),
				    newAccount);
    }

    public void updateAccount(Account account,
			      String description, String url,
			      String username, String password) {
	myAccounts.remove(account.getDescription());
	account.update(description, url, username, password);
	addAccount(account);
    }

    public void createAccount(String description, String url,
			      String username, String password) {
	addAccount(new Account(description, url, username, password));
    }

    public Iterable<Account> allAccounts() {
	return myAccounts.values();
    }

    private static final byte[] FILEMAGIC = "ACCTS.00".getBytes();

    private void readMagic(DataInput in) throws IOException {
	byte[] magic = new byte[FILEMAGIC.length];
        in.readFully(magic);
	for (int i = 0; i < FILEMAGIC.length; i++) {
	    if (magic[i] != FILEMAGIC[i]) {
		String message =
		    "Unknown file format: " + new String(magic);
		throw new IOException(message);
	    }
	}
    }

    public void readAccounts(InputStream raw) throws IOException {
	DataInput in = new DataInputStream(raw);
	readMagic(in);
	int nElements = in.readInt();
	for (int i = 0; i < nElements; i++) {
	    addAccount(new Account(in));
	}
    }

    public void writeAccounts(OutputStream raw) throws IOException {
	DataOutput out = new DataOutputStream(raw);
	out.write(FILEMAGIC);
	out.writeInt(myAccounts.size());
	for (Account acct : myAccounts.values()) {
	    acct.writeAccount(out);
	}
    }
}
