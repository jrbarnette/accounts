/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.swingui;

import jrb.accounts.PasswordCharSpec;
import jrb.accounts.PasswordGenerator;

import java.util.List;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.*;

/**
 */
public class PasswordGenPanel extends JPanel {
    private static final int UPPER = 0;
    private static final int LOWER = 1;
    private static final int DIGITS = 2;
    private static final int SPECIAL = 3;

    private static final String[] CATEGORIES = {
	"Uppercase letters",
	"Lowercase letters",
	"Digits",
	"Special characters",
    };

    JSpinner[] categorySpinners = new JSpinner[CATEGORIES.length];

    Vector<Character> allowed;
    Vector<Character> prohibited;

    JList<Character> allowedList;
    JList<Character> prohibitedList;

    JSpinner minLength;
    JSpinner maxLength;

    private void createAllowedSpecialCharacters() {
	String specials = PasswordCharSpec.SPECIAL;
	int len = specials.length();
	allowed = new Vector<Character>();
	for (int i = 0; i < len; i++) {
	    allowed.add(Character.valueOf(specials.charAt(i)));
	}
	prohibited = new Vector<Character>();
	prohibited.add(Character.valueOf(' '));
    }

    private JComponent createRequiredCategoriesSelector() {
	JPanel contentPanel = new JPanel(new GridLayout(0, 1));
	int index = 0;
	for (String category : CATEGORIES) {
	    JPanel aPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    aPanel.add(new JLabel(category));
	    categorySpinners[index] =
		    new JSpinner(new SpinnerNumberModel(1, 0, 3, 1));
	    aPanel.add(categorySpinners[index]);
	    contentPanel.add(aPanel);
	    index++;
	}

	JPanel tPanel = new JPanel(new BorderLayout());
	tPanel.add(contentPanel, BorderLayout.NORTH);
	tPanel.add(new JPanel(), BorderLayout.CENTER);
	return tPanel;
    }

    private JList<Character> addCharacterList(JPanel parent,
					      String title,
					      Vector<Character> chars) {
	JPanel tPanel = new JPanel(new BorderLayout());
	JPanel cPanel = new JPanel();
	cPanel.add(new JLabel(title));
	tPanel.add(cPanel, BorderLayout.NORTH);
	JList<Character> charList = new JList<Character>();
	charList.setListData(chars);
	charList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	charList.setVisibleRowCount(1);
	tPanel.add(charList, BorderLayout.SOUTH);

	parent.add(tPanel);

	return charList;
    }

    private JComponent createSpecialCharSelector() {
	createAllowedSpecialCharacters();
	JPanel selectorPanel = new JPanel(new GridLayout(0, 1));

	allowedList = addCharacterList(
		selectorPanel, "Allowed special characters", allowed);

	JPanel xferButtonPanel = new JPanel(new GridLayout(1, 0));
	xferButtonPanel.add(new JButton(new AbstractAction("allow") {
	    public void actionPerformed(ActionEvent e) {
		transferChars(prohibited,
			      allowed,
			      prohibitedList.getSelectedValuesList());
	    }
	}));

	xferButtonPanel.add(new JButton(new AbstractAction("prohibit") {
	    public void actionPerformed(ActionEvent e) {
		transferChars(allowed,
			      prohibited,
			      allowedList.getSelectedValuesList());
	    }
	}));
	JPanel wrapper = new JPanel();
	wrapper.add(xferButtonPanel);
	selectorPanel.add(wrapper);

	prohibitedList = addCharacterList(
		selectorPanel, "Prohibited special characters", prohibited);

	allowedList.setPreferredSize(allowedList.getMinimumSize());
	prohibitedList.setPreferredSize(allowedList.getMinimumSize());

	return selectorPanel;
    }

    private JComponent createLengthSelector() {
	JPanel aPanel = new JPanel();

	aPanel.add(new JLabel("Minimum length"));
	SpinnerModel minValueModel = new SpinnerNumberModel(
	    PasswordGenerator.DEFAULT_MIN_LENGTH,
	    PasswordGenerator.MIN_LENGTH,
	    PasswordGenerator.MAX_LENGTH,
	    1);
	minLength = new JSpinner(minValueModel);
	aPanel.add(minLength);

	aPanel.add(new JLabel("Maximum length"));
	SpinnerModel maxValueModel = new SpinnerNumberModel(
	    PasswordGenerator.DEFAULT_MAX_LENGTH,
	    PasswordGenerator.MIN_LENGTH,
	    PasswordGenerator.MAX_LENGTH,
	    1);
	maxLength = new JSpinner(maxValueModel);
	aPanel.add(maxLength);

	return aPanel;
    }

    public PasswordGenPanel() {
	super(new BorderLayout());
	setBorder(BorderFactory.createTitledBorder("Password Parameters"));

	add(createLengthSelector(), BorderLayout.NORTH);
	add(createRequiredCategoriesSelector(), BorderLayout.WEST);
	add(createSpecialCharSelector(), BorderLayout.EAST);
    }

    private static int find(Vector<Character> v, Character c) {
	int lo = 0;
	int hi = v.size();
	while (lo < hi) {
	    int mid = (lo + hi) / 2;
	    int compare = c.compareTo(v.get(mid));
	    if (compare == 0)
		return mid;
	    if (compare < 0)
		hi = mid;
	    else
		lo = mid + 1;
	}
	return lo;
    }

    private void transferChars(Vector<Character> src,
			       Vector<Character> dst,
			       List<Character> cList) {
	for (Character c : cList) {
	    src.remove(find(src, c));
	    dst.add(find(dst, c), c);
	}
	prohibitedList.setListData(prohibited);
	allowedList.setListData(allowed);
    }

    public char[] generatePassword() {
	int min = ((Integer) minLength.getValue()).intValue();
	int max = ((Integer) maxLength.getValue()).intValue();

	if (min > max) {
	    max = min;
	}

	PasswordGenerator gen = new PasswordGenerator(min, max);

	int categoryCount =
		((Integer) categorySpinners[UPPER].getValue()).intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		    PasswordCharSpec.createUppercaseCharSpec(categoryCount));
	}
	categoryCount =
		((Integer) categorySpinners[LOWER].getValue()).intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		    PasswordCharSpec.createLowercaseCharSpec(categoryCount));
	}
	categoryCount =
		((Integer) categorySpinners[DIGITS].getValue()).intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		    PasswordCharSpec.createDigitCharSpec(categoryCount));
	}
	categoryCount =
		((Integer) categorySpinners[SPECIAL].getValue()).intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(new PasswordCharSpec(allowed, categoryCount));
	}

	return gen.generatePassword();
    }
}
