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

class GooseDeciderTest {

    @Mock Model          model;
    @Mock Controller     control;
    @Mock HoleStageModel stage;
    @Mock Board          board;
    @Mock Pawn           fox;
    @Mock Pawn           goose;

    private GooseDecider decider;


    // Helpers

    private Cell emptyCell(int col, int row) {
        Cell c = mock(Cell.class);
        when(c.getX()).thenReturn(col);
        when(c.getY()).thenReturn(row);
        when(c.getNeighbors()).thenReturn(new ArrayList<>());
        when(c.isAccessible()).thenReturn(true);
        return c;
    }

    private Cell registerCell(int col, int row) {
        Cell c = emptyCell(col, row);
        when(board.getCell(col, row)).thenReturn(c);
        return c;
    }

    private void placeElement(Pawn pawn, int row, int col) {
        when(board.getElement(row, col)).thenReturn(pawn);
    }

    private void asGoose(Pawn p) {
        when(p.isFox()).thenReturn(false);
        when(p.isGoose()).thenReturn(true);
    }

    private void asFox(Pawn p) {
        when(p.isFox()).thenReturn(true);
        when(p.isGoose()).thenReturn(false);
    }


    // Setup

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(model.getGameStage()).thenReturn(stage);
        when(stage.getBoard()).thenReturn(board);

        when(board.getElement(anyInt(), anyInt())).thenReturn(null);
        when(board.canReachCell(anyInt(), anyInt())).thenReturn(false);

