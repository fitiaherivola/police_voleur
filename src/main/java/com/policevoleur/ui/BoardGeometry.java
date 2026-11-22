package com.policevoleur.ui;

/**
 * Positions des 21 sommets sur le dessin (Java pur, sans JavaFX, donc testable).
 * Seul l'AFFICHAGE vit ici : les connexions viennent de board.txt.
 *
 * Coordonnees en unites "plateau" : centre (0,0), rayon du grand cercle = 1, axe y vers le bas.
 */
public final class BoardGeometry {

    /** Facons de tracer une arete a l'ecran. */
    public enum EdgeShape {
        /** Segment droit (rayons et axes). */
        LINE,
        /** Arc du grand cercle. */
        OUTER_ARC,
        /** Arc du petit cercle central. */
        SMALL_ARC,
        /** Courbe entre un sommet d'axe (5..8) et une extremite sur le grand cercle. */
        CURVE
    }

    public static final int VERTEX_COUNT = 21;

    public static final double SMALL_RADIUS = 52.0 / 165.0;

    private static final double[][] UNIT = new double[VERTEX_COUNT][];

    static {
        UNIT[0] = new double[] {0, 0};
        // petit cercle : haut, droite, bas, gauche
        UNIT[1] = new double[] {0, -SMALL_RADIUS};
        UNIT[2] = new double[] {SMALL_RADIUS, 0};
        UNIT[3] = new double[] {0, SMALL_RADIUS};
        UNIT[4] = new double[] {-SMALL_RADIUS, 0};
        // sommets sur les axes : haut, droite, bas, gauche
        UNIT[5] = new double[] {0, -105.0 / 165.0};
        UNIT[6] = new double[] {92.0 / 165.0, 0};
        UNIT[7] = new double[] {0, 105.0 / 165.0};
        UNIT[8] = new double[] {-92.0 / 165.0, 0};
        // grand cercle, sens horaire depuis le haut (angles mesures depuis la verticale)
        double[] angles = {0, 31, 68, 90, 112, 149, 180, 211, 248, 270, 292, 329};
        for (int i = 0; i < angles.length; i++) {
            double a = Math.toRadians(angles[i]);
            UNIT[9 + i] = new double[] {Math.sin(a), -Math.cos(a)};
        }
    }

    private BoardGeometry() {
    }

    /** Position du sommet v pour un plateau de centre (cx, cy) et de rayon radius pixels. */
    public static double[] position(int v, double cx, double cy, double radius) {
        double[] u = UNIT[v];
        return new double[] {cx + u[0] * radius, cy + u[1] * radius};
    }

    public static boolean isOuter(int v) {
        return v >= 9 && v <= 20;
    }

    public static boolean isSmallCircle(int v) {
        return v >= 1 && v <= 4;
    }

    public static boolean isAxisVertex(int v) {
        return v >= 5 && v <= 8;
    }

    /** Un des 4 points cardinaux du grand cercle (9 haut, 12 droite, 15 bas, 18 gauche). */
    public static boolean isOuterCardinal(int v) {
        return v == 9 || v == 12 || v == 15 || v == 18;
    }

    public static EdgeShape shape(int a, int b) {
        if (isOuter(a) && isOuter(b)) {
            return EdgeShape.OUTER_ARC;
        }
        if (isSmallCircle(a) && isSmallCircle(b)) {
            return EdgeShape.SMALL_ARC;
        }
        if (isAxisVertex(a) && isOuter(b) && !isOuterCardinal(b)) {
            return EdgeShape.CURVE;
        }
        if (isAxisVertex(b) && isOuter(a) && !isOuterCardinal(a)) {
            return EdgeShape.CURVE;
        }
        return EdgeShape.LINE;
    }

    /** Sommet le plus proche de (x, y) a moins de maxDistance pixels, ou -1. */
    public static int vertexAt(double x, double y, double cx, double cy, double radius, double maxDistance) {
        int best = -1;
        double bestDistance = maxDistance;
        for (int v = 0; v < VERTEX_COUNT; v++) {
            double[] p = position(v, cx, cy, radius);
            double d = Math.hypot(p[0] - x, p[1] - y);
            if (d <= bestDistance) {
                best = v;
                bestDistance = d;
            }
        }
        return best;
    }
}
