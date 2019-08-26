package org.matsim.project.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.utils.gis.matsim2esri.network.CapacityBasedWidthCalculator;
import org.matsim.utils.gis.matsim2esri.network.FeatureGeneratorBuilderImpl;
import org.matsim.utils.gis.matsim2esri.network.Links2ESRIShape;
import org.matsim.utils.gis.matsim2esri.network.PolygonFeatureGenerator;
import org.xml.sax.SAXException;

class NetworkGeneratorHawaii {
	
	public static final String HAWAIICoord = "EPSG:2782";

	public static void main(String [] args) throws SAXException {
		String osm = "C:\\Users\\orran\\Desktop\\ArtigoTeste\\artigo.osm";

		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig()) ;
		Network net = sc.getNetwork();	

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, HAWAIICoord);
		
		OsmNetworkReader onr = new OsmNetworkReader(net,ct); //constrói um novo leitor de openstreetmap 
		onr.setHighwayDefaults(1, "null", 1, 60/3.6, 1, 600); 
		onr.setHighwayDefaults(1, "service", 1, 60/3.6, 1, 600); 
		onr.parse(osm); //começa a conversão de .osm para matsim

		new NetworkCleaner().run(net); //remove links isolados ou não conectados
		String filename = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\artigo\\artigo.xml";
		new NetworkWriter(net).write(filename);

		// Cria um ESRI shape file da rede MATSim

		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(net, HAWAIICoord);
		builder.setWidthCoefficient(0.01);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(CapacityBasedWidthCalculator.class);
		new Links2ESRIShape(net,"C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\artigo\\artigo.shp", builder).write();

	}

}
