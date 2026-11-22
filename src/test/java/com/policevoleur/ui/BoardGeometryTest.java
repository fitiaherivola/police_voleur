package com.policevoleur.ui;

import com.policevoleur.graph.BoardLoader;
import com.policevoleur.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardGeometryTest {

    @Test
    void everyVertexHasADistinctPositionInsideTheBoard() {
        for (int a = 0; a < BoardGeometry.VERTEX_COUNT; a++) {
            double[] p = BoardGeometry.position(a, 0, 0, 1);
            assertTrue(Math.hypot(p[0], p[1]) <= 1.0001, "sommet " + a + " hors du cercle");
            for (int b = a + 1; b < BoardGeometry.VERTEX_COUNT; b++) {
                double[] q = BoardGeometry.position(b, 0, 0, 1);
                assertTrue(Math.hypot(p[0] - q[0], p[1] - q[1]) > 0.1, "sommets " + a + " et " + b + " trop proches");
            }
        }
    }

    @Test
    void edgeShapesMatchTheDrawing() {
        Graph g = BoardLoader.loadDefault();
        int outer = 0;
        int small = 0;
        int curve = 0;
        int line = 0;
        for (int a = 0; a < g.vertexCount(); a++) {
            for (int b : g.neighbors(a)) {
                if (a < b) {
                    switch (BoardGeometry.shape(a, b)) {
                        case OUTER_ARC -> outer++;
                        case SMALL_ARC -> small++;
                        case CURVE -> curve++;
                        case LINE -> line++;
                    }
                }
            }
        }
        assertEquals(12, outer);
        assertEquals(4, small);
        assertEquals(8, curve);
        assertEquals(12, line); // 4 rayons depuis le centre + 8 segments d'axe
    }

    @Test
    void clickingOnAVertexFindsIt() {
        for (int v = 0; v < BoardGeometry.VERTEX_COUNT; v++) {
            double[] p = BoardGeometry.position(v, 300, 300, 250);
            assertEquals(v, BoardGeometry.vertexAt(p[0] + 3, p[1] - 2, 300, 300, 250, 20));
        }
        assertEquals(-1, BoardGeometry.vertexAt(5, 5, 300, 300, 250, 20));
    }
}
