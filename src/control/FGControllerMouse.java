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

    // Two-step click state for geese
    private Pawn selectedPawn = null;

    public FGControllerMouse(Model model, View view, Controller control) {
        super(model, view, control);
    }

    @Override
    public void handle(MouseEvent event) {
        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // Get the actual position of the BoardLook in the scene
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
     * Right-click: adds a cell to the fox's move queue.
     */
    private void handleFoxPlan(FGStageModel stage, Board board, int row, int col) {
        foxMoveQueue.add(new int[]{row, col});
        System.out.println("Cell added to queue: [" + row + "," + col + "] — queue: " + foxMoveQueue.size() + " move(s)");
    }

    /**
     * Left-click: confirms and executes the planned sequence.
     *
     * Logic:
     * 1. Validate the first move — if invalid, cancel the whole sequence
     * 2. Execute the move
     * 3. If it is a capture, check the next move in the list
     * 4. If the next move is also a valid capture, execute it too
     * 5. Continue until end of list, a non-capture move, or an invalid move
     */
    private void handleFoxConfirm(FGStageModel stage, Board board) {
        if (foxMoveQueue.isEmpty()) {
            System.out.println("No move planned. Use right-click to plan moves.");
            return;
        }

        Pawn fox = stage.getFox()[0];
        int currentRow = stage.getFoxRow();
        int currentCol = stage.getFoxCol();

        // Validate and execute each move in the queue
        for (int i = 0; i < foxMoveQueue.size(); i++) {
            int[] move = foxMoveQueue.get(i);
            int toRow = move[0];
            int toCol = move[1];

            // Calculate valid cells from the current position
            board.setValidCells(fox, currentRow, currentCol);

            if (!board.getReachableCells()[toRow][toCol]) {
                // Invalid move → cancel the whole sequence
                System.out.println("Invalid move to [" + toRow + "," + toCol + "] — sequence cancelled.");
                foxMoveQueue.clear();
                board.clearValidCells();
                return;
            }

            boolean isCapture = Math.abs(toRow - currentRow) == 2 || Math.abs(toCol - currentCol) == 2;

            // If this is not the first move and it is not a capture → stop
            if (i > 0 && !isCapture) {
                System.out.println("Non-capture move after a capture to [" + toRow + "," + toCol + "] — sequence stopped.");
                break;
            }

            // Build and execute the action
            ActionList actions = new ActionList();

            if (isCapture) {
                GameElement geeseToEat = board.getFirstElement(
                        (currentRow + toRow) / 2,
                        (currentCol + toCol) / 2);
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

            // Last move in the list → end of turn
            boolean isLastMove = (i == foxMoveQueue.size() - 1);
            actions.setDoEndOfTurn(isLastMove);

            new ActionPlayer(model, control, actions).start();
            System.out.println("Move executed: [" + currentRow + "," + currentCol + "] → [" + toRow + "," + toCol + "]");

            // Update current position for the next move
            currentRow = toRow;
            currentCol = toCol;

            // If this is not a capture, stop after this move
            if (!isCapture) break;
        }

        foxMoveQueue.clear();
        board.clearValidCells();
    }

    private void handleGooseTurn(FGStageModel stage, Board board, int row, int col) {
        if (selectedPawn == null) {
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
            int[] pos = board.getElementCell(selectedPawn);

            if (row == pos[0] && col == pos[1]) {
                selectedPawn.unselect();
                selectedPawn = null;
                board.clearValidCells();
                System.out.println("Goose deselected.");
                return;
            }

            board.setValidCells(selectedPawn, pos[0], pos[1]);

            if (!board.getReachableCells()[row][col]) {
                System.out.println("Cell not reachable for this goose.");
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
}