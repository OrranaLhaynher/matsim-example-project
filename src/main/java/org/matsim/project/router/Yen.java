package org.matsim.project.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.matsim.vehicles.Vehicle;

/**
 * @author Filippo Muzzini
 *
 */
public class Yen {
	
	private Path[] A; 
	//private Dijkstra dijkstra;
	private Network network;
	private int K;
	private TravelWrapper travel;
	private TravelTime time;
	
	public Yen(Network net, int K) {
		this.network = net;
		this.K = K;
		this.A = new Path[K];
	}

	/**
	 * @param source
	 * @param dest
	 * @param person 
	 * @param vehicle 
	 * @return 
	 */
	public List<Path> route(Id<Node> source, Id<Node> dest, double startTime, Person person, Vehicle veh, Network network) {
		TravelTime travelTimes = null;
		TravelDisutility travelCosts = null;
		MatsimClassDijkstra least = new MatsimClassDijkstra(network, travelCosts, travelTimes);
		A = new Path[this.K];
		Node s = network.getNodes().get(source);
		Node d = network.getNodes().get(dest);
		Path path = least.calcLeastCostPath(s, d, 0.0, null, null);
		//Path path = this.dijkstra.calcLeastCostPath(s, d, startTime, person, veh);
		
		TreeSet<Path> B = new TreeSet<Path>((p1, p2) -> Double.compare(p1.travelCost, p2.travelCost));
		A[0] = path;
		NetworkWrapper tmpNet = getNetWrap(network);
		for (int k=1; k<K; k++) {
			for (int i=0; i<=A[k-1].nodes.size()-3; i++) {
				Node spurNode = A[k-1].nodes.get(i);
				List<Node> rootPath = A[k-1].nodes.subList(0, i);
				for (Path p : A) {
					if (p != null && i<p.nodes.size() && rootPath.equals(p.nodes.subList(0, i))) {
						Node node1 = network.getNodes().get(p.nodes.get(i).getId());
						Node node2 = network.getNodes().get(p.nodes.get(i+1).getId());
						tmpNet.removeLink(NetworkUtils.getConnectingLink(node1, node2).getId());
						//travel.removeLink(NetworkUtils.getConnectingLink(node1, node2));
					}
				}
				for (Node rootPathNode : rootPath) {
					if (!rootPathNode.equals(spurNode)) {
						//travel.removeNode(rootPathNode);
						tmpNet.removeNode(rootPathNode.getId());
					}
				}
				Path spurPath = least.calcLeastCostPath(s, d, 0.0, null, null);
				//travel.reset();
				tmpNet.reset();
				if (spurPath != null) {
					Path totalPath = getTotalPath(rootPath, spurPath);
					B.add(totalPath);
				}
			}
			
			if (B.isEmpty()) {
				break;
			}
			
			A[k] = B.pollFirst();
		}
		
		return Arrays.asList(A);
	}
	
	private Path getTotalPath(List<Node> rootPath, Path spurPath) {
		ArrayList<Node> totalNodes = new ArrayList<Node>(rootPath);
		totalNodes.addAll(spurPath.nodes);
		
		ArrayList<Link> totalLinks = new ArrayList<Link>();
		for (int i=0; i<totalNodes.size()-1; i++) {
			Node node1 = network.getNodes().get(totalNodes.get(i).getId());
			Node node2 = network.getNodes().get(totalNodes.get(i+1).getId());
			Link link = NetworkUtils.getConnectingLink(node1, node2);
			totalLinks.add(link);
		}
		
		return new Path(totalNodes, totalLinks, 0.0, 0.0);
	}

	private NetworkWrapper getNetWrap(Network network) {		
		//return new NetworkWrapper(network);
		NetworkWrapper newNet = new NetworkWrapper(NetworkUtils.createNetwork());
		for (Node node: network.getNodes().values()) {
			NetworkUtils.createAndAddNode(newNet, node.getId(), node.getCoord());
		}
		for (Link link : network.getLinks().values()) {
			Node newFromNode = newNet.getNodes().get(link.getFromNode().getId());
			Node newToNode = newNet.getNodes().get(link.getToNode().getId());
			NetworkUtils.createAndAddLink(newNet, Id.createLinkId(link.getId()), newFromNode, newToNode, link.getLength(), link.getFreespeed(), link.getCapacity(), link.getNumberOfLanes(), (String)link.getAttributes().getAttribute(NetworkUtils.ORIGID), (String)link.getAttributes().getAttribute(NetworkUtils.TYPE));
		}
		
		return newNet;
	}

	public void setTravelDisutility(TravelDisutility travel) {
		TravelWrapper wrapper = new TravelWrapper(travel);
		this.travel = wrapper;
	}


	public void setTravelTime(TravelTime time) {
		this.time = time;
	}
	
	class TravelWrapper implements TravelDisutility{
		
		private TravelDisutility delegate;
		private HashSet<Link> removedLinks = new HashSet<Link>();
		//private HashSet<Node> removedNodes = new HashSet<Node>();

