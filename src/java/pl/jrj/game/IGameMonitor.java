package pl.jrj.game;

import javax.ejb.Remote;

@Remote
public interface IGameMonitor {

    public boolean register(int hwork, String album); // hwork - numer zadania, album – numer albumu studenta

    public void initGame(int n, int k, long seed);

    public String verify(String state);
}
