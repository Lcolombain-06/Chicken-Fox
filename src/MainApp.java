import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.view.RootPane;
import boardifier.view.View;
import control.FGController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.FGStageModel;
import view.BoardRender;

public class MainApp extends Application {

    public static final int WIDTH  = 700;
    public static final int HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        try {
            // --- Initialisation du jeu ---
            Model model = new Model();
            model.addHumanPlayer("Player1");  // ← futur écran titre
            model.addHumanPlayer("Player2");

            StageFactory.registerModelAndView("Game", "model.FGStageModel", "view.FGStageView");

            // Version graphique : View nécessite Stage + RootPane
            RootPane rootPane = new RootPane();
            View view = new View(model, primaryStage, rootPane);

            // FGController instancie FGControllerMouse dans son constructeur
            FGController controller = new FGController(model, view);
            controller.setFirstStageName("Game");
            controller.startGame();

            // --- Image du plateau par-dessus le RootPane ---
            Image boardImage = new Image(getClass().getResourceAsStream("/board.png"));
            ImageView boardView = new ImageView(boardImage);
            boardView.setFitWidth(WIDTH);
            boardView.setFitHeight(HEIGHT);
            boardView.setPreserveRatio(true);

            // On place l'image derrière les éléments Boardifier
            rootPane.getChildren().add(0, boardView);

            // Affichage initial des pions
            FGStageModel stageModel = (FGStageModel) model.getGameStage();
            BoardRender renderer = new BoardRender(rootPane, stageModel, WIDTH);
            renderer.refresh();

            // --- Scène ---
            Scene scene = new Scene(rootPane, WIDTH, HEIGHT);
            primaryStage.setTitle("Fox & Geese");
            primaryStage.setScene(scene);
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