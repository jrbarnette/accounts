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
    private static final String[] CATEGORIES = {
	"Uppercase letters",
	"Lowercase letters",
	"Digits",
	"Special characters",
    };

    JRadioButton[] categoryRequired = new JRadioButton[CATEGORIES.length];

    JPasswordField password;

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

    private JComponent[] createConstraintsPanels() {
	JComponent[] panels = new JComponent[CATEGORIES.length];
	int index = 0;
	for (String category : CATEGORIES) {
	    JPanel aPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    aPanel.add(new JLabel(category));
	    categoryRequired[index] = new JRadioButton("Required");
	    aPanel.add(categoryRequired[index]);
	    panels[index] = aPanel;
	    index++;
	}
	return panels;
    }

    private JComponent createCategoriesPanel(JComponent[] categories) {
	JPanel aPanel = new JPanel(new GridLayout(0, 1));
	aPanel.setBorder(BorderFactory.createTitledBorder(
		"Character Requirements"));
	for (JComponent category : categories) {
	    aPanel.add(category);
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

    private JComponent createSpecialsPanel(JComponent[] categories) {
	JPanel specialsPanel = new JPanel(new BorderLayout());

	JPanel tPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	JComponent cPanel = categories[categories.length-1];
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

    private JComponent createPasswordPanel() {
	JPanel aPanel = new JPanel();
	aPanel.add(new JLabel("Password"));
	password = new JPasswordField();
	password.setColumns(PasswordGenerator.MAX_LENGTH);
	aPanel.add(password);
	aPanel.add(new JButton("Generate"));
	return aPanel;
    }

    public PasswordGenPanel() {
	super(new BorderLayout());
	fillSpecialCharacterDefaults();

	JComponent[] categoryConstraints = createConstraintsPanels();
	add(createLengthSelector(), BorderLayout.NORTH);
	add(createCategoriesPanel(categoryConstraints), BorderLayout.WEST);
	add(createSpecialCharSelector(), BorderLayout.EAST);
	add(createPasswordPanel(), BorderLayout.SOUTH);

	allowedList.setPreferredSize(allowedList.getMinimumSize());
	prohibitedList.setPreferredSize(allowedList.getMinimumSize());
    }
}
