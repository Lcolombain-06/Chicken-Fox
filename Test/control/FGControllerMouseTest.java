package control;

import boardifier.model.Model;
import boardifier.view.View;
import boardifier.control.Controller;
import boardifier.view.ElementLook;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.Board;
import model.FGStageModel;
import model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FGControllerMouseTest {

    @Mock private Model model;
    @Mock private View view;
    @Mock private Controller control;
    @Mock private FGStageModel stage;
    @Mock private Board board;
    @Mock private Pawn fox;
    @Mock private Pawn goose;
    @Mock private ElementLook boardLook;
    @Mock private Group group;
    @Mock private MouseEvent event;
    @Mock private Pane mockPane;

    private FGControllerMouse mouseController;

    @BeforeEach
    void setUp() {
        // Prevent NullPointerException when the constructor registers listeners on the pane
        when(view.getRootPane()).thenReturn(mockPane);

        mouseController = new FGControllerMouse(model, view, control);

        // General configuration for the game stage and board elements
        when(model.getGameStage()).thenReturn(stage);
        when(stage.getBoard()).thenReturn(board);
        when(stage.getFox()).thenReturn(new Pawn[]{fox});
        when(stage.getFoxRow()).thenReturn(3);
        when(stage.getFoxCol()).thenReturn(3);

        // Define a real 700x700 rectangle area to simulate the visual board size
        Bounds realBounds = new BoundingBox(0.0, 0.0, 700.0, 700.0);

        // Mock visual components to safely return our custom board dimensions
        when(control.getElementLook(board)).thenReturn(boardLook);
        when(boardLook.getGroup()).thenReturn(group);

        // Provide bounds to ensure coordinate calculations do not fail
        when(group.getBoundsInLocal()).thenReturn(realBounds);
        when(group.localToScene(any(Bounds.class))).thenReturn(realBounds);

        // Set up a standard click position in the middle of cell (1,1)
        when(event.getX()).thenReturn(150.0);
        when(event.getY()).thenReturn(150.0);
    }

    // Check that the method stops immediately if the board UI component is missing
    @Test
    void handle_doesNothing_whenBoardLookIsNull() {
        when(control.getElementLook(board)).thenReturn(null);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        when(model.getIdPlayer()).thenReturn(0);

        mouseController.handle(event);

        verify(board, never()).setValidCells(any(), anyInt(), anyInt());
    }

    // Check that mouse clicks outside the 700x700 board area are ignored
    @Test
    void handle_doesNothing_whenClickOutsideBoard() {
        when(event.getX()).thenReturn(800.0);
        when(event.getY()).thenReturn(800.0);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        when(model.getIdPlayer()).thenReturn(1);

        mouseController.handle(event);

        verify(board, never()).setValidCells(any(), anyInt(), anyInt());
    }

    // Check that a right-click during the fox turn only prepares a move without playing it
    @Test
    void handle_foxRightClick_addsToPlanQueue() {
        when(model.getIdPlayer()).thenReturn(0);
        when(event.getButton()).thenReturn(MouseButton.SECONDARY);

        mouseController.handle(event);

        verify(board, never()).setValidCells(any(), anyInt(), anyInt());
    }

    // Check that a direct left-click without planning anything beforehand does nothing
    @Test
    void handle_foxLeftClick_withEmptyQueue_doesNothing() {
        when(model.getIdPlayer()).thenReturn(0);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);

        mouseController.handle(event);

        verify(board, never()).setValidCells(any(), anyInt(), anyInt());
    }

    // Check that the fox successfully moves when a right-click is followed by a left-click on a valid cell
    @Test
    void handle_foxConfirm_executesMove_whenCellIsReachable() {
        when(model.getIdPlayer()).thenReturn(0);

        when(event.getButton()).thenReturn(MouseButton.SECONDARY);
        mouseController.handle(event);

        boolean[][] reachable = new boolean[7][7];
        reachable[1][1] = true;
        when(board.getReachableCells()).thenReturn(reachable);

        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        mouseController.handle(event);

        verify(board, atLeastOnce()).setValidCells(fox, 3, 3);
    }

    // Check that the move sequence is reset if the fox confirms an impossible destination
    @Test
    void handle_foxConfirm_cancelsSequence_whenMoveIsInvalid() {
        when(model.getIdPlayer()).thenReturn(0);

        when(event.getButton()).thenReturn(MouseButton.SECONDARY);
        mouseController.handle(event);

        boolean[][] reachable = new boolean[7][7];
        when(board.getReachableCells()).thenReturn(reachable);

        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        mouseController.handle(event);

        verify(board).clearValidCells();
    }

    // Check that clicking an empty cell during the geese turn changes nothing
    @Test
    void handle_gooseTurn_clickOnEmptyCell_doesNothing() {
        when(model.getIdPlayer()).thenReturn(1);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        when(board.getElement(1, 1)).thenReturn(null);

        mouseController.handle(event);

        verify(board, never()).setValidCells(any(), anyInt(), anyInt());
    }

    // Check that a goose piece becomes highlighted when the player clicks on it
    @Test
    void handle_gooseTurn_clickOnGoose_selectsIt() {
        when(model.getIdPlayer()).thenReturn(1);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        when(board.getElement(1, 1)).thenReturn(goose);
        when(goose.isGoose()).thenReturn(true);
        when(board.getElementCell(goose)).thenReturn(new int[]{1, 1});

        mouseController.handle(event);

        verify(goose).select();
        verify(board).setValidCells(goose, 1, 1);
    }

    // Check that a geese player cannot click or interact with the fox piece
    @Test
    void handle_gooseTurn_clickOnFox_doesNothing() {
        when(model.getIdPlayer()).thenReturn(1);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        when(board.getElement(1, 1)).thenReturn(fox);
        when(fox.isGoose()).thenReturn(false);

        mouseController.handle(event);

        verify(board, never()).setValidCells(any(), anyInt(), anyInt());
    }

    // Check that clicking a selected goose a second time safely deselects it
    @Test
    void handle_gooseTurn_secondClickOnSameCell_deselectsGoose() {
        when(model.getIdPlayer()).thenReturn(1);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);
        when(board.getElement(1, 1)).thenReturn(goose);
        when(goose.isGoose()).thenReturn(true);
        when(board.getElementCell(goose)).thenReturn(new int[]{1, 1});

        mouseController.handle(event);
        mouseController.handle(event);

        verify(goose).unselect();
        verify(board).clearValidCells();
    }

    // Check that clicking an unreachable destination cancels the selection of the current goose
    @Test
    void handle_gooseTurn_clickOnUnreachableCell_deselectsGoose() {
        when(model.getIdPlayer()).thenReturn(1);
        when(event.getButton()).thenReturn(MouseButton.PRIMARY);

        when(board.getElement(1, 1)).thenReturn(goose);
        when(goose.isGoose()).thenReturn(true);
        when(board.getElementCell(goose)).thenReturn(new int[]{1, 1});
        mouseController.handle(event);

        // Change mouse coordinates to simulate a click on another distant cell
        when(event.getX()).thenReturn(450.0);
        when(event.getY()).thenReturn(450.0);
        boolean[][] reachable = new boolean[7][7];
        when(board.getReachableCells()).thenReturn(reachable);

        mouseController.handle(event);

        verify(goose).unselect();
        verify(board).clearValidCells();
    }
}