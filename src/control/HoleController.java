package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.*;
import boardifier.model.action.ActionList;
import boardifier.view.ContainerLook;
import boardifier.view.ElementLook;
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
        while(! model.isEndStage()) {
            int whoWon = partyWinned(gameStage.getFoxRow(), gameStage.getFoxCol());

            if (whoWon == 1) {
                System.out.println("Fox won!");
                model.setIdWinner(0);
                model.stopStage();
            }

            else if (whoWon == 2) {
                model.setIdWinner(1);
                model.stopStage();
            }

            else if (whoWon == 0) {
                playTurn();
                endOfTurn();
            }
        }
        endGame();
    }

    private void playTurn() {
        // get the new player
        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            System.out.println("COMPUTER PLAYS");
            HoleDecider decider = new HoleDecider(model,this);
            ActionPlayer play = new ActionPlayer(model, this, decider, null);
            play.start();
        }
        else {
            boolean ok = false;
            while (!ok) {
                System.out.print(p.getName()+ " > ");
                try {
                    String line = consoleIn.readLine();
                    if (line.length() == 4) {
                        ok = analyseAndPlay(line);

                    }
                    if (!ok) {
                        System.out.println("incorrect instruction. retry !");
                    }
                }
                catch(IOException e) {}
            }
        }
    }

    public void endOfTurn() {

        model.setNextPlayer();
        // get the new player to display its name
        Player p = model.getCurrentPlayer();
        HoleStageModel stageModel = (HoleStageModel) model.getGameStage();
        stageModel.getPlayerName().setText(p.getName());
    }
    private boolean analyseAndPlay(String line) {
        //System.out.println("line reçue : '" + line + "' longueur : " + line.length());
        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();

        if (line.equals("STOP")){
            model.stopStage();
            return true;
        }
        // Read the coordonates
        int fromR = line.charAt(0) - 'A';
        int fromC = line.charAt(1) - '1';
        int toR   = line.charAt(2) - 'A';
        int toC   = line.charAt(3) - '1';
        //System.out.println("fromR=" + fromR + " fromC=" + fromC + " toR=" + toR + " toC=" + toC);

        if (fromR < 0 || fromR >= 7 || fromC < 0 || fromC >= 7 ||
                toR   < 0 || toR   >= 7 || toC   < 0 || toC   >= 7) {
            System.out.println("These cells cannot be reached !");
            return false;
        }

        // Find the real pawn
        GameElement element = board.getFirstElement(fromR,fromC);


        if (element == null) {
            System.out.println("There is no pawn here !");
            return false;
        }
        Pawn pawn = (Pawn) element;

        // 4. Vérifier que le pion appartient au joueur courant
        int currentPlayer = model.getIdPlayer();
        if (currentPlayer == 0 && !pawn.isFox()) {
            System.out.println("It's the fox turn");
            return false;
        }
        if (currentPlayer == 1 && !pawn.isGoose()) {
            System.out.println("Its Goose turn");
            return false;
        }

        board.setValidCells(pawn, fromR, fromC);
        if (!board.getReachableCells()[toR][toC]){
            System.out.println("You can't move there !");
            return false;
        }

        //update fox coordinates
        if (currentPlayer == 0) {
            gameStage.setFoxCoo(toR, toC);
        }

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, toR, toC);

        if (Math.abs(toC-fromC) == 2 || Math.abs(toR-fromR) == 2){
            GameElement geeseToEat = board.getFirstElement((fromR+toR)/2, (fromC+toC)/2);
            ActionList removeAction = ActionFactory.generateRemoveFromContainer(model, geeseToEat);
            actions.addAll(removeAction);



        }


        actions.setDoEndOfTurn(false);
        ActionPlayer player = new ActionPlayer(model, this, actions);

        ContainerLook boardLook = (ContainerLook) getElementLook(board);
        //System.out.println("boardLook = " + boardLook);
        ElementLook pawnLook = getElementLook(pawn);
        //System.out.println("pawnLook = " + pawnLook);

        player.start();
        update();
        return true;

    }

    private int partyWinned (int row, int col) {
        // 0 = no one | 1 = Fox | 2 = Geese
        int whoWon = 0;

        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();

        //System.out.println("(" + row + "; " + col + ")");

        if (gameStage.getGeeseToPlay() < 4) {
            whoWon = 1;
        }

        else {
            Pawn fox = (Pawn) board.getFirstElement(row, col);
            int reachableCells = board.setValidCells(fox, row, col);
            if (reachableCells == 0) {
                whoWon = 2;
            }

            //System.out.println("reachableCells --> " + reachableCells);
        }

        return whoWon;
    }


}