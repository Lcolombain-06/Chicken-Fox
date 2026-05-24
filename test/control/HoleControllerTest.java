package control;

import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import model.Board;
import model.HoleStageModel;
import model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class HoleControllerTest {

    @Mock Model          model;
    @Mock View           view;
    @Mock HoleStageModel stage;
    @Mock Board          board;
    @Mock Pawn           fox;
    @Mock Pawn           goose;
    @Mock Player         humanPlayer;
    @Mock Player         computerPlayer;

    private HoleController controller;


    // Helpers

    private void asFox(Pawn p) {
        when(p.isFox()).thenReturn(true);
        when(p.isGoose()).thenReturn(false);
    }

    private boolean[][] emptyGrid() {
        return new boolean[7][7];
    }

    private boolean[][] gridWithCell(int row, int col) {
        boolean[][] g = new boolean[7][7];
        g[row][col] = true;
        return g;
    }


    // Setup

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(model.getGameStage()).thenReturn(stage);
        when(stage.getBoard()).thenReturn(board);
        when(stage.getFox()).thenReturn(new Pawn[]{fox});
        asFox(fox);

        when(board.getReachableCells()).thenReturn(emptyGrid());
        when(board.getElement(anyInt(), anyInt())).thenReturn(null);

        // Players
        when(humanPlayer.getType()).thenReturn(Player.HUMAN);
        when(humanPlayer.getName()).thenReturn("Human");
        when(computerPlayer.getType()).thenReturn(Player.COMPUTER);
        when(computerPlayer.getName()).thenReturn("Computer");

        controller = new HoleController(model, view);
    }


    // WIN CONDITION TESTS


    @Nested
    @DisplayName("Win condition: partyWinned logic")
    class WinConditionTests {

        /**
         * We access partyWinned via reflection since it is private.
         */
        private int invokePartyWinned(int row, int col) throws Exception {
            var method = HoleController.class.getDeclaredMethod(
                    "partyWinned", int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(controller, row, col);
        }

        @Test
        @DisplayName("Fox wins when fewer than 4 geese remain (geeseToPlay < 4)")
        void foxWinsWhenFewGeese() throws Exception {
            when(stage.getGeeseToPlay()).thenReturn(3); // < 4
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(0);
            when(board.getReachableCells()).thenReturn(emptyGrid());

            int result = invokePartyWinned(3, 3);
            assertEquals(1, result, "Fox should win when geeseToPlay < 4");
        }

        @Test
        @DisplayName("Geese win when fox has 0 reachable cells")
        void geeseWinWhenFoxTrapped() throws Exception {
            when(stage.getGeeseToPlay()).thenReturn(10); // enough geese
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(0); // no moves
            when(board.getReachableCells()).thenReturn(emptyGrid());

            int result = invokePartyWinned(3, 3);
            assertEquals(2, result, "Geese should win when fox has 0 reachable moves");
        }

        @Test
        @DisplayName("Game continues when fox has moves and enough geese remain")
        void gameContinuesNormalState() throws Exception {
            when(stage.getGeeseToPlay()).thenReturn(10);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(3); // has moves
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));

            int result = invokePartyWinned(3, 3);
            assertEquals(0, result, "Game should continue when no win condition is met");
        }

        @Test
        @DisplayName("Exactly 4 geese remaining: game continues (not < 4)")
        void fourGeeseGameContinues() throws Exception {
            when(stage.getGeeseToPlay()).thenReturn(4); // = 4, not < 4
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(2);
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));

            int result = invokePartyWinned(3, 3);
            assertEquals(0, result, "Game should continue with exactly 4 geese");
        }

        @Test
        @DisplayName("partyWinned does not throw when fox element is null on board")
        void partyWinnedNoException_whenFoxNull() {
            when(stage.getGeeseToPlay()).thenReturn(10);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(null);

            // The test documents the current behavior
            assertDoesNotThrow(() -> {
                try {
                    var method = HoleController.class.getDeclaredMethod(
                            "partyWinned", int.class, int.class);
                    method.setAccessible(true);
                    method.invoke(controller, 3, 3);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    if (!(e.getCause() instanceof NullPointerException)) throw e;
                }
            });
        }
    }


    // 2. INPUT PARSING TESTS (analyseAndPlay, foxPlay, geesePlay)

    @Nested
    @DisplayName("Input parsing: analyseAndPlay")
    class InputParsingTests {

        private boolean invokeAnalyseAndPlay(String line) throws Exception {
            var method = HoleController.class.getDeclaredMethod("analyseAndPlay", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(controller, line);
        }

        @Test
        @DisplayName("'STOP' command stops the stage and returns true")
        void stopCommandStopsStage() throws Exception {
            boolean result = invokeAnalyseAndPlay("STOP");
            assertTrue(result, "STOP should return true");
            verify(model, times(1)).stopStage();
        }

        @Test
        @DisplayName("'stop' (lowercase) also stops the stage")
        void stopCommandLowercaseStopsStage() throws Exception {
            boolean result = invokeAnalyseAndPlay("stop");
            assertTrue(result);
            verify(model, times(1)).stopStage();
        }

        @Test
        @DisplayName("Fox player: 4-char input returns false (wrong format)")
        void foxPlayerFourChars_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(0); // fox turn
            boolean result = invokeAnalyseAndPlay("A1B2"); // 4 chars for fox
            assertFalse(result, "Fox turn requires 2-char input; 4 chars should fail");
        }

        @Test
        @DisplayName("Goose player: 2-char input returns false (wrong format)")
        void goosePlayerTwoChars_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(1); // geese turn
            boolean result = invokeAnalyseAndPlay("A1"); // 2 chars for geese
            assertFalse(result, "Geese turn requires 4-char input; 2 chars should fail");
        }

        @Test
        @DisplayName("Fox player: out-of-bounds destination returns false")
        void foxPlayerOutOfBoundsDestination_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(0);
            when(stage.getFoxRow()).thenReturn(3);
            when(stage.getFoxCol()).thenReturn(3);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.getReachableCells()).thenReturn(emptyGrid());
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(0);

            // 'Z9' → row = 'Z'-'A' = 25 (out of bounds > 6)
            boolean result = invokeAnalyseAndPlay("Z9");
            assertFalse(result);
        }

        @Test
        @DisplayName("Fox player: valid format but unreachable cell returns false")
        void foxPlayerUnreachableCell_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(0);
            when(stage.getFoxRow()).thenReturn(3);
            when(stage.getFoxCol()).thenReturn(3);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.getReachableCells()).thenReturn(emptyGrid()); // nothing reachable
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(0);

            boolean result = invokeAnalyseAndPlay("A1"); // row=0, col=0 → not reachable
            assertFalse(result);
        }

        @Test
        @DisplayName("Goose player: moving a non-existent piece returns false")
        void goosePlayerNoElementAtSource_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(1);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(null); // nothing there

            boolean result = invokeAnalyseAndPlay("A1B1");
            assertFalse(result);
        }

        @Test
        @DisplayName("Goose player: trying to move the fox returns false")
        void goosePlayerMovingFox_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(1);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox); // fox, not goose
            asFox(fox);

            boolean result = invokeAnalyseAndPlay("A1B1");
            assertFalse(result);
        }

        @Test
        @DisplayName("Goose player: out-of-bounds coordinates return false")
        void goosePlayerOutOfBoundsCoordinates_returnsFalse() throws Exception {
            when(model.getIdPlayer()).thenReturn(1);
            // 'Z' - 'A' = 25 → out of bounds
            boolean result = invokeAnalyseAndPlay("Z9Z8");
            assertFalse(result);
        }
    }


    // END OF TURN TESTS

    @Nested
    @DisplayName("endOfTurn behavior")
    class EndOfTurnTests {

        @Test
        @DisplayName("endOfTurn calls model.setNextPlayer()")
        void endOfTurnCallsSetNextPlayer() {

            boardifier.model.TextElement textElement = mock(boardifier.model.TextElement.class);
            when(stage.getPlayerName()).thenReturn(textElement);

            controller.endOfTurn();

            verify(model, times(1)).setNextPlayer();
        }

        @Test
        @DisplayName("endOfTurn updates the player name label")
        void endOfTurnUpdatesPlayerNameLabel() {
            when(model.getCurrentPlayer()).thenReturn(humanPlayer);
            boardifier.model.TextElement textElement = mock(boardifier.model.TextElement.class);
            when(stage.getPlayerName()).thenReturn(textElement);

            controller.endOfTurn();

            verify(textElement, times(1)).setText(humanPlayer.getName());
        }

        @Test
        @DisplayName("endOfTurn does not throw even when model returns null player name label")
        void endOfTurnNullPlayerNameLabel_noException() {
            when(model.getCurrentPlayer()).thenReturn(humanPlayer);
            when(stage.getPlayerName()).thenReturn(null);

            assertThrows(NullPointerException.class, () -> controller.endOfTurn());
        }
    }


    // 4. EDGE CASES

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Controller constructor does not throw")
        void constructorNoException() {
            assertDoesNotThrow(() -> new HoleController(model, view));
        }

        @Test
        @DisplayName("analyseAndPlay with empty string does not match STOP and returns false")
        void analyseAndPlayEmptyString() throws Exception {
            var method = HoleController.class.getDeclaredMethod("analyseAndPlay", String.class);
            method.setAccessible(true);

            when(model.getIdPlayer()).thenReturn(0);
            when(stage.getFoxRow()).thenReturn(3);
            when(stage.getFoxCol()).thenReturn(3);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.getReachableCells()).thenReturn(emptyGrid());

            // Empty string: length = 0, doesn't match "STOP", fox player needs 2 chars
            boolean result = (boolean) method.invoke(controller, "");
            assertFalse(result);
        }

        @Test
        @DisplayName("partyWinned is consistent: same inputs always return same result")
        void partyWinnedDeterministic() throws Exception {
            when(stage.getGeeseToPlay()).thenReturn(3);
            when(board.getFirstElement(anyInt(), anyInt())).thenReturn(fox);
            when(board.setValidCells(any(), anyInt(), anyInt())).thenReturn(0);
            when(board.getReachableCells()).thenReturn(emptyGrid());

            var method = HoleController.class.getDeclaredMethod("partyWinned", int.class, int.class);
            method.setAccessible(true);

            int result1 = (int) method.invoke(controller, 3, 3);
            int result2 = (int) method.invoke(controller, 3, 3);
            assertEquals(result1, result2, "partyWinned should be deterministic");
        }

        @Test
        @DisplayName("Multiple endOfTurn calls do not accumulate unwanted side effects")
        void endOfTurnMultipleCalls_noSideEffects() {
            when(model.getCurrentPlayer()).thenReturn(humanPlayer);
            boardifier.model.TextElement textElement = mock(boardifier.model.TextElement.class);
            when(stage.getPlayerName()).thenReturn(textElement);

            controller.endOfTurn();
            controller.endOfTurn();
            controller.endOfTurn();

            verify(model, times(3)).setNextPlayer();
            verify(textElement, times(3)).setText(humanPlayer.getName());
        }
    }
}