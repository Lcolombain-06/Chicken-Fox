package control;

import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.view.View;
import model.Board;
import model.FGStageModel;
import model.Pawn;

public class FGController extends Controller {

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

    /**
     * Surcharge de stopGame() : on nettoie l'état du handler souris AVANT
     * que boardifier détruise le stage, pour éviter que des clics résiduels
     * sur la scène déclenchent handle() avec model.getGameStage() == null.
     */
    @Override
    public void stopGame() {
        // Réinitialiser la file et la sélection du handler souris
        if (mouseController != null) {
            mouseController.reset();
        }
        // Laisser boardifier faire son nettoyage (stop AnimationTimer, etc.)
        super.stopGame();
    }

    @Override
    public void endOfTurn() {
        // CORRECTION : si la partie a déjà été arrêtée entre-temps, on ignore
        if (model.getGameStage() == null) return;

        // Désélectionner tout
        for (GameElement e : model.getGameStage().getElements()) {
            if (e.isSelected()) e.unselect();
        }

        FGStageModel stage = (FGStageModel) model.getGameStage();

        // Vérifier la victoire
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

        // Changer de joueur
        model.setNextPlayer();
        Player p = model.getCurrentPlayer();
        stage.getPlayerName().setText(p.getName());

        // Mettre à jour le label via l'interface
        if (gameEndListener != null) {
            gameEndListener.updateCurrentPlayer(p.getName(), model.getIdPlayer() == 0);
        }

        // Sélectionner le renard si c'est son tour
        if (model.getIdPlayer() == 0) {
            stage.getFox()[0].select();
        }

        // Lancer l'IA si nécessaire
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