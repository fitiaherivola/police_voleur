package com.policevoleur.ui;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameRules;
import com.policevoleur.graph.BoardLoader;
import com.policevoleur.network.ClientSession;
import com.policevoleur.network.HostSession;
import com.policevoleur.network.LocalSession;
import com.policevoleur.network.NetworkInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

/** Application JavaFX : relie le menu, les sessions (hote / client / local) et l'ecran de jeu. */
public final class PoliceVoleurApp extends Application {

    private Scene scene;
    private GameView currentGame;

    @Override
    public void start(Stage stage) {
        scene = new Scene(new MenuView(this), 1180, 800);
        URL css = getClass().getResource("/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Police et Voleur");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> closeGame());
        stage.show();
    }

    @Override
    public void stop() {
        closeGame();
    }

    private GameEngine newEngine() {
        return new GameEngine(BoardLoader.loadDefault(), GameRules.defaults());
    }

    /** PC 1 : ouvre le serveur. Renvoie un message d'erreur, ou null si tout va bien. */
    String host(int port) {
        GameEngine engine = newEngine();
        HostSession host = new HostSession(engine);
        try {
            host.start(port);
        } catch (IOException e) {
            return "Impossible d'ouvrir le port " + port + " : " + e.getMessage();
        }
        List<String> addresses = NetworkInfo.localAddresses();
        String network = addresses.isEmpty()
                ? "Adresse IP introuvable : vérifiez que ce PC est connecté au Wi-Fi. Port " + port + "."
                : "Donnez au joueur 2 l'adresse " + String.join(" ou ", addresses) + " (port " + port + "). "
                        + "En attente de sa connexion…";
        showGame(new GameView(host, engine, "Vous jouez : les 3 policiers (pions bleus)", network, this::showMenu));
        return null;
    }

    /** PC 2 : se connecte au serveur en arriere-plan ; onError est appele sur le thread JavaFX. */
    void join(String address, int port, Consumer<String> onError) {
        GameEngine engine = newEngine();
        ClientSession client = new ClientSession(engine);
        Thread t = new Thread(() -> {
            try {
                client.connect(address, port, 5000);
                Platform.runLater(() -> showGame(new GameView(client, engine,
                        "Vous jouez : le voleur (pion rouge)", "Connecté à " + address + ":" + port + ".",
                        this::showMenu)));
            } catch (IOException e) {
                client.close();
                Platform.runLater(() -> onError.accept("Connexion impossible : " + e.getMessage()
                        + ". Vérifiez l'adresse, le port et que le PC 1 a bien lancé l'hébergement."));
            }
        }, "client-connect");
        t.setDaemon(true);
        t.start();
    }

    void local() {
        GameEngine engine = newEngine();
        showGame(new GameView(new LocalSession(engine), engine,
                "Mode local : les deux camps se jouent sur cet écran", "", this::showMenu));
    }

    void showMenu() {
        closeGame();
        scene.setRoot(new MenuView(this));
    }

    private void showGame(GameView view) {
        closeGame();
        currentGame = view;
        scene.setRoot(view);
    }

    private void closeGame() {
        if (currentGame != null) {
            currentGame.close();
            currentGame = null;
        }
    }
}
