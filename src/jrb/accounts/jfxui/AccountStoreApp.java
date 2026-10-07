/*
 * Copyright 2026, by J. Richard Barnette.  All Rights Reserved.
 */

package jrb.accounts.jfxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 */
public class AccountStoreApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
	Parent root = FXMLLoader.load(getClass()
                           .getResource("/resources/Accounts.fxml"));
        Scene scene = new Scene(root);
	stage.setTitle("Accounts Manager");
	stage.setScene(scene);
	stage.show();
    }

    public static void main(String[] args) {
	launch();
    }
}
