package com.policevoleur.ai;

import com.policevoleur.algo.Bfs;
import com.policevoleur.algo.GameModel;
import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;
import com.policevoleur.game.Winner;
import com.policevoleur.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modele Minimax du jeu : MAX = les policiers, MIN = le voleur.
 * L'heuristique utilise le BFS (distances et plus court chemin) sur le graphe du plateau.
 */
public final class PoliceThiefModel implements GameModel<GameState> {

    static final double WIN = 10_000;

    private final GameEngine engine;

    public PoliceThiefModel(GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public boolean isTerminal(GameState state) {
        return state.isOver();
    }

    @Override
    public List<GameState> successors(GameState state) {
        List<GameState> result = new ArrayList<>();
        if (state.isOver()) {
            return result;
        }
        if (state.turn() == Role.THIEF) {
            for (int to : engine.thiefMoves(state)) {
                result.add(engine.applyThiefMove(state, to));
            }
        } else {
            for (int[] dest : engine.policeMoves(state)) {
                result.add(engine.applyPoliceMove(state, dest));
            }
        }
        return result;
    }

    /** Valeur du point de vue des policiers : grande = bon pour les policiers. */
    @Override
    public double evaluate(GameState state) {
        if (state.winner() == Winner.POLICE) {
            return WIN - state.moves();
        }
        if (state.winner() == Winner.THIEF) {
            return -WIN + state.moves();
        }
        Graph g = engine.graph();
        int[] fromThief = Bfs.distances(g, state.thief());
        double score = 0;
        // policiers proches du voleur
        for (int i = 0; i < GameState.POLICE_COUNT; i++) {
            score -= 3.0 * fromThief[state.policeAt(i)];
        }
        // peu de cases libres autour du voleur
        int free = 0;
        for (int v : g.neighbors(state.thief())) {
            if (!state.isPoliceAt(v)) {
                free++;
            }
        }
        score -= 12.0 * free;
        // voleur sorti : plus il est proche du centre (en contournant les policiers), mieux c'est pour lui
        if (state.thiefLeftCenter()) {
            Set<Integer> blocked = new HashSet<>();
            for (int p : state.policePositions()) {
                blocked.add(p);
            }
            List<Integer> path = Bfs.shortestPath(g, state.thief(), GameState.CENTER, blocked);
            score += path.isEmpty() ? 60 : 8.0 * (path.size() - 1);
        }
        return score;
    }
}
