/* *********************************************************************** *
 * project: org.matsim.*																															*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.project.population;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

/**
 * This class generates a simple artificial MATSim demand for
 * the german city Löbau. This is similar to the tutorial held
 * at the MATSim user meeting 09 by glaemmel, however based on
 * the matsim api.
 *
 * The files needed to run this tutorial are placed in the matsim examples
 * repository that can be found in the root directory of the matsim
 * sourceforge svn under the path matsimExamples/tutorial/example8DemandGeneration.
 *
 * @author glaemmel
 * @author dgrether
 *
 */

public class RunPopulationMaximumCapacityShelters {
	
	private static final String UTM33N = "PROJCS[\"WGS_1984_UTM_Zone_33N\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",15],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"Meter\",1]]";

	private static final Logger log = Logger.getLogger(RunPopulationMaximumCapacityShelters.class);

	private static int ID = 0;

	private static final String exampleDirectory = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\";

	public static void main(String [] args) throws IOException {
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);
		// input files
		String zonesFile = exampleDirectory + "evacuationArea.shp";
		String networkFile = exampleDirectory + "network.shp";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		//reads the shape file in
		SimpleFeatureSource fts = ShapeFileReader.readDataFile(zonesFile);
		SimpleFeatureSource net = ShapeFileReader.readDataFile(networkFile);
		
		SimpleFeature ft = null;
		SimpleFeature shelter = null;

		//Iterator to iterate over the features from the shape file
		SimpleFeatureIterator it = fts.getFeatures().features();
		SimpleFeatureIterator in = net.getFeatures().features();
		
		while (it.hasNext()) {
			ft = it.next(); 
		}
		
		while (in.hasNext()) {
			shelter = in.next();
		}
		
		it.close();
		in.close();
		
		createPersons(scenario, ft, shelter, (int) 1800, ct); //this method creates the remaining activities
		createActivities(scenario, shelter, ct);//this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\population.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
	}

	private static void createPersons(Scenario scenario, SimpleFeature ft, SimpleFeature shelter, int number, CoordinateTransformation ct) {
		
		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory();
		for (; number > 0; number--) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson( pers ) ;
			Plan plan = pb.createPlan();
			org.locationtech.jts.geom.Point p = getRandomPointInFeature(ft);
			Coord c = MGC.point2Coord(p);
			c = ct.transform(c);
			Activity act = pb.createActivityFromCoord("home", new Coord(p.getX(), p.getY()));
			plan.addActivity(act);
			pers.addPlan( plan ) ;
		}
	}

	private static void createActivities(Scenario scenario, SimpleFeature shelter, CoordinateTransformation ct) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need
		int i = 0;
		List<Coord> coord = new ArrayList<>();
		coord = getShelter(ct);
		
		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
			
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

			//work activity on a random link within one of the commercial areas
			//Coord p = getRandomElement(coord);
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord((coord.get(i).getX()), (coord.get(i).getY()))); // sets the coordinates of shelters
			double startTime = 8*3600;
			shelt.setStartTime(startTime);
			shelt.setEndTime(startTime + 6*3600);
			plan.addActivity(shelt);	
			i++;
		}

	}

	private static org.locationtech.jts.geom.Point getRandomPointInFeature(SimpleFeature ft) {
        org.locationtech.jts.shape.random.RandomPointsBuilder randomPointsBuilder = new org.locationtech.jts.shape.random.RandomPointsBuilder(
				new org.locationtech.jts.geom.GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent( (org.locationtech.jts.geom.Geometry) ft.getDefaultGeometry());
        org.locationtech.jts.geom.Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        return MGC.coordinate2Point(coordinate);
	}
	
    public static Coord getRandomElement(List<Coord> list){ 
        Random rand = new Random(); 
        return list.get(rand.nextInt(list.size())); 
    } 
    
    public static List<Coord> getShelter(CoordinateTransformation ct) {
    	Coord shelterYSF = new Coord ((double) -121.610496, (double) 39.130820);
    	Coord coordYSF = ct.transform(shelterYSF);
    	Coord shelterBCF = new Coord ((double) -121.684840, (double) 39.366423);
    	Coord coordBCF = ct.transform(shelterBCF);
    	Coord shelterEAC = new Coord ((double) -121.830866, (double) 39.761694);
    	Coord coordEAC = ct.transform(shelterEAC);
    	Coord shelterGCF = new Coord ((double) -122.181001, (double) 39.742266);
    	Coord coordGCF = ct.transform(shelterGCF);
    	Coord shelterSDF = new Coord ((double) -121.812576, (double) 39.717312);;
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
    	
    	Collections.shuffle(places); 
    	
    	return places;
    }

}
