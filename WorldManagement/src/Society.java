import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Society {
//Societies are built based on a BaseSociety for a specific World. 
//These are the societies players may meet in the real world. They share a pointer to their BaseSociety and that also determines acitons.
	private BaseSociety society;
	private World world;
	private Map<String, List<String>> domains;
	public Map<String, List<String>> domains(){ return domains; }
	private Map<AnimalSpecies, Integer> knownAnimals; 
	public Map<AnimalSpecies, Integer>knownAnimals(){ return knownAnimals; }
	private AnimalSpecies species;
	public AnimalSpecies species(){ return species; }
	private Ethics ethics;
	private String societyType;
	public Ethics ethics() { return this.ethics; }
	private List<Religion> religions;
	public Society(World w, BaseSociety base){
		society = base;
		world = w;
		societyType = base.getName();
		domains = base.getComplexTag("DOMAINS",",");
		species = w.species().get(base.getBinaryTag("SPECIES"));
		knownAnimals = new HashMap<AnimalSpecies, Integer>();
		
	}
	
	public static Society generateNewSociety(World w, BaseSociety base){
		Society s = new Society(w, base);
		//Generates the basic society, which has a species, among other things.
		//After this, its ethics are decided. These are based off the Species ethics, but permutedd.
		s.ethics = new Ethics(s.society.getComplexTag("ETHICS"));
		s.ethics.permuteEthics(10);
		
		//Place the initial settlement somewhere appropriate on the map
		int x=0;
		int y=0;
		Random r = new Random();
		for (int i=0; i<10000; i++){
			x = r.nextInt(w.biomes().length);
			y = r.nextInt(w.biomes()[0].length);
			if (WorldGenBio.getBiomes(s.species()).contains(w.biomes()[x][y]) && w.societies()[x][y]==null){
				w.societies()[x][y] = s;
				for (AnimalSpecies anim : w.animalMap()[x][y]){
					s.knownAnimals().put(anim, 1);
				}
				break;
			}
		}
		if (s.knownAnimals().size() == 0) {
			 s = null;
		}
		
		//Generate religions. :V
		s.religions = new ArrayList<Religion>();
		for (int d=new Random().nextInt(4); d<10; d++){
			s.religions().add(Religion.generateNewReligion(s));
		}
		
		return s;
	}

	private List<Religion> religions() {
		return religions;
	}
	
}
