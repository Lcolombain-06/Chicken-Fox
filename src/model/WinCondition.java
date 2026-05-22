package model;

public class WinCondition {

    public static final int MIN_CHICKENS_TO_BLOCK = 4;

    /**
     * Comptage des poules
     * Compte le nombre de poulesprésentes sur le plateau.
     * EX : avant chaque tour, on vérifie si assez de poules restent en jeu.**/

    public static int countChickens(Board board) {
        int count = 0;
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                Cell cell = board.getCell(row, col);
                if (!cell.isAccessible()) continue;
                Object element = board.getElement(row, col);
                if (element instanceof Pawn) {
                    Pawn p = (Pawn) element;
                    if (p.isGoose()) count++;
                }
            }
        }
        return count;
    }

    /**
     * Condition de victoire du renard
     * Le renard gagne quand il reste le nb minimal de poules sur le plateau**/
    public static boolean foxWins(Board board) {
        return countChickens(board) < MIN_CHICKENS_TO_BLOCK;
    }

}
