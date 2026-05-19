package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import model.Pawn;

public class PawnLook extends ElementLook {

    public PawnLook(GameElement element) {
        super(element, 1, 1);
    }

    @Override
    protected void render() {
        setSize(1, 1); // ← add this
        Pawn p = (Pawn) element;

        if (p.isFox()) {
            shape[0][0] = "\u001B[31mF\u001B[0m"; // red F
        } else if (p.isGoose()) {
            shape[0][0] = "G";
        } else {
            shape[0][0] = " ";
        }
    }
}