/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.IOException;

import java.security.GeneralSecurityException;

import org.junit.Test;

public class TestAccountStoreV1 extends AccountStoreSupport {
    private static final Account[] TEST_ACCOUNTS = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String TEST_RESOURCE = "test-data/v1-sample.accts";

    public TestAccountStoreV1() {
	super(TEST_ACCOUNTS);
    }

    @Test
    public void testSampleFile()
	    throws IOException, GeneralSecurityException {
	AccountStore accounts = createFromResource(TEST_RESOURCE);
	validateContent(accounts);
    }
}
