/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 */
public class TestMergeHistory {
    // N.B. not "static", because then getAccount() on the elements
    // would return a shared Account that was reused across tests.
    private final AccountTestData[] TEST_DATA = {
	new AccountTestData("xxx desc", "http://c.com", "u0", "p3"),
	new AccountTestData("yyy desc", "http://b.com", "u0", "p2"),
	new AccountTestData("zzz desc", "http://a.com", "u0", "p0"),
    };

    /**
     * Create an account from <code>TEST_DATA</code> that has been
     * updated a given number of times.  The account is constructed
     * from the first element of the data array, then <code>num</code>
     * updates are applied from the remaining elements.
     *
     * @param num The number of updates to apply out of TEST_DATA.
     * @return The newly created <code>Account</code> object.
     */
    private Account createAccountWithUpdates(int num) {
	Account acct = TEST_DATA[0].getAccount();
	for (int i = 1; i < num; i++) {
	    acct.update(TEST_DATA[i].getDescription(),
			TEST_DATA[i].getUrl(),
			TEST_DATA[i].getUsername(),
			TEST_DATA[i].getPassword());
	}
	return acct;
    }

    // merge cases:
	// 0 <- 1
	// A, B
	// 1 <- 0
	// B, A

	// 1, 2 <- 0
	// B, A, A
	// 0, 2 <- 1
	// A, B, A
	// 0, 1 <- 2
	// A, A, B

	// 0 <- 1, 2
	// A, B, B
	// 1 <- 0, 2
	// B, A, B
	// 2 <- 0, 1
	// B, B, A
}
