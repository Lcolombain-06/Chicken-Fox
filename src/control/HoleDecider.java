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

    @Override
    public ActionList decide() {

        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // Fox strategie :

        Pawn fox = stage.getFox()[0];

        int[] pos = board.getElementCell(fox);
        int foxRow = pos[0];
        int foxCol = pos[1];

        board.setValidCells(fox, foxRow, foxCol);

        // 4. Collecter toutes les cases valides
        List<int[]> validMoves = new ArrayList<>();
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (board.getReachableCells()[r][c]) {
                    validMoves.add(new int[]{r, c});
                }
            }
        }

        // 5. Scorer chaque coup et choisir le meilleur
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        for (int[] move : validMoves) {
            int score = scoreMove(fox, foxRow, foxCol, move[0], move[1], board, stage);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

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
        return actions;
    }

    private int scoreMove(Pawn fox, int fromR, int fromC, int toR, int toC, Board board, HoleStageModel stage) {
        int score = 0;

        // 1. Priorité absolue aux captures
        if (Math.abs(toR - fromR) == 2 || Math.abs(toC - fromC) == 2) {
            score += 1000;
        }

        // 2. Pondération position : plus c'est bas, mieux c'est
        score += toR * 10;

        // 3. Bonus si poule isolée à portée depuis la case destination
        Cell destCell = board.getCell(toC, toR);
        for (Cell neighbor : destCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();
            GameElement e = board.getElement(ny, nx);
            if (e != null && ((Pawn) e).isGoose()) {
                // il y a une poule voisine, est-elle isolée ?
                int gooseNeighborCount = 0;
                for (Cell nn : neighbor.getNeighbors()) {
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



        return score;
    }
}
