package com.policevoleur.network;

import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;
import com.policevoleur.game.Winner;

/**
 * Protocole texte (une ligne par message, en UTF-8) entre le serveur (PC 1, policiers)
 * et le client (PC 2, voleur).
 *
 * <pre>
 * Serveur -> client :  HELLO
 *                      STATE thief p1 p2 p3 tour(T|P) sorti(0|1) gagnant(N|P|T) coups
 *                      ERROR message
 * Client -> serveur :  MOVE_THIEF sommet
 *                      RESTART
 * </pre>
 *
 * Le serveur est l'autorite : il verifie chaque coup et renvoie l'etat complet a chaque changement.
 */
public final class Protocol {

    public static final int DEFAULT_PORT = 5555;

    public static final String HELLO = "HELLO";
    public static final String STATE = "STATE";
    public static final String ERROR = "ERROR";
    public static final String MOVE_THIEF = "MOVE_THIEF";
    public static final String RESTART = "RESTART";

    private Protocol() {
    }

    public static String encodeState(GameState s) {
        return STATE + " " + s.thief() + " " + s.policeAt(0) + " " + s.policeAt(1) + " " + s.policeAt(2)
                + " " + (s.turn() == Role.THIEF ? "T" : "P")
                + " " + (s.thiefLeftCenter() ? "1" : "0")
                + " " + switch (s.winner()) {
                    case NONE -> "N";
                    case POLICE -> "P";
                    case THIEF -> "T";
                }
                + " " + s.moves();
    }

    /** Decode une ligne STATE ; leve IllegalArgumentException si elle est mal formee. */
    public static GameState decodeState(String line) {
        String[] p = line.trim().split("\\s+");
        if (p.length != 9 || !p[0].equals(STATE)) {
            throw new IllegalArgumentException("Message STATE invalide : " + line);
        }
        try {
            int thief = Integer.parseInt(p[1]);
            int[] police = {Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4])};
            Role turn = switch (p[5]) {
                case "T" -> Role.THIEF;
                case "P" -> Role.POLICE;
                default -> throw new IllegalArgumentException("Tour invalide : " + p[5]);
            };
            boolean left = switch (p[6]) {
                case "1" -> true;
                case "0" -> false;
                default -> throw new IllegalArgumentException("Drapeau invalide : " + p[6]);
            };
            Winner winner = switch (p[7]) {
                case "N" -> Winner.NONE;
                case "P" -> Winner.POLICE;
                case "T" -> Winner.THIEF;
                default -> throw new IllegalArgumentException("Gagnant invalide : " + p[7]);
            };
            return new GameState(thief, police, turn, left, winner, Integer.parseInt(p[8]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nombre invalide dans : " + line, e);
        }
    }
}
