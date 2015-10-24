import java.util.List;
import java.util.Map;

public class World {
	//Represents the world on a mass scale. Family of the FloorMap class.
	private float[][] hmap, tmap, smoothwind, precip;
	private int[][] binwind, wmasses, oceans, lands, continents, biomes;
	private float wlevel, mlevel;
	private Map<String, AnimalSpecies> species; private Map<String, BaseSociety> intels; private Map<Integer, List<AnimalSpecies>> selection;
	private List<AnimalSpecies>[][] animalMap;
	private Society[][] societies; 
	
	public World() {
	}
	
	public float[][] hmap() { return hmap; }
	public void hmap(float[][] hmap) { this.hmap = hmap; }
	public float[][] tmap() { return tmap; }
	public void tmap(float[][] tmap) { this.tmap = tmap; }
	public float[][] smoothwind() { return smoothwind; }
	public void smoothwind(float[][] smoothwind) { this.smoothwind = smoothwind; }
	public float[][] precip() { return precip; }
	public void precip(float[][] precip) { this.precip = precip; }
	public int[][] binwind() { return binwind; }
	public void binwind(int[][] binwind) { this.binwind = binwind; }
	public int[][] wmasses() { return wmasses;	}
	public void wmasses(int[][] wmasses) { this.wmasses = wmasses; }
	public int[][] oceans() { return oceans; }
	public void oceans(int[][] oceans) { this.oceans = oceans; }
	public int[][] lands() { return lands; }
	public void lands(int[][] lands) { this.lands = lands; }
	public int[][] continents() { return continents; }
	public void continents(int[][] continents) { this.continents = continents; }
	public int[][] biomes() { return biomes; }
	public void biomes(int[][] biomes) { this.biomes = biomes; }
	public Society[][] societies() { return societies; }
	public void societies(Society[][] societies) { this.societies = societies; }
	public float wlevel() { return wlevel; }
	public void wlevel(float wlevel) { this.wlevel = wlevel; }
	public float mlevel() { return mlevel; }
	public void mlevel(float mlevel) { this.mlevel = mlevel; }
	public Map<String, AnimalSpecies> species(){ return species; }
	public void species(Map<String, AnimalSpecies> species){ this.species = species; }
	public Map<String, BaseSociety> intels(){ return intels; }
	public void intels(Map<String, BaseSociety> intels){ this.intels = intels; }
	public void selection(Map<Integer, List<AnimalSpecies>> ret) { this.selection = ret; }
	public Map<Integer, List<AnimalSpecies>> selection(){ return this.selection; }
	public void animalMap(List<AnimalSpecies>[][] group) { this.animalMap = group; }
	public List<AnimalSpecies>[][] animalMap(){ return this.animalMap; }
	
}	

