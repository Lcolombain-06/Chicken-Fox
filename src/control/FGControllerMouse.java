package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.control.ControllerMouse;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import javafx.scene.input.MouseEvent;
import model.Board;
import model.FGStageModel;
import model.Pawn;

/**
 * FGControllerMouse — gère les clics souris.
 *
 * ControllerMouse de Boardifier est minimaliste : il branche automatiquement
 * handle() sur le RootPane via addEventFilter(), c'est tout.
 * La conversion pixel → cellule et la logique de sélection restent à notre charge.
 */
public class FGControllerMouse extends ControllerMouse {

    // !! Mêmes constantes que BoardRenderer et DebugGrid !!
    private static final int MARGIN_LEFT   = 6;
    private static final int MARGIN_TOP    = 5;
    private static final int BOARD_PIXEL_W = 468;
    private static final int BOARD_PIXEL_H = 468;
    private static final double CELL_W = BOARD_PIXEL_W / 7.0;
    private static final double CELL_H = BOARD_PIXEL_H / 7.0;

    // Facteur d'échelle : taille fenêtre / taille originale du PNG (480)
    private final double scale;

    // État du clic en deux temps pour les oies
    private Pawn selectedPawn = null;

    public FGControllerMouse(Model model, View view, Controller control, int windowWidth) {
        super(model, view, control);
        this.scale = windowWidth / 480.0;
    }

    @Override
    public void handle(MouseEvent event) {
        // Conversion pixel → cellule (annuler le scale d'abord)
        double unscaledX = event.getX() / scale;
        double unscaledY = event.getY() / scale;

        int col = (int)((unscaledX - MARGIN_LEFT) / CELL_W);
        int row = (int)((unscaledY - MARGIN_TOP)  / CELL_H);

        if (col < 0 || col > 6 || row < 0 || row > 6) return;

        System.out.println("Clic --> [" + row + "," + col + "]");

        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();
        int currentPlayer = model.getIdPlayer();

        if (currentPlayer == 0) {
            handleFoxTurn(stage, board, row, col);
        } else {
            handleGooseTurn(board, row, col);
        }
    }

    private void handleFoxTurn(FGStageModel stage, Board board, int toRow, int toCol) {
        Pawn fox = stage.getFox()[0];
        int foxRow = stage.getFoxRow();
        int foxCol = stage.getFoxCol();

        board.setValidCells(fox, foxRow, foxCol);

        if (!board.getReachableCells()[toRow][toCol]) {
            System.out.println("Case non atteignable pour le renard.");
            return;
        }

        ActionList actions = new ActionList();

        // Vérifier si c'est un saut (capture)
        if (Math.abs(toRow - foxRow) == 2 || Math.abs(toCol - foxCol) == 2) {
            GameElement geeseToEat = board.getFirstElement(
                    (foxRow + toRow) / 2,
                    (foxCol + toCol) / 2);
            if (geeseToEat != null) {
                actions.addAll(ActionFactory.generateRemoveFromStage(model, geeseToEat));
                stage.eatGeese();
            }
            stage.setFoxCaptured(true);
        } else {
            stage.setFoxCaptured(false);
        }

        actions.addAll(ActionFactory.generateMoveWithinContainer(control, model, fox, toRow, toCol));
        stage.setFoxCoo(toRow, toCol);

        // doEndOfTurn=true --> ActionPlayer appellera nextPlayer() dans FGController
        actions.setDoEndOfTurn(true);
        new ActionPlayer(model, control, actions).start();
    }

    private void handleGooseTurn(Board board, int row, int col) {
        if (selectedPawn == null) {
            // Premier clic : sélectionner une oie
            GameElement e = board.getElement(row, col);
            if (e == null || !(e instanceof Pawn) || !((Pawn) e).isGoose()) {
                System.out.println("Aucune oie ici.");
                return;
            }
            selectedPawn = (Pawn) e;
            int[] pos = board.getElementCell(selectedPawn);
            board.setValidCells(selectedPawn, pos[0], pos[1]);
            System.out.println("Oie sélectionnée en [" + pos[0] + "," + pos[1] + "]");

        } else {
            // Deuxième clic : destination
            int[] pos = board.getElementCell(selectedPawn);

            // Clic sur la même oie = désélectionner
            if (row == pos[0] && col == pos[1]) {
                selectedPawn = null;
                board.clearValidCells();
                System.out.println("Oie désélectionnée.");
                return;
            }

            board.setValidCells(selectedPawn, pos[0], pos[1]);

            if (!board.getReachableCells()[row][col]) {
                System.out.println("Case non atteignable pour cette oie.");
                selectedPawn = null;
                board.clearValidCells();
                return;
            }

            ActionList actions = ActionFactory.generateMoveWithinContainer(control, model, selectedPawn, row, col);
            actions.setDoEndOfTurn(true);
            new ActionPlayer(model, control, actions).start();

            selectedPawn = null;
            board.clearValidCells();
        }
    }
}