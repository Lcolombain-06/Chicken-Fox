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

    // CORRECTION : taille du pion agrandie pour suivre l'agrandissement
    // du plateau (CELL_SIZE 68 -> 90 dans BoardLook). L'offset est recalculé
    // pour rester centré dans la case : -(taille/2 + 0.5) comme avant
    // (50 -> -15.5 correspondait à un centrage avec une marge de 9.5px
    // de chaque côté pour une case de 68px, soit ~14% de la case).
    private static final double PAWN_SIZE = 66; // ~14% de marge pour une case de 90px
    private static final double OFFSET    = -(PAWN_SIZE / 2.0) + 0.5; // -32.5

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
        imageView.setFitWidth(PAWN_SIZE);
        imageView.setFitHeight(PAWN_SIZE);
        imageView.setX(OFFSET);
        imageView.setY(OFFSET);

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