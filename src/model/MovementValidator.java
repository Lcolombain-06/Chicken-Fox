package model;

import java.awt.Point;

/**
 * Classe qui regroupe toutes les règles de déplacement.
 * Elle répond à deux questions principales :
 *   1. "Est-ce que ce déplacement est autorisé ?" → isValidMove()
 *   2. "Y a-t-il un saut (capture) entre deux cases ?" → isJump() / getCapturedPosition()
 */


public class MovementValidator {

    // Constantes internes

    /** Distance d'un déplacement */
    private static final int STEP = 1;

    /** Distance d'un saut par-dessus une pièce adverse (2 cases). */
    private static final int JUMP = 2;


    // Méthode principale

    /**
     * Vérifie si une pièce (pawn) a le droit de se déplacer vers (toRow, toCol).
     *
     * Règles rappelées :
     * - La case d'arrivée doit exister sur le plateau et être accessible.
     * - La case d'arrivée doit être vide (on ne peut pas superposer deux pièces).
     * - Les poules (GOOSE) ne peuvent qu'avancer (vers le bas = row croissant)
     *   ou se déplacer latéralement, jamais en diagonale.
     * - Le renard (FOX) peut aller dans toutes les directions accessibles depuis
     *   sa case, y compris sauter par-dessus une poule pour la capturer
     *   (mais PAS par-dessus un autre renard).
     */
    public static boolean isValidMove(Board board, Pawn pawn, int toRow, int toCol) {
        // Trouver la position actuelle de la pièce sur le plateau
        int[] coords = board.getElementCell(pawn);
        if (coords == null) return false;   // la pièce n'est pas sur le plateau

        int fromRow = coords[0];
        int fromCol = coords[1];

        int deltaRow = toRow - fromRow;
        int deltaCol = toCol - fromCol;

        // --- Vérifications communes ---

        // La case cible doit être dans les limites du plateau (7x7)
        if (toRow < 0 || toRow >= 7 || toCol < 0 || toCol >= 7) return false;

        // La case cible doit être accessible (pas un coin inaccessible du plateau)
        Cell target = board.getCell(toRow, toCol);
        if (!target.isAccessible()) return false;

        // La case cible doit être vide
        if (board.getElement(toRow, toCol) != null) {
            // Cas particulier : le renard peut "passer par-dessus" une case occupée
            // s'il effectue un saut. Le saut lui-même atterrit sur une case vide.
            // Ici la case cible est occupée → invalide dans tous les cas.
            return false;
        }

        // --- Règles spécifiques à chaque type de pièce ---

        if (pawn.isFox()) {
            return isValidFoxMove(board, fromRow, fromCol, toRow, toCol, deltaRow, deltaCol);
        } else {
            return isValidGooseMove(fromRow, fromCol, toRow, toCol, deltaRow, deltaCol);
        }
    }


    // Déplacement du renard

    /**
     * Vérifie si le renard peut aller de (fromRow,fromCol) à (toRow,toCol).
     *
     * Le renard peut :
     *   - Se déplacer d'une case dans toute direction autorisée par ses voisins.
     *   - Sauter par-dessus une POULE (pas un autre renard) pour la capturer,
     *     à condition que la case d'atterrissage soit libre et accessible.
     *
     * Un "saut" correspond à un déplacement de 2 cases dans une direction.
     */
    private static boolean isValidFoxMove(Board board,
                                          int fromRow, int fromCol,
                                          int toRow,   int toCol,
                                          int deltaRow, int deltaCol) {
        int absDR = Math.abs(deltaRow);
        int absDC = Math.abs(deltaCol);

        // --- Déplacement simple (1 case) ---
        if (absDR <= STEP && absDC <= STEP && (absDR + absDC) > 0) {
            // Vérifier que la case cible est bien un voisin direct
            Cell fromCell = board.getCell(fromRow, fromCol);
            Cell toCell   = board.getCell(toRow, toCol);
            return fromCell.getNeighbors().contains(toCell);
        }

        // --- Saut de 2 cases (capture d'une poule) ---
        if (absDR == JUMP || absDC == JUMP) {
            // Les deux deltas doivent être pairs et de même parité
            if (absDR != 0 && absDR != JUMP) return false;
            if (absDC != 0 && absDC != JUMP) return false;

            // Case intermédiaire (la pièce que l'on saute)
            int midRow = fromRow + deltaRow / 2;
            int midCol = fromCol + deltaCol / 2;

            // Il doit y avoir quelque chose à cet endroit
            Object mid = board.getElement(midRow, midCol);
            if (mid == null) return false;

            // On ne peut sauter que par-dessus une POULE, pas un autre renard
            if (!(mid instanceof Pawn)) return false;
            Pawn midPawn = (Pawn) mid;
            if (midPawn.isFox()) return false;  // interdit de sauter un renard

            // La case d'arrivée doit être accessible (déjà vérifiée) et voisin
            // du milieu : vérifier que le chemin est cohérent avec les voisins
            Cell fromCell = board.getCell(fromRow, fromCol);
            Cell midCell  = board.getCell(midRow,  midCol);
            Cell toCell   = board.getCell(toRow,   toCol);

            return fromCell.getNeighbors().contains(midCell)
                    && midCell.getNeighbors().contains(toCell);
        }

        return false;  // déplacement de plus de 2 cases → toujours invalide
    }


