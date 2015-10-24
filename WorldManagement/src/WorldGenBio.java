import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorldGenBio {
	
	@SuppressWarnings("unchecked")
	public static void generateBiology(World w){
		List<AnimalSpecies>[][] animals = (ArrayList<AnimalSpecies>[][]) new ArrayList<?>[w.hmap().length][w.hmap()[0].length];
		System.out.println("Placing animals in biomes...");
		Map<Integer, List<AnimalSpecies>> animap = generateAnimalSelection(w);
		for (int x=0; x<animals.length; x++){
			for (int y=0; y<animals[0].length; y++){
				animals[x][y] = new ArrayList<AnimalSpecies>();
				animals[x][y].addAll(animap.get(w.biomes()[x][y]));
			}
		}
		w.animalMap(animals);
	}
	
	private static int[][] biomeArray = {{10, 7, 8, 6, 6},
										{10, 3, 4, 2, 2},
										{10, 0, 1, 1, 2},
										{10, 0, 14, 14, 15},
										{13, 12, 12, 12, 11}};
								
	
	public static List<Integer> getBiomes(AnimalSpecies c){
		List<Integer> ret = new ArrayList<Integer>();
		List<Integer> bx = new ArrayList<Integer>();
		List<Integer> by = new ArrayList<Integer>();
		for (String biome : c.binaryTags.get("BIOMES").split(",")){
			System.out.println(biome);
			if (biome.equals("any_temp")){ 
				by.add(0); by.add(1); by.add(2); by.add(3); by.add(4);
			}
			else if (biome.equals("hot")){ by.add(4); }
			else if (biome.equals("warm")){ by.add(3); }
			else if (biome.equals("temperate")){ by.add(2); }
			else if (biome.equals("cold")){ by.add(1); }
			else if (biome.equals("frigid")){ by.add(0); }
			
			else if (biome.equals("any_humidity")){
				bx.add(0); bx.add(1); bx.add(2); bx.add(3);
			}
			else if (biome.equals("dry")){ bx.add(0); }
			else if (biome.equals("semidry")){ bx.add(1); }
			else if (biome.equals("semiwarm")){ bx.add(2); }
			else if (biome.equals("warm")){ bx.add(3); }
			else if (biome.equals("alpine")){ bx.add(4); }
			else if (biome.equals("ocean")){ bx.add(5); }
		}
		List<Coordinate> co = new ArrayList<Coordinate>();
		for (int x : bx){
			for (int y : by){
				ret.add(biomeArray[x][y]);
			}
		}
		return ret; 
	}	
	
	private static Map<Integer, List<AnimalSpecies>> generateAnimalSelection(World w){
		//Maps animals to their Biome numbers. Not doing it in the files makes it easier to continue changing biomes V:
		//Map instead of Array again because this allows the cretaion of additional biomes. :V also FUCK ARRAYS seriously ugh
		//  	frigid	cold	temp.	warm	hot
		//dry	10		7		8		6		6
		//sdry	10		3		4		2		2
		//swet	10		0		1		1		2
		//wet	10		0		14		14		15
		//alt	9		9		9		9		9
		//ocean	13		12		12		12		11
		//Initialize map and lists
		Map<Integer, List<AnimalSpecies>> ret = new HashMap<Integer, List<AnimalSpecies>>();
		for (int x=0; x<16; x++){
			ret.put(x, new ArrayList<AnimalSpecies>());
		}
		List<Integer> bx = new ArrayList<Integer>(); //precip: 0 for dry, 1 for semidry, etc., 4 for alpine, 5 for ocean
		List<Integer> by = new ArrayList<Integer>(); //temp: 0 for frigid, 1 for cold, etc. 
		AnimalSpecies c = null;
		//Go through species
		for (String s : w.species().keySet()){
			c = w.species().get(s);
			//if (c.complexTags.get("SOCIAL_STRUCTURE").containsKey("SOCIETY")) continue;
			
			//For any non-Society species, figure out what biomes they're allowed in
			for (String biome : c.binaryTags.get("BIOMES").split(",")){
				System.out.println(biome);
				if (biome.equals("any_temp")){ 
					by.add(0); by.add(1); by.add(2); by.add(3); by.add(4);
				}
				else if (biome.equals("hot")){ by.add(4); }
				else if (biome.equals("warm")){ by.add(3); }
				else if (biome.equals("temperate")){ by.add(2); }
				else if (biome.equals("cold")){ by.add(1); }
				else if (biome.equals("frigid")){ by.add(0); }
				
				else if (biome.equals("any_humidity")){
					bx.add(0); bx.add(1); bx.add(2); bx.add(3);
				}
				else if (biome.equals("dry")){ bx.add(0); }
				else if (biome.equals("semidry")){ bx.add(1); }
				else if (biome.equals("semiwarm")){ bx.add(2); }
				else if (biome.equals("warm")){ bx.add(3); }
				else if (biome.equals("alpine")){ bx.add(4); }
				else if (biome.equals("ocean")){ bx.add(5); }
			}
			List<Coordinate> co = new ArrayList<Coordinate>();
			for (int x : bx){
				for (int y : by){
					boolean cont = false;
					for (Coordinate cord : co) {
						if (biomeArray[cord.x][cord.y] == biomeArray[x][y]) cont = true;
					}
					if (cont) continue;
					co.add(new Coordinate(x,y));
					if (c.complexTags.get("SOCIAL_STRUCTURE").containsKey("SOCIETY")) ;
					else ret.get(biomeArray[x][y]).add(w.species().get(s));
				}
			}
			bx = new ArrayList<Integer>();
			by = new ArrayList<Integer>();
		}
		w.selection(ret);
		return ret;
		
	}
	
	public static void printMap(Map<Integer, List<AnimalSpecies>> map){
		for (Integer key : map.keySet()){
			System.out.println(key+": "+map.get(key).toString());
		}
	}
	
	public static void addAnimalsToMap(World w){
		@SuppressWarnings("unchecked")
		List<AnimalSpecies>[][] group = (ArrayList<AnimalSpecies>[][])new ArrayList[w.hmap().length][w.hmap()[0].length];
		for (int x=0; x<group.length; x++){
			for (int y=0; y<group[0].length; y++){
				int currentBiome = w.biomes()[x][y];
				group[x][y] = w.selection().get(currentBiome);
			}
		}
		w.animalMap(group);
	}
	
	public static void main(String[] args){
		World w = new World();
		WorldGenTopo.genWorld(w, 252, 252, 10, true, 7, 0.5);
		WorldGenFiles.generateAllBases(w);
		generateBiology(w);
		WorldGenSociety.generateSocieties(w, 0);
		printMap(generateAnimalSelection(w));
		
	}
}
