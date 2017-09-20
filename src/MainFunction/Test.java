package MainFunction;

import java.util.HashMap;
import java.util.Iterator;

import graphalgorithms.RouteSearching;
import network.Layer;
import network.Network;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class Test {
    private int m;  
    private int n; 
    private int[] set;  
    private boolean first;  
    private int position;  	 
		
    public Test(int n, int m) {  
        this.m = m;  
        this.n=n;
        first = true;  
        position = n - 1;  
        set = new int[n];  
        for (int i = 0; i < n; i++)  
            set[i] = i + 1;  
    }  
    
    public boolean hasNext() {
    	if(m==n&&first){
    		return true;
    	}
    	else{
        return set[0] < m - set.length +1 ;}  
    }  
  
    public int[] next() {  
        if (first) {  
            first = false;  
            return set;  
        }  
        if (set[set.length - 1] == m)  
            position--;  
        else  
            position = set.length - 1;  
        set[position]++;  
        for (int i = position + 1; i < set.length; i++)  
            set[i] = set[i - 1] + 1;  
        return set;  
    }
    
	public static void main(String[] args) {  
		/*
		 * 测试从M个数中随机抽取m个数的所有情况
		 */
//		     Test nOfm = new Test(5, 5);  
//	        while (nOfm.hasNext()) {  
//	            int[] set = nOfm.next();  
//	            for (int i = 0; i < set.length; i++) {  
//	                System.out.print(set[i]);  
//	            }  
//	            System.out.println();  
//	        } 
//		Network network = new Network("ip over EON", 0, null);
//		network.readPhysicalTopology("G:/Topology/testtop.csv");
//		network.copyNodes();
//		network.createNodepair();// 
//	
//		Layer oplayer = network.getLayerlist().get("Physical");
//		Layer iplayer = network.getLayerlist().get("Layer0");
//		Node nodeA=new Node(null, 0, null, oplayer, 0, 0);
//		Node nodeB=new Node(null, 0, null, oplayer, 0, 0);
//	
//		oplayer.generateNodepairs();
//		HashMap<String,Node>map=oplayer.getNodelist();
//	     Iterator<String>iter=map.keySet().iterator();
//	       while(iter.hasNext()){
//	    	   Node node=(Node)(map.get(iter.next()));
//	    	   if(node.getName().equals("N1"))   nodeA=node;
//	    	   if(node.getName().equals("N7"))   nodeB=node;
//	       }
			
//	       HashMap<String,NodePair>map2=oplayer.getNodepairlist();
//		     Iterator<String>iter2=map2.keySet().iterator();
//		       while(iter2.hasNext()){
//		    	   NodePair nodepair=(NodePair)(map2.get(iter2.next()));
//			       RouteSearching rs=new RouteSearching();
//			       LinearRoute newRoute=new LinearRoute(null, 0, null);
//			       rs.Dijkstras(nodepair.getSrcNode(), nodepair.getDesNode(), oplayer, newRoute, null);
//			       System.out.print(nodepair.getName()+"     ");
//			       newRoute.OutputRoute_node(newRoute);
//		       }
//	       System.out.println(nodeA.getName()+" - "+nodeB.getName());
//	       RouteSearching rs=new RouteSearching();
//	       LinearRoute newRoute=new LinearRoute(null, 0, null);
//	       rs.Dijkstras(nodeA, nodeB, oplayer, newRoute, null);
//	       newRoute.OutputRoute_node(newRoute);
//	       RegeneratorPlace rp=new RegeneratorPlace();
//	       double length=newRoute.getlength();
//	       rp.regeneratorplace(145, length, newRoute, oplayer, iplayer);
	       
	
  
 
		/*
		 * 检验小程序 检验IP层里是否已经有某条链路
		 */
		/*
		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("G:/Topology/68modifylength.csv");
		network.copyNodes();
		network.createNodepair();// 
		Layer iplayer = network.getLayerlist().get("Layer0");
		Node nodeA=new Node(null, 0, null, iplayer, 0, 0);
		Node nodeB=new Node(null, 0, null, iplayer, 0, 0);
		Node nodeC=new Node(null, 0, null, iplayer, 0, 0);
		Node nodeD=new Node(null, 0, null, iplayer, 0, 0);
		
		System.out.println(iplayer.getNodelist().size());
		 HashMap<String,Node>map=iplayer.getNodelist();
	     Iterator<String>iter=map.keySet().iterator();
	       while(iter.hasNext()){
	    	   Node node=(Node)(map.get(iter.next()));
	    	   if(node.getName().equals("N3"))   nodeA=node;
	    	   if(node.getName().equals("N4"))   nodeB=node;
	    	   if(node.getName().equals("N3"))   nodeC=node;
	    	   if(node.getName().equals("N4"))   nodeD=node;
	       }
	
		String name=nodeA.getName()+"-"+nodeB.getName();
		String name2=nodeC.getName()+"-"+nodeD.getName();
		System.out.println(name+"   "+name2);
		Link newlink=new Link(name, 0, null, iplayer, nodeA, nodeB, 0, 0);
		iplayer.addLink(newlink);
		Link findledink=iplayer.findLink(nodeA, nodeB);
		Link findledink2=iplayer.findLink(nodeC, nodeD);// 找不到link的时候怎么办？？？
		System.out.println(findledink.getName());
//		try{
//			System.out.println(findledink2.getName());
//		}catch(java.lang.NullPointerException ex){
//			System.out.println("IP 层没有该链路");
//		}
		
		VirtualLink Vlink = new VirtualLink(nodeA.getName(), nodeB.getName(), 0, 0);
		Vlink.setnature(0);
		Vlink.setUsedcapacity(100);
		Vlink.setFullcapacity(200);// 多出来的flow是从这里产生的
		Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
		Vlink.setlength(4500);
		Vlink.setcost(0);	

		VirtualLink Vlink2 = new VirtualLink(nodeA.getName(), nodeB.getName(), 0, 0);
		Vlink.setnature(0);
		Vlink.setUsedcapacity(100);
		Vlink.setFullcapacity(200);// 多出来的flow是从这里产生的
		Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
		Vlink.setlength(4500);
		Vlink.setcost(0);	
		
		
		findledink.getVirtualLinkList().add(Vlink);
		findledink.getVirtualLinkList().add(Vlink2);
		System.out.println("虚拟链路条数："+findledink.getVirtualLinkList().size());//一条IP 链路上可以加多个虚拟链路并且这些虚拟链路即使所有因素都一样也不会重复
		*/
	}
}	
 

	  

  

