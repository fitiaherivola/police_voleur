package com.policevoleur.algo;

import org.junit.jupiter.api.Test;

import static com.policevoleur.algo.GameTreeNode.leaf;
import static com.policevoleur.algo.GameTreeNode.node;
import static org.junit.jupiter.api.Assertions.*;

/** Verifie Minimax sur les deux arbres de jeu de la diapositive du cours. */
class MinimaxTest {

    private final GameModel<GameTreeNode> model = GameTreeNode.model();

    /** Arbre simple : A (max) ; B1, B2, B3 (min) ; feuilles 12 10 3 / 5 8 10 / 11 2 12. */
    private GameTreeNode simpleTree() {
        return node(
                node(leaf(12), leaf(10), leaf(3)),
                node(leaf(5), leaf(8), leaf(10)),
                node(leaf(11), leaf(2), leaf(12)));
    }

    /** Arbre complique : A max, B C min, D E F G max, feuilles -1 4 2 6 -3 -5 0 7. */
    private GameTreeNode complexTree() {
        return node(
                node(node(leaf(-1), leaf(4)), node(leaf(2), leaf(6))),
                node(node(leaf(-3), leaf(-5)), node(leaf(0), leaf(7))));
    }

    @Test
    void simpleTreeValueIsFive() {
        GameTreeNode a = simpleTree();
        assertEquals(3.0, Minimax.minimax(model, a.children().get(0), 1, false), 0.0);
        assertEquals(5.0, Minimax.minimax(model, a.children().get(1), 1, false), 0.0);
        assertEquals(2.0, Minimax.minimax(model, a.children().get(2), 1, false), 0.0);
        assertEquals(5.0, Minimax.minimax(model, a, 2, true), 0.0);
        assertEquals(1, Minimax.bestMoveIndex(model, a, 2, true));
    }

    @Test
    void complexTreeValueIsFour() {
        GameTreeNode a = complexTree();
        assertEquals(4.0, Minimax.minimax(model, a, 3, true), 0.0);
        assertEquals(0, Minimax.bestMoveIndex(model, a, 3, true));
    }

    @Test
    void minPlayerPicksTheSmallestBranch() {
        GameTreeNode a = simpleTree();
        // MIN a la racine : les fils B1, B2, B3 sont alors joues par MAX (12, 10, 12) ; MIN prend 10, donc B2
        assertEquals(1, Minimax.bestMoveIndex(model, a, 2, false));
    }

    @Test
    void depthLimitUsesTheHeuristicOnInternalNodes() {
        GameTreeNode a = complexTree();
        // profondeur 0 : evaluation directe du noeud, une valeur d'un noeud interne vaut 0 dans ce modele
        assertEquals(0.0, Minimax.minimax(model, a, 0, true), 0.0);
    }

    @Test
    void leafReturnsItsOwnValue() {
        assertEquals(7.0, Minimax.minimax(model, leaf(7), 5, true), 0.0);
        assertEquals(-1, Minimax.bestMoveIndex(model, leaf(7), 5, true));
    }
}
