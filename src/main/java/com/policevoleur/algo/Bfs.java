package com.policevoleur.algo;

import com.policevoleur.graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Parcours en largeur (BFS) selon le pseudo-code du cours :
 * visited[s] <- vrai ; file Q <- {s} ; tant que Q non vide : v <- defiler ;
 * pour chaque voisin u de v non visite : marquer u, enfiler u.
 */
public final class Bfs {

    private Bfs() {
    }

    /** Ordre de visite des sommets atteignables depuis s. */
    public static List<Integer> traverse(Graph g, int s) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[g.vertexCount()];
        Queue<Integer> queue = new ArrayDeque<>();
        visited[s] = true;
        queue.add(s);
        while (!queue.isEmpty()) {
            int v = queue.remove();
            order.add(v);
            for (int u : g.neighbors(v)) {
                if (!visited[u]) {
                    visited[u] = true;
                    queue.add(u);
                }
            }
        }
        return order;
    }

    /** Distance (nombre d'aretes) de s a chaque sommet ; -1 si inatteignable. */
    public static int[] distances(Graph g, int s) {
        return search(g, s, Set.of()).distance;
    }

    /**
     * Plus court chemin de from a to (sommets inclus), sans passer par les sommets de blocked.
     * Liste vide si aucun chemin n'existe. Si from == to, renvoie [from].
     */
    public static List<Integer> shortestPath(Graph g, int from, int to, Set<Integer> blocked) {
        if (blocked.contains(from) || blocked.contains(to)) {
            return List.of();
        }
        Result r = search(g, from, blocked);
        if (r.distance[to] < 0) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        for (int v = to; v != -1; v = r.parent[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }

    public static List<Integer> shortestPath(Graph g, int from, int to) {
        return shortestPath(g, from, to, Set.of());
    }

    private record Result(int[] distance, int[] parent) {
    }

    private static Result search(Graph g, int s, Set<Integer> blocked) {
        int n = g.vertexCount();
        int[] distance = new int[n];
        int[] parent = new int[n];
        Arrays.fill(distance, -1);
        Arrays.fill(parent, -1);
        Queue<Integer> queue = new ArrayDeque<>();
        distance[s] = 0;
        queue.add(s);
        while (!queue.isEmpty()) {
            int v = queue.remove();
            for (int u : g.neighbors(v)) {
                if (distance[u] < 0 && !blocked.contains(u)) {
                    distance[u] = distance[v] + 1;
                    parent[u] = v;
                    queue.add(u);
                }
            }
        }
        return new Result(distance, parent);
    }
}
