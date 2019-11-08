package org.matsim.core.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
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
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.project.router.MatsimClassDijkstra;
import org.matsim.vehicles.Vehicle;
import org.opengis.feature.simple.SimpleFeature;

public class NearShelterLotation{

	private static int ID = 1;
	private static final String Cali = "EPSG:3311";
	private static final String exampleDirectory = "C:\\Users\\orran\\Desktop\\TCC\\areaScheduling\\";
	private static final Logger log = Logger.getLogger(NearShelterLotation.class);

	public static void main(String[] args) throws IOException {

		final String NETWORKFILE = "C:\\Users\\orran\\Desktop\\TCC\\network.xml";
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, Cali);

		// input files
		String zonesFile1 = exampleDirectory + "area1.shp";
		String zonesFile2 = exampleDirectory + "area2.shp";
		String zonesFile3 = exampleDirectory + "area3.shp";
		String zonesFile4 = exampleDirectory + "area4.shp";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		Network network = scenario.getNetwork();

		SimpleFeatureSource area1 = ShapeFileReader.readDataFile(zonesFile1); // reads the shape file in
		SimpleFeatureSource area2 = ShapeFileReader.readDataFile(zonesFile2);
		SimpleFeatureSource area3 = ShapeFileReader.readDataFile(zonesFile3);
		SimpleFeatureSource area4 = ShapeFileReader.readDataFile(zonesFile4);

		SimpleFeature home1 = null;
		SimpleFeature home2 = null;
		SimpleFeature home3 = null;
		SimpleFeature home4 = null;

		// Iterator to iterate over the features from the shape file
		SimpleFeatureIterator it1 = area1.getFeatures().features();
		SimpleFeatureIterator it2 = area2.getFeatures().features();
		SimpleFeatureIterator it3 = area3.getFeatures().features();
		SimpleFeatureIterator it4 = area4.getFeatures().features();

		ArrayList<SimpleFeature> t1 = new ArrayList<SimpleFeature>();
		ArrayList<SimpleFeature> t2 = new ArrayList<SimpleFeature>();
		ArrayList<SimpleFeature> t3 = new ArrayList<SimpleFeature>();
		ArrayList<SimpleFeature> t4 = new ArrayList<SimpleFeature>();

		while (it1.hasNext()) {
			home1 = it1.next();
			Geometry gm = (Geometry) home1.getDefaultGeometry();
			home1.setDefaultGeometry(gm);
			t1.add(home1);
		}

		while (it2.hasNext()) {
			home2 = it2.next();
			Geometry gm2 = (Geometry) home2.getDefaultGeometry();
			home2.setDefaultGeometry(gm2);
			t2.add(home2);
		}

		while (it3.hasNext()) {
			home3 = it3.next();
			Geometry gm3 = (Geometry) home3.getDefaultGeometry();
			home3.setDefaultGeometry(gm3);
			t3.add(home3);
		}

		while (it4.hasNext()) {
			home4 = it4.next();
			Geometry gm4 = (Geometry) home4.getDefaultGeometry();
			home4.setDefaultGeometry(gm4);
			t4.add(home4);
		}

		it1.close();
		it2.close();
		it3.close();
		it4.close();

		createPersons(scenario, t1, t2, t3, t4, (int) 1800, ct);
		createActivities(scenario, ct, network); // this method creates the remaining activities

