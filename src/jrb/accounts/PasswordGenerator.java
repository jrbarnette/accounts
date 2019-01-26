/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 */
class PasswordGenerator {
    public static final int MIN_LENGTH = 6;
    public static final int MAX_LENGTH = 32;
    public static final int DEFAULT_MIN_LENGTH = 6;
    public static final int DEFAULT_MAX_LENGTH = 16;

    private static final PasswordCharSpec DEFAULT_CHAR_SPEC
	    = PasswordCharSpec.createAlphanumericCharSpec(0);

    private final Random random = new Random();

    private List<PasswordCharSpec> charSpecs
	    = new ArrayList<PasswordCharSpec>();
    private PasswordCharSpec defaultCharSpec;

    private int minLength;
    private int maxLength;

    public PasswordGenerator(PasswordCharSpec spec, int min, int max) {
	defaultCharSpec = spec;
	setLength(min, max);
    }

    public PasswordGenerator(PasswordCharSpec spec, int min) {
	defaultCharSpec = spec;
	setLength(min);
    }

    public PasswordGenerator(PasswordCharSpec spec) {
	this(spec, DEFAULT_MIN_LENGTH);
    }

    public PasswordGenerator(int min, int max) {
	this(DEFAULT_CHAR_SPEC, min, max);
    }

    public PasswordGenerator(int min) {
	this(DEFAULT_CHAR_SPEC, min);
    }

    public PasswordGenerator() {
	this(DEFAULT_MIN_LENGTH);
    }

    public void setLength(int min, int max) {
	minLength = min;
	maxLength = max;
    }

    public void setLength(int min) {
	setLength(min, min + DEFAULT_MAX_LENGTH - DEFAULT_MIN_LENGTH);
    }

    public void addCharSpec(char[] allowed, int multiplicity) {
	charSpecs.add(new PasswordCharSpec(allowed, multiplicity));
    }

    public void addCharSpec(Set<Character> allowed, int multiplicity) {
	charSpecs.add(new PasswordCharSpec(allowed, multiplicity));
    }

    public void setDefaultCharSpec(char[] allowed) {
	defaultCharSpec = new PasswordCharSpec(allowed, 0);
    }

    public void setDefaultCharSpec(Set<Character> allowed) {
	defaultCharSpec = new PasswordCharSpec(allowed, 0);
    }

    private int genPasswordLength() {
	int pwdLength = 0;
	for (PasswordCharSpec spec : charSpecs) {
	    pwdLength += spec.multiplicity;
	}
	int min = minLength;
	if (min < pwdLength) {
	    min = pwdLength;
	}
	int numLengths = 1;
	if (min < maxLength) {
	    numLengths += maxLength - min;
	}
	return min + random.nextInt(numLengths);
    }

    private void addCharToPassword(char[] password, int nChars,
				    PasswordCharSpec spec) {
	int idx = random.nextInt(nChars + 1);
	if (idx < nChars) {
	    password[nChars] = password[idx];
	}
	password[idx] = spec.nextCharacter(random);
    }

    public char[] generatePassword() {
	char[] password = new char[genPasswordLength()];
	int nChars = 0;
	// XXX This makes no attempt to enforce that all specs in
	// charSpecs are subsets of defaultCharSpec.
	for (PasswordCharSpec spec : charSpecs) {
	    for (int i = 0; i < spec.multiplicity; i++) {
		addCharToPassword(password, nChars, spec);
		nChars++;
	    }
	}
	while (nChars < password.length) {
	    addCharToPassword(password, nChars, defaultCharSpec);
	    nChars++;
	}
	return password;
    }
}
