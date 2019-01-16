/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import org.junit.Test;

public class TestReadAccountsV1 extends AccountStoreFactory {
    private static final Account[] TEST_ACCOUNTS = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String TEST_RESOURCE = "test-data/v1-sample.accts";

    public TestReadAccountsV1() {
	super(TEST_ACCOUNTS);
    }

    @Test
    public void testSampleFile() {
	validateResource(TEST_RESOURCE);
    }
}
