package com.policevoleur.network;

import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;

/**
 * Ce que l'interface voit d'une partie, qu'elle soit hebergee, rejointe ou locale.
 * Les methodes submit* renvoient un message d'erreur, ou null si le coup est accepte.
 */
public interface GameSession {

    GameState state();

    /** Vrai si ce poste commande le camp indique. */
    boolean controls(Role role);

    String submitThiefMove(int to);

    String submitPoliceMove(int[] destinations);

    String requestRestart();

    void setListener(SessionListener listener);

    void close();
}
