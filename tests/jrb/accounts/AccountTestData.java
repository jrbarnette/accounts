/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

class AccountTestData {
    private Account myAccount;

    AccountTestData(Account acct) {
	myAccount = acct;
    }

    AccountTestData(String description, String url,
		    String username, String password) {
	this(new Account(description, url, username, password));
    }

    boolean matches(Account acct) {
	return myAccount.getDescription().equals(acct.getDescription())
		&& myAccount.getUrl().equals(acct.getUrl())
		&& myAccount.getUsername().equals(acct.getUsername())
		&& myAccount.getPassword().equals(acct.getPassword());
    }

    Account getAccount() {
	return myAccount;
    }
}
