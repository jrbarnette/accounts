/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Map;
import java.util.TreeMap;

/**
 */
class AccountStore {
    private Map<String, Account> myAccounts;

    AccountStore() {
	myAccounts = new TreeMap<String, Account>();
    }

    public void updateAccount(String url, String description,
			      String username, String password) {
	Account newAccount;
	if (myAccounts.containsKey(url)) {
	    newAccount = myAccounts.get(url);
	    newAccount.update(description, username, password);
	} else {
	    newAccount = new Account(url, description,
				     username, password);
	}
	myAccounts.put(url, newAccount);
    }
}
