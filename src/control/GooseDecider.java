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

/**
 * Implementation of the automated tactical decision-making engine for the Geese.
 * <p>
 *     This class uses a score evaluation to analyze and select the mathematically
 *     optimal tactical move based on the board constraints at this point in the game.
 * </p>
 * * @author Tié Rachida Hébie
 */
public class GooseDecider extends Decider {

    public GooseDecider(Model model, Controller control) {
        super(model, control);
    }

    /**
     * Scans the form structure of the grid and return the optimal action sequence.
     * <p>
     *     The loop looks ahead by 1 turn to compute dynamic scores for every valid destination
     *     and retain the highest score
     * </p>
     * @return an ActionList containing the list of chosen parameters
     */
    @Override
    public ActionList decide() {
        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // 1. Locate the fox 1
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
            System.out.println("CRITICAL: GEESE BOT HAS NO VALID MOVES AVAILABLE");
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, bestPawn, bestRowDest, bestColDest);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    /**
     * Evaluate the score of a destination on the board and returns its score
     * @param board The grid model instance
     * @param foxRow current row index of the fox
     * @param foxCol current col index of the fox
     * @param origRow initial row index of the Goose
     * @param origCol initial col index of the Goose
     * @param simRow target destination row index under evaluation
     * @param simCol target destination column index under evaluation
     * @return a score that represent the mathematical utility value of the move
     */
    private int evaluateGooseMove(Board board, int foxRow, int foxCol, int origRow, int origCol, int simRow, int simCol) {

        int score = 0;
        // Count fox free moves AFTER this goose move
        int foxFreeMoves = 0;
        int geeseThreatened = 0;

        Cell foxCell = board.getCell(foxCol, foxRow);
        for (Cell neighbor : foxCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            // Simulate the move
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
        score -= geeseThreatened * 150;

        // Reward moving closer to the fox
        int distBefore = Math.abs(origRow - foxRow) + Math.abs(origCol - foxCol);
        int distAfter  = Math.abs(simRow  - foxRow) + Math.abs(simCol  - foxCol);
        if (distAfter < distBefore) {
            score += 15;
        }

        // The goose form a group
        Cell simCell = board.getCell(simCol, simRow);
        int gooseNeighbors = 0;

        for (Cell neighbor : simCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            if (ny == origRow && nx == origCol) continue;

            GameElement neighborElem = board.getElement(ny, nx);

            if (neighborElem != null && ((Pawn) neighborElem).isGoose()) {
                gooseNeighbors++;
            }
        }
        score += gooseNeighbors * 12;

        if (simRow > foxRow) {
            // The goal here is to stop the fox by being close of his column
            int colDiff = Math.abs(simCol - foxCol);
            if (colDiff <= 1) {
                score += 20; // huge bonus because we are in front of him and his traped
            }
        }

        if ((simRow == foxRow && Math.abs(simCol - foxCol) == 2) ||
                (simCol == foxCol && Math.abs(simRow - foxRow) == 2)) {
            score += 10;
        }


        return score;
    }
}