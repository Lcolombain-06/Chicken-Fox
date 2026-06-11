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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.FGRootPane;

public class MainApp extends Application implements GameEndListener {

    public static final int WIDTH  = 700;
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

        Scene scene = new Scene(rootPane);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Fox & Geese");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Bouton démarrer une partie
        rootPane.getNewGameButton().setOnAction(e -> startGame());

        // Callbacks du popup de la top bar
        rootPane.setMenuCallbacks(
                () -> {
                    // Quitter → retour au menu titre
                    if (controller != null) controller.stopGame();
                    rootPane.showTitleScreen();
                },
                () -> {
                    // Recommencer → arrêter et relancer immédiatement
                    if (controller != null) controller.stopGame();
                    startGame();
                }
        );
    }

    private void startGame() {

        try {

            model = new Model();

            boolean foxHuman   = rootPane.isFoxHuman();
            boolean geeseHuman = rootPane.isGeeseHuman();

            // 0 humain
            if(!foxHuman && !geeseHuman) {

                model.addComputerPlayer("Fox Bot");
                model.addComputerPlayer("Geese Bot");
            }

            // Fox humain
            else if(foxHuman && !geeseHuman) {

                model.addHumanPlayer(rootPane.getPlayer1Name());
                model.addComputerPlayer("Geese Bot");
            }

            // Geese humain
            else if(!foxHuman && geeseHuman) {

                model.addComputerPlayer("Fox Bot");
                model.addHumanPlayer(rootPane.getPlayer2Name());
            }

            // 2 humains
            else {

                model.addHumanPlayer(rootPane.getPlayer1Name());
                model.addHumanPlayer(rootPane.getPlayer2Name());
            }

            StageFactory.registerModelAndView(
                    "Game",
                    "model.FGStageModel",
                    "view.FGStageView"
            );

            View view = new View(
                    model,
                    primaryStage,
                    rootPane
            );

            controller = new FGController(model, view);

            controller.setFirstStageName("Game");
            controller.setGameEndListener(this);
            controller.startGame();

            Player first = model.getCurrentPlayer();

            rootPane.setCurrentPlayer(
                    first.getName(),
                    model.getIdPlayer() == 0
            );

            rootPane.showGameScreen();

        }
        catch(Exception ex) {

            System.out.println(
                    "Erreur démarrage : "
                            + ex.getMessage()
            );

            ex.printStackTrace();
        }
    }

    @Override
    public void showEndGame(String winnerName) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(primaryStage);
            alert.setTitle("Game Over");
            alert.setHeaderText("🏆  GAME OVER");
            alert.setContentText(winnerName + " won the game!");

            ButtonType newGame = new ButtonType("↺  New Game");
            ButtonType quit    = new ButtonType("✖  Quit", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(newGame, quit);

            // Style dialog fin de partie
            alert.getDialogPane().setStyle(
                    "-fx-background-color: #1a1a2e;" +
                            "-fx-border-color: #f5a623;" +
                            "-fx-border-width: 3px;"
            );

            alert.getDialogPane().getButtonTypes().forEach(bt -> {
                Button b = (Button) alert.getDialogPane().lookupButton(bt);
                b.setStyle(
                        "-fx-background-color: #16213e;" +
                                "-fx-text-fill: #e0e0e0;" +
                                "-fx-font-family: 'Courier New', monospace;" +
                                "-fx-border-color: #f5a623;" +
                                "-fx-border-width: 2px;" +
                                "-fx-background-radius: 0;" +
                                "-fx-border-radius: 0;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 16 8 16;"
                );
            });

            alert.showAndWait().ifPresent(choice -> {
                if (choice == newGame) {
                    if (controller != null) controller.stopGame();
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