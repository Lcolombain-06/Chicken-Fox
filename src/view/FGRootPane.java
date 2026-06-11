package view;

import boardifier.view.RootPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
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
    private ToggleGroup foxGroup;
    private ToggleGroup geeseGroup;

    private ToggleButton foxHumanBtn;
    private ToggleButton foxBotBtn;

    private ToggleButton geeseHumanBtn;
    private ToggleButton geeseBotBtn;
    private Button newGameButton;

    // --- Écran de jeu ---
    private BorderPane gamePane;
    private Label currentPlayerLabel;
    private Button backToTitleButton;
    private Button restartButton;

    // Callbacks
    private Runnable onQuitGame;
    private Runnable onRestartGame;

    public FGRootPane() {
        super();
    }

    @Override
    protected void createDefaultGroup() {
        group.getChildren().clear();
    }

    public void initPanes() {
        createTitlePane();
        createGamePane();
        getChildren().addAll(titlePane, gamePane);
        showTitleScreen();
    }

    // =============================================
    //  BOUTON IMAGE AVEC EFFETS HOVER
    // =============================================
    private StackPane createImageButton(String imagePath, int width, int height, Runnable onClick) {
        StackPane wrapper = new StackPane();

        try {
            ImageView imageView = new ImageView(new Image(imagePath));
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);

            wrapper.setOnMouseEntered(e -> {
                imageView.setScaleX(1.08);
                imageView.setScaleY(1.08);
            });
            wrapper.setOnMouseExited(e -> {
                imageView.setScaleX(1.0);
                imageView.setScaleY(1.0);
            });
            wrapper.setOnMousePressed(e -> {
                imageView.setScaleX(0.95);
                imageView.setScaleY(0.95);
            });
            wrapper.setOnMouseReleased(e -> {
                imageView.setScaleX(1.08);
                imageView.setScaleY(1.08);
                if (onClick != null) onClick.run();
            });

            wrapper.getChildren().add(imageView);

        } catch (Exception ex) {
            Button fallback = new Button("START");
            fallback.setOnAction(e -> { if (onClick != null) onClick.run(); });
            wrapper.getChildren().add(fallback);
        }

        return wrapper;
    }

    // =============================================
    //  TOGGLE BUTTON STYLÉ
    // =============================================
    private ToggleButton createCuteToggle(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setPrefSize(90, 60);
        btn.setStyle(
                "-fx-background-color:#424769;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:22px;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:#F6B17A;" +
                        "-fx-border-width:2;"
        );
        btn.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                btn.setStyle(
                        "-fx-background-color:#F6B17A;" +
                                "-fx-text-fill:black;" +
                                "-fx-font-size:22px;" +
                                "-fx-background-radius:12;" +
                                "-fx-border-radius:12;" +
                                "-fx-border-color:white;" +
                                "-fx-border-width:3;"
                );
            } else {
                btn.setStyle(
                        "-fx-background-color:#424769;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-size:22px;" +
                                "-fx-background-radius:12;" +
                                "-fx-border-radius:12;" +
                                "-fx-border-color:#F6B17A;" +
                                "-fx-border-width:2;"
                );
            }
        });
        return btn;
    }

    // =============================================
    //  ÉCRAN TITRE
    // =============================================
    private void createTitlePane() {
        titlePane = new VBox(20);
        titlePane.setPrefSize(700, 750);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setPadding(new Insets(40));

        // --- Fond bg_menu.png ---
        try {
            Image bgImg = new Image("resources/bg_menu.png");
            if (!bgImg.isError()) {
                BackgroundImage bg = new BackgroundImage(
                        bgImg,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                titlePane.setBackground(new Background(bg));
            } else {
                titlePane.setStyle("-fx-background-color: #1a1a2e;");
            }
        } catch (Exception e) {
            titlePane.setStyle("-fx-background-color: #1a1a2e;");
        }

        // --- Titre flottant ---
        Text title = new Text("FOX & GEESE");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 48));
        title.setFill(Color.web("#e94560"));
        title.setStyle("-fx-effect: dropshadow(gaussian, black, 8, 0.6, 2, 2);");

        Timeline floatAnim = new Timeline(
                new KeyFrame(Duration.ZERO,        e -> title.setTranslateY(0)),
                new KeyFrame(Duration.seconds(1.5), e -> title.setTranslateY(-6)),
                new KeyFrame(Duration.seconds(3),   e -> title.setTranslateY(0))
        );
        floatAnim.setCycleCount(Timeline.INDEFINITE);
        floatAnim.play();

        Text subtitle = new Text("A classic strategy game");
        subtitle.setFont(Font.font("Courier New", 14));
        subtitle.setFill(Color.WHITE);
        subtitle.setStyle("-fx-effect: dropshadow(gaussian, black, 6, 0.8, 1, 1);");

        Separator sep = new Separator();
        sep.setPrefWidth(400);

        // --- CORRECTION 1 : déclaration de formCard avant setStyle ---
        VBox formCard = new VBox(15);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(520);
        formCard.setPadding(new Insets(25));

        // --- CORRECTION 2 : DropShadow via setEffect, pas -fx-effect dans setStyle ---
        formCard.setStyle(
                "-fx-background-color: rgba(45,50,80,0.5);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: rgba(6,87,94,0.35);" +
                        "-fx-border-width: 3;"
        );
        DropShadow shadow = new DropShadow();
        shadow.setRadius(18);
        shadow.setOffsetY(5);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        formCard.setEffect(shadow);

        // --- Style des champs texte ---
        String fieldStyle =
                "-fx-background-color:#FFF8E8;" +
                        "-fx-text-fill:#2D3250;" +
                        "-fx-font-size:14px;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-radius:10;" +
                        "-fx-border-color:#F6B17A;" +
                        "-fx-border-width:2;";

        // --- CORRECTION 3 : créer les deux TextField AVANT d'appeler setStyle ---
        player1NameField = new TextField("Player 1");
        player2NameField = new TextField("Player 2");
        player1NameField.setPrefWidth(200);
        player2NameField.setPrefWidth(200);
        player1NameField.setStyle(fieldStyle);
        player2NameField.setStyle(fieldStyle);

        // --- Carte FOX ---
        ImageView foxImg = new ImageView(new Image("resources/Fox.png"));
        foxImg.setFitWidth(72);
        foxImg.setFitHeight(72);

        Label foxTitle2 = new Label("FOX");
        foxTitle2.setTextFill(Color.web("#F6B17A"));
        foxTitle2.setFont(Font.font("Courier New", FontWeight.BOLD, 18));

        foxGroup    = new ToggleGroup();
        foxHumanBtn = createImageToggle(
                "resources/title_icon.png"
        );

        foxBotBtn = createImageToggle(
                "resources/bot.png"
        );
        foxHumanBtn.setToggleGroup(foxGroup);
        foxBotBtn.setToggleGroup(foxGroup);
        foxHumanBtn.setSelected(true);

        HBox foxButtons = new HBox(12, foxHumanBtn, foxBotBtn);
        foxButtons.setAlignment(Pos.CENTER);

        // Visibilité du champ nom fox selon sélection
        foxHumanBtn.selectedProperty().addListener((obs, old, selected) -> {
            player1NameField.setVisible(selected);
            player1NameField.setManaged(selected);
        });

        VBox foxCard = new VBox(10, foxImg, foxTitle2, foxButtons, player1NameField);
        foxCard.setAlignment(Pos.CENTER);
        foxCard.setPadding(new Insets(15));
        foxCard.setStyle(
                "-fx-background-color: rgba(66,71,105,0.7);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #F6B17A;" +
                        "-fx-border-width: 2;"
        );

        // --- Carte GEESE ---
        ImageView geeseImg = new ImageView(new Image("resources/Geese.png"));
        geeseImg.setFitWidth(72);
        geeseImg.setFitHeight(72);

        Label geeseTitle2 = new Label("GEESE");
        geeseTitle2.setTextFill(Color.web("#A7D7F9"));
        geeseTitle2.setFont(Font.font("Courier New", FontWeight.BOLD, 18));

        geeseGroup    = new ToggleGroup();
        geeseHumanBtn = createCuteToggle("🧑");
        geeseBotBtn   = createCuteToggle("🤖");
        geeseHumanBtn.setToggleGroup(geeseGroup);
        geeseBotBtn.setToggleGroup(geeseGroup);
        geeseHumanBtn.setSelected(true);

        HBox geeseButtons = new HBox(12, geeseHumanBtn, geeseBotBtn);
        geeseButtons.setAlignment(Pos.CENTER);

        // Visibilité du champ nom geese selon sélection
        geeseHumanBtn.selectedProperty().addListener((obs, old, selected) -> {
            player2NameField.setVisible(selected);
            player2NameField.setManaged(selected);
        });

        VBox geeseCard = new VBox(10, geeseImg, geeseTitle2, geeseButtons, player2NameField);
        geeseCard.setAlignment(Pos.CENTER);
        geeseCard.setPadding(new Insets(15));
        geeseCard.setStyle(
                "-fx-background-color: rgba(66,71,105,0.7);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #A7D7F9;" +
                        "-fx-border-width: 2;"
        );

        // --- CORRECTION 4 : ajouter foxCard et geeseCard dans un HBox, puis dans formCard ---
        HBox playersBox = new HBox(30, foxCard, geeseCard);
        playersBox.setAlignment(Pos.CENTER);

        formCard.getChildren().add(playersBox);

        // --- Bouton START ---
        newGameButton = new Button();
        newGameButton.setVisible(false);
        newGameButton.setManaged(false);

        StackPane imageBtn = createImageButton(
                "resources/title_icon.png",
                110,
                110,
                () -> newGameButton.fire()
        );

        Text startLabel = new Text("START GAME");
        startLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        startLabel.setFill(Color.WHITE);
        startLabel.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.8, 1, 1);");

        VBox btnBox = new VBox(8, imageBtn, startLabel);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setStyle("-fx-cursor: hand;");
        btnBox.setOnMouseClicked(e -> newGameButton.fire());

        Timeline btnFloat = new Timeline(
                new KeyFrame(Duration.ZERO,        e -> btnBox.setTranslateY(0)),
                new KeyFrame(Duration.seconds(1),   e -> btnBox.setTranslateY(-4)),
                new KeyFrame(Duration.seconds(2),   e -> btnBox.setTranslateY(0))
        );
        btnFloat.setCycleCount(Timeline.INDEFINITE);
        btnFloat.play();

        titlePane.getChildren().addAll(title, subtitle, sep, formCard, btnBox, newGameButton);
    }

    // --- CORRECTION 5 : suppression de updateFormVisibility, foxChoiceBox, humanCountSpinner ---
    // getHumanCount() se base directement sur les ToggleButtons
    public int getHumanCount() {
        int count = 0;
        if (foxHumanBtn.isSelected())   count++;
        if (geeseHumanBtn.isSelected()) count++;
        return count;
    }


    private ToggleButton createImageToggle(String imagePath) {

        ToggleButton btn = new ToggleButton();

        ImageView icon = new ImageView(
                new Image(imagePath)
        );

        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);

        btn.setGraphic(icon);

        btn.setPrefSize(70, 70);

        btn.setStyle(
                "-fx-background-color:#424769;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-radius:14;" +
                        "-fx-border-color:#F6B17A;" +
                        "-fx-border-width:2;"
        );

        btn.selectedProperty().addListener((obs, oldVal, selected) -> {

            if(selected) {

                btn.setStyle(
                        "-fx-background-color:#F6B17A;" +
                                "-fx-background-radius:14;" +
                                "-fx-border-radius:14;" +
                                "-fx-border-color:white;" +
                                "-fx-border-width:3;"
                );
            }
            else {

                btn.setStyle(
                        "-fx-background-color:#424769;" +
                                "-fx-background-radius:14;" +
                                "-fx-border-radius:14;" +
                                "-fx-border-color:#F6B17A;" +
                                "-fx-border-width:2;"
                );
            }
        });

        return btn;
    }

    // =============================================
    //  ÉCRAN DE JEU
    // =============================================
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
        currentPlayerLabel.setText((isFox ? "🦊 Fox" : "🪿 Geese") + " — " + name + "'s turn");
        currentPlayerLabel.setTextFill(isFox ? Color.web("#f5a623") : Color.web("#4a90d9"));
    }

    public void setMenuCallbacks(Runnable onQuit, Runnable onRestart) {
        this.onQuitGame    = onQuit;
        this.onRestartGame = onRestart;
        if (backToTitleButton != null && onQuit != null)
            backToTitleButton.setOnAction(e -> onQuit.run());
        if (restartButton != null && onRestart != null)
            restartButton.setOnAction(e -> onRestart.run());
    }

    // =============================================
    //  GETTERS
    // =============================================
    public Button  getNewGameButton()     { return newGameButton; }
    public Button  getBackToTitleButton() { return backToTitleButton; }
    public Button  getRestartButton()     { return restartButton; }

    public String getPlayer1Name() {
        String n = player1NameField.getText().trim();
        return n.isEmpty() ? "Player 1" : n;
    }

    public String getPlayer2Name() {
        String n = player2NameField.getText().trim();
        return n.isEmpty() ? "Player 2" : n;
    }

    public boolean isFoxHuman() {
        return foxHumanBtn.isSelected();
    }

    public boolean isGeeseHuman() {
        return geeseHumanBtn.isSelected();
    }
}