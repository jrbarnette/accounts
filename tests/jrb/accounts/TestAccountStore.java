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

import java.security.GeneralSecurityException;

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

    private void validateOrdering(boolean createInOrder) {
	for (int num = 0; num <= TEST_ACCOUNTS.length; num++) {
	    validateContent(createTestStore(num, createInOrder));
	}
    }

    @Test
    public void testAddInOrder() {
	validateOrdering(true);
    }

    @Test
    public void testAddOutOfOrder() {
	validateOrdering(false);
    }

    @Test
    public void testDelete() {
	AccountStore accounts = createTestStore(TEST_ACCOUNTS.length);
	for (int num = 0; num < TEST_ACCOUNTS.length; num++) {
	    int idx = TEST_ACCOUNTS.length - num - 1;
	    accounts.deleteAccount(TEST_ACCOUNTS[idx]);
	    validateContent(accounts);
	    assertEquals("Account data size after delete",
			 idx, accounts.size());
	}
    }

    @Test
    public void testFileIO() {
	File tFile = null;
	try {
	    tFile = File.createTempFile("test", ".acct");
	} catch (IOException ioe) {
	    fail("Unable to create temp file: " + ioe.toString());
	}

	try {
	    writeToFile(tFile);
	} catch (IOException ioe) {
	    fail("I/O Exception writing: " + ioe.toString());
	} catch (GeneralSecurityException gse) {
	    fail("Encryption failure writing: " + gse.toString());
	}

	validateFile(tFile);
    }

    public static void main(String argv[]) {
	try {
	    (new TestAccountStore()).writeToFile(new File(argv[0]));
	} catch (IOException ioe) {
	    System.err.println("Failed to write: " + ioe.getMessage());
	} catch (GeneralSecurityException gse) {
	    System.err.println("Encryption failure: " + gse.getMessage());
	}
    }
}
