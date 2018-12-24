/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

/**
 */
class Account {
    private String myUrl;
    private String myDescription;
    private String myUsername;
    private String myPassword;

    public Account(String url, String description,
		   String username, String password) {
	myUrl = url;
	update(description, username, password);
    }

    public void update(String description,
		       String username, String password) {
	myDescription = description;
	myUsername = username;
	myPassword = password;
    }

    public String getUrl() {
	return myUrl;
    }

    public String getDescription() {
	return myDescription;
    }

    public String getUsername() {
	return myUsername;
    }

    public String getPassword() {
	return myPassword;
    }

    public String toString() {
	return myDescription;
    }
}
