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
    Account[] testData;
    String filePassword;

    AccountStoreSupport(Account[] testData, String password) {
	this.testData = testData;
	this.filePassword = password;
    }

    AccountStore createTestStore(int num, boolean inOrder) {
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

    AccountStore createTestStore(int num) {
	return createTestStore(num, true);
    }

    AccountStore createTestStore() {
	return createTestStore(testData.length);
    }

    private AccountStore createFromStream(InputStream in)
	    throws IOException, GeneralSecurityException {
	AccountStore accounts =
	    new AccountStore(in, filePassword.toCharArray());
	in.close();
	return accounts;
    }

    AccountStore createFromFile(File inFile)
	    throws IOException, GeneralSecurityException {
	return createFromStream(new FileInputStream(inFile));
    }

    AccountStore createFromResource(String resource)
	    throws IOException, GeneralSecurityException {
	InputStream in =
	    getClass().getClassLoader().getResourceAsStream(resource);
	if (in == null) {
	    fail("Failed to open resource: " + resource);
	}
	return createFromStream(in);
    }

    AccountStore writeToFile(File outFile)
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createTestStore();
	FileOutputStream out = new FileOutputStream(outFile);
	accounts.writeAccounts(out, filePassword.toCharArray());
	out.close();
	return accounts;
    }

    void validateContent(AccountStore accounts) {
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
