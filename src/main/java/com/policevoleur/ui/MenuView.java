package com.policevoleur.ui;

import com.policevoleur.network.Protocol;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Ecran d'accueil : heberger (PC 1, policiers), rejoindre (PC 2, voleur) ou jouer en local. */
public final class MenuView extends StackPane {

    public MenuView(PoliceVoleurApp app) {
        getStyleClass().add("menu-root");

        Label title = new Label("Police et Voleur");
        title.getStyleClass().add("menu-title");
        Label subtitle = new Label("3 policiers contre 1 voleur, deux ordinateurs sur le même Wi-Fi");
        subtitle.getStyleClass().add("menu-subtitle");

        Label status = new Label();
        status.getStyleClass().add("status-label");
        status.setWrapText(true);

        // --- PC 1 : hote
        Label hostTitle = new Label("PC 1 : héberger la partie (joueur Policiers)");
        hostTitle.getStyleClass().add("section-title");
        TextField hostPort = new TextField(Integer.toString(Protocol.DEFAULT_PORT));
        hostPort.setPromptText("Port");
        Button hostButton = new Button("Héberger et attendre le joueur 2");
        hostButton.getStyleClass().add("primary");
        hostButton.setMaxWidth(Double.MAX_VALUE);
        hostButton.setOnAction(e -> {
            Integer port = parsePort(hostPort.getText());
            if (port == null) {
                status.setText("Port invalide (entre 1 et 65535).");
                return;
            }
            String error = app.host(port);
            if (error != null) {
                status.setText(error);
            }
        });

        // --- PC 2 : client
        Label joinTitle = new Label("PC 2 : rejoindre la partie (joueur Voleur)");
        joinTitle.getStyleClass().add("section-title");
        TextField joinAddress = new TextField();
        joinAddress.setPromptText("Adresse IP du PC 1, par exemple 192.168.1.20");
        TextField joinPort = new TextField(Integer.toString(Protocol.DEFAULT_PORT));
        joinPort.setPromptText("Port");
        Button joinButton = new Button("Rejoindre");
        joinButton.getStyleClass().add("primary");
        joinButton.setMaxWidth(Double.MAX_VALUE);
        joinButton.setOnAction(e -> {
            Integer port = parsePort(joinPort.getText());
            String address = joinAddress.getText().trim();
            if (address.isEmpty()) {
                status.setText("Saisissez l'adresse IP du PC 1.");
                return;
            }
            if (port == null) {
                status.setText("Port invalide (entre 1 et 65535).");
                return;
            }
            status.setText("Connexion en cours…");
            joinButton.setDisable(true);
            app.join(address, port, message -> {
                status.setText(message);
                joinButton.setDisable(false);
            });
        });

        // --- local
        Label localTitle = new Label("Test sans réseau");
        localTitle.getStyleClass().add("section-title");
        Button localButton = new Button("Jouer en local (deux joueurs, même écran)");
        localButton.setMaxWidth(Double.MAX_VALUE);
        localButton.setOnAction(e -> app.local());

        VBox card = new VBox(10, title, subtitle,
                hostTitle, hostPort, hostButton,
                joinTitle, joinAddress, joinPort, joinButton,
                localTitle, localButton, status);
        card.getStyleClass().add("menu-card");
        card.setMaxWidth(520);
        card.setPadding(new Insets(28));
        card.setAlignment(Pos.TOP_LEFT);
        getChildren().add(card);
    }

    private static Integer parsePort(String text) {
        try {
            int port = Integer.parseInt(text.trim());
            return port >= 1 && port <= 65535 ? port : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
