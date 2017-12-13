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
	
	public ArrayList<NodePair> NodePairRadom(int nodepairNum,String filename,Layer mylayer){//�������nodepair�б�
//		Layer mylayer= new Layer(null, 0, null, null);
		ArrayList<LinearRoute> routelist_once=new ArrayList<LinearRoute>();
		int serial=0;
		
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
		}//Ϊnodepair���
		
		////����nodepair
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
			if(has==0) continue;//�������demand
			nodepairlist.add(nodepair);
		}
		return nodepairlist;
	}
	
	public HashMap<String, NodePair> TrafficNumRadom(ArrayList<NodePair>nodepairlist ){
		randomfunction radom=new randomfunction();
		HashMap<String, NodePair> NodepairWithTraffic=new HashMap<String, NodePair>(0);
		for(NodePair nodePair:nodepairlist){
//			setDemand++;
//			System.out.println(nodePair.getName());
//			if(setDemand<nodepairNum/5){
			nodePair.setTrafficdemand(radom.Num_random(1, 1025)[0]+200);//����200G-1T������
//			}
//			else{
//				nodePair.setTrafficdemand(radom.Num_random(1, demandlimit)[0]+1);
//			}
		}
//		System.out.println();
		for(NodePair nodePair:nodepairlist){
//			System.out.print(nodePair.getName()+"   ");
//			System.out.println(nodePair.getSlotsnum());
			NodepairWithTraffic.put(nodePair.getName(), nodePair);
		}
		return NodepairWithTraffic;
	}
}
