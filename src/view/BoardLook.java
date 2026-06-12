package view;

import boardifier.view.GridLook;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import model.Board;

public class BoardLook extends GridLook {

    // CORRECTION : plateau agrandi de 68 -> 90px par case (476px -> 630px)
    // GridLook utilise CELL_SIZE pour calculer la position de chaque pion,
    // donc augmenter cette seule valeur agrandit le plateau ET déplace
    // automatiquement les pions aux bonnes nouvelles positions, sans
    // qu'ils ne se décalent par rapport aux cases.
    private static final int CELL_SIZE = 88;

    public BoardLook(Board board) {
        super(CELL_SIZE, CELL_SIZE, board, 0, 0, Color.TRANSPARENT);
    }

    @Override
    protected void render() {
        super.render();

        Image boardImage = new Image("resources/board.png");
        ImageView boardView = new ImageView(boardImage);
        boardView.setFitWidth(CELL_SIZE * 7);
        boardView.setFitHeight(CELL_SIZE * 7);
        boardView.setX(0);
        boardView.setY(0);

        addNode(boardView);
    }
}