/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.net.URL;

/**
 */
class Account {
    private String myUrl;
    private String myDescription;
    private String myUsername;
    private String myPassword;

    public Account(String description, String url,
		   String username, String password) {
	update(description, url, username, password);
    }

    public void update(String description, String url,
		       String username, String password) {
	myDescription = description;
	myUrl = url;
	myUsername = username;
	myPassword = password;
    }

    @Override
    public boolean equals(Object o) {
	return o instanceof Account
	    && myDescription == ((Account) o).myDescription
	    && myUrl == ((Account) o).myUrl
	    && myUsername == ((Account) o).myUsername
	    && myPassword == ((Account) o).myPassword;
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
