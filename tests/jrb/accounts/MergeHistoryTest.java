/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the {@link Account#mergeHistory} method.
 */
public class MergeHistoryTest {
    // N.B. not "static", because then getAccount() on the elements
    // would return a shared Account that was reused across tests.
    private final AccountTestData[] TEST_DATA = {
	new AccountTestData("xxx desc", "http://c.com", "u0", "p4"),
	new AccountTestData("xxx desc", "http://c.com", "u0", "p3"),
	new AccountTestData("yyy desc", "http://b.com", "u0", "p2"),
	new AccountTestData("zzz desc", "http://a.com", "u0", "p0"),
    };

    private void testMerge(boolean[] useTgt) {
	Account tgtAcct = TEST_DATA[0].getAccount();
	Account srcAcct = tgtAcct.clone();
	boolean prev = useTgt.length == 0 || useTgt[0];
	for (int i = 0; i < useTgt.length; i++) {
	    Account acct = null;
	    if (prev != useTgt[i]) {
		Date refTime = new Date();
		while (refTime.equals(new Date())) {
		    continue;
		}
	    }
	    prev = useTgt[i];
	    if (useTgt[i]) {
		acct = srcAcct;
	    } else {
		acct = tgtAcct;
	    }
	    acct.update(TEST_DATA[i+1].getDescription(),
			TEST_DATA[i+1].getUrl(),
			TEST_DATA[i+1].getUsername(),
			TEST_DATA[i+1].getPassword());
	}
	tgtAcct.mergeHistory(srcAcct);
	int count = tgtAcct.getUpdateCount();
	assertEquals("Number of history entries after merge",
		     useTgt.length + 1, count);
	for (int i = 0; i < count; i++) {
	    AccountData entry = tgtAcct.getUpdateData(i);
	    assertTrue(TEST_DATA[count - i - 1].matches(entry));
	}
    }

    @Test
    public void testMergeEmpty() {
	// target: A
	// source: A
	// result: no change
	testMerge(new boolean[] {});
    }

    @Test
    public void testAddFromMerge() {
	// target: A
	// source: A, B
	// result: copy of source
	testMerge(new boolean[] { false });
    }

    @Test
    public void testAddNothing() {
	// target: A, B
	// source: A
	// result: no change
	testMerge(new boolean[] { true });
    }

    @Test
    public void testMergeAddAtEnd() {
	// target: A, B
	// source: A, C
	// result: C is added to end
	testMerge(new boolean[] { true, false });
    }

    @Test
    public void testMergeAddInMiddle() {
	// target: A, C
	// source: A, B
	// result: B is inserted before C
	testMerge(new boolean[] { false, true });
    }

    @Test
    public void testMergeAddNothingToTwo() {
	// target: A, B, C
	// source: A
	// result: no change
	testMerge(new boolean[] { true, true });
    }

    @Test
    public void testMergeAddTwoAtEnd() {
	// target: A
	// source: A, B, C
	// result: B and C are added to end
	testMerge(new boolean[] { false, false });
    }

    @Test
    public void testMergeAddBeforeAndAfter() {
	// target: A, C
	// source: A, B, D
	// result: B is added before C; D is added after (at end)
	testMerge(new boolean[] { false, true, false });
    }

    @Test
    public void testMergeInsertTwo() {
	// target: A, D
	// source: A, B, C
	// result: B and C are inserted before D
	testMerge(new boolean[] { false, false, true });
    }
}
