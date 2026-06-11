package view;

import boardifier.view.RootPane;
import javafx.animation.ScaleTransition;
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
import javafx.util.Duration;

public class FGRootPane extends RootPane {

    // --- Écran titre ---
    private VBox titlePane;
    private TextField player1NameField;
    private TextField player2NameField;
    private Spinner<Integer> humanCountSpinner;
    private ToggleGroup foxPlayerGroup;
    private RadioButton foxHumanRadio;
    private Button newGameButton;   // bouton logique invisible, écouté par MainApp

    // --- Écran de jeu ---
    private BorderPane gamePane;
    private Label currentPlayerLabel;
    private Button backToTitleButton;
    private Button restartButton;

    // Callbacks menu in-game
    private Runnable onQuitGame;
    private Runnable onRestartGame;

    public FGRootPane() { super(); }

    @Override
    protected void createDefaultGroup() { group.getChildren().clear(); }

    public void initPanes() {
        createTitlePane();
        createGamePane();
        getChildren().addAll(titlePane, gamePane);
        showTitleScreen();
    }

    // =========================================================
    //  BOUTON IMAGE PNG CLIQUABLE
    // =========================================================

    /**
     * Crée un bouton visuel basé sur un fichier PNG.
     *
     * Comportement :
     *   - Survol   → agrandissement ×1.08 (80 ms)
     *   - Pression → rétrécissement ×0.93 (60 ms)
     *   - Relâche  → retour ×1.0 + déclenchement du onClick
     *   - setSmooth(false) → rendu pixel-art net, sans interpolation
     *
     * Si l'image est introuvable → bouton pixel-art rouge de secours.
     *
     * @param imagePath  chemin vers le PNG  (ex: "resources/title_icon.png")
     * @param width      largeur d'affichage en px
     * @param height     hauteur d'affichage en px
     * @param onClick    action à déclencher au relâchement du clic
     * @return           StackPane cliquable contenant l'ImageView
     */
    private StackPane createImageButton(String imagePath, double width, double height, Runnable onClick) {
        StackPane wrapper = new StackPane();
        wrapper.setPrefSize(width, height);
        wrapper.setMaxSize(width, height);
        wrapper.setStyle("-fx-cursor: hand;");

        try {
            Image img = new Image(imagePath);
            if (img.isError()) throw new Exception("Image introuvable : " + imagePath);

            ImageView iv = new ImageView(img);
            iv.setFitWidth(width);
            iv.setFitHeight(height);
            iv.setPreserveRatio(true);
            iv.setSmooth(false);   // pixel-art net

            // --- Animations ---
            ScaleTransition hoverIn  = new ScaleTransition(Duration.millis(80), iv);
            hoverIn.setToX(1.08); hoverIn.setToY(1.08);

            ScaleTransition hoverOut = new ScaleTransition(Duration.millis(80), iv);
            hoverOut.setToX(1.0); hoverOut.setToY(1.0);

            ScaleTransition pressDown = new ScaleTransition(Duration.millis(60), iv);
            pressDown.setToX(0.93); pressDown.setToY(0.93);

            ScaleTransition pressUp  = new ScaleTransition(Duration.millis(80), iv);
            pressUp.setToX(1.0); pressUp.setToY(1.0);

            wrapper.setOnMouseEntered(e  -> { hoverOut.stop();  hoverIn.playFromStart(); });
            wrapper.setOnMouseExited(e   -> { hoverIn.stop();   hoverOut.playFromStart(); });
            wrapper.setOnMousePressed(e  -> pressDown.playFromStart());
            wrapper.setOnMouseReleased(e -> { pressUp.playFromStart(); if (onClick != null) onClick.run(); });

            wrapper.getChildren().add(iv);

        } catch (Exception ex) {
            // Fallback pixel-art si l'image est absente
            Button fallback = new Button("▶  START GAME");
            fallback.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
            fallback.setStyle(
                    "-fx-background-color: #e94560;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 0;" +
                            "-fx-border-color: #ffffff;" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 0;" +
                            "-fx-padding: 12 32 12 32;" +
                            "-fx-cursor: hand;"
            );
            fallback.setOnAction(e -> { if (onClick != null) onClick.run(); });
            newGameButton = fallback;
            wrapper.getChildren().add(fallback);
        }
        return wrapper;
    }

