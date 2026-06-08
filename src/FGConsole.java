import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.view.View;
import control.FGController;
import javafx.application.Application;

import java.util.Scanner;

public class FGConsole {
    static final Scanner input = new Scanner(System.in);


    public static void main(String[] args) {
        Model model = new Model();
        String inputFile = null;

        if (args.length == 0) {
            inputFile = playerSelection(model);

        } else {
            int nbPlayers;
            try {
                nbPlayers = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                printUsage();
                return;
            }

            if (nbPlayers < 0 || nbPlayers > 2) {
                System.out.println("Error: the number of players must be 0, 1, or 2.");
                printUsage();
                return;
            }

            if (nbPlayers == 0) {
                // bot vs bot — pas de fichier attendu
                model.addComputerPlayer("Player1 (bot)");
                model.addComputerPlayer("Player2 (bot)");
                System.out.println("bot vs bot mod selected.");

            } else if (nbPlayers == 1) {
                // 1 human : args[1] optionnel = qui joue le fox (1 ou 2)
                int whoIsFox = 1; // par défaut l'human joue le fox
                if (args.length >= 2) {
                    try {
                        whoIsFox = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        // args[1] n'est pas un entier → on garde la valeur par défaut
                        System.out.println("Warning: value ignored for whoIsFox, the human plays fox.");
                    }
                }

                if (whoIsFox == 2) {
                    model.addComputerPlayer("Player1 (bot)");
                    model.addHumanPlayer("Player");
                    System.out.println("Bot = fox | human = geese.");
                } else {
                    model.addHumanPlayer("Player");
                    model.addComputerPlayer("Player2 (bot)");
                    System.out.println("Human = fox | Bot = geese.");
                }

            } else {
                // 2 players : args[1] optionnel = fichier de coups
                model.addHumanPlayer("Player1");
                model.addHumanPlayer("Player2");
                System.out.println("Human vs Human mod selected.");

                if (args.length >= 2) {
                    // Vérifie que ce n'est pas un nombre (mauvais argument)
                    if (!args[1].matches("\\d+")) {
                        inputFile = args[1];
                    } else {
                        System.out.println("Warning : argument \"" + args[1] + "\" ignored (expected: file name).");
                    }
                }
            }
        }

        StageFactory.registerModelAndView("Game", "model.FGStageModel", "view.FGStageView");

        View view = new View(model);
        FGController control = new FGController(model, view);
        control.setFirstStageName("Game");

        if (inputFile != null) {
            control.setInputFile(inputFile);
        }

        try {
            control.startGame();

            // Injecter le modèle et le controller dans MainApp AVANT de lancer JavaFX
            MainApp.setContext(model, control);

            // Lance JavaFX sur le thread principal (obligatoire)
            Application.launch(MainApp.class);

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }



    private static String playerSelection(Model model) {
        String inputFile = null;

        titlePrint();
        System.out.println("\n");
        System.out.println("Welcome in Geese & Fox !");
        System.out.print("Number of human player (0 / 1 / 2) : ");
        int nbPlayers;
        try {
            nbPlayers = Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
            nbPlayers = 2; // valeur par défaut
        }
        System.out.println();

        if (nbPlayers <= 0) {
            model.addComputerPlayer("Player1 (bot)");
            model.addComputerPlayer("Player2 (bot)");
            System.out.println("bot vs bot mod selected !");

        } else if (nbPlayers == 1) {
            System.out.print("Who play the fox ? (1 = human, 2 = bot) : ");
            int whoIsFox;
            try {
                whoIsFox = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                whoIsFox = 1;
            }

            System.out.print("\nYour name : ");
            String playerName = input.nextLine().trim();
            if (playerName.isEmpty()) playerName = "Player";
            System.out.println();

            if (whoIsFox == 2) {
                model.addComputerPlayer("Player1 (bot)");
                model.addHumanPlayer(playerName);
                System.out.println("Bot = fox | " + playerName + " = geese.");
            } else {
                model.addHumanPlayer(playerName);
                model.addComputerPlayer("Player2 (bot)");
                System.out.println(playerName + " = fox | Bot = geese.");
            }

        } else {
            System.out.print("Player 1 name (fox) : ");
            String p1 = input.nextLine().trim();
            if (p1.isEmpty()) p1 = "Player1";

            System.out.print("Player 2 name (geese) : ");
            String p2 = input.nextLine().trim();
            if (p2.isEmpty()) p2 = "Player2";

            model.addHumanPlayer(p1);
            model.addHumanPlayer(p2);
            System.out.println("human vs human mod selected !");

            System.out.print("\nInput file to play ? (Enter to ignore) : ");
            String file = input.nextLine().trim();
            if (!file.isEmpty()) {
                inputFile = file;
            }
        }

        return inputFile;
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage :");
        System.out.println("  java FGConsole                   → interactive menu");
        System.out.println("  java FGConsole 0                 → bot vs bot");
        System.out.println("  java FGConsole 1 [1|2]           → 1 human (1=fox, 2=geese)");
        System.out.println("  java FGConsole 2                 → human vs human");
        System.out.println("  java FGConsole 2 entree.txt      → human vs human + input file");
        System.out.println();
        System.out.println("input file format :");
        System.out.println("  Blank lines and lines starting with # are ignored.");
        System.out.println("  Fox input : 2 characters, e.g. C3");
        System.out.println("  Geese input : 4 characters, e.g. E2D2");
    }

    private static void titlePrint() {
        String yellow = "\u001B[33m"; // yellow
        String red = "\u001B[31m"; // red
        String reset = "\u001B[0m"; // reset

        System.out.println(
                yellow + "        /$$$$$$  /$$$$$$$$ /$$$$$$$$  /$$$$$$  /$$$$$$$$   " + reset + "    /$$   /$$ /$$  " + red + "     /$$$$$$$$ /$$$$$$  /$$   /$$\n" +
                yellow + "       /$$__  $$| $$_____/| $$_____/ /$$__  $$| $$_____/   " + reset + "   | $$$ | $$| $/  " + red + "    | $$_____//$$__  $$| $$  / $$\n" +
                yellow + "      | $$  \\__/| $$      | $$      | $$  \\__/| $$       " + reset + "     | $$$$| $$|_/ " + red + "      | $$     | $$  \\ $$|  $$/ $$/\n" +
                yellow + "      | $$ /$$$$| $$$$$   | $$$$$   |  $$$$$$ | $$$$$      " + reset + "   | $$ $$ $$      " + red + "    | $$$$$  | $$  | $$ \\  $$$$/ \n" +
                yellow + "      | $$|_  $$| $$__/   | $$__/    \\____  $$| $$__/     " + reset + "    | $$  $$$$     " + red + "     | $$__/  | $$  | $$  >$$  $$ \n" +
                yellow + "      | $$  \\ $$| $$      | $$       /$$  \\ $$| $$       " + reset + "     | $$\\  $$$   " + red + "       | $$     | $$  | $$ /$$/\\  $$\n" +
                yellow + "      |  $$$$$$/| $$$$$$$$| $$$$$$$$|  $$$$$$/| $$$$$$$$   " + reset + "   | $$ \\  $$     " + red + "     | $$     |  $$$$$$/| $$  \\ $$\n" +
                yellow + "       \\______/ |________/|________/ \\______/ |________/ " + reset + "     |__/  \\__/   " + red + "       |__/      \\______/ |__/  |__/\n");
        System.out.print(reset);  // reset
    }
}