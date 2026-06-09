package view;

import boardifier.view.RootPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * FGRootPane — gère les deux écrans de l'application :
 *   - titlePane  : écran titre avec formulaire de configuration
 *   - gamePane   : écran de jeu avec menu et plateau
 *
 * Les deux panneaux sont empilés dans le group de RootPane.
 * On affiche l'un ou l'autre via show/hide.
 */
public class FGRootPane extends RootPane {

    // --- Écran titre ---
    private VBox titlePane;
    public TextField player1NameField;
    public TextField player2NameField;
    public Spinner<Integer> humanCountSpinner;
    public ToggleGroup foxPlayerGroup;
    public RadioButton foxHumanRadio;
    public RadioButton foxBotRadio;
    public Button newGameButton;

    // --- Écran de jeu ---
    private BorderPane gamePane;
    public Label currentPlayerLabel;
    public Button backToTitleButton;

    public FGRootPane() {
        super();
    }

    @Override
    public void createDefaultGroup() {
        // Fond noir général
        group.getChildren().clear();

        createTitlePane();
        createGamePane();

        // Au démarrage, afficher l'écran titre
        group.getChildren().addAll(titlePane, gamePane);
        showTitleScreen();
    }

    // =============================================
    //  ÉCRAN TITRE
    // =============================================
    private void createTitlePane() {
        titlePane = new VBox(20);
        titlePane.setPrefSize(700, 750);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setPadding(new Insets(40));
        titlePane.setStyle("-fx-background-color: #1a1a2e;");

        // Titre du jeu
        Text title = new Text("FOX & GEESE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setFill(Color.web("#e94560"));

        Text subtitle = new Text("A classic strategy game");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setFill(Color.web("#a8a8b3"));

        // Séparateur
        Separator sep = new Separator();
        sep.setPrefWidth(400);

        // --- Formulaire ---
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(400);

        // Nombre de joueurs humains
        HBox humanCountBox = new HBox(15);
        humanCountBox.setAlignment(Pos.CENTER_LEFT);
        Label humanCountLabel = new Label("Human players :");
        humanCountLabel.setTextFill(Color.WHITE);
        humanCountLabel.setMinWidth(160);
        humanCountSpinner = new Spinner<>(0, 2, 2);
        humanCountSpinner.setPrefWidth(80);
        humanCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateFormVisibility(newVal));
        humanCountBox.getChildren().addAll(humanCountLabel, humanCountSpinner);

        // Qui joue le renard ? (visible si 1 humain)
        VBox foxChoiceBox = new VBox(8);
        Label foxLabel = new Label("Who plays the Fox ?");
        foxLabel.setTextFill(Color.WHITE);
        foxPlayerGroup = new ToggleGroup();
        foxHumanRadio = new RadioButton("Human");
        foxHumanRadio.setTextFill(Color.web("#f5a623"));
        foxHumanRadio.setToggleGroup(foxPlayerGroup);
        foxHumanRadio.setSelected(true);
        foxBotRadio = new RadioButton("Bot");
        foxBotRadio.setTextFill(Color.web("#a8a8b3"));
        foxBotRadio.setToggleGroup(foxPlayerGroup);
        HBox radioBox = new HBox(20, foxHumanRadio, foxBotRadio);
        foxChoiceBox.getChildren().addAll(foxLabel, radioBox);

        // Noms des joueurs
        VBox namesBox = new VBox(10);

        HBox p1Box = new HBox(15);
        p1Box.setAlignment(Pos.CENTER_LEFT);
        Label p1Label = new Label("Fox player name :");
        p1Label.setTextFill(Color.web("#f5a623"));
        p1Label.setMinWidth(160);
        player1NameField = new TextField("Player 1");
        player1NameField.setPrefWidth(200);
        p1Box.getChildren().addAll(p1Label, player1NameField);

        HBox p2Box = new HBox(15);
        p2Box.setAlignment(Pos.CENTER_LEFT);
        Label p2Label = new Label("Geese player name :");
        p2Label.setTextFill(Color.web("#4a90d9"));
        p2Label.setMinWidth(160);
        player2NameField = new TextField("Player 2");
        player2NameField.setPrefWidth(200);
        p2Box.getChildren().addAll(p2Label, player2NameField);

        namesBox.getChildren().addAll(p1Box, p2Box);

        form.getChildren().addAll(humanCountBox, foxChoiceBox, namesBox);

        // Bouton New Game
        newGameButton = new Button("▶  NEW GAME");
        newGameButton.setStyle(
                "-fx-background-color: #e94560;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 40 12 40;" +
                        "-fx-cursor: hand;"
        );

        titlePane.getChildren().addAll(title, subtitle, sep, form, newGameButton);

        // Initialiser la visibilité selon la valeur par défaut (2 humains)
        updateFormVisibility(2);
    }

