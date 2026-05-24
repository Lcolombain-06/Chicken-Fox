package model;

import boardifier.model.Model;
import boardifier.model.StageElementsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HoleStageModelTest {

    private HoleStageModel stageModel;
    private Board board;

    @BeforeEach
    void setUp() {
        stageModel = new HoleStageModel("test", null);
        board = new Board(0, 0, stageModel);
        stageModel.setBoard(board);
        stageModel.endInitialization();
    }

    // Test on the geese calculator

    @Test
    void initialGeeseCountIsThirteen() {
        assertEquals(13, stageModel.getGeeseToPlay(),
                "Initial goose count must be 13");
    }

    @Test
    void eatGeseDectementsCounter() {
        stageModel.eatGeese();
        assertEquals(12, stageModel.getGeeseToPlay(), "After one capture, goose count must be 12");
    }

    @Test
    void eatGeeseMultipleTimeDecrementCorrectly(){
        for (int i =0; i<5; i++){
            stageModel.eatGeese();
        }
        assertEquals(8, stageModel.getGeeseToPlay(), "After 5 captures, goose count must be 8");
    }

    @Test
    void eatAllGeeseReachesZero(){
        for (int i = 0; i<13; i++){
            stageModel.eatGeese();
        }
        assertEquals(0, stageModel.getGeeseToPlay(), "After 13 captures, goose count must be 0");
    }

    // Test on Fox coordinates

    @Test
    void initialFoxRowIsTwo(){
        assertEquals(2, stageModel.getFoxRow(), "Initial fox row must be 2");
    }

    @Test
    void initialFoxColIsThree() {
        assertEquals(3, stageModel.getFoxCol(), "Initial fox col must be 3");
    }

    @Test
    void setFoxCooUpdatesRow() {
        stageModel.setFoxCoo(4, 5);
        assertEquals(4, stageModel.getFoxRow(),
                "setFoxCoo must update the fox row");
    }

    @Test
    void setFoxCooUpdatesCol() {
        stageModel.setFoxCoo(4, 5);
        assertEquals(5, stageModel.getFoxCol(),
                "setFoxCoo must update the fox column");
    }

    @Test
    void setFoxCooToZeroZero() {
        stageModel.setFoxCoo(0, 0);
        assertEquals(0, stageModel.getFoxRow());
        assertEquals(0, stageModel.getFoxCol());
    }


    // Tests on FoxCapture

    @Test
    void initialFoxCapturedIsFalse() {
        assertFalse(stageModel.isFoxCaptured(), "foxCaptured must be false at the start");
    }

    @Test
    void setFoxCapturedToTrue() {
        stageModel.setFoxCaptured(true);
        assertTrue(stageModel.isFoxCaptured(), "setFoxCaptured(true) must set the flag to true");
    }

    @Test
    void setFoxCapturedToFalse() {
        stageModel.setFoxCaptured(true);
        stageModel.setFoxCaptured(false);
        assertFalse(stageModel.isFoxCaptured(), "setFoxCaptured(false) must reset the flag to false");
    }

    // Tests on setGeese and setFox

    @Test
    void setGeeseStoresArray() {
        Pawn[] geese = {
                new Pawn(Pawn.GOOSE, stageModel),
                new Pawn(Pawn.GOOSE, stageModel)
        };
        stageModel.setGeese(geese);
        assertNotNull(stageModel.getGeese(), "getGeese() must not return null after setGeese()");
        assertEquals(2, stageModel.getGeese().length);
    }

    @Test
    void setFoxStoresArray() {
        Pawn[] fox = { new Pawn(Pawn.FOX, stageModel) };
        stageModel.setFox(fox);
        assertNotNull(stageModel.getFox(), "getFox() must not return null after setFox()");
        assertEquals(1, stageModel.getFox().length);
    }

    @Test
    void setFoxArrayContainsFoxPawn() {
        Pawn fox = new Pawn(Pawn.FOX, stageModel);
        stageModel.setFox(new Pawn[]{fox});
        assertTrue(stageModel.getFox()[0].isFox(), "The stored pawn must be a fox");
    }

    // Test on board

    @Test
    void setBoardStoresBoard() {
        assertNotNull(stageModel.getBoard(), "getBoard() must not return null after setBoard()");
    }

    @Test
    void setBoardStoresCorrectInstance() {
        assertSame(board, stageModel.getBoard(), "getBoard() must return the exact same Board instance");
    }
}