import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseSociety {
	//Society governs the basic traits of any culture that spawns from one of the base societies
	private List<String> simpleTags;
	private Map<String, String> binaryTags;
	private Map<String, Map<String, String>> complexTags;
	public BaseSociety(){
		simpleTags = new ArrayList<String>();
		binaryTags = new HashMap<String, String>();
		complexTags = new HashMap<String, Map<String, String>>();
	}
	
	public void addTag(String tag) {
		// TODO Auto-generated method stub
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
				addComplexTag(pTag, subtags[x].split(":")[0], subtags[x].split(":")[1]);
			}
		}
	}

	public void addSimpleTag(String tag){
		System.out.println("Adding simple tag: "+tag);
		System.out.println(tag);
		simpleTags.add(tag);
	}
	
	public void addBinaryTag(String tag, String value){
		System.out.println("Adding binary tag: "+tag);
		System.out.println(tag+"-"+value);
		binaryTags.put(tag, value);
	}

	private void addComplexTag(String tag, String subtag, String value) {
		System.out.println("Adding complex tag: "+tag);
		System.out.println(tag+":"+subtag+"-"+value);
		if (complexTags.containsKey(tag)){
			complexTags.get(tag).put(subtag, value);
		}
		else {
			complexTags.put(tag, new HashMap<String, String>());
			complexTags.get(tag).put(subtag, value);
		}
		System.out.println("The following complex tags exist: "+complexTags.keySet().toString());
	}

	public String getName() {
		return binaryTags.get("SOCIETY");
	}
	
	public Map<String, List<String>> getComplexTag(String key, String delim){
		//Returns a map from the sub-keys to split of their values by delim
		if (!complexTags.containsKey(key)){
			System.out.println("ERROR: "+key+" is invalid.");
			System.out.println("The following are legal:");
			for (String k : complexTags.keySet()){
				System.out.println(k);
			}
		}
		Map<String, List<String>> ret = new HashMap<String, List<String>>();
		System.out.println(complexTags.keySet().toString());
		for (String subkey : complexTags.get(key).keySet()){
			List<String> subret = new ArrayList<String>();
			for (String s : complexTags.get(key).get(subkey).split(delim)){
				subret.add(s);
			}
			ret.put(subkey, subret);
		}
		return ret;
	}

	public String getBinaryTag(String tag) {
		return binaryTags.get(tag);
	}

	public boolean containsBTag(String tag){
		return binaryTags.containsKey(tag);
	}
	
	public Map<String, String> getComplexTag(String string) {
		return complexTags.get(string);
		
	}
}
