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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.Board;
import model.FGStageModel;
import model.Pawn;

import java.util.ArrayList;
import java.util.List;

public class FGControllerMouse extends ControllerMouse {

    // Queue of planned moves for the fox (right-click)
    private final List<int[]> foxMoveQueue = new ArrayList<>();

    // Currently selected goose pawn (two-click selection flow)
    private Pawn selectedPawn = null;

    public FGControllerMouse(Model model, View view, Controller control) {
        super(model, view, control);
    }

    @Override
    public void handle(MouseEvent event) {
        // Game stopped (quit or restart in progress): ignore input
        if (model.getGameStage() == null) return;

        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // Get the real position of the board look in the scene
        ElementLook boardLook = control.getElementLook(board);
        if (boardLook == null) return;

        Bounds b = boardLook.getGroup().localToScene(boardLook.getGroup().getBoundsInLocal());
        double boardX = b.getMinX();
        double boardY = b.getMinY();
        double cellW  = b.getWidth()  / 7.0;
        double cellH  = b.getHeight() / 7.0;

        double relX = event.getX() - boardX;
        double relY = event.getY() - boardY;

        int col = (int)(relX / cellW);
        int row = (int)(relY / cellH);

        if (col < 0 || col > 6 || row < 0 || row > 6) return;

        int currentPlayer = model.getIdPlayer();

        if (currentPlayer == 0) {
            if (event.getButton() == MouseButton.SECONDARY) {
                // Right-click → add the cell to the planning queue
                handleFoxPlan(stage, board, row, col);
            } else if (event.getButton() == MouseButton.PRIMARY) {
                // Left-click → confirm and execute the planned sequence
                handleFoxConfirm(stage, board);
            }
        } else {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleGooseTurn(stage, board, row, col);
            }
        }
    }

    /**
     * Right click: add a target cell to the fox's planned move queue.
     */
    private void handleFoxPlan(FGStageModel stage, Board board, int row, int col) {
        foxMoveQueue.add(new int[]{row, col});
        System.out.println("Cell added to queue: [" + row + "," + col + "] - queue: " + foxMoveQueue.size() + " move(s)");
    }

    /**
     * Left-click: confirms and executes the planned sequence.
     */
    private void handleFoxConfirm(FGStageModel stage, Board board) {
        if (foxMoveQueue.isEmpty()) {
            System.out.println("No move planned. Right-click to plan a move.");
            return;
        }

        Pawn fox = stage.getFox()[0];
        int currentRow = stage.getFoxRow();
        int currentCol = stage.getFoxCol();

        // All actions for the whole sequence (single capture or capture chain)
        ActionList allActions = new ActionList();
        boolean anyMove = false;

        for (int i = 0; i < foxMoveQueue.size(); i++) {
            int[] move = foxMoveQueue.get(i);
            int toRow = move[0];
            int toCol = move[1];

            // Calculate valid cells from the current position
            board.setValidCells(fox, currentRow, currentCol);

            if (!board.getReachableCells()[toRow][toCol]) {
                System.out.println("Invalid move to [" + toRow + "," + toCol + "] - sequence cancelled.");
                foxMoveQueue.clear();
                board.clearValidCells();
                // If nothing was queued yet, abort completely; otherwise keep what we have
                if (!anyMove) return;
                break;
            }

            // A jump of 2 cells (row or column) is a capture move!
            boolean isCapture = Math.abs(toRow - currentRow) == 2 || Math.abs(toCol - currentCol) == 2;

            // After a capture, only another capture is allowed (multi-capture rule)
            if (i > 0 && !isCapture) {
                System.out.println("Non-capture move after a capture at [" + toRow + "," + toCol + "] - sequence stopped.");
                break;
            }

            if (isCapture) {
                // Remove the goose that gets jumped over
                GameElement geeseToEat = board.getFirstElement(
                        (currentRow + toRow) / 2,
                        (currentCol + toCol) / 2);
                if (geeseToEat != null) {
                    allActions.addAll(ActionFactory.generateRemoveFromStage(model, geeseToEat));
                    stage.eatGeese();
                }
                stage.setFoxCaptured(true);
            } else {
                stage.setFoxCaptured(false);
            }

            // Add the fox movement t o the same action list
            allActions.addAll(ActionFactory.generateMoveWithinContainer(control, model, fox, toRow, toCol));
            stage.setFoxCoo(toRow, toCol);
            anyMove = true;

            System.out.println("Move executed: [" + currentRow + "," + currentCol + "] -> [" + toRow + "," + toCol + "]");

            currentRow = toRow;
            currentCol = toCol;

            // Stop the chain after a non-capture move (only one move allowed per turn)
            if (!isCapture) break;
        }

        if (anyMove) {
            // End the turn only once, after the whole sequence has been built
            allActions.setDoEndOfTurn(true);
            new ActionPlayer(model, control, allActions).start();
        }

        foxMoveQueue.clear();
        board.clearValidCells();
    }

    /**
     * Left click for the geese: select a goose, then click a destination cell to move it.
     */
    private void handleGooseTurn(FGStageModel stage, Board board, int row, int col) {
        if (selectedPawn == null) {
            // First click: select a goose
            GameElement e = board.getElement(row, col);
            if (e == null || !(e instanceof Pawn) || !((Pawn) e).isGoose()) {
                System.out.println("No goose here.");
                return;
            }
            selectedPawn = (Pawn) e;
            int[] pos = board.getElementCell(selectedPawn);
            board.setValidCells(selectedPawn, pos[0], pos[1]);
            selectedPawn.select();
            System.out.println("Goose selected at [" + pos[0] + "," + pos[1] + "]");

        } else {
            // Second click: move the selected goose or deselect it
            int[] pos = board.getElementCell(selectedPawn);

            if (row == pos[0] && col == pos[1]) {
                // Clicked the same cell againnn: deselect
                selectedPawn.unselect();
                selectedPawn = null;
                board.clearValidCells();
                System.out.println("Goose deselected.");
                return;
            }

            board.setValidCells(selectedPawn, pos[0], pos[1]);

            if (!board.getReachableCells()[row][col]) {
                System.out.println("Unreachable cell for this goose.");
                selectedPawn.unselect();
                selectedPawn = null;
                board.clearValidCells();
                return;
            }

            selectedPawn.unselect();
            ActionList actions = ActionFactory.generateMoveWithinContainer(control, model, selectedPawn, row, col);
            actions.setDoEndOfTurn(true);
            new ActionPlayer(model, control, actions).start();
            selectedPawn = null;
            board.clearValidCells();
        }
    }

    /**
     * Called by FGController.stopGame() to clear internal state
     * before the handler could possibly still be triggered.
     */
    public void reset() {
        foxMoveQueue.clear();
        selectedPawn = null;
    }
}