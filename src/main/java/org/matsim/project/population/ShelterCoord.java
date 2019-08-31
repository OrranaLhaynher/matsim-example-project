package org.matsim.project.population;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class ShelterCoord {

	private static final String UTM33N = "EPSG:2782";
	
	public static void main(String[] args) {
		List<Coord> coord = getCoordinates();
		System.out.println(coord);
	}

	public static Coord getCoord(CoordinateTransformation ct) {
		
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
	    
	    Coord coord = getRandomElement(list);
	    return coord;
	    
	}
	
	public static Coord getRandomElement(List<Coord> list){ 
        Random rand = new Random(); 
        return list.get(rand.nextInt(list.size())); 
	} 
	
	public static List<Coord> getCoordinates() {
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);

        Coord shelterHCES = new Coord ((double) -155.28821406, (double) 19.41980187);
		Coord coordHCES = ct.transform(shelterHCES); 
		
		Coord shelterHSH = new Coord ((double) -155.086, (double) 19.7055);
		Coord coordHSH = ct.transform(shelterHSH);
		
		Coord shelterUN = new Coord ((double) -154.92, (double) 19.4735);
        Coord coordUN = ct.transform(shelterUN);
        
        List<Coord> list = new ArrayList<>(); 
	     
	    list.add(coordHCES);
	    list.add(coordHSH);
        list.add(coordUN);
        
        return list;
    }

}
