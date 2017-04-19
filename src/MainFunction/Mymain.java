package MainFunction;

import network.Layer;
import network.Network;

public class Mymain {

	public static void main(String[] args) {

		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("G:/Topology/5.csv");
		network.copyNodes();//
		network.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand

		Layer iplayer = network.getLayerlist().get("Layer0");
		Layer oplayer = network.getLayerlist().get("Physical");

		WorkingGrooming mf=new WorkingGrooming();
		mf.MyWorkingGrooming(network, iplayer, oplayer);
		
	}
}
	

	//写到在光层建立工作光路之后加入保护


