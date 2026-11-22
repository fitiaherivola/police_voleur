package com.policevoleur.ui;

import com.policevoleur.game.GameEngine;
import com.policevoleur.game.GameState;
import com.policevoleur.graph.Graph;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Set;

/**
 * Dessin du plateau circulaire, des sommets, des pions et des deplacements possibles.
 * Les aretes sont celles du graphe charge depuis board.txt ; BoardGeometry ne decide que de leur forme.
 */
public final class BoardCanvas extends Canvas {

    /** Pion selectionne : voleur. */
    public static final int THIEF_PAWN = 3;

    public interface VertexClickListener {
        void onVertexClicked(int vertex);
    }

    private static final Color EDGE = Color.web("#c9d1e6");
    private static final Color VERTEX_FILL = Color.web("#2a3042");
    private static final Color VERTEX_STROKE = Color.web("#8f9bb8");
    private static final Color POLICE_COLOR = Color.web("#4c8dff");
    private static final Color THIEF_COLOR = Color.web("#ff5a5f");
    private static final Color TARGET_COLOR = Color.web("#3ddc84");
    private static final Color SELECT_COLOR = Color.web("#ffd54a");
    private static final Color HOME_COLOR = Color.web("#ffb347");

    private final Graph graph;
    private VertexClickListener clickListener;

    private GameState state;
    private int[] shownPolice = GameEngine.START_POLICE.clone();
    private int selectedPawn = -1;
    private Set<Integer> targets = Set.of();
    private int hintVertex = -1;
    private boolean showLabels = true;

    public BoardCanvas(Graph graph) {
        this.graph = graph;
        widthProperty().addListener((o, a, b) -> draw());
        heightProperty().addListener((o, a, b) -> draw());
        setOnMouseClicked(e -> {
            int v = BoardGeometry.vertexAt(e.getX(), e.getY(), centerX(), centerY(), boardRadius(), 28);
            if (v >= 0 && clickListener != null) {
                clickListener.onVertexClicked(v);
            }
        });
    }

    public void setClickListener(VertexClickListener listener) {
        this.clickListener = listener;
    }

    public void setShowLabels(boolean show) {
        this.showLabels = show;
        draw();
    }

    /**
     * Met a jour l'affichage.
     *
     * @param state        etat reel de la partie
     * @param shownPolice  positions affichees des policiers (peuvent differer de l'etat pendant la preparation du tour)
     * @param selectedPawn 0..2 = policier, THIEF_PAWN = voleur, -1 = aucun
     * @param targets      sommets de destination possibles pour le pion selectionne
     * @param hintVertex   sommet suggere par l'IA, ou -1
     */
    public void update(GameState state, int[] shownPolice, int selectedPawn, Set<Integer> targets, int hintVertex) {
        this.state = state;
        this.shownPolice = shownPolice.clone();
        this.selectedPawn = selectedPawn;
        this.targets = targets;
        this.hintVertex = hintVertex;
        draw();
    }

    private double centerX() {
        return getWidth() / 2;
    }

    private double centerY() {
        return getHeight() / 2;
    }

    private double boardRadius() {
        return Math.min(getWidth(), getHeight()) / 2 - 36;
    }

