package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import general.Constant;
import network.Layer;
import network.Link;
import network.Network;
import network.NodePair;
import subgraph.LinearRoute;

public class Mymain {

	public static void main(String[] args) {
		int numOfTransponder = 0;
	
//		产生的节点对之间的容量(int)(Math.random()*(2*Constant.AVER_DEMAND-20));
		LinearRoute ipWorkRoute = new LinearRoute(null, 0, null);
		LinearRoute opWorkRoute = new LinearRoute(null, 0, null);
		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("G:/Topology/68modifylength.csv");
		network.copyNodes();
		network.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand

		Layer iplayer = network.getLayerlist().get("Layer0");
		Layer oplayer = network.getLayerlist().get("Physical");

		ArrayList<NodePair> demandlist = Rankflow(iplayer);

		for (int n = 0; n < demandlist.size(); n++) {
			boolean iproutingFlag = false;
			boolean ipproFlag = false;
			boolean opworkFlag = false;
			
			NodePair nodepair = demandlist.get(n);
			System.out.println();
			System.out.println();
			System.out.println("正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());
//			先在IP层路由工作
			IPWorkingGrooming ipwg = new IPWorkingGrooming();
			iproutingFlag = ipwg.ipWorkingGrooming(nodepair, iplayer, oplayer,numOfTransponder,ipWorkRoute);//在ip层工作路由

//			if(iproutingFlag){//ip层工作路由成功 建立保护
//				ipProGrooming ipprog=new ipProGrooming();
//				ipproFlag=ipprog.ipprotectiongrooming(iplayer, oplayer, nodepair, ipWorkRoute, numOfTransponder, true);
//				
//				if(!ipproFlag){//在ip层保护路由受阻 则在光层路由保护
//					opProGrooming opg=new opProGrooming();
//					opg.opprotectiongrooming(iplayer, oplayer, nodepair, ipWorkRoute, numOfTransponder, true);
//				}
//				
//			}
			
			//ip层工作路由不成功 在光层路由工作 
			if (!iproutingFlag) {
				opWorkingGrooming opwg=new opWorkingGrooming();
				opworkFlag=opwg.opWorkingGrooming(nodepair, iplayer, oplayer,opWorkRoute,numOfTransponder);
//				if(opworkFlag){//在光层成功建立工作路径
//					ipProGrooming ipprog=new ipProGrooming();
//					ipproFlag=ipprog.ipprotectiongrooming(iplayer, oplayer, nodepair, opWorkRoute, numOfTransponder, false);
//			
//					if(!ipproFlag){//在ip层保护路由受阻 则在光层路由保护
//						opProGrooming opg=new opProGrooming();
//						opg.opprotectiongrooming(iplayer, oplayer, nodepair, opWorkRoute, numOfTransponder, false);
//					}
//				}
				
			}
		}

		
		// WorkingGrooming mf=new WorkingGrooming();
		// mf.MyWorkingGrooming(network, iplayer, oplayer);

	}

	public static ArrayList<NodePair> Rankflow(Layer IPlayer) {
		ArrayList<NodePair> nodepairlist = new ArrayList<NodePair>(2000);
		HashMap<String, NodePair> map3 = IPlayer.getNodepairlist();
		Iterator<String> iter3 = map3.keySet().iterator();
		while (iter3.hasNext()) {
			NodePair np = (NodePair) (map3.get(iter3.next()));
			if (nodepairlist.size() == 0)
				nodepairlist.add(np);
			else {
				boolean insert = false;
				for (int i = 0; i < nodepairlist.size(); i++) {
					int m_flow = np.getTrafficdemand();
					int n_flow = nodepairlist.get(i).getTrafficdemand();

					if (m_flow > n_flow) {
						nodepairlist.add(i, np);
						insert = true;
						break;
					}

				}

				if (insert == false)
					nodepairlist.add(np);
			}
		}
		return nodepairlist;
	}
	
	public ArrayList<Integer> spectrumallocationOneRoute(Boolean routeflag,LinearRoute route,ArrayList<Link> linklist,int slotnum) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		if(routeflag){			
			linklistOnroute = route.getLinklist();
		}
		else{
			linklistOnroute=linklist;
		}
//		route.OutputRoute_node(route);// debug
		for (Link link : linklistOnroute) {
//			if (route.getSlotsnum() == 0) {
//				System.out.println("路径上没有slot需要分配");
//				break;
//			}
			link.getSlotsindex().clear();
			// slotarray和slotindex的区别？？
			for (int start = 0; start < link.getSlotsarray().size() - slotnum; start++) {
				int flag = 0;
				for (int num = start; num <slotnum + start; num++) {
					if (link.getSlotsarray().get(num).getoccupiedreqlist().size() != 0) {// 该波长已经被占用
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					link.getSlotsindex().add(start);// 查找可用slot的起点
				}
			}
		} // 以上所有的link分配完

		Link firstlink = linklistOnroute.get(0);
		ArrayList<Integer> sameindex = new ArrayList<Integer>();
		sameindex.clear();

		for (int s = 0; s < firstlink.getSlotsindex().size(); s++) {
			int index = firstlink.getSlotsindex().get(s);
			int flag = 1;

			for (Link otherlink : linklistOnroute) {
				if (otherlink.getName().equals(firstlink.getName()))
					continue;
				if (!otherlink.getSlotsindex().contains(index)) {
					flag = 0;
					break;
				}
			}
			if (flag == 1) {
				sameindex.add(index); //挑选出该路径上所有link共同的slot start数
			}
		}
	 //测试频谱分配问题
		for (Link link : linklistOnroute) {
			System.out.println("");
			System.out.println("测试频谱分配：");
				System.out.println("链路：  "+link.getName()+"    "+link.getSlotsindex().size());	
			}
//		for(int m:sameindex) System.out.print(m+"");
		return sameindex;
	}
}

