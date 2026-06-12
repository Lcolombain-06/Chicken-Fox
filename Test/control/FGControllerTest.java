package control;

import boardifier.view.View;
import model.Board;
import model.FGStageModel;
import model.Pawn;
import boardifier.model.Player;
import boardifier.model.TextElement;
import boardifier.model.GameElement;
import boardifier.control.ActionPlayer;
import boardifier.model.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FGControllerTest {

    @Mock private Model model;
    @Mock private View view;
    @Mock private FGStageModel stage;
    @Mock private Board board;
    @Mock private Pawn fox;
    @Mock private Player player;
    @Mock private TextElement playerNameLabel;
    @Mock private GameEndListener gameEndListener;

    private FGController controller;

    @BeforeEach
    void setUp() {
        // Intercept the creation of FGControllerMouse inside the constructor
        try (MockedConstruction<FGControllerMouse> ignored =
                     mockConstruction(FGControllerMouse.class)) {
            controller = new FGController(model, view);
        }
        controller.setGameEndListener(gameEndListener);

        // This stub is required for every test because it is called first
        when(model.getGameStage()).thenReturn(stage);
    }

    // Helper method to set up a regular turn without a quick victory
    private void setupStandardTurnContext() {
        when(stage.getElements()).thenReturn(List.of());
        when(stage.getBoard()).thenReturn(board);
        when(stage.getFox()).thenReturn(new Pawn[]{fox});
        when(stage.getFoxRow()).thenReturn(3);
        when(stage.getFoxCol()).thenReturn(2);
        when(stage.getPlayerName()).thenReturn(playerNameLabel);
    }

    // Test that the fox wins when there are fewer than 4 geese left
    @Test
    void endOfTurn_foxWins_whenGeeseCountBelowFour() {
        when(stage.getElements()).thenReturn(List.of());
        when(stage.getGeeseToPlay()).thenReturn(3);
        when(model.getPlayers()).thenReturn(List.of(player, mock(Player.class)));
        when(player.getName()).thenReturn("Alice");

        controller.endOfTurn();

        verify(model).setIdWinner(0);
        verify(model).stopStage();
        verify(gameEndListener).showEndGame("Alice");
        verify(model, never()).setNextPlayer();
    }

    // Boundary test: check that the fox wins when exactly 3 geese remain
    @Test
    void endOfTurn_foxWins_exactlyThreeGeese() {
        when(stage.getElements()).thenReturn(List.of());
        when(stage.getGeeseToPlay()).thenReturn(3);
        when(model.getPlayers()).thenReturn(List.of(player, mock(Player.class)));
        when(player.getName()).thenReturn("P1");

        controller.endOfTurn();

        verify(model).setIdWinner(0);
    }

    // Test that the game continues when there are exactly 4 geese left
    @Test
    void endOfTurn_noFoxWin_whenGeeseCountEqualsFour() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(4);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2); // The fox can still move
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("P1");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        controller.endOfTurn();

        verify(model, never()).stopStage();
    }

    // Test that the geese win when the fox has no valid moves left
    @Test
    void endOfTurn_geeseWin_whenFoxHasNoValidMoves() {
        when(stage.getElements()).thenReturn(List.of());
        when(stage.getBoard()).thenReturn(board);
        when(stage.getFox()).thenReturn(new Pawn[]{fox});
        when(stage.getFoxRow()).thenReturn(3);
        when(stage.getFoxCol()).thenReturn(2);

        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(0); // 0 moves means the fox is trapped
        when(model.getPlayers()).thenReturn(List.of(mock(Player.class), player));
        when(player.getName()).thenReturn("Bob");

        controller.endOfTurn();

        verify(model).setIdWinner(1);
        verify(model).stopStage();
        verify(gameEndListener).showEndGame("Bob");
        verify(model, never()).setNextPlayer();
    }

    // Test that geese do not win if the fox has at least one valid move
    @Test
    void endOfTurn_noGeeseWin_whenFoxHasAtLeastOneMove() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(1);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("P1");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        controller.endOfTurn();

        verify(model, never()).stopStage();
    }

    // Test that the system correctly changes the player when nobody wins
    @Test
    void endOfTurn_switchesPlayer_whenNoVictory() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Claire");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        controller.endOfTurn();

        verify(model).setNextPlayer();
        verify(playerNameLabel).setText("Claire");
    }

    // Test that the listener updates correctly with the current player information
    @Test
    void endOfTurn_updatesListenerWithCurrentPlayer() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Claire");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        controller.endOfTurn();

        verify(gameEndListener).updateCurrentPlayer("Claire", false);
    }

    // Test that the listener sets isFox to true when the player ID is 0
    @Test
    void endOfTurn_updatesListener_isFoxTrue_whenIdPlayerIsZero() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Dave");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(0);

        controller.endOfTurn();

        verify(gameEndListener).updateCurrentPlayer("Dave", true);
    }

    // Test that the fox pawn is automatically selected during the fox's turn
    @Test
    void endOfTurn_selectsFox_whenFoxTurn() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Dave");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(0);

        controller.endOfTurn();

        verify(fox).select();
    }

    // Test that the fox pawn is not selected during the geese's turn
    @Test
    void endOfTurn_doesNotSelectFox_whenGeeseTurn() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Eve");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        controller.endOfTurn();

        verify(fox, never()).select();
    }

    // Test that the system starts FoxDecider when the computer plays as the fox
    @Test
    void endOfTurn_launchesFoxDecider_whenComputerIsIdPlayerZero() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("CPU");
        when(player.getType()).thenReturn(Player.COMPUTER);
        when(model.getIdPlayer()).thenReturn(0);

        try (MockedConstruction<FoxDecider> foxDecider = mockConstruction(FoxDecider.class);
             MockedConstruction<ActionPlayer> actionPlayer = mockConstruction(ActionPlayer.class)) {

            controller.endOfTurn();

            assert foxDecider.constructed().size() == 1;
            assert actionPlayer.constructed().size() == 1;
            verify(actionPlayer.constructed().get(0)).start();
        }
    }

    // Test that the system starts GooseDecider when the computer plays as the geese
    @Test
    void endOfTurn_launchesGooseDecider_whenComputerIsIdPlayerOne() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("CPU");
        when(player.getType()).thenReturn(Player.COMPUTER);
        when(model.getIdPlayer()).thenReturn(1);

        try (MockedConstruction<GooseDecider> gooseDecider = mockConstruction(GooseDecider.class);
             MockedConstruction<ActionPlayer> actionPlayer = mockConstruction(ActionPlayer.class)) {

            controller.endOfTurn();

            assert gooseDecider.constructed().size() == 1;
            assert actionPlayer.constructed().size() == 1;
            verify(actionPlayer.constructed().get(0)).start();
        }
    }

    // Test that no computer AI is started if the current player is a human
    @Test
    void endOfTurn_doesNotLaunchAI_whenPlayerIsHuman() {
        setupStandardTurnContext();
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Human");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        try (MockedConstruction<ActionPlayer> actionPlayer = mockConstruction(ActionPlayer.class)) {
            controller.endOfTurn();
            assert actionPlayer.constructed().isEmpty();
        }
    }

    // Test that the system only unselects elements that are currently selected
    @Test
    void endOfTurn_unselects_onlySelectedElements() {
        GameElement selected    = mock(GameElement.class);
        GameElement notSelected = mock(GameElement.class);
        when(selected.isSelected()).thenReturn(true);
        when(notSelected.isSelected()).thenReturn(false);
        when(stage.getElements()).thenReturn(List.of(selected, notSelected));

        // Trigger a quick fox victory to stop execution early
        when(stage.getGeeseToPlay()).thenReturn(2);
        when(model.getPlayers()).thenReturn(List.of(player, mock(Player.class)));
        when(player.getName()).thenReturn("Alice");

        controller.endOfTurn();

        verify(selected).unselect();
        verify(notSelected, never()).unselect();
    }

    // Test that the application does not crash with a NullPointerException if the listener is null during a win
    @Test
    void endOfTurn_noNullPointer_whenListenerIsNull_andFoxWins() {
        controller.setGameEndListener(null);
        when(stage.getElements()).thenReturn(List.of());
        when(stage.getGeeseToPlay()).thenReturn(2);
        when(model.getPlayers()).thenReturn(List.of(player, mock(Player.class)));
        when(player.getName()).thenReturn("Alice");

        controller.endOfTurn();

        verify(model).stopStage();
    }

    // Test that the application does not crash with a NullPointerException if the listener is null during a normal turn
    @Test
    void endOfTurn_noNullPointer_whenListenerIsNull_andGameContinues() {
        setupStandardTurnContext();
        controller.setGameEndListener(null);
        when(stage.getGeeseToPlay()).thenReturn(5);
        when(board.setValidCells(fox, 3, 2)).thenReturn(2);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("P1");
        when(player.getType()).thenReturn(Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(1);

        controller.endOfTurn();

        verify(model).setNextPlayer();
    }
}