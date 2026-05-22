package model;

import model.Board;
import model.Cell;
import model.Pawn;
import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class BoardTest {

    private static class StubStageModel extends GameStageModel {
        public StubStageModel() {
            super("test", null);
        }

        @Override
        public StageElementsFactory getDefaultElementFactory() {
            return null;
        }
    }

    /**
     * Le plateau que l'on teste, recréé avant chaque test.
     */
    private Board board;

    /**
     * Méthode appelée avant chaque test.
     * Elle crée un plateau vide
     */
    @BeforeEach
    void setUp() {
        board = new Board(0, 0, new StubStageModel())
    }

    // Tests sur l'accessibilité des cases
    /**
     * Vérifie que les quatre coins du plateau (et les cases autour)
     * sont bien marqués comme inaccessibles.**/
    @Test
    void cornerCellsAreNotAccessible() {
        // Coins du plateau 7x7 : (row=0,col=0), (row=0,col=1), (row=1,col=0), (row=6,col=6)
        // Rappel : getCell(row, col) → on passe row en premier, col en second
        assertFalse(board.getCell(0, 0).isAccessible(), "Le coin (0,0) ne doit pas être accessible");
        assertFalse(board.getCell(0, 1).isAccessible(), "Le coin (0,1) ne doit pas être accessible");
        assertFalse(board.getCell(1, 0).isAccessible(), "Le coin (1,0) ne doit pas être accessible");
        assertFalse(board.getCell(6, 6).isAccessible(), "Le coin (6,6) ne doit pas être accessible");
    }

    /**
     * Vérifie que la case cau centre du plateau
     * est accessible.
     * La case (3,3)
     */
    @Test
    void centerCellIsAccessible() {
        assertTrue(board.getCell(3, 3).isAccessible(), "La case centrale (3,3) doit être accessible");
    }


     // Vérifie que les cases du bras supérieur de la croix sont accessibles.
    @Test
    void crossCellsAreAccessible() {
        assertTrue(board.getCell(0, 2).isAccessible(), "La case (0,2) du bras supérieur doit être accessible");
        assertTrue(board.getCell(0, 3).isAccessible(), "La case (0,3) du bras supérieur doit être accessible");
        assertTrue(board.getCell(0, 4).isAccessible(), "La case (0,4) du bras supérieur doit être accessible");
    }

    // Tests sur les voisins
    /**
     * Vérifie que la case centrale connaît ses voisins
     * : haut, bas, gauche, droite.
     */
    @Test
    void orthogonalNeighborsExist() {
        Cell center = board.getCell(3, 3);
        Cell up    = board.getCell(2, 3);  // une rangée au-dessus
        Cell down  = board.getCell(4, 3);  // une rangée en-dessous
        Cell left  = board.getCell(3, 2);  // une colonne à gauche
        Cell right = board.getCell(3, 4);  // une colonne à droite

        assertTrue(center.getNeighbors().contains(up),    "Voisin du haut manquant");
        assertTrue(center.getNeighbors().contains(down),  "Voisin du bas manquant");
        assertTrue(center.getNeighbors().contains(left),  "Voisin de gauche manquant");
        assertTrue(center.getNeighbors().contains(right), "Voisin de droite manquant");
    }

    // Tests sur les voisins en diagonal

    /**
     * Vérifie qu'une case "paire" (dont la somme ligne+colonne est paire)
     * possède bien des voisins diagonaux.
     *
     *seules les cases dont (row + col) est pair
     * ont des connexions diagonales, ce qui permet certains mouvements
     */
    @Test
    void diagonalNeighborOnEvenCell() {
        Cell c    = board.getCell(2, 2);  // (2+2)%2 == 0 → case paire
        Cell diag = board.getCell(3, 3);  // voisin en diagonale bas-droite
        assertTrue(c.getNeighbors().contains(diag),
                "La case paire (2,2) doit avoir (3,3) comme voisin diagonal");
    }

    /**
     * Vérifie qu'une case "impaire" (dont la somme ligne+colonne est impaire)
     * N'a PAS de voisins diagonaux.
     *
     *Les cases impaires sont reliées uniquement en orthogonal.
     */
    @Test
    void noDiagonalNeighborOnOddCell() {
        Cell c    = board.getCell(2, 3);  // (2+3)%2 == 1 → case impaire
        Cell diag = board.getCell(3, 4);  // case en diagonale bas-droite
        assertFalse(c.getNeighbors().contains(diag),
                "La case impaire (2,3) ne doit PAS avoir (3,4) comme voisin diagonal");
    }


    // Test sur les cases inaccessibles

    /**
     * Vérifie qu'une case inaccessible (un coin) n'a aucun voisin.
     *
     * Les cases hors du terrain de jeu ne doivent pas être reliées
     * au reste du plateau : leur liste de voisins doit être vide.
     */
    @Test
    void inaccessibleCellHasNoNeighbors() {
        Cell corner = board.getCell(0, 0);  // coin supérieur gauche, inaccessible
        assertTrue(corner.getNeighbors().isEmpty(),
                "Une case inaccessible ne doit avoir aucun voisin");
    }

}