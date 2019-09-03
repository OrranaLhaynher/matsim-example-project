package org.matsim.project.population;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

public class PopulationNearShelter{
	
	private static final String UTM33N = "EPSG:2782";	
	private static final Logger log = Logger.getLogger(PopulationNearShelter.class);
	private static final String exampleDirectory = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\artigo\\";
	private static final String csvFile = "C:\\Users\\orran\\Desktop\\ArtigoTeste\\dataset_after.csv";
	
	public static void main(String [] args) throws IOException {
		
		final String NETWORKFILE = exampleDirectory + "artigo.xml";
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);
		// input files
		String zonesFile = exampleDirectory + "area.shp";
		String networkFile = exampleDirectory + "artigo.shp";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		Network network = scenario.getNetwork();
		
		MatsimClassDijkstra leastCost = new MatsimClassDijkstra(network, null, null);
		
		SimpleFeatureSource home = ShapeFileReader.readDataFile(zonesFile); //reads the shape file in
		SimpleFeatureSource net = ShapeFileReader.readDataFile(networkFile);
		
		Random rnd = new Random();
		SimpleFeature hom = null;
		SimpleFeature shelter = null;

		//Iterator to iterate over the features from the shape file
		SimpleFeatureIterator it = home.getFeatures().features();
		SimpleFeatureIterator in = net.getFeatures().features();
		
		while (in.hasNext()) {
			shelter = in.next();
		}
		while (it.hasNext()) {
			hom = it.next(); 
		}
		
		in.close();
		it.close();
		
		int columns = 3;
		createPersons(scenario, hom, rnd, (int) 27, ct, columns);
		createActivities(scenario, rnd, shelter, ct, network, leastCost); //this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\artigo\\population_after.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
    }

	private static void createActivities(Scenario scenario, Random rnd,  SimpleFeature shelter, CoordinateTransformation ct, Network network, MatsimClassDijkstra leastCost) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need
		String[] id = new String[3];
		id[0] = "10699104971101079910497110";
		id[1] = "111100100109971101111171165553";
		id[2] = "99111103100101115105103110";

		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
			
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

			for (int i = 0; i < 3; i++) {
				if(pers.getId().equals(Id.createPersonId(id[i]))){
					List<Coord> coord = ShelterCoord.getCoordinates();
					Activity mov = pb.createActivityFromCoord("mov", new Coord(coord.get(i).getX(), coord.get(i).getY()));
					double startTime = 7.5*3600;
					mov.setStartTime(startTime);
					mov.setEndTime(startTime + 1*1800);
					plan.addActivity(mov);
	
					Leg leg1 = pb.createLeg(TransportMode.car);
					plan.addLeg(leg1); // there needs to be a log between two activities
				} 
			}

			//shelter activity on a random shelter among the shelter set
			org.locationtech.jts.geom.Point p = getShelterPointInFeature(rnd, shelter, ct, homeAct, network, leastCost);
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord(p.getX(), p.getY()));
			double startTime = 8*3600;
			shelt.setStartTime(startTime);
			plan.addActivity(shelt);

		}

	}

	private static void createPersons(Scenario scenario, SimpleFeature ft, Random rnd, int number, CoordinateTransformation ct, int col) {
	
		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory();
		String[][] position = new String[number][col];
		position = CSV.getCSVData(csvFile, number, col);
		int i = 0;

		for (; number > 0; number--) {
			Person pers = pb.createPerson(Id.create(position[i][0], Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = new Coord(Double.parseDouble(position[i][1]), Double.parseDouble(position[i][2]));
			Coord coord = ct.transform(c);
			Activity act = pb.createActivityFromCoord("home", new Coord(coord.getX(), coord.getY()));
			plan.addActivity(act);
			pers.addPlan(plan);
			i++;
		}
	}

	public static Point getShelterPointInFeature(Random rnd, SimpleFeature shelter, CoordinateTransformation ct,
			Activity home, Network network, MatsimClassDijkstra leastCost) {

		/*Coord x = home.getCoord();				
		Node node = NetworkUtils.getNearestNode((network), x); 
		Node node1 = null;
		List<Path> path = new ArrayList<Path>();
		List<Coord> y = ShelterCoord.getCoord(ct);
		int pos = 0;
		
		for (int i = 0; i < y.size(); i++) {
    		node1 = NetworkUtils.getNearestNode((network), y.get(i)); 
    		Path p = leastCost.calcLeastCostPath(node, node1, 0, null, null);
    		path.add(p);
		}*/
    	
		//System.out.println(path);
		Coord c = ShelterCoord.getCoord(ct);
		return MGC.coord2Point(c);
    	
	}

}
