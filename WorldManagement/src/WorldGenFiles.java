import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WorldGenFiles {
	public static File[] getFileContents(String fold){
		File folder = new File(fold);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        System.out.println(file.getName());
		    }
		}
		return listOfFiles;
	}
	
	public static Map<String, AnimalSpecies> createSpecies(File[] files){
		Map<String, AnimalSpecies> species = new HashMap<String, AnimalSpecies>();
		AnimalSpecies s;
		for (File f: files){
			s = buildSpecies(f);
			species.put(s.getName(), s);
		}
		return species;
	}
	
	public static AnimalSpecies buildSpecies(File f){
		AnimalSpecies s = new AnimalSpecies();
        BufferedReader input = null;
		String tags = "";
        try {
            input = new BufferedReader(new FileReader(f));
        	String str = input.readLine();
            while (str != null){
            	tags+=str;
            	str = input.readLine();
            }
            System.out.println(tags);
		} catch (IOException e){ 
			e.printStackTrace();
		}
		addTags(tags, s);
		return s;
	}
	
	public static AnimalSpecies addTags(String lst, AnimalSpecies s){
		String currentTag = "";
		for (int x=0; x<lst.length(); x++){
			if (lst.charAt(x)=='['){
				currentTag = "";
			}
			else if (lst.charAt(x)==']'){
				s.addTag(currentTag);
			}
			else {
				currentTag+=lst.charAt(x);
			}
		}
		return s;
	}
	
	public static Map<String, BaseSociety> createSocieties(File[] files){
		Map<String, BaseSociety> societies = new HashMap<String, BaseSociety>();
		BaseSociety s;
		for (File f: files){
			s = buildSociety(f);
			societies.put(s.getName(), s);
		}
		return societies;
	}
	
	public static BaseSociety buildSociety(File f){
		BaseSociety s = new BaseSociety();
        BufferedReader input = null;
		String tags = "";
        try {
            input = new BufferedReader(new FileReader(f));
        	String str = input.readLine();
            while (str != null){
            	tags+=str;
            	str = input.readLine();
            }
            System.out.println(tags);
		} catch (IOException e){ 
			e.printStackTrace();
		}
		addTags(tags, s);
		return s;
	}
	
	public static BaseSociety addTags(String lst, BaseSociety s){
		String currentTag = "";
		for (int x=0; x<lst.length(); x++){
			if (lst.charAt(x)=='['){
				currentTag = "";
			}
			else if (lst.charAt(x)==']'){
				s.addTag(currentTag);
			}
			else {
				currentTag+=lst.charAt(x);
			}
		}
		return s;
	}
	
	public static void generateAllBases(World w){
		Map<String, AnimalSpecies> species = createSpecies(getFileContents("animals\\"));
		w.species(species);
		Map<String, BaseSociety> intels = createSocieties(getFileContents("societies\\"));
		w.intels(intels);
	}
	
	public static void main(String[] args){
		//World wgt = WorldGenTopo.genWorld(512, 256, 20, true, 7, 0.750);
		File[] f = getFileContents("societies\\");
		createSocieties(f);
		f = getFileContents("animals\\");
		createSpecies(f);
	}
}