		String popFilename = "C:\\Users\\orran\\Desktop\\TCC\\population1.xml.gz";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename);

	}

	private static void createPersons(Scenario scenario, ArrayList<SimpleFeature> t1, ArrayList<SimpleFeature> t2, ArrayList<SimpleFeature> t3, ArrayList<SimpleFeature> t4, int number, CoordinateTransformation ct) {

		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory();

		for (number=0; number<450; number++) {
			// 37:00 minutos de diferença entre o fim das atividades home

			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t1);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			act.setEndTime(7.23*3600); //07:23  7.383333333333333
			plan.addActivity(act);
			pers.addPlan(plan);
		}
		for (number= 450; number<900; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t2);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			plan.addActivity(act);
			act.setEndTime(7.45*3600);  //08:00
			pers.addPlan(plan);
		}
		for (number= 900; number<1350; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t3);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			plan.addActivity(act);
			act.setEndTime(8*3600); //08:37
			pers.addPlan(plan);
		}
		for (number= 1350; number<1800; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t4);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			plan.addActivity(act);
			act.setEndTime(8.40*3600); //09:14 9.233333333333333‬
			pers.addPlan(plan);
		}
	}

	private static void createActivities(Scenario scenario, CoordinateTransformation ct, Network network) {

		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); // the population builder creates all we need
		Coord shelter = new Coord();
		List<Node> coord = new ArrayList<>();
		Node[] y = getShelter(ct, network);
		coord = getShelterLotation(y, network);

		for (Person pers : pop.getPersons().values()) { // this loop iterates over all persons
			Plan plan = pers.getPlans().get(0); // each person has exactly one plan, that has been created in createPersons(...)

			List<Integer> valuesList = getNearShelterPointInFeature(ct, scenario, network, (Activity) plan.getPlanElements().get(0));
			int key = valuesList.get(0);
			int key1 = valuesList.get(1);
			int key2 = valuesList.get(2);
			int key3 = valuesList.get(3);
			int key4 = valuesList.get(4);

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities
			
			if (coord.contains(y[key])) {
				coord.remove(y[key]);
				shelter = y[key].getCoord();
			}else if ((!coord.contains(y[key])) && (coord.contains(y[key1]))){
				coord.remove(y[key1]);
				shelter = y[key1].getCoord();
			}else if((!coord.contains(y[key])) && (!coord.contains(y[key1])) && (coord.contains(y[key2]))) {
				coord.remove(y[key2]);
				shelter = y[key2].getCoord();
			}else if((!coord.contains(y[key])) && (!coord.contains(y[key1])) && (!coord.contains(y[key2])) && (coord.contains(y[key3]))) {
				coord.remove(y[key3]);
				shelter = y[key3].getCoord();
			}else if((!coord.contains(y[key])) && (!coord.contains(y[key1])) && (!coord.contains(y[key2])) && (!coord.contains(y[key3])) && (coord.contains(y[key4]))) {
				coord.remove(y[key4]);
				shelter = y[key4].getCoord();
			}

			// shelter activity on a random shelter among the shelter set
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord((shelter.getX()), (shelter.getY())));
			double startTime = 7.25*3600;
			shelt.setStartTime(startTime);
			plan.addActivity(shelt);
		}

	}

	public static Coord getCoordInGeometry(ArrayList<SimpleFeature> t) {
		Iterator<SimpleFeature> iter = t.iterator();
		Collections.shuffle(t);

		RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
		randomPointsBuilder.setNumPoints(1);
		randomPointsBuilder.setExtent((Geometry) iter.next().getDefaultGeometry());
		Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
		return MGC.coordinate2Coord(coordinate);
	}

	public static List<Integer> getNearShelterPointInFeature(CoordinateTransformation ct, Scenario scenario, Network network, Activity homeAct) {
	
		Map<Double, Integer> travelLength = new LinkedHashMap<Double, Integer>();
		Map<Integer, Path> path = new LinkedHashMap<Integer, Path>();
		TravelTime travelTimes = null;
		TravelDisutility travelCosts = null;
		Node[] y = getShelter(ct, network);
		MatsimClassDijkstra least = new MatsimClassDijkstra(network, travelCosts, travelTimes);

		Coord x = homeAct.getCoord();
		Node node = NetworkUtils.getNearestNode(network, x);


		for (int i = 0; i < y.length; i++) {
			Node node1 = y[i];
			path.put(i, least.calcLeastCostPath(node, node1, 0.0, null, null));
		}

		for (int i = 0; i < path.size(); i++) {
			travelLength.put(getLinkTravelDisutility(path.get(i).links, 0.0, null, null), i);
		}

		final Map<Double, Integer> sortedByCount = travelLength.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		List<Integer> valuesList = new ArrayList<Integer>(sortedByCount.values());
	
		return valuesList;
	}

	public static double getLinkTravelDisutility(List<Link> link, double time, Person person, Vehicle vehicle) {
		double length = 0;
		for (Link link1 : link) {
			length = link1.getLength() + length;
		}
		return length;
	}

	public static Node[] getShelter (CoordinateTransformation ct, Network network){

		Coord shelterYSF = new Coord ((double) -121.610496, (double) 39.130820);
		Coord coordYSF = ct.transform(shelterYSF);
		Node YSF = NetworkUtils.getNearestNode(network, coordYSF);

    	Coord shelterBCF = new Coord ((double) -121.684840, (double) 39.366423);
		Coord coordBCF = ct.transform(shelterBCF);
		Node BCF = NetworkUtils.getNearestNode(network, coordBCF);

    	Coord shelterEAC = new Coord ((double) -121.830866, (double) 39.761694);
		Coord coordEAC = ct.transform(shelterEAC);
		Node EAC = NetworkUtils.getNearestNode(network, coordEAC);

    	Coord shelterGCF = new Coord ((double) -122.181001, (double) 39.742266);
		Coord coordGCF = ct.transform(shelterGCF);
		Node GCF = NetworkUtils.getNearestNode(network, coordGCF);

    	Coord shelterSDF = new Coord ((double) -121.812576, (double) 39.717312);;
		Coord coordSDF = ct.transform(shelterSDF);
		Node SDF = NetworkUtils.getNearestNode(network, coordSDF);
		
		Node[] y = new Node[5];

		y[0] = EAC;
		y[1] = GCF;
		y[2] = SDF;
		y[3] = YSF;
		y[4] = BCF;

		return y;
	}

	public static List<Node> getShelterLotation(Node[] list, Network network) {
    	
    	List<Node> places = new ArrayList<>();
    			
    	for (int i=0; i<620; i++) {
			places.add(list[2]);
		}
		for (int i=0; i<250; i++) {
     	   places.add(list[0]);
     	}
		 for (int i=0; i<100; i++) {
    	   places.add(list[4]);
    	}
    	for (int i=0; i<450; i++) {
     	   places.add(list[1]);
     	}
    	for (int i=0; i<380; i++) {
     	   places.add(list[3]);
     	}
    	
    	//Collections.shuffle(places); //fazer teste sobre essa linha (acho que no script atual é desnecessária)
    	
    	return places;
	}
	
}