module accounts {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;

    exports jrb.accounts.jfxui;
    opens jrb.accounts.jfxui;
}
