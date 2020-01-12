/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 */
class Account {
    private static class AccountData
	    implements Comparable<AccountData> {
	String url;
	String description;
	String username;
	String password;
	Date timestamp;

	AccountData(String description, String url,
		    String username, String password,
		    Date timestamp) {
	    this.description = description;
	    this.url = url;
	    this.username = username;
	    this.password = password;
	    this.timestamp = timestamp;
	}

	AccountData(String description, String url,
		    String username, String password) {
	    this(description, url, username, password, new Date());
	}

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof AccountData)) {
		return false;
	    }
	    AccountData data = (AccountData) o;
	    return description.equals(data.description)
		    && url.equals(data.url)
		    && username.equals(data.username)
		    && password.equals(data.password)
		    && timestamp.equals(data.timestamp);
	}

	@Override
	public int compareTo(AccountData data) {
	    int rv = timestamp.compareTo(data.timestamp);
	    if (rv != 0) return rv;
	    rv = description.compareTo(data.description);
	    if (rv != 0) return rv;
	    rv = url.compareTo(data.url);
	    if (rv != 0) return rv;
	    rv = username.compareTo(data.username);
	    if (rv != 0) return rv;
	    return password.compareTo(data.password);
	}
    }

    private UUID myUUID;
    private SortedSet<AccountData> myHistory;

    public Account(String description, String url,
		   String username, String password) {
	myUUID = UUID.randomUUID();
	myHistory = new TreeSet<AccountData>();
	AccountData data = new AccountData(
		description, url, username, password);
	myHistory.add(data);
    }

    Account(DataInput in, int formatVersion) throws IOException {
	myHistory = new TreeSet<AccountData>();
	readAccount(in, formatVersion);
    }

    public void update(String description, String url,
		       String username, String password) {
	AccountData data = new AccountData(
		description, url, username, password);
	AccountData prevData = myHistory.last();
	// The unit tests will fail without this loop, and I'm pretty
	// sure this is a fix, not a hack.
	while (data.timestamp.equals(prevData.timestamp)) {
	    data.timestamp = new Date();
	}
	myHistory.add(data);
    }

    void readAccount(DataInput in, int formatVersion)
	    throws IOException {
	int size;
	if (formatVersion >= AccountStore.FORMAT_V2) {
	    myUUID = UUID.fromString(in.readUTF());
	    size = in.readInt();
	} else {
	    myUUID = UUID.randomUUID();
	    size = 1;
	}
	while (size > 0) {
	    String description = in.readUTF();
	    String url = in.readUTF();
	    String username = in.readUTF();
	    String password = in.readUTF();
	    Date timestamp;
	    if (formatVersion >= AccountStore.FORMAT_V2) {
		timestamp = new Date(in.readLong());
	    } else {
		timestamp = new Date();
	    }
	    AccountData data = new AccountData(
		    description, url, username, password, timestamp);
	    myHistory.add(data);
	    size--;
	}
    }

    void writeAccount(DataOutput out) throws IOException {
	out.writeUTF(myUUID.toString());
	out.writeInt(myHistory.size());
	for (AccountData data : myHistory) {
	    out.writeUTF(data.description);
	    out.writeUTF(data.url);
	    out.writeUTF(data.username);
	    out.writeUTF(data.password);
	    out.writeLong(data.timestamp.getTime());
	}
    }

    public String getUrl() {
	return myHistory.last().url;
    }

    public String getDescription() {
	return myHistory.last().description;
    }

    public String getUsername() {
	return myHistory.last().username;
    }

    public String getPassword() {
	return myHistory.last().password;
    }

    public String toString() {
	return getDescription();
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Account)) {
	    return false;
	}
	Account other = (Account) o;
	return myUUID.equals(other.myUUID)
		&& myHistory.equals(other.myHistory);
    }
}
