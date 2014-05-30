import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import GamePanel.EnergyChangeListener;


public class Main extends JFrame implements ActionListener, EnergyChangeListener{

	private static final long serialVersionUID = -5412761068511534848L;
	private GamePanel game;

	private JTextPane energyPane;

	public Main(){

		// Initialise cities
		City city1 = new City(50, 40);
		City city2 = new City(100, 80);
		City city3 = new City(200, 20);

		// Initialise tanks
		city1.setTank(new Tank(true));
		city2.setTank(new Tank(false));

		City[] cities = new City[]{city1, city2, city3};

		Street[] streets =
				new Street[]{new Street(20, 50, 40, 100, 80),
				new	Street(40, 100, 80, 200, 20),
				new Street(30, 50, 40, 200, 20)};


		setLayout(new BorderLayout());

		game = new GamePanel(cities, streets);
		add(game, BorderLayout.CENTER);

		JButton endTurn = new JButton("End Turn");
		endTurn.addActionListener(this);

		energyPane = new JTextPane();
		energyPane.setText("Energy: ");

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(endTurn, BorderLayout.WEST);
		bottomPanel.add(energyPane, BorderLayout.EAST);

		add(bottomPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	public static void main(String[] args){
		new Main();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Button press");
	}

	@Override
	public void energyChanged(int newScore){
		this.energyPane.setText("Energy: " + newScore);
	}
}
