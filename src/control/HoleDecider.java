package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.Board;
import model.HolePawnPot;
import model.HoleStageModel;
import model.Pawn;

import java.awt.*;
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
        // do a cast get a variable of the real type to get access to the attributes of HoleStageModel
        HoleStageModel stage = (HoleStageModel)model.getGameStage();
        HoleBoard board = stage.getBoard(); // get the board
        HolePawnPot pot = null; // the pot where to take a pawn
        GameElement pawn = null; // the pawn that is moved
        int rowDest = 0; // the dest. row in board
        int colDest = 0; // the dest. col in board

        if (model.getIdPlayer() == Pawn.PAWN_BLACK) {
            pot = stage.getBlackPot();
        }
        else {
            pot = stage.getRedPot();
        }

        for(int i=0;i<4;i++) {
            Pawn p = (Pawn)pot.getElement(i,0);
            // if there is a pawn in i.
            if (p != null) {
                // get the valid cells
                List<Point> valid = board.computeValidCells(p.getNumber());
                if (valid.size() != 0) {
                    // choose at random one of the valid cells
                    int id = loto.nextInt(valid.size());
                    pawn = p;
                    rowDest = valid.get(id).y;
                    colDest = valid.get(id).x;
                    break; // stop the loop
                }
            }
        }

        ActionList actions = ActionFactory.generatePutInContainer( model, pawn, "holeboard", rowDest, colDest);
        actions.setDoEndOfTurn(true); // after playing this action list, it will be the end of turn for current player.

        return actions;
    }

    public boolean verifierVictoirePoules() {
        Cell cellRenard = trouverPositionRenard();
        if (cellRenard == null) return false;

        // On récupère les voisins déjà filtrés par initNeighbors()
        for (Cell voisin : cellRenard.getNeighbors()) {


            // Si une case voisine est vide, le renard peut bouger
            if (voisin.isEmpty()) {
                return false;
            }


            // Si le voisin est une poule, on regarde si le renard peut sauter par-dessus
            if (voisin.hasPoule()) {
                if (peutSauterParDessus(cellRenard, voisin)) {
                    return false;
                }
            }
        }


        return true;
    }

    /**
     * Calcule si un saut est possible au-dessus d'une poule.
     */
    private boolean peutSauterParDessus(Cell renard, Cell poule) {
        // Calcul de la direction du saut
        int dx = poule.getX() - renard.getX();
        int dy = poule.getY() - renard.getY();

        // Coordonnées de la case d'atterrissage
        int cibleX = poule.getX() + dx;
        int cibleY = poule.getY() + dy;

        // Vérification des limites du tableau
        if (cibleX >= 0 && cibleX < 7 && cibleY >= 0 && cibleY < 7) {
            Cell cible = getCell(cibleX, cibleY);
            return cible.isAccessible() && cible.isEmpty();
        }

        return false;
    }

    /**
     * Parcourt le plateau pour localiser le renard.
     */
    private Cell trouverPositionRenard() {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                Cell c = getCell(x, y);
                if (c.isAccessible() && c.hasRenard()) {
                    return c;
                }
            }
        }
        return null;
    }
}
