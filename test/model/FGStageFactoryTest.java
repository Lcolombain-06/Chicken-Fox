package model;

import boardifier.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FGStageFactoryTest {

    private FGStageModel stageModel;
    private FGStageFactory factory;
    private Model modelMock;

    @BeforeEach
    void setUp() {
        modelMock = mock(Model.class);
        when(modelMock.getCurrentPlayerName()).thenReturn("TestPlayer");
        stageModel = new FGStageModel("test", modelMock);

        factory = new FGStageFactory(stageModel);
        factory.setup();
        stageModel.endInitialization();
    }

    // -- Tests on the board

    @Test
    void boardIsCreated() {
        assertNotNull(stageModel.getBoard(), "Board must be created after setup()");
    }

    // -- Tests on the Fox

    @Test
    void foxArrayIsCreated() {
        assertNotNull(stageModel.getFox(), "Fox array must not be null after setup()");
    }

    @Test
    void foxArrayHasOneElement() {
        assertEquals(1, stageModel.getFox().length, "Fox array must contain one fox");
    }

    @Test
    void foxIsFoxType() {
        assertTrue(stageModel.getFox()[0].isFox(), "The fox pawn must be of FOX type");
    }

    @Test
    void foxIsPlacedAtRow2Col3() {
        Board board = stageModel.getBoard();
        boardifier.model.GameElement e = board.getFirstElement(2, 3);
        assertNotNull(e, "Fox must be placed at row 2 col 3");
        assertTrue(((Pawn) e).isFox(), "The element at (2,3) must be the fox");
    }

    // --- Tests on geese ---

    @Test
    void geeseArrayIsCreated() {
        assertNotNull(stageModel.getGeese(), "Geese array must not be null after setup()");
    }

    @Test
    void geeseArrayHasThirteenElements() {
        assertEquals(13, stageModel.getGeese().length, "Geese array must contain exactly 13 geese");
    }

    @Test
    void allGeeseAreGooseType() {
        for (Pawn p : stageModel.getGeese()) {
            assertNotNull(p, "No goose in the array must be null");
            assertTrue(p.isGoose(), "Every pawn in geese array must be goose type");
        }
    }

    @Test
    void geeseArePlacedInBottomRows() {
        Board board = stageModel.getBoard();
        int gooseCount = 0;
        for (int row = 4; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boardifier.model.GameElement e = board.getFirstElement(row, col);
                if (e != null && ((Pawn) e).isGoose()) gooseCount++;
            }
        }
        assertEquals(13, gooseCount, "13 geese must be placed in rows 4, 5 and 6");
    }

    @Test
    void noGeeseInTopRows() {
        Board board = stageModel.getBoard();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 7; col++) {
                boardifier.model.GameElement e = board.getFirstElement(row, col);
                if (e != null) {
                    assertFalse(((Pawn) e).isGoose(), "No goose must be placed above row 4");
                }
            }
        }
    }

    @Test
    void geeseOnlyOnAccessibleCells() {
        Board board = stageModel.getBoard();
        for (int row = 4; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boardifier.model.GameElement e = board.getFirstElement(row, col);
                if (e != null && ((Pawn) e).isGoose()) {
                    assertTrue(board.getCell(col, row).isAccessible(), "Geese must only be on accessible cells");
                }
            }
        }
    }

    @Test
    void geeseCounterStartsAtThirteen() {
        assertEquals(13, stageModel.getGeeseToPlay(), "Geese counter must start at 13");
    }
}