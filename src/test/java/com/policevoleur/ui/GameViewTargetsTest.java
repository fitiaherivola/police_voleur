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
    void policeTargetsStayAdjacentToCurrentPositionAndSkipOccupiedVertices() {
        GameState s = engine.applyThiefMove(engine.initialState(), 4);

        Set<Integer> targets = GameView.availablePoliceTargets(engine, s, 0);

        assertEquals(Set.of(0, 5), targets);
        assertTrue(targets.stream().allMatch(v -> engine.graph().areAdjacent(1, v)));
        assertFalse(targets.contains(4));
        assertFalse(targets.contains(2));
    }

    @Test
    void nextPoliceTurnUsesTheNewPositionsWithoutJumpTargets() {
        GameState afterPolice = engine.applyPoliceMove(engine.applyThiefMove(engine.initialState(), 4),
                new int[] {5, 2, 3});
        GameState s = engine.applyThiefMove(afterPolice, 8);

        Set<Integer> targets = GameView.availablePoliceTargets(engine, s, 1);

        assertEquals(Set.of(0, 1, 6), targets);
        assertTrue(targets.stream().allMatch(v -> engine.graph().areAdjacent(2, v)));
        assertFalse(targets.contains(9));
    }
}
