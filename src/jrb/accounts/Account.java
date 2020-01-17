/*
 * Copyright 2018, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * A class representing one account in an account store.  An
 * <code>Account</code> contains a time-ordered sequence of
 * <code>AccountData</code> objects assigned to the account since its
 * creation.  The history is appended to by calls to {@link #update}.
 *<p>
 * As an instance of <code>AccountData</code>, an <code>Account</code>
 * returns the values of its account data properties as of the most
 * recent update.
 */
class Account extends AccountData {
    /**
     * One entry in the update history of an <code>Account</code>.
     */
    private static class UpdateEntry extends AccountData
	    implements Comparable<UpdateEntry> {
	private String description;
	private String url;
	private String username;
	private String password;
	private Date timestamp;

	UpdateEntry(String description, String url,
		    String username, String password,
		    Date timestamp) {
	    this.description = description;
	    this.url = url;
	    this.username = username;
	    this.password = password;
	    this.timestamp = timestamp;
	}

	UpdateEntry(String description, String url,
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

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof UpdateEntry)) {
		return false;
	    }
	    UpdateEntry data = (UpdateEntry) o;
	    return description.equals(data.description)
		    && url.equals(data.url)
		    && username.equals(data.username)
		    && password.equals(data.password)
		    && timestamp.equals(data.timestamp);
	}

	@Override
	public int compareTo(UpdateEntry data) {
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

    /**
     * A UUID distinguishing this account from all others.  The UUID is
     * permanently assigned when the account is first created, and is
     * not changed by updates.  The UUID is written as part of save
     * files, and is read back when restoring from a file.  Thus, if two
     * different save files have an account with the same UUID, the two
     * files describe the same account, although the histories may have
     * diverged.
     */
    private UUID myUUID;

    /**
     * The history of <code>AccountData</code> entries created by
     * updates to this account.
     */
    private List<UpdateEntry> myHistory;

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
	myHistory = new Vector<UpdateEntry>();
	myUUID = UUID.randomUUID();
	UpdateEntry data = new UpdateEntry(
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
	myHistory = new Vector<UpdateEntry>();
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
	UpdateEntry data = new UpdateEntry(
		description, url, username, password);
	UpdateEntry prevData = myHistory.get(myHistory.size() - 1);
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
     *<p>
     * XXX - It seems like there's no legitimate reason for both this
     * method and the constructor, and that this method should be
     * deleted...
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
	    if (size <= 0) {
		throw new AccountFileFormatException(
			"Invalid history size for account");
	    }
	} else {
	    myUUID = UUID.randomUUID();
	    size = 1;
	}
	Date prevTimestamp = new Date(0);
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
	    if (prevTimestamp.compareTo(timestamp) >= 0) {
		throw new AccountFileFormatException(
			"Account history not in time order");
	    }
	    prevTimestamp = timestamp;
	    UpdateEntry data = new UpdateEntry(
		    description, url, username, password, timestamp);
	    myHistory.add(data);
	    size--;
	}
	if (prevTimestamp.compareTo(new Date()) > 0) {
	    throw new AccountFileFormatException(
		    "Account history has entries in the future");
	}
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
	for (UpdateEntry data : myHistory) {
	    out.writeUTF(data.description);
	    out.writeUTF(data.url);
	    out.writeUTF(data.username);
	    out.writeUTF(data.password);
	    out.writeLong(data.timestamp.getTime());
	}
    }

    /**
     * Return this account's UUID as a string. The UUID is permanently
     * assigned when the account is first created, and is preserved
     * across both history updates and file save/restore cycles.
     *
     * @return This account's current UUID in canonical text format.
     */
    public String getUUID() {
	return myUUID.toString();
    }

    /**
     * Return the number of updates that have been applied to this
     * account.  This number is one more than the maximum value that
     * may be passed into {@link #getUpdateData}.
     *
     * @return The total number of times <code>update()</code> has been
     *     called on this account.
     */
    public int getUpdateCount() {
	return myHistory.size();
    }

    /**
     * Return account data from the update history.  The update history
     * is accessed by an index, which counts the number of calls to
     * {@link #update} back in time to go.  Thus, index 0 refers to
     * the current data, index 1 refers to the data just before the most
     * recent call to <code>update()</code>, and so on.
     *
     * @param index The number of updates back in time to go.
     * @return Account data as it was <code>index</code> updates ago.
     */
    public AccountData getUpdateData(int index) {
	return myHistory.get(myHistory.size() - index - 1);
    }

    /**
     * Return this account's description property, as of the most recent
     * update.
     *
     * @return This account's current description.
     */
    public String getDescription() {
	return myHistory.get(myHistory.size() - 1).description;
    }

    /**
     * Return this account's URL property, as of the most recent update.
     *
     * @return This account's current URL.
     */
    public String getUrl() {
	return myHistory.get(myHistory.size() - 1).url;
    }

    /**
     * Return this account's user name property, as of the most recent
     * update.
     *
     * @return This account's current user name.
     */
    public String getUsername() {
	return myHistory.get(myHistory.size() - 1).username;
    }

    /**
     * Return this account's password property, as of the most recent
     * update.
     *
     * @return This account's current password.
     */
    public String getPassword() {
	return myHistory.get(myHistory.size() - 1).password;
    }

    /**
     * Return the timestamp of the most recent update to this account.
     *
     * @return The current timestamp of this account's data.
     */
    public Date getTimestamp() {
	return myHistory.get(myHistory.size() - 1).timestamp;
    }

    /**
     * Returns a string representation of this account.  The account's
     * current description is used as its representation.
     *
     * @return This account's current description.
     */
    public String toString() {
	return getDescription();
    }

    /**
     * Indicates whether this <code>Account</code> is equal to some
     * arbitrary object.
     *
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
