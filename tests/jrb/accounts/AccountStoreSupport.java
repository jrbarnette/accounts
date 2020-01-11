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

abstract class AccountStoreSupport {
    private static final String PASSWORD = "password";

    protected Account[] testData;

    protected AccountStoreSupport(Account[] testData) {
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

    private AccountStore createFromStream(InputStream in)
	    throws IOException, GeneralSecurityException {
	AccountStore accounts =
	    new AccountStore(in, PASSWORD.toCharArray());
	in.close();
	return accounts;
    }

    protected AccountStore createFromFile(File inFile)
	    throws IOException, GeneralSecurityException {
	return createFromStream(new FileInputStream(inFile));
    }

    protected AccountStore createFromResource(String resource)
	    throws IOException, GeneralSecurityException {
	InputStream in =
	    getClass().getClassLoader().getResourceAsStream(resource);
	if (in == null) {
	    fail("Failed to open resource: " + resource);
	}
	return createFromStream(in);
    }

    protected AccountStore writeToFile(File outFile)
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createTestStore();
	FileOutputStream out = new FileOutputStream(outFile);
	accounts.writeAccounts(out, PASSWORD.toCharArray());
	out.close();
	return accounts;
    }

    protected void validateContent(AccountStore accounts) {
	int i = 0;
	for (Account acct : accounts.allAccounts()) {
	    assertEquals("Account data wrong or out of order",
			 testData[i], acct);
	    i++;
	}
	assertEquals("Number of items seen by account iterator",
		     accounts.size(), i);
    }
}
