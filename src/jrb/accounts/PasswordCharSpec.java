/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Random;
import java.util.Collection;

/**
 */
class PasswordCharSpec {
    private static final String UPPERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    private static String LETTERS = UPPERS + LOWERS;
    private static String ALPHANUMERIC = LETTERS + DIGITS;
    private static String ALL = ALPHANUMERIC + SPECIAL;

    char[] allowableChars;
    int multiplicity;

    PasswordCharSpec(char[] chars, int mult) {
	allowableChars = chars;
	multiplicity = mult;
    }

    PasswordCharSpec(String chars, int mult) {
	this(chars.toCharArray(), mult);
    }

    PasswordCharSpec(Collection<Character> chars, int mult) {
	allowableChars = new char[chars.size()];
	int index = 0;
	for (Character c : chars) {
	    allowableChars[index] = c.charValue();
	    index++;
	}
	multiplicity = mult;
    }

    static PasswordCharSpec createUppercaseCharSpec(int multiplicity) {
	return new PasswordCharSpec(UPPERS, multiplicity);
    }

    static PasswordCharSpec createLowercaseCharSpec(int multiplicity) {
	return new PasswordCharSpec(LOWERS, multiplicity);
    }

    static PasswordCharSpec createDigitCharSpec(int multiplicity) {
	return new PasswordCharSpec(DIGITS, multiplicity);
    }

    static PasswordCharSpec createLetterCharSpec(int multiplicity) {
	return new PasswordCharSpec(LETTERS, multiplicity);
    }

    static PasswordCharSpec createAnyCharSpec(int multiplicity) {
	return new PasswordCharSpec(ALL, multiplicity);
    }

    static PasswordCharSpec createAlphanumericCharSpec(int multiplicity) {
	return new PasswordCharSpec(ALPHANUMERIC, multiplicity);
    }

    public char nextCharacter(Random r) {
	return allowableChars[r.nextInt(allowableChars.length)];
    }
}
