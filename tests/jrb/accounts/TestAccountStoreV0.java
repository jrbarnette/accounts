/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;

/**
 * Tests to confirm reading V0 format files works as expected.
 * N.B. Support for the V0 file format has been removed, so the tests
 * are designed to detect appropriate failure.
 */
public class TestAccountStoreV0 extends AccountStoreSupport {
    private static final AccountTestData[] TEST_DATA = {};

    private static final String PASSWORD = "password";

    private static final String TEST_RESOURCE = "test-data/v0-sample.accts";

    public TestAccountStoreV0() {
	super(TEST_DATA, PASSWORD);
    }

    /**
     * Test reading the sample V0 file. Reading a V0 file is expected to
     * raise an exception, so the test fail if the read operation
     * succeeds, and succeed if the expected exception is raised.
     */
    @Test
    public void testSampleFile()
	    throws IOException, GeneralSecurityException {
	try {
	    AccountStore accounts = createFromResource(TEST_RESOURCE);
	    org.junit.Assert.fail("Reading V0 format file succeeded");
	} catch (AccountFileFormatException fe) {
	    // This is what we expect.
	}
    }
}
