/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

abstract class AccountStoreFactory {
    protected Account[] testData;

    protected AccountStoreFactory(Account[] testData) {
	this.testData = testData;
    }

    protected AccountStore createTestStore(int num, boolean inOrder) {
	AccountStore accounts = new AccountStore();
	for (int i = 0; i < num; i++) {
	    if (inOrder) {
		accounts.addAccount(testData[i]);
	    } else {
		accounts.addAccount(testData[num - i - 1]);
	    }
	}
	return accounts;
    }

    protected AccountStore createTestStore(int num) {
	return createTestStore(num, true);
    }

    protected AccountStore createTestStore() {
	return createTestStore(testData.length);
    }
}
