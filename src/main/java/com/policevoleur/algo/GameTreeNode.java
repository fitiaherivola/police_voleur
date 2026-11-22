package com.policevoleur.algo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Arbre de jeu explicite, comme dans les diapositives du cours (racine A, feuilles avec valeurs).
 * Une feuille porte sa valeur ; un noeud interne porte ses fils.
 */
public final class GameTreeNode {

    private final double value;
    private final List<GameTreeNode> children;

    private GameTreeNode(double value, List<GameTreeNode> children) {
        this.value = value;
        this.children = children;
    }

    public static GameTreeNode leaf(double value) {
        return new GameTreeNode(value, Collections.emptyList());
    }

    public static GameTreeNode node(GameTreeNode... children) {
        return new GameTreeNode(0, List.of(children));
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public double value() {
        return value;
    }

    public List<GameTreeNode> children() {
        return children;
    }

    /** Modele Minimax qui lit l'arbre tel quel. */
    public static GameModel<GameTreeNode> model() {
        return new GameModel<>() {
            @Override
            public boolean isTerminal(GameTreeNode state) {
                return state.isLeaf();
            }

            @Override
            public double evaluate(GameTreeNode state) {
                return state.value;
            }

            @Override
            public List<GameTreeNode> successors(GameTreeNode state) {
                return state.children;
            }
        };
    }

    @Override
    public String toString() {
        return isLeaf() ? Double.toString(value) : "node" + Arrays.toString(children.toArray());
    }
}
