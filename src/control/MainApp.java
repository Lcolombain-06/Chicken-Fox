package control;

import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.view.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import view.FGRootPane;

public class MainApp extends Application {

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

        Scene scene = new Scene(rootPane.getGroup(), WIDTH, HEIGHT);
        primaryStage.setTitle("Fox & Geese");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Bouton "New Game" → lire le formulaire et démarrer
        rootPane.newGameButton.setOnAction(e -> startGame());

        // Bouton "← Menu" → revenir à l'écran titre
        rootPane.backToTitleButton.setOnAction(e -> {
            if (controller != null) controller.stopGame();
            rootPane.showTitleScreen();
        });
    }

    /**
     * Lit le formulaire, configure le modèle et lance la partie.
     */
    private void startGame() {
        try {
            model = new Model();

            int nbHumans = rootPane.humanCountSpinner.getValue();

            if (nbHumans == 0) {
                // Bot vs Bot
                model.addComputerPlayer("Fox Bot");
                model.addComputerPlayer("Geese Bot");

            } else if (nbHumans == 1) {
                // 1 humain
                boolean humanIsFox = rootPane.foxHumanRadio.isSelected();
                String humanName = rootPane.player1NameField.getText().trim();
                if (humanName.isEmpty()) humanName = "Player";

                if (humanIsFox) {
                    model.addHumanPlayer(humanName);
                    model.addComputerPlayer("Geese Bot");
                } else {
                    model.addComputerPlayer("Fox Bot");
                    model.addHumanPlayer(humanName);
                }

            } else {
                // 2 humains
                String p1 = rootPane.player1NameField.getText().trim();
                String p2 = rootPane.player2NameField.getText().trim();
                if (p1.isEmpty()) p1 = "Player 1";
                if (p2.isEmpty()) p2 = "Player 2";
                model.addHumanPlayer(p1);
                model.addHumanPlayer(p2);
            }

            StageFactory.registerModelAndView("Game", "model.FGStageModel", "view.FGStageView");
            View view = new View(model, primaryStage, rootPane);

            controller = new FGController(model, view);
            controller.setFirstStageName("Game");
            controller.startGame();

            // Afficher le plateau Boardifier dans le centre de gamePane
            rootPane.setBoardCenter(rootPane.getGroup().getChildren().get(0));

            // Mettre à jour le label du joueur courant
            Player first = model.getCurrentPlayer();
            rootPane.setCurrentPlayer(first.getName(), model.getIdPlayer() == 0);

            // Passer à l'écran de jeu
            rootPane.showGameScreen();

        } catch (Exception ex) {
            System.out.println("Erreur démarrage : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Affiche la fenêtre de fin de partie.
     * Appelée depuis FGController.endOfTurn() quand la partie est terminée.
     */
    public void showEndGame(String winnerName) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initOwner(primaryStage);
            alert.setHeaderText("🎉 Congratulations!");
            alert.setContentText(winnerName + " won the game!");

            ButtonType newGame = new ButtonType("New Game");
            ButtonType quit    = new ButtonType("Quit");
            alert.getButtonTypes().setAll(newGame, quit);

            alert.showAndWait().ifPresent(choice -> {
                if (choice == newGame) {
                    if (controller != null) controller.stopGame();
                    rootPane.showTitleScreen();
                } else {
                    Platform.exit();
                }
            });
        });
    }

    public FGRootPane getFGRootPane() {
        return rootPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}