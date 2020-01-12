/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;

public class TestAccountStoreV0 extends AccountStoreSupport {
    private static final AccountTestData[] TEST_DATA = {
	new AccountTestData("xxx desc", "http://c.com", "u0", "p3"),
	new AccountTestData("yyy desc", "http://b.com", "u0", "p2"),
	new AccountTestData("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String PASSWORD = "password";

    private static final String TEST_RESOURCE = "test-data/v0-sample.accts";

    public TestAccountStoreV0() {
	super(TEST_DATA, PASSWORD);
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
	AccountStore accounts = createFromResource(TEST_RESOURCE);
	validateContent(createFromSaveRestore(accounts));
    }
}
