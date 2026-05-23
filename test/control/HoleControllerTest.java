package control;

import boardifier.model.Model;
import control.HoleController;
import model.Board;
import model.HoleStageModel;
import model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    // ---------------------------------------------------------------
    // Infrastructure commune à tous les tests
    // ---------------------------------------------------------------

    private Model          model;
    private HoleStageModel gameStage;
    private Board          board;
    private HoleController controller;

    /**
     * Avant chaque test on repart d'une partie toute fraîche :
     *  - Un Model vide (pas de View, pas de joueurs réels).
     *  - Un HoleStageModel branché sur ce Model.
     *  - Un Board vide posé sur le HoleStageModel.
     *  - Un HoleController qui utilise ce même Model.
     *
     * On appelle endInitialization() pour désactiver le callback
     * qui décompte geeseToPlay automatiquement à chaque pose —
     * on veut contrôler ça manuellement dans les tests.
     */
    @BeforeEach
    void setUp() {
        model      = new Model();
        gameStage  = new HoleStageModel("test", model);
        board      = new Board(0, 0, gameStage);

        gameStage.setBoard(board);
        model.setGameStage(gameStage);

        // Désactive le callback "auto-décompte" pour les tests
        gameStage.endInitialization();

        // HoleController(model, view) : view peut être null pour les tests
        // qui n'appellent pas update() ni les méthodes d'affichage.
        controller = new HoleController(model, null);
    }

    /**
     * Méthode utilitaire : appelle la méthode PRIVÉE partyWinned(row, col)
     * via la Réflexion Java.
     *
     * Pourquoi la réflexion ? partyWinned est privée car elle est un
     * détail d'implémentation interne au contrôleur. Mais c'est elle
     * qui contient toute la logique de victoire → il est pertinent de
     * la tester directement plutôt que via stageLoop() entier.
     *
     * @param row  Ligne où se trouve le renard.
     * @param col  Colonne où se trouve le renard.
     * @return 0 = personne ne gagne, 1 = renard gagne, 2 = poules gagnent.
     */
    private int callPartyWinned(int row, int col) throws Exception {
        Method m = HoleController.class.getDeclaredMethod("partyWinned", int.class, int.class);
        m.setAccessible(true);
        return (int) m.invoke(controller, row, col);
    }

    // ---------------------------------------------------------------
    // Tests : personne ne gagne encore (retour 0)
    // ---------------------------------------------------------------

    /**
     * En début de partie, le compteur de poules est à 13 (≥ 4)
     * et le renard a des cases libres autour de lui.
     * → Personne ne doit gagner : retour attendu = 0.
     *
     * On place le renard au centre (3,3) du plateau vide.
     */
    @Test
    void nobodyWinsAtStart() throws Exception {
        // Renard au centre, plateau vide autour → plusieurs cases libres
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(0, result,
                "En début de partie personne ne doit gagner (geeseToPlay=13, renard libre)");
    }

    /**
     * Avec exactement 4 poules restantes (geeseToPlay == 4),
     * la condition renard (< 4) n'est pas encore atteinte.
     * Si le renard a encore des cases libres → retour = 0.
     */
    @Test
    void nobodyWinsWhenExactlyFourGeese() throws Exception {
        // On simule qu'il ne reste que 4 poules dans le compteur
        // geeseToPlay démarre à 13, on "mange" 9 poules
        for (int i = 0; i < 9; i++) gameStage.eatGeese(); // 13 - 9 = 4

        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(0, result,
                "Avec 4 poules restantes et le renard libre, personne ne gagne");
    }

    // ---------------------------------------------------------------
    // Tests : le RENARD gagne (retour 1)
    // ---------------------------------------------------------------

    /**
     * Le renard gagne quand geeseToPlay < 4.
     * Avec 3 poules restantes, la condition est atteinte.
     * → Retour attendu = 1.
     *
     * La position du renard n'a pas d'importance ici : le renard
     * gagne avant même qu'on vérifie s'il est bloqué.
     */
    @Test
    void foxWinsWhenThreeGeeseLeft() throws Exception {
        // geeseToPlay = 13 - 10 = 3
        for (int i = 0; i < 10; i++) gameStage.eatGeese();

        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(1, result,
                "Le renard doit gagner quand il reste 3 poules (< 4)");
    }

    /**
     * Le renard gagne aussi quand il ne reste plus aucune poule (0).
     * Cas extrême pour vérifier la robustesse de la condition.
     */
    @Test
    void foxWinsWhenNoGeeseLeft() throws Exception {
        // geeseToPlay = 13 - 13 = 0
        for (int i = 0; i < 13; i++) gameStage.eatGeese();

        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(1, result,
                "Le renard doit gagner quand il ne reste aucune poule");
    }

    /**
     * Avec 1 poule restante, le renard gagne (1 < 4).
     * Cas limite entre "aucune poule" et "assez de poules".
     */
    @Test
    void foxWinsWhenOneGooseLeft() throws Exception {
        // geeseToPlay = 13 - 12 = 1
        for (int i = 0; i < 12; i++) gameStage.eatGeese();

        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(1, result,
                "Le renard doit gagner quand il reste 1 poule (< 4)");
    }

    /**
     * La condition du renard (geeseToPlay < 4) est prioritaire sur
     * celle des poules (renard bloqué). Même si le renard était
     * entouré, si geeseToPlay < 4 alors c'est le renard qui gagne.
     *
     * On vérifie que le if/else est dans le bon ordre.
     */
    @Test
    void foxConditionTakesPriorityOverGeeseCondition() throws Exception {
        // Seulement 2 poules restantes → condition renard activée
        for (int i = 0; i < 11; i++) gameStage.eatGeese(); // 13-11=2

        // On bloque le renard avec des poules (même si peu nombreuses)
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        // On place des poules autour pour (tenter de) simuler un blocage
        gameStage.putInContainer(new Pawn(Pawn.GOOSE, gameStage), board, 2, 3);
        gameStage.putInContainer(new Pawn(Pawn.GOOSE, gameStage), board, 4, 3);

        int result = callPartyWinned(3, 3);

        // Renard gagne car geeseToPlay=2 < 4, malgré la présence de poules
        assertEquals(1, result,
                "Le renard doit gagner (< 4 poules) même si des poules sont proches");
    }

    // ---------------------------------------------------------------
    // Tests : les POULES gagnent (retour 2)
    // ---------------------------------------------------------------

    /**
     * Les poules gagnent quand le renard n'a plus aucune case accessible.
     * On encercle le renard en (3,3) avec des poules sur ses 8 voisins.
     *
     * Important : geeseToPlay doit rester ≥ 4, sinon la condition
     * du renard prendrait le dessus avant même de vérifier le blocage.
     * Ici geeseToPlay = 13 (valeur initiale, on ne mange rien).
     */
    @Test
    void geeseWinWhenFoxIsFullySurrounded() throws Exception {
        // Renard au centre
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        // On bloque les 8 voisins du renard (orthogonaux + diagonaux)
        int[][] neighbors = {
                {2, 2}, {2, 3}, {2, 4},
                {3, 2},         {3, 4},
                {4, 2}, {4, 3}, {4, 4}
        };
        for (int[] pos : neighbors) {
            gameStage.putInContainer(new Pawn(Pawn.GOOSE, gameStage), board, pos[0], pos[1]);
        }

        int result = callPartyWinned(3, 3);

        assertEquals(2, result,
                "Les poules doivent gagner quand le renard est totalement encerclé");
    }

    /**
     * Si le renard a encore au moins une case libre voisine,
     * les poules ne gagnent pas.
     *
     * On place le renard en (3,3) et on bloque 7 de ses 8 voisins,
     * laissant (2,3) libre. → Retour attendu = 0.
     */
    @Test
    void geeseDoNotWinWhenFoxHasOneFreeCell() throws Exception {
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        // Tous les voisins sauf (2,3) sont bloqués
        int[][] neighbors = {
                {2, 2},         {2, 4},   // (2,3) libre intentionnellement
                {3, 2},         {3, 4},
                {4, 2}, {4, 3}, {4, 4}
        };
        for (int[] pos : neighbors) {
            gameStage.putInContainer(new Pawn(Pawn.GOOSE, gameStage), board, pos[0], pos[1]);
        }

        int result = callPartyWinned(3, 3);

        assertEquals(0, result,
                "Les poules ne doivent pas gagner si le renard a encore une case libre");
    }

    /**
     * Sur un plateau totalement vide (pas de renard posé),
     * partyWinned() ne doit pas planter (le renard est null à ce row/col).
     *
     * Dans le code de partyWinned : board.getFirstElement(row,col) peut
     * renvoyer null si rien n'est là, et setValidCells serait appelé avec null.
     * Ce test vérifie la robustesse du code dans ce cas de bord.
     *
     * On s'attend à ce que le résultat soit 0 (personne) ou que la méthode
     * gère le cas sans lancer d'exception — comportement observable.
     */
    @Test
    void partyWinnedDoesNotCrashWithNoFoxOnBoard() throws Exception {
        // geeseToPlay = 13 → la branche fox (< 4) n'est pas prise
        // Le renard n'est pas posé → getFirstElement retourne null
        // On attend soit 0, soit une gestion gracieuse sans exception

        assertDoesNotThrow(() -> {
            int result = callPartyWinned(3, 3);
            // Si le code gère null proprement, il renvoie probablement 0
            // (aucun mouvement trouvé) ou 2 (0 cases → poules gagnent).
            // On vérifie juste que ça ne crash pas.
            assertTrue(result == 0 || result == 2,
                    "Sans renard sur le plateau, le résultat doit être 0 ou 2");
        });
    }

    // ---------------------------------------------------------------
    // Tests sur eatGeese() et le compteur geeseToPlay
    // ---------------------------------------------------------------

    /**
     * Vérifie que eatGeese() décrémente bien geeseToPlay de 1 à chaque appel.
     *
     * C'est la méthode utilisée par le contrôleur après chaque capture
     * du renard. Si elle ne fonctionne pas, la condition de victoire
     * du renard ne sera jamais déclenchée correctement.
     */
    @Test
    void eatGeeseDecrementsCounter() {
        // Départ à 13
        assertEquals(13, gameStage.getGeeseToPlay(),
                "geeseToPlay doit démarrer à 13");

        gameStage.eatGeese();
        assertEquals(12, gameStage.getGeeseToPlay(),
                "Après 1 eatGeese(), geeseToPlay doit être 12");

        gameStage.eatGeese();
        assertEquals(11, gameStage.getGeeseToPlay(),
                "Après 2 eatGeese(), geeseToPlay doit être 11");
    }

    /**
     * Vérifie que la transition 4 → 3 (le seuil exact de victoire du renard)
     * bascule bien le résultat de partyWinned de 0 à 1.
     *
     * C'est le cas limite le plus important : à 4 poules personne ne gagne,
     * à 3 poules le renard gagne. On vérifie ce basculement précis.
     */
    @Test
    void foxWinThresholdIsExactlyFour() throws Exception {
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        // Amener à exactement 4 poules → personne ne gagne encore
        for (int i = 0; i < 9; i++) gameStage.eatGeese(); // 13-9=4
        int at4 = callPartyWinned(3, 3);
        assertEquals(0, at4,
                "À 4 poules restantes, le renard ne doit pas encore gagner");

        // Une poule de plus mangée → 3 poules → le renard gagne
        gameStage.eatGeese(); // 4-1=3
        int at3 = callPartyWinned(3, 3);
        assertEquals(1, at3,
                "À 3 poules restantes, le renard doit gagner (3 < 4)");
        // ok
    }
}
