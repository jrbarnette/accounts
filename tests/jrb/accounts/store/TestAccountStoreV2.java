/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.store;

import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;

/**
 * Tests to confirm reading original V2 format files works as expected.
 * The test reads an account store from a file created by the original
 * V2 file save code, and compares the resulting
 * <code>AccountStore</code> object against the expected contents.
 */
public class TestAccountStoreV2 extends AccountStoreSupport {
    /**
     * Name (file path) of the resource containing our sample V2 test
     * data.  Note that the content of this file is fixed, and mustn't
     * be changed.
     */
    private static final String TEST_RESOURCE = "test-data/v2-sample.accts";

    /**
     * Array of account data representing the content of our sample data
     * file.  Note that this array represents the content of the file,
     * not a parameter that can be changed or shared.
     */
    private static final AccountTestData[] TEST_DATA = {
	new AccountTestData("xxx desc", "http://c.com", "u0", "p3"),
	new AccountTestData("yyy desc", "http://b.com", "u0", "p2"),
	new AccountTestData("zzz desc", "http://a.com", "u0", "p0"),
    };

    /**
     * Password used to encrypt the sample data file.
     */
    private static final String PASSWORD = "password";

    public TestAccountStoreV2() {
	super(TEST_DATA, PASSWORD);
    }

    @Test
    public void testSampleFile()
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createFromResource(TEST_RESOURCE);
	validateContent(accounts);
    }
}
