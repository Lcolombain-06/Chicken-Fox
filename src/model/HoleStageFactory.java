package model;

import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * HoleStageFactory must create the game elements that are defined in HoleStageModel
 * WARNING: it just creates the game element and NOT their look, which is done in HoleStageView.
 *
 * If there must be a precise position in the display for the look of a game element, then this element must be created
 * with that position in the virtual space and MUST NOT be placed in a container element. Indeed, for such
 * elements, the position in their virtual space will match the position on the display. For example, in the following,
 * the black pot is placed in 18,0. When displayed on screen, the top-left character of the black pot will be effectively
 * placed at column 18 and row 0.
 *
 * Otherwise, game elements must be put in a container and it will be the look of the container that will manage
 * the position of element looks on the display. For example, pawns are put in a ContainerElement. Thus, their virtual space is
 * in fact the virtual space of the container and their location in that space in managed by boardifier, depending of the
 * look of the container.
 *
 */
public class HoleStageFactory extends StageElementsFactory {
    private HoleStageModel stageModel;

    public HoleStageFactory(GameStageModel gameStageModel) {
        super(gameStageModel);
        stageModel = (HoleStageModel) gameStageModel;
    }

    @Override
    public void setup() {

        Board board = new Board (0,0, stageModel);
        stageModel.setBoard(board);

        Pawn[] chikens = new Pawn[13];

        for (int i = 0; i < 13; i++) {
            chikens[i] = new Pawn (i, Pawn.PAWN_BLACK, stageModel);
        }
        stageModel.setChickens(chikens);

        Pawn[] fox = new Pawn[1];
        fox[0] = new Pawn (0, Pawn.PAWN_RED, stageModel);
        stageModel.setFox(fox);

        int i = 0;

        for (int col = 2; col <= 4; col++) {
            stageModel.putInContainer(chikens[i], board, 0, col);
            i++;
        }

        for (int col = 2; col <= 4; col++) {
            stageModel.putInContainer(chikens[i], board, 1, col);
            i++;
        }

        for (int col = 0; col <= 6; col++) {
            stageModel.putInContainer (chikens[i], board, 2, col);
            i++;
        }

        stageModel.putInContainer(fox[0], board, 6, 3);

        /*
        TO FULFILL:
            - create the board, pots, pawns and set them in the stage model
            - assign the pawns to their cells in the pots
         */
    }
}
