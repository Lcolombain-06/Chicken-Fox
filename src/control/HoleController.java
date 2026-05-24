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

/**
 * Main game controller for the Fox and Geese game.
 * <p>
 * This class manages the main game loop, checks victory conditions,
 * switches players between turns, and processes both human console inputs
 * and computer AI actions.
 * </p>
 */
public class HoleController extends Controller {

    BufferedReader consoleIn;
    boolean firstPlayer;

    public HoleController(Model model, View view) {
        super(model, view);
        firstPlayer = true;
    }

    /**
     * Starts and controls the main game loop.
     * <p>
     * The loop runs as long as the game stage is active. It continuously checks
     * if a player has won. If not, it executes the next turn.
     * </p>
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
            }

            else if (whoWon == 2) {
                System.out.println("Geese won!");
                model.setIdWinner(1);
                model.stopStage();
            }

            else if (whoWon == 0) {
                stageInnerLoop(gameStage);
                endOfTurn();
            }
        }
        endGame();
    }

    /**
     * Manages multi-capture turns for the Fox.
     * <p>
     * If the fox captures a goose, the loop checks if another jump
     * is possible. If yes, the fox player keeps playing.
     * </p>
     *
     * @param gameStage The current stage model
     */
    private void stageInnerLoop(HoleStageModel gameStage) {
        do {
            playTurn();
            // If the fox just made a capture, check for chain captures
            if (gameStage.isFoxCaptured()) {
                Board board = gameStage.getBoard();
                Pawn fox = (Pawn) board.getFirstElement(gameStage.getFoxRow(), gameStage.getFoxCol());

                // Stop the chain if no more captures are legally possible
                if (!board.foxCanCapture(fox, gameStage.getFoxRow(), gameStage.getFoxCol())) {
                    gameStage.setFoxCaptured(false);
                    break;
                }

                System.out.println("Another capture is possible, it's still" + model.getCurrentPlayer().getName() + "turn!");
            }
        } while (gameStage.isFoxCaptured());
    }

    /**
     * Executes a single turn for the current player.
     * <p>
     * If the player is a computer, it calls the AI deciders.
     * If the player is human, it reads and parses input from the console.
     * </p>
     */
    private void playTurn() {
        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            System.out.println("COMPUTER PLAYS");

            ActionList actions;
            if (model.getIdPlayer() == 0) {
                // Fox AI strategy
                FoxDecider decider = new FoxDecider(model, this); // choix de la stratégie
                actions = decider.decide();
            } else {
                // Geese AI strategy
                GooseDecider decider = new GooseDecider(model, this);
                actions = decider.decide();
            }

            actions.setDoEndOfTurn(false);
            ActionPlayer play = new ActionPlayer(model, this, actions);
            play.start();
            update();
        }
        else {
            boolean ok = false;
            while (!ok) {
                System.out.print(p.getName() + " > ");
                try {
                    String line = consoleIn.readLine();
                    if (line.length() == 4 || line.length() == 2) {
                        ok = analyseAndPlay(line);
                    }
                    if (!ok) {
                        System.out.println("Incorrect instruction. Please retry!");
                    }
                }
                catch (IOException e) {
                    System.out.println("Error reading input.");
                }
            }
        }
    }

    /**
     * Handles game state mutations at the end of a player's turn.
     */
    @Override
    public void endOfTurn() {

        model.setNextPlayer();
        // get the new player to display its name
        Player p = model.getCurrentPlayer();
        HoleStageModel stageModel = (HoleStageModel) model.getGameStage();
        stageModel.getPlayerName().setText(p.getName());
    }


    /**
     * Parses the raw text command entered by a human player
     *
     * @param line The string input from console.
     * @return true if the command was legal and processed, false otherwise
     */
    private boolean analyseAndPlay(String line) {
        if (line.equalsIgnoreCase("STOP")) {
            model.stopStage();
            return true;
        }

        int currentPlayer = model.getIdPlayer();

        if (currentPlayer == 0) {
            // Fox : Only 2 characters needed (destination)
            if (line.length() != 2) {
                System.out.println("Required format : 2 characters (ex: C3)");
                return false;
            }
            return foxPlay(line);

        } else {
            // Geese : 4 characters (start + end)
            if (line.length() != 4) {
                System.out.println("Required format : 4 characters (ex: E3D3)");
                return false;
            }
            return geesePlay(line);
        }
    }

    /**
     * Validates and executes a human move command for the Fox.
     */
    private boolean foxPlay(String line) {
        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();
        gameStage.setFoxCaptured(false);

        // start is saved in the model
        int fromR = gameStage.getFoxRow();
        int fromC = gameStage.getFoxCol();

        // destination set by the player
        int toR = line.charAt(0) - 'A'; // Use the ASCII to get correct coordonate
        int toC = line.charAt(1) - '1';

        if (toR < 0 || toR >= 7 || toC < 0 || toC >= 7) {
            System.out.println("Incorrect coordinates !");
            return false;
        }

        GameElement element = board.getFirstElement(fromR, fromC);
        if (element == null) {
            System.out.println("Error : Fox not found !");
            return false;
        }
        Pawn fox = (Pawn) element;

        board.setValidCells(fox, fromR, fromC);
        if (!board.getReachableCells()[toR][toC]) {
            System.out.println("Impossible move !");
            return false;
        }

        // update fox coordinates
        gameStage.setFoxCoo(toR, toC);

        ActionList actions = new ActionList();

        // Check if the move is a jump over a goose
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

    /**
     * Validates and executes a human move command for a Goose.
     */
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
            System.out.println("Error into coordinates !");
            return false;
        }

        GameElement element = board.getFirstElement(fromR, fromC);
        if (element == null) {
            System.out.println("There is no goose here !");
            return false;
        }
        Pawn goose = (Pawn) element;

        if (!goose.isGoose()) {
            System.out.println("It's the geese's turn !");
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

        board.setValidCells(goose, fromR, fromC);
        if (!board.getReachableCells()[toR][toC]) {
            System.out.println("Target cell is too far or unreachable.");
            return false;
        }

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, goose, toR, toC);
        actions.setDoEndOfTurn(false);
        ActionPlayer player = new ActionPlayer(model, this, actions);
        player.start();
        update();

        return true;
    }

    /**
     * Evaluation routine checking victory flags state
     *
     * @param row The current row index of the Fox
     * @param col The current column index of the Fox
     * @return 0 if the game continues, 1 if the Fox wins, 2 if the Geese win
     */
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