		public TravelWrapper(TravelDisutility travel) {
			this.delegate = travel;
		}

		/* (non-Javadoc)
		 * @see org.matsim.core.router.util.TravelDisutility#getLinkTravelDisutility(org.matsim.api.core.v01.network.Link, double, org.matsim.api.core.v01.population.Person, org.matsim.vehicles.Vehicle)
		 */
		@Override
		public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
			if (isRemoved(link)) {
				return Double.POSITIVE_INFINITY;
			} else {
				return this.delegate.getLinkTravelDisutility(link, time, person, vehicle);
			}
		}

		/* (non-Javadoc)
		 * @see org.matsim.core.router.util.TravelDisutility#getLinkMinimumTravelDisutility(org.matsim.api.core.v01.network.Link)
		 */
		@Override
		public double getLinkMinimumTravelDisutility(Link link) {
			if (isRemoved(link)) {
				return Double.POSITIVE_INFINITY;
			} else {
				return this.delegate.getLinkMinimumTravelDisutility(link);
			}
		}
		
		public void removeLink(Link link) {
			this.removedLinks.add(link);
		}
		
		public void removeNode(Node node) {
			for (Link l : node.getInLinks().values()) {
				this.removedLinks.add(l);
			}
			for (Link l : node.getOutLinks().values()) {
				this.removedLinks.add(l);
			}
		}
		
		public void reset() {
			this.removedLinks.clear();
		}

		/**
		 * @param link
		 * @return
		 */
		private boolean isRemoved(Link link) {
			return this.removedLinks.contains(link);
		}
	}
	
	class NetworkWrapper implements Network {
		
		private Network delegate;
		private HashSet<Link> removedLinks;
		private HashSet<Node> removedNodes;
		
		public NetworkWrapper(Network network) {
			Logger.getLogger(network.getClass()).setLevel(Level.OFF);
			this.delegate = network;
			this.removedLinks = new HashSet<Link>();
			this.removedNodes = new HashSet<Node>();
		}
		
		
		public void reset() {
			for (Node node : this.removedNodes) {
				this.addNode(node);
			}
			for (Link link : this.removedLinks) {
				this.addLink(link);
			}
			this.removedLinks.clear();
			this.removedNodes.clear();
		}

		/* (non-Javadoc)
		 * @see org.matsim.utils.objectattributes.attributable.Attributable#getAttributes()
		 */
		@Override
		public Attributes getAttributes() {
			return this.delegate.getAttributes();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getFactory()
		 */
		@Override
		public NetworkFactory getFactory() {
			return this.delegate.getFactory();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getNodes()
		 */
		@Override
		public Map<Id<Node>, ? extends Node> getNodes() {
			return this.delegate.getNodes();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getLinks()
		 */
		@Override
		public Map<Id<Link>, ? extends Link> getLinks() {
			return this.delegate.getLinks();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getCapacityPeriod()
		 */
		@Override
		public double getCapacityPeriod() {
			return this.delegate.getCapacityPeriod();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getEffectiveLaneWidth()
		 */
		@Override
		public double getEffectiveLaneWidth() {
			return this.delegate.getEffectiveLaneWidth();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#addNode(org.matsim.api.core.v01.network.Node)
		 */
		@Override
		public void addNode(Node nn) {
			this.delegate.addNode(nn);;
			
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#addLink(org.matsim.api.core.v01.network.Link)
		 */
		@Override
		public void addLink(Link ll) {
			this.delegate.addLink(ll);;
			
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#removeNode(org.matsim.api.core.v01.Id)
		 */
		@Override
		public Node removeNode(Id<Node> nodeId) {
			Node node = this.delegate.removeNode(nodeId);
			if (node != null) {
				this.removedNodes.add(node);
			}
			return node;
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#removeLink(org.matsim.api.core.v01.Id)
		 */
		@Override
		public Link removeLink(Id<Link> linkId) {
			Link link = this.delegate.removeLink(linkId);
			if (link != null) {
				this.removedLinks.add(link);
			}
			return link;
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#setCapacityPeriod(double)
		 */
		@Override
		public void setCapacityPeriod(double capPeriod) {
			this.delegate.setCapacityPeriod(capPeriod);
			
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#setEffectiveCellSize(double)
		 */
		@Override
		public void setEffectiveCellSize(double effectiveCellSize) {
			this.delegate.setEffectiveCellSize(effectiveCellSize);
			
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#setEffectiveLaneWidth(double)
		 */
		@Override
		public void setEffectiveLaneWidth(double effectiveLaneWidth) {
			this.delegate.setEffectiveLaneWidth(effectiveLaneWidth);
			
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#setName(java.lang.String)
		 */
		@Override
		public void setName(String name) {
			this.delegate.setName(name);
			
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getName()
		 */
		@Override
		public String getName() {
			return this.delegate.getName();
		}

		/* (non-Javadoc)
		 * @see org.matsim.api.core.v01.network.Network#getEffectiveCellSize()
		 */
		@Override
		public double getEffectiveCellSize() {
			return this.delegate.getEffectiveCellSize();
		}
		
	}

}