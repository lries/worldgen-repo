import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Ethics {
/*Represents a society or religion's ethics
Ethics aren't just important for enforcing criminal law; they also heavily impact inter-society relationships
Ethical issues currently hardcoded are:
(0) MAKE_WAR_OWN; inciting, fighting one's own society, rebelling as a group, or making war with other kin societies with the same ethics,
(1) MAKE_WAR_ALLY: the same, but with allied societies;
(2) MAKE_WAR_NEUTRAL: the same, but with societies that are neutral;
(3) MAKE_WAR_ENEMY: the same, but with emenies; 
(4) MAKE_SLAVES: slavery; 
(5) KILL_OWN: killing own kind, OUTSIDE of a war;
(6) KILL_ALLY: killing allied kind, outside war;
(7) KILL_NEUTRAL: killing neutral intelligent life, outside war;
(8) KILL_ENEMY: killing enemy races, outside war;
(9) STEAL: steal;
(10) CANNIBALISM: eating other members of the same species;
(11) EAT_OTHER_INTELLIGENT: eating other society-forming creatures;
(12) EAT_MEAT: eating meat;
(13) LYING: lying, outside of oaths or law;
(14) OATHBREAKING: lying, within oaths;
(15) VANDALISM: duh;
(16) TORTURE_OWN: torturing own kind, outside of war (then default to torture_fun/torture_serious);
(17) TORTURE_ALLY: as above, but for allied kind;
(18) TORTURE_FUN_INTELLIGENT: torturing neutral, enemy, or war combatant for entertainment;
(19) TORTURE_SERIOUS_INTELLIGENT: for information;
(20) TORTURE_ANIMAL: torturing an animal;
(21) DEFY_GENDER_ROLE_F: defy societal gender roles. Egalitarian societies won't have this tag. 
(22) DEFY_GENDER_ROLE_M: as above
(23) DEFY_GENDER_ROLE_O: as above. 
(24) HOMOSEXUALITY: engaging in romantic or sexual behavior with same-gender individuals or with individuals seen as gender-abberative under another moral clause;
(25) TRANSGENDERISM: having a gender that doesn't match one's binary reproductive role; those without binary reproductive laws are exempt;
(26) REPRODUCE_OUTSIDE_RELATIONSHIP: children outside wedlock;
(27) QUESTION_AUTHORITY: rabble-rousing et all;]

Ethics are especially important. The ethics class holds a set of ethics, manages differences between ethics etc. 
As for the values they can hold, each holds two values: circumstance and punishment
Circumstance values are:
-normal (0); the society doesn't recognize this as something that's an ethical issue at all;
-religious_matter (1); the society regards this as a religious matter and a god is always spawned related to it;
-okay (2); the society regards this as an acceptable, but understands it as an ethical issue
-only_minor_circumstances (3); the society thinks this is bad but usually okay: for instance you shouldn't tell little white lies for no reason, but it's okay to tell them because you don't want to tell your buddy her ass looks fat in her dress
-only_serious_circumstances (4); the society thinks this is bad but okay under serious circumstances: stealing is bad, but if you need to feed your family it's okay
-only_self_defense (5); the society recognizes this as very bad, but okay to save your life: it's bad to murder, but in self defense it might be okay
-only_extreme_circumstances (6); the society recognizes this as horrible, but okay in a very extreme situation: if it's our village vs. their village, pick our village
-never (7); NO

Punishment values are:
-null (0); the value for any 0, 1, or maybe 2 situations
-disdain (1); other society members will get a small relationship hit
-shun (2); other society members will get a large relationship hit
-personal_matter (3); the affected society member will get a large relationship hit
-punish_minor (4); a minor punishment will be inflicted, depending on society punishment
-punish_serious (5); a major punishment
-punish_extreme (6); "..."
-punish_exile (7); the character is exiled. Exiled characters may attempt to join other societies
-punish_capital (8); the character is executed. 

What exactly consitutes minor, serious, and extreme punishment varies: a pretty standard society might have:
-small fee;
-imprisonment;
-reappropriation of all property and imprisonment;
-excommunication (if religious) and exile;
-execution;
Unlisted ethics are assumed normal,null. 
	 */

	private Map<String, int[]> baseValues;
	private Map<String, String> baseEthics; //baseEthics represents the, well, basic ethics of the society. :V

	public Ethics(Map<String,String> baseEthics){
		this.baseEthics = baseEthics;
		setBaseValues();
	}

	public void permuteEthics(int permutations){
		//Permutes baseValues (not baseEthics, which can be used to reset these changes.)
		Random r = new Random(); int rand;
		String[] s = new String[28];
		baseValues.keySet().toArray(s);
		for (int x=0; x<permutations; x++){
			String key = s[r.nextInt(s.length)];
			if (key == null) continue;
			if ((rand=r.nextInt(3))==0){
				if (baseValues.get(key) == null) System.out.println("Base values set inaccurately");
				baseValues.get(key)[0] = baseValues.get(key)[0]-1;
				if (r.nextInt(2)==0) baseValues.get(key)[1] = baseValues.get(key)[1]-1;
			}
			else if (rand == 1){
				if (baseValues.get(key) == null) System.out.println("Base values set inaccurately");
				baseValues.get(key)[0] = baseValues.get(key)[0]+1;
				if (r.nextInt(2)==0) baseValues.get(key)[1] = baseValues.get(key)[1]+1;
			}
		}
	}
	
	public void setBaseValues(){
		System.out.println("Generated base values.");
		//Fixes the base values for the ethics BASED ON BASEETHICS. 
		this.baseValues = new HashMap<String, int[]>();
		for (String key : baseEthics.keySet()){
			if (key == null) continue;
			int v0 = 0; int v1 = 0;
			switch (baseEthics.get(key).split(",")[0]){
			case "normal": v0=0; break;
			case "religious_matter": v0=1; break;
			case "okay": v0=2; break;
			case "only_minor_circumstances": v0=3; break;
			case "only_serious_circumstances": v0=4; break;
			case "only_self_defense": v0=5; break;
			case "only_extreme_circumstances": v0=6; break;
			case "never": v0=7; break;
			default: v0=0; break;
			}
			
			switch (baseEthics.get(key).split(",")[1]){
			case "null": v1=0; break;
			case "disdain": v1=1; break;
			case "shun": v1=2; break;
			case "personal_matter": v1=3; break;
			case "punish_minor": v1=4; break;
			case "punish_serious": v1=5; break;
			case "punish_extreme": v1=6; break;
			case "punish_exile": v1=7; break;
			case "punish_capital": v1=8; break;
			default: v1=1; break;
			}
			int[] v = {v0, v1};
			System.out.println(v0+" "+v1);
			if (v != null && key != null) baseValues.put(key, v);
		}
	}
	
	public int[] getEthicalValue(String key){
		//Returns an ethical value, or default
		if (baseValues.containsKey(key)){
			return baseValues.get(key);
		}
		else {
			int[] ret = {0, 0};
			return ret;
		}
	}
}
