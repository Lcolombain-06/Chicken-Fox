package model;


public class WinCondition {

    public static final int MIN_CHICKENS_TO_BLOCK = 4;


    // Comptage des poules


    /**
     * Compte le nombre de poules présentes sur le plateau.
     * Exemple d'utilisation : avant chaque tour, on vérifie si assez
     * de poules restent
     */
    public static int countChickens(Board board) {
        int count = 0;
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                Cell cell = board.getCell(row, col);
                if (!cell.isAccessible()) continue;

                //A FINIR PAS TOUCHE
            }
        }
        return count;
    }
}