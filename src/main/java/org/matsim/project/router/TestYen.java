package org.matsim.project.router;

import java.util.List;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * Test of Yen's algorithm for computing the K shortest loopless paths between two nodes in a graph.
 */

public class TestYen {

    public static void main(String args[]) {
        /* Uncomment any of these example tests */
        int K = 4;
        Scenario scenario;
        final String NETWORKFILE = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\grid_network.xml";
        
        scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		Network net; 
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		net = scenario.getNetwork();

        usageExample1(net,K);
    }

    public static void usageExample1(Network network, int k) {
    	
        /* Read graph from file */
        System.out.print("Reading data from file... ");
        System.out.println("complete.");

        /* Compute the K shortest paths and record the completion time */
        System.out.print("using Yen's algorithm... ");
        List<Path> ksp;
        long timeStart = System.currentTimeMillis();
        Yen yenAlgorithm = new Yen(network, null, null);
		ksp = yenAlgorithm.ksp_v2(network, network.getNodes().get(Id.createNodeId(21)), network.getNodes().get(Id.createNodeId(16)), 0, null, null, k);
        long timeFinish = System.currentTimeMillis();
        System.out.println("complete.");

        System.out.println("Operation took " + (timeFinish - timeStart) / 1000.0 + " seconds.");

        /* Output the K shortest paths */
        System.out.println("k) cost: [path]");
        int n = 0;
        for (Path p : ksp) {
            System.out.println(++n + ") " + p.links + "\n");
            System.out.println("\n"+ksp.size());
        }
    }
}
