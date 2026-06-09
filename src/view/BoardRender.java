package view;

import boardifier.model.GameElement;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import model.Board;
import model.FGStageModel;
import model.Pawn;

public class BoardRender {

    //!! Même constante que le ClickHandler, pour conversion des coo.
    private static final int MARGIN_LEFT   = 6;
    private static final int MARGIN_TOP    = 5;
    private static final int BOARD_PIXEL_W = 468;
    private static final int BOARD_PIXEL_H = 468;

    private static final double CELL_W = BOARD_PIXEL_W / 7.0;
    private static final double CELL_H = BOARD_PIXEL_H / 7.0;

    private static final double PAWN_SIZE = CELL_W * 0.75;


    private Pane root;
    private FGStageModel stageModel;
    private double scale;

    private Image foxImage;
    private Image geeseImage;

    public BoardRender(Pane root, FGStageModel stageModel, double windowSize) {
        this.root = root;
        this.stageModel = stageModel;
        this.scale = windowSize / 480.0;

        this.foxImage = new Image("file:src/resources/Fox.png");
        this.geeseImage = new Image("file:src/resources/Geese.png");
    }

    public void refresh() {
        // Supprimer tous les pions déjà affichés (tag userData = "pawn")
        root.getChildren().removeIf(node -> "pawn".equals(node.getUserData()));

        Board board = stageModel.getBoard();

        // Parcourir toute la grille Boardifier
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                GameElement element = board.getElement(row, col);
                if (element == null) continue;

                Pawn pawn = (Pawn) element;

                // Conversion cellule → pixel
                double pixelX = (MARGIN_LEFT + col * CELL_W + (CELL_W - PAWN_SIZE) / 2.0) * scale;
                double pixelY = (MARGIN_TOP  + row * CELL_H + (CELL_H - PAWN_SIZE) / 2.0) * scale;

                // Créer l'ImageView du pion
                ImageView view = new ImageView(pawn.isFox() ? foxImage : geeseImage);
                view.setFitWidth(PAWN_SIZE * scale);
                view.setFitHeight(PAWN_SIZE * scale);
                view.setLayoutX(pixelX);
                view.setLayoutY(pixelY);
                view.setUserData("pawn"); // tag pour pouvoir les supprimer au refresh

                root.getChildren().add(view);
            }
        }
    }

}
