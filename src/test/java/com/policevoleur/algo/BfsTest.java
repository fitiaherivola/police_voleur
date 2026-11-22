package com.policevoleur.algo;

import com.policevoleur.graph.BoardLoader;
import com.policevoleur.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BfsTest {

    /** Graphe de la diapositive BFS du cours : r s t u / v w x y. */
    private Graph courseGraph() {
        // r=0 s=1 t=2 u=3 v=4 w=5 x=6 y=7
        Graph g = new Graph(8);
        g.addEdge(0, 1); // r-s
        g.addEdge(0, 4); // r-v
        g.addEdge(1, 5); // s-w
        g.addEdge(5, 2); // w-t
        g.addEdge(5, 6); // w-x
        g.addEdge(2, 6); // t-x
        g.addEdge(2, 3); // t-u
        g.addEdge(6, 3); // x-u
        g.addEdge(6, 7); // x-y
        g.addEdge(3, 7); // u-y
        return g;
    }

    @Test
    void distancesMatchCourseExample() {
        int[] d = Bfs.distances(courseGraph(), 1); // depuis s
        assertArrayEquals(new int[] {1, 0, 2, 3, 2, 1, 2, 3}, d);
    }

    @Test
    void traverseVisitsEveryReachableVertexOnceInLevelOrder() {
        List<Integer> order = Bfs.traverse(courseGraph(), 1);
        assertEquals(8, order.size());
        assertEquals(8, Set.copyOf(order).size());
        assertEquals(1, order.get(0));
        int[] d = Bfs.distances(courseGraph(), 1);
        for (int i = 1; i < order.size(); i++) {
            assertTrue(d[order.get(i - 1)] <= d[order.get(i)]);
        }
    }

    @Test
    void shortestPathOnBoard() {
        Graph g = BoardLoader.loadDefault();
        assertEquals(List.of(0), Bfs.shortestPath(g, 0, 0));
        assertEquals(List.of(0, 4, 8, 18), Bfs.shortestPath(g, 0, 18));
        List<Integer> p = Bfs.shortestPath(g, 0, 9);
        assertEquals(4, p.size());
        assertEquals(0, p.get(0));
        assertEquals(9, p.get(3));
    }

    @Test
    void shortestPathAvoidsBlockedVertices() {
        Graph g = BoardLoader.loadDefault();
        List<Integer> p = Bfs.shortestPath(g, 0, 18, Set.of(4));
        assertFalse(p.isEmpty());
        assertFalse(p.contains(4));
        assertTrue(p.size() > 4);
    }

    @Test
    void noPathWhenEveryExitIsBlocked() {
        Graph g = BoardLoader.loadDefault();
        assertTrue(Bfs.shortestPath(g, 0, 18, Set.of(1, 2, 3, 4)).isEmpty());
        assertTrue(Bfs.shortestPath(g, 0, 18, Set.of(18)).isEmpty());
    }

    @Test
    void everyBoardVertexIsReachable() {
        Graph g = BoardLoader.loadDefault();
        for (int d : Bfs.distances(g, 0)) {
            assertTrue(d >= 0);
        }
    }

    @Test
    void consecutivePathVerticesAreAdjacent() {
        Graph g = BoardLoader.loadDefault();
        for (int to = 0; to < g.vertexCount(); to++) {
            List<Integer> p = Bfs.shortestPath(g, 0, to);
            for (int i = 1; i < p.size(); i++) {
                assertTrue(g.areAdjacent(p.get(i - 1), p.get(i)));
            }
        }
    }
}
