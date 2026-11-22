package com.policevoleur.ui;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.graph.BoardLoader;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameViewTargetsTest {

    private final GameEngine engine = new GameEngine(BoardLoader.loadDefault());

    @Test
    void policeTargetsStayAdjacentToOriginalPositionAndSkipOccupiedVertices() {
        GameState s = engine.applyThiefMove(engine.initialState(), 4);
        int[] staged = s.policePositions();

        Set<Integer> targets = GameView.availablePoliceTargets(engine, s, staged, 0);

        assertEquals(Set.of(0, 5), targets);
        assertTrue(targets.stream().allMatch(v -> engine.graph().areAdjacent(1, v)));
        assertFalse(targets.contains(4));
        assertFalse(targets.contains(2));
    }

    @Test
    void stagingAnotherPawnNeverCreatesAJumpTarget() {
        GameState s = engine.applyThiefMove(engine.initialState(), 4);
        int[] staged = {5, 2, 3};

        Set<Integer> targets = GameView.availablePoliceTargets(engine, s, staged, 0);

        assertTrue(targets.stream().allMatch(v -> engine.graph().areAdjacent(1, v)));
        assertFalse(targets.contains(9));
        assertFalse(targets.contains(6));
    }
}
