/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

abstract class AccountStoreFactory {
    private static final String PASSWORD = "password";

    protected Account[] testData;

    protected AccountStoreFactory(Account[] testData) {
	this.testData = testData;
    }

    protected AccountStore createTestStore(int num, boolean inOrder) {
	AccountStore accounts = new AccountStore();
	for (int i = 0; i < num; i++) {
	    if (inOrder) {
		accounts.addAccount(testData[i]);
	    } else {
		accounts.addAccount(testData[num - i - 1]);
	    }
	}
	return accounts;
    }

    protected AccountStore createTestStore(int num) {
	return createTestStore(num, true);
    }

    protected AccountStore createTestStore() {
	return createTestStore(testData.length);
    }

    protected void writeToFile(File outFile)
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createTestStore();
	FileOutputStream out = new FileOutputStream(outFile);
	accounts.writeAccounts(out, PASSWORD.toCharArray());
	out.close();
    }

    protected void validateContent(AccountStore accounts) {
	int i = 0;
	for (Account acct : accounts.allAccounts()) {
	    assertEquals("Account data wrong or out of order",
			 testData[i], acct);
	    i++;
	}
    }

    private void validateStream(InputStream in)
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = new AccountStore(in, PASSWORD.toCharArray());
	in.close();
	validateContent(accounts);
    }

    protected void validateFile(File inFile) {
	FileInputStream in = null;
	try {
	    in = new FileInputStream(inFile);
	} catch (IOException ioe) {
	    fail("Failed to open accounts file: " + ioe.getMessage());
	}
	try {
	    validateStream(in);
	} catch (IOException ioe) {
	    fail("Failed to read accounts from file: " + ioe.getMessage());
	} catch (GeneralSecurityException gse) {
	    fail("Decryption failure: " + gse.getMessage());
	}
    }

    protected void validateResource(String resource) {
	InputStream in =
	    getClass().getClassLoader().getResourceAsStream(resource);
	if (in == null) {
	    fail("Failed to open resource");
	}
	try {
	    validateStream(in);
	} catch (IOException ioe) {
	    fail("Failed to read accounts from resource: " + ioe.getMessage());
	} catch (GeneralSecurityException gse) {
	    fail("Decryption failure: " + gse.getMessage());
	}
    }
}
