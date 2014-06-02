package tanks;

public class City{
	public final int x;
	public final int y;
	public final String name;
	private Tank tank = null;

	public City(String name, int x, int y){
		this.name = name;
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
		if(tank != null) tank.setCity(null);
		this.tank = null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof City){
			City other = (City) obj;
			return other.name.equals(name);
		}
		return false;
	}
}