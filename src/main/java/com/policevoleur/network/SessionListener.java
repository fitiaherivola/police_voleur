package com.policevoleur.network;

import com.policevoleur.game.GameState;

/**
 * Notifications d'une session. Les methodes sont appelees depuis un thread reseau :
 * l'interface graphique doit repasser sur son propre thread (Platform.runLater).
 */
public interface SessionListener {

    /** Nouvel etat de la partie (apres un coup ou un redemarrage). */
    default void onState(GameState state) {
    }

    /** Message d'information ou d'erreur a afficher. */
    default void onInfo(String message) {
    }

    /** L'autre joueur vient de se connecter. */
    default void onOpponentConnected() {
    }

    /** L'autre joueur est parti ou la connexion est perdue. */
    default void onOpponentLeft() {
    }
}
