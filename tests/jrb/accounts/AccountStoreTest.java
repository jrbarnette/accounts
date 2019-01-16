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

public class AccountStoreTest extends AccountStoreFactory {
    private static final Account[] TEST_ACCOUNTS = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    public AccountStoreTest() {
	super(TEST_ACCOUNTS);
    }

    private void testContent(AccountStore accounts) {
	int i = 0;
	for (Account acct : accounts.allAccounts()) {
	    assertEquals("Account data wrong or out of order",
			 TEST_ACCOUNTS[i], acct);
	    i++;
	}
    }

    @Test
    public void testFileIO()
	    throws GeneralSecurityException {
	File tFile = null;
	try {
	    tFile = File.createTempFile("test", ".acct");
	} catch (IOException ioe) {
	    fail("Unable to create temp file: " + ioe.toString());
	}
	AccountStore accounts = createTestStore();
	try {
	    FileOutputStream out = new FileOutputStream(tFile);
	    accounts.writeAccounts(out, "password".toCharArray());
	    out.close();
	} catch (IOException ioe) {
	    fail("I/O Exception writing: " + ioe.toString());
	} catch (GeneralSecurityException gce) {
	    fail("Security Exception writing: " + gce.toString());
	}
	try {
	    FileInputStream in = new FileInputStream(tFile);
	    accounts = new AccountStore(in, "password".toCharArray());
	    in.close();
	} catch (IOException ioe) {
	    fail("I/O Exception reading: " + ioe.toString());
	// } catch (GeneralSecurityException gce) {
	//     fail("Security Exception reading: " + gce.toString());
	}
	System.out.println("got here");
	testContent(accounts);
    }
}
