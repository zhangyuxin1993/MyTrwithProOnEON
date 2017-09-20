package MainFunction;

import java.util.ArrayList;

import network.Link;
import subgraph.LinearRoute;

public class RouteAndRegPlace {
	private LinearRoute route = new LinearRoute(null, 0, null);
	private int regnum = 0;
	private int newFSnum=0;
	private ArrayList<Integer> regnode=new ArrayList<Integer>();
	private int nature=0;  //属性工作是0保护是1
	
	public RouteAndRegPlace(LinearRoute route, int nature) {
		super();
		this.route = route;
		this.nature = nature;
	}
	public LinearRoute getRoute(){
		return route;
	}
	public void setregnum(int  regnum) {
		this.regnum=regnum;
	}
	public int getregnum() {
		return regnum;
	}
	public void setnewFSnum(int  newFSnum) {
		this.newFSnum=newFSnum;
	}
	public int getnewFSnum() {
		return newFSnum;
	}
	public void setregnode(ArrayList<Integer> regnode) {
		this.regnode.addAll(regnode);
	}
	public ArrayList<Integer> getregnode() {
		return regnode;
	}
	public void setnature(int  nature) {
		this.nature=nature;
	}
	public int getnature() {
		return nature;
	}

	}
	
	
 
