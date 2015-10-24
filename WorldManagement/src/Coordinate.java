public class Coordinate{
	//int[2] is for NERDS :V
	public int x;
	public int y;
	public int dir;
	public Coordinate(int x, int y){
		this.x = x;
		this.y = y;
	}
	@Override
	public boolean equals(Object o){
		Coordinate c = (Coordinate) o;
		return this.x == c.x && this.y == c.y;
	}
}

