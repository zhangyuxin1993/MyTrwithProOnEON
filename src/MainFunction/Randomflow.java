package MainFunction;

import general.Constant;
import general.New_File_Out_Put;
import java.util.HashMap;
import java.util.Iterator;

import network.Layer;
import network.Network;
import network.NodePair;

public class Randomflow {
	public static String file="Node3";
	public static String writename = "f:/Data/"+file+".csv";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Network network=new Network("ip over wdm", 0, "");
		network.readPhysicalTopology("E:\\ampl topology\\N6S8.csv");
		network.copyNodes();
		network.createNodepair();

		Layer iplayer=network.getLayerlist().get("Layer0");
		Layer optlayer=network.getLayerlist().get("Physical");
		
		HashMap<String,NodePair>map=iplayer.getNodepairlist();
   	    Iterator<String>iter=map.keySet().iterator();	
   	    while(iter.hasNext()){     		  		 
   	    	NodePair nodepair=(NodePair)(map.get(iter.next()));  	
   	    	int flow=generateTrafficDemand(); //随机生成一定范围内的slotNum
   	    	System.out.println(nodepair.getName()+"  "+flow);
   	    	nodepair.setTrafficdemand(flow);  	
   	    }
     	 
   	    WriteNodepairSlotNum(iplayer,writename);
     	
     	 
	    }
	
	//随机产生demand flow(in Gb/s)
		public static int generateTrafficDemand(){
			int flow=0;
		    int randomnum=(int)(Math.random()*(2*Constant.AVER_DEMAND-20));
		    flow=randomnum+10;
		    return flow;
		}
	
	
	  public static void WriteNodepairSlotNum(Layer layer, String writename)
	    {
	    	New_File_Out_Put file=new New_File_Out_Put();

	    	file.filewrite(writename,"GenerateFlowBetweenNodepair,");
			
	    	file.filewrite(writename,"Nodepair,"+"Flow");
			
			
			HashMap<String,NodePair>map1=layer.getNodepairlist();
	        Iterator<String>iter1=map1.keySet().iterator();
			while(iter1.hasNext()){ 
				NodePair nodepair=(NodePair)(map1.get(iter1.next()));
				file.filewrite(writename,nodepair.getName()+","+nodepair.getTrafficdemand());
				
			}
	    }
	  
}
	    
     
 
 


	
	
	
	
	
	

