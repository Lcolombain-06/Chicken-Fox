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

        Scene scene = new Scene(rootPane, WIDTH, HEIGHT);
        primaryStage.setTitle("Fox & Geese");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        rootPane.getNewGameButton().setOnAction(e -> startGame());

        // Callbacks pour les boutons du gamePane (quitter + recommencer)
        rootPane.setMenuCallbacks(
                () -> {                          // ← quitter
                    stopCurrentGame();
                    rootPane.showTitleScreen();
                    // CORRECTION 1 : différer d'un pulse pour laisser boardifier
                    // terminer ses propres appels setWidth/Height avant de restaurer
                    Platform.runLater(this::restoreWindowSize);
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
        // CORRECTION 2 : nullifier aussi le modèle pour que boardifier
        // ne garde pas de fils/listeners accrochés à l'ancien modèle
        model = null;
    }

    // Remet la fenêtre à la taille de l'écran titre
    // (boardifier peut l'avoir redimensionnée)
    private void restoreWindowSize() {
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
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