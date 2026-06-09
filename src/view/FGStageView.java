package view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import boardifier.view.TextLook;
import model.FGStageModel;
import model.Pawn;

public class FGStageView extends GameStageView {

    public FGStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
        width = 650;
        height = 450;
    }

    @Override
    public void createLooks() {
        FGStageModel model = (FGStageModel)gameStageModel;

        //addLook(new BoardLook(320, model.getBoard()));
    }
}