package com.policevoleur.game;

import java.util.Arrays;

/** Etat immuable d'une partie : positions, tour, victoire. */
public final class GameState {

    public static final int POLICE_COUNT = 3;
    public static final int CENTER = 0;

    private final int thief;
    private final int[] police;
    private final Role turn;
    private final boolean thiefLeftCenter;
    private final Winner winner;
    private final int moves;

    public GameState(int thief, int[] police, Role turn, boolean thiefLeftCenter, Winner winner, int moves) {
        if (police == null || police.length != POLICE_COUNT) {
            throw new IllegalArgumentException("Il faut exactement " + POLICE_COUNT + " policiers");
        }
        this.thief = thief;
        this.police = police.clone();
        this.turn = turn;
        this.thiefLeftCenter = thiefLeftCenter;
        this.winner = winner;
        this.moves = moves;
    }

    public int thief() {
        return thief;
    }

    public int policeAt(int index) {
        return police[index];
    }

    public int[] policePositions() {
        return police.clone();
    }

    public boolean isPoliceAt(int vertex) {
        for (int p : police) {
            if (p == vertex) {
                return true;
            }
        }
        return false;
    }

    public Role turn() {
        return turn;
    }

    /** Vrai des que le voleur a quitte le centre au moins une fois. */
    public boolean thiefLeftCenter() {
        return thiefLeftCenter;
    }

    public Winner winner() {
        return winner;
    }

    public boolean isOver() {
        return winner != Winner.NONE;
    }

    /** Nombre de coups joues (un coup = le deplacement d'un camp). */
    public int moves() {
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GameState other)) {
            return false;
        }
        return thief == other.thief
                && Arrays.equals(police, other.police)
                && turn == other.turn
                && thiefLeftCenter == other.thiefLeftCenter
                && winner == other.winner
                && moves == other.moves;
    }

    @Override
    public int hashCode() {
        int h = Arrays.hashCode(police);
        h = 31 * h + thief;
        h = 31 * h + turn.hashCode();
        h = 31 * h + (thiefLeftCenter ? 1 : 0);
        h = 31 * h + winner.hashCode();
        h = 31 * h + moves;
        return h;
    }

    @Override
    public String toString() {
        return "GameState{thief=" + thief + ", police=" + Arrays.toString(police) + ", turn=" + turn
                + ", left=" + thiefLeftCenter + ", winner=" + winner + ", moves=" + moves + "}";
    }
}
