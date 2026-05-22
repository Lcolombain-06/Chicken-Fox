package test;

import boardifier.model.Model;
import control.HoleController;
import model.Board;
import model.HoleStageModel;
import model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;


public class HoleControllerTest {

    private Model model;
    private HoleStageModel gameStage;
    private Board board;
    private HoleController controller;

    @BeforeEach
    void setUp() throws Exception {
        model = new Model();
        gameStage = new HoleStageModel("test", model);
        board = new Board(0, 0, gameStage);

        gameStage.setBoard(board);
        model.setGameStage(gameStage);
        gameStage.endInitialization();

        controller = new HoleController(model, null);


        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        java.io.BufferedReader fakeReader =
                new java.io.BufferedReader(new java.io.InputStreamReader(emptyStream));

        Field consoleInField = HoleController.class.getDeclaredField("consoleIn");
        consoleInField.setAccessible(true);
        consoleInField.set(controller, fakeReader);
    }


}
