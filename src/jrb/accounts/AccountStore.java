/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 */
class AccountStore {
    private TreeMap<String, Account> myAccounts;

    AccountStore() {
	myAccounts = new TreeMap<String, Account>();
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
}
