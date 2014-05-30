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
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener{

	private static final long serialVersionUID = 5128406007754605413L;

	private EnergyChangeListener energyChangeListener;

	// Circle width and offset (for cities)
	private static final int cW = 11;
	private static final int cOff = (cW - 1) / 2;

	private Street[] streets;
	private City[] cities;

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

	private Tank selectedTank;

	private int energy = 100;

	public GamePanel(City[] cities, Street[] streets) {
		super();
		this.cities = cities;
		this.streets = streets;
		addMouseListener(this);
		addMouseMotionListener(this);
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
			g2d.drawLine(s.startX, s.startY, s.endX, s.endY);

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

				// Highlight selected tank
				if(selectedTank != null && selectedTank.equals(tank))
					g2d.setColor(g2d.getColor().darker().darker());

				// Draw tank fill
				for(int i = 0; i < tankInX.length; i++)
					g2d.drawLine(tankInX[i] + tX, tankInY[i] + tY, tankInX[i] + tX, tankInY[i] + tY);

				g2d.setColor(Color.cyan);
				g2d.drawLine(c.x, c.y, c.x, c.y);
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
		if(selectedTank == null){
			// Tank has been selected
			selectedTank = tank;
		}else{
			// Tank was already selected and we're clicking another one
			if(canAttack(selectedTank, tank)){
				attack(selectedTank, tank);
				repaint();
			}
		}
	}

	private void attack(Tank attacker, Tank victim) {
		victim.getCity().removeTank();
		energy -= 25;
		if(energyChangeListener != null) energyChangeListener.energyChanged(energy);
	}

	private boolean canAttack(Tank attacker, Tank victim) {
		return (attacker.friendly() != victim.friendly() && // Not on same team
				energy - 25 >= 0 && // Enough energy
				adjacent(attacker.getCity(), victim.getCity())); // Adjacent cities

	}

	private boolean adjacent(City city, City city2) {
		// TODO
		return true;
	}

	public void cityClicked(City city){
		System.out.println("You clicked: " + city);
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

			if(new Ellipse2D.Double(c.x - cOff, c.y - cOff, cW, cW).contains(x, y)){
				cityClicked(c);
				break;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();

		boolean hover = false;

		// See if we've clicked a city or it's tank if it has one
		for(City c : cities){
			if(c.getTank() != null){
				int tX = c.x + tXOff;
				int tY = c.y + tYOff;

				Rectangle rectangle;
				if((rectangle = new Rectangle(tX, tY, 16, 14)).contains(x,  y)){
					hover = true;
					if(hoverShape != null && hoverShape.equals(rectangle)){
						break;
					}
					hoverShape = rectangle;
					repaint();
					break;
				}
			}

			Ellipse2D.Double circle;
			if((circle = new Ellipse2D.Double(c.x - cOff, c.y - cOff, cW, cW)).contains(x, y)){
				hover = true;
				if(hoverShape != null && hoverShape.equals(circle)){
					break;
				}
				hoverShape = circle;
				repaint();
				break;
			}
		}
		if(!hover){
			Shape temp = hoverShape;
			hoverShape = null;
			if(temp != null) repaint();
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

	public void setScoreChangeListener(EnergyChangeListener listener){
		this.energyChangeListener = listener;
	}

	@Override	public void mouseExited(MouseEvent arg0) {}
	@Override	public void mousePressed(MouseEvent arg0) {}
	@Override	public void mouseReleased(MouseEvent arg0) {}
	@Override	public void mouseDragged(MouseEvent arg0) {}
	@Override	public void mouseEntered(MouseEvent arg0) {}


	public interface EnergyChangeListener{
		public void energyChanged(int newScore);
	}

}