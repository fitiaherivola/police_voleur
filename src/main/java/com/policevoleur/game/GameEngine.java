package com.policevoleur.game;

import com.policevoleur.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Regles du jeu : validation des deplacements, application des coups, conditions de victoire.
 *
 * <ul>
 *   <li>Le voleur joue en premier ; les tours alternent voleur / policiers.</li>
 *   <li>Le voleur doit se deplacer a chaque tour, vers un voisin non occupe par un policier.</li>
 *   <li>Un seul policier se deplace par tour, puis le tour passe au voleur.</li>
 *   <li>Un policier ne peut pas aller sur la case du voleur : la victoire se fait par blocage.</li>
 *   <li>Le voleur gagne s'il revient au sommet 0 apres en etre sorti.</li>
 *   <li>Les policiers gagnent quand, apres leur coup, le voleur n'a plus aucun deplacement.</li>
 * </ul>
 */
public final class GameEngine {

    public static final int[] START_POLICE = {1, 2, 3};

    private final Graph graph;
    private final GameRules rules;

    public GameEngine(Graph graph) {
        this(graph, GameRules.defaults());
    }

    public GameEngine(Graph graph, GameRules rules) {
        this.graph = graph;
        this.rules = rules;
    }

    public Graph graph() {
        return graph;
    }

    public GameRules rules() {
        return rules;
    }

    /** Depart : voleur au centre, policiers en 1, 2, 3, au voleur de jouer. Jamais une capture. */
    public GameState initialState() {
        return new GameState(GameState.CENTER, START_POLICE, Role.THIEF, false, Winner.NONE, 0);
    }

    // ------------------------------------------------------------------ voleur

    /** Sommets ou le voleur peut aller (voisins non occupes par un policier). */
    public List<Integer> thiefMoves(GameState s) {
        List<Integer> result = new ArrayList<>();
        for (int v : graph.neighbors(s.thief())) {
            if (!s.isPoliceAt(v)) {
                result.add(v);
            }
        }
        return result;
    }

    /** Message d'erreur si le coup est interdit, null s'il est valide. */
    public String thiefMoveError(GameState s, int to) {
        if (s.isOver()) {
            return "La partie est terminee.";
        }
        if (s.turn() != Role.THIEF) {
            return "Ce n'est pas le tour du voleur.";
        }
        if (to < 0 || to >= graph.vertexCount()) {
            return "Sommet inexistant.";
        }
        if (!graph.areAdjacent(s.thief(), to)) {
            return "Le voleur ne peut aller que sur un sommet directement relie au sien.";
        }
        if (s.isPoliceAt(to)) {
            return "Ce sommet est occupe par un policier.";
        }
        return null;
    }

    public GameState applyThiefMove(GameState s, int to) {
        String error = thiefMoveError(s, to);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        boolean left = s.thiefLeftCenter() || (s.thief() == GameState.CENTER && to != GameState.CENTER);
        boolean escaped = to == GameState.CENTER && s.thiefLeftCenter();
        return new GameState(to, s.policePositions(), Role.POLICE, left,
                escaped ? Winner.THIEF : Winner.NONE, s.moves() + 1);
    }

    // --------------------------------------------------------------- policiers

    /** Sommets ou le policier numero index peut aller (voisins libres, sauf la case du voleur). */
    public List<Integer> policeTargets(GameState s, int index) {
        List<Integer> result = new ArrayList<>();
        int from = s.policeAt(index);
        for (int v : graph.neighbors(from)) {
            if (v == s.thief() || s.isPoliceAt(v)) {
                continue;
            }
            result.add(v);
        }
        return result;
    }

    /** Message d'erreur si le coup de police est interdit, null s'il est valide. */
    public String policeMoveError(GameState s, int[] dest) {
        if (s.isOver()) {
            return "La partie est terminee.";
        }
        if (s.turn() != Role.POLICE) {
            return "Ce n'est pas le tour des policiers.";
        }
        if (dest == null || dest.length != GameState.POLICE_COUNT) {
            return "Il faut une destination pour chacun des " + GameState.POLICE_COUNT + " policiers.";
        }
        int mover = -1;
        for (int i = 0; i < dest.length; i++) {
            int from = s.policeAt(i);
            int to = dest[i];
            if (to < 0 || to >= graph.vertexCount()) {
                return "Sommet inexistant.";
            }
            if (to != from) {
                if (mover >= 0) {
                    return "Un seul policier peut se deplacer par tour.";
                }
                mover = i;
            }
        }
        if (mover < 0) {
            return "Un policier doit se deplacer.";
        }
        int from = s.policeAt(mover);
        int to = dest[mover];
        if (!graph.areAdjacent(from, to)) {
            return "Le policier " + (mover + 1) + " ne peut aller que sur un sommet directement relie au sien.";
        }
        if (to == s.thief()) {
            return "Un policier ne peut pas aller sur la case du voleur.";
        }
        if (s.isPoliceAt(to)) {
            return "Ce sommet est occupe par un policier.";
        }
        return null;
    }

    /** Tous les coups de police valides : un seul policier se deplace (utilise par l'IA). */
    public List<int[]> policeMoves(GameState s) {
        List<int[]> result = new ArrayList<>();
        if (s.isOver() || s.turn() != Role.POLICE) {
            return result;
        }
        for (int i = 0; i < GameState.POLICE_COUNT; i++) {
            for (int to : policeTargets(s, i)) {
                int[] dest = s.policePositions();
                dest[i] = to;
                if (policeMoveError(s, dest) == null) {
                    result.add(dest);
                }
            }
        }
        return result;
    }

    public GameState applyPoliceMove(GameState s, int[] dest) {
        String error = policeMoveError(s, dest);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        GameState next = new GameState(s.thief(), dest, Role.THIEF, s.thiefLeftCenter(), Winner.NONE, s.moves() + 1);
        if (thiefMoves(next).isEmpty()) {
            return new GameState(next.thief(), dest, Role.THIEF, next.thiefLeftCenter(), Winner.POLICE, next.moves());
        }
        return next;
    }
}
