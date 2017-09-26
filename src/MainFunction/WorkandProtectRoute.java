package MainFunction;

import java.util.ArrayList;

import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class WorkandProtectRoute {//一条业务工作路径 保护路径 以及上面使用的再生器节点
	private NodePair demand = new NodePair(null, 0, null, null, null, null);
	private ArrayList<Link> worklinklist=new ArrayList<Link>();
	private ArrayList<Link> prolinklist=new ArrayList<Link>();
	private ArrayList<Node> regnodelist=new ArrayList<Node>();
	private ArrayList<VirtualLink> provirtuallinklist=new ArrayList<>();
	private ArrayList<Node> sharenodelist=new ArrayList<Node>();
	private ArrayList<Node> newregnodelist=new ArrayList<Node>();

	 
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
	
	public void setprovirtuallinklist(ArrayList<VirtualLink> provirtuallinklist) {
		this.provirtuallinklist.addAll(provirtuallinklist);
	}
	public ArrayList<VirtualLink> getprovirtuallinklist() {
		return provirtuallinklist;
	}
	
	public void setprolinklist(ArrayList<Link> prolinklist) {
		this.prolinklist.addAll(prolinklist);
	}
	public ArrayList<Link> getprolinklist() {
		return prolinklist;
	}
	
	public void setnewregnodelist(ArrayList<Node> newregnodelist) {
		this.newregnodelist.addAll(newregnodelist);
	}
	public ArrayList<Node> getnewregnodelist() {
		return newregnodelist;
	}
	
	public void setsharenodelist(ArrayList<Node> sharenodelist) {
		this.sharenodelist.addAll(sharenodelist);
	}
	public ArrayList<Node> getsharenodelist() {
		return sharenodelist;
	}
	public void setregnodelist(ArrayList<Node> regnodelist) {
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
