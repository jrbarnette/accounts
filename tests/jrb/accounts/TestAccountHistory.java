/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for <code>Account</code> methods relating to examining
 * account update histories.
 */
public class TestAccountHistory {
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

    /**
     * Test the {@link Account#getUpdateData} method.
     */
    @Test
    public void testGetUpdateData() {
	for (int count = 1; count < TEST_DATA.length; count++) {
	    Account acct = createAccountWithUpdates(count);
	    for (int i = 0; i < count; i++) {
		AccountData update = acct.getUpdateData(i);
		assertTrue("Update record doesn't match its data",
			   TEST_DATA[count - i - 1].matches(update));
	    }
	}
    }

    /**
     * Test the {@link Account#getUpdateCount} method.
     */
    @Test
    public void testGetUpdateCount() {
	for (int count = 1; count < TEST_DATA.length; count++) {
	    Account acct = createAccountWithUpdates(count);
	    assertEquals("Account's update count doesn't match",
			 count, acct.getUpdateCount());
	}
    }
}
