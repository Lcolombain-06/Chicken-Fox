package view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

/**
 * Outil de debug : affiche le centre de chaque cellule sur le Pane JavaFX.
 * Appeler showGrid() pour afficher, hideGrid() pour masquer.
 * Utiliser les mêmes constantes que ClickHandler et BoardRenderer.
 */
public class DebugGrid {

    // !! Mêmes valeurs que ClickHandler et BoardRenderer !!
    private static final int MARGIN_LEFT   = 6;
    private static final int MARGIN_TOP    = 5;
    private static final int BOARD_PIXEL_W = 468;
    private static final int BOARD_PIXEL_H = 468;
    private static final double CELL_W = BOARD_PIXEL_W / 7.0;
    private static final double CELL_H = BOARD_PIXEL_H / 7.0;

    private final Pane root;
    private final double scale;

    public DebugGrid(Pane root, double windowSize) {
        this.root  = root;
        this.scale = windowSize / 480.0;
    }

    /**
     * Affiche une croix rouge + coordonnée (row,col) au centre de chaque cellule.
     */
    public void showGrid() {
        // Supprimer un éventuel affichage précédent
        hideGrid();

        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {

                // Centre de la cellule en pixels
                double cx = (MARGIN_LEFT + col * CELL_W + CELL_W / 2.0) * scale;
                double cy = (MARGIN_TOP  + row * CELL_H + CELL_H / 2.0) * scale;

                // Croix rouge
                double size = 6;
                Line h = new Line(cx - size, cy, cx + size, cy);
                Line v = new Line(cx, cy - size, cx, cy + size);
                h.setStroke(Color.RED);
                v.setStroke(Color.RED);
                h.setStrokeWidth(2);
                v.setStrokeWidth(2);
                h.setUserData("debug");
                v.setUserData("debug");

                // Point central
                Circle dot = new Circle(cx, cy, 3, Color.RED);
                dot.setUserData("debug");

                // Label (row, col)
                Text label = new Text(cx + 5, cy - 5, row + "," + col);
                label.setFill(Color.BLUE);
                label.setStyle("-fx-font-size: 9px;");
                label.setUserData("debug");

                root.getChildren().addAll(h, v, dot, label);
            }
        }
    }

    /**
     * Supprime tous les éléments de debug du Pane.
     */
    public void hideGrid() {
        root.getChildren().removeIf(node -> "debug".equals(node.getUserData()));
    }
}
