package view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import boardifier.view.TextLook;
import model.HoleStageModel;
import model.Pawn;

public class HoleStageView extends GameStageView {

    public HoleStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
    }

    @Override
    public void createLooks() {

        HoleStageModel model = (HoleStageModel) gameStageModel;

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