package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Pawn;

public class PawnLook extends ElementLook {

    // Images normales
    private static final Image FOX_IMAGE   = new Image("resources/Fox.png");
    private static final Image GOOSE_IMAGE = new Image("resources/Geese.png");

    // Images quand sélectionné
    private static final Image FOX_SELECTED_IMAGE   = new Image("resources/FoxSelect.png");
    private static final Image GOOSE_SELECTED_IMAGE = new Image("resources/GeeseSelect.png");

    // Référence à l'ImageView pour pouvoir la modifier après render()
    private ImageView imageView;
    private boolean isFox;

    public PawnLook(GameElement element) {
        super(element);
    }

    @Override
    protected void render() {
        Pawn pawn = (Pawn) element;
        isFox = pawn.isFox();

        imageView = new ImageView(isFox ? FOX_IMAGE : GOOSE_IMAGE);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setX(-15.5);
        imageView.setY(-15.5);

        addNode(imageView);
    }

    @Override
    public void onSelectionChange() {
        if (imageView == null) return;

        if (element.isSelected()) {
            // Pion sélectionné --> image alternative
            imageView.setImage(isFox ? FOX_SELECTED_IMAGE : GOOSE_SELECTED_IMAGE);
        } else {
            // Pion désélectionné --> image normale
            imageView.setImage(isFox ? FOX_IMAGE : GOOSE_IMAGE);
        }
    }

    public void onChange() { }
}