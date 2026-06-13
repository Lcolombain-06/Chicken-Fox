package view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import boardifier.view.TextLook;
import model.FGStageModel;
import model.Pawn;

public class FGStageView extends GameStageView {

    public FGStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
        width  = 900;
        height = 750;
    }

    @Override
    public void createLooks() {
        FGStageModel model = (FGStageModel) gameStageModel;

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

        // Invisible text look: only here so setText() on playerName
        // has an associated look and doesn't trigger an NPE.
        // The actual player turn display is handled by currentPlayerLabel in FGRootPane.
        if (model.getPlayerName() != null) {
            addLook(new TextLook(1, "0x00000000", model.getPlayerName()));
        }
    }
}