    // Déplacement d'une poule

    /**
     * Vérifie si une poule peut aller de (fromRow,fromCol) à (toRow,toCol).
     *
     * La poule peut :
     *   - Avancer d'une case (row+1 = vers le bas).
     *   - Se déplacer latéralement d'une case (même row, col±1).
     *
     * La poule NE peut PAS :
     *   - Reculer (row-1).
     *   - Se déplacer en diagonale.
     *   - Sauter par-dessus une autre pièce.
     */
    private static boolean isValidGooseMove(int fromRow, int fromCol,
                                            int toRow,   int toCol,
                                            int deltaRow, int deltaCol) {
        int absDR = Math.abs(deltaRow);
        int absDC = Math.abs(deltaCol);

        // Mouvement de exactement 1 case
        if (absDR > STEP || absDC > STEP) return false;
        if (absDR == 0 && absDC == 0)     return false;  // pas de mouvement

        // Pas de diagonale : il faut que l'un des deux deltas soit 0
        if (absDR != 0 && absDC != 0) return false;  // diagonal

        // Pas de recul : deltaRow doit être ≥ 0 (la poule ne peut pas remonter)
        if (deltaRow < 0) return false;

        return true;
    }

    // ---------------------------------------------------------------
    // Détection et localisation des sauts
    // ---------------------------------------------------------------

    /**
     * Indique si le mouvement de (fromRow,fromCol) à (toRow,toCol)
     * est un saut (déplacement de 2 cases dans au moins une direction).
     *
     * Exemples :
     *   isJump(5,3 → 3,3) = true  (saut vertical de 2 cases vers le haut)
     *   isJump(3,3 → 4,3) = false (déplacement simple)
     *
     * @return true s'il s'agit d'un saut, false sinon.
     */
    public static boolean isJump(int fromRow, int fromCol, int toRow, int toCol) {
        int absDR = Math.abs(toRow - fromRow);
        int absDC = Math.abs(toCol - fromCol);
        return absDR == JUMP || absDC == JUMP;
    }

    /**
     * Renvoie la position de la pièce "capturée" lors d'un saut,
     * c'est-à-dire la case intermédiaire entre la case de départ et la case d'arrivée.
     *
     * Si ce n'est pas un saut (déplacement simple), renvoie null.
     *
     * Exemple :
     *   getCapturedPosition(5,3 → 3,3) renvoie Point(col=3, row=4)
     *   getCapturedPosition(4,2 → 2,4) renvoie Point(col=3, row=3)
     *
     * Note : le Point retourné suit la convention (x=col, y=row).
     *
     * @param fromRow Ligne de départ.
     * @param fromCol Colonne de départ.
     * @param toRow   Ligne d'arrivée.
     * @param toCol   Colonne d'arrivée.
     * @return La position intermédiaire (case capturée), ou null si ce n'est pas un saut.
     */
    public static Point getCapturedPosition(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isJump(fromRow, fromCol, toRow, toCol)) return null;

        int midRow = (fromRow + toRow) / 2;
        int midCol = (fromCol + toCol) / 2;

        // Convention Point : x = colonne, y = ligne
        return new Point(midCol, midRow);
    }
}