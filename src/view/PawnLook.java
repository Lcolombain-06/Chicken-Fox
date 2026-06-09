package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Pawn;

public class PawnLook extends ElementLook {

    private static final int SIZE = 50;

    public PawnLook(GameElement element) {
        super(element); // constructeur correct : juste l'element
    }

    @Override
    protected void render() {
        Pawn pawn = (Pawn) element;

        String imagePath = pawn.isFox() ? "resources/Fox.png" : "resources/Geese.png";
        Image image = new Image(imagePath);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(SIZE);
        imageView.setFitHeight(SIZE);
        imageView.setX(0);
        imageView.setY(0);

        addNode(imageView); // obligatoire sinon invisible
    }

    @Override
    public void onSelectionChange() {
        // futur : ajouter un contour quand le pion est sélectionné
    }
}