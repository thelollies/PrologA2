package tanks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jpl.Atom;
import jpl.Query;
import jpl.Term;
import jpl.Util;
import jpl.Variable;

public class GamePanelSimple extends Game implements MouseListener{

	private static final long serialVersionUID = 5128406007754605413L;

	// Circle width and offset (for cities)
	private static final int cW = 11;
	private static final int cOff = (cW - 1) / 2;

	private List<Street> streets;
	private List<City> cities;

	// Tank offset
	private static final int tXOff = -7;
	private static final int tYOff = -7;

	// Tank Outline
	private static final int[] tankOutX =
		{2,3,4,5,6,7,8,9,2,9,2,9,10,11,12,13,14,15,2,3,4,15,4,15,4,9,10,11,12,13,14,15,
		0,1,2,3,4,9,10,11,12,13,0,13,0,13,0,13,0,13,0,1,12,13,1,2,11,12,2,3,4,5,6,7,8,9,
		10,11};
	private static final int[] tankOutY =
		{0,0,0,0,0,0,0,0,1,1,2,2,2,2,2,2,2,2,3,3,3,3,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,
		6,6,6,7,7,8,8,9,9,10,10,11,11,11,11,12,12,12,12,13,13,13,13,13,13,13,13,13,13};

	// Tank Fill
	private static final int[] tankInX =
		{3,4,5,6,7,8,3,4,5,6,7,8,5,6,7,8,9,10,11,12,13,14,5,6,7,8,9,10,11,12,13,14,5,6,7,
		8,5,6,7,8,1,2,3,4,5,6,7,8,9,10,11,12,1,2,3,4,5,6,7,8,9,10,11,12,1,2,3,4,5,6,7,8,
		9,10,11,12,1,2,3,4,5,6,7,8,9,10,11,12,2,3,4,5,6,7,8,9,10,11,3,4,5,6,7,8,9,10};
	private static final int[] tankInY =
		{1,1,1,1,1,1,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,5,5,5,5,6,6,6,
		6,7,7,7,7,7,7,7,7,7,7,7,7,8,8,8,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,9,9,9,10,10,
		10,10,10,10,10,10,10,10,10,10,11,11,11,11,11,11,11,11,11,11,12,12,12,12,12,12,12,12};

	private Shape hoverShape;
	private final Color hoverFill = new Color(0, 140, 255, 155);
	private final Color hoverOutline = new Color(0, 13, 199, 155);

	private int energy = 100;

	public GamePanelSimple(List<City> cities, List<Street> streets) {
		super();
		this.cities = cities;
		this.streets = streets;
		addMouseListener(this);
		
		// Get initial list of tanks
		Query q = new Query("initialTankPositions", new Term[]{new Variable("TankList")});
		Hashtable<?, ?> result = q.oneSolution();
		loadTanksFromTerm((Term)result.get("TankList"));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// White background
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		// Draw Streets
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for(Street s : streets)
			g2d.drawLine(s.startX(), s.startY(), s.endX(), s.endY());

		// Draw cities and tanks
		g2d.setStroke(new BasicStroke(0.01f));
		for(City c : cities){
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(Color.darkGray);
			g2d.fillOval(c.x - cOff, c.y - cOff, cW, cW);

			Tank tank;
			if((tank = c.getTank()) != null){

				// Dont want to anti alias these drawings
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

				// Tank position centred
				int tX = c.x + tXOff;
				int tY = c.y + tYOff;

				// Draw tank outline
				g2d.setColor(Color.black);
				for(int i = 0; i < tankOutX.length; i++)
					g2d.drawLine(tX + tankOutX[i], tY + tankOutY[i], tX + tankOutX[i], tY + tankOutY[i]);

				// Tank fill colour
				if(tank.friendly()) g2d.setColor(Color.green);
				else g2d.setColor(Color.red);

				// Draw tank fill
				for(int i = 0; i < tankInX.length; i++)
					g2d.drawLine(tankInX[i] + tX, tankInY[i] + tY, tankInX[i] + tX, tankInY[i] + tY);
			}

		}

		if(hoverShape != null){
			g2d.setColor(hoverFill);
			g2d.fill(hoverShape);
			g2d.setColor(hoverOutline);
			g2d.draw(hoverShape);
		}
	}

	public void tankClicked(Tank tank){
		// This method calls: tanksKillable(TankLocation, TankList, Move, Killable) in tanks.pl

		// Query to find streets
		Term arg[] = 
			{new Atom(tank.getCity().name), 
				toTankList(), 
				new Variable("Move"), 
				new Variable("Killable")};
		Query    q = new Query("tanksKillable", arg);
		
		Hashtable<?,?> result = (Hashtable<?, ?>)q.oneSolution();
		Term move = (Term)result.get("Move");
		Term killable = (Term)result.get("Killable");

		StringBuilder moves = new StringBuilder("[");
		Term[] moveTerms = move.toTermArray();
		for(int i = 0; i < moveTerms.length; i++){
			String comma = i + 1 != moveTerms.length ? ", " : "";
			moves.append(String.format("%s%s", moveTerms[i].toString(), comma));
		}
		moves.append("]");
		System.out.printf("Killable: %s\nMove: %s\n", killable.toString(), moves.toString());

	}

	public Term toTankList(){
		StringBuilder tankString = new StringBuilder("[");

		List<Tank> tanks = tanks();
		for(int i = 0; i < tanks.size(); i++){
			Tank t = tanks.get(i);
			String team = t.friendly() ? "black" : "red";
			String city = t.getCity().name;
			String comma = i + 1 != tanks.size() ? ", " : ""; 
			tankString.append(String.format("tank(%s, %s)%s", team, city, comma));	
		}
		tankString.append("]");
		return Util.textToTerm(tankString.toString());
	}
	
	private void loadTanksFromTerm(Term tankTerm){
		Term[] tankArray = tankTerm.toTermArray();
		for(Term t : tankArray){
			Term[] arg = t.args();
			boolean friendly = arg[0].toString().equals("black");
			City c = nameToCity(arg[1].toString());
			c.setTank(new Tank(friendly));
		}
	}
	
	private City nameToCity(String name){
		for(City c : cities){
			if(c.name.equals(name)) return c;
		}
		return null;
	}

	private List<Tank> tanks(){
		List<Tank> tanks = new ArrayList<Tank>();
		for(City c : cities)
			if(c.getTank() != null) tanks.add(c.getTank());

		return tanks;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();

		// See if we've clicked a city or it's tank if it has one
		for(City c : cities){
			Tank tank;
			if((tank = c.getTank()) != null){
				int tX = c.x + tXOff;
				int tY = c.y + tYOff;

				// Check tank outline
				if(new Rectangle(tX, tY, 16, 14).contains(x,  y)){
					tankClicked(tank);
					break;
				}
			}
		}
	}

	@Override
	public Dimension getSize() {
		return new Dimension(500,500);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500,500);
	}

	public void setScoreChangeListener(GameChangeListener listener){
		this.gameChangeListener = listener;
	}

	@Override	public void mouseExited(MouseEvent arg0) {}
	@Override	public void mousePressed(MouseEvent arg0) {}
	@Override	public void mouseReleased(MouseEvent arg0) {}
	@Override	public void mouseEntered(MouseEvent arg0) {}

	public void setGameChangeListener(GameChangeListener listener){
		this.gameChangeListener = listener;
	}

	@Override
	public void endTurn() {
	}

	@Override
	public void startGame() {
	}

}