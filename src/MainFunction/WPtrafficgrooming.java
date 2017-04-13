//package MainFunction;
//import java.io.IOException;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import demand.Request;
//
//import resource.ResourceOnLink;
//import subgraph.LinearRoute;
//import general.Constant;
//import graphalgorithms.RouteSearching;
//import network.Layer;
//import network.Link;
//import network.Network;
//import network.Node;
//import network.NodePair;
//import networkdesign.trafficgrooming;
//import networkdesign.wptrafficgrooming;
//public class WPtrafficgrooming {
//
//	/**
//	 * @param args
//	 * @throws IOException 
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//	    //½¨Á¢¶þ²ãÍøÂç
//
//			Network network=new Network("ip over wdm", 0, "");
//			network.readPhysicalTopology("E:\\ampl topology\\N6S8.csv");
//			network.copyNodes();
//			network.createNodepair();
//
//			Layer iplayer=network.getLayerlist().get("Layer0");
//			Layer optlayer=network.getLayerlist().get("Physical");
//		    wptrafficgrooming mywptrafficgrooming=new wptrafficgrooming();
//		    mywptrafficgrooming.grooming(network, optlayer, iplayer);
//	   }
//}
//
