package algorithm;

import java.nio.file.Path;
import java.util.*;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import com.vividsolutions.jts.geomgraph.Edge;

/**
 * Yen's algorithm for computing the K shortest loopless paths between two nodes in a graph.
 *
 * Copyright (C) 2015  Brandon Smock (dr.brandon.smock@gmail.com, GitHub: bsmock)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by Brandon Smock on September 23, 2015.
 * Last updated by Brandon Smock on December 24, 2015.
 */
public final class Yen implements LeastCostPathCalculator {

    	private final Network network;
    	private Map<Id<Node>,Double> costToNode = new HashMap<Id<Node>, Double>(); // custo para o nó
    	private Map<Id<Node>,Id<Node>> previousNodes = new HashMap<Id<Node>, Id<Node>>(); // nós anteriores
    	PriorityQueue<Id<Node>> queue = new PriorityQueue<Id<Node>>(11, new Comparator<Id<Node>>() {

    		@Override
    		public int compare(Id<Node> o1, Id<Node> o2) {
    			return costToNode.get(o1).compareTo(costToNode.get(o2)); //compara o custo de dois nós diferentes
    		}

    	});
    	Yen(Network network, TravelDisutility travelCosts,
    			TravelTime travelTimes) {
    		this.network = network;

    	}

    	@Override
    	public Path calcLeastCostPath(Node fromNode, Node toNode, double starttime,
    			Person person, Vehicle vehicle) {

    		initializeNetwork(fromNode.getId()); //inicializa rede

    		while (!queue.isEmpty()) {
    			Id<Node> currentId = queue.poll(); //id atual recebe head da fila
    			if (currentId == toNode.getId()) return createPath(toNode.getId(),fromNode.getId()); //se id atual for igual id do destino, retorna createPath
    			Node currentNode = network.getNodes().get(currentId); //nó atual recebe nó do id atual na rede
    			for (Link link:  currentNode.getOutLinks().values()){
    				Node currentToNode = link.getToNode();
    				double distance = link.getLength() + this.costToNode.get(currentId);
    				if (distance < this.costToNode.get(currentToNode.getId())){
    					this.costToNode.put(currentToNode.getId(), distance);
    					update(currentToNode.getId());
    					this.previousNodes.put(currentToNode.getId(), currentId);
    				}
    			}
    		}

    		return null;
    	}

    	private Path createPath(Id<Node> toNodeId, Id<Node> fromNodeId) {
    		List<Node> nodes = new ArrayList<Node>();
    		List<Link> links = new ArrayList<Link>();
    		Node lastNode = network.getNodes().get(toNodeId);
    		while (!lastNode.getId().equals(fromNodeId)){
    			if (!lastNode.getId().equals(toNodeId)) 
    				nodes.add(0, lastNode);
    			Node newLastNode = network.getNodes().get(this.previousNodes.get(lastNode.getId()));
    			Link l = NetworkUtils.getConnectingLink(newLastNode,lastNode);
    			links.add(0, l);
    			lastNode = newLastNode;
    		}


    		return new Path(nodes,links,0.0,0.0);
    	}

    	private void initializeNetwork(Id<Node> startNode) {
    		for (Node node : network.getNodes().values()){
    			this.costToNode.put(node.getId(), Double.POSITIVE_INFINITY);
    			this.previousNodes.put(node.getId(), null);
    		}
    		this.costToNode.put(startNode, 0.0);
    		this.queue.add(startNode);

    	}
    	
    	private void update(Id<Node> nodeToUpdate){
    		this.queue.remove(nodeToUpdate);
    		this.queue.add(nodeToUpdate);
    	}
   
