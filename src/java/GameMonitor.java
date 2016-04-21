import pl.jrj.game.IGameMonitor;

import java.util.Random;
import javax.ejb.Remote;
import javax.ejb.Stateful;

/*
Implementacja beana JJ'a
 */
@Remote
@Stateful
public class GameMonitor implements IGameMonitor {

    boolean debug = true;
    private String letters = "ABCDEFGHIJKLMNOPQRSTUVW";
    private StringBuilder answer = new StringBuilder();

    private Random random;

    @Override
    public boolean register(int hwork, String album) {
        return (hwork == 5 && album.contains("98123"));
    }

    @Override
    public void initGame(int n, int k, long seed) {

        random = new Random(seed);

        for (int i = 0; i < k; i++) {
            int randomPosition = random.nextInt(n);
            answer.append(letters.substring(randomPosition,
                    randomPosition + 1));
        }
        System.out.format("WYNIK to: [%s]\n", answer);
    }

    @Override
    public String verify(String state) {

        int positionHits = 0;
        int colorHits = 0;

        boolean[] excludePositions = new boolean[state.length()];
        boolean[] excludeColors = new boolean[state.length()];

        for (int i = 0; i < state.length(); i++) {
            if (answer.charAt(i) == state.charAt(i)) {
                positionHits++;
                excludePositions[i] = true;
            }
        }

        for (int i = 0; i < state.length(); i++) {
            char asd = state.subSequence(i, i + 1).charAt(0);

            if (!excludePositions[i]) {

                int indexContains = contains(answer.toString().toCharArray(), asd, excludePositions, excludeColors);

                if (indexContains != -1) {
                    excludeColors[indexContains] = true;
                    colorHits++;
                }
            }
        }

        return String.format("%d%d", positionHits, colorHits);
    }

    private static int contains(char[] solution, int key, boolean[] counted, boolean[] counted2) {
        int position = -1;

        for (int index = 0; index < solution.length; index++) {
            if (solution[index] == key && !counted2[index] && !counted[index]) {
                position = index;
                return position;
            }
        }
        return position;
    }

}
