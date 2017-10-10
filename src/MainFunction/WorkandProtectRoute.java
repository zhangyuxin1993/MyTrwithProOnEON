package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;

import demand.Request;
import network.Link;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class WorkandProtectRoute {//һ��ҵ����·�� ����·�� �Լ�����ʹ�õ��������ڵ�
	private NodePair demand = new NodePair(null, 0, null, null, null, null);
	private ArrayList<Link> worklinklist=new ArrayList<Link>();
	private ArrayList<Link> prolinklist=new ArrayList<Link>();
	private ArrayList<VirtualLink> provirtuallinklist=new ArrayList<>();
	private HashMap<Integer, Regenerator> regthinglist=null; 
	private ArrayList<Regenerator> Regneratorlist=new ArrayList<Regenerator>();
	private LinearRoute proroute=new LinearRoute(null, 0, null);
//	private HashMap<Link, ArrayList<Integer>> SlotindexUseOnlink=null;
	private ArrayList<FSshareOnlink> FSoneachLink=null;
	 private ArrayList<Regenerator> newreglist=new ArrayList<Regenerator>();
	 private ArrayList<Regenerator> sharereglist=new ArrayList<Regenerator>();
	 private Request request=new Request(demand);
	 
	public WorkandProtectRoute(NodePair demand) {
		super();
		this.demand = demand;
	}
	
	public void setrequest(Request  request) {
		this.request=request;
	}
	public Request getrequest() {
		return request;
	}
	
	
	public void setFSoneachLink(ArrayList<FSshareOnlink> FSoneachLink) {
		this.FSoneachLink=FSoneachLink;
	}
	public ArrayList<FSshareOnlink> getFSoneachLink() {
		return FSoneachLink;
	}
	
	public void setproroute(LinearRoute  proroute) {
		this.proroute=proroute;
	}
	public LinearRoute getproroute() {
		return proroute;
	}
	
	public void setregthinglist(HashMap<Integer, Regenerator> regthinglist) {
		this.regthinglist=regthinglist;
	}
	public HashMap<Integer, Regenerator> getregthinglist() {
		return regthinglist;
	}
	
	public void setworklinklist(ArrayList<Link> worklinklist) {
		this.worklinklist.addAll(worklinklist);
	}
	public ArrayList<Link> getworklinklist() {
		return worklinklist;
	}
	
	public void setRegeneratorlist(ArrayList<Regenerator> Regneratorlist) {
		this.Regneratorlist.addAll(Regneratorlist);
	}
	public ArrayList<Regenerator> getRegeneratorlist() {
		return Regneratorlist;
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
	
	public void setnewreglist(ArrayList<Regenerator> newreglist) {
		this.newreglist.addAll(newreglist);
	}
	public ArrayList<Regenerator> getnewreglist() {
		return newreglist;
	}
	
	public void setsharereglist(ArrayList<Regenerator> sharereglist) {
		this.sharereglist.addAll(sharereglist);
	}
	public ArrayList<Regenerator> getsharereglist() {
		return sharereglist;
	}
 
	
	public void setdemand(NodePair  demand) {
		this.demand=demand;
	}
	public NodePair getdemand() {
		return demand;
	}

 
}
