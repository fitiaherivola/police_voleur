package com.policevoleur.ai;

import com.policevoleur.algo.Minimax;
import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;

import java.util.List;

/**
 * Suggestions de coups par Minimax. Le mode principal du jeu reste humain contre humain :
 * l'IA ne sert qu'a l'aide au jeu ("Suggestion").
 */
public final class GameAI {

    /** Profondeur (en demi-coups) pour conseiller les policiers (~125 coups possibles par tour). */
    public static final int POLICE_DEPTH = 3;
    /** Profondeur pour conseiller le voleur (au plus 4 coups possibles). */
    public static final int THIEF_DEPTH = 4;

    private final GameEngine engine;
    private final PoliceThiefModel model;

    public GameAI(GameEngine engine) {
        this.engine = engine;
        this.model = new PoliceThiefModel(engine);
    }

    /** Meilleur sommet pour le voleur, ou -1 s'il ne peut pas jouer. */
    public int bestThiefMove(GameState state, int depth) {
        if (state.isOver() || state.turn() != Role.THIEF) {
            return -1;
        }
        List<Integer> moves = engine.thiefMoves(state);
        if (moves.isEmpty()) {
            return -1;
        }
        // l'ordre de model.successors suit celui de engine.thiefMoves
        int index = Minimax.bestMoveIndex(model, state, depth, false);
        return index < 0 ? -1 : moves.get(index);
    }

    /** Meilleur tour pour les 3 policiers (destinations), ou null s'ils ne peuvent pas jouer. */
    public int[] bestPoliceMove(GameState state, int depth) {
        if (state.isOver() || state.turn() != Role.POLICE) {
            return null;
        }
        List<int[]> moves = engine.policeMoves(state);
        if (moves.isEmpty()) {
            return null;
        }
        // l'ordre de model.successors suit celui de engine.policeMoves
        int index = Minimax.bestMoveIndex(model, state, depth, true);
        return index < 0 ? null : moves.get(index);
    }
}
