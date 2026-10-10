/*
 * Copyright 2026, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.jfxui;

import jrb.accounts.password.PasswordCharSpec;
import jrb.accounts.password.PasswordGenerator;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;

/**
 */
public class AccountStoreController implements Initializable {
    @FXML
    private ListView<Character> allowedSpecial;

    @FXML
    private ListView<Character> prohibitedSpecial;

    @FXML
    private Spinner<Integer> minPassword;

    @FXML
    private Spinner<Integer> maxPassword;

    @FXML
    private Spinner<Integer> numUppercase;

    @FXML
    private Spinner<Integer> numLowercase;

    @FXML
    private Spinner<Integer> numDigit;

    @FXML
    private Spinner<Integer> numSpecial;

    private void initializePasswordGeneration() {
	minPassword.getEditor().setPrefColumnCount(3);
	minPassword.getValueFactory().setValue(
	    PasswordGenerator.DEFAULT_MIN_LENGTH);
	maxPassword.getEditor().setPrefColumnCount(3);
	maxPassword.getValueFactory().setValue(
	    PasswordGenerator.DEFAULT_MAX_LENGTH);

	String specials = PasswordCharSpec.SPECIAL;
	ObservableList<Character> allowedList
	    = FXCollections.observableArrayList();
	for (int i = 0; i < specials.length(); i++) {
	    allowedList.add(
		Character.valueOf(specials.charAt(i)));
	}
	allowedSpecial.setItems(allowedList);
	allowedSpecial.getSelectionModel()
	    .setSelectionMode(SelectionMode.MULTIPLE);

	ObservableList<Character> prohibitedList
	    = FXCollections.observableArrayList();
	prohibitedList.add(Character.valueOf(' '));
	prohibitedSpecial.setItems(prohibitedList);
	prohibitedSpecial.getSelectionModel()
	    .setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void allowSelectedCharacters(ActionEvent actionEvent) {
	ObservableList<Character> toBeAllowed
	    = prohibitedSpecial.getSelectionModel().getSelectedItems();
	// N.B. removing from the list alters the selection, so must
	// "add" before "remove".
	allowedSpecial.getItems().addAll(toBeAllowed);
	prohibitedSpecial.getItems().removeAll(toBeAllowed);
    }

    @FXML
    private void prohibitSelectedCharacters(ActionEvent actionEvent) {
	ObservableList<Character> toBeProhibited
	    = allowedSpecial.getSelectionModel().getSelectedItems();
	// N.B. removing from the list alters the selection, so must
	// "add" before "remove".
	prohibitedSpecial.getItems().addAll(toBeProhibited);
	allowedSpecial.getItems().removeAll(toBeProhibited);
    }

    @FXML
    private void generatePassword(ActionEvent actionEvent) {
	int min = minPassword.getValue().intValue();
	int max = maxPassword.getValue().intValue();

	if (min > max) {
	    max = min;
	}

	PasswordGenerator gen = new PasswordGenerator(min, max);

	int categoryCount = numUppercase.getValue().intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		    PasswordCharSpec.createUppercaseCharSpec(categoryCount));
	}
	categoryCount = numLowercase.getValue().intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		    PasswordCharSpec.createLowercaseCharSpec(categoryCount));
	}
	categoryCount = numDigit.getValue().intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		    PasswordCharSpec.createDigitCharSpec(categoryCount));
	}
	categoryCount = numSpecial.getValue().intValue();
	if (categoryCount > 0) {
	    gen.addCharSpec(
		new PasswordCharSpec(allowedSpecial.getItems(),
				     categoryCount));
	}

	// gen.generatePassword();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
	initializePasswordGeneration();
    }
}
