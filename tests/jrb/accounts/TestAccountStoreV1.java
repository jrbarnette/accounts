/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;

/**
 * Tests to confirm reading original V1 format files works as expected.
 * The test reads an account store from a file created by the original
 * V1 file save code, and compares the resulting
 * <code>AccountStore</code> object against the expected contents.
 */
public class TestAccountStoreV1 extends AccountStoreSupport {
    /**
     * Name (file path) of the resource containing our sample V1 test
     * data.  Note that the content of this file is fixed, and mustn't
     * be changed.
     */
    private static final String TEST_RESOURCE = "test-data/v1-sample.accts";

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

    public TestAccountStoreV1() {
	super(TEST_DATA, PASSWORD);
    }

    /**
     * Test that we can read the given V1 file sample. Read the accounts
     * from the sample file, and assert that the resulting object
     * matches <code>TEST_DATA</code>.
     *
     * @throws IOException The test should fail because of an unexpected
     *     error reading or opening the sample file.
     * @throws GeneralSecurityException The test should fail because of
     *     an unexpected error decrypting the sample file.
     */
    @Test
    public void testSampleFile()
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createFromResource(TEST_RESOURCE);
	validateContent(accounts);
    }
}
