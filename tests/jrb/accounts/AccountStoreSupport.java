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
    AccountTestData[] testData;
    String filePassword;

    AccountStoreSupport(AccountTestData[] testData, String password) {
	this.testData = testData;
	this.filePassword = password;
    }

    AccountStore createTestStore(int num, boolean inOrder) {
	AccountStore accounts = new AccountStore();
	for (int i = 0; i < num; i++) {
	    int index;
	    if (inOrder) {
		index = i;
	    } else {
		index = num - i - 1;
	    }
	    accounts.addAccount(testData[index].getAccount());
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

    AccountStore createFromResource(String resource)
	    throws IOException, GeneralSecurityException {
	InputStream in =
	    getClass().getClassLoader().getResourceAsStream(resource);
	if (in == null) {
	    fail("Failed to open resource: " + resource);
	}
	return createFromStream(in);
    }

    void writeToFile(AccountStore accounts, File outFile)
	    throws IOException, GeneralSecurityException {
	FileOutputStream out = new FileOutputStream(outFile);
	accounts.writeAccounts(out, filePassword.toCharArray());
	out.close();
    }

    AccountStore createFromSaveRestore(AccountStore accounts)
	    throws IOException, GeneralSecurityException {
	File tempFile = File.createTempFile("test", ".acct");
	tempFile.deleteOnExit();
	writeToFile(accounts, tempFile);
	return createFromStream(new FileInputStream(tempFile));
    }

    void validateContent(AccountStore accounts) {
	int i = 0;
	for (Account acct : accounts) {
	    assertTrue("Account data wrong or out of order",
		       testData[i].matches(acct));
	    i++;
	}
	assertEquals("Number of items seen by account iterator",
		     accounts.size(), i);
    }
}
