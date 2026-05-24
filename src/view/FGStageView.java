package view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import boardifier.view.TextLook;
import model.FGStageModel;
import model.Pawn;

public class FGStageView extends GameStageView {

    public FGStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
    }

    @Override
    public void createLooks() {

        FGStageModel model = (FGStageModel) gameStageModel;

        addLook(new TextLook(model.getPlayerName()));



        addLook(new BoardLook(model.getBoard()));

        if (model.getGeese() != null) {
            for (Pawn p : model.getGeese()) {
                if (p != null) addLook(new PawnLook(p));
            }
        }

        if (model.getFox() != null) {
            for (Pawn p : model.getFox()) {
                if (p != null) addLook(new PawnLook(p));
            }
        }
    }
}