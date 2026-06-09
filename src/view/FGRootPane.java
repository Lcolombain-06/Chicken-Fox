package view;

import boardifier.view.RootPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * FGRootPane — personnalise le fond de la fenêtre.
 *
 * Selon le tutoriel, on hérite de RootPane et on surcharge
 * createDefaultGroup() pour définir l'arrière-plan.
 * L'image du plateau est gérée par BoardLook, donc ici
 * on met juste un fond noir simple.
 */
public class FGRootPane extends RootPane {

    @Override
    public void createDefaultGroup() {
        // Fond noir derrière le plateau
        Rectangle background = new Rectangle(700, 700, Color.BLACK);
        group.getChildren().clear();
        group.getChildren().add(background);
    }
}
