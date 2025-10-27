/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.password;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestPasswordGenerator {
    private static final int ITER = 1000;

    private static final int[] TEST_LENGTHS = { 1, 6, 16, 25 };

    @Test
    public void testLength() {
	for (int l : TEST_LENGTHS) {
	    PasswordGenerator gen = new PasswordGenerator(l, l);
	    for (int i = 0; i < ITER; i++) {
		assertEquals("Password length is wrong",
			     l, gen.generatePassword().length);
	    }
	}
    }

    @Test
    public void testSetLength() {
	PasswordGenerator gen = new PasswordGenerator();
	for (int l : TEST_LENGTHS) {
	    gen.setLength(l, l);
	    for (int i = 0; i < ITER; i++) {
		assertEquals("Password length is wrong",
			     l, gen.generatePassword().length);
	    }
	}
    }

    @Test
    public void testLengthRange() {
	for (int l : TEST_LENGTHS) {
	    PasswordGenerator gen = new PasswordGenerator(l, l + 1);
	    for (int i = 0; i < ITER; i++) {
		int actualLength = gen.generatePassword().length;
		assertTrue("Password length is out of range",
			   l <= actualLength && actualLength <= l + 1);
	    }
	}
    }

    @Test
    public void testSetLengthRange() {
	PasswordGenerator gen = new PasswordGenerator();
	for (int l : TEST_LENGTHS) {
	    gen.setLength(l, l + 1);
	    for (int i = 0; i < ITER; i++) {
		int actualLength = gen.generatePassword().length;
		assertTrue("Password length is out of range",
			   l <= actualLength && actualLength <= l + 1);
	    }
	}
    }

    @Test
    public void testSpecMultiplicity() {
	int mult = 2;
	PasswordGenerator gen = new PasswordGenerator(mult, mult);
	char[] testChar = { 'A' };
	gen.addCharSpec(testChar, mult);
	for (int i = 0; i < ITER; i++) {
	    for (char c : gen.generatePassword()) {
		assertEquals("Unexpected password character",
			     testChar[0], c);
	    }
	}
    }

    @Test
    public void testDefaultOnly() {
	int len = 2;
	PasswordGenerator gen = new PasswordGenerator(len, len);
	char[] testChar = { 'A' };
	gen.setDefaultCharSpec(testChar);
	for (int i = 0; i < ITER; i++) {
	    for (char c : gen.generatePassword()) {
		assertEquals("Unexpected password character",
			     testChar[0], c);
	    }
	}
    }

    @Test
    public void testDefaultAndCharSpec() {
	int mult = 1;
	int len = mult + 2;
	PasswordGenerator gen = new PasswordGenerator(len, len);
	char[] specChar = { 'A' };
	char[] defaultChar = { 'B' };
	gen.addCharSpec(specChar, mult);
	gen.setDefaultCharSpec(defaultChar);
	for (int i = 0; i < ITER; i++) {
	    int specCount = 0;
	    int defaultCount = 0;
	    for (char c : gen.generatePassword()) {
		if (c == specChar[0]) {
		    specCount++;
		} else if (c == defaultChar[0]) {
		    defaultCount++;
		} else {
		    fail("Unxpected character: " + c);
		}
	    }
	    assertEquals("Wrong number of specified characters",
			 mult, specCount);
	    assertEquals("Wrong number of default characters",
			 len - mult, defaultCount);
	}
    }
}
