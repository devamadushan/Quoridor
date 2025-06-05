module com.dryt.quoridor {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.prefs;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.dryt.quoridor.app to javafx.fxml;
    exports com.dryt.quoridor.app;
    opens com.dryt.quoridor.controller to javafx.fxml;
    exports com.dryt.quoridor.controller;
}