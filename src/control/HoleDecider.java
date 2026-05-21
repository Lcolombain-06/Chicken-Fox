package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.Board;
import model.HoleStageModel;
import model.Pawn;

public class HoleDecider extends Decider {

    public HoleDecider(Model model, Controller control) {
        super(model, control);
    }

    @Override
    public ActionList decide() {
        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        GameElement bestPawn = null;
        int bestRowDest = 0;
        int bestColDest = 0;
        int bestScore = -99999;

        // 1. Locate the REAL position of the Fox on the board
        int foxX = -1;
        int foxY = -1;
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (board.getElement(y, x) != null) {
                    Pawn p = (Pawn) board.getElement(y, x);
                    if (p.isFox()) {
                        foxY = y;
                        foxX = x;
                        break;
                    }
                }
            }
            if (foxY != -1) break;
        }

        // 2. Loop through all cells to find the Geese/Chickens
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {

                if (board.getElement(y, x) != null) {
                    Pawn p = (Pawn) board.getElement(y, x);

                    // If it's a goose, evaluate its possible destinations
                    if (!p.isFox()) {

                        // Compute the real reachable cells for this specific goose
                        board.setValidCells(p, y, x);

                        for (int rowDest = 0; rowDest < 7; rowDest++) {
                            for (int colDest = 0; colDest < 7; colDest++) {

                                if (board.canReachCell(rowDest, colDest)) {

                                    // --- MATHEMATICAL SIMULATION WITHOUT MOVING PIECES ---
                                    // Compute the score by simulating the move mentally.
                                    // Instead of physically moving the pawn, we pass its future and past coordinates.
                                    int currentScore = evaluateBoardVirtually(board, foxY, foxX, y, x, rowDest, colDest);

                                    if (currentScore > bestScore) {
                                        bestScore = currentScore;
                                        bestPawn = p;
                                        bestRowDest = rowDest;
                                        bestColDest = colDest;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Safety check if no valid move was found
        if (bestPawn == null) {
            System.out.println("COMPUTER HAS NO VALID MOVES");
            ActionList emptyAction = new ActionList();
            emptyAction.setDoEndOfTurn(true);
            return emptyAction;
        }

        // 3. The AI executes the chosen real move ONLY at the end of its mathematical reflection
        board.clearValidCells();
        ActionList actions = ActionFactory.generateMoveWithinContainer(model, bestPawn, bestRowDest, bestColDest);
        actions.setDoEndOfTurn(true);

        return actions;
    }

    /**
     * Evaluates the board state in a purely mathematical way.
     * We ignore what is physically on (origRow, origCol) because the goose is supposed to have left.
     * We act as if the goose is now located on (simRow, simCol).
     */
    private int evaluateBoardVirtually(Board board, int foxY, int foxX,
                                       int origRow, int origCol, int simRow, int simCol) {
        int foxMoves = 0;
        int chickensInDanger = 0;

        for (model.Cell neighbor : board.getCell(foxX, foxY).getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            GameElement elementNeighbor = board.getElement(ny, nx);
            if (ny == origRow && nx == origCol) elementNeighbor = null;
            if (ny == simRow  && nx == simCol)  elementNeighbor = board.getElement(origRow, origCol);

            if (elementNeighbor == null) {
                foxMoves++;
            } else {
                int jumpX = nx + (nx - foxX);
                int jumpY = ny + (ny - foxY);

                if (jumpX >= 0 && jumpX < 7 && jumpY >= 0 && jumpY < 7) {
                    if (board.getCell(jumpX, jumpY).isAccessible()) {
                        GameElement elementJump = board.getElement(jumpY, jumpX);
                        if (jumpY == origRow && jumpX == origCol) elementJump = null;
                        if (jumpY == simRow  && jumpX == simCol)  elementJump = board.getElement(origRow, origCol);

                        if (elementJump == null) {
                            chickensInDanger++;
                        }
                    }
                }
            }
        }

        return -(foxMoves * 10) - (chickensInDanger * 50);
    }}