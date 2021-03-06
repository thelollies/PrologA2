package tanks;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import jpl.Atom;
import jpl.Query;
import jpl.Term;
import jpl.Variable;


public class Main extends JFrame implements GameChangeListener{

	private static final long serialVersionUID = -5412761068511534848L;

	private JTextPane energyPane;
	private Game game;

	public Main(boolean simple, String mapName){

		Map<String, City> cityMap = new HashMap<String, City>();
		List<Street> streets = new ArrayList<Street>();

		// Load tanks.pl
		new Query("[tanks]").hasSolution();
		new Query("["+mapName+"]").hasSolution();

		// Query to find streets
		Term arg[] = {new Variable("From"), new Variable("To"), new Variable("Cost")};
		Query    q = new Query("road", arg);

		while (q.hasMoreElements()){
			Hashtable<?,?> result = (Hashtable<?, ?>)q.nextElement();

			Term fromTerm = (Term)result.get("From");
			Term toTerm = (Term)result.get("To");
			Term costTerm = (Term)result.get("Cost");

			City from;
			City to;
			if((from = cityMap.get(fromTerm.toString())) == null){
				Point fromPos = getPosition(fromTerm.toString());
				from = new City(fromTerm.toString(), fromPos.x, fromPos.y);
				cityMap.put(fromTerm.toString(), from);
			}
			if((to = cityMap.get(toTerm.toString())) == null){
				Point toPos = getPosition(toTerm.toString());
				to = new City(toTerm.toString(), toPos.x, toPos.y);
				cityMap.put(toTerm.toString(), to);
			}

			streets.add(new Street(costTerm.intValue() ,from, to));
		}

		setLayout(new BorderLayout());

		if(simple)
			game = new GamePanelSimple(new ArrayList<City>(cityMap.values()), streets);
		else
			game = new GamePanel(new ArrayList<City>(cityMap.values()), streets);

		game.setGameChangeListener(this);
		add(game, BorderLayout.CENTER);

		JButton endTurn = new JButton("End Turn");
		endTurn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				game.endTurn();
			}
		});

		energyPane = new JTextPane();
		energyPane.setText("Energy: ");

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(endTurn, BorderLayout.WEST);
		bottomPanel.add(energyPane, BorderLayout.EAST);

		add(bottomPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);

		if(game instanceof GamePanel){
			int result = JOptionPane.showConfirmDialog(this, "Fix map before playing?");
			if(result == JOptionPane.YES_OPTION){
				GamePanel g = (GamePanel)game;
				if(!g.fixMap()){
					JOptionPane.showConfirmDialog(this, "Could not fix the map.");
				}
			}
		}
		game.startGame();
	}

	public static void main(String[] args){
		String mapName = args[0];
		boolean simpleMode = args.length > 1 && args[1].equalsIgnoreCase("-simple");
		new Main(simpleMode, mapName);
	}

	@Override
	public void energyChanged(int newScore){
		this.energyPane.setText("Energy: " + newScore);
	}

	public Point getPosition(String cityName){
		// Query to find streets
		Term arg[] = {new Atom(cityName), new Variable("X"), new Variable("Y")};
		Query    q = new Query("position", arg);

		Hashtable<?,?> result = (Hashtable<?, ?>)q.oneSolution();
		Term x = (Term)result.get("X");
		Term y = (Term)result.get("Y");

		return new Point(x.intValue(), y.intValue());
	}

	@Override
	public void win(boolean playerWin) {
		String team = playerWin ? "Player" : "Computer";
		JOptionPane.showMessageDialog(this, team + " wins!");
		System.exit(0);
	}
}
