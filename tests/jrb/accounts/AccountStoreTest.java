/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import org.junit.Test;
import static org.junit.Assert.*;

public class AccountStoreTest {
    private static final Account[] testAccounts = {
	new Account("xxx desc", "http://c.com", "u0", "p3"),
	new Account("yyy desc", "http://b.com", "u0", "p2"),
	new Account("zzz desc", "http://a.com", "u0", "p0"),
    };

    private static AccountStore createTestStore(int num, boolean inOrder) {
	AccountStore accounts = new AccountStore();
	for (int i = 0; i < num; i++) {
	    if (inOrder) {
		accounts.addAccount(testAccounts[i]);
	    } else {
		accounts.addAccount(testAccounts[num - i - 1]);
	    }
	}
	return accounts;
    }

    private static int countElements(AccountStore accounts) {
	int n = 0;
	for (Account acct : accounts.allAccounts()) {
	    n++;
	}
	return n;
    }

    @Test
    public void testEntryCounts() {
	for (int num = 0; num <= testAccounts.length; num++) {
	    AccountStore accounts = createTestStore(num, true);
	    assertEquals("Wrong number of elements",
			 num, countElements(accounts));
	}
    }

    private void testOrdering(boolean createInOrder) {
	for (int num = 0; num <= testAccounts.length; num++) {
	    AccountStore accounts = createTestStore(num, createInOrder);
	    int i = 0;
	    for (Account acct : accounts.allAccounts()) {
		assertEquals("Account data wrong or out of order",
			     testAccounts[i], acct);
		i++;
	    }
	}
    }

    @Test
    public void testAddInOrder() {
	testOrdering(true);
    }

    @Test
    public void testAddOutOfOrder() {
	testOrdering(false);
    }
}
