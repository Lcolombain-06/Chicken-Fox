package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.Board;
import model.Cell;
import model.HoleStageModel;
import model.Pawn;

public class GooseDecider extends Decider {

    public GooseDecider(Model model, Controller control) {
        super(model, control);
    }

    @Override
    public ActionList decide() {
        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // 1. Locate the fox
        int foxRow = -1, foxCol = -1;
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                GameElement e = board.getElement(y, x);
                if (e != null && ((Pawn) e).isFox()) {
                    foxRow = y;
                    foxCol = x;
                }
            }
        }

        GameElement bestPawn = null;
        int bestRowDest = 0;
        int bestColDest = 0;
        int bestScore = Integer.MIN_VALUE;

        // 2. Evaluate every goose and every valid move
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                GameElement e = board.getElement(y, x);
                if (e == null) continue;
                Pawn p = (Pawn) e;
                if (p.isFox()) continue;

                board.setValidCells(p, y, x);

                for (int rowDest = 0; rowDest < 7; rowDest++) {
                    for (int colDest = 0; colDest < 7; colDest++) {
                        if (!board.canReachCell(rowDest, colDest)) continue;

                        int score = evaluateGooseMove(board, foxRow, foxCol, y, x, rowDest, colDest);

                        if (score > bestScore) {
                            bestScore = score;
                            bestPawn = p;
                            bestRowDest = rowDest;
                            bestColDest = colDest;
                        }
                    }
                }
            }
        }

        // 3. Clear reachable cells so the fox turn is not polluted
        board.clearValidCells();

        if (bestPawn == null) {
            System.out.println("GEESE BOT HAS NO VALID MOVES");
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, bestPawn, bestRowDest, bestColDest);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    /**
     * Scores a goose move based on how well it blocks the fox.
     * Higher is better for the geese.
     *
     * Criteria:
     *  - Heavily penalise moves that leave a goose capturable
     *  - Reward moves that reduce the number of free cells around the fox
     *  - Reward advancing (moving toward the fox row) to tighten the blockade
     */
    private int evaluateGooseMove(Board board, int foxRow, int foxCol,
                                  int origRow, int origCol, int simRow, int simCol) {
        int score = 0;

        // --- A. Count fox free moves AFTER this goose move ---
        int foxFreeMoves = 0;
        int geeseThreatened = 0;

        Cell foxCell = board.getCell(foxCol, foxRow);
        for (Cell neighbor : foxCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            // Simulate the move: origRow/Col is now empty, simRow/Col is now occupied
            GameElement elem = board.getElement(ny, nx);
            if (ny == origRow && nx == origCol) elem = null;
            if (ny == simRow  && nx == simCol)  elem = board.getElement(origRow, origCol);

            if (elem == null) {
                foxFreeMoves++;
            } else {
                // Can the fox jump over this piece?
                int jumpX = nx + (nx - foxCol);
                int jumpY = ny + (ny - foxRow);
                if (jumpX >= 0 && jumpX < 7 && jumpY >= 0 && jumpY < 7) {
                    if (board.getCell(jumpX, jumpY).isAccessible()) {
                        GameElement jumpElem = board.getElement(jumpY, jumpX);
                        if (jumpY == origRow && jumpX == origCol) jumpElem = null;
                        if (jumpY == simRow  && jumpX == simCol)  jumpElem = board.getElement(origRow, origCol);
                        if (jumpElem == null) {
                            geeseThreatened++;
                        }
                    }
                }
            }
        }

        // Fewer fox moves = better for geese
        score -= foxFreeMoves * 10;

        // Avoid putting a goose in capture range
        score -= geeseThreatened * 50;

        // --- B. Reward moving closer to the fox (tightening the blockade) ---
        int distBefore = Math.abs(origRow - foxRow) + Math.abs(origCol - foxCol);
        int distAfter  = Math.abs(simRow  - foxRow) + Math.abs(simCol  - foxCol);
        if (distAfter < distBefore) {
            score += 15;
        }

        // --- C. Reward advancing upward on the board (geese move up) ---
        if (simRow < origRow) {
            score += 5;
        }

        return score;
    }
}