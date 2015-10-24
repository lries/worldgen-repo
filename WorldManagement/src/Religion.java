import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Religion {
	//Represents a specific deity's religion (possibly also binary religions e.g. Twin Gods?)
	//It's regional; when a religion splits the branches will be spread
	//Branches of the same religion have ptrs. 
	private Society primarySociety; //represents the primary society of this religious branch
	private List<String> domain;
	private String superdomain;
	private String type;
	private Monster deity;
	private Ethics ethics;
	private List<String> personalEthics;
	public Religion(){ }
	
	public static Religion generateNewReligion(Society primarySociety){
		Religion rel = new Religion();
		//Select a domain from the Society's domains.
		//A faith can have up to three connected domains. Later date: check if a domain is in use
		List<String> domain = new ArrayList<String>();
		Random r = new Random();
		int domainVal = r.nextInt(primarySociety.domains().keySet().size());
		int number = r.nextInt(3);
		for (int x=0; x<1+number; x++){
			List<String> possibilities = primarySociety.domains().get(primarySociety.domains().keySet().toArray()[domainVal]); 
			domain.add(possibilities.get(r.nextInt(possibilities.size())));
		}
		String superdomain = (String) primarySociety.domains().keySet().toArray()[domainVal];
		//These must be a known animal, and should be in an associated domain (possibly unless the religion is Shamanistic?)
		int rand = r.nextInt(5);
		List<AnimalSpecies> listOfSpecies = new ArrayList<AnimalSpecies>();
		for (AnimalSpecies species : primarySociety.knownAnimals().keySet()){
			for (String dom : domain){
				if (species.getBinaryTag("DOMAINS", ",").contains(dom)){
					listOfSpecies.add(species);
					break;
				}
			}
		}
		listOfSpecies.add(primarySociety.species());
		if (rand > 2) { 
			//choose a regular species
			rel.deity = MonsterGen.generateMonster(listOfSpecies, true, null);
		}
		else {
			//create a monster of the Society's species
			rel.deity = MonsterGen.generateMonster(listOfSpecies, true, primarySociety.species());	
		}
		//With domain and deity selection complete, up next will be structural design and ethics.
		//For right now, religions are probably going to copy their ethics; strutures aren't fully implemented yet.
		rel.ethics = primarySociety.ethics();
		rel.ethics.permuteEthics(10);
		return rel;
	}
}
