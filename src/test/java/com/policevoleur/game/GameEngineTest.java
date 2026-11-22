package com.policevoleur.game;

import com.policevoleur.graph.BoardLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private final GameEngine engine = new GameEngine(BoardLoader.loadDefault());

    private static GameState state(int thief, int[] police, Role turn, boolean left) {
        return new GameState(thief, police, turn, left, Winner.NONE, 0);
    }

    @Test
    void initialStateMatchesTheDrawing() {
        GameState s = engine.initialState();
        assertEquals(0, s.thief());
        assertArrayEquals(new int[] {1, 2, 3}, s.policePositions());
        assertEquals(Role.THIEF, s.turn());
        assertEquals(Winner.NONE, s.winner());
        assertFalse(s.thiefLeftCenter());
    }

    @Test
    void thiefStartingAtCenterIsNeverACapture() {
        GameState s = engine.initialState();
        assertFalse(s.isOver());
        assertEquals(List.of(4), engine.thiefMoves(s));
    }

    @Test
    void thiefCannotJumpOrStepOnAPoliceman() {
        GameState s = engine.initialState();
        assertNotNull(engine.thiefMoveError(s, 5), "saut interdit");
        assertNotNull(engine.thiefMoveError(s, 1), "case occupee");
        assertNotNull(engine.thiefMoveError(s, 0), "il doit bouger");
        assertNotNull(engine.thiefMoveError(s, 99));
        assertNull(engine.thiefMoveError(s, 4));
    }

    @Test
    void turnsAlternateStartingWithTheThief() {
        GameState s = engine.initialState();
        assertNotNull(engine.policeMoveError(s, new int[] {5, 2, 3}), "pas le tour des policiers");
        GameState afterThief = engine.applyThiefMove(s, 4);
        assertEquals(Role.POLICE, afterThief.turn());
        assertTrue(afterThief.thiefLeftCenter());
        assertNotNull(engine.thiefMoveError(afterThief, 0), "pas le tour du voleur");
        GameState afterPolice = engine.applyPoliceMove(afterThief, new int[] {5, 2, 3});
        assertEquals(Role.THIEF, afterPolice.turn());
        assertEquals(2, afterPolice.moves());
    }

    @Test
    void policeValidation() {
        GameState s = engine.applyThiefMove(engine.initialState(), 4); // voleur en 4, policiers 1 2 3
        assertNull(engine.policeMoveError(s, new int[] {5, 2, 3}));
        assertNotNull(engine.policeMoveError(s, new int[] {1, 2, 3}), "au moins un doit bouger");
        assertNotNull(engine.policeMoveError(s, new int[] {9, 2, 3}), "pas voisin");
        assertNotNull(engine.policeMoveError(s, new int[] {1, 2, 4}), "pas sur le voleur");
        assertNotNull(engine.policeMoveError(s, new int[] {1, 1, 3}), "meme sommet interdit");
        assertNotNull(engine.policeMoveError(s, new int[] {5, 2}), "3 destinations");
        assertNotNull(engine.policeMoveError(s, null));
    }

    @Test
    void sharedVertexCanBeAllowedByRules() {
        GameEngine permissive = new GameEngine(BoardLoader.loadDefault(), new GameRules(false, true));
        GameState s = permissive.applyThiefMove(permissive.initialState(), 4);
        // policiers en 1, 2, 3 : les policiers 1 et 3 peuvent tous deux aller sur le centre
        assertNull(permissive.policeMoveError(s, new int[] {0, 2, 0}));
        assertNotNull(engine.policeMoveError(engine.applyThiefMove(engine.initialState(), 4), new int[] {0, 2, 0}));
    }

    @Test
    void policeMustAllMoveRuleForbidsStayingPut() {
        GameEngine strict = new GameEngine(BoardLoader.loadDefault(), new GameRules(true, false));
        GameState s = strict.applyThiefMove(strict.initialState(), 4);
        assertNotNull(strict.policeMoveError(s, new int[] {5, 2, 3}));
        for (int[] move : strict.policeMoves(s)) {
            for (int i = 0; i < 3; i++) {
                assertTrue(move[i] != s.policeAt(i));
            }
        }
        assertFalse(strict.policeMoves(s).isEmpty());
    }

    @Test
    void everyGeneratedPoliceMoveIsValid() {
        GameState s = engine.applyThiefMove(engine.initialState(), 4);
        List<int[]> moves = engine.policeMoves(s);
        assertFalse(moves.isEmpty());
        for (int[] m : moves) {
            assertNull(engine.policeMoveError(s, m));
        }
    }

    @Test
    void policeWinByBlockingEveryMoveOfTheThief() {
        // Dessin 2 : voleur en 18, policiers en 17, 19 et 8 (ici le dernier arrive de 4).
        GameState s = state(18, new int[] {17, 19, 4}, Role.POLICE, true);
        GameState after = engine.applyPoliceMove(s, new int[] {17, 19, 8});
        assertEquals(Winner.POLICE, after.winner());
        assertTrue(after.isOver());
        assertTrue(engine.thiefMoves(after).isEmpty());
    }

    @Test
    void noWinnerWhileTheThiefStillHasAMove() {
        GameState s = state(18, new int[] {17, 19, 4}, Role.POLICE, true);
        GameState after = engine.applyPoliceMove(s, new int[] {17, 19, 3});
        assertEquals(Winner.NONE, after.winner());
        assertEquals(List.of(8), engine.thiefMoves(after));
    }

    @Test
    void thiefWinsByReturningToTheCenterAfterLeavingIt() {
        GameState s = engine.applyThiefMove(engine.initialState(), 4);
        s = engine.applyPoliceMove(s, new int[] {5, 2, 3});
        GameState end = engine.applyThiefMove(s, 0);
        assertEquals(Winner.THIEF, end.winner());
    }

    @Test
    void policeCanOccupyTheCenterToBlockTheReturn() {
        GameState s = state(4, new int[] {1, 2, 3}, Role.POLICE, true);
        GameState after = engine.applyPoliceMove(s, new int[] {0, 2, 3});
        assertFalse(engine.thiefMoves(after).contains(0));
    }

    @Test
    void noMoveAfterTheGameIsOver() {
        GameState over = new GameState(0, new int[] {5, 2, 3}, Role.POLICE, true, Winner.THIEF, 4);
        assertNotNull(engine.thiefMoveError(over, 4));
        assertNotNull(engine.policeMoveError(over, new int[] {1, 2, 3}));
        assertTrue(engine.policeMoves(over).isEmpty());
    }

    @Test
    void applyingAnIllegalMoveThrows() {
        assertThrows(IllegalArgumentException.class, () -> engine.applyThiefMove(engine.initialState(), 5));
    }
}
