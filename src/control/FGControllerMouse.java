package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.control.ControllerMouse;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import boardifier.view.ElementLook;
import boardifier.view.View;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import model.Board;
import model.FGStageModel;
import model.Pawn;

public class FGControllerMouse extends ControllerMouse {

    // État du clic en deux temps pour les oies
    private Pawn selectedPawn = null;

    public FGControllerMouse(Model model, View view, Controller control) {
        super(model, view, control);
    }

    @Override
    public void handle(MouseEvent event) {
        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // Récupérer la position et taille RÉELLES du BoardLook dans la scène
        ElementLook boardLook = control.getElementLook(board);
        if (boardLook == null) return;

        Bounds b = boardLook.getGroup().localToScene(boardLook.getGroup().getBoundsInLocal());
        double boardX = b.getMinX();
        double boardY = b.getMinY();
        double cellW  = b.getWidth()  / 7.0;
        double cellH  = b.getHeight() / 7.0;

        // Conversion pixel --> cellule
        double relX = event.getX() - boardX;
        double relY = event.getY() - boardY;

        int col = (int)(relX / cellW);
        int row = (int)(relY / cellH);

        System.out.println("Board : x=" + boardX + " y=" + boardY + " cellW=" + cellW + " cellH=" + cellH);
        System.out.println("Clic relatif : relX=" + relX + " relY=" + relY + " --> [" + row + "," + col + "]");

        if (col < 0 || col > 6 || row < 0 || row > 6) return;

        int currentPlayer = model.getIdPlayer();
        if (currentPlayer == 0) {
            handleFoxTurn(stage, board, row, col);
        } else {
            handleGooseTurn(stage, board, row, col);
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
        actions.setDoEndOfTurn(true);
        new ActionPlayer(model, control, actions).start();
    }

    private void handleGooseTurn(FGStageModel stage, Board board, int row, int col) {
        for (GameElement e : model.getGameStage().getElements()) {
            if (e.isSelected()) e.unselect();
        }

        if (selectedPawn == null) {
            GameElement e = board.getElement(row, col);
            if (e == null || !(e instanceof Pawn) || !((Pawn) e).isGoose()) {
                System.out.println("Aucune oie ici.");
                return;
            }
            selectedPawn = (Pawn) e;
            int[] pos = board.getElementCell(selectedPawn);
            board.setValidCells(selectedPawn, pos[0], pos[1]);
            System.out.println("Oie sélectionnée en [" + pos[0] + "," + pos[1] + "]");
            selectedPawn.select();

        } else {
            int[] pos = board.getElementCell(selectedPawn);

            if (row == pos[0] && col == pos[1]) {
                selectedPawn.unselect();
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