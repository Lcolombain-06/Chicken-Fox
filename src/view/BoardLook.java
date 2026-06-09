package view;

import boardifier.view.GridLook;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import model.Board;

public class BoardLook extends GridLook {

    private static final int CELL_SIZE = 68;

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
