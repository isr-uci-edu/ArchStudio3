package archstudio.comp.graphlayout;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class GraphLayout implements java.io.Serializable{

	protected ArrayList nodes;
	protected ArrayList edges;
	
	public GraphLayout(){
		nodes = new ArrayList();
		edges = new ArrayList();
	}
	
	public void addNode(Node n){
		nodes.add(n);
	}
	
	public int getNumNodes(){
		return nodes.size();
	}
	
	public Node getNodeAt(int index){
		return (Node)nodes.get(index);
	}
	
	public void addEdge(Edge e){
		edges.add(e);
	}
	
	public int getNumEdges(){
		return edges.size();
	}
	
	public Edge getEdgeAt(int index){
		return (Edge)edges.get(index);
	}
	
	public static class Node{
		protected String nodeId;
		protected Rectangle bounds;
		
		protected ArrayList ports;
		/*
		protected ArrayList northPortIds;
		protected ArrayList southPortIds;
		protected ArrayList eastPortIds;
		protected ArrayList westPortIds;
		protected ArrayList unknownPortIds;
		*/
		public Node(){
			this.nodeId = null;
			this.bounds = null;
			
			ports = new ArrayList();
			/*
			northPortIds = new ArrayList();
			southPortIds = new ArrayList();
			eastPortIds = new ArrayList();
			westPortIds = new ArrayList();
			unknownPortIds = new ArrayList();
			*/
		}
		
		public void setNodeId(String nodeId){
			this.nodeId = nodeId;
		}
		
		public String getNodeId(){
			return nodeId;
		}
		
		public void setBounds(Rectangle bounds){
			this.bounds = bounds;
		}
		
		public Rectangle getBounds(){
			return bounds;
		}
		
		public void addPort(AbstractPort p){
			ports.add(p);
		}
		
		public AbstractPort[] getAllPorts(){
			return (AbstractPort[])ports.toArray(new AbstractPort[0]);
		}
		
		/*
		public void addNorthPortId(String portId){
			northPortIds.add(portId);
		}

		public void addSouthPortId(String portId){
			southPortIds.add(portId);
		}
		
		public void addEastPortId(String portId){
			eastPortIds.add(portId);
		}

		public void addWestPortId(String portId){
			westPortIds.add(portId);
		}
		
		public void addUnknownPortId(String portId){
			unknownPortIds.add(portId);
		}
		
		public String[] getNorthPortIds(){
			return (String[])northPortIds.toArray(new String[0]);
		}
		
		public String[] getSouthPortIds(){
			return (String[])southPortIds.toArray(new String[0]);
		}

		public String[] getWestPortIds(){
			return (String[])westPortIds.toArray(new String[0]);
		}

		public String[] getEastPortIds(){
			return (String[])eastPortIds.toArray(new String[0]);
		}

		public String[] getUnknownPortIds(){
			return (String[])unknownPortIds.toArray(new String[0]);
		}
		*/
	}
	
	public static abstract class AbstractPort{
		protected String id;
		
		public AbstractPort(String id){
			this.id = id;
		}
		
		public void setId(String id){
			this.id = id;
		}
		
		public String getId(){
			return id;
		}
	}
	
	public static class PlainPort extends AbstractPort{
		public PlainPort(String id){
			super(id);
		}
	}
	
	public static class CompassPort extends AbstractPort{
		public static final int NORTH = 1;
		public static final int SOUTH = 2;
		public static final int EAST = 3;
		public static final int WEST = 4;
		
		protected int direction;
		
		public CompassPort(String id, int direction){
			super(id);
			this.direction = direction;
		}
		
		public void setDirection(int direction){
			this.direction = direction;
		}
		
		public int getDirection(){
			return direction;
		}
	}
	
	public static class Edge{
		protected String edgeId;
		protected ArrayList points = new ArrayList();

		protected String node1Id, port1Id;
		protected String node2Id, port2Id;
		
		public Edge(){
		}
		
		public Edge(Point[] pointArray){
			for(int i = 0; i < pointArray.length; i++){
				points.add(pointArray[i]);
			}
		}
		
		public void setEndpoint1(String nodeId, String portId){
			this.node1Id = nodeId;
			this.port1Id = portId;
		}
		
		public void setEndpoint2(String nodeId, String portId){
			this.node2Id = nodeId;
			this.port2Id = portId;
		}
		
		public String getNode1Id(){
			return node1Id;
		}
		
		public String getNode2Id(){
			return node2Id;
		}
		
		public String getPort1Id(){
			return port1Id;
		}
		
		public String getPort2Id(){
			return port2Id;
		}
		
		public void addPoint(Point p){
			points.add(p);
		}
		
		public int getNumPoints(){
			return points.size();
		}
		
		public Point getPointAt(int i){
			return (Point)points.get(i);
		}

		public String getEdgeId() {
			return edgeId;
		}

		public void setEdgeId(String string) {
			edgeId = string;
		}

	}
}
