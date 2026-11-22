package com.policevoleur.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Charge le plateau depuis le fichier de donnees board.txt.
 * Aucune connexion n'est codee en dur : tout vient du fichier, issu du dessin de reference.
 */
public final class BoardLoader {

    public static final String DEFAULT_RESOURCE = "/board.txt";

    private BoardLoader() {
    }

    /** Charge le plateau par defaut embarque dans le projet. */
    public static Graph loadDefault() {
        try (InputStream in = BoardLoader.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Ressource introuvable : " + DEFAULT_RESOURCE);
            }
            return load(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Lit un plateau : "vertices N" puis des lignes "edge A B". Les lignes # sont ignorees. */
    public static Graph load(BufferedReader reader) throws IOException {
        Graph graph = null;
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\s+");
            switch (parts[0]) {
                case "vertices" -> {
                    if (graph != null || parts.length != 2) {
                        throw error(lineNumber, line);
                    }
                    graph = new Graph(Integer.parseInt(parts[1]));
                }
                case "edge" -> {
                    if (graph == null || parts.length != 3) {
                        throw error(lineNumber, line);
                    }
                    graph.addEdge(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                }
                default -> throw error(lineNumber, line);
            }
        }
        if (graph == null) {
            throw new IllegalArgumentException("Fichier de plateau vide");
        }
        return graph;
    }

    private static IllegalArgumentException error(int lineNumber, String line) {
        return new IllegalArgumentException("Ligne " + lineNumber + " invalide : " + line);
    }
}