    // =========================================================
    //  ÉCRAN TITRE
    // =========================================================
    private void createTitlePane() {
        titlePane = new VBox(20);
        titlePane.setPrefSize(700, 750);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setPadding(new Insets(40));

        // Fond bg_menu.png (cover)
        try {
            Image bgImg = new Image("resources/bg_menu.png");
            if (!bgImg.isError()) {
                BackgroundImage bg = new BackgroundImage(bgImg,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
                titlePane.setBackground(new Background(bg));
            } else {
                titlePane.setStyle("-fx-background-color: #1a1a2e;");
            }
        } catch (Exception e) {
            titlePane.setStyle("-fx-background-color: #1a1a2e;");
        }

        Text title = new Text("FOX & GEESE");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 48));
        title.setFill(Color.web("#e94560"));
        title.setStyle("-fx-effect: dropshadow(gaussian, black, 8, 0.7, 2, 2);");

        Text subtitle = new Text("A classic strategy game");
        subtitle.setFont(Font.font("Courier New", 14));
        subtitle.setFill(Color.WHITE);
        subtitle.setStyle("-fx-effect: dropshadow(gaussian, black, 6, 0.9, 1, 1);");

        Separator sep = new Separator();
        sep.setPrefWidth(400);

        // Formulaire
        VBox formCard = buildForm();

        // Bouton logique invisible (écouté par MainApp via getNewGameButton())
        newGameButton = new Button();
        newGameButton.setVisible(false);
        newGameButton.setManaged(false);

        // Bouton visuel : icône PNG + label en dessous
        StackPane imageBtn = createImageButton(
                "resources/title_icon.png",
                100, 100,
                () -> newGameButton.fire()
        );

