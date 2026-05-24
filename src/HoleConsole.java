import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.view.View;
import control.FGController;

import java.util.Scanner;

public class HoleConsole {
    static final Scanner input = new Scanner(System.in);


    public static void main(String[] args) {
        String player = "Player";
        String bot = "(bot)";

        // 1. Modèle global
        Model model = new Model();

        if (args.length >= 1 && args.length < 3) {
            int nbrplayer = Integer.parseInt(args[0]);
            int whoIsFox = 1;

            if (args.length == 2) {
                whoIsFox = Integer.parseInt(args[1]);
            }

            if (nbrplayer == 0) {
                model.addComputerPlayer(player + "1 " + bot);
                model.addComputerPlayer(player + "2 " + bot);
            }

            else if (nbrplayer == 1) {
                if (whoIsFox == 1 || whoIsFox > 2) {
                    model.addHumanPlayer(player);
                    model.addComputerPlayer(player + "2 " + bot);
                }

                else {
                    model.addComputerPlayer(player + "1 " + bot);
                    model.addHumanPlayer(player);
                }
            }

            else {
                model.addHumanPlayer(player);
                model.addHumanPlayer(player);
            }
        }

        else {
            playerSelection(model);
            System.out.println();
        }


        // 2. Enregistrement du stage
        StageFactory.registerModelAndView("hole", "model.FGStageModel", "view.FGStageView");

        // 3. Vue et contrôleur
        View view = new View(model);
        FGController control = new FGController(model, view);
        control.setFirstStageName("hole");

        // 4. Lancement du jeu
        try {
            control.startGame();   // initialise le stage + appelle la factory
            control.stageLoop();   // boucle de jeu
        } catch (Exception e) {
            System.out.println("Erreur lors du lancement du jeu : " + e.getMessage());
        }
    }



    private static void playerSelection(Model model) {
        String player = "Player";
        String bot = "(bot)";

        titlePrint();
        System.out.println("\n");
        System.out.println("Welcome in geese and fox!");
        System.out.print("Select the number of human player: ");
        int nbrplayer = input.nextInt();
        input.nextLine();
        System.out.println();

        if (nbrplayer == 0) {
            model.addComputerPlayer(player + "1 " + bot);
            model.addComputerPlayer(player + "2 " + bot);
            System.out.println("Bot-only mod selected!");
        }

        else if (nbrplayer == 1) {
            System.out.println("Bot-human mod selected!");
            System.out.print("Who will play the fox (1 = player; 2 = bot): ");
            int whoIsFox = input.nextInt();
            input.nextLine();

            System.out.print("\nPlayer, choose your name: ");
            player = input.nextLine();
            System.out.println();

            if (whoIsFox == 1 || whoIsFox > 2) {
                model.addHumanPlayer(player);
                model.addComputerPlayer("Player2 " + bot);
                System.out.println("Player will be the fox!");
            } else {
                model.addComputerPlayer("Player1 " + bot);
                model.addHumanPlayer(player);
                System.out.println("bot will be the fox!");
            }
        }

        else {
            System.out.print("\nPlayer1, choose your name: ");
            player = input.nextLine();
            System.out.println();
            model.addHumanPlayer(player);

            System.out.print("\nPlayer2, choose your name: ");
            player = input.nextLine();
            System.out.println();
            model.addHumanPlayer(player);
            System.out.println("Human VS Human mode selected!");
        }
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