    private double[] pos(int v) {
        return BoardGeometry.position(v, centerX(), centerY(), boardRadius());
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        double r = boardRadius();
        if (r < 40 || state == null) {
            return;
        }
        double cx = centerX();
        double cy = centerY();

        gc.setFill(Color.web("#141824"));
        gc.fillOval(cx - r - 18, cy - r - 18, 2 * (r + 18), 2 * (r + 18));

        // aretes du graphe
        gc.setStroke(EDGE);
        gc.setLineWidth(4);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineDashes((double[]) null);
        for (int a = 0; a < graph.vertexCount(); a++) {
            for (int b : graph.neighbors(a)) {
                if (a < b) {
                    drawEdge(gc, a, b);
                }
            }
        }

        // maison du voleur : sommet 0
        double[] home = pos(0);
        gc.setStroke(HOME_COLOR);
        gc.setLineWidth(2);
        gc.setLineDashes(5, 4);
        gc.strokeOval(home[0] - 26, home[1] - 26, 52, 52);
        gc.setLineDashes((double[]) null);

        // sommets
        for (int v = 0; v < graph.vertexCount(); v++) {
            double[] p = pos(v);
            gc.setFill(VERTEX_FILL);
            gc.setStroke(VERTEX_STROKE);
            gc.setLineWidth(2);
            gc.fillOval(p[0] - 8, p[1] - 8, 16, 16);
            gc.strokeOval(p[0] - 8, p[1] - 8, 16, 16);
        }

        // destinations possibles
        for (int v : targets) {
            double[] p = pos(v);
            gc.setFill(Color.color(TARGET_COLOR.getRed(), TARGET_COLOR.getGreen(), TARGET_COLOR.getBlue(), 0.25));
            gc.fillOval(p[0] - 19, p[1] - 19, 38, 38);
            gc.setStroke(TARGET_COLOR);
            gc.setLineWidth(3);
            gc.strokeOval(p[0] - 19, p[1] - 19, 38, 38);
        }
        if (hintVertex >= 0) {
            double[] p = pos(hintVertex);
            gc.setStroke(SELECT_COLOR);
            gc.setLineWidth(3);
            gc.setLineDashes(6, 4);
            gc.strokeOval(p[0] - 24, p[1] - 24, 48, 48);
            gc.setLineDashes((double[]) null);
        }

        // ancien emplacement des policiers deja deplaces (preparation du tour)
        int[] actual = state.policePositions();
        for (int i = 0; i < actual.length; i++) {
            if (shownPolice[i] != actual[i]) {
                double[] from = pos(actual[i]);
                double[] to = pos(shownPolice[i]);
                gc.setStroke(Color.color(POLICE_COLOR.getRed(), POLICE_COLOR.getGreen(), POLICE_COLOR.getBlue(), 0.6));
                gc.setLineWidth(2);
                gc.setLineDashes(6, 5);
                gc.strokeLine(from[0], from[1], to[0], to[1]);
                gc.strokeOval(from[0] - 16, from[1] - 16, 32, 32);
                gc.setLineDashes((double[]) null);
            }
        }

        // numeros des sommets
        if (showLabels) {
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.web("#8f9bb8"));
            for (int v = 0; v < graph.vertexCount(); v++) {
                double[] p = pos(v);
                double dx = p[0] - cx;
                double dy = p[1] - cy;
                double len = Math.max(1, Math.hypot(dx, dy));
                gc.fillText(Integer.toString(v), p[0] + dx / len * 22, p[1] + dy / len * 22);
            }
        }

        // pions
        for (int i = 0; i < shownPolice.length; i++) {
            drawPawn(gc, shownPolice[i], POLICE_COLOR, Integer.toString(i + 1), selectedPawn == i);
        }
        drawPawn(gc, state.thief(), THIEF_COLOR, "V", selectedPawn == THIEF_PAWN);
    }

    private void drawPawn(GraphicsContext gc, int vertex, Color color, String text, boolean selected) {
        double[] p = pos(vertex);
        if (selected) {
            gc.setStroke(SELECT_COLOR);
            gc.setLineWidth(4);
            gc.strokeOval(p[0] - 23, p[1] - 23, 46, 46);
        }
        gc.setFill(color);
        gc.fillOval(p[0] - 16, p[1] - 16, 32, 32);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(p[0] - 16, p[1] - 16, 32, 32);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, p[0], p[1]);
    }

    private void drawEdge(GraphicsContext gc, int a, int b) {
        double[] pa = pos(a);
        double[] pb = pos(b);
        switch (BoardGeometry.shape(a, b)) {
            case LINE -> gc.strokeLine(pa[0], pa[1], pb[0], pb[1]);
            case OUTER_ARC, SMALL_ARC -> strokeArc(gc, pa, pb);
            case CURVE -> {
                int mid = BoardGeometry.isAxisVertex(a) ? a : b;
                int end = mid == a ? b : a;
                double[] m = pos(mid);
                double[] e = pos(end);
                double qx;
                double qy;
                if (mid == 5 || mid == 7) {
                    qx = m[0] + (e[0] - m[0]) * 0.55;
                    qy = m[1];
                } else {
                    qx = m[0];
                    qy = m[1] + (e[1] - m[1]) * 0.55;
                }
                gc.beginPath();
                gc.moveTo(e[0], e[1]);
                gc.quadraticCurveTo(qx, qy, m[0], m[1]);
                gc.stroke();
            }
        }
    }

    /** Arc de cercle (centre du plateau) entre deux points, trace en petits segments. */
    private void strokeArc(GraphicsContext gc, double[] pa, double[] pb) {
        double cx = centerX();
        double cy = centerY();
        double a0 = Math.atan2(pa[1] - cy, pa[0] - cx);
        double a1 = Math.atan2(pb[1] - cy, pb[0] - cx);
        double delta = a1 - a0;
        while (delta > Math.PI) {
            delta -= 2 * Math.PI;
        }
        while (delta <= -Math.PI) {
            delta += 2 * Math.PI;
        }
        double r0 = Math.hypot(pa[0] - cx, pa[1] - cy);
        double r1 = Math.hypot(pb[0] - cx, pb[1] - cy);
        int steps = 28;
        gc.beginPath();
        gc.moveTo(pa[0], pa[1]);
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            double ang = a0 + delta * t;
            double rad = r0 + (r1 - r0) * t;
            gc.lineTo(cx + rad * Math.cos(ang), cy + rad * Math.sin(ang));
        }
        gc.stroke();
    }
}
