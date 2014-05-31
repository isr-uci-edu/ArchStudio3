/*
 * Created on Nov 22, 2005
 *
 */
package archstudio.comp.aac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class InterfaceGraph {
    // This stores all nodes, which are interfaces of ADL
    Set			allNodes = new HashSet();
    // sorted, because we need a name to index mapping
    // This stores the direct connected interfaces
    SortedMap	linked = new TreeMap();
    // This stores the indirectly connected interfaces
    SortedMap	closure = new TreeMap();
    // List of edges
    Set			allEdges = new HashSet();
    
    public static class Edge {
    	String		from;
    	String		to;
    	
    	public Edge(String from, String to) {
    		this.from = from;
    		this.to = to;
    	}
    }
    
    public InterfaceGraph() {
        
    }
    
    /**
     * Add an edge
     * 
     * @param fromInterface		the starting node (an interface)
     * @param toInterface		the ending node (an interface)
     */
    public void addEdge(String fromInterface, String toInterface) {
        SortedSet	interfaceTo;
        if (linked.containsKey(fromInterface)) {
            interfaceTo = (SortedSet)linked.get(fromInterface);
        }
        else {
            interfaceTo = new TreeSet();
            linked.put(fromInterface, interfaceTo);
        }
        interfaceTo.add(toInterface);
        allNodes.add(fromInterface);
        allNodes.add(toInterface);
        allEdges.add(new Edge(fromInterface, toInterface));
    }
    
    /**
     * Add all edges of another graph
     * 
     * @param ig 		another graph
     * @param prefix	a prefix for the names of the newly added edges
     */
    public void addEges(InterfaceGraph ig, String prefix) {
    	Set oe = new HashSet(ig.allEdges);
    	for (Iterator i = oe.iterator(); i.hasNext(); ) {
    		Edge e = (Edge)i.next(); 
    		addEdge(prefix + e.from, prefix + e.to);
    	}
    }
    
    /**
     * Get all edges of this graph
     * 
     * @return a set of edges
     */
    public Set getEdges() {
    	return allEdges;
    }
    
    /**
     * List the directly linked nodes
     *
     */
    public void showLinked() {
		for (Iterator i = linked.keySet().iterator(); i.hasNext(); ) {
		    String		from = (String)i.next();
		    SortedSet	tos = (SortedSet)linked.get(from);
		    for (Iterator t = tos.iterator(); t.hasNext(); ) {
		        String to = (String)t.next();
		        System.out.println(from + " -> " + to);
		    }
		}
    }
    
    /**
     * List the directly and indirectly linked nodes
     *
     */
    public void showReachable() {
        for(Iterator i = closure.keySet().iterator(); i.hasNext(); ) {
            String		fromInterface = (String)i.next();
            SortedMap	interfaceTo = (SortedMap)closure.get(fromInterface);
            for (Iterator j = interfaceTo.keySet().iterator(); j.hasNext();) {
                String 	toInterface = (String)j.next();
                List	path = (List)interfaceTo.get(toInterface); 
                System.out.println(fromInterface + " reaches " + toInterface);
                System.out.print("\t");
                for (Iterator k = path.iterator(); k.hasNext(); ) {
                    System.out.print(k.next());
                    System.out.print(", ");
                }
                System.out.println();
            }
        }
    }

    /**
     * Calculate the reachability closure
     * 
     * @return the reachability closure
     */
    public SortedMap calculateClosure() {
        int		numberOfNodes = allNodes.size();

        // Initialize that nothing is connected
        List[][]	connected = new ArrayList[numberOfNodes][numberOfNodes];
        for (int i = 0; i<numberOfNodes; i++) {
            for (int j = 0; j<numberOfNodes; j++) {
                connected[i][j] = null;
            }
        }
        
        // Give each name an index, based on its order
        Map		nameToIndex = new HashMap();
        Map		indexToName = new HashMap();
        int		index = 0;
        for (Iterator i = allNodes.iterator(); i.hasNext(); ) {
            String		name = (String)i.next();
            Integer		indexObject = new Integer(index);
            nameToIndex.put(name, indexObject);
            indexToName.put(indexObject, name);
            index++;
        }

        // initialize the connected based on direct connection
        Iterator ni = allNodes.iterator();
        int start = 0; 
        while (ni.hasNext()) {
            String			from = (String)ni.next();
            SortedSet		interfaceTo = (SortedSet)linked.get(from);
            if (interfaceTo != null) {
	            for (Iterator j = interfaceTo.iterator(); j.hasNext(); ) {
	                String	to = (String)j.next();
	                int		end = ((Integer)nameToIndex.get(to)).intValue();
	                // create a path for the direct link
	                List	path = new ArrayList();
	                path.add(from);
	                path.add(to);
	                connected[start][end] = path;
	            }
            }
            start++;
        }
        
        // calculate the closure using Floyd algorithm
        for (int i = 0; i<numberOfNodes; i++ ) {
            for (int j = 0; j<numberOfNodes; j++) {
                for (int k = 0; k<numberOfNodes; k++) {
                    List pji = connected[j][i];
                    List pik = connected[i][k];
                    List pjk = connected[j][k]; 
                    if ( pji != null &&  pik != null &&
                        ( pjk == null || 
	                     pji.size() + pik.size() < pjk.size() + 1)) {
	                    // if the path is the only path, or a shorter path, then use it
                        // two paths are connected, merge them
                        // make copy, so the original will be untouched
                        List	path1 = new ArrayList(pji);
                        List	path2 = new ArrayList(pik);
                        // remove the duplicate
                        path2.remove(0);
                        List	path12 = new ArrayList();
                        path12.addAll(path1);
                        path12.addAll(path2);
                        connected[j][k] = path12;
                        // ISSUE: here we maintain a shortest path, should we maintain all?
                    }
                }
            }
        }

        // Map the closure back to string interfaces
        closure = new TreeMap();
        start = 0;
        for (Iterator i = allNodes.iterator(); i.hasNext(); ) {
            String		fromInterface = (String)i.next();
            SortedMap	connectedInterfaces = new TreeMap();
            for (int j = 0; j<numberOfNodes; j++) {
                List	path = connected[start][j];
                if (path != null) {
                    String	toInterface = (String)indexToName.get(new Integer(j));
                    connectedInterfaces.put(toInterface, path);
                }
            }
            start++;
            closure.put(fromInterface, connectedInterfaces);
        }
        return closure;
    }
	
    /**
     * Get the shortest path from one interface to another
     * 
     * @param fromInterface		the starting interface
     * @param toInterface   	the ending interface
     * @return the shortest path between the two interfaces. Null if there is no path.
     */
	public List getPath(String fromInterface, String toInterface) {
	    List	result = null;
	    SortedMap		connectedInterfaces = (SortedMap)closure.get(fromInterface);
	    if (connectedInterfaces != null) {
	        result = (List)connectedInterfaces.get(toInterface);
	    }
	    return result;
	}
}
