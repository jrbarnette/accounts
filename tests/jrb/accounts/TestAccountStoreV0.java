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

public class TestAccountStoreV0 extends AccountStoreSupport {
    private static final Account[] TEST_ACCOUNTS = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String TEST_RESOURCE = "test-data/v0-sample.accts";

    public TestAccountStoreV0() {
	super(TEST_ACCOUNTS);
    }

    @Test
    public void testSampleFile()
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createFromResource(TEST_RESOURCE);
	validateContent(accounts);
    }

    @Test
    public void testConversion()
	    throws IOException, GeneralSecurityException {
	File tFile = File.createTempFile("test", ".acct");
	AccountStore accounts = createFromResource(TEST_RESOURCE);
	accounts.writeAccounts(new FileOutputStream(tFile));
	validateContent(createFromFile(tFile));
    }
}
