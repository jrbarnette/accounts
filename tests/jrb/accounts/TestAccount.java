/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests against the joint behavior of {@link Account} and
 * {@link AccountTestData}.  The main objective here is to test the
 * <code>Account</code> class, but we need to be able to trust that
 * <code>AccountTestData</code> is a proper proxy, so some of the tests
 * here exercise the latter class, or exercise interactions between the
 * two.
 *<p>
 * We test these basic behaviors:
 *<ul>
 *<li> If you construct an <code>Account</code> its various accessor
 *     methods return the values you constructed it with.
 *<li> If you compare identical accounts with
 *     <code>AccountTestData.matches()</code>, the result is a match.
 *<li> If you compare distinct accounts with
 *     <code>AccountTestData.matches()</code>, the result is no match.
 *<li> When comparing with <code>AccountTestData.matches()</code>,
 *     every account field is significant.
 *<li> <code>Account.update()</code> updates all fields as expected.
 *<li> Accounts can be written out and then read back without changes.
 *</ul>
 */
public class TestAccount {
    private static final String[] DESCRIPTION = {
	"a", "b",
    };

    private static final String[] URL = {
	"http://a.com", "http://b.com",
    };

    private static final String[] USERNAME = {
	"username0", "username1",
    };

    private static final String[] PASSWORD = {
	"p0", "p1",
    };

