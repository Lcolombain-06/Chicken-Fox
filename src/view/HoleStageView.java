package view;

import boardifier.model.ContainerElement;
import boardifier.model.GameElement;
import boardifier.model.GameStageModel;
import boardifier.view.ContainerLook;
import boardifier.view.ElementLook;
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
        /*
        // DEBUG
        ContainerLook boardLook = (ContainerLook) getElementLook(model.getBoard());
        ContainerElement board = model.getBoard();

        //System.out.println("Board size: " + board.getNbRows() + "x" + board.getNbCols());

        for (int row = 0; row < board.getNbRows(); row++) {
            for (int col = 0; col < board.getNbCols(); col++) {
                GameElement el = board.getElement(row, col);
                if (el != null) {
                    //System.out.println("Found element at [" + row + "][" + col + "]: " + el);
                    ElementLook elLook = getElementLook(el);
                    //System.out.println("  → look: " + elLook);
                    if (elLook != null) {
                        boardLook.addInnerLook(elLook, row, col);
                    }
                }
            }
        }*/
    }
}