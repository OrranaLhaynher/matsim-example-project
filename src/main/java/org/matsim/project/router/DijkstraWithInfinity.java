package org.matsim.project.router;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.util.PreProcessDijkstra;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.RouterPriorityQueue;

/**
 * @author Filippo Muzzini
 *
 */
public class DijkstraWithInfinity extends Dijkstra {

	/**
	 * @param network
	 * @param costFunction
	 * @param timeFunction
	 */
	protected DijkstraWithInfinity(Network network, TravelDisutility costFunction, TravelTime timeFunction) {
		super(network, costFunction, timeFunction);
	}
	
	protected DijkstraWithInfinity(final Network network, final TravelDisutility costFunction, final TravelTime timeFunction,
			final PreProcessDijkstra preProcessData) {
		super(network, costFunction, timeFunction, preProcessData);
	}
	
	@Override
	protected boolean addToPendingNodes(final Link l, final Node n,
			final RouterPriorityQueue<Node> pendingNodes, final double currTime,
			final double currCost, final Node toNode) {
		final double travelCost = this.costFunction.getLinkTravelDisutility(l, currTime, this.getPerson(), this.getVehicle());
		if (travelCost == Double.POSITIVE_INFINITY) {
			return false;
		} else {
			return super.addToPendingNodes(l, n, pendingNodes, currTime, currCost, toNode);
		}
	}

}