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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import view.FGRootPane;

public class MainApp extends Application implements GameEndListener {

    public static final int WIDTH  = 900;
    public static final int HEIGHT = 750;

    private Model model;
    private FGController controller;
    private FGRootPane rootPane;
    private Stage primaryStage;

    // Build the UI, set up the scene, and wire the title screen buttons.
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        rootPane = new FGRootPane();
        rootPane.initPanes();

        rootPane.setPrimaryStage(primaryStage, WIDTH, HEIGHT);

        Scene scene = new Scene(rootPane, WIDTH, HEIGHT);
        primaryStage.setTitle("Fox & Geese");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        rootPane.getNewGameButton().setOnAction(e -> startGame());

        rootPane.setMenuCallbacks(
                () -> {
                    stopCurrentGame();
                    rootPane.showTitleScreen();
                },
                () -> {
                    stopCurrentGame();
                    startGame();
                }
        );
    }

    // Create a new model and players based on the title screen choices, then start the game.
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

    // Stop the running controller and clear the model, if any.
    private void stopCurrentGame() {
        if (controller != null) {
            controller.stopGame();
            controller = null;
        }
        model = null;
    }

    // Show the end-of-game popup with styled "New Game" and "Quit" buttons.
    @Override
    public void showEndGame(String winnerName) {
        Platform.runLater(() -> {
            stopCurrentGame();

            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(primaryStage);
            alert.setTitle("Fox & Geese");
            alert.setHeaderText("Congratulations!");
            alert.setContentText(winnerName + " won the game!");

            ButtonType newGame = new ButtonType("New Game", ButtonType.OK.getButtonData());
            ButtonType quit    = new ButtonType("Quit", ButtonType.CANCEL.getButtonData());
            alert.getButtonTypes().setAll(newGame, quit);

            DialogPane pane = alert.getDialogPane();
            pane.setStyle(
                    "-fx-background-color: #013039;" +
                            "-fx-border-color: rgba(2,21,37,0.77);" +
                            "-fx-border-width: 3;"
            );
            pane.lookup(".header-panel").setStyle(
                    "-fx-background-color: #013039;"
            );
            pane.lookup(".content.label").setStyle(
                    "-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 14px;"
            );
            pane.lookup(".header-panel .label").setStyle(
                    "-fx-text-fill: #e94560; -fx-font-family: 'Courier New';" +
                            "-fx-font-weight: bold; -fx-font-size: 20px;"
            );

            String pinkBtn =
                    "-fx-background-color: #e94560; -fx-text-fill: white;" +
                            "-fx-font-family: 'Courier New'; -fx-font-weight: bold;" +
                            "-fx-background-radius: 10; -fx-padding: 8 20;";
            String greenBtn =
                    "-fx-background-color: #4a90d9; -fx-text-fill: white;" +
                            "-fx-font-family: 'Courier New'; -fx-font-weight: bold;" +
                            "-fx-background-radius: 10; -fx-padding: 8 20;";

            Button newGameBtn = (Button) pane.lookupButton(newGame);
            Button quitBtn    = (Button) pane.lookupButton(quit);
            newGameBtn.setStyle(pinkBtn);
            quitBtn.setStyle(greenBtn);

            alert.showAndWait().ifPresent(choice -> {
                if (choice == newGame) {
                    startGame();
                } else {
                    rootPane.showTitleScreen();
                }
            });
        });
    }

    // Update the current player label on the UI thread.
    @Override
    public void updateCurrentPlayer(String name, boolean isFox) {
        Platform.runLater(() -> rootPane.setCurrentPlayer(name, isFox));
    }

    public static void main(String[] args) {
        launch(args);
    }
}