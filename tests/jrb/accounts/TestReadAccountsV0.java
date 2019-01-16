/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestReadAccountsV0 extends AccountStoreFactory {
    private static final Account[] TEST_ACCOUNTS = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String TEST_RESOURCE = "test-data/v0-sample.accts";

    public TestReadAccountsV0() {
	super(TEST_ACCOUNTS);
    }

    @Test
    public void testSampleFile() {
	validateResource(TEST_RESOURCE);
    }

    @Test
    public void testConversion() {
	File tFile = null;
	try {
	    tFile = File.createTempFile("test", ".acct");
	} catch (IOException ioe) {
	    fail("Unable to create temp file: " + ioe.toString());
	}

	AccountStore accounts = readFromResource(TEST_RESOURCE);

	try {
	    accounts.writeAccounts(new FileOutputStream(tFile));
	} catch (IOException ioe) {
	    fail("I/O Exception writing: " + ioe.toString());
	} catch (GeneralSecurityException gse) {
	    fail("Encryption failure writing: " + gse.toString());
	}

	validateFile(tFile);
    }
}
