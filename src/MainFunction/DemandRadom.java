package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import graphalgorithms.RouteSearching;
import network.Layer;
import network.NodePair;
import randomfunctions.randomfunction;
import subgraph.LinearRoute;

public class DemandRadom {
	
	public HashMap<String, NodePair> demandradom(int nodepairNum,String filename,Layer mylayer){
//		int nodepairNum=10;//随机产生的节点数
		int demandlimit=250;
//		Layer mylayer= new Layer(null, 0, null, null);
		ArrayList<LinearRoute> routelist_once=new ArrayList<LinearRoute>();
		RouteSearching rs=new RouteSearching();
		int serial=0;
		HashMap<String, NodePair> RadomNodepairlist=new HashMap<String, NodePair>();
		
//		mylayer.readTopology(filename);
//		mylayer.generateNodepairs(); 
		HashMap<String,Integer> nodepair_serial=new HashMap<String,Integer>();
		ArrayList<NodePair> nodepairlist= new ArrayList<NodePair>();
		HashMap<String, NodePair> Snodepair = mylayer.getNodepairlist();
		Iterator<String> iter1 = Snodepair.keySet().iterator();
		while (iter1.hasNext()) 
		{
			
			NodePair nodepairser=(NodePair) (Snodepair.get(iter1.next()));
			nodepair_serial.put(nodepairser.getName(),serial);
			serial++;
//			System.out.println(nodepairser.getName()+"  "+serial);
		}//为nodepair编号
		
		////产生nodepair
		randomfunction radom=new randomfunction();
		int[] nodepair_num=radom.Dif_random(nodepairNum, mylayer.getNodepairNum());
		int has=0;
		HashMap<String, NodePair> map = mylayer.getNodepairlist();
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) 
		{
			has=0;
			routelist_once.clear();
			NodePair nodepair=(NodePair) (map.get(iter.next()));

//			rs.findAllRoute(nodepair.getSrcNode(), nodepair.getDesNode(), mylayer, null, 100, routelist_once);
//			if(routelist_once.size()<3) continue;
			for(int a=0;a<nodepair_num.length;a++){
				if(nodepair_num[a]==nodepair_serial.get(nodepair.getName())){
					has=1;
					break;
				}
					
			}
			if(has==0) continue;//随机产生demand
			nodepairlist.add(nodepair);
		}
//		System.out.println("随机产生的节点对：");
		int setDemand=0;
		for(NodePair nodePair:nodepairlist){
			setDemand++;
//			System.out.println(nodePair.getName());
			if(setDemand<nodepairNum/5){
				nodePair.setTrafficdemand(radom.Num_random(1, 10)[0]);
			}
			else{
				nodePair.setTrafficdemand(radom.Num_random(1, demandlimit)[0]);
			}
		}
//		System.out.println();
		for(NodePair nodePair:nodepairlist){
//			System.out.print(nodePair.getName()+"   ");
//			System.out.println(nodePair.getSlotsnum());
			RadomNodepairlist.put(nodePair.getName(), nodePair);
		}
		return RadomNodepairlist;
	}

//	public static void main(String[] args) {
//		DemandRadom dr=new DemandRadom();
//		dr.demandradom(5,"G:/Topology/NSFNET.csv");
//	}

}
