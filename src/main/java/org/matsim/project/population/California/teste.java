package org.matsim.project.population.California;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class teste {
    private static final String CaliCoord = "EPSG:3311";
    static CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84,
            CaliCoord);

    public static void main(String[] args) {
        Coord shelterYSF = new Coord(-121.610496, 39.130820);
        System.out.println(shelterYSF);
        Coord coordYSF = ct.transform(shelterYSF);
        System.out.println(coordYSF);
    	Coord shelterBCF = new Coord (-121.684840, 39.366423);
    	Coord coordBCF = ct.transform(shelterBCF);
    	Coord shelterEAC = new Coord (-121.830866, 39.761694);
    	Coord coordEAC = ct.transform(shelterEAC);
    	Coord shelterGCF = new Coord (-122.181001, 39.742266);
    	Coord coordGCF = ct.transform(shelterGCF);
    	Coord shelterSDF = new Coord (-121.812576, 39.717312);;
    	Coord coordSDF = ct.transform(shelterSDF);
    	
    	List<Coord> places = new ArrayList<>();
    			
    	for (int i=0; i<100; i++) {
    	   places.add(coordBCF);
    	}
    	for (int i=0; i<620; i++) {
    	   places.add(coordSDF);
    	}
    	for (int i=0; i<250; i++) {
     	   places.add(coordEAC);
     	}
    	for (int i=0; i<450; i++) {
     	   places.add(coordGCF);
     	}
    	for (int i=0; i<380; i++) {
     	   places.add(coordYSF);
     	}
    
	}
}