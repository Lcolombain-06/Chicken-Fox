package control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Enregistre les coups joués dans un fichier texte rejouable par HoleConsole.
 * Usage : java HoleConsole 2 ma_partie.txt
 */
public class GameRecorder {

    private final BufferedWriter writer;

    public GameRecorder(String outputPath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPath));
    }

    public void recordMove(String move) {
        try {
            writer.write(move);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("[GameRecorder] Erreur écriture : " + e.getMessage());
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("[GameRecorder] Erreur fermeture : " + e.getMessage());
        }
    }
}
