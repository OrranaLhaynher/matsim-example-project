package tutorial.population.demandGenerationFromShapefile;

import java.io.IOException;
import java.util.ArrayList;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;

public class PopulationGenerationRandomly {
	
	private static final String UTM33N = "PROJCS[\"WGS_1984_UTM_Zone_33N\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",15],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"Meter\",1]]";
	
	private static final Logger log = Logger.getLogger(PopulationGenerationRandomly.class);

	private static int ID = 0;

	private static final String exampleDirectory = "C:\\Users\\orran\\Desktop\\TentativaMATSim\\Network\\";

	public static void main(String [] args) throws IOException {
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);
		// input files
		String zonesFile = exampleDirectory + "evacuationArea.shp";
		String networkFile = exampleDirectory + "network.shp";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

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
		
		createPersons(scenario, hom, rnd, (int) 1800, ct);
		createActivities(scenario, rnd, shelter, ct); //this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\Desktop\\TentativaMATSim\\Network\\popul.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
	}

	private static void createActivities(Scenario scenario, Random rnd,  SimpleFeature shelter, CoordinateTransformation ct) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need

		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
			
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

			//work activity on a random link within one of the commercial areas
			Point p = getShelterPointInFeature(rnd, ct);
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord(p.getX(), p.getY()));
			double startTime = 8*3600;
			shelt.setStartTime(startTime);
			shelt.setEndTime(startTime + 6*3600);
			plan.addActivity(shelt);

		}

	}

	private static void createPersons(Scenario scenario, SimpleFeature ft, Random rnd, int number, CoordinateTransformation ct) {
		
		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory();
		for (; number > 0; number--) {
			Person pers = pb.createPerson(Id.create(ID++, Person.class));
			pop.addPerson( pers ) ;
			Plan plan = pb.createPlan();
			Point p = getRandomPointInFeature(rnd, ft);
			Activity act = pb.createActivityFromCoord("home", new Coord(p.getX(), p.getY()));
			plan.addActivity(act);
			pers.addPlan( plan ) ;
			
		}
	}

	private static Point getRandomPointInFeature(Random rnd, SimpleFeature ft) {

        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent( (Geometry)ft.getDefaultGeometry());
        Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        return MGC.coordinate2Point(coordinate);
        
	}
	
	private static Point getShelterPointInFeature(Random rnd, CoordinateTransformation ct) {
		
		Coord shelter1 = new Coord ((double) -121.830866, (double) 39.761694);
		Coord shelter2 = new Coord ((double) -121.812576, (double) 39.717312);
		Coord shelter3 = new Coord ((double) -122.181001, (double) 39.742266);
		Coord shelter4 = new Coord ((double) -121.610496, (double) 39.130820);
		Coord shelter5 = new Coord ((double) -121.684840, (double) 39.366423);
		Coord coordinate = ct.transform(shelter1);
		Coord coordinate1 = ct.transform(shelter2);
		Coord coordinate2 = ct.transform(shelter3);
		Coord coordinate3 = ct.transform(shelter4);
		Coord coordinate4 = ct.transform(shelter5);
		Coord coord;
		
		List<Coord> list = new ArrayList<>(); 
        // add 5 element in ArrayList 
        list.add(coordinate);
        list.add(coordinate1);
        list.add(coordinate2);
        list.add(coordinate3);
        list.add(coordinate4);
        
        // take a random element from list and print them 
        coord = getRandomElement(list); 
         
        return MGC.coord2Point(coord);
        
	}
	
    public static Coord getRandomElement(List<Coord> list){ 
        Random rand = new Random(); 
        return list.get(rand.nextInt(list.size())); 
    } 
        
}
