package population;

import java.io.IOException;
import java.util.Random;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;
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
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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
public class RunPopulation {

	private static final Logger log = Logger.getLogger(RunPopulation.class);

	private static int ID = 0;

	private static final String exampleDirectory = "C:\\\\Users\\\\orran\\\\Desktop\\\\TentativaMATSim\\\\Network\\\\";

	public static void main(String [] args) throws IOException {

		// input files
		String zonesFile = exampleDirectory + "evacuationArea.shp";
		String networkFile = exampleDirectory + "network.shp";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		SimpleFeatureSource fts = ShapeFileReader.readDataFile(zonesFile); //reads the shape file in
		SimpleFeatureSource net = ShapeFileReader.readDataFile(networkFile);
		
		Random rnd = new Random();
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
		
		createPersons(scenario, ft, rnd, (int) 1800);
		it.close();
		
		createActivities(scenario, rnd, shelter); //this method creates the remaining activities
		in.close();
		
		String popFilename = "C:\\Users\\orran\\Desktop\\\\\\\\TentativaMATSim\\\\\\\\Network\\\\\\\\pop.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
	}

	private static void createActivities(Scenario scenario, Random rnd,  SimpleFeature shelter) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need

		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
			
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

			//work activity on a random link within one of the commercial areas
			Point p = getShelterPointInFeature(rnd, shelter);
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord(p.getX(), p.getY()));
			double startTime = 8*3600;
			shelt.setStartTime(startTime);
			shelt.setEndTime(startTime + 6*3600);
			plan.addActivity(shelt);

		}

	}

	private static void createPersons(Scenario scenario, SimpleFeature ft, Random rnd, int number) {
		
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
	
	private static Point getShelterPointInFeature(Random rnd, SimpleFeature ft) {
		
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent( (Geometry)ft.getDefaultGeometry());
        Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        return MGC.coordinate2Point(coordinate);
        
	}
	
	

}
