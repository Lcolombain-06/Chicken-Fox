package control;

import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.view.View;
import model.Board;
import model.FGStageModel;
import model.Pawn;

/**
 * FGController — contrôleur principal du jeu Fox & Geese (version graphique).
 *
 * DIFFÉRENCE MAJEURE avec la version console :
 *   Avant : FGController avait une stageLoop() avec des readLine() bloquants.
 *   Maintenant : FGController implémente nextPlayer(), appelé AUTOMATIQUEMENT
 *   par ActionPlayer après chaque coup (quand doEndOfTurn=true dans l'ActionList).
 *
 * FLUX D'EXÉCUTION :
 *   1. Le joueur clique → FGControllerMouse.handle()
 *   2. FGControllerMouse crée une ActionList avec doEndOfTurn=true
 *   3. ActionPlayer joue les actions (déplace le pion, supprime l'oie, etc.)
 *   4. ActionPlayer voit doEndOfTurn=true → appelle nextPlayer() ici
 *   5. nextPlayer() change le joueur et lance l'IA si besoin
 *   6. Si IA, l'IA crée sa propre ActionList → ActionPlayer → nextPlayer() à nouveau
 *
 *
 */
public class FGController extends Controller {
    FGControllerMouse mouseController;

    public FGController(Model model, View view) {
        super(model, view);

        // Instancier et enregistrer le contrôleur souris dans Boardifier
        // C'est Boardifier qui branchera automatiquement les événements JavaFX
        FGControllerMouse mouseController = new FGControllerMouse(model, view, this, 700);
        this.mouseController = mouseController;
    }

    /**
     * nextPlayer() — appelé automatiquement par ActionPlayer après chaque coup.
     *
     * C'est ici que vit toute la logique d'alternance :
     *   - vérifier la victoire
     *   - changer de joueur
     *   - lancer l'IA si c'est son tour
     */
    @Override
    public void nextPlayer() {

        // --- 1. Vérifier les conditions de victoire AVANT de changer de joueur ---
        FGStageModel stage = (FGStageModel) model.getGameStage();

        int whoWon = checkVictory(stage);
        if (whoWon == 1) {
            System.out.println("Le Renard a gagné !");
            model.setIdWinner(0);
            model.stopStage();
            return;
        } else if (whoWon == 2) {
            System.out.println("Les Oies ont gagné !");
            model.setIdWinner(1);
            model.stopStage();
            return;
        }

        // --- 2. Changer de joueur ---
        model.setNextPlayer();
        Player p = model.getCurrentPlayer();
        stage.getPlayerName().setText(p.getName());
        System.out.println("Tour de : " + p.getName());

        // --- 3. Si c'est l'IA, jouer automatiquement ---
        if (p.getType() == Player.COMPUTER) {
            System.out.println("COMPUTER PLAYS");

            // L'IA choisit son coup via le Decider approprié
            // ActionPlayer appellera nextPlayer() à nouveau quand l'IA aura joué
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
        // Si c'est un humain, on ne fait rien : on attend le prochain clic souris
    }

    /**
     * Vérifie les conditions de victoire.
     * @return 0 = jeu continue, 1 = Renard gagne, 2 = Oies gagnent
     */
    private int checkVictory(FGStageModel stage) {
        // Le renard gagne s'il a mangé assez d'oies
        if (stage.getGeeseToPlay() < 4) return 1;

        // Les oies gagnent si le renard n'a plus de mouvement possible
        Board board = stage.getBoard();
        Pawn fox = stage.getFox()[0];
        int moves = board.setValidCells(fox, stage.getFoxRow(), stage.getFoxCol());
        if (moves == 0) return 2;

        return 0;
    }
}
