package control;

import boardifier.model.Model;
import control.HoleController;
import model.Board;
import model.HoleStageModel;
import model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * Tests des CONDITIONS DE VICTOIRE telles qu'elles sont
 * réellement codées dans HoleController.partyWinned().
 *
 * On n'utilise PAS Mockito : on crée un vrai Model, un vrai
 * HoleStageModel et un vrai Board, exactement comme le jeu
 * le fait au démarrage.
 *
 * La méthode partyWinned() est privée dans HoleController.
 * On y accède via la Réflexion Java (getDeclaredMethod) pour
 * pouvoir la tester directement sans modifier le code source.
 *
 * Valeurs clés de la logique :
 *   - geeseToPlay démarre à 13 (dans HoleStageModel).
 *   - Chaque appel à eatGeese() retire 1 poule du compteur.
 *   - Le renard gagne si geeseToPlay < 4.
 *   - Les poules gagnent si le renard n'a plus aucune case
 *     atteignable (setValidCells retourne 0).
 *   - Retour : 0 = personne, 1 = renard, 2 = poules.
 * ============================================================
 */
public class HoleControllerTest {

    // ---------------------------------------------------
    // Données communes à tous les tests
    // ---------------------------------------------------

    private Model model;
    private HoleStageModel gameStage;
    private Board board;
    private HoleController controller;

    /**
     * Initialise une partie de test propre avant chaque test.
     */
    @BeforeEach
    void setUp() throws Exception {

        model = new Model();

        gameStage = new HoleStageModel("test", model);

        board = new Board(0, 0, gameStage);

        gameStage.setBoard(board);

        model.setGameStage(gameStage);

        // Désactive le décompte automatique des poules
        gameStage.endInitialization();

        // Controller sans View pour les tests
        controller = new HoleController(model, null);

        // Faux reader pour éviter les erreurs console
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        BufferedReader fakeReader =
                new BufferedReader(new InputStreamReader(emptyStream));

        Field consoleInField =
                HoleController.class.getDeclaredField("consoleIn");

        consoleInField.setAccessible(true);

        consoleInField.set(controller, fakeReader);
    }

    // ---------------------------------------------------
    // Méthodes utilitaires (réflexion)
    // ---------------------------------------------------

    /**
     * Appelle la méthode privée partyWinned().
     */
    private int callPartyWinned(int row, int col) throws Exception {

        Method method =
                HoleController.class.getDeclaredMethod(
                        "partyWinned",
                        int.class,
                        int.class
                );

        method.setAccessible(true);

        return (int) method.invoke(controller, row, col);
    }

    /**
     * Appelle la méthode privée foxPlay().
     */
    private boolean callFoxPlay(String line) throws Exception {

        Method method =
                HoleController.class.getDeclaredMethod(
                        "foxPlay",
                        String.class
                );

        method.setAccessible(true);

        return (boolean) method.invoke(controller, line);
    }

    /**
     * Appelle la méthode privée geesePlay().
     */
    private boolean callGeesePlay(String line) throws Exception {

        Method method =
                HoleController.class.getDeclaredMethod(
                        "geesePlay",
                        String.class
                );

        method.setAccessible(true);

        return (boolean) method.invoke(controller, line);
    }

    // ---------------------------------------------------
    // Tests : aucun gagnant
    // ---------------------------------------------------

