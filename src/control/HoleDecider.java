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

    public HoleDecider(Model model, Controller control) {
        super(model, control);
    }


    public int[] chooseBestMove() { // plus ActionList

        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        Pawn fox = stage.getFox()[0];

        int[] pos = board.getElementCell(fox);
        int foxRow = pos[0];
        int foxCol = pos[1];

        board.setValidCells(fox, foxRow, foxCol);

        List<int[]> validMoves = new ArrayList<>();
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (board.getReachableCells()[r][c]) {
                    validMoves.add(new int[]{r, c});
                }
            }
        }

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        for (int[] move : validMoves) {
            int score = scoreMove(fox, foxRow, foxCol, move[0], move[1], board, stage);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        board.setValidCells(fox, foxRow, foxCol);

        board.resetReachableCells(false);
        return bestMove;
    }

    private int scoreMove(Pawn fox, int fromR, int fromC, int toR, int toC, Board board, HoleStageModel stage) {
        int score = 0;

        // 1. Priorité absolue aux captures
        if (Math.abs(toR - fromR) == 2 || Math.abs(toC - fromC) == 2) {
            score += 1000;


            boolean canCapture = board.foxCanCapture(fox, toR, toC);

            board.setValidCells(fox, fromR, fromC);
            if (canCapture) score += 1000;
        }

                // 2. Pondération position
                score += toR * 10;

                // 3. Bonus si poule isolée à portée depuis la case destination
                Cell destCell = board.getCell(toC, toR);
                for (Cell geese : destCell.getNeighbors()) {
                    int nx = geese.getX();
                    int ny = geese.getY();
                    GameElement e = board.getElement(ny, nx);
                    if (e != null && ((Pawn) e).isGoose()) {
                        // il y a une poule voisine, est-elle isolée ?
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

                board.resetReachableCells(false);
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

        ActionList actions = ActionFactory.generateMoveWithinContainer(
                model, fox, bestMove[0], bestMove[1]);

        if (Math.abs(bestMove[0] - foxRow) == 2 || Math.abs(bestMove[1] - foxCol) == 2) {
            GameElement geeseToEat = board.getFirstElement(
                    (foxRow + bestMove[0]) / 2,
                    (foxCol + bestMove[1]) / 2);
            if (geeseToEat != null) {
                actions.addAll(ActionFactory.generateRemoveFromContainer(model, geeseToEat));
                stage.eatGeese();
            }
            stage.setFoxCaptured(true);
        }

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

/*
// 6. Créer et retourner l'action
        ActionList actions = ActionFactory.generateMoveWithinContainer(
                model, fox, bestMove[0], bestMove[1]); // ← bestMove pas chosen

        // si c'est un saut, manger la poule
        if (Math.abs(bestMove[0] - foxRow) == 2 || Math.abs(bestMove[1] - foxCol) == 2) {
            GameElement geeseToEat = board.getFirstElement(
                    (foxRow + bestMove[0]) / 2,
                    (foxCol + bestMove[1]) / 2
            );
            ActionList removeAction = ActionFactory.generateRemoveFromContainer(model, geeseToEat);
            actions.addAll(removeAction);
            stage.eatGeese();
        }
stage.setFoxCoo(bestMove[0], bestMove[1]);
        actions.setDoEndOfTurn(true);
 */