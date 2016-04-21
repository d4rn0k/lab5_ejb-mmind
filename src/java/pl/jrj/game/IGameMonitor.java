package pl.jrj.game;

import javax.ejb.Remote;

/**
 * EJB GameMonitor interface.
 *
 * @author JJ
 * @version 1.0
 */
@Remote
public interface IGameMonitor {

    /**
     * Register method.
     *
     * @param hwork numer zadania
     * @param album album studenta
     * @return
     */
    public boolean register(int hwork, String album);

    /**
     * Initialize method.
     *
     * @param n all colors number
     * @param k all pegs number
     * @param seed random seed
     */
    public void initGame(int n, int k, long seed);

    /**
     * Returns number of positions hits and good colors in wrong positions hits.
     *
     * @param state state to verify
     * @return
     */
    public String verify(String state);
}
