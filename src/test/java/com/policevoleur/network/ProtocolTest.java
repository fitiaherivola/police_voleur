package com.policevoleur.network;

import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;
import com.policevoleur.game.Winner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {

    @Test
    void stateSurvivesEncodeDecode() {
        GameState[] samples = {
                new GameState(0, new int[] {1, 2, 3}, Role.THIEF, false, Winner.NONE, 0),
                new GameState(18, new int[] {17, 19, 8}, Role.THIEF, true, Winner.POLICE, 11),
                new GameState(0, new int[] {5, 2, 3}, Role.POLICE, true, Winner.THIEF, 4),
        };
        for (GameState s : samples) {
            assertEquals(s, Protocol.decodeState(Protocol.encodeState(s)));
        }
    }

    @Test
    void encodedFormatIsStable() {
        GameState s = new GameState(4, new int[] {5, 2, 3}, Role.POLICE, true, Winner.NONE, 1);
        assertEquals("STATE 4 5 2 3 P 1 N 1", Protocol.encodeState(s));
    }

    @Test
    void malformedStatesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Protocol.decodeState("STATE 1 2"));
        assertThrows(IllegalArgumentException.class, () -> Protocol.decodeState("HELLO 1 2 3 4 T 0 N 0"));
        assertThrows(IllegalArgumentException.class, () -> Protocol.decodeState("STATE a 2 3 4 T 0 N 0"));
        assertThrows(IllegalArgumentException.class, () -> Protocol.decodeState("STATE 0 1 2 3 X 0 N 0"));
        assertThrows(IllegalArgumentException.class, () -> Protocol.decodeState("STATE 0 1 2 3 T 2 N 0"));
        assertThrows(IllegalArgumentException.class, () -> Protocol.decodeState("STATE 0 1 2 3 T 0 Z 0"));
    }
}
