/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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

    Account(DataInput in) throws IOException {
	readAccount(in);
    }

    @Override
    public boolean equals(Object o) {
	return o instanceof Account
	    && myDescription == ((Account) o).myDescription
	    && myUrl == ((Account) o).myUrl
	    && myUsername == ((Account) o).myUsername
	    && myPassword == ((Account) o).myPassword;
    }

    public void update(String description, String url,
		       String username, String password) {
	myDescription = description;
	myUrl = url;
	myUsername = username;
	myPassword = password;
    }

    void readAccount(DataInput in) throws IOException {
	myDescription = in.readUTF();
	myUrl = in.readUTF();
	myUsername = in.readUTF();
	myPassword = in.readUTF();
    }

    void writeAccount(DataOutput out) throws IOException {
	out.writeUTF(myDescription);
	out.writeUTF(myUrl);
	out.writeUTF(myUsername);
	out.writeUTF(myPassword);
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
