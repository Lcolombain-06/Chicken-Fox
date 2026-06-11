import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.view.View;
import control.FGController;
import control.GameEndListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.FGRootPane;

public class MainApp extends Application implements GameEndListener {

    public static final int WIDTH  = 900;
    public static final int HEIGHT = 750;

    private Model model;
    private FGController controller;
    private FGRootPane rootPane;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        rootPane = new FGRootPane();
        rootPane.initPanes();

        // Donner au rootPane la référence au stage pour qu'il puisse
        // restaurer lui-même la taille au bon moment dans showTitleScreen()
        rootPane.setPrimaryStage(primaryStage, WIDTH, HEIGHT);

        Scene scene = new Scene(rootPane, WIDTH, HEIGHT);
        primaryStage.setTitle("Fox & Geese");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        rootPane.getNewGameButton().setOnAction(e -> startGame());

        rootPane.setMenuCallbacks(
                () -> {                          // ← quitter
                    stopCurrentGame();
                    rootPane.showTitleScreen();  // la restauration de taille est faite ici
                },
                () -> {                          // ← recommencer
                    stopCurrentGame();
                    startGame();
                }
        );
    }

    private void startGame() {
        try {
            model = new Model();

            boolean foxHuman   = rootPane.isFoxHuman();
            boolean geeseHuman = rootPane.isGeeseHuman();

            if (!foxHuman && !geeseHuman) {
                model.addComputerPlayer("Fox Bot");
                model.addComputerPlayer("Geese Bot");
            } else if (foxHuman && !geeseHuman) {
                model.addHumanPlayer(rootPane.getPlayer1Name());
                model.addComputerPlayer("Geese Bot");
            } else if (!foxHuman && geeseHuman) {
                model.addComputerPlayer("Fox Bot");
                model.addHumanPlayer(rootPane.getPlayer2Name());
            } else {
                model.addHumanPlayer(rootPane.getPlayer1Name());
                model.addHumanPlayer(rootPane.getPlayer2Name());
            }

            StageFactory.registerModelAndView("Game", "model.FGStageModel", "view.FGStageView");
            View view = new View(model, primaryStage, rootPane);
            controller = new FGController(model, view);
            controller.setFirstStageName("Game");
            controller.setGameEndListener(this);
            controller.startGame();

            Player first = model.getCurrentPlayer();
            rootPane.setCurrentPlayer(first.getName(), model.getIdPlayer() == 0);
            rootPane.showGameScreen();

        } catch (Exception ex) {
            System.out.println("Erreur démarrage : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void stopCurrentGame() {
        if (controller != null) {
            controller.stopGame();
            controller = null;
        }
        model = null;
    }

    @Override
    public void showEndGame(String winnerName) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initOwner(primaryStage);
            alert.setHeaderText("Congratulations!");
            alert.setContentText(winnerName + " won the game!");

            ButtonType newGame = new ButtonType("New Game");
            ButtonType quit    = new ButtonType("Quit");
            alert.getButtonTypes().setAll(newGame, quit);

            alert.showAndWait().ifPresent(choice -> {
                if (choice == newGame) {
                    stopCurrentGame();
                    startGame();
                } else {
                    Platform.exit();
                }
            });
        });
    }

    @Override
    public void updateCurrentPlayer(String name, boolean isFox) {
        Platform.runLater(() -> rootPane.setCurrentPlayer(name, isFox));
    }

    public static void main(String[] args) {
        launch(args);
    }
}