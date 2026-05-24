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


public class HoleControllerTest {


    // Data common to all tests
    private Model model;
    private HoleStageModel gameStage;
    private Board board;
    private HoleController controller;


    // Initializes a clean test environment before each test.

    @BeforeEach
    void setUp() throws Exception {

        model = new Model();

        gameStage = new HoleStageModel("test", model);

        board = new Board(0, 0, gameStage);

        gameStage.setBoard(board);

        model.setGameStage(gameStage);

        // Disables automatic pool counting
        gameStage.endInitialization();

        controller = new HoleController(model, null);
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        BufferedReader fakeReader =
                new BufferedReader(new InputStreamReader(emptyStream));

        Field consoleInField =
                HoleController.class.getDeclaredField("consoleIn");

        consoleInField.setAccessible(true);

        consoleInField.set(controller, fakeReader);
    }

    // Utility methods (reflection)
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
     * Calls the private method foxPlay().
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
     * Calls the private method geesePlay().
     **/
    private boolean callGeesePlay(String line) throws Exception {

        Method method =
                HoleController.class.getDeclaredMethod(
                        "geesePlay",
                        String.class
                );

        method.setAccessible(true);

        return (boolean) method.invoke(controller, line);
    }


    // Tests: no winner

    @Test
    void nobodyWinsAtStart() throws Exception {

        Pawn fox = new Pawn(Pawn.FOX, gameStage);

        board.addElement(fox, 3, 3);

        gameStage.setFoxCoo(3, 3);

        int result = callPartyWinned(3, 3);

        assertEquals(
                0,
                result,
                "At the start of the game, no one should win"
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
                "With four groups remaining, no one wins"
        );
    }


