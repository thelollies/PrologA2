package tanks;
public class Street{
	private final City from;
	private final City to;
	public final int cost;

	public Street(int cost, City from, City to){
		this.cost = cost;
		this.from = from;
		this.to = to;
	}
	
	public int startX(){return from.x;}
	public int startY(){return from.y;}
	public int endX(){return to.x;}
	public int endY(){return to.y;}

}