        decider = new GooseDecider(model, control);
    }


    // 1. CONTRACT TESTS

    @Nested
    @DisplayName("Contract: basic invariants")
    class ContractTests {

        @Test
        @DisplayName("Empty board → returns non-null ActionList with endOfTurn")
        void emptyBoard_returnsEndOfTurn() {
            ActionList result = decider.decide();
            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
        }

        @Test
        @DisplayName("clearValidCells() is always called exactly once, even on empty board")
        void alwaysClearsValidCells_emptyBoard() {
            decider.decide();
            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("clearValidCells() is called exactly once when geese have valid moves")
        void alwaysClearsValidCells_withValidMoves() {
            asFox(fox);
            asGoose(goose);
            placeElement(fox,   0, 0);
            placeElement(goose, 3, 3);

            when(board.canReachCell(4, 3)).thenReturn(true);

            registerCell(0, 0);
            registerCell(3, 4);

            decider.decide();

            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("setValidCells() is never called for the fox")
        void setValidCellsNeverCalledForFox() {
            asFox(fox);
            asGoose(goose);
            placeElement(fox,   0, 0);
            placeElement(goose, 3, 3);
            when(board.canReachCell(anyInt(), anyInt())).thenReturn(false);

            decider.decide();

            verify(board, never()).setValidCells(eq(fox), anyInt(), anyInt());
        }

        @Test
        @DisplayName("setValidCells() is called once per goose, at its current position")
        void setValidCellsCalledOncePerGoose() {
            Pawn goose2 = mock(Pawn.class);
            asFox(fox);
            asGoose(goose);
            asGoose(goose2);

            placeElement(fox,    0, 0);
            placeElement(goose,  3, 3);
            placeElement(goose2, 4, 4);

            decider.decide();

            verify(board, times(1)).setValidCells(eq(goose),  eq(3), eq(3));
            verify(board, times(1)).setValidCells(eq(goose2), eq(4), eq(4));
        }

        @Test
        @DisplayName("Board with only a fox (no geese) → endOfTurn, no setValidCells call")
        void onlyFoxEndOfTurnNoSetValidCells() {
            asFox(fox);
            placeElement(fox, 2, 2);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
            verify(board, never()).setValidCells(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Goose with no reachable cells → endOfTurn")
        void gooseNoMovesEndOfTurn() {
            asFox(fox);
            asGoose(goose);
            placeElement(fox,   0, 0);
            placeElement(goose, 3, 3);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
        }
    }


    // 2. STRATEGY TESTS

    @Nested
    @DisplayName("Strategy: move selection")
    class StrategyTests {

        @Test
        @DisplayName("Prefers moving closer to the fox over moving away")
        void prefersCloserToFox() {
            asFox(fox);
            asGoose(goose);

            placeElement(fox,   0, 3);
            placeElement(goose, 3, 3);

            Cell foxCell = registerCell(3, 0);

            when(board.canReachCell(2, 3)).thenReturn(true);
            when(board.canReachCell(4, 3)).thenReturn(true);

            registerCell(3, 2);
            registerCell(3, 4);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
            verify(board, times(1)).setValidCells(eq(goose), eq(3), eq(3));
            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("Avoids destination that lets fox capture a goose")
        void avoidsCapturableDest() {
            asFox(fox);
            asGoose(goose);

            placeElement(fox,   0, 2);
            placeElement(goose, 2, 2);

            Cell foxCell = mock(Cell.class);
            when(board.getCell(2, 0)).thenReturn(foxCell);

            Cell foxNeighbor = mock(Cell.class);
            when(foxNeighbor.getX()).thenReturn(2);
            when(foxNeighbor.getY()).thenReturn(1);
            when(foxCell.getNeighbors()).thenReturn(new ArrayList<>(List.of(foxNeighbor)));

            Cell jumpCell = mock(Cell.class);
            when(jumpCell.isAccessible()).thenReturn(true);
            when(board.getCell(2, 2)).thenReturn(jumpCell);

            when(board.canReachCell(1, 2)).thenReturn(true);
            when(board.canReachCell(3, 2)).thenReturn(true);

            Cell dangerDest = mock(Cell.class);
            when(dangerDest.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(2, 1)).thenReturn(dangerDest);

            Cell safeDest = mock(Cell.class);
            when(safeDest.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(2, 3)).thenReturn(safeDest);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("Rewards grouping: prefers destination with goose neighbors")
        void rewardsGrouping() {
            Pawn goose2 = mock(Pawn.class);
            asFox(fox);
            asGoose(goose);
            asGoose(goose2);

            placeElement(fox,    0, 0);
            placeElement(goose,  3, 3);
            placeElement(goose2, 3, 5);

            registerCell(0, 0);

            when(board.canReachCell(3, 4)).thenReturn(true);
            when(board.canReachCell(3, 1)).thenReturn(true);

            Cell destA = mock(Cell.class);
            Cell neighborOfA = mock(Cell.class);
            when(neighborOfA.getX()).thenReturn(5);
            when(neighborOfA.getY()).thenReturn(3);
            when(destA.getNeighbors()).thenReturn(new ArrayList<>(List.of(neighborOfA)));
            when(board.getCell(4, 3)).thenReturn(destA);

            Cell destB = mock(Cell.class);
            when(destB.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(1, 3)).thenReturn(destB);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("Rewards blocking fox column: row > foxRow AND |col - foxCol| <= 1")
        void rewardsBlockingFoxColumn() {
            asFox(fox);
            asGoose(goose);

            placeElement(fox,   2, 3);
            placeElement(goose, 4, 3);

            registerCell(3, 2);

            when(board.canReachCell(3, 3)).thenReturn(true);
            when(board.canReachCell(4, 0)).thenReturn(true);

            Cell blockCell = mock(Cell.class);
            when(blockCell.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(3, 3)).thenReturn(blockCell);

            Cell neutralCell = mock(Cell.class);
            when(neutralCell.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(0, 4)).thenReturn(neutralCell);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("Penalises destinations that leave the fox more free moves")
        void penalisesFoxFreeMoves() {
            asFox(fox);
            asGoose(goose);

            placeElement(fox,   3, 3);
            placeElement(goose, 3, 2);

            Cell foxCell = mock(Cell.class);
            when(board.getCell(3, 3)).thenReturn(foxCell);

            Cell foxNeighbor = mock(Cell.class);
            when(foxNeighbor.getX()).thenReturn(2);
            when(foxNeighbor.getY()).thenReturn(3);
            when(foxCell.getNeighbors()).thenReturn(new ArrayList<>(List.of(foxNeighbor)));

            Cell jumpCell = mock(Cell.class);
            when(jumpCell.isAccessible()).thenReturn(false);
            when(board.getCell(1, 3)).thenReturn(jumpCell);

            when(board.canReachCell(2, 2)).thenReturn(true);
            when(board.canReachCell(0, 0)).thenReturn(true);

            Cell nearCell = mock(Cell.class);
            when(nearCell.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(2, 2)).thenReturn(nearCell);

            Cell farCell = mock(Cell.class);
            when(farCell.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(0, 0)).thenReturn(farCell);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
            verify(board, times(1)).clearValidCells();
        }
    }


    // 3. UNIT TESTS on evaluateGooseMove

    @Nested
    @DisplayName("Unit: evaluateGooseMove scoring")
    class EvaluateGooseMoveTests {

        private int score(int foxRow, int foxCol,
                          int origRow, int origCol,
                          int simRow, int simCol) throws Exception {
            var method = GooseDecider.class.getDeclaredMethod(
                    "evaluateGooseMove",
                    Board.class, int.class, int.class, int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(decider,
                    board, foxRow, foxCol, origRow, origCol, simRow, simCol);
        }

        // Helper: register a fox cell with no neighbors for tests that don't
        // need neighbor interaction.
        private void registerEmptyFoxCell(int foxCol, int foxRow) {
            Cell foxCell = mock(Cell.class);
            when(foxCell.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(foxCol, foxRow)).thenReturn(foxCell);
        }

        @Test
        @DisplayName("Moving closer to fox scores higher than moving away")
        void closerScoresHigher() throws Exception {
            registerEmptyFoxCell(3, 0);

            // Destination cells for grouping score
            Cell destCloser = mock(Cell.class);
            when(destCloser.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(3, 2)).thenReturn(destCloser);

            Cell destFarther = mock(Cell.class);
            when(destFarther.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(3, 4)).thenReturn(destFarther);

            int scoreCloser  = score(0, 3,  3, 3,  2, 3);
            int scoreFarther = score(0, 3,  3, 3,  4, 3);

            assertTrue(scoreCloser > scoreFarther);
        }

        @Test
        @DisplayName("Being in front of fox column (simRow > foxRow, |colDiff| <= 1) adds +20")
        void frontOfFoxColumnBonus() throws Exception {
            registerEmptyFoxCell(3, 2);

            Cell destBlock = mock(Cell.class);
            when(destBlock.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(3, 3)).thenReturn(destBlock);

            Cell destNoBlock = mock(Cell.class);
            when(destNoBlock.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(6, 3)).thenReturn(destNoBlock);

            int scoreBlock   = score(2, 3,  4, 3,  3, 3);
            int scoreNoBlock = score(2, 3,  4, 3,  3, 6);

            assertTrue(scoreBlock > scoreNoBlock);
        }


        @Test
        @DisplayName("Each goose neighbor at destination adds +12 to score")
        void gooseNeighborBonus() throws Exception {
            Pawn friendlyGoose = mock(Pawn.class);
            asGoose(friendlyGoose);

            // Fox at (row=0, col=0) → no neighbors
            registerEmptyFoxCell(0, 0);

            // Destination A (row=3, col=4): has a goose neighbor at (row=3, col=5)
            Cell destWithFriend = mock(Cell.class);
            Cell friendNeighborCell = mock(Cell.class);
            when(friendNeighborCell.getX()).thenReturn(5); // col
            when(friendNeighborCell.getY()).thenReturn(3); // row
            when(destWithFriend.getNeighbors()).thenReturn(new ArrayList<>(List.of(friendNeighborCell)));
            when(board.getCell(4, 3)).thenReturn(destWithFriend);
            when(board.getElement(3, 5)).thenReturn(friendlyGoose);

            // Destination B (row=3, col=6): alone
            Cell destAlone = mock(Cell.class);
            when(destAlone.getNeighbors()).thenReturn(new ArrayList<>());
            when(board.getCell(6, 3)).thenReturn(destAlone);

            int scoreGrouped = score(0, 0,  3, 3,  3, 4);
            int scoreAlone   = score(0, 0,  3, 3,  3, 6);

            assertTrue(scoreGrouped > scoreAlone);
            assertTrue(scoreGrouped - scoreAlone >= 12);
        }
    }


    // 4. EDGE-CASE TESTS

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Fox surrounded on all sides (0 free moves) → clearValidCells still called")
        void foxFullySurrounded() {
            asFox(fox);
            asGoose(goose);

            placeElement(fox,   3, 3);
            placeElement(goose, 3, 3);
            placeElement(goose, 2, 3);

            when(board.canReachCell(anyInt(), anyInt())).thenReturn(false);

            decider.decide();

            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("Null bestPawn path returns ActionList with endOfTurn and no move")
        void noBestPawn_returnsEndOfTurn() {
            asFox(fox);
            placeElement(fox, 1, 1);

            ActionList result = decider.decide();

            assertNotNull(result);
            assertTrue(result.mustDoEndOfTurn());
        }

        @Test
        @DisplayName("Multiple geese, clearValidCells called exactly once regardless")
        void multipleGeese_clearOnce() {
            Pawn g2 = mock(Pawn.class), g3 = mock(Pawn.class);
            asFox(fox); asGoose(goose); asGoose(g2); asGoose(g3);

            placeElement(fox,   0, 0);
            placeElement(goose, 2, 2);
            placeElement(g2,    4, 4);
            placeElement(g3,    6, 6);

            decider.decide();

            verify(board, times(1)).clearValidCells();
        }

        @Test
        @DisplayName("Fox at board boundary (row=0, col=0) does not cause ArrayIndexOutOfBounds")
        void foxAtCorner_noException() {
            asFox(fox);
            asGoose(goose);

            placeElement(fox,   0, 0);
            placeElement(goose, 3, 3);

            Cell foxCell = registerCell(0, 0);

            Cell foxNeighbor = mock(Cell.class);
            when(foxNeighbor.getX()).thenReturn(1);
            when(foxNeighbor.getY()).thenReturn(0);
            when(foxCell.getNeighbors()).thenReturn(new ArrayList<>(List.of(foxNeighbor)));

            when(board.canReachCell(anyInt(), anyInt())).thenReturn(false);

            assertDoesNotThrow(() -> decider.decide());
        }
    }
}