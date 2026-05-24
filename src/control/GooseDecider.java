package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.Board;
import model.Cell;
import model.FGStageModel;
import model.Pawn;

/**
 * AI Decision Engine for the Geese team
 * <p>
 * This class calculates the best possible move for the geese
 * It looks 1 turn ahead (Greedy strategy) and evaluates every valid move
 * It chooses the move that gets the highest score based on our game rules
 * </p>
 */
public class GooseDecider extends Decider {

    public GooseDecider(Model model, Controller control) {
        super(model, control);
    }

    /**
     * Finds the best move for the geese during their turn
     * <p>
     * This method loops through all geese on the board, finds their possible
     * movements, and calls evaluateGooseMove() to give each move a score.
     * The move with the highest score is selected and executed.
     * </p>
     * @return an ActionList containing the list of chosen parameters
     */
    @Override
    public ActionList decide() {
        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // 1. Find the current position of the Fox
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

        // 2. Check every goose and every possible move (1-turn lookahead)
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                GameElement e = board.getElement(y, x);
                if (e == null) continue;
                Pawn p = (Pawn) e;
                if (p.isFox()) continue; // Skip if it is the fox

                // Get all valid destination cells for this specific goose
                board.setValidCells(p, y, x);

                for (int rowDest = 0; rowDest < 7; rowDest++) {
                    for (int colDest = 0; colDest < 7; colDest++) {
                        if (!board.canReachCell(rowDest, colDest)) continue;

                        // Calculate the score for this move
                        int score = evaluateGooseMove(board, foxRow, foxCol, y, x, rowDest, colDest);

                        // Save this move if it has a better score
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

        // 3. Clean up the board valid cells state
        board.clearValidCells();

        // check: if no goose can move
        if (bestPawn == null) {
            System.out.println("CRITICAL: GEESE BOT HAS NO VALID MOVES AVAILABLE");
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        // Create and return the movement action for the framework
        ActionList actions = ActionFactory.generateMoveWithinContainer(model, bestPawn, bestRowDest, bestColDest);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    /**
     * Calculates a score for a specific goose move
     <p>
     * The score is based on 5 simple tactical rules:
     * 1. Blocking the fox (fewer free moves for him is better).
     * 2. Safety (avoid cells where the fox can jump over a goose).
     * 3. Distance (reward moving closer to the fox).
     * 4. Teamwork (reward staying close to other geese).
     * 5. Frontal line (bonus for trapping the fox from above).
     * </p>
     *
     * @param board The current game board
     * @param foxRow The current row of the Fox
     * @param foxCol The current column of the Fox
     * @param origRow The starting row of the Goose
     * @param origCol The starting column of the Goose
     * @param simRow The destination row we want to test
     * @param simCol The destination column we want to test
     * @return An integer score. Higher scores mean better moves
     */
    private int evaluateGooseMove(Board board, int foxRow, int foxCol, int origRow, int origCol, int simRow, int simCol) {

        int score = 0;
        int foxFreeMoves = 0;
        int geeseThreatened = 0;

        // --- RULE 1 & 2: Fox moves and geese safety
        Cell foxCell = board.getCell(foxCol, foxRow);
        for (Cell neighbor : foxCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            // Simulate the move virtually in memory
            GameElement elem = board.getElement(ny, nx);
            if (ny == origRow && nx == origCol) elem = null;
            if (ny == simRow  && nx == simCol)  elem = board.getElement(origRow, origCol);

            if (elem == null) {
                foxFreeMoves++; // The fox has an empty space to move
            } else {
                // Check if the fox can jump over this piece to capture it
                int jumpX = nx + (nx - foxCol);
                int jumpY = ny + (ny - foxRow);
                if (jumpX >= 0 && jumpX < 7 && jumpY >= 0 && jumpY < 7) {
                    if (board.getCell(jumpX, jumpY).isAccessible()) {
                        GameElement jumpElem = board.getElement(jumpY, jumpX);

                        // Update the virtual state for the jump landing cell
                        if (jumpY == origRow && jumpX == origCol) jumpElem = null;
                        if (jumpY == simRow  && jumpX == simCol)  jumpElem = board.getElement(origRow, origCol);
                        if (jumpElem == null) {
                            geeseThreatened++; // The fox can safely jump and eat a goose
                        }
                    }
                }
            }
        }

        // Apply penalties based on the tacticales rules
        score -= foxFreeMoves * 10; // Fewer fox moves = better for geese
        score -= geeseThreatened * 150; // Avoid putting a goose in capture range


        // --- RULE 3: Reward moving closer to the fox
        int distBefore = Math.abs(origRow - foxRow) + Math.abs(origCol - foxCol);
        int distAfter  = Math.abs(simRow  - foxRow) + Math.abs(simCol  - foxCol);
        if (distAfter < distBefore) {
            score += 15; // Give points if the goose moves closer to the fox
        }

        // --- RULE 4: The goose form a group
        Cell simCell = board.getCell(simCol, simRow);
        int gooseNeighbors = 0;


        for (Cell neighbor : simCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            if (ny == origRow && nx == origCol) continue; // Skip the old position

            GameElement neighborElem = board.getElement(ny, nx);

            if (neighborElem != null && ((Pawn) neighborElem).isGoose()) {
                gooseNeighbors++;
            }
        }
        score += gooseNeighbors * 12; // Give points for staying close to other geese

        // --- RULE 5: Barrage
        if (simRow > foxRow) {
            // The goal here is to stop the fox by being close of his column
            int colDiff = Math.abs(simCol - foxCol);
            if (colDiff <= 1) {
                score += 20; // Big bonus if the goose is right in front of the fox
            }
        }

        // Bonus if the goose is 2 cells away from the fox
        if ((simRow == foxRow && Math.abs(simCol - foxCol) == 2) ||
                (simCol == foxCol && Math.abs(simRow - foxRow) == 2)) {
            score += 10;
        }

        return score;
    }
}