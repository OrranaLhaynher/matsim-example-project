package org.matsim.project.events;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * This class contains a main method to call the 
 * example event handlers MyEventHandler1-3.
 * 
 * @author dgrether
 */
public class RunEventsHandlingExample {
	
	public static void main(String[] args) {

		//path to events file. For this you first need to run a simulation.
		Config config = ConfigUtils.loadConfig("C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\California\\config.xml");
		config.controler().setLastIteration(5);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        final String NETWORKFILE = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\California\\artigo.xml";
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		Network network = scenario.getNetwork();
		Controler controler = new Controler(scenario);
        controler.run();

		String inputFile = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\California\\output\\output_events.xml.gz";

		//create an event object
		EventsManager events = EventsUtils.createEventsManager();

		//create the handler and add it
		CongestionDetectionEventHandler handler1 = new CongestionDetectionEventHandler(network);
		events.addHandler(handler1);
		
        //create the reader and read the file
		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(inputFile);
		
		System.out.println("Events file read!");
	}

}