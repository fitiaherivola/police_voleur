package com.policevoleur.algo;

import java.util.List;

/**
 * Description d'un jeu a deux joueurs pour {@link Minimax}.
 * Les valeurs sont toujours donnees du point de vue du joueur MAX.
 */
public interface GameModel<S> {

    /** Vrai si l'etat est terminal (feuille de l'arbre de jeu). */
    boolean isTerminal(S state);

    /** Valeur heuristique de l'etat, du point de vue de MAX. */
    double evaluate(S state);

    /** Etats atteignables en un coup depuis state. */
    List<S> successors(S state);
}
