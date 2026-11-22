package com.policevoleur.algo;

import java.util.List;

/**
 * Minimax selon le pseudo-code du cours :
 *
 * <pre>
 * Minimax(etat, profondeur, estMax)
 *   si profondeur = 0 ou etat terminal : retourner la valeur heuristique de etat
 *   si estMax : v = -inf ; pour chaque mouvement valide m : v = max(v, Minimax(m, profondeur-1, faux)) ; retourner v
 *   sinon     : v = +inf ; pour chaque mouvement valide m : v = min(v, Minimax(m, profondeur-1, vrai)) ; retourner v
 * </pre>
 */
public final class Minimax {

    private Minimax() {
    }

    public static <S> double minimax(GameModel<S> model, S state, int depth, boolean isMax) {
        if (depth == 0 || model.isTerminal(state)) {
            return model.evaluate(state);
        }
        List<S> moves = model.successors(state);
        if (moves.isEmpty()) {
            return model.evaluate(state);
        }
        if (isMax) {
            double v = Double.NEGATIVE_INFINITY;
            for (S m : moves) {
                v = Math.max(v, minimax(model, m, depth - 1, false));
            }
            return v;
        } else {
            double v = Double.POSITIVE_INFINITY;
            for (S m : moves) {
                v = Math.min(v, minimax(model, m, depth - 1, true));
            }
            return v;
        }
    }

    /**
     * Indice du meilleur successeur de state pour le joueur qui joue (MAX si isMax, sinon MIN).
     * A egalite, le premier successeur est retenu. Renvoie -1 s'il n'y a aucun coup.
     */
    public static <S> int bestMoveIndex(GameModel<S> model, S state, int depth, boolean isMax) {
        List<S> moves = model.successors(state);
        int best = -1;
        double bestValue = isMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (int i = 0; i < moves.size(); i++) {
            double v = minimax(model, moves.get(i), depth - 1, !isMax);
            if (best == -1 || (isMax ? v > bestValue : v < bestValue)) {
                best = i;
                bestValue = v;
            }
        }
        return best;
    }
}
