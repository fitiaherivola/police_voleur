package com.policevoleur.game;

/**
 * Options de regles. La regle du nombre de policiers deplaces par tour est fixe :
 * un seul policier joue par tour, son coup est applique immediatement.
 */
public record GameRules() {

    public static GameRules defaults() {
        return new GameRules();
    }
}
