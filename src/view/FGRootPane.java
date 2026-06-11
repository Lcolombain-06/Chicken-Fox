package view;

import boardifier.view.RootPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class FGRootPane extends RootPane {

    // --- Écran titre ---
    private VBox titlePane;
    private TextField player1NameField;
    private TextField player2NameField;
    private Spinner<Integer> humanCountSpinner;
    private ToggleGroup foxPlayerGroup;
    private RadioButton foxHumanRadio;
    private Button newGameButton;

    // --- Écran de jeu ---
    private BorderPane gamePane;
    private Label currentPlayerLabel;

    // Top bar menu button (image cliquable)
    private Button menuIconButton;

    // Callbacks pour le popup de la top bar
    private Runnable onQuitGame;
    private Runnable onRestartGame;

    public FGRootPane() {
        super();
    }

    @Override
    protected void createDefaultGroup() {
        group.getChildren().clear();
    }

    /**
     * Initialise et ajoute les panneaux JavaFX.
     * DOIT être appelé depuis MainApp.start() après new FGRootPane().
     */
    public void initPanes() {
        // Charger le CSS pixel-game
        try {
            String cssPath = getClass().getResource("/resources/style.css") != null
                    ? getClass().getResource("/resources/style.css").toExternalForm()
                    : "resources/style.css";
            getStylesheets().add(cssPath);
        } catch (Exception e) {
            // CSS non critique, on continue sans
        }

        createTitlePane();
        createGamePane();

        getChildren().addAll(titlePane, gamePane);
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
        titlePane.getStyleClass().add("title-pane");
        titlePane.setStyle("-fx-background-color: #1a1a2e;");

        // Icône du jeu en haut
        ImageView titleIcon = loadImageView("resources/title_icon.png", 60, 60);
        if (titleIcon == null) {
            // Fallback si l'image n'existe pas : un carré coloré
            titleIcon = new ImageView();
        }

        Text title = new Text("FOX & GEESE");
        title.getStyleClass().add("title-text");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 48));
        title.setFill(Color.web("#e94560"));

        Text subtitle = new Text("▓▒░  A classic strategy game  ░▒▓");
        subtitle.getStyleClass().add("subtitle-text");
        subtitle.setFont(Font.font("Courier New", 14));
        subtitle.setFill(Color.web("#a8a8b3"));

        Separator sep = new Separator();
        sep.setPrefWidth(400);

        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);

        // Nombre de joueurs humains
        HBox humanCountBox = new HBox(15);
        humanCountBox.setAlignment(Pos.CENTER_LEFT);
        Label humanCountLabel = new Label("[ Human players ]");
        humanCountLabel.getStyleClass().add("form-label");
        humanCountLabel.setTextFill(Color.WHITE);
        humanCountLabel.setFont(Font.font("Courier New", 14));
        humanCountLabel.setMinWidth(170);
        humanCountSpinner = new Spinner<>(1, 2, 2);
        humanCountSpinner.setPrefWidth(80);
        humanCountSpinner.getStyleClass().add("game-spinner");
        humanCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateFormVisibility(newVal));
        humanCountBox.getChildren().addAll(humanCountLabel, humanCountSpinner);

        // Choix qui joue le renard
        VBox foxChoiceBox = new VBox(8);
        Label foxLabel = new Label("[ Who plays the Fox? ]");
        foxLabel.setTextFill(Color.WHITE);
        foxLabel.setFont(Font.font("Courier New", 14));
        foxPlayerGroup = new ToggleGroup();
        foxHumanRadio = new RadioButton("Human");
        foxHumanRadio.setTextFill(Color.web("#f5a623"));
        foxHumanRadio.setToggleGroup(foxPlayerGroup);
        foxHumanRadio.setSelected(true);
        RadioButton foxBotRadio = new RadioButton("Bot");
        foxBotRadio.setTextFill(Color.web("#a8a8b3"));
        foxBotRadio.setToggleGroup(foxPlayerGroup);
        foxChoiceBox.getChildren().addAll(foxLabel, new HBox(20, foxHumanRadio, foxBotRadio));

        // Noms des joueurs
        VBox namesBox = new VBox(10);
        HBox p1Box = new HBox(15);
        p1Box.setAlignment(Pos.CENTER_LEFT);
        Label p1Label = new Label("▶ Fox player name :");
        p1Label.getStyleClass().add("fox-label");
        p1Label.setTextFill(Color.web("#f5a623"));
        p1Label.setFont(Font.font("Courier New", 13));
        p1Label.setMinWidth(170);
        player1NameField = new TextField("Player 1");
        player1NameField.setPrefWidth(200);
        player1NameField.getStyleClass().add("game-field");
        p1Box.getChildren().addAll(p1Label, player1NameField);

        HBox p2Box = new HBox(15);
        p2Box.setAlignment(Pos.CENTER_LEFT);
        Label p2Label = new Label("▶ Geese player name :");
        p2Label.getStyleClass().add("geese-label");
        p2Label.setTextFill(Color.web("#4a90d9"));
        p2Label.setFont(Font.font("Courier New", 13));
        p2Label.setMinWidth(170);
        player2NameField = new TextField("Player 2");
        player2NameField.setPrefWidth(200);
        player2NameField.getStyleClass().add("game-field");
        p2Box.getChildren().addAll(p2Label, player2NameField);

        namesBox.getChildren().addAll(p1Box, p2Box);
        form.getChildren().addAll(humanCountBox, foxChoiceBox, namesBox);

        newGameButton = new Button("▶  START GAME");
        newGameButton.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        newGameButton.setTextFill(Color.web("#ffffff"));
        newGameButton.setStyle(
                "-fx-background-color: #e94560;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: #ffffff;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 0;" +
                        "-fx-padding: 12 32 12 32;" +
                        "-fx-cursor: hand;"
        );
        newGameButton.setOnMouseEntered(e -> newGameButton.setStyle(
                "-fx-background-color: #c73652;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: #ffffff;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 0;" +
                        "-fx-padding: 12 32 12 32;" +
                        "-fx-cursor: hand;"
        ));
        newGameButton.setOnMouseExited(e -> newGameButton.setStyle(
                "-fx-background-color: #e94560;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: #ffffff;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 0;" +
                        "-fx-padding: 12 32 12 32;" +
                        "-fx-cursor: hand;"
        ));

        // Sélecteur de couleur de fond
        HBox bgColorBox = new HBox(12);
        bgColorBox.setAlignment(Pos.CENTER);
        Label bgLabel = new Label("[ Background color ]");
        bgLabel.setTextFill(Color.web("#a8a8b3"));
        bgLabel.setFont(Font.font("Courier New", 13));
        javafx.scene.control.ColorPicker bgColorPicker = new javafx.scene.control.ColorPicker(Color.web("#1a1a2e"));
        bgColorPicker.setPrefWidth(100);
        bgColorPicker.setStyle("-fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        bgColorPicker.setOnAction(e -> {
            Color c = bgColorPicker.getValue();
            String hex = String.format("#%02x%02x%02x",
                    (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
            titlePane.setStyle("-fx-background-color: " + hex + ";");
        });
        bgColorBox.getChildren().addAll(bgLabel, bgColorPicker);

        VBox titleIconBox = new VBox(6, titleIcon, title);
        titleIconBox.setAlignment(Pos.CENTER);

        titlePane.getChildren().addAll(titleIconBox, subtitle, sep, form, newGameButton, bgColorBox);
        updateFormVisibility(2);
    }

    private void updateFormVisibility(int nbHumans) {
        if (titlePane == null) return;
        VBox form         = (VBox) titlePane.getChildren().get(3);
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
        gamePane.getStyleClass().add("game-pane");
        gamePane.setStyle("-fx-background-color: #1a1a2e;");

        // Top bar
        HBox topBar = createTopBar();
        gamePane.setTop(topBar);

        // Label joueur courant (bas)
        currentPlayerLabel = new Label("Current player : -");
        currentPlayerLabel.getStyleClass().add("current-player-label");
        currentPlayerLabel.setTextFill(Color.WHITE);
        currentPlayerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        currentPlayerLabel.setPadding(new Insets(8, 16, 8, 16));
        currentPlayerLabel.setMaxWidth(Double.MAX_VALUE);
        currentPlayerLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(currentPlayerLabel, Pos.CENTER);
        gamePane.setBottom(currentPlayerLabel);

        // Zone de jeu (group Boardifier) au centre
        StackPane center = new StackPane(group);
        center.setStyle("-fx-background-color: #1a1a2e;");
        gamePane.setCenter(center);
    }

    /**
     * Crée la top bar de l'écran de jeu.
     * Contient : icône menu (cliquable → popup) | titre centré | espace droit équivalent.
     */
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("game-topbar");
        topBar.setStyle(
                "-fx-background-color: #0f0f23;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 0 0 3 0;" +
                        "-fx-padding: 6 16 6 16;"
        );
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Bouton icône gauche
        menuIconButton = new Button();
        menuIconButton.getStyleClass().add("menu-icon-btn");
        menuIconButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8 4 8;");

        ImageView menuImg = loadImageView("resources/menu_icon.png", 32, 32);
        if (menuImg != null) {
            menuIconButton.setGraphic(menuImg);
        } else {
            // Fallback texte si l'image n'est pas trouvée
            menuIconButton.setText("☰");
            menuIconButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #f5a623;" +
                            "-fx-font-size: 22px;" +
                            "-fx-cursor: hand;"
            );
        }

        menuIconButton.setOnAction(e -> showMenuPopup());

        // Titre centré
        Label titleLabel = new Label("FOX & GEESE");
        titleLabel.getStyleClass().add("topbar-title");
        titleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#e94560"));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Espace droit pour équilibrer (même largeur que le bouton gauche)
        Region spacer = new Region();
        spacer.setPrefWidth(50);

        topBar.getChildren().addAll(menuIconButton, titleLabel, spacer);
        return topBar;
    }

    /**
     * Affiche le popup de menu en jeu (quitter / recommencer).
     */
    private void showMenuPopup() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Game Menu");
        alert.setHeaderText("⚙  GAME MENU");
        alert.setContentText("What would you like to do?");

        ButtonType restartBtn = new ButtonType("↺  Restart");
        ButtonType quitBtn    = new ButtonType("✖  Quit to Menu");
        ButtonType cancelBtn  = new ButtonType("▶  Continue", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(restartBtn, quitBtn, cancelBtn);

        // Style du dialog
        alert.getDialogPane().setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 3px;"
        );

        // Appliquer le style à tous les boutons du dialog
        alert.getDialogPane().getButtonTypes().forEach(bt -> {
            Button b = (Button) alert.getDialogPane().lookupButton(bt);
            b.setStyle(
                    "-fx-background-color: #16213e;" +
                            "-fx-text-fill: #e0e0e0;" +
                            "-fx-font-family: 'Courier New', monospace;" +
                            "-fx-border-color: #e94560;" +
                            "-fx-border-width: 2px;" +
                            "-fx-background-radius: 0;" +
                            "-fx-border-radius: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 8 16 8 16;"
            );
            b.setOnMouseEntered(ev -> b.setStyle(
                    "-fx-background-color: #e94560;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Courier New', monospace;" +
                            "-fx-border-color: #e94560;" +
                            "-fx-border-width: 2px;" +
                            "-fx-background-radius: 0;" +
                            "-fx-border-radius: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 8 16 8 16;"
            ));
            b.setOnMouseExited(ev -> b.setStyle(
                    "-fx-background-color: #16213e;" +
                            "-fx-text-fill: #e0e0e0;" +
                            "-fx-font-family: 'Courier New', monospace;" +
                            "-fx-border-color: #e94560;" +
                            "-fx-border-width: 2px;" +
                            "-fx-background-radius: 0;" +
                            "-fx-border-radius: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 8 16 8 16;"
            ));
        });

        // Style header
        if (alert.getDialogPane().lookup(".header-panel") != null) {
            alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #0f0f23;");
        }

        alert.showAndWait().ifPresent(choice -> {
            if (choice == restartBtn && onRestartGame != null) {
                onRestartGame.run();
            } else if (choice == quitBtn && onQuitGame != null) {
                onQuitGame.run();
            }
        });
    }

    // =============================================
    //  NAVIGATION
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

    public void setCurrentPlayer(String name, boolean isFox) {
        String symbol = isFox ? "🦊" : "🪿";
        currentPlayerLabel.setText(symbol + "  " + (isFox ? "Fox" : "Geese") + " — " + name + "'s turn");
        currentPlayerLabel.setTextFill(isFox ? Color.web("#f5a623") : Color.web("#4a90d9"));
    }

    // =============================================
    //  UTILITAIRE IMAGES
    // =============================================
    private ImageView loadImageView(String path, double w, double h) {
        try {
            Image img = new Image(path);
            if (!img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(w);
                iv.setFitHeight(h);
                iv.setPreserveRatio(true);
                return iv;
            }
        } catch (Exception e) {
            // Image non trouvée, retourner null → fallback
        }
        return null;
    }

    // =============================================
    //  GETTERS
    // =============================================
    public Button  getNewGameButton()      { return newGameButton; }
    public int     getHumanCount()         { return humanCountSpinner.getValue(); }
    public boolean isHumanFox()            { return foxHumanRadio.isSelected(); }
    public String  getPlayer1Name() {
        String n = player1NameField.getText().trim();
        return n.isEmpty() ? "Player 1" : n;
    }
    public String  getPlayer2Name() {
        String n = player2NameField.getText().trim();
        return n.isEmpty() ? "Player 2" : n;
    }

    /**
     * Définit les callbacks déclenchés depuis le popup de la top bar.
     * @param onQuit     exécuté quand l'utilisateur choisit "Quitter"
     * @param onRestart  exécuté quand l'utilisateur choisit "Recommencer"
     */
    public void setMenuCallbacks(Runnable onQuit, Runnable onRestart) {
        this.onQuitGame    = onQuit;
        this.onRestartGame = onRestart;
    }
}