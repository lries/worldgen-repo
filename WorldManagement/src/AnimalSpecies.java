import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AnimalSpecies {
	//Designates a species of animal.
	public List<String> simpleTags;
	public Map<String, String> binaryTags;
	public Map<String, Map<String, String>> complexTags;

	public AnimalSpecies(){
		simpleTags = new ArrayList<String>();
		binaryTags = new HashMap<String, String>();
		complexTags = new HashMap<String, Map<String, String>>();
	}
	
	public void addTag(String tag) {
		// TODO Auto-generated method stub
		System.out.println("Adding tag: "+tag);
		String[] subtags = tag.split(";");
		if (subtags.length == 1) {
			subtags = subtags[0].split(":");
			if (subtags.length == 1) addSimpleTag(subtags[0]);
			else addBinaryTag(subtags[0], subtags[1]);
		}
		else{
			String pTag = subtags[0].split(":")[0];
			addComplexTag(subtags[0].split(":")[0], subtags[0].split(":")[1], subtags[0].split(":")[2]);
			for (int x=1; x<subtags.length; x++){
				if (subtags[x].split(":").length > 1)
					addComplexTag(pTag, subtags[x].split(":")[0], subtags[x].split(":")[1]);
				else 
					addComplexTag(pTag, subtags[x].split(":")[0], "no_param");
			}
		}
	}

	public void addSimpleTag(String tag){
		System.out.println(tag);
		simpleTags.add(tag);
	}
	
	public void addBinaryTag(String tag, String value){
		System.out.println(tag+"-"+value);
		binaryTags.put(tag, value);
	}

	private void addComplexTag(String tag, String subtag, String value) {
		System.out.println(tag+":"+subtag+"-"+value);
		if (complexTags.containsKey(tag)){
			complexTags.get(tag).put(subtag, value);
		}
		else {
			complexTags.put(tag, new HashMap<String, String>());
			complexTags.get(tag).put(subtag, value);
		}
	}
	
	public String getName() {
		return binaryTags.get("ANIMAL");
	}
	
	public String toString() { 
		return binaryTags.get("ANIMAL");
	}
	
	public List<String> getBinaryTag(String key, String delim){
		return Arrays.asList(binaryTags.get(key).split(delim));
	}
	
	public Map<String, List<String>> getComplexTag(String key, String delim){
		//Returns a map from the sub-keys to split of their values by delim
		for (String mykeys: complexTags.get(key).keySet()){
			System.out.println(mykeys);
		}
		Map<String, List<String>> ret = new HashMap<String, List<String>>();
		for (String subkey : complexTags.get(key).keySet()){
			List<String> subret = new ArrayList<String>();
			for (String s : complexTags.get(key).get(subkey).split(delim)){
				subret.add(s);
			}
			ret.put(subkey, subret);
			System.out.println(subkey+":"+subret);
		}
		return ret;
	}
	
}
