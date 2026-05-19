package model;

import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

public class HoleStageFactory extends StageElementsFactory {

    private HoleStageModel stageModel;

    public HoleStageFactory(GameStageModel gameStageModel) {
        super(gameStageModel);
        this.stageModel = (HoleStageModel) gameStageModel;
    }

    @Override
    public void setup() {

        TextElement playerName = new TextElement(stageModel.getCurrentPlayerName(), stageModel);
        playerName.setLocation(0, 0);
        stageModel.setPlayerName(playerName);

        Board board = new Board(0, 0, stageModel);
        stageModel.setBoard(board);

        Pawn[] geese = new Pawn[13];
        int index = 0;

        for (int row = 4; row < 7; row++) {       // changer l'implementation, trop de break
            for (int col = 0; col < 7; col++) {
                if (index >= 13) break;
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