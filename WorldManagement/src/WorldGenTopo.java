import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class WorldGenTopo {
	/*
	 * The world is generated in "chunks" representing some number of in-game tiles
	 * The floor map class represents a portion (one "chunk"?) and a floor-map can be generated from a chunk. 
	 * I'm using all these TODOs because Eclipse lets me navigate by them super easy :D :D :D 
	 */
	public static void genWorld(World w, int xc, int yc, int r, boolean landEdge, int octaves, double waterLevel){
		//Generates a world. Takes the following parameters: 
		//width, height, river #, waterLevel (in %of map): self-explanatory
		//whether land can be at the edge of the map
		//number of octaves. With edges I think 7 or 8 looks best; without 6 or 7. 
		//Generate a heightmap
		float[][] hmap;
		float[][] tmap;
		float[][] smoothwind;
		int[][] binwind, wmasses, oceans, lands, continents;;
		float[][] precip;
		int[][] biomes;
		float wlevel;
		float mlevel;
		List<List<Coordinate>> rivers; List<Coordinate> heads; int[][] basinsAt;

		System.out.println("Generating map...");
		hmap = genWhiteNoise(xc, yc,(long)(Math.random()*1000000));
		hmap = genPerlinNoise(hmap,  octaves);
		if (landEdge) hmap = distortMap(hmap, 30, octaves, true);
		float[][] offsets = genBG(xc, yc);

		//Find the water and mountain levels.
		float[] hsample = new float[xc*yc];
		int n = 0;
		for (int x=0; x<hmap.length; x++){
			for (int y=0; y<hmap[0].length; y++){
				if (!landEdge) hmap[x][y]=hmap[x][y]*offsets[x][y];
				hsample[n]=hmap[x][y];
				n++;
			}
		}
		Arrays.sort(hsample);
		if (!landEdge) hmap = distortMap(hmap, 40, octaves, true);
		boolean[][] water = new boolean[hmap.length][hmap[0].length];
		boolean[][] mountains = new boolean[hmap.length][hmap[0].length];
		wlevel = hsample[(int) (hsample.length*waterLevel)];
		mlevel = hsample[(int) (hsample.length*.99)];
		toImage(hmap, "perlin.png", wlevel, mlevel);

		System.out.println("Finding oceans...");
		wmasses = detWater(hmap, wlevel);
		toImageOceans(wmasses, hmap, "masses.png", wlevel, mlevel);
		oceans = defineLargeRegions(wmasses, 500);
		toImageOceans(oceans, hmap, "oceans.png", wlevel, mlevel);

		System.out.println("Finding continents...");
		lands = detLandmass(hmap, wlevel);
		toImageOceans(lands, hmap, "landmasses.png", wlevel, mlevel);
		continents = defineLargeRegions(lands, 500);
		toImageOceans(continents, hmap, "continents.png", wlevel, mlevel);

		//Generate temperature map
		// 
		System.out.println("Generating temperature map...");
		tmap = new float[hmap.length][hmap[0].length];
		for (int x=0; x<hmap.length; x++){
			for (int y=0; y<hmap[0].length; y++){
				if (hmap[x][y] < wlevel){
					water[x][y]=true;
					mountains[x][y]=false;
				}
				else water[x][y] = false;
				float vfact = tmap[0].length/2 - Math.abs(tmap[0].length/2 - y);
				float hfact = hmap[x][y]*45;
				tmap[x][y]=vfact+hfact;
				if (hmap[x][y] > mlevel) mountains[x][y] = true;
				else mountains[x][y] = false;
			}
		}
		tmap = distortMap(tmap, 30, 5, true);
		toImageTemp(tmap, hmap, "tmap.png", wlevel);

		//Generate rivers? may be superior to do this before generation
		System.out.println("Creating rivers...");
		rivers = new ArrayList<List<Coordinate>>();
		heads = new ArrayList<Coordinate>();
		for (int x=0; x<r; x++){
			rivers.add(generateRiver(hmap, water, mountains, heads, wlevel, mlevel, (long)(Math.random()*1000000)));
		}
		System.out.println("Detecting river basins...");
		basinsAt = detectRiverBasins(rivers, hmap, water);

		toImageRiverCurrents(rivers, hmap, "currents.png", wlevel, mlevel, basinsAt);
		toImageRivers(rivers, hmap, "rivers.png", wlevel, mlevel);
		toImageBool(water, "water.png");

		System.out.println("Creating wind...");
		//Generate wind. This should probably play into temperature but IDK. 
		smoothwind = genWind(xc, yc);
		binwind = genRoughWindMap(smoothwind);
		toImageRWind(binwind, hmap, "roughwind.png", wlevel);

		System.out.println("Creating precipitation...");
		//Generate precipitation.
		precip = genPrecip(binwind, hmap, water, mountains);
		toImagePrecip(precip, hmap, "precip.png", wlevel);
		toImageOceans(basinsAt, hmap, "basins.png", wlevel, mlevel);
		//Generate biomes
		System.out.println("Creating biomes...");
		biomes = makeBiomes(precip, hmap, tmap, wlevel, mlevel, waterLevel);
		toImageBiomes(hmap, biomes, "biomes.png", wlevel, mlevel);

		w.hmap(hmap);
		w.tmap(tmap);
		w.smoothwind(smoothwind);
		w.binwind(binwind);
		w.wmasses(wmasses);
		w.oceans(oceans);
		w.lands(lands);
		w.continents(continents);
		w.precip(precip);
		w.biomes(biomes);
		w.wlevel(wlevel);
		w.mlevel(mlevel);
	}

	//BIOME METHODS
	// TODO biomes
	private static int[][] smoothBiomes(int[][] biomes, float[][] hmap, int deg){
		Coordinate dir;
		Coordinate pos;
		int score;
		Random r = new Random();
		for (int x=0; x<biomes.length; x++){
			for (int y=0; y<biomes[0].length; y++){
				score = 0;
				pos = new Coordinate(x, y);
				dir = goNorth(pos, hmap);
				if (biomes[x][y] == biomes[dir.x][dir.y]) score++;
				if (score >= deg) continue;
				pos = new Coordinate(x, y);
				dir = goSouth(pos, hmap);
				if (biomes[x][y] == biomes[dir.x][dir.y])score++;
				if (score >= deg) continue;
				pos = new Coordinate(x, y);
				dir = goEast(pos, hmap);
				if (biomes[x][y] == biomes[dir.x][dir.y]) score++;
				if (score >= deg) continue;
				pos = new Coordinate(x, y);
				dir = goWest(pos, hmap);
				if (biomes[x][y] == biomes[dir.x][dir.y]) score++;
				if (score >= deg) continue;

				switch(r.nextInt(4)){
				case 0: dir = goNorth(pos, hmap); biomes[x][y]=biomes[dir.x][dir.y]; break;
				case 1: dir = goSouth(pos, hmap); biomes[x][y]=biomes[dir.x][dir.y]; break;
				case 2: dir = goEast(pos, hmap); biomes[x][y]=biomes[dir.x][dir.y]; break;
				case 3: biomes[x][y]=biomes[dir.x][dir.y]; break;
				}
			}
		}
		return biomes;
	}

	private static int[][] makeBiomes(float[][] rain, float[][] hmap, float[][] temp, float wlevel, float mlevel, double wper){
		/* BIOMES
		 * Wet: wetlands(14), rainforest(15) 
		 * Semiwet: taiga (0), temperate forest (1), prarie (4)
		 * Semidry: savanna (2), steppe (3), prarie (4)
		 * Dry: desert (hot (6), cold (7), arid (8)
		 * Special: mountain (9), tundra (10)
		 * Ocean: Tropical (11), temperate (12), arctic (13)
		 * I might differentiate even further BUT not now. 
		 */
		float maxTemp = 0;
		float minTemp = 100000;
		float minPercip = 0;
		for (int x=0; x<temp.length; x++){
			for (int y=0; y<temp[0].length; y++){
				if (temp[x][y] > maxTemp && hmap[x][y]>wlevel) maxTemp = temp[x][y]; 
				if (temp[x][y] < minTemp && hmap[x][y]>wlevel) minTemp = temp[x][y];
				if (rain[x][y] > minPercip && hmap[x][y]>wlevel) minPercip = rain[x][y];
			}
		}
		System.out.println(minTemp+ " "+maxTemp+" "+minPercip);
		//Determine what temperatures constitute frigid, cold, temperate, warm, hot
		//Frigid and hot get 15% of land, cold and warm get 20%, temperate gets 30%

		double frigid = .15*(maxTemp - minTemp)+minTemp;
		double cold  = .35*(maxTemp - minTemp)+minTemp;
		double temperate = .65*(maxTemp - minTemp)+minTemp;
		double warm = .85*(maxTemp - minTemp)+minTemp;


		//Determine what percipitation values constitute dry, semidry, semiwet, wet
		//Dry constitutes 15%, semidry 35%, semiwet 45%, wet 5%.  

		double wet = .05*(minPercip+1);
		double semiwet = .50*(minPercip+1);
		double semidry = .85*(minPercip+1);

		int[][] biomes = new int[rain.length][rain[0].length];
		for (int x=0; x<biomes.length; x++){
			for (int y=0; y<biomes[0].length; y++){
				biomes[x][y] = -1; //value to make errors obvious
				//Mountain?
				if (hmap[x][y] > mlevel) biomes[x][y] = 9;
				//Ocean? Right now this triggers off some lakes too; I'll fix it later.
				//fix it l8r ahahahhahahahahaha 

				else if (hmap[x][y] < wlevel) {
					if (temp[x][y] < frigid) { biomes[x][y] = 13; }
					else if (temp[x][y] < warm) { biomes[x][y] = 12; }
					else { biomes[x][y] = 11; }
				}
				//Frigid land
				else if (temp[x][y] < frigid) biomes[x][y] = 10; 
				//Cold land
				else if (temp[x][y] < cold) { 
					if (rain[x][y] < 0) biomes[x][y] = 7;
					else if (rain[x][y] < semiwet) biomes[x][y] = 0;
					else if (rain[x][y] < semidry) biomes[x][y] = 3;
					else biomes[x][y] = 7;
				}
				else if (temp[x][y] < temperate) {
					if (rain[x][y] < 0) biomes[x][y] = 8;
					else if (rain[x][y] < wet) biomes[x][y] = 14;
					else if (rain[x][y] < semiwet) biomes[x][y] = 1;
					else if (rain[x][y] < semidry) biomes[x][y] = 4;
					else biomes[x][y] = 8;
				}
				else if (temp[x][y] < warm) {
					if (rain[x][y] < 0) biomes[x][y] = 6;
					else if (rain[x][y] < wet) biomes[x][y] = 14;
					else if (rain[x][y] < semiwet) biomes[x][y] = 1;
					else if (rain[x][y] < semidry) biomes[x][y] = 2;
					else biomes[x][y] = 6;
				}
				else {
					if (rain[x][y] < 0) biomes[x][y] = 6;
					else if (rain[x][y] < wet) biomes[x][y] = 15;
					else if (rain[x][y] < semiwet) biomes[x][y] = 2;
					else if (rain[x][y] < semidry) biomes[x][y] = 2;
					else biomes[x][y] = 6;
				}
			}
		}

		return smoothBiomes(biomes, hmap, 2);
	}

	//PRECIPITATION METHODS
	// TODO precip
	private static float[][] genPrecip(int[][] wind, float[][] hmap, boolean[][] water, boolean[][] mountains){
		//The lower the value the greater the preciptitation. I'll standardize it LATER.
		float[][] precip = new float[wind.length][wind[0].length];
		for (int x=0; x<precip.length; x++){
			for (int y=0; y<precip[0].length; y++){
				Coordinate current = new Coordinate(x, y);
				int val = 0;
				int iter = 0;
				while (!water[current.x][current.y] && !mountains[current.x][current.y] && iter < 10000){
					val++;
					iter++;
					switch (wind[current.x][current.y]){
					case 1: current = goSouth(current, hmap); break;
					case 2: current = goSouth(current, hmap); current = goWest(current, hmap); break;
					case 3: current = goWest(current, hmap); break;
					case 4: current = goWest(current,hmap); current = goNorth(current, hmap); break;
					case 5: current = goNorth(current, hmap); break;
					case 6: current = goNorth(current, hmap);  current = goEast(current, hmap); break;
					case 7: current = goEast(current, hmap); break;
					case 8: current = goEast(current, hmap); current = goSouth(current, hmap); break;
					}
				}
				if (water[current.x][current.y]) precip[x][y] = val;
				else precip[x][y] = -1;
			}
		}
		return precip;
	}

	private static Coordinate goNorth(Coordinate c, float[][] map){
		if (c.y == map[0].length-1) return new Coordinate(c.x, 0);
		else return new Coordinate(c.x, c.y+1);
	}
	private static Coordinate goSouth(Coordinate c, float[][] map){
		if (c.y == 0) return new Coordinate(c.x, map[0].length - 1);
		else return new Coordinate(c.x, c.y-1);
	}
	private static Coordinate goWest(Coordinate c, float[][] map){
		if (c.x == map.length-1) return new Coordinate(0, c.y);
		else return new Coordinate(c.x+1, c.y);
	}
	private static Coordinate goEast(Coordinate c, float[][] map){
		if (c.x == 0) return new Coordinate(map.length-1, c.y);
		else return new Coordinate(c.x-1, c.y);
	}

	//CONTINENT, ISLAND DETERMINATION
	//TODO land definition
	//This could probably be collate with Ocean determination, but I'm thinking about changing ocean parameters.
	//In which case it of course cannot.
	//River basin lakes count for continent size; at the moment low-lying lakes don't. Probably something to fix later.

	private static int[][] detLandmass(float[][] hmap, float wlevel){
		boolean[][] bool = new boolean[hmap.length][hmap[0].length];
		int[][] lands = new int[hmap.length][hmap[0].length];
		for (int x=0; x<hmap.length; x++){
			for (int y=0; y<hmap[0].length; y++){
				if (hmap[x][y] >= wlevel) bool[x][y] = true;
				else bool[x][y] = false;
				lands[x][y] = -1;
			}
		}
		int count = 0;
		for (int x=0; x<bool.length; x++){
			for (int y=0; y<bool[0].length; y++){
				if (bool[x][y]){
					findBody(new Coordinate(x, y), bool, hmap, lands, count);
					count++;
				}
			}	
		}
		return lands;
	}

	private static int[][] defineLargeRegions(int[][] landmasses, int size){
		//Defines large regions. Designed for continents; also finds oceans but I think finding lakes is more interesting. 
		int[][] continents = new int[landmasses.length][landmasses[0].length];
		int max = 0;
		for (int x=0; x<landmasses.length; x++){
			for (int y=0; y<landmasses[0].length; y++){
				if (landmasses[x][y] > max) max = landmasses[x][y];
			}
		}
		int[] samples = new int[max + 1];
		for (int x=0; x<landmasses.length; x++){
			for (int y=0; y<landmasses[0].length; y++){
				if (landmasses[x][y] != -1) samples[landmasses[x][y]]++;
			}
		}
		for (int x=0; x<landmasses.length; x++){
			for (int y=0; y<landmasses[0].length; y++){
				if (landmasses[x][y] != -1 && samples[landmasses[x][y]] >= size) { 
					continents[x][y] = landmasses[x][y];
				}
				else continents[x][y] = -1;
			}
		}


		return continents;
	}

	//OCEAN, RIVER, AND LAKE DETERMINATION
	//rad
	//TODO ocean definition
	//  detWater(hmap, wlevel);
	private static int[][] detWater(float[][] hmap, float wlevel){
		boolean[][] bool = new boolean[hmap.length][hmap[0].length];
		int[][] oceans = new int[hmap.length][hmap[0].length];
		for (int x=0; x<hmap.length; x++){
			for (int y=0; y<hmap[0].length; y++){
				if (hmap[x][y] < wlevel) bool[x][y] = true;
				else bool[x][y] = false;
				oceans[x][y] = -1;
			}
		}
		int count = 0;
		for (int x=0; x<bool.length; x++){
			for (int y=0; y<bool[0].length; y++){
				if (bool[x][y]){
					findBody(new Coordinate(x, y), bool, hmap, oceans, count);
					count++;
				}
			}	
		}
		return oceans;
	}

	private static void findBody(Coordinate c, boolean[][] field, float[][] hmap, int[][] oceans, int count){
		//Coordinate c: the place of visitation
		//field: the boundaries. true = good :V
		//hmap: heightmap
		//oceans: list of oceans 2 set to
		//count: value for oceans
		List<Coordinate> frontier = new ArrayList<Coordinate>();
		List<Coordinate> visited = new ArrayList<Coordinate>();
		frontier.add(c);
		Coordinate current; Coordinate successor;
		while (!frontier.isEmpty()){
			current = frontier.remove(0);
			if (!field[current.x][current.y]) continue;
			visited.add(current);
			oceans[current.x][current.y] = count;
			field[current.x][current.y] = false;

			successor = goNorth(current, hmap);
			if (field[successor.x][successor.y]) frontier.add(successor);

			successor = goEast(current, hmap);
			if (field[successor.x][successor.y]) frontier.add(successor);

			successor = goSouth(current, hmap);
			if (field[successor.x][successor.y]) frontier.add(successor);

			successor = goWest(current, hmap);
			if (field[successor.x][successor.y]) frontier.add(successor);
		}
	}


	@SuppressWarnings("unused")
	private static int[][] defineSmallRegions(int[][] landmasses, int size){
		//Defines small regions. Designed for islands; also finds lakes is more interesting. 
		int[][] continents = new int[landmasses.length][landmasses[0].length];
		int max = 0;
		for (int x=0; x<landmasses.length; x++){
			for (int y=0; y<landmasses[0].length; y++){
				if (landmasses[x][y] > max) max = landmasses[x][y];
			}
		}
		int[] samples = new int[max + 1];
		for (int x=0; x<landmasses.length; x++){
			for (int y=0; y<landmasses[0].length; y++){
				if (landmasses[x][y] != -1) samples[landmasses[x][y]]++;
			}
		}
		for (int x=0; x<landmasses.length; x++){
			for (int y=0; y<landmasses[0].length; y++){
				if (landmasses[x][y] != -1 && samples[landmasses[x][y]] <= size) { 
					continents[x][y] = landmasses[x][y];
				}
				else continents[x][y] = -1;
			}
		}


		return continents;
	}


	//WIND METHODS
	//As a note I think this was probably the hardest bit of design, but I'm pretty happy with it. Still playing around a little.
	//I'm hoping this'll really help set off the biomes, since the heatmap is kinda dull. 
	// TODO wind 
	private static float[][] genWind(int xc, int yc){
		//Wind generation. Uses a scale from 0-360deg (0=360):
		//0: north; 90: east; 180: south; 270: west
		float[][] wind = new float[xc][yc];
		Random r = new Random();
		int longGap = (yc/6)+1;
		float innerGap, ld, d;
		float cur;
		int p=0;
		int[] choices = new int[7];
		int[] dc = new int[4];
		int last = 23;
		for (int x=0; x<7; x++){
			int cx = r.nextInt(4);
			while (cx == last || dc[cx] > 1){
				cx = r.nextInt(4);
			}
			last = cx;
			dc[cx]++;
			choices[x]=cx*90;
		}
		for (int c=0; c<4; c++){
			if (dc[c]==0) {
				int cx = r.nextInt(7);
				while (dc[choices[cx]/90] < 2){
					cx = r.nextInt(7);
				}
				dc[choices[cx]/90]--;
				choices[cx] = c*90;
				dc[c]=1;
			}
		}
		choices[6]=choices[0];
		for (int iter=0; iter<6; iter++){
			//Select a "basic direction" for the next position
			ld = choices[iter];
			d = choices[iter+1];
			cur = ld;
			innerGap = (d-ld)/longGap;
			//Fills.
			for (int y=p; y<Math.min(p+longGap, wind[0].length); y++){
				for (int x=0; x<wind.length; x++){
					wind[x][y] = cur;
				}
				cur += innerGap;
			}
			p = p+longGap;	
		}
		wind = distortMap(distortMap(wind, 200, 8, false), 200, 8, false);
		return wind;
	}

	public static int[][] genRoughWindMap(float[][] smoothwind){
		//generates a rough wind map with values like this:
		/* 8 1 2
		 * 7   3
		 * 6 5 4
		 */
		int[][] rwm = new int[smoothwind.length][smoothwind[0].length];
		for (int x=0; x<smoothwind.length; x++){
			for (int y=0; y<smoothwind[0].length; y++){
				/*	if (smoothwind[x][y] < 22.5 || smoothwind[x][y] > 337.5) rwm[x][y] = 1;
				else if (smoothwind[x][y] < 67.5) rwm[x][y] = 2;
				else if (smoothwind[x][y] < 112.5) rwm[x][y] = 3;
				else if (smoothwind[x][y] < 157.5) rwm[x][y] = 4;
				else if (smoothwind[x][y] < 202.5) rwm[x][y] = 5;
				else if (smoothwind[x][y] < 247.5) rwm[x][y] = 6;
				else if (smoothwind[x][y] < 292.5) rwm[x][y] = 7;
				else rwm[x][y] = 8;
				 */

				//GENERATION ISSUE: BAD NUMBERS
				double val = smoothwind[x][y] / 45;
				double rnd = val%1;
				if (val < 1) {
					if (rnd < Math.random()) { rwm[x][y] = 8; }
					else rwm[x][y] = 1;
				}
				else {
					if (rnd < Math.random()) { rwm[x][y] = (int) val; }
					else rwm[x][y] = (int) val+1;
				}
			}
		}
		return rwm;
	}

	//RIVER METHODS
	// TODO Rivers
	private static boolean checkHeads(List<Coordinate> heads, Coordinate h, int minDist){
		//checks if the head of a river is too close to another one
		for (Coordinate head : heads){
			if (Math.sqrt((head.x-h.x)*(head.x-h.x) + (head.y-h.y)*(head.y-h.y)) < minDist) return false;
		}
		return true;
	}

	private static int direction(Coordinate source, Coordinate to, float[][] hmap){
		//Returns the direction from source to to. Assumes they're consecutive, but works fine either way except in the case of wrapping.
		//8 1 2
		//7   3
		//6 5 4

		boolean left = false, right = false, up = false, down = false;
		if (source.x == 0 && to.x == hmap.length -1) left = true;
		else if (to.x == 0 && source.x == hmap.length -1) right = true;
		else {
			left = source.x > to.x;
			right = source.x < to.x;
		}
		if (source.y == 0 && to.y == hmap[0].length -1) up = true;
		else if (to.y == 0 && source.y == hmap[0].length -1) down = true;
		else {
			down = source.y < to.y;
			up = source.y > to.y;
		}
		if (up){
			if (left) return 8;
			if (right) return 2;
			return 1;
		}
		if (down){
			if (left) return 6;
			if (right) return 4;
			return 5;
		}
		if (left) return 7;
		return 3;
	}


	private static int[][] detectRiverBasins(List<List<Coordinate>> rivers, float[][] hmap, boolean[][] water){
		//Detects river basins. They are removed from the Rivers list, as well. :)
		//Currently, there's an error with the left sides of bodies of water. 
		int[][] basins = new int[hmap.length][hmap[0].length];
		for (int x=0; x<basins.length; x++){
			for (int y=0; y<basins[0].length; y++){
				basins[x][y] = -1;
			}
		}
		for (int river=0; river<rivers.size(); river++){
			for (int i=0; i<rivers.get(river).size(); i++){
				boolean xwrap=false, ywrap=false; 
				int wl=0, xit = 0;
				for (int x=rivers.get(river).get(i).x-1; x<rivers.get(river).get(i).x+2; x++){
					xit = 0;
					for (int y=rivers.get(river).get(i).y-1; y<rivers.get(river).get(i).y+2; y++){
						if (x==rivers.get(river).get(i).x && y==rivers.get(river).get(i).y) continue;
						if (x<0) { x=hmap.length-1; xwrap=true; }
						if (y<0) { y=hmap[0].length-1; ywrap=true; }

						if (x>=hmap.length) { x=0; xwrap=true; }
						if (y>=hmap[0].length) { y=0; ywrap=true; }

						if (water[x][y]) wl++;
						if (wl > 2) break;

						if (x==hmap.length-1 && xwrap) { x=-1; xwrap=false; }
						if (y==hmap[0].length-1 && ywrap) { y=-1; ywrap=false; }
						if (x == 0 && xwrap) {
							x=hmap.length-1;
							xit++;
						};
						if (y == 0 && ywrap) break;
					}
					if (wl > 2 || xit>3 || ywrap) break;
				}				
				if (wl > 2) basins[rivers.get(river).get(i).x][rivers.get(river).get(i).y] = 1;
			}
		}
		return basins;
	}

	private static List<Coordinate> generateRiver(float[][] hmap, boolean[][] w, boolean[][] m, List<Coordinate> heads, float wlevel, float mlevel, long seed){
		//Generates a river. :B
		List<Coordinate> river = new ArrayList<Coordinate>();
		int[][] water = detWater(hmap, wlevel);
		int[][] ends = defineLargeRegions(water, 100);
		Random r = new Random(seed);	
		int x = r.nextInt(hmap.length);
		int y = r.nextInt(hmap[0].length);
		int iter = 0;
		if (r.nextFloat() < 0.35){	
			while (w[x][y] || !checkHeads(heads, new Coordinate(x,y), 10)){	
				iter++;
				if (iter > 1000) { return new ArrayList<Coordinate>(); }
				x = r.nextInt(hmap.length);
				y = r.nextInt(hmap[0].length);
			}
		}
		else {
			while (!m[x][y] || !checkHeads(heads, new Coordinate(x,y), 10)){	
				iter++;
				if (iter > 1000) { return new ArrayList<Coordinate>(); }
				x = r.nextInt(hmap.length);
				y = r.nextInt(hmap[0].length);
			}
		}
		Coordinate head = new Coordinate(x, y);
		if (w[head.x][head.y]) return new ArrayList<Coordinate>();
		heads.add(head);
		river.add(head);
		Coordinate current = head;
		iter = 0;
		while (ends[current.x][current.y] == -1 && iter < 1000){
			iter++;
			Coordinate next = new Coordinate(Math.max(0, current.x-1), Math.max(0, current.y-1));
			for (int i=Math.max(0, current.x-1); i<=Math.min(current.x+1, hmap.length-1); i++){
				for (int j=Math.max(0, current.y-1); j<=Math.min(current.y+1, hmap[0].length-1); j++){
					if (i != current.x || j != current.y){
						if (hmap[i][j] < hmap[next.x][next.y] && notIn(i, j, river)) next = new Coordinate(i, j);
					}
				}
			}
			if (current.x == 0) {
				if (hmap[hmap.length-1][current.y] < hmap[next.x][next.y] && notIn(hmap.length-1, current.y, river)) next = new Coordinate(hmap.length-1, current.y);
			}
			if (current.y == 0){
				if (hmap[current.x][hmap[0].length-1] < hmap[next.x][next.y] && notIn(current.x, hmap[0].length-1, river)) next = new Coordinate(current.x, hmap[0].length-1);
			}
			if (current.x == hmap.length-1){
				if (hmap[0][current.y] < hmap[next.x][next.y] && notIn(0, current.y, river)) next = new Coordinate(0, current.y);
			}
			if (current.y == hmap[0].length-1){
				if (hmap[current.x][0] < hmap[next.x][next.y] && notIn(current.x, 0, river)) next = new Coordinate(current.x, 0);

			}
			river.add(next);
			current.dir = direction(current, next, hmap);
			current = next;
		}
		if (iter == 1000) {
			heads.remove(head);
			return new ArrayList<Coordinate>();
		}
		for (Coordinate c : river){
			w[c.x][c.y] = true;
		}
		return river;
	}

	private static boolean notIn(int i, int j, List<Coordinate> river) {
		//checks if x, y is in the coordinate. lazy :V
		for (Coordinate c : river){
			if (c.x == i && c.y == j) return false;
		}
		return true;
	}

	//GENERAL DERIVATION METHODS
	// TODO general

	private static float[][] distortMap (float[][] map, int degrees, int octaves, boolean xof){
		//Distorts a map. 
		float[][] newMap = new float[map.length][map[0].length];
		float[][] xOff = genPerlinNoise(genWhiteNoise(newMap.length, newMap[0].length,(long)(Math.random()*100000)), octaves);
		float[][] yOff = genPerlinNoise(genWhiteNoise(newMap.length, newMap[0].length,(long)(Math.random()*100000)), octaves);
		int xo, yo;
		for (int x=0; x<map.length; x++){
			for (int y=0; y<map[0].length; y++){
				if (xof) xo=(int) (x+xOff[x][y]*degrees); 
				else xo=x;
				yo=(int) (y+yOff[x][y]*degrees); 
				while (xo >= map.length) xo -= map.length;
				while (yo >= map[0].length) yo -= map[0].length;
				newMap[x][y]=map[xo][yo];
			}
		}
		return newMap;
	}

	private static float[][] genWhiteNoise (int width, int height, long seed){
		//Generates an array of noise in ranges 0-1. :VVV
		Random r = new Random(seed);
		float[][] noise = new float[width][height];
		for (int x=0; x<width; x++){
			for (int y=0; y<height; y++){
				noise[x][y] = (float) r.nextDouble();
			}
		}
		return noise;
	}

	private static float[][] genSmoothNoise(float[][] baseNoise, int octave){
		//Smooths noise out
		int width = baseNoise.length;
		int height = baseNoise[0].length;
		float[][] smoothNoise = new float[width][height];

		int samplePeriod = 1<<octave;
		float sampleFrequency = 1f/samplePeriod;

		for (int i=0; i<width; i++){
			int si0 = (i/samplePeriod)*samplePeriod;
			int si1 = (si0 + samplePeriod) % width;
			float hblend = (i - si0) * sampleFrequency;

			for (int j=0; j<height; j++){
				int sj0 = (j/samplePeriod)*samplePeriod;
				int sj1 = (sj0 + samplePeriod) % height;
				float vblend = (j - sj0) * sampleFrequency;

				float top = Interpolate(baseNoise[si0][sj0], baseNoise[si1][sj0], hblend);
				float bottom = Interpolate(baseNoise[si0][sj1], baseNoise[si1][sj1], hblend);
				smoothNoise[i][j] = Interpolate(top, bottom, vblend);
			}
		}
		return smoothNoise;
	}

	private static float[][] genPerlinNoise(float[][] baseNoise, int octaveCount){
		//Generates Perlin noise
		//More octaves = smoother; anything less than 7 or more than 10 is probably useless for a heightmap
		//Although you get tons of islands with o=6; that can be cool
		int width = baseNoise.length;
		int height = baseNoise[0].length;
		float[][][] smoothNoise = new float[octaveCount][][];
		float persistance = 0.5f;

		for (int i=0; i<octaveCount; i++){
			smoothNoise[i] = genSmoothNoise(baseNoise, i);
		}

		float[][] perlinNoise = new float[width][height];
		float amplitude = 1.0f;
		float totalAmplitude = 0.0f;
		for (int o = octaveCount-1; o>=0; o--){
			amplitude*=persistance;
			totalAmplitude += amplitude;
			for (int i=0; i<width; i++){
				for (int j=0; j<height; j++){
					perlinNoise[i][j] += smoothNoise[o][i][j]*amplitude;
				}
			}
		}
		for (int i = 0; i<width; i++)
		{
			for (int j = 0; j<height; j++)
			{
				perlinNoise[i][j] /= totalAmplitude;
			}
		}
		return perlinNoise;
	}

	private static float Interpolate(float x0, float x1, float alpha) {
		//interpolates :V
		return x0*(1-alpha) + alpha*x1;
	}

	private static float[][] genBG(int xc, int yc){
		//Generates a simple box gradient and offsets it slightly to make it less obvious. Lets me make certain guarantees about my landmass.
		float[][] bg = new float[xc][yc];
		for (int x=0; x<xc; x++){
			for (int y=0; y<yc; y++){
				bg[x][y]=1;
			}
		}
		double iter = 2.0/Math.min(xc, yc);
		System.out.println(iter);
		float it = 0;
		int rng = 0;
		for (int i=0; i<Math.min(xc/2, yc/2) && it<1; i++){
			addRing(bg, rng, rng, it);
			rng += 1;
			it += iter*1.7;
		}

		float[][] offset = genWhiteNoise(xc, yc,(long)(Math.random()*1000000));
		offset = genPerlinNoise(offset, 6);
		for (int x=0; x<xc; x++){
			for (int y=0; y<yc; y++){
				bg[x][y]*=(offset[x][y]+bg[x][y]*2)/3;
			}
		}

		return bg;

	}

	private static void addRing(float[][] f, int xr, int yr, float a){
		//creates a "ring" of a value in the map.
		for (int x=xr; x<f.length - xr; x++) {
			f[x][yr] = a;
			f[x][f[0].length - 1 - yr]=a;
		}
		for (int y=yr; y<f[0].length - yr; y++){
			f[xr][y] = a;
			f[f.length - 1 - xr][y] = a;
		}
	}

	//IMAGE METHODS
	// TODO image methods


	private static void toImageOceans(int[][] oceans, float[][] f, String filename, float wlevel, float mlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		float max = 0;
		for (int x=0; x<oceans.length; x++){
			for (int y=0; y<oceans[0].length; y++){
				if (max < oceans[x][y]) max = oceans[x][y];
			}
		}
		float ratio = 255/max/1.25f;
		System.out.println(max+" "+ratio);
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (oceans[i][i2]<0){
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(0,color,0));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					g.setColor(new Color((int)(ratio*oceans[i][i2]),(int)(ratio*oceans[i][i2]),(int)(ratio*oceans[i][i2])));
					g.fillRect(i, i2, 1, 1);
				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}


	private static void toImage(float[][] f, String filename, float wlevel, float mlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (f[i][i2]<wlevel){
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				}
				else if (f[i][i2]<mlevel){
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(0,color,0));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(color,color,color));
					g.fillRect(i, i2, 1, 1);

				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}

	private static void toImageBool(boolean[][] f, String filename){
		//exports  boolean
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (f[i][i2]){
					g.setColor(new Color(0,0,0));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					g.setColor(new Color(255,255,255));
					g.fillRect(i, i2, 1, 1);
				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}


	private static void toImageBiomes(float[][] f, int[][] biomes, String filename, float wlevel, float mlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (f[i][i2]<wlevel){
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				} else {
					switch (biomes[i][i2]){
					case -1: g.setColor(new Color(255,0,0)); break;
					case 0: g.setColor(new Color(20,100,60)); break;
					case 1: g.setColor(new Color(30,160,70)); break;
					case 2: g.setColor(new Color(150,150,0)); break;
					case 3: g.setColor(new Color(100,70,0)); break;
					case 4: g.setColor(new Color(100,255,0)); break;
					case 6: 
					case 7:
					case 8: g.setColor(new Color(100,100,50)); break;
					case 9: g.setColor(new Color(100,0,150)); break;
					case 10: g.setColor(new Color(255,255,255)); break;
					case 11: 
					case 12: 
					case 13: break;
					case 14: g.setColor(new Color(0,230,200)); break;
					case 15: g.setColor(new Color(0,215,0)); break;
					case 16: g.setColor(new Color(0,150,0)); break;
					}
					g.fillRect(i, i2, 1, 1);
				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}

	private static void toImageRivers(List<List<Coordinate>> rivers, float[][] f, String filename, float wlevel, float mlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (f[i][i2]<wlevel){
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				}
				else if (f[i][i2]<mlevel){
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(0,color,0));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					int color = (int)(f[i][i2]*254);
					g.setColor(new Color(color,color,color));
					g.fillRect(i, i2, 1, 1);

				}
			}
			for (List<Coordinate> river: rivers){
				for (Coordinate r : river){
					int color = (int)(f[r.x][r.y]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(r.x, r.y, 1, 1);
				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}

	private static void toImagePrecip(float[][] f, float[][] hmap, String filename, float wlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		float max = 0;
		for (int x=0; x<f.length; x++){
			for (int y=0; y<f[0].length; y++){
				if (f[x][y] > max) { 
					max = f[x][y]; 
				}
			}
		}
		float ratio = 254/(max+1);
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (hmap[i][i2]<wlevel){
					int color = (int)(hmap[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					int color;
					if (f[i][i2] == -1) {color = (int)((max+1)*ratio);}
					else {color = (int)(f[i][i2]*ratio);}
					g.setColor(new Color(color,color,color));
					g.fillRect(i, i2, 1, 1);

				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}

	private static void toImageTemp(float[][] f, float[][] hmap, String filename, float wlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		float max = 0;
		for (int x=0; x<f.length; x++){
			for (int y=0; y<f[0].length; y++){
				if (f[x][y] > max) { 
					max = f[x][y]; 
				}
			}
		}
		float ratio = 254/max;
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (hmap[i][i2]<wlevel || f[i][i2]<0){
					int color = (int)(hmap[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					int color = (int)(f[i][i2]*ratio);
					g.setColor(new Color(color,color,color));
					g.fillRect(i, i2, 1, 1);

				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}

	private static void toImageRWind(int[][] f, float[][] hmap, String filename, float wlevel){
		//exports heightmap with waterlevel and mountainlevel to file filename
		BufferedImage bi = new BufferedImage(f.length,f[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		for (int i=0; i<f.length; i++){
			for (int i2=0; i2<f[0].length; i2++){
				if (hmap[i][i2]<wlevel){
					int color = (int)(hmap[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					switch(f[i][i2]){
					case 1: g.setColor(new Color(200,0,0)); break;   //NORTH - RED
					case 2: g.setColor(new Color(200,100,0)); break; //NEAST - ORANGE
					case 3: g.setColor(new Color(200,200,0)); break; //EAST  - YELLOW
					case 4: g.setColor(new Color(0,200,0)); break;   //SEAST - GREEN
					case 5: g.setColor(new Color(0,200,200)); break; //SOUTH - BRIGHT BLUE
					case 6: g.setColor(new Color(0,0,200)); break;   //SWEST - BLUE
					case 7: g.setColor(new Color(100,0,200)); break; //WEST  - BURPLE 
					case 8: g.setColor(new Color(200,0,200)); break; //NWEST - PURPLE
					}
					g.fillRect(i, i2, 1, 1);

				}
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}

	private static void toImageRiverCurrents(List<List<Coordinate>> rivers, float[][] hmap, String filename, float wlevel, float mlevel, int[][] basinsAt){
		//exports heightmap with waterlevel and mountainlevel to file filename
		BufferedImage bi = new BufferedImage(hmap.length,hmap[0].length,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		//Render image

		for (int i=0; i<hmap.length; i++){
			for (int i2=0; i2<hmap[0].length; i2++){
				if (hmap[i][i2]<wlevel){
					int color = (int)(hmap[i][i2]*254);
					g.setColor(new Color(0,0,color));
					g.fillRect(i, i2, 1, 1);
				}
				else if (hmap[i][i2]<mlevel){
					int color = (int)(hmap[i][i2]*254);
					g.setColor(new Color(0,color,0));
					g.fillRect(i, i2, 1, 1);
				}
				else {
					int color = (int)(hmap[i][i2]*254);
					g.setColor(new Color(color,color,color));
					g.fillRect(i, i2, 1, 1);
				}
			}
		}
		//Render rivers
		for (List<Coordinate> river : rivers){
			for (Coordinate c : river){
				if (basinsAt[c.x][c.y] != -1) {
					int color = (int)(hmap[c.x][c.y]*254);
					g.setColor(new Color(0,0,color));	
				}
				else{
					switch(c.dir){
					case 1: g.setColor(new Color(200,0,0)); break;   //NORTH - RED
					case 2: g.setColor(new Color(200,100,0)); break; //NEAST - ORANGE
					case 3: g.setColor(new Color(200,200,0)); break; //EAST  - YELLOW
					case 4: g.setColor(new Color(0,200,0)); break;   //SEAST - GREEN
					case 5: g.setColor(new Color(0,200,200)); break; //SOUTH - BRIGHT BLUE
					case 6: g.setColor(new Color(0,0,200)); break;   //SWEST - BLUE
					case 7: g.setColor(new Color(100,0,200)); break; //WEST  - BURPLE 
					case 8: g.setColor(new Color(200,0,200)); break; //NWEST - PURPLE
					}
				}
				g.fillRect(c.x, c.y, 1, 1);
			}
		}
		try {
			File file = new File(filename);
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("World to image conversion failed.");
			return;
		}
	}


	//MAIN
	// TODO main
	public static void main(String[] args){
		genWorld(new World(), 512, 256, 20, true, 8, 0.650);
	}

}
