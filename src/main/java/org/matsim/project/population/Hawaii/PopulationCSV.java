package org.matsim.project.population.Hawaii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
//import org.matsim.api.core.v01.network.Network;
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
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class PopulationCSV{
	
	private static final String UTM33N = "EPSG:2782";	
	private static final Logger log = Logger.getLogger(PopulationCSV.class);
	private static final String exampleDirectory = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\artigo\\";
	private static final String csvFile = "C:\\Users\\orran\\Desktop\\allpoints\\nduplicates_before.csv";
	private static final String csvFileD = "C:\\Users\\orran\\Desktop\\allpoints\\duplicates_before.csv";

	public static void main(String [] args) throws IOException {
		
		final String NETWORKFILE = exampleDirectory + "artigo.xml";
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, UTM33N);

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		//Network network = scenario.getNetwork();
		
		
		int columns = 3;
		HashSet<String> hs = new HashSet<String>(); 
		hs = createPersons(scenario, 148, ct, columns);
		createActivities(scenario, hs, 148, ct, columns); //this method creates the remaining activities
		
		String popFilename = "C:\\Users\\orran\\Desktop\\allpoints\\population_before.xml";
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(popFilename); // and finally the population will be written to a xml file
		log.info("population written to: " + popFilename); 
		
    }

	private static void createActivities(Scenario scenario, HashSet<String> hs, int number, CoordinateTransformation ct, int col) {
		
		Population pop =  scenario.getPopulation();
		PopulationFactory pb = pop.getFactory(); //the population builder creates all we need
		String[][] dupl = new String[number][col];
		dupl = CSV.getCSVData(csvFileD, number, col);
		HashSet<String> hp = new HashSet<String>();
		
		for (Person pers : pop.getPersons().values()) { //this loop iterates over all persons
			
			Plan plan = pers.getPlans().get(0); //each person has exactly one plan, that has been created in createPersons(...)
			Activity homeAct = (Activity) plan.getPlanElements().get(0); //every plan has only one activity so far (home activity)
			homeAct.setEndTime(7*3600); // sets the endtime of this activity to 7 am

			Leg leg = pb.createLeg(TransportMode.car);
			plan.addLeg(leg); // there needs to be a log between two activities

			for (int i = 0; i < 148; i++) {
				if(pers.getId().equals(Id.createPersonId(dupl[i][0]))){
					if ((hp.add(dupl[i][1]) && (hp.add(dupl[i][2])))) {
						Coord c = new Coord(Double.parseDouble(dupl[i][1]), Double.parseDouble(dupl[i][2]));
						Coord coord = ct.transform(c);
						Activity mov = pb.createActivityFromCoord("mov", new Coord(coord.getX(), coord.getY()));
						double startTime = 7.5*3600;
						mov.setStartTime(startTime);
						mov.setEndTime(startTime + 1*1800);
						if(!mov.getCoord().equals(homeAct.getCoord())){
							plan.addActivity(mov);
							Leg leg1 = pb.createLeg(TransportMode.car);
							plan.addLeg(leg1); // there needs to be a leg between two activities
						}
					}
				} 
			}

			//shelter activity on a random shelter among the shelter set
			Coord p = getShelterPointInFeature(ct);
			Activity shelt = pb.createActivityFromCoord("shelter", new Coord(p.getX(), p.getY()));
			double startTime = 8*3600;
			shelt.setStartTime(startTime);
			plan.addActivity(shelt);

		}

	}

	private static HashSet<String> createPersons(Scenario scenario, int number, CoordinateTransformation ct, int col) {
	
		Population pop = scenario.getPopulation();
		PopulationFactory pb = pop.getFactory();
		String[][] position = new String[number][col];
		position = CSV.getCSVData(csvFile, number, col);
		HashSet<String> hs = new HashSet<String>(); 
		int i = 0;

		for (; number > 0; number--) {
			Person pers = pb.createPerson(Id.create(position[i][0], Person.class));
			pop.addPerson(pers);
			Plan plan = pb.createPlan();
			Coord c = new Coord(Double.parseDouble(position[i][1]), Double.parseDouble(position[i][2]));
			hs.add(position[i][1]);
			hs.add(position[i][2]);
			Coord coord = ct.transform(c);
			Activity act = pb.createActivityFromCoord("home", new Coord(coord.getX(), coord.getY()));
			plan.addActivity(act);
			pers.addPlan(plan);
			i++;
		}
		return hs;
	}

	public static Coord getShelterPointInFeature(CoordinateTransformation ct) {
		Coord shelterHCES = new Coord (-155.084807, 19.721082);
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
	
		Random rand = new Random(); 
        return list.get(rand.nextInt(list.size()));
	}

}
