package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Pawn;

public class PawnLook extends ElementLook {

    private static final Image FOX_IMAGE   = new Image("resources/Fox.png");
    private static final Image GOOSE_IMAGE = new Image("resources/Geese.png");

    private static final Image FOX_SELECTED_IMAGE   = new Image("resources/FoxSelect.png");
    private static final Image GOOSE_SELECTED_IMAGE = new Image("resources/GeeseSelect.png");

    private static final double PAWN_SIZE = 70;
    private static final double OFFSET    = -(PAWN_SIZE / 2.0) + 5.5 ;

    private ImageView imageView;
    private boolean isFox;

    public PawnLook(GameElement element) {
        super(element);
    }

    // Create and position the pawn image, centered in its cell.
    @Override
    protected void render() {
        Pawn pawn = (Pawn) element;
        isFox = pawn.isFox();

        imageView = new ImageView(isFox ? FOX_IMAGE : GOOSE_IMAGE);
        imageView.setFitWidth(PAWN_SIZE);
        imageView.setFitHeight(PAWN_SIZE);
        imageView.setX(OFFSET);
        imageView.setY(OFFSET);

        addNode(imageView);
    }

    // Switch the pawn image when it gets selected or deselected.
    @Override
    public void onSelectionChange() {
        if (imageView == null) return;

        if (element.isSelected()) {
            imageView.setImage(isFox ? FOX_SELECTED_IMAGE : GOOSE_SELECTED_IMAGE);
        } else {
            imageView.setImage(isFox ? FOX_IMAGE : GOOSE_IMAGE);
        }
    }

    @Override
    public void onFaceChange() {
        // Pawns don't have a "face" (no flip side), nothing to update.
    }
}