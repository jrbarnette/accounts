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
    private static final String SPECIAL = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    private static final String[] CATEGORIES = {
	"Uppercase letters",
	"Lowercase letters",
	"Digits",
	"Special characters",
    };

    private static final String[] CONSTRAINTS = { "Allowed", "Required" };

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

    private int find(Vector<Character> v, Character c) {
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

    private void allowSpecialChar(Character c) {
	transferChar(prohibited, allowed, c);
    }

    private void prohibitSpecialChar(Character c) {
	transferChar(allowed, prohibited, c);
    }

    private JList addCharacterList(JPanel parent,
				   String title,
				   Vector<Character> chars) {
	JList charList = new JList<Character>();
	charList.setListData(chars);
	charList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	charList.setVisibleRowCount(1);
	JPanel aPanel = new JPanel();
	aPanel.setBorder(BorderFactory.createTitledBorder(title));
	aPanel.add(charList);
	parent.add(aPanel);
	return charList;
    }

    public PasswordGenPanel() {
	super(new BorderLayout());
	JPanel leftPanel = new JPanel(new GridLayout(0, 1));
	for (String category : CATEGORIES) {
	    JPanel bPanel = new JPanel(new GridLayout(0, 2));
	    bPanel.setBorder(BorderFactory.createTitledBorder(category));
	    bPanel.add(new JRadioButton(CONSTRAINTS[0]));
	    bPanel.add(new JRadioButton(CONSTRAINTS[1]));
	    leftPanel.add(bPanel);
	}
	add(leftPanel, BorderLayout.WEST);

	int len = SPECIAL.length();
	allowed = new Vector<Character>();
	for (int i = 0; i < len; i++) {
	    allowed.add(Character.valueOf(SPECIAL.charAt(i)));
	}
	prohibited = new Vector<Character>();
	prohibited.add(Character.valueOf(' '));

	JPanel rightPanel = new JPanel(new GridLayout(0, 1));

	allowedList = addCharacterList(
		rightPanel, "Allowed special characters", allowed);

	JPanel xferButtonPanel = new JPanel();

	xferButtonPanel.add(new JButton(new AbstractAction("+") {
	    public void actionPerformed(ActionEvent e) {
		for (Character c : prohibitedList.getSelectedValuesList()) {
		    allowSpecialChar(c);
		}
	    }
	}));

	xferButtonPanel.add(new JButton(new AbstractAction("-") {
	    public void actionPerformed(ActionEvent e) {
		for (Character c : allowedList.getSelectedValuesList()) {
		    prohibitSpecialChar(c);
		}
	    }
	}));
	rightPanel.add(xferButtonPanel);

	prohibitedList = addCharacterList(
		rightPanel, "Prohibited special characters", prohibited);

	add(rightPanel, BorderLayout.EAST);
    }
}
