/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.store;

import java.util.Date;

class AccountTestData extends AccountData {
    private String description;
    private String url;
    private String username;
    private String password;
    private Date timestamp;

    private Account myAccount;

    AccountTestData(String description, String url,
		    String username, String password,
		    Date timestamp) {
	myAccount = new Account(description, url, username, password);
	this.description = description;
	this.url = url;
	this.username = username;
	this.password = password;
	this.timestamp = timestamp;
    }

    AccountTestData(String description, String url,
		    String username, String password) {
	this(description, url, username, password, new Date());
    }

    public String getDescription() {
	return description;
    }

    public String getUrl() {
	return url;
    }

    public String getUsername() {
	return username;
    }

    public String getPassword() {
	return password;
    }

    public Date getTimestamp() {
	return timestamp;
    }

    Account getAccount() {
	return myAccount;
    }

    boolean matches(AccountData acct) {
	return description.equals(acct.getDescription())
		&& url.equals(acct.getUrl())
		&& username.equals(acct.getUsername())
		&& password.equals(acct.getPassword());
    }
}
