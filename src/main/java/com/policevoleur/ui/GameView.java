package com.policevoleur.ui;

import com.policevoleur.ai.GameAI;
import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;
import com.policevoleur.network.GameSession;
import com.policevoleur.network.HostSession;
import com.policevoleur.network.LocalSession;
import com.policevoleur.network.SessionListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ecran de jeu : plateau a gauche, panneau d'information et boutons a droite.
 *
 * Voleur : cliquer sur le pion rouge, puis sur une case verte.
 * Policiers : cliquer sur un pion bleu, puis sur une case verte (pour chacun des 3 pions),
 * puis "Valider le tour". Recliquer sur un pion deja deplace annule son deplacement.
 */
public final class GameView extends BorderPane {

    private final GameSession session;
    private final GameEngine engine;
    private final GameAI ai;
    private final boolean singleScreen;
    private final boolean isHost;

    private final BoardCanvas board;
    private final Label turnLabel = new Label();
    private final Label bannerLabel = new Label();
    private final Label messageLabel = new Label();
    private final Label networkLabel = new Label();
    private final Button validateButton = new Button("Valider le tour des policiers");
    private final Button cancelButton = new Button("Annuler mes déplacements");
    private final Button hintButton = new Button("Suggestion (Minimax)");
    private final Button restartButton = new Button("Recommencer");
    private final Button menuButton = new Button("Retour au menu");
    private final CheckBox labelsBox = new CheckBox("Afficher les numéros des sommets");

    private GameState state;
    private int[] staged;
    private int selectedPolice = -1;
    private boolean selectedThief;
    private int hintVertex = -1;
    private boolean hintRunning;