        Text startLabel = new Text("▶  START GAME");
        startLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        startLabel.setFill(Color.WHITE);
        startLabel.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.8, 1, 1);");

        VBox btnBox = new VBox(6, imageBtn, startLabel);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setStyle("-fx-cursor: hand;");
        btnBox.setOnMouseClicked(e -> newGameButton.fire());

        // Ordre des enfants dans titlePane (indices utilisés par updateFormVisibility) :
        // 0=title  1=subtitle  2=sep  3=formCard  4=btnBox  5=newGameButton(invisible)
        titlePane.getChildren().addAll(title, subtitle, sep, formCard, btnBox, newGameButton);
        updateFormVisibility(2);
    }

    // =========================================================
    //  FORMULAIRE
    // =========================================================

    /**
     * Construit le panneau de configuration de la partie :
     *
     *  ┌─────────────────────────────────────────┐
     *  │  [ Human players ]       [Spinner 1-2]  │
     *  │                                         │
     *  │  [ Who plays the Fox? ]  (si 1 joueur)  │
     *  │     ● Human   ○ Bot                     │
     *  │                                         │
     *  │  ▶ Fox player name :   [TextField]      │  (si ≥1 humain)
     *  │  ▶ Geese player name : [TextField]      │  (si 2 humains)
     *  └─────────────────────────────────────────┘
     *
     * La visibilité dynamique est gérée par updateFormVisibility().
     *
     * @return VBox stylisée prête à être ajoutée au titlePane
     */
    private VBox buildForm() {
        VBox formCard = new VBox(15);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(420);
        formCard.setPadding(new Insets(20, 30, 20, 30));
        formCard.setStyle(
                "-fx-background-color: rgba(10, 10, 30, 0.75);" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 0;"
        );

        // ── 1. Nombre de joueurs humains ──────────────────────────
        HBox humanCountBox = new HBox(15);
        humanCountBox.setAlignment(Pos.CENTER_LEFT);

        Label humanCountLabel = new Label("[ Human players ]");
        humanCountLabel.setTextFill(Color.WHITE);
        humanCountLabel.setFont(Font.font("Courier New", 14));
        humanCountLabel.setMinWidth(170);

        // min=1 : on interdit 0 joueur humain
        humanCountSpinner = new Spinner<>(1, 2, 2);
        humanCountSpinner.setPrefWidth(80);
        humanCountSpinner.valueProperty().addListener(
                (obs, oldVal, newVal) -> updateFormVisibility(newVal)
        );

        humanCountBox.getChildren().addAll(humanCountLabel, humanCountSpinner);

        // ── 2. Qui joue le renard ? (affiché seulement si 1 joueur humain) ──
        VBox foxChoiceBox = new VBox(8);

        Label foxLabel = new Label("[ Who plays the Fox? ]");
        foxLabel.setTextFill(Color.WHITE);
        foxLabel.setFont(Font.font("Courier New", 14));

        foxPlayerGroup = new ToggleGroup();

        foxHumanRadio = new RadioButton("Human");
        foxHumanRadio.setTextFill(Color.web("#f5a623"));
        foxHumanRadio.setFont(Font.font("Courier New", 13));
        foxHumanRadio.setToggleGroup(foxPlayerGroup);
        foxHumanRadio.setSelected(true);    // Human joue le renard par défaut

        RadioButton foxBotRadio = new RadioButton("Bot");
        foxBotRadio.setTextFill(Color.web("#a8a8b3"));
        foxBotRadio.setFont(Font.font("Courier New", 13));
        foxBotRadio.setToggleGroup(foxPlayerGroup);

        HBox radioRow = new HBox(20, foxHumanRadio, foxBotRadio);
        radioRow.setAlignment(Pos.CENTER_LEFT);

        foxChoiceBox.getChildren().addAll(foxLabel, radioRow);

        // ── 3. Noms des joueurs ───────────────────────────────────
        VBox namesBox = new VBox(10);

        // Joueur Fox (visible dès qu'il y a au moins 1 humain)
        HBox p1Box = new HBox(15);
        p1Box.setAlignment(Pos.CENTER_LEFT);

        Label p1Label = new Label("▶ Fox player name :");
        p1Label.setTextFill(Color.web("#f5a623"));
        p1Label.setFont(Font.font("Courier New", 13));
        p1Label.setMinWidth(170);

        player1NameField = new TextField("Player 1");
        player1NameField.setPrefWidth(200);

        p1Box.getChildren().addAll(p1Label, player1NameField);

        // Joueur Geese (visible seulement si 2 humains)
        HBox p2Box = new HBox(15);
        p2Box.setAlignment(Pos.CENTER_LEFT);

        Label p2Label = new Label("▶ Geese player name :");
        p2Label.setTextFill(Color.web("#4a90d9"));
        p2Label.setFont(Font.font("Courier New", 13));
        p2Label.setMinWidth(170);

        player2NameField = new TextField("Player 2");
        player2NameField.setPrefWidth(200);

        p2Box.getChildren().addAll(p2Label, player2NameField);

        namesBox.getChildren().addAll(p1Box, p2Box);

        // ── Assemblage ────────────────────────────────────────────
        // Indices dans formCard : 0=humanCountBox  1=foxChoiceBox  2=namesBox
        formCard.getChildren().addAll(humanCountBox, foxChoiceBox, namesBox);
        return formCard;
    }

    // =========================================================
    //  VISIBILITÉ DYNAMIQUE DU FORMULAIRE
    // =========================================================

    /**
     * Met à jour la visibilité des sections du formulaire selon le nombre
     * de joueurs humains sélectionné dans le spinner.
     *
     * Règles :
     *   nbHumans == 1 → foxChoiceBox visible  (qui joue le renard ?)
     *   nbHumans >= 1 → p1Box visible         (nom du joueur Fox)
     *   nbHumans == 2 → p2Box visible         (nom du joueur Geese)
     */
    private void updateFormVisibility(int nbHumans) {
        if (titlePane == null) return;

        // titlePane[3] = formCard
        VBox formCard     = (VBox) titlePane.getChildren().get(3);
        // formCard[0]=humanCountBox  [1]=foxChoiceBox  [2]=namesBox
        VBox foxChoiceBox = (VBox) formCard.getChildren().get(1);
        VBox namesBox     = (VBox) formCard.getChildren().get(2);
        HBox p1Box        = (HBox) namesBox.getChildren().get(0);
        HBox p2Box        = (HBox) namesBox.getChildren().get(1);

        foxChoiceBox.setVisible(nbHumans == 1);
        foxChoiceBox.setManaged(nbHumans == 1);

        p1Box.setVisible(nbHumans >= 1);
        p1Box.setManaged(nbHumans >= 1);

        p2Box.setVisible(nbHumans == 2);
        p2Box.setManaged(nbHumans == 2);
    }

    // =========================================================
    //  ÉCRAN DE JEU
    // =========================================================
    private void createGamePane() {
        gamePane = new BorderPane();
        gamePane.setPrefSize(700, 750);
        gamePane.setStyle("-fx-background-color: #1a1a2e;");

        HBox menuBar = new HBox(10);
        menuBar.setPadding(new Insets(8, 15, 8, 15));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle(
                "-fx-background-color: #0f0f23;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 0 0 3 0;"
        );

        backToTitleButton = new Button("← Menu");
        backToTitleButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #a8a8b3;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );

        restartButton = new Button("↺ Restart");
        restartButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #a8a8b3;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );

        Label topTitle = new Label("FOX & GEESE");
        topTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        topTitle.setTextFill(Color.web("#e94560"));
        HBox.setHgrow(topTitle, Priority.ALWAYS);
        topTitle.setAlignment(Pos.CENTER);
        topTitle.setMaxWidth(Double.MAX_VALUE);

        menuBar.getChildren().addAll(backToTitleButton, topTitle, restartButton);
        gamePane.setTop(menuBar);

        currentPlayerLabel = new Label("Current player : -");
        currentPlayerLabel.setTextFill(Color.WHITE);
        currentPlayerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        currentPlayerLabel.setPadding(new Insets(8, 0, 8, 0));
        currentPlayerLabel.setMaxWidth(Double.MAX_VALUE);
        currentPlayerLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(currentPlayerLabel, Pos.CENTER);
        gamePane.setBottom(currentPlayerLabel);

        StackPane center = new StackPane(group);
        center.setStyle("-fx-background-color: #1a1a2e;");
        gamePane.setCenter(center);
    }

    // =========================================================
    //  NAVIGATION
    // =========================================================
    public void showTitleScreen() {
        titlePane.setVisible(true);  titlePane.setManaged(true);
        gamePane.setVisible(false);  gamePane.setManaged(false);
    }

    public void showGameScreen() {
        titlePane.setVisible(false); titlePane.setManaged(false);
        gamePane.setVisible(true);   gamePane.setManaged(true);
    }

    public void setCurrentPlayer(String name, boolean isFox) {
        currentPlayerLabel.setText((isFox ? "🦊 Fox" : "🪿 Geese") + " — " + name + "'s turn");
        currentPlayerLabel.setTextFill(isFox ? Color.web("#f5a623") : Color.web("#4a90d9"));
    }

    public void setMenuCallbacks(Runnable onQuit, Runnable onRestart) {
        this.onQuitGame    = onQuit;
        this.onRestartGame = onRestart;
        if (backToTitleButton != null && onQuit    != null) backToTitleButton.setOnAction(e -> onQuit.run());
        if (restartButton     != null && onRestart != null) restartButton.setOnAction(e -> onRestart.run());
    }

    // =========================================================
    //  GETTERS
    // =========================================================
    public Button  getNewGameButton()     { return newGameButton; }
    public Button  getBackToTitleButton() { return backToTitleButton; }
    public Button  getRestartButton()     { return restartButton; }
    public int     getHumanCount()        { return humanCountSpinner.getValue(); }
    public boolean isHumanFox()           { return foxHumanRadio.isSelected(); }
    public String  getPlayer1Name()       { String n = player1NameField.getText().trim(); return n.isEmpty() ? "Player 1" : n; }
    public String  getPlayer2Name()       { String n = player2NameField.getText().trim(); return n.isEmpty() ? "Player 2" : n; }
}