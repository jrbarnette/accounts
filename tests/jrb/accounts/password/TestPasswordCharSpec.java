/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.password;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class TestPasswordCharSpec {
    private static final int ITER = 1000;

    private void validateSpec(PasswordCharSpec spec,
			      Set<Character> allowed, int multiplicity) {
	Random r = new Random();
	for (int i = 0; i < ITER; i++) {
	    char c = spec.nextCharacter(r);
	    assertTrue("Character '" + c + "' out of range.",
		       allowed.contains(Character.valueOf(c)));
	}
	assertEquals("Wrong multiplicity", multiplicity, spec.multiplicity);
    }

    private static void addCharRange(Set<Character> set,
				     char first, char last) {
	for (char c = first; c <= last; c++) {
	    set.add(Character.valueOf(c));
	}
    }

    @Test
    public void testUppercaseCharSpec() {
	Set<Character> allowed = new HashSet<Character>();
	addCharRange(allowed, 'A', 'Z');
	validateSpec(PasswordCharSpec.createUppercaseCharSpec(1),
		     allowed, 1);
    }

    @Test
    public void testLowercaseCharSpec() {
	Set<Character> allowed = new HashSet<Character>();
	addCharRange(allowed, 'a', 'z');
	validateSpec(PasswordCharSpec.createLowercaseCharSpec(1),
		     allowed, 1);
    }

    @Test
    public void testDigitCharSpec() {
	Set<Character> allowed = new HashSet<Character>();
	addCharRange(allowed, '0', '9');
	validateSpec(PasswordCharSpec.createDigitCharSpec(1),
		     allowed, 1);
    }

    @Test
    public void testAlphanumericCharSpec() {
	Set<Character> allowed = new HashSet<Character>();
	addCharRange(allowed, 'A', 'Z');
	addCharRange(allowed, 'a', 'z');
	addCharRange(allowed, '0', '9');
	validateSpec(PasswordCharSpec.createAlphanumericCharSpec(1),
		     allowed, 1);
    }

    @Test
    public void testSingletonCharSpec() {
	Set<Character> allowed = new HashSet<Character>();
	allowed.add('A');
	int[] multiplicities = { 1, 10 };
	for (int m : multiplicities) {
	    validateSpec(new PasswordCharSpec(allowed, m),
			 allowed, m);
	}
    }
}
