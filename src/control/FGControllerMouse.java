package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.control.ControllerMouse;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import boardifier.view.ElementLook;
import boardifier.view.View;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.Board;
import model.FGStageModel;
import model.Pawn;

import java.util.ArrayList;
import java.util.List;

public class FGControllerMouse extends ControllerMouse {

    // File de coups planifiés pour le renard (clic droit)
    private final List<int[]> foxMoveQueue = new ArrayList<>();

    // État du clic en deux temps pour les oies
    private Pawn selectedPawn = null;

    public FGControllerMouse(Model model, View view, Controller control) {
        super(model, view, control);
    }

    @Override
    public void handle(MouseEvent event) {
        FGStageModel stage = (FGStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // Récupérer la position réelle du BoardLook dans la scène
        ElementLook boardLook = control.getElementLook(board);
        if (boardLook == null) return;

        Bounds b = boardLook.getGroup().localToScene(boardLook.getGroup().getBoundsInLocal());
        double boardX = b.getMinX();
        double boardY = b.getMinY();
        double cellW  = b.getWidth()  / 7.0;
        double cellH  = b.getHeight() / 7.0;

        double relX = event.getX() - boardX;
        double relY = event.getY() - boardY;

        int col = (int)(relX / cellW);
        int row = (int)(relY / cellH);

        if (col < 0 || col > 6 || row < 0 || row > 6) return;

        int currentPlayer = model.getIdPlayer();

        if (currentPlayer == 0) {
            if (event.getButton() == MouseButton.SECONDARY) {
                // Clic droit → ajouter la case à la file de planification
                handleFoxPlan(stage, board, row, col);
            } else if (event.getButton() == MouseButton.PRIMARY) {
                // Clic gauche → confirmer et exécuter la séquence planifiée
                handleFoxConfirm(stage, board);
            }
        } else {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleGooseTurn(stage, board, row, col);
            }
        }
    }

    /**
     * Clic droit : ajoute une case à la file de déplacements du renard.
     */
    private void handleFoxPlan(FGStageModel stage, Board board, int row, int col) {
        foxMoveQueue.add(new int[]{row, col});
        System.out.println("Case ajoutée à la file : [" + row + "," + col + "] — file : " + foxMoveQueue.size() + " coup(s)");
    }

    /**
     * Clic gauche : confirme et exécute la séquence planifiée.
     *
     * Logique :
     * 1. Vérifier le premier coup — si invalide, annuler toute la séquence
     * 2. Exécuter le coup
     * 3. Si c'est une prise, vérifier le coup suivant dans la liste
     * 4. Si le suivant est une prise valide, l'exécuter aussi
     * 5. Continuer jusqu'à fin de liste, coup non-prise, ou coup invalide
     */
    private void handleFoxConfirm(FGStageModel stage, Board board) {
        if (foxMoveQueue.isEmpty()) {
            System.out.println("Aucun coup planifié. Utilisez le clic droit pour planifier.");
            return;
        }

        Pawn fox = stage.getFox()[0];
        int currentRow = stage.getFoxRow();
        int currentCol = stage.getFoxCol();

        // Vérifier et exécuter chaque coup de la file
        for (int i = 0; i < foxMoveQueue.size(); i++) {
            int[] move = foxMoveQueue.get(i);
            int toRow = move[0];
            int toCol = move[1];

            // Calculer les cases valides depuis la position actuelle
            board.setValidCells(fox, currentRow, currentCol);

            if (!board.getReachableCells()[toRow][toCol]) {
                // Coup invalide → annuler toute la séquence
                System.out.println("Coup invalide en [" + toRow + "," + toCol + "] — séquence annulée.");
                foxMoveQueue.clear();
                board.clearValidCells();
                return;
            }

            boolean isCapture = Math.abs(toRow - currentRow) == 2 || Math.abs(toCol - currentCol) == 2;

            // Si ce n'est pas le premier coup et ce n'est pas une prise → arrêter
            if (i > 0 && !isCapture) {
                System.out.println("Coup non-prise après une capture en [" + toRow + "," + toCol + "] — séquence arrêtée.");
                break;
            }

            // Construire et exécuter l'action
            ActionList actions = new ActionList();

            if (isCapture) {
                GameElement geeseToEat = board.getFirstElement(
                        (currentRow + toRow) / 2,
                        (currentCol + toCol) / 2);
                if (geeseToEat != null) {
                    actions.addAll(ActionFactory.generateRemoveFromStage(model, geeseToEat));
                    stage.eatGeese();
                }
                stage.setFoxCaptured(true);
            } else {
                stage.setFoxCaptured(false);
            }

            actions.addAll(ActionFactory.generateMoveWithinContainer(control, model, fox, toRow, toCol));
            stage.setFoxCoo(toRow, toCol);

            // Dernier coup de la liste → fin du tour
            boolean isLastMove = (i == foxMoveQueue.size() - 1);
            actions.setDoEndOfTurn(isLastMove);

            new ActionPlayer(model, control, actions).start();
            System.out.println("Coup exécuté : [" + currentRow + "," + currentCol + "] → [" + toRow + "," + toCol + "]");

            // Mettre à jour la position courante pour le prochain coup
            currentRow = toRow;
            currentCol = toCol;

            // Si ce n'est pas une prise, on s'arrête après ce coup
            if (!isCapture) break;
        }

        foxMoveQueue.clear();
        board.clearValidCells();
    }

    private void handleGooseTurn(FGStageModel stage, Board board, int row, int col) {
        if (selectedPawn == null) {
            GameElement e = board.getElement(row, col);
            if (e == null || !(e instanceof Pawn) || !((Pawn) e).isGoose()) {
                System.out.println("Aucune oie ici.");
                return;
            }
            selectedPawn = (Pawn) e;
            int[] pos = board.getElementCell(selectedPawn);
            board.setValidCells(selectedPawn, pos[0], pos[1]);
            selectedPawn.select();
            System.out.println("Oie sélectionnée en [" + pos[0] + "," + pos[1] + "]");

        } else {
            int[] pos = board.getElementCell(selectedPawn);

            if (row == pos[0] && col == pos[1]) {
                selectedPawn.unselect();
                selectedPawn = null;
                board.clearValidCells();
                System.out.println("Oie désélectionnée.");
                return;
            }

            board.setValidCells(selectedPawn, pos[0], pos[1]);

            if (!board.getReachableCells()[row][col]) {
                System.out.println("Case non atteignable pour cette oie.");
                selectedPawn.unselect();
                selectedPawn = null;
                board.clearValidCells();
                return;
            }

            selectedPawn.unselect();
            ActionList actions = ActionFactory.generateMoveWithinContainer(control, model, selectedPawn, row, col);
            actions.setDoEndOfTurn(true);
            new ActionPlayer(model, control, actions).start();
            selectedPawn = null;
            board.clearValidCells();
        }
    }
}