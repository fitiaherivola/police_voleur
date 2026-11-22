package com.policevoleur.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void edgesAreUndirectedInListAndMatrix() {
        Graph g = new Graph(3);
        g.addEdge(0, 2);
        assertTrue(g.areAdjacent(0, 2));
        assertTrue(g.areAdjacent(2, 0));
        assertFalse(g.areAdjacent(0, 1));
        assertTrue(g.neighbors(0).contains(2));
        assertTrue(g.neighbors(2).contains(0));
        assertEquals(1, g.edgeCount());
        assertEquals(1, g.adjacencyMatrix()[0][2]);
        assertEquals(1, g.adjacencyMatrix()[2][0]);
    }

    @Test
    void rejectsLoopsDuplicatesAndUnknownVertices() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(1, 1));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(1, 0));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 3));
    }

    @Test
    void boardHasExpectedSizeAndDegrees() {
        Graph g = BoardLoader.loadDefault();
        assertEquals(21, g.vertexCount());
        assertEquals(36, g.edgeCount());
        assertEquals(4, g.degree(0));
        for (int v = 1; v <= 4; v++) {
            assertEquals(4, g.degree(v), "petit cercle " + v);
        }
        for (int v = 5; v <= 8; v++) {
            assertEquals(4, g.degree(v), "sommet d'axe " + v);
        }
        for (int v : new int[] {9, 12, 15, 18}) {
            assertEquals(3, g.degree(v), "cardinal du grand cercle " + v);
        }
        for (int v : new int[] {10, 11, 13, 14, 16, 17, 19, 20}) {
            assertEquals(3, g.degree(v), "extremite de courbe " + v);
        }
    }

    @Test
    void boardMatrixIsSymmetricWithEmptyDiagonal() {
        Graph g = BoardLoader.loadDefault();
        int[][] m = g.adjacencyMatrix();
        int ones = 0;
        for (int i = 0; i < m.length; i++) {
            assertEquals(0, m[i][i]);
            for (int j = 0; j < m.length; j++) {
                assertEquals(m[i][j], m[j][i]);
                ones += m[i][j];
            }
        }
        assertEquals(2 * g.edgeCount(), ones);
    }

    @Test
    void secondDrawingCaptureCaseMatchesGraph() {
        // Dessin 2 : voleur en 18, policiers en 17, 19 et 8 = tous ses voisins.
        Graph g = BoardLoader.loadDefault();
        assertEquals(java.util.Set.of(17, 19, 8), new java.util.HashSet<>(g.neighbors(18)));
    }

    @Test
    void startPositionLeavesOneExitFromCenter() {
        // Policiers en 1, 2, 3 : seul le sommet 4 reste libre autour du centre.
        Graph g = BoardLoader.loadDefault();
        assertEquals(java.util.Set.of(1, 2, 3, 4), new java.util.HashSet<>(g.neighbors(0)));
    }
}
