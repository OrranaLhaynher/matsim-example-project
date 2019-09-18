package org.matsim.project.population;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Point;
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

public class PopulationMinicurso{
	
	private static final String UTM33N = "EPSG:2782";	
	private static final Logger log = Logger.getLogger(PopulationCSV.class);
	//private static final String exampleDirectory = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\artigo\\";
    private static final String csvFile = "C:\\Users\\orran\\Desktop\\Minicurso\\uniquepoints\\dataset_after.csv";

	public static void main(String [] args) throws IOException {
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        
        int columns = 3;
		createPersons(scenario, 141, ct, columns);
		createActivities(scenario,ct); //this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\Desktop\\Minicurso\\population_after.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
    }

	private static void createActivities(Scenario scenario, CoordinateTransformation ct) {
		
		Population pop =  scenario.getPopulation();
        PopulationFactory pb = pop.getFactory(); //the population builder creates all we need
        
		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
			
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

            //shelter activity on a random shelter among the shelter set
            Point p = getShelterPointInFeature(ct);
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord(p.getX(), p.getY()));
			double startTime = 8*3600;
			shelt.setStartTime(startTime);
			plan.addActivity(shelt);

		}

	}

	private static void createPersons(Scenario scenario, int number, CoordinateTransformation ct, int col) {
	
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

	public static Point getShelterPointInFeature(CoordinateTransformation ct) {

        Coord c = ShelterCoord.getCoord(ct);
        return MGC.coord2Point(c);
    	
    }
	
}
