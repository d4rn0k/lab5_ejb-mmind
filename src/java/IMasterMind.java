import javax.ejb.Remote;

/**
 * @author KK
 */
@Remote
public interface IMasterMind {

    /**
     * Initialize method for MasterMind game.
     *
     * @param colorsCount Number of colors
     * @param pegsCount   Number of pegs
     * @param seed        Seed for random generator
     */
    void initialize(int colorsCount, int pegsCount, long seed);

    /**
     * Main method to play in MasterMind game.
     *
     * @return moves count.
     */
    int play();

}
