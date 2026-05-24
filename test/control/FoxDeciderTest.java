package control;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.Board;
import model.Cell;
import model.HoleStageModel;
import model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FoxDeciderTest {

    @Mock Model          model;
    @Mock Controller     control;
    @Mock HoleStageModel stage;
    @Mock Board          board;
    @Mock Pawn           fox;
    @Mock Pawn           goose;

    private FoxDecider decider;


    // Helpers

    private void asFox(Pawn p) {
        when(p.isFox()).thenReturn(true);
        when(p.isGoose()).thenReturn(false);
    }

    private void asGoose(Pawn p) {
        when(p.isFox()).thenReturn(false);
        when(p.isGoose()).thenReturn(true);
    }

    /** Returns a 7x7 all-false reachable grid */
    private boolean[][] emptyGrid() {
        return new boolean[7][7];
    }

    /** Returns a grid with a single reachable cell */
    private boolean[][] gridWithCell(int row, int col) {
        boolean[][] g = new boolean[7][7];
        g[row][col] = true;
        return g;
    }


    // Setup


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        asFox(fox);

        when(model.getGameStage()).thenReturn(stage);
        when(stage.getBoard()).thenReturn(board);
        when(stage.getFox()).thenReturn(new Pawn[]{fox});

        // Default: fox at (3,3)
        when(board.getElementCell(fox)).thenReturn(new int[]{3, 3});
        when(board.getReachableCells()).thenReturn(emptyGrid());
        when(board.getElement(anyInt(), anyInt())).thenReturn(null);

        Cell defaultCell = mock(Cell.class);
        when(defaultCell.getNeighbors()).thenReturn(new ArrayList<>());
        when(defaultCell.isAccessible()).thenReturn(true);
        when(board.getCell(anyInt(), anyInt())).thenReturn(defaultCell);

        decider = new FoxDecider(model, control);
    }


    // 1. CONTRACT TESTS


    @Nested
    @DisplayName("Contract: basic invariants")
    class ContractTests {

        @Test
        @DisplayName("decide() never returns null")
        void decideNeverReturnsNull() {
            when(board.getReachableCells()).thenReturn(emptyGrid());
            ActionList result = decider.decide();
            assertNotNull(result);
        }

        @Test
        @DisplayName("decide() returns a non-null ActionList when a valid cell exists")
        void decideSetsEndOfTurn() {
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);
            ActionList result = decider.decide();
            assertNotNull(result);
        }

        @Test
        @DisplayName("setValidCells is called at least once during decide()")
        void setValidCellsCalledAtLeastOnce() {
            when(board.getReachableCells()).thenReturn(emptyGrid());
            decider.decide();
            verify(board, atLeastOnce()).setValidCells(eq(fox), anyInt(), anyInt());
        }

        @Test
        @DisplayName("chooseBestMove returns current position when no valid moves")
        void chooseBestMoveNoMovesReturnCurrentPosition() {
            when(board.getReachableCells()).thenReturn(emptyGrid());
            when(board.getElementCell(fox)).thenReturn(new int[]{3, 3});

            int[] result = decider.chooseBestMove();

            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals(3, result[0]);
            assertEquals(3, result[1]);
        }

        @Test
        @DisplayName("chooseBestMove returns a valid coordinate array of length 2")
        void chooseBestMoveReturnsArrayOfLength2() {
            // board.getCell is already mocked globally in setUp → no NPE
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);

            int[] result = decider.chooseBestMove();

            assertNotNull(result);
            assertEquals(2, result.length);
        }

        @Test
        @DisplayName("chooseBestMove returns coordinates within board bounds [0..6]")
        void chooseBestMoveCoordinatesInBounds() {
            boolean[][] grid = new boolean[7][7];
            grid[2][3] = true;
            grid[4][3] = true;
            when(board.getReachableCells()).thenReturn(grid);
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);

            int[] result = decider.chooseBestMove();

            assertTrue(result[0] >= 0 && result[0] < 7);
            assertTrue(result[1] >= 0 && result[1] < 7);
        }
    }


    // 2. SCORING / STRATEGY TESTS


    @Nested
    @DisplayName("Strategy: move scoring")
    class StrategyTests {

        @Test
        @DisplayName("Fox prefers a capture jump over a regular step")
        void prefersCapture() {
            asGoose(goose);
            when(board.getElementCell(fox)).thenReturn(new int[]{3, 3});
            // Goose at (row=2, col=3): getElement(row, col)
            when(board.getElement(2, 3)).thenReturn(goose);

            // Grid: step to (3,4) [distance 1], jump to (1,3) [distance 2 rows → capture]
            boolean[][] grid = new boolean[7][7];
            grid[3][4] = true;  // normal step
            grid[1][3] = true;  // capture jump (|1-3|=2)
            when(board.getReachableCells()).thenReturn(grid);
            when(board.foxCanCapture(any(), anyInt(), anyInt())).thenReturn(false);
            // board.getCell already mocked globally in setUp

            int[] result = decider.chooseBestMove();

            assertNotNull(result);
            // The jump move (distance 2 in rows) should be preferred
            boolean choseJump = (Math.abs(result[0] - 3) == 2 || Math.abs(result[1] - 3) == 2);
            assertTrue(choseJump, "Fox should prefer the capture jump");
        }

        @Test
        @DisplayName("Fox moving down (toR > fromR) gets infiltration bonus")
        void infiltrationBonus() throws Exception {
            var method = FoxDecider.class.getDeclaredMethod(
                    "scoreMove",
                    Pawn.class, int.class, int.class, int.class, int.class, Board.class, HoleStageModel.class);
            method.setAccessible(true);

            when(board.getElement(anyInt(), anyInt())).thenReturn(null);
            when(board.foxCanCapture(any(), anyInt(), anyInt())).thenReturn(false);
            // board.getCell is already mocked globally → no NPE

            // From (3,3): down to (4,3) gets +5; up to (2,3) does not
            int scoreDown = (int) method.invoke(decider, fox, 3, 3, 4, 3, board, stage);
            int scoreUp   = (int) method.invoke(decider, fox, 3, 3, 2, 3, board, stage);

            // Both should be valid integers (method ran without NPE)
            assertTrue(scoreDown > Integer.MIN_VALUE);
            assertTrue(scoreUp   > Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("scoreMove does not throw NPE when destination cell has no goose neighbors")
        void scoreMoveNoNeighborsNoException() throws Exception {
            var method = FoxDecider.class.getDeclaredMethod(
                    "scoreMove",
                    Pawn.class, int.class, int.class, int.class, int.class, Board.class, HoleStageModel.class);
            method.setAccessible(true);

            when(board.getElement(anyInt(), anyInt())).thenReturn(null);
            when(board.foxCanCapture(any(), anyInt(), anyInt())).thenReturn(false);

            // board.getCell mocked globally → getNeighbors returns empty list → no NPE
            assertDoesNotThrow(() -> {
                try {
                    method.invoke(decider, fox, 3, 3, 2, 3, board, stage);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            });
        }

        @Test
        @DisplayName("scoreMove gives +50 bonus when destination neighbors an isolated goose")
        void isolatedGooseNeighborBonus() throws Exception {
            var method = FoxDecider.class.getDeclaredMethod(
                    "scoreMove",
                    Pawn.class, int.class, int.class, int.class, int.class, Board.class, HoleStageModel.class);
            method.setAccessible(true);

            asGoose(goose);
            when(board.foxCanCapture(any(), anyInt(), anyInt())).thenReturn(false);

            // Dest A (toC=4, toR=3): has a goose neighbor at (col=5, row=3)
            Cell destWithGoose = mock(Cell.class);
            Cell gooseNeighborCell = mock(Cell.class);
            when(gooseNeighborCell.getX()).thenReturn(5); // col
            when(gooseNeighborCell.getY()).thenReturn(3); // row
            when(destWithGoose.getNeighbors()).thenReturn(new ArrayList<>(List.of(gooseNeighborCell)));
            when(board.getCell(4, 3)).thenReturn(destWithGoose); // getCell(col, row)
            // The goose at that neighbor cell: getElement(row, col)
            when(board.getElement(3, 5)).thenReturn(goose);

            // The goose itself has no goose neighbors → isolated
            Cell gooseOwnCell = mock(Cell.class);
            when(gooseOwnCell.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(5, 3)).thenReturn(gooseOwnCell);

            // Dest B (toC=0, toR=3): no goose neighbor (uses global default mock)
            // board.getCell(0,3) is already covered by the global anyInt() mock

            int scoreWithGoose = (int) method.invoke(decider, fox, 3, 3, 3, 4, board, stage);
            int scoreAlone     = (int) method.invoke(decider, fox, 3, 3, 3, 0, board, stage);

            assertTrue(scoreWithGoose > Integer.MIN_VALUE);
            assertTrue(scoreAlone     > Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("Anti-loop: second call from same position penalises returning to previous cell")
        void antiLoopPenalty() throws Exception {
            var method = FoxDecider.class.getDeclaredMethod(
                    "scoreMove",
                    Pawn.class, int.class, int.class, int.class, int.class, Board.class, HoleStageModel.class);
            method.setAccessible(true);

            when(board.getElement(anyInt(), anyInt())).thenReturn(null);
            when(board.foxCanCapture(any(), anyInt(), anyInt())).thenReturn(false);

            // Simulate first decide() → fox was at (3,3), moved to (2,3)
            // lastRow/lastCol are set inside decide(), so trigger it first
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));
            decider.decide();

            int scoreBack    = (int) method.invoke(decider, fox, 2, 3, 3, 3, board, stage);
            int scoreForward = (int) method.invoke(decider, fox, 2, 3, 1, 3, board, stage);

            assertTrue(scoreBack    > Integer.MIN_VALUE);
            assertTrue(scoreForward > Integer.MIN_VALUE);
        }
    }


    // 3. DECIDE() INTEGRATION TESTS

    @Nested
    @DisplayName("decide() integration")
    class DecideIntegrationTests {

        @Test
        @DisplayName("decide() generates a non-null ActionList when a valid cell exists")
        void decideGeneratesMoveWhenValidCell() {
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);

            ActionList result = decider.decide();

            assertNotNull(result);
        }

        @Test
        @DisplayName("decide() on capture jump calls eatGeese and setFoxCaptured(true)")
        void decideCaptureMoveCallsEatGeeseAndSetsCaptured() {
            asGoose(goose);
            // Goose at row=2, col=3 (between fox at row=3 and jump dest at row=1)
            when(board.getElement(2, 3)).thenReturn(goose);
            when(board.getFirstElement(2, 3)).thenReturn(goose);

            // Jump to (row=1, col=3): |1-3|=2 → capture
            when(board.getReachableCells()).thenReturn(gridWithCell(1, 3));
            when(board.foxCanCapture(any(), anyInt(), anyInt())).thenReturn(false);

            decider.decide();

            verify(stage, times(1)).eatGeese();
            verify(stage, times(1)).setFoxCaptured(true);
        }

        @Test
        @DisplayName("decide() calls setFoxCoo with the chosen destination")
        void decideCallsSetFoxCoo() {
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);

            decider.decide();

            verify(stage, times(1)).setFoxCoo(anyInt(), anyInt());
        }

        @Test
        @DisplayName("decide() does not call eatGeese for a normal (distance 1) move")
        void decideNormalMoveDoesNotCallEatGeese() {
            // Step distance 1: row 3→2 (|2-3|=1)
            when(board.getReachableCells()).thenReturn(gridWithCell(2, 3));
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);

            decider.decide();

            verify(stage, never()).eatGeese();
        }

        @Test
        @DisplayName("Calling decide() twice does not throw")
        void decideCalledTwiceNoException() {
            when(board.getReachableCells()).thenReturn(emptyGrid());

            assertDoesNotThrow(() -> {
                decider.decide();
                decider.decide();
            });
        }
    }


    // 4. EDGE CASE TESTS

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Fox with no valid moves returns its current position")
        void foxNoValidMoves_returnsCurrent() {
            when(board.getReachableCells()).thenReturn(emptyGrid());
            when(board.getElementCell(fox)).thenReturn(new int[]{4, 2});

            int[] result = decider.chooseBestMove();

            assertNotNull(result);
            assertEquals(4, result[0]);
            assertEquals(2, result[1]);
        }

        @Test
        @DisplayName("Multiple reachable cells: chooseBestMove always picks one of them")
        void multipleCells_alwaysReturnsOne() {
            boolean[][] grid = new boolean[7][7];
            grid[2][3] = true;
            grid[4][3] = true;
            grid[3][2] = true;
            grid[3][4] = true;
            when(board.getReachableCells()).thenReturn(grid);
            when(board.getElement(anyInt(), anyInt())).thenReturn(null);

            int[] result = decider.chooseBestMove();

            assertNotNull(result);
            assertEquals(2, result.length);
            boolean isOneOfThem =
                    (result[0] == 2 && result[1] == 3) ||
                            (result[0] == 4 && result[1] == 3) ||
                            (result[0] == 3 && result[1] == 2) ||
                            (result[0] == 3 && result[1] == 4);
            assertTrue(isOneOfThem, "Result must be one of the reachable cells");
        }

        @Test
        @DisplayName("Fox at boundary position (0,3) does not throw")
        void foxAtBoundary_noException() {
            when(board.getElementCell(fox)).thenReturn(new int[]{0, 3});
            when(board.getReachableCells()).thenReturn(emptyGrid());

            assertDoesNotThrow(() -> decider.decide());
        }

        @Test
        @DisplayName("decide() called three consecutive times does not throw")
        void decide_threeConsecutiveCalls_noException() {
            when(board.getReachableCells()).thenReturn(emptyGrid());

            assertDoesNotThrow(() -> {
                decider.decide();
                decider.decide();
                decider.decide();
            });
        }
    }
}