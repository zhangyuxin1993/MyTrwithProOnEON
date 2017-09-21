package MainFunction;

import java.util.ArrayList;

import network.Link;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class WorkandProtectRoute {
	private NodePair demand = new NodePair(null, 0, null, null, null, null);
	private ArrayList<Link> worklinklist=new ArrayList<Link>();
	private ArrayList<Link> prolinklist=new ArrayList<Link>();
	private ArrayList<Node> regnodelist=new ArrayList<Node>();
	public WorkandProtectRoute(NodePair demand) {
		super();
		this.demand = demand;
	}
	public void setworklinklist(ArrayList<Link> worklinklist) {
		this.worklinklist.addAll(worklinklist);
	}
	public ArrayList<Link> getworklinklist() {
		return worklinklist;
	}
	
	public void setprolinklist(ArrayList<Link> prolinklist) {
		this.prolinklist.addAll(prolinklist);
	}
	public ArrayList<Link> getprolinklist() {
		return prolinklist;
	}
	
	public void setdemand(ArrayList<Node> demand) {
		this.regnodelist.addAll(regnodelist);
	}
	public ArrayList<Node> getregnodelist() {
		return regnodelist;
	}
	
	public void setdemand(NodePair  demand) {
		this.demand=demand;
	}
	public NodePair getdemand() {
		return demand;
	}
}
