package com.policevoleur.game;

/**
 * Options de regles. Les valeurs par defaut s'appuient sur ce que le joueur a precise ;
 * les deux options ci-dessous sont des points que le sujet ne tranche pas, ils sont donc
 * reglables ici en un seul endroit.
 *
 * @param policeMustAllMove  true : chacun des 3 policiers doit bouger a chaque tour.
 *                           false (defaut) : un policier peut rester sur place, mais au moins un doit bouger.
 * @param allowSharedVertex  true : deux policiers peuvent partager un sommet. false (defaut) : interdit.
 */
public record GameRules(boolean policeMustAllMove, boolean allowSharedVertex) {

    public static GameRules defaults() {
        return new GameRules(false, false);
    }
}
