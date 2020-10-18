/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	    accounts.createAccount(
		    testData[index].getDescription(),
		    testData[index].getUrl(),
		    testData[index].getUsername(),
		    testData[index].getPassword());
	}
	return accounts;
    }

    void extendTestStore(AccountStore accounts, int num) {
	int numAccounts = accounts.size();
	for (int i = 0; i < num; i++) {
	    int index = i + numAccounts;
	    accounts.createAccount(
		    testData[index].getDescription(),
		    testData[index].getUrl(),
		    testData[index].getUsername(),
		    testData[index].getPassword());
	}
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

    AccountStore createFromSaveRestore(AccountStore accounts)
	    throws IOException, GeneralSecurityException {
	ByteArrayOutputStream output = new ByteArrayOutputStream();
	accounts.writeAccounts(output, filePassword.toCharArray());
	ByteArrayInputStream input =
		new ByteArrayInputStream(output.toByteArray());
	return createFromStream(input);
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
