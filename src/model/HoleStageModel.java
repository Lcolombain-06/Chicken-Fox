package model;

import boardifier.model.*;

public class HoleStageModel extends GameStageModel {

    private int geeseToPlay;
    private int foxToPlay;
    private Pawn selectedPawn;

    private Board board;

    private Pawn[] geese;
    private Pawn[] fox;

    private int foxRow;
    private int foxCol;
    private boolean foxCaptured; //true when the fox did a capture

    private TextElement playerName;

    private boolean initializing = true;

    public HoleStageModel(String name, Model model) {
        super(name, model);

        geeseToPlay = 13;
        foxToPlay = 1;

        foxRow = 2;
        foxCol = 3;
        foxCaptured = false;

        setupCallbacks();
    }

    public void endInitialization() {
        initializing = false;
    }

    public Board getBoard() {
        return board;
    }

    public Pawn[] getGeese() {
        return geese;
    }

    public Pawn[] getFox() {
        return fox;
    }

    public Pawn getSelectedPawn() {
        return selectedPawn;
    }

    public TextElement getPlayerName() {
        return playerName;
    }

    public void setBoard(Board board) {
        this.board = board;
        addContainer(board);
    }

    public void setGeese(Pawn[] geese) {
        this.geese = geese;
        for (Pawn p : geese) {
            if (p != null) addElement(p);
        }
    }

    public void setFox(Pawn[] fox) {
        this.fox = fox;
        for (Pawn p : fox) {
            if (p != null) addElement(p);
        }
    }

    public void setSelectedPawn(Pawn pawn) {
        this.selectedPawn = pawn;
    }

    public void setPlayerName(TextElement t) {
        this.playerName = t;
        addElement(t);
    }

    private void setupCallbacks() {

        onPutInContainer((element, container, row, col) -> {

            if (initializing) return;

            if (!(element instanceof Pawn)) return;

            Pawn p = (Pawn) element;

            if (container == board) {

                if (p.isGoose()) geeseToPlay--;
                else if (p.isFox()) foxToPlay--;

                if (geeseToPlay == 0 && foxToPlay == 0) {
                    System.out.println("Game finished");
                    model.stopStage();
                }
            }
        });
    }

    public int getGeeseToPlay () {
        return this.geeseToPlay;
    }

    public void eatGeese() {
        this.geeseToPlay = geeseToPlay - 1;
    }

    //coordinates of the fox
    public int getFoxRow() {
        return this.foxRow;
    }

    public int getFoxCol() {
        return this.foxCol;
    }

    // gestion of multi-capturing
    public boolean isFoxCaptured() {
        return foxCaptured;
    }
    public void setFoxCaptured(boolean foxCaptured) {
        this.foxCaptured = foxCaptured;
    }

    public void setFoxCoo (int row, int col) {
        this.foxRow = row;
        this.foxCol = col;
    }

    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new HoleStageFactory(this);
    }
}