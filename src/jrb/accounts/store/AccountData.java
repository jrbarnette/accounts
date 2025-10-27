/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.store;

import java.util.Date;
import java.util.UUID;

/**
 * An abstract class representing the data known about one account in an
 * account store.  Account data consists of five user visible items:
 *<dl style="indent: 3em">
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
 *<dt>Timestamp</dt>
 *<dd> The time (as a <code>Date</code>) when the data was created.
 *</dl>
 */
public abstract class AccountData {
    /**
     * Return the account's description property.
     *
     * @return This account's description.
     */
    public abstract String getDescription();

    /**
     * Return the account's URL property.
     *
     * @return This account's URL.
     */
    public abstract String getUrl();

    /**
     * Return the account's user name property.
     *
     * @return This account's user name.
     */
    public abstract String getUsername();

    /**
     * Return the account's password property.
     *
     * @return This account's password.
     */
    public abstract String getPassword();

    /**
     * Return the timestamp on this account data.
     *
     * @return The time when this data was created.
     */
    public abstract Date getTimestamp();
}
