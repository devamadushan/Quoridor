module com.dryt.quoridor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.dryt.quoridor to javafx.fxml;
    opens com.dryt.quoridor.controllers to javafx.fxml;
    opens com.dryt.quoridor.model to javafx.fxml;

    exports com.dryt.quoridor;
    exports com.dryt.quoridor.controllers;
    exports com.dryt.quoridor.gameLogic;
    exports com.dryt.quoridor.model;
}
