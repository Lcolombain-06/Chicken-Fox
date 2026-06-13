package control;

import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.view.View;
import javafx.application.Platform;
import model.Board;
import model.FGStageModel;
import model.Pawn;

public class FGController extends Controller {

    // Interface instead of MainApp directly → no package dependency issue
    private GameEndListener gameEndListener;
    private FGControllerMouse mouseController;

    public FGController(Model model, View view) {
        super(model, view);
        mouseController = new FGControllerMouse(model, view, this);
        setControlMouse(mouseController);
    }

    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }

    @Override
    public void stopGame() {
        if (mouseController != null) {
            mouseController.reset();
        }
        super.stopGame();
    }

    @Override
    public void endOfTurn() {
        // Deselect everything
        for (GameElement e : model.getGameStage().getElements()) {
            if (e.isSelected()) e.unselect();
        }
        if (model.getGameStage() == null) return;

        FGStageModel stage = (FGStageModel) model.getGameStage();

        // Check for a victory
        int whoWon = checkVictory(stage);
        if (whoWon != 0) {
            String winnerName;
            if (whoWon == 1) {
                winnerName = model.getPlayers().get(0).getName();
                model.setIdWinner(0);
            } else {
                winnerName = model.getPlayers().get(1).getName();
                model.setIdWinner(1);
            }
            model.stopStage();
            if (gameEndListener != null) gameEndListener.showEndGame(winnerName);
            return;
        }

        // Switch player
        model.setNextPlayer();
        Player p = model.getCurrentPlayer();
        stage.getPlayerName().setText(p.getName());

        // Update the label via the interface
        if (gameEndListener != null) {
            gameEndListener.updateCurrentPlayer(p.getName(), model.getIdPlayer() == 0);
        }

        // Select the fox if it is its turn
        if (model.getIdPlayer() == 0) {
            stage.getFox()[0].select();
        }
        // Différer les select/unselect sur le prochain pulse JavaFX
        // pour ne pas interférer avec l'ActionPlayer encore en cours
        Platform.runLater(() -> {
            if (model.getGameStage() == null) return;

            // Désélectionner tout
            for (GameElement e : model.getGameStage().getElements()) {
                if (e.isSelected()) e.unselect();
            }

            // Sélectionner le renard si c'est son tour
            if (model.getIdPlayer() == 0) {
                stage.getFox()[0].select();
            }
        });

        // Launch the AI if necessary
        if (p.getType() == Player.COMPUTER) {
            System.out.println("COMPUTER PLAYS");
            if (model.getIdPlayer() == 0) {
                FoxDecider decider = new FoxDecider(model, this);
                new ActionPlayer(model, this, decider, null).start();
            } else {
                GooseDecider decider = new GooseDecider(model, this);
                new ActionPlayer(model, this, decider, null).start();
            }
        }
    }

    private int checkVictory(FGStageModel stage) {
        if (stage.getGeeseToPlay() < 4) return 1;
        Board board = stage.getBoard();
        Pawn fox = stage.getFox()[0];
        int moves = board.setValidCells(fox, stage.getFoxRow(), stage.getFoxCol());
        if (moves == 0) return 2;
        return 0;
    }
}