	    public List<Path> ksp_v2(Network network, Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle, int K) {
	        // Initialize containers for candidate paths and k shortest paths
	        ArrayList<Path> ksp = new ArrayList<Path>();
	        PriorityQueue<Path> candidates = new PriorityQueue<Path>();
	
	        try {
	            /* Compute and add the shortest path */
	            Path kthPath = calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);
	            ksp.add(kthPath);
	
	            /* Iteratively compute each of the k shortest paths */
	            for (int k = 1; k < K; k++) {
	                // Get the (k-1)st shortest path
	                Path previousPath = ksp.get(k-1);
	
	                /* Iterate over all of the nodes in the (k-1)st shortest path except for the target node; for each node,
	                   (up to) one new candidate path is generated by temporarily modifying the graph and then running
	                   Dijkstra's algorithm to find the shortest path between the node and the target in the modified
	                   graph */
	                LinkedList<Link> rootPathLinks = new LinkedList<Link>();
	                Iterator<Link> it = previousPath.links.iterator();
	                
	                for (int i = 0; i < previousPath.links.size(); i++) {
	                    if (i > 0) {
	                        rootPathLinks.add(it.next());
	                    }
	
	                    // Initialize container to store the edited (removed) links
	                    LinkedList<Link> removedLinks = new LinkedList<Link>();
	
	                    // Spur node = currently visited node in the (k-1)st shortest path
	                    Node spurNode = previousPath.links.get(i).getFromNode();
	
	                    // Root path = prefix portion of the (k-1)st path up to the spur node
	                    // REFACTOR THIS
	                    Path rootPath = (Path) previousPath.nodes.get(i);
	
	                    /* Iterate over all of the (k-1) shortest paths */
	                    for(Path p:ksp) {
	                    
	                        int pSize = p.links.size();
	                        if (pSize < i)
	                            continue;
	                        boolean rootMatch = true;
	                        for (int rootPos = 0; rootPos < i; rootPos++) {
	                            if (!p.links.get(rootPos).equals(rootPathLinks.get(rootPos))) {
	                                rootMatch = false;
	                                break;
	                            }
	                        }
	                        // Check to see if this path has the same prefix/root as the (k-1)st shortest path
	                        if (rootMatch) {
	                            /* If so, eliminate the next edge in the path from the graph (later on, this forces the spur
	                               node to connect the root path with an un-found suffix path) */
	                            Link re = p.links.get(i);
	                            network.removeLink(re.getId());
	                            removedLinks.add(re);
	                        }
	                    }
	
	                    /* Temporarily remove all of the nodes in the root path, other than the spur node, from the graph */
	                    for(Link rootPathEdge : rootPathLinks) {
	                        Node rn = rootPathEdge.getFromNode();
	                        if (!rn.equals(spurNode)) {
	                        	removedLinks.addAll((Collection<? extends Link>) network.removeNode(rn.getId()));
	                        }
	                    }
	
	                    // Spur path = shortest path from spur node to target node in the reduced graph
	                    Path spurPath = calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);;
	
	                    // If a new spur path was identified...
	                    if (spurPath != null) {
	                        // Concatenate the root and spur paths to form the new candidate path
	                        Path totalPath = rootPath;
	                        totalPath.links.add((Link) spurPath);

	                        // If candidate path has not been generated previously, add it
	                        if (!candidates.contains(totalPath))
	                            candidates.add(totalPath);
	                    }

	
	                    // Restore removed links
	                    network.addLink((Link) removedLinks);
	                }
	
	                /* Identify the candidate path with the shortest cost */
	                boolean isNewPath;
	                do {
	                    kthPath = candidates.poll();
	                    isNewPath = true;
	                    if (kthPath != null) {
	                        for (Path p : ksp) {
	                            // Check to see if this candidate path duplicates a previously found path
	                            if (p.equals(kthPath)) {
	                                isNewPath = false;
	                                break;
	                            }
	                        }
	                    }
	                } while(!isNewPath);
	
	                // If there were not any more candidates, stop
	                if (kthPath == null)
	                    break;
	
	                // Add the best, non-duplicate candidate identified as the k shortest path
	                ksp.add(kthPath);
	            }
	        } catch (Exception e) {
	            System.out.println(e);
	            e.printStackTrace();
	        }
	
	        // Return the set of k shortest paths
	        return ksp;
	    }
	   
}
