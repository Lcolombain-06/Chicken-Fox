package control;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FoxDecider using Mockito only.
 * All dependencies are mocked to isolate decision logic.
 */
class FoxDeciderTest {

    @Mock
    Model model;
    @Mock
    Controller controller;
    @Mock
    HoleStageModel stage;
    @Mock
    Board board;
    @Mock
    Pawn fox;

    private FoxDecider decider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(model.getGameStage()).thenReturn(stage);
        when(stage.getBoard()).thenReturn(board);
        when(stage.getFox()).thenReturn(new Pawn[]{fox});

        decider = new FoxDecider(model, controller);
    }
}