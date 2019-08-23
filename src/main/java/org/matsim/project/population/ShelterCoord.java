package org.matsim.project.population;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;

public class ShelterCoord {
	
	public static List<Coord> getCoord(CoordinateTransformation ct) {
		
		Coord shelterHCES = new Coord ((double) -155.084807, (double) 19.721082);
		Coord coordHCES = ct.transform(shelterHCES); 
		
		Coord shelterHSH = new Coord ((double) -155.090788, (double) 19.723933);
		Coord coordHSH = ct.transform(shelterHSH);
		
		Coord shelterUN = new Coord ((double) -155.98662, (double) 19.63223);
		Coord coordUN = ct.transform(shelterUN);
		
		Coord shelterUHHAF = new Coord ((double) -155.04953, (double) 19.65319);
		Coord coordUHHAF = ct.transform(shelterUHHAF);
		
		Coord shelterPACRC = new Coord ((double) -155.04791, (double) 19.73122);
		Coord coordPACRC = ct.transform(shelterPACRC);
		
		Coord shelterHCC = new Coord ((double) -156.022302, (double) 19.810874);
		Coord coordHCC = ct.transform(shelterHCC);
		
		Coord shelterCCECS = new Coord ((double) -155.079334, (double) 19.703855);
		Coord coordCCECS = ct.transform(shelterCCECS);
		
		Coord shelterTDKICP = new Coord ((double) -155.08957, (double) 19.6977);
		Coord coordTDKICP = ct.transform(shelterTDKICP);
		
		Coord shelterUHH = new Coord ((double) -155.08146, (double) 19.70101);
		Coord coordUHH = ct.transform(shelterUHH);
		
		Coord shelterHPA = new Coord ((double) -155.698639, (double) 20.029981);
		Coord coordHPA = ct.transform(shelterHPA);
		
		Coord shelterHPALM = new Coord ((double) -155.673607, (double) 20.024664);
		Coord coordHPALM = ct.transform(shelterHPALM);
		
		//Coord coord;
		List<Coord> list = new ArrayList<>(); 
	     
	    list.add(coordHCES);
	    list.add(coordHSH);
	    list.add(coordUN);
	    list.add(coordUHHAF);
	    list.add(coordPACRC);
	    list.add(coordHCC);
	    list.add(coordCCECS);
	    list.add(coordTDKICP);
	    list.add(coordUHH);
	    list.add(coordHPA);
	    list.add(coordHPALM);
	    
	    //coord = getRandomElement(list);
	    return list;
	    
	}
	
	public static Coord getRandomElement(List<Coord> list){ 
        Random rand = new Random(); 
        return list.get(rand.nextInt(list.size())); 
    } 

}
