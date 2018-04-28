/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Map;
import java.util.TreeMap;

/**
 */
class AccountStore {
    static class AccountData {
	String description;
	String username;
	String password;

	AccountData(String description,
		    String username, String password) {
	    this.description = description;
	    this.username = username;
	    this.password = password;
	}
    }
    Map<String, AccountData> myAccounts;

    AccountStore() {
	myAccounts = new TreeMap<String, AccountData>();
    }

    public void updateAccount(String url, String description,
			      String username, String password) {
	AccountData newAccount;
	if (myAccounts.containsKey(url)) {
	    newAccount = myAccounts.get(url);
	} else {
	    newAccount = new AccountData(description,
					 username, password);
	}
	myAccounts.put(url, newAccount);
    }
}
