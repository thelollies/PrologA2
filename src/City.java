
public class City{
	public final int x;
	public final int y;
	private Tank tank = null;

	public City(int x, int y){
		this.x = x;
		this.y = y;
	}

	public Tank getTank(){
		return this.tank;
	}

	public void setTank(Tank tank){
		this.tank = tank;
		tank.setCity(this);
	}

	public void removeTank(){
		tank.setCity(null);
		this.tank = null;
	}
}