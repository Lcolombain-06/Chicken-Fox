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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class FoxEvadeDecider extends Decider {

    private static final Random loto = new Random(Calendar.getInstance().getTimeInMillis());

    private int lastRow = -1;
    private int lastCol = -1;

    public FoxEvadeDecider(Model model, Controller control) {
        super(model, control);
    }


    public int[] chooseBestMove() {

        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();
        Pawn fox = stage.getFox()[0];

        int[] pos = board.getElementCell(fox);
        int foxRow = pos[0];
        int foxCol = pos[1];

        // Calculate the empty cells
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

        for (int[] move : validMoves) {
            int score = scoreMove(fox, foxRow, foxCol, move[0], move[1], board, stage);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        int[] bestMove = bestMoves.get(loto.nextInt(bestMoves.size()));
        board.setValidCells(fox, foxRow, foxCol);

        board.resetReachableCells(false);

        // Choose a cell if the list is empty
        if (bestMoves.isEmpty()) {
            return validMoves.get(loto.nextInt(validMoves.size()));
        }
        return bestMove;
    }

    private int scoreMove(Pawn fox, int fromR, int fromC, int toR, int toC, Board board, HoleStageModel stage) {
        int score = 0;

        if (Math.abs(toR - fromR) == 2 || Math.abs(toC - fromC) == 2) {
            boolean canCapture = board.foxCanCapture(fox, toR, toC);

            // On remet l'état initial des cellules pour ne pas polluer la suite
            board.setValidCells(fox, fromR, fromC);

            if (canCapture) {
                score += 1500; // Gros bonus : s'il y a une poule gratuite, on la mange !
                return score;  // Pas besoin de calculer le reste, c'est le choix prioritaire
            }
        }

        // reward destination cells with lots of empty space around
        Cell destCell = board.getCell(toC, toR);
        for (Cell neighbor : destCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();
            GameElement e = board.getElement(ny, nx);
            if (e == null) {
                score += 10; // +10 for each empty escape route around the destination
            }
        }

        // away from geese's center of gravity
        Pawn[] geese = stage.getGeese();
        int totalRow = 0;
        int totalCol = 0;
        int activeGeeseCount = 0;

        for (Pawn goose : geese) {
            if (goose == null) continue;
            int[] pos = board.getElementCell(goose);
            if (pos == null) continue; // ignore captured geese

            totalRow += pos[0];
            totalCol += pos[1];
            activeGeeseCount++;
        }

        if (activeGeeseCount > 0) {
            int dangerRow = totalRow / activeGeeseCount;
            int dangerCol = totalCol / activeGeeseCount;

            int distanceToDanger = Math.abs(toR - dangerRow) + Math.abs(toC - dangerCol);
            score += distanceToDanger * 10; // stay far away from the pack
        }

        // Freedom of movement preferences
        int distCentreOrigine = Math.abs(fromR - 3) + Math.abs(fromC - 3);
        int distCentreDestination = Math.abs(toR - 3) + Math.abs(toC - 3);

        if (distCentreDestination < distCentreOrigine) {
            score += 15; // center provides more escape directions
        }

        if (toR > fromR) {
            score += 5; // preference for moving down if equal
        }

        // Anti infinite loop system
        if (toR == lastRow && toC == lastCol) {
            score -= 800; // Strong penalty to force alternative paths
        }



        // Add noise to break perfect math ties
        score += loto.nextInt(5);

        return score;
    }



    @Override
    public ActionList decide() {
        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();
        Pawn fox = stage.getFox()[0];
        int[] pos = board.getElementCell(fox);
        int foxRow = pos[0];
        int foxCol = pos[1];

        int[] bestMove = chooseBestMove();

        // keep last move in memory
        this.lastRow = foxRow;
        this.lastCol = foxCol;

        ActionList actions = new ActionList();

        if (Math.abs(bestMove[0] - foxRow) == 2 || Math.abs(bestMove[1] - foxCol) == 2) {
            GameElement geeseToEat = board.getFirstElement(
                    (foxRow + bestMove[0]) / 2,
                    (foxCol + bestMove[1]) / 2);
            if (geeseToEat != null) {
                actions.addAll(ActionFactory.generateRemoveFromStage(model, geeseToEat));
                stage.eatGeese();
            }
            stage.setFoxCaptured(true);
        }


        actions.addAll(ActionFactory.generateMoveWithinContainer(model, fox, bestMove[0], bestMove[1]));

        stage.setFoxCoo(bestMove[0], bestMove[1]);
        actions.setDoEndOfTurn(true);
        return actions;
    }


}


// Stratégie renard idée :
// 1. priorité absolue aux captures possible
// 2. priorité encore plus forte si plusieurs capture possible
// 3. pondération + si déplacement vers le bas
// 4. pondération + si poule isolée (scan du plateau pour orienter la direction)

