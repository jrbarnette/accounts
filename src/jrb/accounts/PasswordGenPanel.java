/*
 * Copyright 2019, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts;

import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.*;

/**
 */
public class PasswordGenPanel extends JPanel {
    JRadioButton uppercaseAllowed;
    JRadioButton lowercaseAllowed;
    JRadioButton digitsAllowed;
    JRadioButton specialsAllowed;

    JRadioButton uppercaseRequired;
    JRadioButton lowercaseRequired;
    JRadioButton digitsRequired;
    JRadioButton specialsRequired;

    Vector<Character> allowed;
    Vector<Character> prohibited;

    JList<Character> allowedList;
    JList<Character> prohibitedList;

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

    private void transferChar(Vector<Character> src,
			      Vector<Character> dst,
			      Character c) {
	src.remove(find(src, c));
	dst.add(find(dst, c), c);
	prohibitedList.setListData(prohibited);
	allowedList.setListData(allowed);
    }

    private void fillSpecialCharacterDefaults() {
	String SPECIAL = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
	int len = SPECIAL.length();
	allowed = new Vector<Character>();
	for (int i = 0; i < len; i++) {
	    allowed.add(Character.valueOf(SPECIAL.charAt(i)));
	}
	prohibited = new Vector<Character>();
	prohibited.add(Character.valueOf(' '));
    }

    private JList<Character> addCharacterList(JPanel parent,
					      String title,
					      Vector<Character> chars) {
	JList<Character> charList = new JList<Character>();
	charList.setListData(chars);
	charList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	charList.setVisibleRowCount(1);
	JPanel aPanel = new JPanel();
	aPanel.setBorder(BorderFactory.createTitledBorder(title));
	aPanel.add(charList);
	parent.add(aPanel);
	return charList;
    }

    private JComponent createConstraintsPanel(String category) {
	JPanel aPanel = new JPanel(new GridLayout(0, 2));
	aPanel.setBorder(BorderFactory.createTitledBorder(category));
	aPanel.add(new JRadioButton("Allowed"));
	aPanel.add(new JRadioButton("Required"));
	return aPanel;
    }

    private JComponent createCategoriesPanel() {
	String[] CATEGORIES = {
	    "Uppercase letters",
	    "Lowercase letters",
	    "Digits",
	};

	JPanel aPanel = new JPanel(new GridLayout(0, 1));
	for (String category : CATEGORIES) {
	    aPanel.add(createConstraintsPanel(category));
	}

	JPanel tPanel = new JPanel(new BorderLayout());
	tPanel.add(aPanel, BorderLayout.NORTH);
	tPanel.add(new JPanel(), BorderLayout.CENTER);
	return tPanel;
    }

    private JComponent createSpecialCharSelector() {
	JPanel selectorPanel = new JPanel(new GridLayout(0, 1));

	allowedList = addCharacterList(
		selectorPanel, "Allowed special characters", allowed);

	JPanel xferButtonPanel = new JPanel();
	xferButtonPanel.add(new JButton(new AbstractAction("allow") {
	    public void actionPerformed(ActionEvent e) {
		for (Character c : prohibitedList.getSelectedValuesList()) {
		    transferChar(prohibited, allowed, c);
		}
	    }
	}));

	xferButtonPanel.add(new JButton(new AbstractAction("prohibit") {
	    public void actionPerformed(ActionEvent e) {
		for (Character c : allowedList.getSelectedValuesList()) {
		    transferChar(allowed, prohibited, c);
		}
	    }
	}));
	selectorPanel.add(xferButtonPanel);

	prohibitedList = addCharacterList(
		selectorPanel, "Prohibited special characters", prohibited);

	return selectorPanel;
    }

    private JComponent createSpecialsPanel() {
	JPanel specialsPanel = new JPanel(new BorderLayout());
	// specialsPanel.setBorder(BorderFactory.createEmptyBorder());

	JPanel tPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	// tPanel.setBorder(BorderFactory.createEmptyBorder());
	JComponent cPanel = createConstraintsPanel("Special characters");
	cPanel.setPreferredSize(cPanel.getMinimumSize());
	tPanel.add(cPanel);

	specialsPanel.add(tPanel, BorderLayout.NORTH);
	specialsPanel.add(createSpecialCharSelector(),
			  BorderLayout.CENTER);
	return specialsPanel;
    }

    private JComponent createLengthSelector() {
	JPanel aPanel = new JPanel();
	aPanel.setBorder(BorderFactory.createTitledBorder("Length"));

	aPanel.add(new JLabel("Minimum"));
	SpinnerNumberModel minValueModel = new SpinnerNumberModel(
	    PasswordGenerator.DEFAULT_MIN_LENGTH,
	    PasswordGenerator.MIN_LENGTH,
	    PasswordGenerator.MAX_LENGTH,
	    1);
	JSpinner minLength = new JSpinner(minValueModel);
	aPanel.add(minLength);

	aPanel.add(new JLabel("Maximum"));
	SpinnerNumberModel maxValueModel = new SpinnerNumberModel(
	    PasswordGenerator.DEFAULT_MAX_LENGTH,
	    PasswordGenerator.MIN_LENGTH,
	    PasswordGenerator.MAX_LENGTH,
	    1);
	JSpinner maxLength = new JSpinner(maxValueModel);
	aPanel.add(maxLength);

	return aPanel;
    }

    public PasswordGenPanel() {
	super(new BorderLayout());
	fillSpecialCharacterDefaults();

	add(createLengthSelector(), BorderLayout.NORTH);
	add(createCategoriesPanel(), BorderLayout.WEST);
	add(createSpecialsPanel(), BorderLayout.EAST);

	allowedList.setPreferredSize(allowedList.getMinimumSize());
	prohibitedList.setPreferredSize(allowedList.getMinimumSize());
    }
}
