/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import org.junit.Test;
import static org.junit.Assert.*;

public class AccountTest {
    private static final String[] DESCRIPTION = {
	"a", "b",
    };

    private static final String[] URL = {
	"http://a.com", "http://b.com",
    };

    private static final String[] USERNAME = {
	"username0", "username1",
    };

    private static final String[] PASSWORD = {
	"p0", "p1",
    };

    @Test
    public void testDescription() {
	Account acct = new Account(DESCRIPTION[0], URL[0],
				   USERNAME[0], PASSWORD[0]);
	assertEquals("Description doesn't match constructor",
		     DESCRIPTION[0], acct.getDescription());
    }

    @Test
    public void testUrl() {
	Account acct = new Account(DESCRIPTION[0], URL[0],
				   USERNAME[0], PASSWORD[0]);
	assertEquals("URL doesn't match constructor",
		     URL[0], acct.getUrl());
    }

    @Test
    public void testUsername() {
	Account acct = new Account(DESCRIPTION[0], URL[0],
				   USERNAME[0], PASSWORD[0]);
	assertEquals("User name doesn't match constructor",
		     USERNAME[0], acct.getUsername());
    }

    @Test
    public void testPassword() {
	Account acct = new Account(DESCRIPTION[0], URL[0],
				   USERNAME[0], PASSWORD[0]);
	assertEquals("Password doesn't match constructor",
		     PASSWORD[0], acct.getPassword());
    }

    @Test
    public void testEquality() {
	Account acct1 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	Account acct2 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	assertEquals("Identical Account objects not equal",
		     acct1, acct2);
    }

    @Test
    public void testUnequalDescription() {
	Account acct1 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	Account acct2 = new Account(DESCRIPTION[1], URL[0],
				    USERNAME[0], PASSWORD[0]);
	assertNotEquals("Different descriptions, but equal",
		        acct1, acct2);
    }

    @Test
    public void testUnequalUrl() {
	Account acct1 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	Account acct2 = new Account(DESCRIPTION[0], URL[1],
				    USERNAME[0], PASSWORD[0]);
	assertNotEquals("Different URLs, but equal",
		        acct1, acct2);
    }

    @Test
    public void testUnequalUsername() {
	Account acct1 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	Account acct2 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[1], PASSWORD[0]);
	assertNotEquals("Different user names, but equal",
		        acct1, acct2);
    }

    @Test
    public void testUnequalPassword() {
	Account acct1 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	Account acct2 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[1]);
	assertNotEquals("Different passwords, but equal",
		        acct1, acct2);
    }

    @Test
    public void testObjectEquals() {
	Account acct = new Account(DESCRIPTION[0], URL[0],
				   USERNAME[0], PASSWORD[0]);
	assertFalse("Account.equals() returned true for Object",
		    acct.equals(new Object()));
    }

    @Test
    public void testUpdate() {
	Account acct1 = new Account(DESCRIPTION[0], URL[0],
				    USERNAME[0], PASSWORD[0]);
	Account acct2 = new Account(DESCRIPTION[1], URL[1],
				    USERNAME[1], PASSWORD[1]);
	acct1.update(acct2.getDescription(), acct2.getUrl(),
		     acct2.getUsername(), acct2.getPassword());
	assertEquals("Accounts not equal after update()",
		     acct1, acct2);
    }
}
