public class Tank{
	public final boolean friendly;
	private City city;

	public Tank(boolean friendly){
		this.friendly = friendly;
	}

	public boolean friendly(){
		return friendly;
	}

	public City getCity(){
		return city;
	}

	public void setCity(City city){
		this.city = city;
	}
}