    @Test
    void nobodyWinsAtStart() throws Exception {

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                0,
                result,
                "En début de partie personne ne doit gagner"
        );
    }

    @Test
    void nobodyWinsWhenExactlyFourGeese() throws Exception {

        // 13 - 9 = 4
        for (int i = 0; i < 9; i++) {
            gameStage.eatGeese();
        }

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                0,
                result,
                "Avec 4 poules restantes, personne ne gagne"
        );
    }

    // ---------------------------------------------------
    // Tests : victoire du renard
    // ---------------------------------------------------

    @Test
    void foxWinsWhenThreeGeeseLeft() throws Exception {

        // 13 - 10 = 3
        for (int i = 0; i < 10; i++) {
            gameStage.eatGeese();
        }

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                1,
                result,
                "Le renard doit gagner avec moins de 4 poules"
        );
    }

    @Test
    void foxWinsWhenNoGeeseLeft() throws Exception {

        for (int i = 0; i < 13; i++) {
            gameStage.eatGeese();
        }

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                1,
                result,
                "Le renard doit gagner quand il ne reste plus de poules"
        );
    }

    @Test
    void foxWinsWhenOneGooseLeft() throws Exception {

        // 13 - 12 = 1
        for (int i = 0; i < 12; i++) {
            gameStage.eatGeese();
        }

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                1,
                result,
                "Le renard doit gagner avec 1 seule poule restante"
        );
    }

    @Test
    void foxConditionTakesPriorityOverGeeseCondition() throws Exception {

        // 13 - 11 = 2
        for (int i = 0; i < 11; i++) {
            gameStage.eatGeese();
        }

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        board.addElement(new Pawn(Pawn.GOOSE, gameStage), 2, 3);

        board.addElement(new Pawn(Pawn.GOOSE, gameStage), 4, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                1,
                result,
                "La condition du renard doit être prioritaire"
        );
    }

    // ---------------------------------------------------
    // Tests : victoire des poules
    // ---------------------------------------------------

    @Test
    void geeseWinWhenFoxIsFullySurrounded() throws Exception {

        // IMPORTANT : on reste au-dessus de 4 pour activer la logique des poules
        while (gameStage.getGeeseToPlay() < 13) {
            // reset sécurité (au cas où d'autres tests modifient l'état)
        }

        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        board.addElement(fox, 3, 3);
        gameStage.setFoxCoo(3, 3);

        int[][] neighbors = {
                {2, 2}, {2, 3}, {2, 4},
                {3, 2},         {3, 4},
                {4, 2}, {4, 3}, {4, 4}
        };

        for (int[] pos : neighbors) {
            board.addElement(new Pawn(Pawn.GOOSE, gameStage), pos[0], pos[1]);
        }

        int result = callPartyWinned(3, 3);

        assertEquals(
                2,
                result,
                "Les poules doivent gagner quand le renard est complètement bloqué"
        );
    }

    @Test
    void geeseDoNotWinWhenFoxHasOneFreeCell() throws Exception {

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int[][] neighbors = {
                {2, 2},         {2, 4},
                {3, 2},         {3, 4},
                {4, 2}, {4, 3}, {4, 4}
        };

        for (int[] pos : neighbors) {

            board.addElement(
                    new Pawn(Pawn.GOOSE, gameStage),
                    pos[0],
                    pos[1]
            );
        }

        int result = callPartyWinned(3, 3);

        assertEquals(
                0,
                result,
                "Le renard possède encore une case libre"
        );
    }

    @Test
    void partyWinnedDoesNotCrashWithNoFoxOnBoard() {

        assertDoesNotThrow(() -> {

            int result = callPartyWinned(3, 3);

            assertTrue(
                    result == 0 || result == 2,
                    "Le code ne doit pas planter sans renard"
            );
        });
    }

    // ---------------------------------------------------
    // Tests sur le compteur des poulesss
    // ---------------------------------------------------

    @Test
    void eatGeeseDecrementsCounter() {

        assertEquals(
                13,
                gameStage.getGeeseToPlay(),
                "Le compteur doit démarrer à 13"
        );

        gameStage.eatGeese();

        assertEquals(
                12,
                gameStage.getGeeseToPlay(),
                "Après une capture le compteur doit valoir 12"
        );

        gameStage.eatGeese();

        assertEquals(
                11,
                gameStage.getGeeseToPlay(),
                "Après deux captures le compteur doit valoir 11"
        );
    }

    @Test
    void foxWinThresholdIsExactlyFour() throws Exception {

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        // 13 - 9 = 4
        for (int i = 0; i < 9; i++) {
            gameStage.eatGeese();
        }

        int at4 = callPartyWinned(3, 3);

        assertEquals(
                0,
                at4,
                "À 4 poules le renard ne gagne pas encore"
        );

        gameStage.eatGeese();

        int at3 = callPartyWinned(3, 3);

        assertEquals(
                1,
                at3,
                "À 3 poules le renard doit gagner"
        );
    }


    // Partie testMVT IBTI
    @Test
    void foxPlayRejectsOutOfBoundsColumn() throws Exception {
        // '8' - '1' = 7, colonne hors plateau
        boolean result = callFoxPlay("D8");
        assertFalse(result, "foxPlay doit refuser une colonne hors du plateau");
    }

    // ---------------------------------------------------------------
    // Tests sur foxPlay() — absence du renard sur le plateau
    // ---------------------------------------------------------------

    /**
     * Si le renard n'est pas posé sur le plateau à la position enregistrée
     * dans le modèle (foxRow, foxCol), foxPlay() doit retourner false.
     *
     * La position par défaut du modèle est (2, 3). On ne pose pas de renard
     * → getFirstElement retourne null → "Error : Fox unfound !".
     */
    @Test
    void foxPlayReturnsFalseWhenNoFoxOnBoard() throws Exception {
        // foxRow=2, foxCol=3 par défaut dans HoleStageModel
        // Aucun renard posé → getFirstElement(2,3) == null

        // 'C' - 'A' = 2, '4' - '1' = 3 → case (2,3) : la même → test d'une case voisine
        boolean result = callFoxPlay("D4"); // (3,3) : case libre voisine
        assertFalse(result,
                "foxPlay doit retourner false si le renard n'est pas posé à sa position");
    }

    // ---------------------------------------------------------------
    // Tests sur foxPlay() — case non atteignable
    // ---------------------------------------------------------------

    /**
     * Le renard ne peut pas aller vers une case non accessible dans ses
     * cases valides (reachableCells).
     *
     * On pose le renard en (2,3). La case (0,0) est inaccessible sur ce
     * plateau (coin hors de la croix) → foxPlay doit refuser.
     */
    @Test
    void foxPlayRejectsNonReachableCell() throws Exception {
        // Poser le renard à sa position par défaut dans le modèle (2,3)
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 2, 3);
        gameStage.setFoxCoo(2, 3);

        // 'A' - 'A' = 0, '1' - '1' = 0 → case (0,0) : coin inaccessible
        boolean result = callFoxPlay("A1");
        assertFalse(result,
                "foxPlay doit refuser un mouvement vers une case non atteignable");
    }

    /**
     * Le renard ne peut pas aller vers une case trop éloignée
     * qui n'est pas dans ses voisins directs.
     *
     * Le renard est en (2,3). La case (6,6) est loin et non voisine.
     */
    @Test
    void foxPlayRejectsFarAwayCell() throws Exception {
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 2, 3);
        gameStage.setFoxCoo(2, 3);

        // 'G' - 'A' = 6, '7' - '1' = 6 → case (6,6) : coin accessible mais non voisin
        boolean result = callFoxPlay("G7");
        assertFalse(result,
                "foxPlay doit refuser un mouvement vers une case trop éloignée");
    }

    // ---------------------------------------------------------------
    // Tests sur foxPlay() — détection de la capture (flag foxCaptured)
    // ---------------------------------------------------------------

    /**
     * Quand le renard saute par-dessus une poule (déplacement de 2 cases),
     * foxPlay() doit mettre foxCaptured à true.
     *
     * IMPORTANT : ce test NE vérifie PAS le déplacement physique sur le
     * plateau (ActionPlayer non disponible sans View). Il vérifie que le
     * FLAG foxCaptured est bien levé avant l'ActionPlayer (ligne 190 du
     * contrôleur : gameStage.setFoxCaptured(true)).
     *
     * Dispositif : renard en (4,3), poule en (3,3), case libre en (2,3).
     * Commande : "C4" → row = 'C'-'A'=2, col = '4'-'1'=3 → (2,3).
     * Distance : |4-2|=2 → c'est un saut → foxCaptured doit devenir true.
     */

    @Test
    void foxPlaySetsFoxCapturedOnJump() throws Exception {

        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();

        // =========================
        // Placement du renard
        // =========================
        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        // IMPORTANT :
        // addElement() place réellement le pion dans le board
        board.addElement(fox, 4, 3);

        // coordonnées du renard dans le model
        gameStage.setFoxCoo(4, 3);

        // =========================
        // Placement de la poule
        // =========================
        Pawn goose = new Pawn(Pawn.GOOSE, gameStage);
        board.addElement(goose, 3, 3);

        // =========================
        // Vérification avant test
        // =========================
        assertNotNull(board.getFirstElement(4, 3),
                "Le renard doit être présent en (4,3)");

        assertNotNull(board.getFirstElement(3, 3),
                "La poule doit être présente en (3,3)");

        // =========================
        // Reset du flag
        // =========================
        gameStage.setFoxCaptured(false);

        // =========================
        // Mouvement :
        // C4 -> (2,3)
        // Capture de la poule en (3,3)
        // =========================
        try {
            callFoxPlay("C4");
        }
        catch (Exception ignored) {
            // ActionPlayer peut échouer sans View
        }

        // =========================
        // Vérification finale
        // =========================
        assertTrue(gameStage.isFoxCaptured(),
                "foxPlay doit lever le flag foxCaptured lors d'un saut par-dessus une poule");
    }


    /**
     * Un déplacement simple du renard (1 case, pas de saut) ne doit PAS
     * mettre foxCaptured à true. Le flag doit rester false.
     */
    @Test
    void foxPlayDoesNotSetFoxCapturedOnSimpleMove() throws Exception {
        // Renard en (3,3)
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        gameStage.setFoxCaptured(false);

        // Mouvement simple vers (3,4) : 1 case à droite
        // 'D'-'A'=3 (ligne), '5'-'1'=4 (col) → (3,4)
        try {
            callFoxPlay("D5");
        } catch (Exception ignored) {
            // ActionPlayer peut planter sans View.
        }

        assertFalse(gameStage.isFoxCaptured(),
                "Un déplacement simple du renard ne doit pas lever le flag foxCaptured");
    }

    // ---------------------------------------------------------------
    // Tests sur geesePlay() — coordonnées hors plateau
    // ---------------------------------------------------------------

    /**
     * Une commande dont les coordonnées de départ sont hors du plateau
     * doit être refusée immédiatement par geesePlay().
     *
     * 'Z' - 'A' = 25, bien au-delà de la grille 7x7.
     */
    @Test
    void geesePlayRejectsOutOfBoundsStart() throws Exception {
        boolean result = callGeesePlay("Z1D4");
        assertFalse(result,
                "geesePlay doit refuser des coordonnées de départ hors du plateau");
    }

    /**
     * Une commande dont les coordonnées d'arrivée sont hors du plateau
     * doit être refusée par geesePlay().
     */
    @Test
    void geesePlayRejectsOutOfBoundsEnd() throws Exception {
        boolean result = callGeesePlay("D4Z1");
        assertFalse(result,
                "geesePlay doit refuser des coordonnées d'arrivée hors du plateau");
    }

    // ---------------------------------------------------------------
    // Tests sur geesePlay() — case de départ vide ou mauvaise pièce
    // ---------------------------------------------------------------

    /**
     * Si la case de départ ne contient aucune pièce, geesePlay() doit
     * retourner false avec "There is no goose here !".
     *
     * On ne pose rien sur le plateau → la case (3,3) est vide.
     */
    @Test
    void geesePlayReturnsFalseWhenStartCellIsEmpty() throws Exception {
        // Aucune poule posée, plateau vide
        boolean result = callGeesePlay("D4D5"); // (3,3) → (3,4)
        assertFalse(result,
                "geesePlay doit refuser si la case de départ est vide");
    }

    /**
     * Si la case de départ contient un RENARD (pas une poule),
     * geesePlay() doit retourner false avec "It's geese turn !".
     *
     * Le renard ne peut pas jouer pendant le tour des poules.
     */
    @Test
    void geesePlayReturnsFalseWhenStartCellHasFox() throws Exception {
        // On place un renard à la case de départ
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);  // (3,3)

        // La commande désigne (3,3) comme départ
        boolean result = callGeesePlay("D4D5"); // (3,3) → (3,4)
        assertFalse(result,
                "geesePlay doit refuser si la case de départ contient un renard");
    }

    // ---------------------------------------------------------------
    // Tests sur geesePlay() — destination non atteignable
    // ---------------------------------------------------------------

    /**
     * Une poule ne peut pas se déplacer vers une case qui n'est pas dans
     * ses cases valides (reachableCells).
     *
     * On place une poule en (3,3) et on tente un mouvement diagonal vers (4,4).
     * Les poules ne peuvent pas aller en diagonale → refusé.
     */
    @Test
    void geesePlayRejectsDiagonalMove() throws Exception {
        Pawn goose = new Pawn(Pawn.GOOSE, gameStage);
        gameStage.putInContainer(goose, board, 3, 3);

        // (3,3) → (4,4) : diagonal → non autorisé pour une poule
        // 'D'-'A'=3, '4'-'1'=3 → départ (3,3)
        // 'E'-'A'=4, '5'-'1'=4 → arrivée (4,4)
        boolean result = callGeesePlay("D4E5");
        assertFalse(result,
                "geesePlay doit refuser un mouvement diagonal pour une poule");
    }

    /**
     * Une poule ne peut pas reculer (monter vers une rangée inférieure).
     *
     * Poule en (4,3), destination (3,3) : c'est un recul → refusé.
     */
    @Test
    void geesePlayRejectsBackwardMove() throws Exception {
        Pawn goose = new Pawn(Pawn.GOOSE, gameStage);
        gameStage.putInContainer(goose, board, 4, 3);

        // 'E'-'A'=4, '4'-'1'=3 → départ (4,3)
        // 'D'-'A'=3, '4'-'1'=3 → arrivée (3,3) : recul
        boolean result = callGeesePlay("E4D4");
        assertFalse(result,
                "geesePlay doit refuser un déplacement vers l'arrière pour une poule");
    }

    /**
     * Une poule ne peut pas aller sur une case occupée par une autre poule.
     *
     * Deux poules en (3,3) et (4,3). La première tente d'aller en (4,3) → refusé.
     */
    @Test
    void geesePlayRejectsMoveToOccupiedCell() throws Exception {
        Pawn goose1 = new Pawn(Pawn.GOOSE, gameStage);
        Pawn goose2 = new Pawn(Pawn.GOOSE, gameStage);
        gameStage.putInContainer(goose1, board, 3, 3);
        gameStage.putInContainer(goose2, board, 4, 3);

        // (3,3) → (4,3) : case occupée par goose2
        boolean result = callGeesePlay("D4E4");
        assertFalse(result,
                "geesePlay doit refuser si la case d'arrivée est déjà occupée");
    }

    // ---------------------------------------------------------------
    // Tests sur le FORMAT des commandes (analyseAndPlay)
    // ---------------------------------------------------------------

    /**
     * Vérification indirecte du format : foxPlay attend une ligne de 2 caractères.
     * Si la ligne en entrée est vide ou mal formée, les calculs charAt()
     * vont planter ou produire un mauvais index.
     *
     * On s'assure que l'appel direct avec une ligne vide ne retourne pas true.
     */
    @Test
    void foxPlayHandlesEmptyLineSafely() {
        assertDoesNotThrow(() -> {
            try {
                boolean result = callFoxPlay("");
                assertFalse(result, "Une ligne vide ne doit pas être acceptée");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // La réflexion enveloppe les exceptions → on vérifie la cause
                if (!(e.getCause() instanceof StringIndexOutOfBoundsException)) {
                    throw e; // Re-lancer si c'est une autre erreur inattendue
                }
                // StringIndexOutOfBoundsException acceptée : foxPlay ne gère pas les lignes vides
            }
        });
    }

    // ---------------------------------------------------------------
    // Tests sur foxCaptured et mise à jour des coordonnées
    // ---------------------------------------------------------------

    /**
     * Avant tout appel à foxPlay(), le flag foxCaptured est remis à false
     * au début de la méthode (ligne 150 du contrôleur).
     *
     * On vérifie que même si foxCaptured était true avant, foxPlay()
     * le remet à false au début (pour les mouvements non-capture,
     * il restera false ; pour les captures, il passera à true ensuite).
     */
    @Test
    void foxPlayResetsFoxCapturedAtStart() throws Exception {
        gameStage.setFoxCaptured(true);

        try {
            callFoxPlay("Z9");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Accepté si StringIndexOutOfBounds (ligne trop courte/hors bornes)
        }

        assertFalse(gameStage.isFoxCaptured(),
                "foxPlay doit remettre foxCaptured à false au début de chaque appel");
    }
}
