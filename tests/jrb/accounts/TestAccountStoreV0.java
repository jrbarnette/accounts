/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;

public class TestAccountStoreV0 extends AccountStoreSupport {
    private static final AccountTestData[] TEST_DATA = {};

    private static final String PASSWORD = "password";

    private static final String TEST_RESOURCE = "test-data/v0-sample.accts";

    public TestAccountStoreV0() {
	super(TEST_DATA, PASSWORD);
    }

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
