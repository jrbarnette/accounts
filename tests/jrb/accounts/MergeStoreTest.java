/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the {@link AccountStore#mergeAccounts()} method.
 */
public class MergeStoreTest extends AccountStoreSupport {
    private static final AccountTestData[] TEST_DATA = {
	new AccountTestData("xxx desc", "http://c.com", "u0", "p3"),
	new AccountTestData("yyy desc", "http://b.com", "u0", "p2"),
	new AccountTestData("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static final String PASSWORD = "password";

    public MergeStoreTest() {
	super(TEST_DATA, PASSWORD);
    }

    @Test
    public void testMergeEmpty() {
	AccountStore orig = new AccountStore();
	AccountStore merge = orig.clone();
	orig.mergeAccounts(merge);
	assertEquals(0, orig.size());
	assertEquals(0, merge.size());
    }

    @Test
    public void testMergeAdd() {
	AccountStore orig = createTestStore(1);
	AccountStore merge = orig.clone();
	extendTestStore(merge, 1);

	orig.mergeAccounts(merge);

	assertEquals(merge, orig);
	assertEquals(2, orig.size());
    }

    @Test
    public void testMergeNoChange() {
	AccountStore orig = createTestStore(1);
	AccountStore merge = orig.clone();
	extendTestStore(orig, 1);
	AccountStore origCopy = orig.clone();

	orig.mergeAccounts(merge);

	assertEquals(origCopy, orig);
	assertEquals(1, merge.size());
    }
}
