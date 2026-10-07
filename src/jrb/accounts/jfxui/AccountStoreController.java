/*
 * Copyright 2026, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.jfxui;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;

/**
 */
public class AccountStoreController {
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
}
