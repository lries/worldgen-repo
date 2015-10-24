import java.util.Set;

public class Monster {
	private String name;
	private String type;
	private String size;
	private AnimalSpecies patron;
	private AnimalSpecies subpatron;
	private Set<String> bodyMods;
	private Set<String> skinMods;
	private Set<String> combatMods;
	private Set<String> chimeraMods;

	//A monster is a very powerful creature who doesn't come directly from a single Animal Species or Society. All Religions have a Monster as God.
	//Basically, it's a creture that's an instance of itself :P 
	//A monster has all the traits of an animal or sentinent, plus all the traits it needs behaviorally. I'll probably have to do Tag stuff later but w/t/f/e.
	
	public Monster(String name, String type, String size, AnimalSpecies patron, AnimalSpecies subpatron,
			Set<String> bodyMods, Set<String> skinMods, Set<String> combatMods, Set<String> chimeraMods) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.patron = patron;
		this.subpatron = subpatron;
		this.bodyMods = bodyMods;
		this.skinMods = skinMods;
		this.combatMods = combatMods;
		this.chimeraMods = chimeraMods;
	}
}
