/*
 * Copyright 2020, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.store;

import java.io.IOException;

class AccountFileFormatException extends IOException {
    public AccountFileFormatException(String msg) {
	super("File format error: " + msg);
    }
}