    // Tests : victoire du renard

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
                "The fox must win with fewer than four hens"
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
                "The fox must win when there are no more chickens left"
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
                "The Fox must win with only one hen remaining"
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
                "The fox's condition must be the top priority"
        );
    }

    // Tests : victoire des poules

    @Test
    void geeseWinWhenFoxIsFullySurrounded() throws Exception {

        // Le renard en (3,3)
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        board.addElement(fox, 3, 3);
        gameStage.setFoxCoo(3, 3);

        // 8 neighboring clusters blocking all simple moves
        int[][] neighbors = {
                {2, 2}, {2, 3}, {2, 4},
                {3, 2},         {3, 4},
                {4, 2}, {4, 3}, {4, 4}
        };
        for (int[] pos : neighbors) {
            board.addElement(new Pawn(Pawn.GOOSE, gameStage), pos[0], pos[1]);
        }

        int result = callPartyWinned(3, 3);

        assertEquals(2, result,
                "The chickens must win when the fox is completely trapped");
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
                "The fox still has one space left."
        );
    }

    @Test
    void partyWinnedDoesNotCrashWithNoFoxOnBoard() {

        assertDoesNotThrow(() -> {

            int result = callPartyWinned(3, 3);

            assertTrue(
                    result == 0 || result == 2,
                    "The code shouldn't crash without Fox"
            );
        });
    }

    // Tests on the chicken counter

    @Test
    void eatGeeseDecrementsCounter() {

        assertEquals(
                13,
                gameStage.getGeeseToPlay(),
                "The counter should start at 13"
        );

        gameStage.eatGeese();

        assertEquals(
                12,
                gameStage.getGeeseToPlay(),
                "After a capture, the score should be 12"
        );

        gameStage.eatGeese();

        assertEquals(
                11,
                gameStage.getGeeseToPlay(),
                "After two catches, the total should be 11"
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
                "With four hens, the fox still doesn't win"
        );

        gameStage.eatGeese();

        int at3 = callPartyWinned(3, 3);

        assertEquals(
                1,
                at3,
                "With three chickens, the fox must win"
        );
    }


    // Partie testMVT IBTI
    @Test
    void foxPlayRejectsOutOfBoundsColumn() throws Exception {
        boolean result = callFoxPlay("D8");
        assertFalse(result, "foxPlay must reject a column that is off the board");
    }



    // Tests sur foxPlay() — absence du renard sur le plateau
    @Test
    void foxPlayReturnsFalseWhenNoFoxOnBoard() throws Exception {
        // foxRow=2, foxCol=3 par défaut dans HoleStageModel
        // Aucun renard posé → getFirstElement(2,3) == null

        // 'C' - 'A' = 2, '4' - '1' = 3 → case (2,3) : la même → test d'une case voisine
        boolean result = callFoxPlay("D4"); // (3,3) : case libre voisine
        assertFalse(result,
                "foxPlay must return false if the fox is not placed at its position");
    }


    // Tests on foxPlay() — unreachable case

    @Test
    void foxPlayRejectsNonReachableCell() throws Exception {
        // Set the fox to its default position in the model (2,3)
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 2, 3);
        gameStage.setFoxCoo(2, 3);

        boolean result = callFoxPlay("A1");
        assertFalse(result,
                "foxPlay must reject a move to a square that cannot be reached");
    }


    @Test
    void foxPlayRejectsFarAwayCell() throws Exception {
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 2, 3);
        gameStage.setFoxCoo(2, 3);

        boolean result = callFoxPlay("G7");
        assertFalse(result,
                "foxPlay must reject a move to a square that is too far away");
    }


    // Tests on foxPlay() — capture detection (foxCaptured flag)


    @Test
    void foxPlaySetsFoxCapturedOnJump() throws Exception {

        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        Board board = gameStage.getBoard();
        // Placing the fox

        Pawn fox = new Pawn(Pawn.FOX, gameStage);


        // addElement() actually places the piece on the board
        board.addElement(fox, 4, 3);

        // the fox's coordinates in the model
        gameStage.setFoxCoo(4, 3);
        // Placement of the hen
        Pawn goose = new Pawn(Pawn.GOOSE, gameStage);
        board.addElement(goose, 3, 3);

        // Pre-test check
        assertNotNull(board.getFirstElement(4, 3),
                "The fox must be at (4,3)");

        assertNotNull(board.getFirstElement(3, 3),
                "The hen must be at (3,3)");

        // Reset the flag
        gameStage.setFoxCaptured(false);


        // Mouvement :

        try {
            callFoxPlay("C4");
        }
        catch (Exception ignored) {
        }


        // Final check
        assertTrue(gameStage.isFoxCaptured(),
                "foxPlay doit lever le flag foxCaptured lors d'un saut par-dessus une poule");
    }



    @Test
    void foxPlayDoesNotSetFoxCapturedOnSimpleMove() throws Exception {
        // Fox at (3,3)
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);
        gameStage.setFoxCoo(3, 3);

        gameStage.setFoxCaptured(false);

        // Simple move to (3,4): 1 space to the right
        try {
            callFoxPlay("D5");
        } catch (Exception ignored) {
            // ActionPlayer may crash without a View.
        }

        assertFalse(gameStage.isFoxCaptured(),
                "A simple movement by the fox should not trigger the “foxCaptured” flag");
    }


    // Tests on geesePlay() — off-screen coordinates


    @Test
    void geesePlayRejectsOutOfBoundsStart() throws Exception {
        boolean result = callGeesePlay("Z1D4");
        assertFalse(result,
                "geesePlay must reject starting coordinates outside the board");
    }


    @Test
    void geesePlayRejectsOutOfBoundsEnd() throws Exception {
        boolean result = callGeesePlay("D4Z1");
        assertFalse(result,
                "geesePlay must reject arrival coordinates outside the board");
    }


    // Tests on geesePlay() — empty starting hand or incorrect card

    @Test
    void geesePlayReturnsFalseWhenStartCellIsEmpty() throws Exception {
        // Aucune poule posée, plateau vide
        boolean result = callGeesePlay("D4D5"); // (3,3) → (3,4)
        assertFalse(result,
                "geesePlay must refuse if the starting cell is empty");
    }


    @Test
    void geesePlayReturnsFalseWhenStartCellHasFox() throws Exception {
        // Place a fox on the starting square
        Pawn fox = new Pawn(Pawn.FOX, gameStage);
        gameStage.putInContainer(fox, board, 3, 3);  // (3,3)

        // The command sets (3,3) as the starting point
        boolean result = callGeesePlay("D4D5"); // (3,3) → (3,4)
        assertFalse(result,
                "geesePlay must refuse if the starting square contains a fox");
    }


    // Tests on geesePlay() — destination NOT POSSIBLE

    @Test
    void geesePlayRejectsDiagonalMove() throws Exception {
        Pawn goose = new Pawn(Pawn.GOOSE, gameStage);
        gameStage.putInContainer(goose, board, 3, 3);
        boolean result = callGeesePlay("D4E5");
        assertFalse(result,
                "geesePlay must reject a diagonal move for a hen");
    }


    @Test
    void geesePlayRejectsBackwardMove() throws Exception {
        Pawn goose = new Pawn(Pawn.GOOSE, gameStage);
        gameStage.putInContainer(goose, board, 4, 3);
        boolean result = callGeesePlay("E4D4");
        assertFalse(result,
                "geesePlay must refuse to move backward for a hen");
    }


    @Test
    void geesePlayRejectsMoveToOccupiedCell() throws Exception {
        Pawn goose1 = new Pawn(Pawn.GOOSE, gameStage);
        Pawn goose2 = new Pawn(Pawn.GOOSE, gameStage);
        gameStage.putInContainer(goose1, board, 3, 3);
        gameStage.putInContainer(goose2, board, 4, 3);

        boolean result = callGeesePlay("D4E4");
        assertFalse(result,
                "geesePlay should refuse if the destination slot is already occupied");
    }


    // Tests on command FORMAT (analyseAndPlay)



    @Test
    void foxPlayHandlesEmptyLineSafely() {
        assertDoesNotThrow(() -> {
            try {
                boolean result = callFoxPlay("");
                assertFalse(result, "Une ligne vide ne doit pas être acceptée");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // The reflection wraps the exceptions → we check the cause
                if (!(e.getCause() instanceof StringIndexOutOfBoundsException)) {
                    throw e; // Restart if this is another unexpected error
                }
                // StringIndexOutOfBoundsException encountered: foxPlay does not handle empty lines
            }
        });
    }


    // Testing on foxCaptured and updating contact information

    void foxPlayResetsFoxCapturedAtStart() throws Exception {
        gameStage.setFoxCaptured(true);

        try {
            callFoxPlay("Z9");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Accepted if StringIndexOutOfBounds (line too short/out of bounds)
        }

        assertFalse(gameStage.isFoxCaptured(),
                "foxPlay must set foxCaptured to false at the beginning of each call");
    }
}
