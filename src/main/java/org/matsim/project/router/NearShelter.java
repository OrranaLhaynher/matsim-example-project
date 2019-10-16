package org.matsim.project.population.California;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.PreProcessDijkstra;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.pt.router.MultiNodeDijkstra;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.project.population.Hawaii.ShelterCoord;
import org.opengis.feature.simple.SimpleFeature;

public class NearShelter{
	
	private static int ID = 1;
	private static final String UTM33N = "EPSG:3311";	
	private static final Logger log = Logger.getLogger(NearShelter.class);
	private static final String exampleDirectory = "C:\\Users\\orran\\Desktop\\TCC\\";

	public static void main(String [] args) throws IOException {
		
		final String NETWORKFILE = exampleDirectory + "network.xml";
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		Network network = scenario.getNetwork();
		
		createPersons(scenario, (int) 10, ct);
		createActivities(scenario, ct, network); //this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\Desktop\\population.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
    }

	private static void createActivities(Scenario scenario, CoordinateTransformation ct, Network network) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need
		List<Coord> coord = new ArrayList<>();
		coord = getShelter(ct);
		int i = 0;
		
		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
	
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am
            
            Coord x = homeAct.getCoord();				
            Node node = NetworkUtils.getNearestNode((network), x); 
        
			Leg leg = pb.createLeg(TransportMode.car);
            plan.addLeg(leg); // there needs to be a log between two activities

			//shelter activity on a random shelter among the shelter set
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord((coord.get(i).getX()), (coord.get(i).getY())));
			double startTime = 10*3600;
			shelt.setStartTime(startTime);
			plan.addActivity(shelt);
			i++;
		}

	}

	private static void createPersons(Scenario scenario, int number, CoordinateTransformation ct) {
	
		Population pop = scenario.getPopulation();
        PopulationFactory pb = pop.getFactory();
        
		Person pers = pb.createPerson(Id.create(ID++, Person.class));
		pop.addPerson(pers);
		Plan plan = pb.createPlan();
		Coord c = new Coord (-148971.01740496838, 189179.24419068824);
		Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
		plan.addActivity(act);
		pers.addPlan(plan);
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
	
	public static Point getShelterPointInFeature(Random rnd, SimpleFeature shelter, CoordinateTransformation ct,
			Activity home, Network network, MatsimClassDijkstra leastCost) {

		Coord x = home.getCoord();				
		Node node = NetworkUtils.getNearestNode((network), x); 
		Node node1 = null;
		List<Path> path = new ArrayList<Path>();
		List<Coord> y = new ArrayList<Coord>();
		y.add(ShelterCoord.getCoord(ct));
		//int pos = 0;
		
		PreProcessDijkstra preProcessData = new PreProcessDijkstra();
		preProcessData.run(network);
		TravelDisutility costFunction = 0;
		TravelTime startTime= 7*3600;
		LeastCostPathCalculator routingAlgo = new DijkstraFactory(network, costFunction, startTime, preProcessData);
		routingAlgo.calcLeastCostPath(node, node1, 7*3600, null, null);
    
	}

}