    /**
     * Adapte le formulaire selon le nombre de joueurs humains sélectionné.
     */
    private void updateFormVisibility(int nbHumans) {
        if (titlePane == null) return;
        VBox form = (VBox) titlePane.getChildren().get(3);
        VBox foxChoiceBox = (VBox) form.getChildren().get(1);
        VBox namesBox     = (VBox) form.getChildren().get(2);
        HBox p1Box        = (HBox) namesBox.getChildren().get(0);
        HBox p2Box        = (HBox) namesBox.getChildren().get(1);

        foxChoiceBox.setVisible(nbHumans == 1);
        foxChoiceBox.setManaged(nbHumans == 1);
        p1Box.setVisible(nbHumans >= 1);
        p1Box.setManaged(nbHumans >= 1);
        p2Box.setVisible(nbHumans == 2);
        p2Box.setManaged(nbHumans == 2);
    }

    // =============================================
    //  ÉCRAN DE JEU
    // =============================================
    private void createGamePane() {
        gamePane = new BorderPane();
        gamePane.setPrefSize(700, 750);
        gamePane.setStyle("-fx-background-color: #1a1a2e;");

        // --- Menu en haut ---
        HBox menuBar = new HBox(10);
        menuBar.setPadding(new Insets(8, 15, 8, 15));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle("-fx-background-color: #16213e;");

        backToTitleButton = new Button("← Menu");
        backToTitleButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #a8a8b3;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );

        menuBar.getChildren().add(backToTitleButton);
        gamePane.setTop(menuBar);

        // --- Nom du joueur courant ---
        currentPlayerLabel = new Label("Current player : -");
        currentPlayerLabel.setTextFill(Color.WHITE);
        currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        currentPlayerLabel.setPadding(new Insets(8, 0, 8, 0));
        BorderPane.setAlignment(currentPlayerLabel, Pos.CENTER);
        gamePane.setBottom(currentPlayerLabel);

        // Le plateau de jeu (group de Boardifier) sera ajouté au centre par control.MainApp
    }

    // =============================================
    //  NAVIGATION ENTRE ÉCRANS
    // =============================================
    public void showTitleScreen() {
        titlePane.setVisible(true);
        titlePane.setManaged(true);
        gamePane.setVisible(false);
        gamePane.setManaged(false);
    }

    public void showGameScreen() {
        titlePane.setVisible(false);
        titlePane.setManaged(false);
        gamePane.setVisible(true);
        gamePane.setManaged(true);
    }

    /**
     * Permet à control.MainApp de placer le plateau Boardifier au centre de l'écran de jeu.
     */
    public void setBoardCenter(javafx.scene.Node boardNode) {
        StackPane center = new StackPane(boardNode);
        center.setStyle("-fx-background-color: #1a1a2e;");
        gamePane.setCenter(center);
    }

    /**
     * Met à jour le label du joueur courant.
     */
    public void setCurrentPlayer(String name, boolean isFox) {
        String icon = isFox ? "🦊" : "🪿";
        currentPlayerLabel.setText(icon + "  " + name + "'s turn");
        currentPlayerLabel.setTextFill(isFox ? Color.web("#f5a623") : Color.web("#4a90d9"));
    }
}