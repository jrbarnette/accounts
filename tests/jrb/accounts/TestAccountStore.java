/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestAccountStore extends AccountStoreFactory {
    private static final Account[] TEST_ACCOUNTS = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    public TestAccountStore() {
	super(TEST_ACCOUNTS);
    }

    private static int countElements(AccountStore accounts) {
	int n = 0;
	for (Account acct : accounts.allAccounts()) {
	    n++;
	}
	return n;
    }

    @Test
    public void testReportedSize() {
	for (int num = 0; num <= TEST_ACCOUNTS.length; num++) {
	    AccountStore accounts = createTestStore(num);
	    assertEquals("AccountStore size() reports wrong value",
			 num, accounts.size());
	}
    }

    @Test
    public void testActualSize() {
	for (int num = 0; num <= TEST_ACCOUNTS.length; num++) {
	    AccountStore accounts = createTestStore(num);
	    String msg =
		"AccountStore iterates through the wrong number of elements";
	    assertEquals(msg, num, countElements(accounts));
	}
    }

    private void testContent(AccountStore accounts) {
	int i = 0;
	for (Account acct : accounts.allAccounts()) {
	    assertEquals("Account data wrong or out of order",
			 TEST_ACCOUNTS[i], acct);
	    i++;
	}
    }

    private void testOrdering(boolean createInOrder) {
	for (int num = 0; num <= TEST_ACCOUNTS.length; num++) {
	    testContent(createTestStore(num, createInOrder));
	}
    }

    @Test
    public void testAddInOrder() {
	testOrdering(true);
    }

    @Test
    public void testAddOutOfOrder() {
	testOrdering(false);
    }

    @Test
    public void testFileIO() {
	File tFile = null;
	try {
	    tFile = File.createTempFile("test", ".acct");
	} catch (IOException ioe) {
	    fail("Unable to create temp file: " + ioe.toString());
	}
	AccountStore accounts = createTestStore();
	try {
	    FileOutputStream out = new FileOutputStream(tFile);
	    accounts.writeAccounts(out);
	    out.close();
	} catch (IOException ioe) {
	    fail("I/O Exception writing: " + ioe.toString());
	}
	try {
	    FileInputStream in = new FileInputStream(tFile);
	    accounts = new AccountStore(in);
	    in.close();
	} catch (IOException ioe) {
	    fail("I/O Exception reading: " + ioe.toString());
	}
	testContent(accounts);
    }
}
