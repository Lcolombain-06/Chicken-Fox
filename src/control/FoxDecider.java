package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * AI Decision Engine for the Fox team
 *     This class uses a score evaluation to analyze and select the mathematically
 *     optimal tactical move based on the board constraints at this point in the game.
 *
 */
public class FoxDecider extends Decider {

    private static final Random loto = new Random(Calendar.getInstance().getTimeInMillis());

    private int lastRow = -1;
    private int lastCol = -1;

    public FoxDecider(Model model, Controller control) {
        super(model, control);
    }

    /**
     * Checks all legal moves for the fox and finds the best one
     *
     * @return an integer array with the best target coordinates [row, col]
     */
    public int[] chooseBestMove() {
        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();
        Pawn fox = stage.getFox()[0];

        // Get the current position of the fox
        int[] pos = board.getElementCell(fox);
        int foxRow = pos[0];
        int foxCol = pos[1];

        // Find all reachable cells for the fox
        board.setValidCells(fox, foxRow, foxCol);

        List<int[]> validMoves = new ArrayList<>();
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (board.getReachableCells()[r][c]) {
                    validMoves.add(new int[]{r, c});
                }
            }
        }

        // security
        if (validMoves.isEmpty()) {
            return new int[]{foxRow, foxCol};
        }

        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();

        // Calculate the score for each possible move
        for (int[] move : validMoves) {
            int score = scoreMove(fox, foxRow, foxCol, move[0], move[1], board, stage);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move); // Save ties to choose randomly later
            }
        }

        // Reset the board valid cells state
        board.setValidCells(fox, foxRow, foxCol);
        board.resetReachableCells(false);

        // Choose a cell if the list is empty
        if (bestMoves.isEmpty()) {
            return validMoves.get(loto.nextInt(validMoves.size()));
        }
        int[] bestMove = bestMoves.get(loto.nextInt(bestMoves.size()));

        return bestMove;
    }

    /**
     * Calculates a score for a specific fox move
     * <p>
     * The score is based on 5 tactical rules:
     * 1. Capturing (absolute priority to jumping over geese)
     * 2. Center control (bonus for moving closer to the center)
     * 3. Direction (slight bonus for moving down)
     * 4. Hunting (bonus for targeting isolated geese)
     * 5. Anti-loop (malus if the fox goes back to its previous cell)
     * </p>
     *
     * @return An integer score. Higher scores mean better moves.
     */
    private int scoreMove(Pawn fox, int fromR, int fromC, int toR, int toC, Board board, FGStageModel stage) {
        int score = 0;

        // --- RULE 1: Absolute priority to capturing geese
        if (Math.abs(toR - fromR) == 2 || Math.abs(toC - fromC) == 2) {
            score += 1000; // General bonus for a jump move
            boolean canCapture = board.foxCanCapture(fox, toR, toC);
            board.setValidCells(fox, fromR, fromC);
            if (canCapture) score += 1000; // Extra massive bonus for another capture possible after this one
        }

        // --- RULE 2: Move in direction of the center for better angles
        int distCentreOrigine = Math.abs(fromR - 3) + Math.abs(fromC - 3);
        int distCentreDestination = Math.abs(toR - 3) + Math.abs(toC - 3);

        if (distCentreDestination < distCentreOrigine) {
            score += 15; // Bonus for controlling central cells
        }

        // --- RULE 3: Bonus for infiltration down the geese
        if (toR > fromR) {
            score += 5;
        }

        // evaluate position
        if (toR <= 4){
            score += toR * 10;
        }
        else {
            score -= toR * 10;
        }


        // --- RULE 4: Hunt isolated geese
        Cell destCell = board.getCell(toC, toR);
        for (Cell geese : destCell.getNeighbors()) {
            int nx = geese.getX();
            int ny = geese.getY();
            GameElement e = board.getElement(ny, nx);
            if (e != null && ((Pawn) e).isGoose()) {
                int gooseNeighborCount = 0;
                // Count how many neighbors this goose has
                for (Cell nn : geese.getNeighbors()) {
                    GameElement e2 = board.getElement(nn.getY(), nn.getX());
                    if (e2 != null && ((Pawn) e2).isGoose()) {
                        gooseNeighborCount++;
                    }
                }

                // If the goose has 1 or 0 neighbors, it is alone and weak
                if (gooseNeighborCount <= 1) {
                    score += 50; // bonus to attack this isolated goose

                }
            }

        }

        // --- RULE 5: prevent infinite games (try to)
        if (toR == lastRow && toC == lastCol) {

            if (score < 1000) {
                score -= 800; // Big penalty to stop the fox from doing useless back-and-forth moves
            }
        }

        // Add some random noise to break perfect equalities and help the choice
        score += loto.nextInt(100);

        return score;

    }


    /**
     * Executes the move chosen by the AI during the fox's turn
     *
     * It updates the loop memory, handles removing a goose if it was captured,
     * and sends the movement to Boardifier.
     *
     * @return The finalized ActionList sequence for this turn
     */
    @Override
    public ActionList decide() {
        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();
        Pawn fox = stage.getFox()[0];

        int[] pos = board.getElementCell(fox);
        int foxRow = pos[0];
        int foxCol = pos[1];

        // Get the best move coordinates
        int[] bestMove = chooseBestMove();

        // Save current position as the last position in memory before moving
        this.lastRow = foxRow;
        this.lastCol = foxCol;

        ActionList actions = new ActionList();

        // Check if the move is a jump (capture move)
        if (Math.abs(bestMove[0] - foxRow) == 2 || Math.abs(bestMove[1] - foxCol) == 2) {
            GameElement geeseToEat = board.getFirstElement(
                    (foxRow + bestMove[0]) / 2,
                    (foxCol + bestMove[1]) / 2);
            if (geeseToEat != null) {
                // Generate actions to remove the goose from the board
                actions.addAll(ActionFactory.generateRemoveFromStage(model, geeseToEat));
                stage.eatGeese(); // Update game state statistics
            }
            stage.setFoxCaptured(true);
        }

        // Add the movement action to the list
        actions.addAll(ActionFactory.generateMoveWithinContainer(control, model, fox, bestMove[0], bestMove[1]));

        stage.setFoxCoo(bestMove[0], bestMove[1]);
        actions.setDoEndOfTurn(true);
        return actions;
    }
}

