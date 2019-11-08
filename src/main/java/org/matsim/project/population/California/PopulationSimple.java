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

public class PopulationSimple{
	
	private static int ID = 1;
	private static final String UTM33N = "EPSG:3311";	
	private static final Logger log = Logger.getLogger(PopulationSimple.class);
	private static final String exampleDirectory = "C:\\Users\\orran\\Desktop\\TCC\\areaScheduling\\";

	public static void main(String [] args) throws IOException {
		
		final String NETWORKFILE = "C:\\Users\\orran\\Desktop\\TCC\\network.xml";
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);
		// input files
		String zonesFile1 = exampleDirectory + "area1.shp";
		String zonesFile2 = exampleDirectory + "area2.shp";
		String zonesFile3 = exampleDirectory + "area3.shp";
		String zonesFile4 = exampleDirectory + "area4.shp";
		String networkFile = "C:\\Users\\orran\\Desktop\\TCC\\network.shp";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		Network network = scenario.getNetwork();
		
		SimpleFeatureSource area1 = ShapeFileReader.readDataFile(zonesFile1); //reads the shape file in
		SimpleFeatureSource area2 = ShapeFileReader.readDataFile(zonesFile2);
		SimpleFeatureSource area3 = ShapeFileReader.readDataFile(zonesFile3);
		SimpleFeatureSource area4 = ShapeFileReader.readDataFile(zonesFile4);
		SimpleFeatureSource net = ShapeFileReader.readDataFile(networkFile);
		
		Random rnd = new Random();
		SimpleFeature home1 = null;
		SimpleFeature home2 = null;
		SimpleFeature home3 = null;
		SimpleFeature home4 = null;
		SimpleFeature shelter = null;

		//Iterator to iterate over the features from the shape file
		SimpleFeatureIterator it1 = area1.getFeatures().features();
		SimpleFeatureIterator it2 = area2.getFeatures().features();
		SimpleFeatureIterator it3 = area3.getFeatures().features();
		SimpleFeatureIterator it4 = area4.getFeatures().features();
		SimpleFeatureIterator in = net.getFeatures().features();

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
		
		while (in.hasNext()) {
			SimpleFeature sh = in.next();
			shelter = sh;
		}
		
		it1.close();
		it2.close();
		it3.close();
		it4.close();
		in.close();
		
		createPersons(scenario, t1, t2, t3, t4, rnd, (int) 1800, ct);
		createActivities(scenario, rnd, shelter, ct, network); //this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\Desktop\\TCC\\populationSimple1.xml.gz";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
    }

	private static void createActivities(Scenario scenario, Random rnd,  SimpleFeature shelter, CoordinateTransformation ct, Network network) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need

		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
	
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			//Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			//homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

			Point p = getShelter(ct);

			//shelter activity on a random shelter among the shelter set
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord(p.getX(), p.getY()));
			double startTime = 7.25*3600;
			shelt.setStartTime(startTime);
			plan.addActivity(shelt);
		}

	}

	private static void createPersons(Scenario scenario, ArrayList<SimpleFeature> t1, ArrayList<SimpleFeature> t2, ArrayList<SimpleFeature> t3, ArrayList<SimpleFeature> t4, Random rnd, int number, CoordinateTransformation ct) {
	
		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory();

		for (number=0; number<450; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t1);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			act.setEndTime(7.23*3600);
			plan.addActivity(act);
			pers.addPlan(plan);
		}
		for (number= 450; number< 900; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t2);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			plan.addActivity(act);
			act.setEndTime(7.45*3600);
			pers.addPlan(plan);
		}
		for (number= 900; number<1350; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t3);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			plan.addActivity(act);
			act.setEndTime(8*3600);
			pers.addPlan(plan);
		}
		for (number= 1350; number<1800; number++) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = getCoordInGeometry(t4);
			Activity act = pb.createActivityFromCoord("home", new Coord(c.getX(), c.getY()));
			plan.addActivity(act);
			act.setEndTime(8.40*3600);
			pers.addPlan(plan);
		}
	}

	public static Coord getCoordInGeometry(ArrayList<SimpleFeature> t) {
		Iterator<SimpleFeature> iter = t.iterator(); 
        Collections.shuffle(t); 
    	
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent( (Geometry)iter.next().getDefaultGeometry());
		Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        return MGC.coordinate2Coord(coordinate);
	}

	public static Point getShelter(CoordinateTransformation ct) {
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
    	
		List<Coord> list = new ArrayList<>(); 

        // add 5 element in ArrayList 
        list.add(coordYSF);
        list.add(coordBCF);
        list.add(coordEAC);
        list.add(coordGCF);
        list.add(coordSDF);
        
        // take a random element from list and print them 
		Random rand = new Random(); 
         
        return MGC.coord2Point(list.get(rand.nextInt(list.size())));
        
	}

}
