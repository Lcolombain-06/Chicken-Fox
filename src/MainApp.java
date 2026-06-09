import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.view.RootPane;
import boardifier.view.View;
import control.FGController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.FGRootPane;

/**
 * MainApp — point d'entrée de la version graphique.
 *
 * Beaucoup plus simple maintenant : Boardifier gère tout l'affichage
 * via les looks. Plus besoin de BoardRenderer ni d'ImageView manuel.
 */
public class MainApp extends Application {

    public static final int WIDTH  = 700;
    public static final int HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        try {
            Model model = new Model();
            model.addHumanPlayer("Player1");
            model.addComputerPlayer("Player2");

            StageFactory.registerModelAndView("Game", "model.FGStageModel", "view.FGStageView");

            FGRootPane rootPane = new FGRootPane();

            // View gère elle-même la scène et le stage en interne
            View view = new View(model, primaryStage, rootPane);

            FGController controller = new FGController(model, view);
            controller.setFirstStageName("Game");
            controller.startGame();

            // Ne pas créer de nouvelle Scene — View s'en est déjà chargé
            // Il suffit de configurer le stage et l'afficher
            primaryStage.setTitle("Fox & Geese");
            primaryStage.setWidth(700);
            primaryStage.setHeight(700);
            primaryStage.show();

        } catch (Exception e) {
            System.out.println("Erreur dans start() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
