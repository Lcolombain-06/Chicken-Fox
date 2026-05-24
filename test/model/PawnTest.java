package model;

import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PawnTest {

    private static class StubStageModel extends GameStageModel {
        public StubStageModel() { super("test", null); }
        @Override
        public StageElementsFactory getDefaultElementFactory() { return null; }
    }

    private StubStageModel stageModel;

    @BeforeEach
    void setUp() {
        stageModel = new StubStageModel();
    }

    // -- Test on the Fox Type

    @Test
    void foxPawnIsFox(){
        Pawn p = new Pawn(Pawn.FOX, stageModel);
        assertTrue(p.isFox(), "A Fox must return true for isFox()");
    }

    @Test
    void foxPawnIsNotGoose(){
        Pawn p = new Pawn(Pawn.FOX, stageModel);
        assertFalse(p.isGoose(), "A Fox must return false for isGoose()");
    }

    @Test
    void foxPawnGetTypeReturnFox(){
        Pawn p = new Pawn(Pawn.FOX, stageModel);
        assertEquals(Pawn.FOX, p.getType(), "getType() must return the Fox constant");
    }

    // -- Test on the Goose Type

    @Test
    void goosePawnIsGoose(){
        Pawn g = new Pawn(Pawn.GOOSE, stageModel);
        assertTrue(g.isGoose(), "A Goose must return true for isGoose()");
    }

    @Test
    void goosePawnIsNotFox(){
        Pawn g = new Pawn(Pawn.GOOSE, stageModel);
        assertFalse(g.isFox(), "A Goose must return false for isFox()");
    }

    @Test
    void goosePawnGetTypeReturnGoose(){
        Pawn g = new Pawn(Pawn.GOOSE, stageModel);
        assertEquals(Pawn.GOOSE, g.getType(), "getType() must return the GOOSE constant");
    }

    @Test
    void twoPawnsAreIndependent(){
        Pawn fox = new Pawn(Pawn.FOX, stageModel);
        Pawn goose = new Pawn(Pawn.GOOSE, stageModel);
        assertNotEquals(fox.getType(), goose.getType(), "A fox and a goose must have different types");
    }
}
