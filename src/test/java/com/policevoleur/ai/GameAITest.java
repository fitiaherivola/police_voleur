package com.policevoleur.ai;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.game.Role;
import com.policevoleur.game.Winner;
import com.policevoleur.graph.BoardLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameAITest {

    private final GameEngine engine = new GameEngine(BoardLoader.loadDefault());
    private final GameAI ai = new GameAI(engine);

    @Test
    void thiefTakesTheImmediateWinBackToTheCenter() {
        GameState s = new GameState(4, new int[] {13, 14, 15}, Role.THIEF, true, Winner.NONE, 6);
        assertEquals(0, ai.bestThiefMove(s, 2));
    }

    @Test
    void policeTakeTheImmediateCapture() {
        GameState s = new GameState(18, new int[] {17, 19, 4}, Role.POLICE, true, Winner.NONE, 6);
        int[] move = ai.bestPoliceMove(s, 1);
        assertNotNull(move);
        GameState after = engine.applyPoliceMove(s, move);
        assertEquals(Winner.POLICE, after.winner());
    }

    @Test
    void policePreferAFasterCaptureAtDeeperSearch() {
        GameState s = new GameState(18, new int[] {17, 19, 4}, Role.POLICE, true, Winner.NONE, 6);
        int[] move = ai.bestPoliceMove(s, 3);
        assertEquals(Winner.POLICE, engine.applyPoliceMove(s, move).winner());
    }

    @Test
    void suggestionsAreAlwaysLegalFromTheStartingPosition() {
        GameState s = engine.initialState();
        int t = ai.bestThiefMove(s, GameAI.THIEF_DEPTH);
        assertNull(engine.thiefMoveError(s, t));
        GameState afterThief = engine.applyThiefMove(s, t);
        int[] p = ai.bestPoliceMove(afterThief, GameAI.POLICE_DEPTH);
        assertNull(engine.policeMoveError(afterThief, p));
    }

    @Test
    void noSuggestionWhenItIsNotTheirTurnOrGameIsOver() {
        GameState s = engine.initialState();
        assertNull(ai.bestPoliceMove(s, 1));
        GameState over = new GameState(0, new int[] {5, 2, 3}, Role.POLICE, true, Winner.THIEF, 4);
        assertEquals(-1, ai.bestThiefMove(over, 2));
        assertNull(ai.bestPoliceMove(over, 2));
    }

    @Test
    void evaluationRanksWinsAboveEverything() {
        PoliceThiefModel model = new PoliceThiefModel(engine);
        GameState policeWin = new GameState(18, new int[] {17, 19, 8}, Role.THIEF, true, Winner.POLICE, 5);
        GameState thiefWin = new GameState(0, new int[] {5, 2, 3}, Role.POLICE, true, Winner.THIEF, 5);
        GameState ongoing = engine.initialState();
        assertTrue(model.evaluate(policeWin) > model.evaluate(ongoing));
        assertTrue(model.evaluate(thiefWin) < model.evaluate(ongoing));
    }
}
