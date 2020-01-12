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

public class TestAccountStore extends AccountStoreSupport {
    private static final AccountTestData[] TEST_DATA = {
	new AccountTestData("xxx desc", "http://c.com", "u0", "p3"),
	new AccountTestData("yyy desc", "http://b.com", "u0", "p2"),
	new AccountTestData("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String PASSWORD = "password";

    public TestAccountStore() {
	super(TEST_DATA, PASSWORD);
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
	for (int num = 0; num <= testData.length; num++) {
	    AccountStore accounts = createTestStore(num);
	    assertEquals("AccountStore size() reports wrong value",
			 num, accounts.size());
	}
    }

    @Test
    public void testActualSize() {
	for (int num = 0; num <= testData.length; num++) {
	    AccountStore accounts = createTestStore(num);
	    String msg =
		"AccountStore iterates through the wrong number of elements";
	    assertEquals(msg, num, countElements(accounts));
	}
    }

    private void validateOrdering(boolean createInOrder) {
	for (int num = 0; num <= testData.length; num++) {
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
	AccountStore accounts = createTestStore();
	for (int num = 0; num < testData.length; num++) {
	    int idx = testData.length - num - 1;
	    accounts.deleteAccount(testData[idx].getAccount());
	    validateContent(accounts);
	    assertEquals("Account data size after delete",
			 idx, accounts.size());
	}
    }

    @Test
    public void testFileSaveRestore()
	    throws IOException, GeneralSecurityException {
	AccountStore origAccounts = createTestStore();
	AccountStore newAccounts = createFromSaveRestore(origAccounts);
	assertEquals("AccountStore changed by save/restore cycle",
		     origAccounts, newAccounts);
    }

    private void writeTestDataFile(String filename)
	    throws IOException, GeneralSecurityException {
	writeToFile(createTestStore(), new File(filename));
    }

    public static void main(String argv[]) {
	try {
	    (new TestAccountStore()).writeTestDataFile(argv[0]);
	} catch (IOException ioe) {
	    System.err.println("Failed to write: " + ioe.getMessage());
	} catch (GeneralSecurityException gse) {
	    System.err.println("Encryption failure: " + gse.getMessage());
	}
    }
}
