package model;

import model.Board;
import model.Cell;
import model.Pawn;
import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class BoardTest {

    private static class StubStageModel extends GameStageModel {
        public StubStageModel() {
            super("test", null);
        }

        @Override
        public StageElementsFactory getDefaultElementFactory() {
            return null;
        }
    }

    /**
     * Le plateau que l'on teste, recréé avant chaque test.
     */
    private Board board;

    /**
     * Méthode appelée avant chaque test.
     * Elle crée un plateau vide
     */
    @BeforeEach
    void setUp() {
        board = new Board(0, 0, new StubStageModel())
    }
}