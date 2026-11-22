package com.policevoleur.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graphe simple non oriente (sans boucle ni arete multiple), represente a la fois
 * par une liste d'adjacence et par une matrice d'adjacence, comme dans le cours.
 * Les sommets sont numerotes de 0 a n-1.
 */
public class Graph {

    private final int vertexCount;
    private final List<List<Integer>> adjacencyList;
    private final boolean[][] adjacencyMatrix;
    private int edgeCount;

    public Graph(int vertexCount) {
        if (vertexCount <= 0) {
            throw new IllegalArgumentException("Le graphe doit avoir au moins un sommet");
        }
        this.vertexCount = vertexCount;
        this.adjacencyList = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacencyList.add(new ArrayList<>());
        }
        this.adjacencyMatrix = new boolean[vertexCount][vertexCount];
    }

    /** Ajoute l'arete non orientee {a, b}. */
    public void addEdge(int a, int b) {
        checkVertex(a);
        checkVertex(b);
        if (a == b) {
            throw new IllegalArgumentException("Boucle interdite sur le sommet " + a);
        }
        if (adjacencyMatrix[a][b]) {
            throw new IllegalArgumentException("Arete deja presente : " + a + "-" + b);
        }
        adjacencyMatrix[a][b] = true;
        adjacencyMatrix[b][a] = true;
        adjacencyList.get(a).add(b);
        adjacencyList.get(b).add(a);
        edgeCount++;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edgeCount;
    }

    /** Voisins du sommet v (liste d'adjacence), en lecture seule. */
    public List<Integer> neighbors(int v) {
        checkVertex(v);
        return Collections.unmodifiableList(adjacencyList.get(v));
    }

    /** Vrai si a et b sont directement relies (matrice d'adjacence). */
    public boolean areAdjacent(int a, int b) {
        checkVertex(a);
        checkVertex(b);
        return adjacencyMatrix[a][b];
    }

    /** Degre = nombre de voisins. */
    public int degree(int v) {
        return neighbors(v).size();
    }

    /** Copie de la matrice d'adjacence (1 si arete, 0 sinon). */
    public int[][] adjacencyMatrix() {
        int[][] copy = new int[vertexCount][vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            for (int j = 0; j < vertexCount; j++) {
                copy[i][j] = adjacencyMatrix[i][j] ? 1 : 0;
            }
        }
        return copy;
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= vertexCount) {
            throw new IllegalArgumentException("Sommet inexistant : " + v);
        }
    }
}