    public GameView(GameSession session, GameEngine engine, String roleText, String networkText, Runnable onMenu) {
        this.session = session;
        this.engine = engine;
        this.ai = new GameAI(engine);
        this.singleScreen = session instanceof LocalSession;
        this.isHost = session instanceof HostSession;

        getStyleClass().add("game-root");

        board = new BoardCanvas(engine.graph());
        board.setClickListener(this::onVertexClicked);
        Pane boardPane = new Pane(board);
        boardPane.setMinSize(0, 0);
        boardPane.setPrefSize(720, 720);
        board.widthProperty().bind(boardPane.widthProperty());
        board.heightProperty().bind(boardPane.heightProperty());
        setCenter(boardPane);

        Label title = new Label("Police et Voleur");
        title.getStyleClass().add("side-title");
        Label roleLabel = new Label(roleText);
        roleLabel.getStyleClass().add("role-label");
        roleLabel.setWrapText(true);
        turnLabel.getStyleClass().add("turn-label");
        turnLabel.setWrapText(true);
        bannerLabel.getStyleClass().add("banner");
        bannerLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setWrapText(true);
        networkLabel.setText(networkText);
        networkLabel.getStyleClass().add("network-label");
        networkLabel.setWrapText(true);

        Label legend = new Label("Pions bleus 1, 2, 3 : policiers.  Pion rouge V : voleur.\n"
                + "Cercle orange pointillé : sommet 0, où le voleur doit revenir.\n"
                + "Cercles verts : déplacements possibles.");
        legend.getStyleClass().add("legend");
        legend.setWrapText(true);

        for (Button b : new Button[] {validateButton, cancelButton, hintButton, restartButton, menuButton}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        validateButton.getStyleClass().add("primary");
        validateButton.setOnAction(e -> submitPolice());
        cancelButton.setOnAction(e -> resetStaged());
        hintButton.setOnAction(e -> requestHint());
        restartButton.setOnAction(e -> {
            String error = session.requestRestart();
            if (error != null) {
                setMessage(error);
            }
        });
        menuButton.setOnAction(e -> onMenu.run());
        labelsBox.setSelected(true);
        labelsBox.setOnAction(e -> board.setShowLabels(labelsBox.isSelected()));
        labelsBox.getStyleClass().add("labels-box");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox side = new VBox(12, title, roleLabel, turnLabel, bannerLabel, messageLabel, networkLabel,
                validateButton, cancelButton, hintButton, restartButton, labelsBox, spacer, legend, menuButton);
        side.getStyleClass().add("side-panel");
        side.setPadding(new Insets(20));
        side.setPrefWidth(330);
        side.setMinWidth(300);
        setRight(side);

        session.setListener(new SessionListener() {
            @Override
            public void onState(GameState s) {
                Platform.runLater(() -> applyState(s));
            }

            @Override
            public void onInfo(String message) {
                Platform.runLater(() -> setMessage(message));
            }

            @Override
            public void onOpponentConnected() {
                Platform.runLater(() -> {
                    networkLabel.setText("Adversaire connecté.");
                    setMessage("L'adversaire est connecté.");
                });
            }

            @Override
            public void onOpponentLeft() {
                Platform.runLater(() -> {
                    networkLabel.setText("Adversaire déconnecté.");
                    setMessage(isHost
                            ? "Le joueur 2 s'est déconnecté. La partie reprendra s'il se reconnecte."
                            : "Connexion au serveur perdue. Retournez au menu pour vous reconnecter.");
                });
            }
        });
        applyState(session.state());
    }

    /** Ferme la session (reseau) associee a cet ecran. */
    public void close() {
        session.close();
    }

    // ------------------------------------------------------------------ etat

    private void applyState(GameState s) {
        state = s;
        staged = s.policePositions();
        selectedPolice = -1;
        selectedThief = false;
        hintVertex = -1;
        if (s.isOver()) {
            setMessage(resultText(s));
        } else if (singleScreen && s.turn() == Role.POLICE) {
            setMessage("Tour des policiers : déplacez jusqu'aux 3 pions bleus, puis validez.");
        } else if (singleScreen) {
            setMessage("Au tour du voleur : cliquez sur le pion rouge, puis une case verte.");
        } else if (session.controls(s.turn())) {
            setMessage(s.turn() == Role.POLICE
                    ? "Tour des policiers : préparez vos déplacements, puis validez."
                    : "C'est à vous de jouer.");
        } else {
            setMessage("En attente de l'adversaire…");
        }
        refresh();
    }

    private void refresh() {
        int pawn = selectedThief ? BoardCanvas.THIEF_PAWN : selectedPolice;
        board.update(state, staged, pawn, currentTargets(), hintVertex);

        if (state.isOver()) {
            turnLabel.setText("Partie terminée");
            bannerLabel.setText(resultText(state));
        } else {
            String who = state.turn().label();
            if (!singleScreen) {
                who += session.controls(state.turn()) ? " (vous)" : " (adversaire)";
            }
            turnLabel.setText("Au tour : " + who);
            bannerLabel.setText("");
        }
        bannerLabel.setVisible(state.isOver());
        bannerLabel.setManaged(state.isOver());

        boolean myTurn = !state.isOver() && session.controls(state.turn());
        boolean policeTurn = myTurn && state.turn() == Role.POLICE;
        boolean policeControl = session.controls(Role.POLICE);
        validateButton.setVisible(policeControl);
        validateButton.setManaged(policeControl);
        cancelButton.setVisible(policeControl);
        cancelButton.setManaged(policeControl);
        validateButton.setDisable(!policeTurn);
        cancelButton.setDisable(!policeTurn);
        hintButton.setDisable(!myTurn || hintRunning);
    }

    private static String resultText(GameState s) {
        return switch (s.winner()) {
            case THIEF -> "Victoire du voleur : il est revenu au sommet 0 sans être capturé.";
            case POLICE -> "Victoire des policiers : le voleur est bloqué, il n'a plus aucun déplacement.";
            case NONE -> "";
        };
    }

    private Set<Integer> currentTargets() {
        if (state.isOver()) {
            return Set.of();
        }
        if (state.turn() == Role.THIEF && session.controls(Role.THIEF) && selectedThief) {
            return new LinkedHashSet<>(engine.thiefMoves(state));
        }
        if (state.turn() == Role.POLICE && session.controls(Role.POLICE) && selectedPolice >= 0) {
            return availablePoliceTargets(engine, state, staged, selectedPolice);
        }
        return Set.of();
    }

    static Set<Integer> availablePoliceTargets(GameEngine engine, GameState state, int[] staged, int index) {
        Set<Integer> targets = new LinkedHashSet<>(engine.policeTargets(state, index));
        for (int i = 0; i < staged.length; i++) {
            if (i != index) {
                targets.remove(staged[i]);
            }
        }
        return targets;
    }

    private void setMessage(String text) {
        messageLabel.setText(text);
    }

    // ------------------------------------------------------------------ souris

    private void onVertexClicked(int v) {
        if (state.isOver()) {
            return;
        }
        Role turn = state.turn();
        if (!session.controls(turn)) {
            setMessage("Ce n'est pas votre tour.");
            return;
        }
        if (turn == Role.THIEF) {
            clickThief(v);
        } else {
            clickPolice(v);
        }
    }

    private void clickThief(int v) {
        if (v == state.thief()) {
            selectedThief = !selectedThief;
            refresh();
            return;
        }
        if (selectedThief && engine.thiefMoves(state).contains(v)) {
            String error = session.submitThiefMove(v);
            selectedThief = false;
            if (error != null) {
                setMessage(error);
            }
            refresh();
            return;
        }
        selectedThief = false;
        setMessage("Cliquez sur le voleur (rouge), puis sur une case verte voisine.");
        refresh();
    }

    private void clickPolice(int v) {
        int pawn = -1;
        for (int i = 0; i < staged.length; i++) {
            if (staged[i] == v) {
                pawn = i;
                break;
            }
        }
        if (pawn >= 0) {
            if (staged[pawn] != state.policeAt(pawn)) {
                staged[pawn] = state.policeAt(pawn);
                setMessage("Déplacement du policier " + (pawn + 1) + " annulé.");
            } else if (selectedPolice == pawn) {
                selectedPolice = -1;
            } else {
                selectedPolice = pawn;
            }
            refresh();
            return;
        }
        boolean occupied = false;
        for (int i = 0; i < staged.length; i++) {
            if (i != selectedPolice && staged[i] == v) {
                occupied = true;
            }
        }
        if (selectedPolice >= 0 && !occupied && engine.policeTargets(state, selectedPolice).contains(v)) {
            staged[selectedPolice] = v;
            selectedPolice = -1;
            refresh();
            return;
        }
        selectedPolice = -1;
        setMessage("Cliquez sur un policier, puis sur une case verte voisine. Validez ensuite le tour.");
        refresh();
    }

    private void submitPolice() {
        String error = session.submitPoliceMove(staged.clone());
        if (error != null) {
            setMessage(error);
        } else {
            selectedPolice = -1;
        }
        refresh();
    }

    private void resetStaged() {
        staged = state.policePositions();
        selectedPolice = -1;
        hintVertex = -1;
        setMessage("Déplacements annulés.");
        refresh();
    }

    // ------------------------------------------------------------------ suggestion Minimax

    private void requestHint() {
        GameState snapshot = state;
        if (snapshot.isOver() || !session.controls(snapshot.turn()) || hintRunning) {
            return;
        }
        hintRunning = true;
        setMessage("Calcul de la suggestion (Minimax)…");
        refresh();
        Thread t = new Thread(() -> {
            int thiefMove = -1;
            int[] policeMove = null;
            if (snapshot.turn() == Role.THIEF) {
                thiefMove = ai.bestThiefMove(snapshot, GameAI.THIEF_DEPTH);
            } else {
                policeMove = ai.bestPoliceMove(snapshot, GameAI.POLICE_DEPTH);
            }
            int tm = thiefMove;
            int[] pm = policeMove;
            Platform.runLater(() -> showHint(snapshot, tm, pm));
        }, "minimax-hint");
        t.setDaemon(true);
        t.start();
    }

    private void showHint(GameState snapshot, int thiefMove, int[] policeMove) {
        hintRunning = false;
        if (!snapshot.equals(state)) {
            refresh();
            return;
        }
        if (thiefMove >= 0) {
            selectedThief = true;
            hintVertex = thiefMove;
            setMessage("Suggestion Minimax : allez au sommet " + thiefMove + " (entouré en jaune).");
        } else if (policeMove != null) {
            staged = policeMove.clone();
            selectedPolice = -1;
            StringBuilder sb = new StringBuilder("Suggestion Minimax : ");
            boolean first = true;
            for (int i = 0; i < policeMove.length; i++) {
                if (policeMove[i] != state.policeAt(i)) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append("policier ").append(i + 1).append(" vers le sommet ").append(policeMove[i]);
                    first = false;
                }
            }
            sb.append(". Validez le tour ou modifiez-le.");
            setMessage(sb.toString());
        } else {
            setMessage("Aucune suggestion disponible.");
        }
        refresh();
    }
}
