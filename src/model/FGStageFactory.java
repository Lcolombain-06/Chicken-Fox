package model;

import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * Factory class responsible for creating and placing game elements on the board.
 * <p>
 * This class sets up the initial layout of the game: it initializes the current player's
 * name display, creates the grid board, sets up the 13 geese at their starting spots,
 * and places the fox at its initial position.
 * </p>
 */
public class FGStageFactory extends StageElementsFactory {

    private FGStageModel stageModel;

    public FGStageFactory(GameStageModel gameStageModel) {
        super(gameStageModel);
        this.stageModel = (FGStageModel) gameStageModel;
    }

    /**
     * Sets up and positions all initial components inside the game area.
     * <p>
     * Initializes the player name text label, creates the grid board, spawns the geese
     * in the bottom rows of the cross shape, and spawns the fox at coordinates [2,3].
     * </p>
     */
    @Override
    public void setup() {

        TextElement playerName = new TextElement(stageModel.getCurrentPlayerName(), stageModel);
        playerName.setLocation(0, 0);
        stageModel.setPlayerName(playerName);

        Board board = new Board(0, 0, stageModel);
        stageModel.setBoard(board);

        Pawn[] geese = new Pawn[stageModel.getGeeseToPlay()];
        int index = 0;

        for (int row = 4; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (index >= stageModel.getGeeseToPlay()) break;
                if (!board.getCell(col, row).isAccessible()) continue;

                Pawn p = new Pawn(Pawn.GOOSE, stageModel);
                board.addElement(p, row, col);
                geese[index++] = p;
            }
        }
        stageModel.setGeese(geese);

        Pawn fox = new Pawn(Pawn.FOX, stageModel);
        board.addElement(fox, 2, 3);
        stageModel.setFox(new Pawn[]{fox});
    }
}