    /**
     * Test {@link Account#getDescription}.
     */
    @Test
    public void testDescription() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	assertEquals("Description doesn't match constructor",
		     DESCRIPTION[0], data.getAccount().getDescription());
    }

    /**
     * Test {@link Account#getUrl}.
     */
    @Test
    public void testUrl() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	assertEquals("URL doesn't match constructor",
		     URL[0], data.getAccount().getUrl());
    }

    /**
     * Test {@link Account#getUsername}.
     */
    @Test
    public void testUsername() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	assertEquals("User name doesn't match constructor",
		     USERNAME[0], data.getAccount().getUsername());
    }

    /**
     * Test {@link Account#getPassword}.
     */
    @Test
    public void testPassword() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	assertEquals("Password doesn't match constructor",
		     PASSWORD[0], data.getAccount().getPassword());
    }

    /**
     * Test {@link AccountTestData#matches} is true against two
     # equivalent accounts.
     */
    @Test
    public void testMatches() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		new String(data.getAccount().getDescription()),
		new String(data.getAccount().getUrl()),
		new String(data.getAccount().getUsername()),
		new String(data.getAccount().getPassword()));
	assertTrue("Identical Account objects don't match",
		   data.matches(acct));
    }

    /**
     * Test {@link AccountTestData#matches} is false against two
     # completely different accounts.
     */
    @Test
    public void testNoMatches() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		DESCRIPTION[1], URL[1], USERNAME[1], PASSWORD[1]);
	assertFalse("Completely different Account objects match",
		    data.matches(acct));
    }

    /**
     * Test that the description field is significant to
     * {@link AccountTestData#matches}.
     */
    @Test
    public void testUnequalDescription() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		DESCRIPTION[1], URL[0], USERNAME[0], PASSWORD[0]);
	assertFalse("Different descriptions, but accounts match",
		    data.matches(acct));
    }

    /**
     * Test that the URL field is significant to
     * {@link AccountTestData#matches}.
     */
    @Test
    public void testUnequalUrl() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		DESCRIPTION[0], URL[1], USERNAME[0], PASSWORD[0]);
	assertFalse("Different URLs, but accounts match",
		    data.matches(acct));
    }

    /**
     * Test that the Username field is significant to
     * {@link AccountTestData#matches}.
     */
    @Test
    public void testUnequalUsername() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		DESCRIPTION[0], URL[0], USERNAME[1], PASSWORD[0]);
	assertFalse("Different user names, but accounts match",
		    data.matches(acct));
    }

    /**
     * Test that the Password field is significant to
     * {@link AccountTestData#matches}.
     */
    @Test
    public void testUnequalPassword() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[1]);
	assertFalse("Different passwords, but accounts match",
		    data.matches(acct));
    }

    /**
     * Test that {@link Account#update} updates all fields to the
     * new passed in values.
     */
    @Test
    public void testUpdate() {
	AccountTestData data = new AccountTestData(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct = new Account(
		DESCRIPTION[1], URL[1], USERNAME[1], PASSWORD[1]);
	acct.update(
		new String(data.getAccount().getDescription()),
		new String(data.getAccount().getUrl()),
		new String(data.getAccount().getUsername()),
		new String(data.getAccount().getPassword()));
	assertTrue("Accounts don't match after update()",
		   data.matches(acct));
    }

    /**
     * Create a <code>DataInput</code> object that can be read to reconstruct
     * the given account.  The implementation uses {@link
     * Account#writeAccount} to save the account data, and then
     * constructs its return value to read from the saved output.
     *
     * @param account The account to be saved for reconstruction.
     * @return A data input object from which the <code>account</code>
     *     parameter can be reconstructed.
     * @throws IOException Any exception is passed up as a test failure.
     */
    private DataInput createAccountDataInput(Account account)
	    throws IOException {
	ByteArrayOutputStream output = new ByteArrayOutputStream();
	account.writeAccount(new DataOutputStream(output));
	ByteArrayInputStream input =
		new ByteArrayInputStream(output.toByteArray());
	return new DataInputStream(input);
    }

    /**
     * Test using the {@link Account#Account(DataInput, int)}
     * constructor to read a freshly created account.
     *<p>
     * Constructs a new account, and writes it out using {@link
     * Account#writeAccount}.  Then calls the constructor against
     * the saved account data, and asserts that the two account
     * objects are equal.
     *
     * @throws IOException Any exception is passed up as a test failure.
     */
    @Test
    public void testReadAccountConstructor() throws IOException {
	Account acct0 = new Account(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct1 = new Account(createAccountDataInput(acct0),
				    AccountStore.FORMAT_CURRENT);
	assertEquals("Accounts don't match after write/read cycle",
		     acct0, acct1);
    }

    /**
     * Test using {@link Account#readAccount(DataInput, int)} to read a
     * freshly created account into another existing account object.
     *<p>
     * Constructs two new, different accounts.  Then writes out one
     * account, and reads it back into the other.  Asserts that the
     * second account is now equal to the first.
     *
     * @throws IOException Any exception is passed up as a test failure.
     */
    @Test
    public void testReadAccountMethod() throws IOException {
	Account acct0 = new Account(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	Account acct1 = new Account(
		DESCRIPTION[1], URL[1], USERNAME[1], PASSWORD[1]);
	acct1.readAccount(createAccountDataInput(acct0),
			  AccountStore.FORMAT_CURRENT);
	assertEquals("Accounts don't match after write/read cycle",
		     acct0, acct1);
    }

    /**
     * Test using the {@link Account#Account(DataInput, int)}
     * constructor to read an account with a history of one update.
     *<p>
     * Constructs a new account, applies an update, and writes it out
     * using {@link Account#writeAccount}.  Then calls the constructor
     * against the saved account data, and asserts that the two account
     * objects are equal.
     *
     * @throws IOException Any exception is passed up as a test failure.
     */
    @Test
    public void testReadNewUpdatedAccount() throws IOException {
	Account acct0 = new Account(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	acct0.update(
		DESCRIPTION[1], URL[1], USERNAME[1], PASSWORD[1]);
	Account acct1 = new Account(createAccountDataInput(acct0),
				    AccountStore.FORMAT_CURRENT);
	assertEquals("Accounts don't match after write/read cycle",
		     acct0, acct1);
    }

    /**
     * Test using {@link Account#readAccount(DataInput, int)} to read an
     * account with a history of one update into another existing
     * account object.
     *<p>
     * Constructs two new, different accounts, and applies updates to
     * each of them.  Then writes out one account, and reads it back
     * into the other.  Asserts that the second account is now equal to
     * the first.
     *
     * @throws IOException Any exception is passed up as a test failure.
     */
    @Test
    public void testReadExistingUpdatedAccount() throws IOException {
	Account acct0 = new Account(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	acct0.update(
		DESCRIPTION[1], URL[1], USERNAME[1], PASSWORD[1]);
	Account acct1 = new Account(
		DESCRIPTION[1], URL[1], USERNAME[1], PASSWORD[1]);
	acct1.update(
		DESCRIPTION[0], URL[0], USERNAME[0], PASSWORD[0]);
	acct1.readAccount(createAccountDataInput(acct0),
			  AccountStore.FORMAT_CURRENT);
	assertEquals("Accounts don't match after write/read cycle",
		     acct0, acct1);
    }
}
