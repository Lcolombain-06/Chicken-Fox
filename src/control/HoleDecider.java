package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HoleDecider extends Decider {

    private static final Random loto = new Random(Calendar.getInstance().getTimeInMillis());

    private int lastRow = -1;
    private int lastCol = -1;

    public HoleDecider(Model model, Controller control) {
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

        // absolut priority to capturating
        if (Math.abs(toR - fromR) == 2 || Math.abs(toC - fromC) == 2) {
            score += 1000;
            boolean canCapture = board.foxCanCapture(fox, toR, toC);
            board.setValidCells(fox, fromR, fromC);
            if (canCapture) score += 1000;
        }

        int distCentreOrigine = Math.abs(fromR - 3) + Math.abs(fromC - 3);
        int distCentreDestination = Math.abs(toR - 3) + Math.abs(toC - 3);

        if (distCentreDestination < distCentreOrigine) {
            score += 15; // Bonus if it goes near the center for more angles of attack
        }

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


        // Bonus if the goose is alone and near to the destination
        Cell destCell = board.getCell(toC, toR);
        for (Cell geese : destCell.getNeighbors()) {
            int nx = geese.getX();
            int ny = geese.getY();
            GameElement e = board.getElement(ny, nx);
            if (e != null && ((Pawn) e).isGoose()) {

                int gooseNeighborCount = 0;
                for (Cell nn : geese.getNeighbors()) {
                    GameElement e2 = board.getElement(nn.getY(), nn.getX());
                    if (e2 != null && ((Pawn) e2).isGoose()) {
                        gooseNeighborCount++;
                    }
                }

                if (gooseNeighborCount <= 1) {
                    score += 50; // poule isolée, bonus

                }
            }

        }

        // No infinit game so we put a malus if the fox goes on a cell he was uselessly
        if (toR == lastRow && toC == lastCol) {

            if (score < 1000) {
                score -= 800;
            }
        }

        // add some noise in case the choice for the fox is blocked
        score += loto.nextInt(100);

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

