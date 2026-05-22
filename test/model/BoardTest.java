package model;

public class BoardTest {

    public class BoardTest {

        private static class StubStageModel extends GameStageModel {
            public StubStageModel() { super("test", null); }

            @Override
            public StageElementsFactory getDefaultElementFactory() { return null; }
        }
        /** Le plateau que l'on teste, recréé avant chaque test. */
        private Board board;
        /**
         * Méthode appelée avant chaque test.
         * Elle crée un plateau vide
         */
        @BeforeEach
        void setUp() {
            board = new Board(0, 0, new StubStageModel());
        }
}
