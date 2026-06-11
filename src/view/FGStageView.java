package view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import model.FGStageModel;
import model.Pawn;

/**
 * FGStageView — crée tous les looks du stage.
 *
 * Selon le tutoriel, createLooks() instancie un look pour chaque
 * élément du modèle. Boardifier se charge ensuite de les afficher
 * et de les mettre à jour automatiquement.
 *
 * Les dimensions sont maintenant en pixels (mode graphique).
 */
public class FGStageView extends GameStageView {

    public FGStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
        // Dimensions de la zone d'affichage du stage en pixels
        width = 600;
        height = 600;
    }

    @Override
    public void createLooks() {
        FGStageModel model = (FGStageModel) gameStageModel;

        // Look du plateau — BoardLook gère l'image PNG + le positionnement des pions
        addLook(new BoardLook(model.getBoard()));

        // Look de chaque oie
        if (model.getGeese() != null) {
            for (Pawn p : model.getGeese()) {
                if (p != null) addLook(new PawnLook(p));
            }
        }

        // Look du renard
        if (model.getFox() != null) {
            for (Pawn p : model.getFox()) {
                if (p != null) addLook(new PawnLook(p));
            }
        }
    }
}