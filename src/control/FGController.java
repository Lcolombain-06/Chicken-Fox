package control;

import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.view.View;
import model.Board;
import model.FGStageModel;
import model.Pawn;
import view.FGRootPane;

public class FGController extends Controller {

    // Référence à control.MainApp pour afficher la fin de partie
    private MainApp mainApp;

    public FGController(Model model, View view) {
        super(model, view);
        FGControllerMouse mouseController = new FGControllerMouse(model, view, this);
        setControlMouse(mouseController);
    }

    /**
     * Permet à control.MainApp de s'enregistrer pour recevoir les événements de fin de partie.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void endOfTurn() {
        // Désélectionner tout
        for (boardifier.model.GameElement e : model.getGameStage().getElements()) {
            if (e.isSelected()) e.unselect();
        }

        FGStageModel stage = (FGStageModel) model.getGameStage();

        // Vérifier la victoire
        int whoWon = checkVictory(stage);
        if (whoWon != 0) {
            String winnerName;
            if (whoWon == 1) {
                winnerName = model.getPlayers().get(0).getName(); // Fox
                model.setIdWinner(0);
            } else {
                winnerName = model.getPlayers().get(1).getName(); // Geese
                model.setIdWinner(1);
            }
            model.stopStage();

            // Afficher la fenêtre de fin de partie via control.MainApp
            if (mainApp != null) mainApp.showEndGame(winnerName);
            return;
        }

        // Changer de joueur
        model.setNextPlayer();
        Player p = model.getCurrentPlayer();
        stage.getPlayerName().setText(p.getName());

        // Mettre à jour le label JavaFX du joueur courant
        if (mainApp != null) {
            FGRootPane rootPane = mainApp.getFGRootPane();
            rootPane.setCurrentPlayer(p.getName(), model.getIdPlayer() == 0);
        }

        // Sélectionner le renard visuellement si c'est son tour
        if (model.getIdPlayer() == 0) {
            stage.getFox()[0].select();
        }

        // Lancer l'IA si nécessaire
        if (p.getType() == Player.COMPUTER) {
            System.out.println("COMPUTER PLAYS");
            if (model.getIdPlayer() == 0) {
                FoxDecider decider = new FoxDecider(model, this);
                ActionPlayer play = new ActionPlayer(model, this, decider, null);
                play.start();
            } else {
                GooseDecider decider = new GooseDecider(model, this);
                ActionPlayer play = new ActionPlayer(model, this, decider, null);
                play.start();
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