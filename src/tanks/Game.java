package tanks;

import javax.swing.JPanel;

abstract class Game extends JPanel implements GameInterface{
	
	protected GameChangeListener gameChangeListener;
	public void setGameChangeListener(GameChangeListener listener){
		this.gameChangeListener = listener;
	}
	
}
