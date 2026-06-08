import boardifier.model.Model;
import control.FGController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.FGStageModel;

public class MainApp extends Application {
    // appelle pour demarrer le jeu
    private static Model model;
    private static FGController controller;

    public static void setContext(Model m, FGController c) {
        model = m;
        controller = c;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Pane root = new Pane();

            // Placer board.png dans src/resources/
            Image boardImage = new Image("file:src/resources/board.png");
            ImageView boardView = new ImageView(boardImage);

            boardView.setFitWidth(700);
            boardView.setPreserveRatio(true);
            root.getChildren().add(boardView);

            FGStageModel stageModel = (FGStageModel) model.getGameStage();
            BoardRender renderer = new BoardRender(root, stageModel, 700);
            renderer.refresh();

            // --- Scène et handler de clic ---
            Scene scene = new Scene(root, 700, 700);
            ClickHandler clickHandler = new ClickHandler(700, 700, this.controller, renderer);
            scene.setOnMouseClicked(clickHandler);

            //DebugGrid debugGrid = new DebugGrid(root, 700);
            //debugGrid.showGrid();

            primaryStage.setTitle("Fox & Geese");
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        catch (Exception e) {
            System.out.println("Erreur dans Start(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
