import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MonsterGen {
	//handles generation of Legendary Monsters, gods, etc.
	//first, it's named and its patron animal is chosen
	//second, the shape of the deity is chosen: whether it's "legless", "beast-headed", "humanoid", "quadrepdal", or "many-legged"
	//It's then selected if it should be "normal", "large", or "gargantuan"; normal may be forbidden when generating
	//Monstrous traits are then selected - for instance, abnormal coloration, spined back, etc. The mutations are seperated into categoies:
	////Body mods: 0-2 are selected for legless-humanoid, 0-3 for other. "Extra monstrous" monsters with extra may be generated. Ex. extra head(s), strange wings, external bones, bony potrusions, thick scales, etc.
	////Skin mods: 1-2, including thick fur, scales, and unusual coloration; rarely, skinless characters may be generated, or characters made of stone, metal, gems, or spectral characters.  
	////Combat mods: 0-1, including firebreathing, webspinning, etc
	////"Chimera" mods: Only generated for non-beast-headed, and only rarely. Random traits from another domain-appropriate animal are added on.


	private static final String[] colorDesc = {"shiny","dull","dun","#color-freckled","#color-speckled","efferescent","pale","deep","dark","vibrant"};
	private static final String[] colors = {"red","blue","yellow","green","indigo","gold","silver","black","gray","violet","maroon","white","pink","ivory","brown","mottled","swamp","chocolate"};
	private static final char[] vowels = {'a','e','i','o','u'};
	private static final char[] consonants = {'b','c','d','f','g','h','j','k','l','m','n','p','q','r','s','t','v','w','x','y','z'};

	public static Monster generateMonster(List<AnimalSpecies> set, boolean allowNormalSize, AnimalSpecies setPatron){
		Random r = new Random();
		//Name and kind
		String name = generateName();
		AnimalSpecies patron = setPatron;
		if (setPatron == null) patron = set.get(r.nextInt(set.size()));
		AnimalSpecies subpatron = null;

		//Select type
		String type="";
		switch(r.nextInt(10)){
		case 0: 
		case 1:
		case 2:
		case 3: type = "quadrepedal"; break;
		case 4:
		case 5:
		case 6: type = "humanoid"; break;
		case 7: type = "legless"; break;
		case 8: type = "many-limbed"; break;
		case 9:
		case 10: type = "beast-headed"; break;
		}
		String size;
		if (r.nextInt(4)<2) size = "massive";
		else size = "truly huge";
		if (r.nextInt(4)==0 && allowNormalSize) size = "regular-sized";

		//Traits
		String description = name + " is a " + type + size + patron.getName()+".";
		Set<String> bodyMods = new HashSet<String>(); //holds body modifications.
		Set<String> skinMods = new HashSet<String>(); //holds skin modifications.
		Set<String> combatMods = new HashSet<String>(); //holds combat modifications.
		Set<String> chimeraMods = new HashSet<String>(); //holds chimeric modifications.
		int bModCount = r.nextInt(3);
		if (type.equals("many-limbed") || type.equals("quadrepedal")) bModCount = r.nextInt(4);
		int sModCount = r.nextInt(2)+1;
		int combatModNum = r.nextInt(10);
		int chimeraModNum = r.nextInt(10);

		for (int x=0; x<bModCount; x++){
			selectBodyMod(bodyMods);
		}
		for (int x=0; x<sModCount; x++){
			selectSkinMod(skinMods);
		}
		setCombatMods(combatModNum, combatMods);
		subpatron = setChimeraMods(type, chimeraModNum, chimeraMods, set, patron);

		skinMods.remove("colormod");
		
		System.out.println(name + " is a "+type+" "+size+" "+patron+". They have the following unusual traits:");
		System.out.println();
		System.out.print("MAJOR MUTATIONS: ");
		for (String bodyMod : bodyMods) { System.out.print(bodyMod+" "); }
		System.out.println();
		System.out.print("DERMAL MUTATIONS: ");
		for (String skinMod : skinMods) { System.out.print(skinMod+" "); }
		System.out.println();
		System.out.print("COMBAT MUTATIONS: ");
		for (String combatMod : combatMods) { System.out.print(combatMod+" "); }
		System.out.println();
		System.out.print("CHIMERA MUTATIONS: ");
		for (String chimeraMod : chimeraMods) { System.out.print(chimeraMod+" "); }
		System.out.println();
		System.out.println();
		return new Monster(name, type, size, patron, subpatron, bodyMods, skinMods, combatMods, chimeraMods);
	}

	private static AnimalSpecies setChimeraMods(String type, int chimeraModNum, Set<String> chimeraMods, List<AnimalSpecies> elligibleSpecies, AnimalSpecies patron) {
		if (chimeraModNum > 2 || elligibleSpecies.size() == 1) return null;
		//find all parts
		Random r = new Random();
		AnimalSpecies mut = patron;
		while (mut == patron){
			mut = elligibleSpecies.get(r.nextInt(elligibleSpecies.size()));
		}
		Map<String,List<String>> partMap = mut.getComplexTag("STRUCTURE", ",");
		List<String> elligibleParts = new ArrayList<String>();
		//find elligible (e.g. nonzero) parts
		for (String key : partMap.keySet()){
			Integer.parseInt(partMap.get(key).get(0));
			if (Integer.parseInt(partMap.get(key).get(0)) > 0 && (!type.equals("legless") || !key.equals("LEGS")) && (!type.equals("quadrepedal") || !key.equals("ARMS")) && (!type.equals("beast-headed") || !key.equals("HEAD")) ){
				elligibleParts.add(key);
			}
		}

		if (elligibleParts.size() == 0) return null;

		if (chimeraModNum > 0 || elligibleParts.size() < 2){ 
			//light mutation: selects only one body part (or multiple if symmetric)
			//select part
			String part = elligibleParts.get(r.nextInt(elligibleParts.size()));
			chimeraMods.add(mut.getName() + " " + part);
			return mut;

		}
		else { 
			//heavy mutation: selects two body parts
			//select parts
			while (chimeraMods.size() < 2){
				String part = elligibleParts.get(r.nextInt(elligibleParts.size()));
				if (!chimeraMods.contains(part)) chimeraMods.add(mut.getName() + " " + part);}
			return mut;
		}
	}

	private static void setCombatMods(int combatModNum, Set<String> combatMods) {
		//Combat mods provide a bonus in a fight, in the case where that may occur. 
		//They are as follows:
		//fire-breathing (4)
		//poisonous gas (8)
		//caustic mist (9)
		//"contains a great gale" (7) 
		//boiling blood (5 or 6)
		//poisonous blood (5 or 6)
		//caustic blood (5 or 6)
		//bleeds fog (5 or 6)
		//crackles with lightning (3)

		if (combatModNum < 3) { return; }
		if (combatModNum == 3) { combatMods.add("lightning"); return; }
		if (combatModNum == 4) { combatMods.add("fire-breathing"); return; }
		if (combatModNum < 7) { 
			Random r = new Random();
			int rand = r.nextInt(4);
			if (rand == 0) { combatMods.add("boiling blood"); return; }
			if (rand == 1) { combatMods.add("poisonous blood"); return; }
			if (rand == 2) { combatMods.add("fog blood"); return; }
			combatMods.add("caustic blood"); return;
		}
		if (combatModNum == 7) { combatMods.add("gale"); return; }
		if (combatModNum == 8) { combatMods.add("poisonous gas"); return; }
		combatMods.add("caustic mist"); return;		
	}

	private static void selectSkinMod(Set<String> skinMods) {
		//Skin mods
		//including thick fur, scales, and unusual coloration
		//rarely, skinless characters may be generated, or characters made of stone, metal, gems, or spectral characters.
		if (skinMods.contains("skinless")) return;
		Random r = new Random();
		if (r.nextInt(10) < 7 && !skinMods.contains("colormod")){
			//Color mods are the most common, although a creture can't have more than one. Most beasts do. 
			String baseColor = colors[r.nextInt(colors.length)];
			String descColor = colorDesc[r.nextInt(colorDesc.length)];
			skinMods.add("colormod");
			skinMods.add(descColor + " " + baseColor +  " color");
			return;
		}
		int rand = r.nextInt(10);
		if (rand == 0) { skinMods.clear(); skinMods.add("skinless"); }
		else if (rand == 1 && !skinMods.contains("stone-like")) { skinMods.add("crystalline"); }
		else if (rand == 2) { skinMods.add("translucent"); }
		else if (rand == 3 && !skinMods.contains("crystalline")) { skinMods.add("stone-like"); }
		else if (rand < 7) { skinMods.add("fur"); }
		else if (rand < 10) { skinMods.add("scales"); }
	}

	public static void selectBodyMod(Set<String> bodyMods){
		//Body generation :V:
		//Ex. extra head(s), strange wings, external bones, bony protrusions, thick scales, etc.
		Random r = new Random();
		int rand = r.nextInt(3);
		if (rand < 2){
			//Dermal-ish mods: horns, spikes, gas pouches, shells, and extra eyes
			switch (r.nextInt(5)){
			case 0: bodyMods.add("extra eyes"); break;
			case 1: bodyMods.add("grand horns"); break;
			case 2: bodyMods.add("huge spines"); break;
			case 3: bodyMods.add("gaseous pouch"); break;
			case 4: bodyMods.add("thick shell"); break;
			}
		}
		else {
			//Mutilative mods: additional body parts, mouths where they don't belong, massive manes (!!of eyes!!), body covered in stars, and permanently glowing with ghosts that hop from eye to eye
			switch (r.nextInt(5)){
			case 0: 
				switch (r.nextInt(3)){
				case 0: bodyMods.add("hungering tail"); break;
				case 1: bodyMods.add((r.nextInt(1)+1) + " extra heads"); break;
				case 2: bodyMods.add("massive prehensile tongue"); break;
				}
			case 1: bodyMods.add("torso mouth"); break;
			case 2:
				switch (r.nextInt(3)){
				case 0: bodyMods.add("flaming mane"); break;
				case 1: bodyMods.add("mane of eyes"); break;
				case 2: bodyMods.add("umbral mane"); break;
				}
				break;
			case 3: bodyMods.add("astral form"); break;
			case 4: bodyMods.add("haunted"); break;
			}
		}
	}

	public static String generateName(){
		//Name generation. I'll probably look for a library to help later. Later variants may allow a society language to be chosen.
		//It guarantees a 3-7 character name with at least one vowel. In any case I have to find a profanity checker. 
		String name = "";
		Random r = new Random();
		int length = r.nextInt(5);
		length += 3;
		name = ""+vowels[r.nextInt(5)]; 
		boolean lastVowel = true;
		if (r.nextInt(2) == 0){ //select if first letter is vowel or not
			name = ""+consonants[r.nextInt(consonants.length)];
			lastVowel = false;
		}
		for (int x=0; x<length-1; x++){
			if (lastVowel && r.nextInt(10) < 7){
				name += ""+consonants[r.nextInt(consonants.length)];
				lastVowel = false;
			}
			else if (lastVowel){
				name += ""+vowels[r.nextInt(5)]; 
				lastVowel = true;

			}
			else if (!lastVowel && r.nextInt(10) < 7){
				name += ""+vowels[r.nextInt(5)]; 
				lastVowel = true;
			}
			else {
				name += ""+consonants[r.nextInt(consonants.length)];
				lastVowel = false;	
			}
		}
		System.out.println(name);
		for (int x=0; x<name.length(); x++){
			for (char v : vowels){
				if (name.charAt(x) == v){
					name = (Character.toUpperCase(name.charAt(0)) + name.substring(1));
					return name;
				}
			}
		}
		return generateName();
	}

	public static void main(String[] args){
		File[] f = WorldGenFiles.getFileContents("animals\\");
		Map<String, AnimalSpecies> speciesMap = WorldGenFiles.createSpecies(f);
		List<AnimalSpecies> species = new ArrayList<AnimalSpecies>();
		for (String key : speciesMap.keySet()){
			species.add(speciesMap.get(key));
		}
		generateMonster(species, true, null);
	}

}
