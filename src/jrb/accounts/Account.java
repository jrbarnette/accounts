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
 * A class representing one account in an account store.  An account
 * tracks four user visible data items:
 *<dl>
 *<dt>Description</dt>
 *<dd> User provided text describing or naming the account.
 *<dt>URL</dt>
 *<dd> The URL of the account.  Typically a website, but it could be
 *     any resource.  Currently, there are no syntax checks on this
 *     data.
 *<dt>Username</dt>
 *<dd> The username to use when logging in.
 *<dt>Password</dt>
 *<dd> The password to use when logging in.
 *</dl>
 *<p>
 * Each account keeps a log of the history of changes made to its data.
 * Each entry in the log contains all of the data for the account, plus
 * the time when the entry was created.
 *<p>
 * <em>Note:</em> The history log is part of the save file format, but
 * currently isn't accessible via the API.
 */
class Account {
    private static class AccountData
	    implements Comparable<AccountData> {
	String description;
	String url;
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

    /**
     * Create a new account object from initial data values.  The given
     * data becomes the initial entry in the account's history.
     *
     * @param description The initial description for this account.
     * @param url The initial URL for this account.
     * @param username The initial user name for this account.
     * @param password The initial password for this account.
     */
    public Account(String description, String url,
		   String username, String password) {
	myHistory = new TreeSet<AccountData>();
	myUUID = UUID.randomUUID();
	AccountData data = new AccountData(
		description, url, username, password);
	myHistory.add(data);
    }

    /**
     * Create a new account object by reading it from an input stream.
     * The created account will include a complete history as it was
     * saved in the file.
     *
     * @param in The data input stream from which to read the account's
     *     data.
     * @param formatVersion The version of the file format being read.
     *     Prior to version 2, there was no account history, but only
     *     a single entry with current account data.
     * @throws IOException Indicates a failure reading account data.
     */
    Account(DataInput in, int formatVersion) throws IOException {
	myHistory = new TreeSet<AccountData>();
	readAccount(in, formatVersion);
    }

    /**
     * Add a new entry to this account's history.  The new entry's
     * timestamp will be later than all prior entries.
     *
     * @param description The new description for this account.
     * @param url The new URL for this account.
     * @param username The new user name for this account.
     * @param password The new password for this account.
     */
    public void update(String description, String url,
		       String username, String password) {
	AccountData data = new AccountData(
		description, url, username, password);
	AccountData prevData = myHistory.last();
	// The unit tests will fail without this loop, and I'm pretty
	// sure this is a fix, not a hack.
	while (data.timestamp.compareTo(prevData.timestamp) <= 0) {
	    data.timestamp = new Date();
	}
	myHistory.add(data);
    }

    /**
     * Reinitialize this account by reading it from an input stream.
     * The account's current data and history will be replaced with the
     * data and history as found in the stream.
     *<p>
     * In the case of a pre-version 2 format, the account is given a
     * single history entry, timestamped as of the current time.
     *
     * @param in The data input stream from which to read the account's
     *     data.
     * @param formatVersion The version of the file format being read.
     *     Prior to version 2, there was no account history, but only
     *     a single entry with account data.
     * @throws IOException Indicates a failure reading account data.
     */
    void readAccount(DataInput in, int formatVersion)
	    throws IOException {
	myHistory.clear();
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
	assert myHistory.last().timestamp.compareTo(new Date()) >= 0;
    }

    /**
     * Write this account to an output stream.  Full account history
     * will be written, consistent with the file format as of version 2.
     *
     * @param out The data output stream to which to write the account's
     *     data.
     * @throws IOException Indicates a failure writing account data.
     */
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

    /**
     * Return this account's description property, as of the most recent
     * update.
     *
     * @return This account's current description.
     */
    public String getDescription() {
	return myHistory.last().description;
    }

    /**
     * Return this account's URL property, as of the most recent update.
     *
     * @return This account's current URL.
     */
    public String getUrl() {
	return myHistory.last().url;
    }

    /**
     * Return this account's user name property, as of the most recent
     * update.
     *
     * @return This account's current user name.
     */
    public String getUsername() {
	return myHistory.last().username;
    }

    /**
     * Return this account's password property, as of the most recent
     * update.
     *
     * @return This account's current password.
     */
    public String getPassword() {
	return myHistory.last().password;
    }

    /**
     * @return This account's current description.
     */
    public String toString() {
	return getDescription();
    }

    /**
     * @return True if <code>o</code> is an <code>Account</code> object
     *     and its UUID and history are equal.
     */
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
