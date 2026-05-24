package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.*;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import model.Board;
import model.HoleStageModel;
import model.Pawn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class HoleController extends Controller {

    BufferedReader consoleIn;
    boolean firstPlayer;

    public HoleController(Model model, View view) {
        super(model, view);
        firstPlayer = true;
    }

    /**
     * Defines what to do within the single stage of the single party
     * It is pretty straight forward to write :
     */
    public void stageLoop() {
        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
        update();
        while (!model.isEndStage()) {
            int whoWon = partyWinned(gameStage.getFoxRow(), gameStage.getFoxCol());

            if (whoWon == 1) {
                System.out.println("Fox won!");
                model.setIdWinner(0);
                model.stopStage();
            } else if (whoWon == 2) {
                model.setIdWinner(1);
                model.stopStage();
            } else if (whoWon == 0) {
                stageInnerLoop(gameStage);
                endOfTurn();
            }
        }
        endGame();
    }

    // Inner stage loop for if the fox do multiple captures
    private void stageInnerLoop(HoleStageModel gameStage) {
        do {
            playTurn();
            if (gameStage.isFoxCaptured()) {
                Board board = gameStage.getBoard();
                Pawn fox = (Pawn) board.getFirstElement(gameStage.getFoxRow(), gameStage.getFoxCol());

                if (!board.foxCanCapture(fox, gameStage.getFoxRow(), gameStage.getFoxCol())) {
                    gameStage.setFoxCaptured(false);
                    break;
                }

                System.out.println("Another capture is possible, it's still" + model.getCurrentPlayer().getName() + "turn!");
            }
        } while (gameStage.isFoxCaptured());
    }

    private void playTurn() {
        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            System.out.println("COMPUTER PLAYS");

            ActionList actions;
            if (model.getIdPlayer() == 0) {
                HoleDecider decider = new HoleDecider(model, this);
                actions = decider.decide();
            } else {
                GooseDecider decider = new GooseDecider(model, this);
                actions = decider.decide();
            }

            actions.setDoEndOfTurn(false);
            ActionPlayer play = new ActionPlayer(model, this, actions);
            play.start();
            update();
        } else {
            boolean ok = false;
            while (!ok) {
                System.out.print(p.getName() + " > ");
                try {
                    String line = consoleIn.readLine();
                    if (line.length() == 4 || line.length() == 2) {
                        ok = analyseAndPlay(line);
                    }
                    if (!ok) {
                        System.out.println("incorrect instruction. retry !");
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void endOfTurn() {

        model.setNextPlayer();
        // get the new player to display its name
        Player p = model.getCurrentPlayer();
        HoleStageModel stageModel = (HoleStageModel) model.getGameStage();
        stageModel.getPlayerName().setText(p.getName());
    }

    private boolean analyseAndPlay(String line) {
        if (line.equalsIgnoreCase("STOP")) {
            model.stopStage();
            return true;
        }

        int currentPlayer = model.getIdPlayer();

        if (currentPlayer == 0) {
            // Fox : Only 2 characters needed (destination)
            if (line.length() != 2) {
                System.out.println("needed format : 2 characters (ex: C3)");
                return false;
            }
            return foxPlay(line);

        } else {
            // Geese : 4 characters (start + end)
            if (line.length() != 4) {
                System.out.println("needed format : 4 characters (ex: E3D3)");
                return false;
            }
            return geesePlay(line);
        }
    }

    private boolean foxPlay(String line) {
        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();
        gameStage.setFoxCaptured(false);

        // start is saved in the model
        int fromR = gameStage.getFoxRow();
        int fromC = gameStage.getFoxCol();

        // destination set by the player
        int toR = line.charAt(0) - 'A';
        int toC = line.charAt(1) - '1';

        if (toR < 0 || toR >= 7 || toC < 0 || toC >= 7) {
            System.out.println("incorrect coordinates !");
            return false;
        }

        GameElement element = board.getFirstElement(fromR, fromC);
        if (element == null) {
            System.out.println("Error : Fox unfound !");
            return false;
        }
        Pawn fox = (Pawn) element;

        board.setValidCells(fox, fromR, fromC);
        if (!board.getReachableCells()[toR][toC]) {
            System.out.println("impossible move !");
            return false;
        }

        // update fox coordinates
        gameStage.setFoxCoo(toR, toC);

        ActionList actions = new ActionList();

        // if a geese is taken
        if (Math.abs(toC - fromC) == 2 || Math.abs(toR - fromR) == 2) {
            GameElement geeseToEat = board.getFirstElement((fromR + toR) / 2, (fromC + toC) / 2);
            ActionList removeAction = ActionFactory.generateRemoveFromStage(model, geeseToEat);
            actions.addAll(removeAction);
            gameStage.eatGeese();

            gameStage.setFoxCaptured(true); //flag update for multi-captures
        }
        actions.addAll(ActionFactory.generateMoveWithinContainer(model, fox, toR, toC));

        actions.setDoEndOfTurn(false);
        ActionPlayer player = new ActionPlayer(model, this, actions);
        player.start();
        update();

        return true;
    }

    private boolean geesePlay(String line) {
        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();

        // start and end coordinates
        int fromR = line.charAt(0) - 'A';
        int fromC = line.charAt(1) - '1';
        int toR = line.charAt(2) - 'A';
        int toC = line.charAt(3) - '1';

        if (fromR < 0 || fromR >= 7 || fromC < 0 || fromC >= 7 ||
                toR < 0 || toR >= 7 || toC < 0 || toC >= 7) {
            System.out.println("error into coordinates !");
            return false;
        }

        GameElement element = board.getFirstElement(fromR, fromC);
        if (element == null) {
            System.out.println("There is no goose here !");
            return false;
        }
        Pawn goose = (Pawn) element;

        if (!goose.isGoose()) {
            System.out.println("It's geese turn !");
            return false;
        }
        // debug output
        System.out.println("Valid cells for the goose at " + fromR + "," + fromC + " :");
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                System.out.print(board.getReachableCells()[r][c] ? "1" : "0");
            }
            System.out.println();
        }

        //System.out.println("Requested cell [" + toR + "][" + toC + "] = " + board.getReachableCells()[toR][toC]);
        board.setValidCells(goose, fromR, fromC);
        if (!board.getReachableCells()[toR][toC]) {
            System.out.println("too far");
            return false;
        }

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, goose, toR, toC);
        actions.setDoEndOfTurn(false);
        ActionPlayer player = new ActionPlayer(model, this, actions);
        player.start();
        update();

        return true;
    }


    private int partyWinned(int row, int col) {
        int whoWon = 0;

        HoleStageModel gameStage =
                (HoleStageModel) model.getGameStage();

        Board board = gameStage.getBoard();

        if (gameStage.getGeeseToPlay() < 4) {
            whoWon = 1;
        } else {
            Pawn fox =
                    (Pawn) board.getFirstElement(row, col);

            int reachableCells =
                    board.setValidCells(fox, row, col);

            System.out.println("reachableCells = " + reachableCells);

            for (int r = 0; r < 7; r++) {
                for (int c = 0; c < 7; c++) {
                    System.out.print(
                            board.getReachableCells()[r][c]
                                    ? "1 "
                                    : "0 "
                    );
                }
                System.out.println();
            }

            if (reachableCells == 0) {
                whoWon = 2;
            }
        }

        return whoWon;
    }
}