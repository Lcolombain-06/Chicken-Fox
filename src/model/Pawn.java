package model;

import boardifier.model.GameElement;
import boardifier.model.GameStageModel;

public class Pawn extends GameElement {

    public static final int GOOSE = 0;
    public static final int FOX = 1;

    private int type;

    public Pawn(int type, GameStageModel gameStageModel) {
        super(gameStageModel, 1); // Set 0 for the "basic" type in Element type
        this.type = type;
    }

    public boolean isFox() {
        return type == FOX;
    }

    public boolean isGoose() {
        return type == GOOSE;
    }

    public int getType() {
        return type;
    }
}