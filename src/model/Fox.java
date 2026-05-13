package model;

import boardifier.model.GameStageModel;

public class Fox extends Pawn {

    public Fox(int number, GameStageModel gameStageModel) {
        // un renard est toujours PAWN_RED
        super(number, Pawn.PAWN_RED, gameStageModel);
    }
}