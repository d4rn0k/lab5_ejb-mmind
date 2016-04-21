
import pl.jrj.game.*;

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
    private String answer = "ABDE";

    private Random random;

    @Override
    public boolean register(int hwork, String album) {
        return (hwork == 5 && album.contains("98123"));
    }

    @Override
    public void initGame(int n, int k, long seed) {

        random = new Random(seed);

        if (debug) {
            return;
        }

        for (int i = 0; i < k; i++) {
            int randomPosition = random.nextInt(n);
            answer += letters.substring(randomPosition, randomPosition + 1);
        }
    }

    @Override
    public String verify(String state) {

        int positionHits = 0;
        int colorHits = 0;
        StringBuilder tempAnswer = new StringBuilder(answer);

        for (int i = 0; i < state.length(); i++) {
            if (tempAnswer.charAt(i) == state.charAt(i)) {
                positionHits++;
                tempAnswer.setCharAt(i, '-');
            }
        }

        for (int i = 0; i < state.length(); i++) {
            CharSequence asd = tempAnswer.subSequence(i, i + 1);
            if (state.contains(asd)) {
                colorHits++;
                tempAnswer.setCharAt(i, '.');
            }
        }

        return String.format("%d%d", positionHits, colorHits);
    }

}
