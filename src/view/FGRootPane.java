package view;

import boardifier.view.GameStageView;
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

    // --- Title screen ---
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
    private StackPane boardContainer;

    // --- Game screen ---
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
        // group sera attaché à boardContainer dans init()
    }

    // Appelé par boardifier à chaque startGame() — on récupère la main après
    @Override
    public void init(GameStageView gameStageView) {
        super.init(gameStageView);
        // super.init() a fait getChildren().clear() + add(group)
        // On remet notre hiérarchie et on accroche group au bon endroit
        getChildren().setAll(titlePane, gamePane);
        boardContainer.getChildren().setAll(group);
    }

    public void initPanes() {
        createTitlePane();
        createGamePane();

        // Les deux écrans remplissent toujours toute la fenêtre
        titlePane.prefWidthProperty().bind(widthProperty());
        titlePane.prefHeightProperty().bind(heightProperty());
        gamePane.prefWidthProperty().bind(widthProperty());
        gamePane.prefHeightProperty().bind(heightProperty());

        getChildren().setAll(titlePane, gamePane);
        showTitleScreen();
    }

    // =============================================
    //  IMAGE BUTTON WITH HOVER EFFECTS
    // =============================================
    private StackPane createImageButton(String imagePath, int width, int height, Runnable onClick) {
        StackPane wrapper = new StackPane();
        try {
            ImageView imageView = new ImageView(new Image(imagePath));
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            wrapper.setOnMouseEntered(e  -> { imageView.setScaleX(1.08); imageView.setScaleY(1.08); });
            wrapper.setOnMouseExited(e   -> { imageView.setScaleX(1.0);  imageView.setScaleY(1.0);  });
            wrapper.setOnMousePressed(e  -> { imageView.setScaleX(0.95); imageView.setScaleY(0.95); });
            wrapper.setOnMouseReleased(e -> {
                imageView.setScaleX(1.08); imageView.setScaleY(1.08);
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
    //  STYLED TOGGLE BUTTON
    // =============================================
    private ToggleButton createCuteToggle(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setPrefSize(90, 60);
        btn.setStyle(
                "-fx-background-color:#424769;-fx-text-fill:white;-fx-font-size:22px;" +
                        "-fx-background-radius:12;-fx-border-radius:12;-fx-border-color:#F6B17A;-fx-border-width:2;"
        );
        btn.selectedProperty().addListener((obs, oldVal, selected) -> btn.setStyle(selected
                ? "-fx-background-color:#F6B17A;-fx-text-fill:black;-fx-font-size:22px;" +
                "-fx-background-radius:12;-fx-border-radius:12;-fx-border-color:white;-fx-border-width:3;"
                : "-fx-background-color:#424769;-fx-text-fill:white;-fx-font-size:22px;" +
                "-fx-background-radius:12;-fx-border-radius:12;-fx-border-color:#F6B17A;-fx-border-width:2;"
        ));
        return btn;
    }

    // =============================================
    //  IMAGE TOGGLE BUTTON
    // =============================================
    private ToggleButton createImageToggle(String imagePath) {
        ToggleButton btn = new ToggleButton();
        ImageView icon = new ImageView(new Image(imagePath));
        icon.setFitWidth(40); icon.setFitHeight(40); icon.setPreserveRatio(true);
        btn.setGraphic(icon);
        btn.setPrefSize(70, 70);
        btn.setStyle(
                "-fx-background-color:#424769;-fx-background-radius:14;" +
                        "-fx-border-radius:14;-fx-border-color:#F6B17A;-fx-border-width:2;"
        );
        btn.selectedProperty().addListener((obs, oldVal, selected) -> btn.setStyle(selected
                ? "-fx-background-color:#F6B17A;-fx-background-radius:14;" +
                "-fx-border-radius:14;-fx-border-color:white;-fx-border-width:3;"
                : "-fx-background-color:#424769;-fx-background-radius:14;" +
                "-fx-border-radius:14;-fx-border-color:#F6B17A;-fx-border-width:2;"
        ));
        return btn;
    }

    // =============================================
    //  TITLE SCREEN
    // =============================================
    private void createTitlePane() {
        titlePane = new VBox(20);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setPadding(new Insets(40));

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
        title.setStyle("-fx-effect: dropshadow(gaussian, black, 8, 0.6, 2, 2);");

        Timeline floatAnim = new Timeline(
                new KeyFrame(Duration.ZERO,         e -> title.setTranslateY(0)),
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

        VBox formCard = new VBox(15);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(520);
        formCard.setPadding(new Insets(25));
        formCard.setStyle(
                "-fx-background-color: rgba(45,50,80,0.5);-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;-fx-border-color: rgba(6,87,94,0.35);-fx-border-width: 3;"
        );
        DropShadow shadow = new DropShadow();
        shadow.setRadius(18); shadow.setOffsetY(5); shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        formCard.setEffect(shadow);

        String fieldStyle =
                "-fx-background-color:#FFF8E8;-fx-text-fill:#2D3250;-fx-font-size:14px;" +
                        "-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#F6B17A;-fx-border-width:2;";

        player1NameField = new TextField("Player 1");
        player2NameField = new TextField("Player 2");
        player1NameField.setPrefWidth(200); player1NameField.setStyle(fieldStyle);
        player2NameField.setPrefWidth(200); player2NameField.setStyle(fieldStyle);

        // FOX card
        ImageView foxImg = new ImageView(new Image("resources/Fox.png"));
        foxImg.setFitWidth(72); foxImg.setFitHeight(72);
        Label foxTitle2 = new Label("FOX");
        foxTitle2.setTextFill(Color.web("#F6B17A"));
        foxTitle2.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        foxGroup    = new ToggleGroup();
        foxHumanBtn = createImageToggle("resources/title_icon.png");
        foxBotBtn   = createImageToggle("resources/bot.png");
        foxHumanBtn.setToggleGroup(foxGroup); foxBotBtn.setToggleGroup(foxGroup);
        foxHumanBtn.setSelected(true);
        HBox foxButtons = new HBox(12, foxHumanBtn, foxBotBtn);
        foxButtons.setAlignment(Pos.CENTER);
        foxHumanBtn.selectedProperty().addListener((obs, old, selected) -> {
            player1NameField.setVisible(selected); player1NameField.setManaged(selected);
        });
        VBox foxCard = new VBox(10, foxImg, foxTitle2, foxButtons, player1NameField);
        foxCard.setAlignment(Pos.CENTER); foxCard.setPadding(new Insets(15));
        foxCard.setStyle("-fx-background-color: rgba(66,71,105,0.7);-fx-background-radius: 14;" +
                "-fx-border-radius: 14;-fx-border-color: #F6B17A;-fx-border-width: 2;");

        // GEESE card
        ImageView geeseImg = new ImageView(new Image("resources/Geese.png"));
        geeseImg.setFitWidth(72); geeseImg.setFitHeight(72);
        Label geeseTitle2 = new Label("GEESE");
        geeseTitle2.setTextFill(Color.web("#A7D7F9"));
        geeseTitle2.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        geeseGroup    = new ToggleGroup();
        geeseHumanBtn = createCuteToggle("🧑");
        geeseBotBtn   = createCuteToggle("🤖");
        geeseHumanBtn.setToggleGroup(geeseGroup); geeseBotBtn.setToggleGroup(geeseGroup);
        geeseHumanBtn.setSelected(true);
        HBox geeseButtons = new HBox(12, geeseHumanBtn, geeseBotBtn);
        geeseButtons.setAlignment(Pos.CENTER);
        geeseHumanBtn.selectedProperty().addListener((obs, old, selected) -> {
            player2NameField.setVisible(selected); player2NameField.setManaged(selected);
        });
        VBox geeseCard = new VBox(10, geeseImg, geeseTitle2, geeseButtons, player2NameField);
        geeseCard.setAlignment(Pos.CENTER); geeseCard.setPadding(new Insets(15));
        geeseCard.setStyle("-fx-background-color: rgba(66,71,105,0.7);-fx-background-radius: 14;" +
                "-fx-border-radius: 14;-fx-border-color: #A7D7F9;-fx-border-width: 2;");

        HBox playersBox = new HBox(30, foxCard, geeseCard);
        playersBox.setAlignment(Pos.CENTER);
        formCard.getChildren().add(playersBox);

        // START button (invisible, déclenché par l'image)
        newGameButton = new Button();
        newGameButton.setVisible(false);
        newGameButton.setManaged(false);

        StackPane imageBtn = createImageButton("resources/title_icon.png", 110, 110,
                () -> newGameButton.fire());

        Text startLabel = new Text("START GAME");
        startLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        startLabel.setFill(Color.WHITE);
        startLabel.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.8, 1, 1);");

        VBox btnBox = new VBox(8, imageBtn, startLabel);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setStyle("-fx-cursor: hand;");
        btnBox.setOnMouseClicked(e -> newGameButton.fire());

        Timeline btnFloat = new Timeline(
                new KeyFrame(Duration.ZERO,       e -> btnBox.setTranslateY(0)),
                new KeyFrame(Duration.seconds(1), e -> btnBox.setTranslateY(-4)),
                new KeyFrame(Duration.seconds(2), e -> btnBox.setTranslateY(0))
        );
        btnFloat.setCycleCount(Timeline.INDEFINITE);
        btnFloat.play();

        titlePane.getChildren().addAll(title, subtitle, sep, formCard, btnBox, newGameButton);
    }

    // =============================================
    //  GAME SCREEN
    // =============================================
    private void createGamePane() {
        gamePane = new BorderPane();
        gamePane.setStyle("-fx-background-color: #1a1a2e;");

        // ── TOP BAR ──────────────────────────────────────────────────────────
        HBox menuBar = new HBox(15);
        menuBar.setPadding(new Insets(10, 15, 10, 15));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle(
                "-fx-background-color: #0f0f23;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 0 0 3 0;"
        );

        ImageView quitImg = new ImageView(new Image("resources/title_icon.png"));
        quitImg.setFitWidth(28); quitImg.setFitHeight(28);
        backToTitleButton = new Button();
        backToTitleButton.setGraphic(quitImg);
        backToTitleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        Label topTitle = new Label("FOX & GEESE");
        topTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        topTitle.setTextFill(Color.web("#e94560"));
        HBox.setHgrow(topTitle, Priority.ALWAYS);
        topTitle.setMaxWidth(Double.MAX_VALUE);
        topTitle.setAlignment(Pos.CENTER);

        ImageView restartImg = new ImageView(new Image("resources/title_icon.png"));
        restartImg.setFitWidth(28); restartImg.setFitHeight(28);
        restartButton = new Button();
        restartButton.setGraphic(restartImg);
        restartButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        menuBar.getChildren().addAll(backToTitleButton, topTitle, restartButton);
        gamePane.setTop(menuBar);

        // ── BOTTOM LABEL ─────────────────────────────────────────────────────
        currentPlayerLabel = new Label("Current player : -");
        currentPlayerLabel.setTextFill(Color.WHITE);
        currentPlayerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        currentPlayerLabel.setPadding(new Insets(8));
        currentPlayerLabel.setMaxWidth(Double.MAX_VALUE);
        currentPlayerLabel.setAlignment(Pos.CENTER);
        currentPlayerLabel.setStyle("-fx-background-color: #0f0f23;");
        gamePane.setBottom(currentPlayerLabel);

        // ── CENTER (board) ───────────────────────────────────────────────────
        boardContainer = new StackPane();
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setStyle("-fx-background-color: #1a1a2e;");
        gamePane.setCenter(boardContainer);
    }

    // =============================================
    //  NAVIGATION
    // =============================================
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

    // =============================================
    //  GETTERS
    // =============================================
    public Button     getNewGameButton()     { return newGameButton; }
    public Button     getBackToTitleButton() { return backToTitleButton; }
    public Button     getRestartButton()     { return restartButton; }
    public StackPane  getBoardContainer()    { return boardContainer; }

    public String  getPlayer1Name() { String n = player1NameField.getText().trim(); return n.isEmpty() ? "Player 1" : n; }
    public String  getPlayer2Name() { String n = player2NameField.getText().trim(); return n.isEmpty() ? "Player 2" : n; }
    public boolean isFoxHuman()     { return foxHumanBtn.isSelected(); }
    public boolean isGeeseHuman()   { return geeseHumanBtn.isSelected(); }
    public int getHumanCount() {
        int c = 0;
        if (foxHumanBtn.isSelected())   c++;
        if (geeseHumanBtn.isSelected()) c++;
        return c;
    }
}