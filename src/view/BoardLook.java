package view;

import boardifier.view.GridLook;
import javafx.scene.paint.Color;
import model.Board;

public class BoardLook extends GridLook {

    private static final int CELL_SIZE = 67;

    public BoardLook(Board board) {
        // borderWidth=2, borderColor=BLACK pour voir la grille
        super(CELL_SIZE, CELL_SIZE, board, 0, 2, Color.BLACK);
    }

    @Override
    protected void render() {
        // Juste la grille par défaut de GridLook, sans image
        super.render();
    }
}