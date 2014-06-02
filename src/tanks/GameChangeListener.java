package tanks;
public interface GameChangeListener{
	public void energyChanged(int newScore);
	public void win(boolean playerWin);
}