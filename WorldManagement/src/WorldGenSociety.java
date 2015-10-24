import java.util.ArrayList;
import java.util.List;

public class WorldGenSociety {
	//generates Societies
	//every Society 
	
	public static List<Society> generateSocieties(World w, int numberOfExtraSocieties){
		//Society generation 
		w.societies(new Society[w.hmap().length][w.hmap()[0].length]);
		List<Society> societies = new ArrayList<Society>();
		BaseSociety[] intels = new BaseSociety[w.intels().keySet().size()];
		int i2=0;
		for (String key : w.intels().keySet()){
			intels[i2]=w.intels().get(key);
			i2++;
		}
		for (int i=0; i<intels.length; i++){
			societies.add(Society.generateNewSociety(w, intels[i]));
		}
		for (int i=0; i<numberOfExtraSocieties; i++){
			
		}
		return societies; 
	}	
	
}
