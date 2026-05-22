package control;

import boardifier.control.ActionPlayer;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import model.Board;
import model.HoleStageModel;
import model.Pawn;

/**
 * ProfilingController — subclass of Controller for bot-vs-bot profiling.
 *
 * Differences from the parent:
 *   - overrides stageLoop() to check stopFlag before every turn (timeout support)
 *   - counts fox moves and geese moves in shared int[] arrays
 */
public class ProfilingController extends HoleController {

    private final int[]     foxMovesRef;
    private final int[]     geeseMovesRef;
    private final boolean[] stopFlag;

    public ProfilingController(Model model, View view,
                               int[]     foxMovesRef,
                               int[]     geeseMovesRef,
                               boolean[] stopFlag) {
        super(model, view);
        this.foxMovesRef   = foxMovesRef;
        this.geeseMovesRef = geeseMovesRef;
        this.stopFlag      = stopFlag;
    }

    @Override
    public void stageLoop() {
        HoleStageModel gameStage = (HoleStageModel) model.getGameStage();
        update();

        while (!model.isEndStage()) {

            // timeout guard
            if (stopFlag[0] || Thread.currentThread().isInterrupted()) {
                return; // Profiler will record DRAW_TIMEOUT
            }

            // win check
            int whoWon = partyWinned(gameStage.getFoxRow(), gameStage.getFoxCol());

            if (whoWon == 1) {
                model.setIdWinner(0);
                model.stopStage();
                break;
            } else if (whoWon == 2) {
                model.setIdWinner(1);
                model.stopStage();
                break;
            }

            // play one full turn (if multi-capture, it'll count as one turn)
            int playerBefore = model.getIdPlayer();
            profilingInnerLoop(gameStage);

            if (playerBefore == 0) foxMovesRef[0]++;
            else                   geeseMovesRef[0]++;

            endOfTurn();
        }

        endGame();
    }

    // replicates stageInnerLoop()
    private void profilingInnerLoop(HoleStageModel gameStage) {
        do {
            botPlayTurn();

            if (gameStage.isFoxCaptured()) {
                Board board = gameStage.getBoard();
                Pawn fox = (Pawn) board.getFirstElement(
                        gameStage.getFoxRow(), gameStage.getFoxCol());

                if (!board.foxCanCapture(fox, gameStage.getFoxRow(), gameStage.getFoxCol())) {
                    gameStage.setFoxCaptured(false);
                    break;
                }
            }
        } while (gameStage.isFoxCaptured());
    }

    private void botPlayTurn() {
        ActionList actions;
        if (model.getIdPlayer() == 0) {
            actions = new HoleDecider(model, this).decide();
        } else {
            actions = new GooseDecider(model, this).decide();
        }
        actions.setDoEndOfTurn(false);
        new ActionPlayer(model, this, actions).start();
        update();
    }
}
