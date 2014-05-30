public class Street{
	public final int startX;
	public final int startY;
	public final int endX;
	public final int endY;
	public final int cost;

	public Street(int cost, int startX, int startY, int endX, int endY){
		this.cost = cost;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
}