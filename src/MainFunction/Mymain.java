package MainFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import general.Constant;
import general.file_out_put;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class Mymain {
	public static String OutFileName = "D:\\zyx\\programFile\\RegwithProandTrgro\\NSFNET.dat";
	public static void main(String[] args) throws IOException {
		int numOfTransponder = 0;
		Onlyfortest ot=new Onlyfortest();
		HashMap<String, NodePair> Readnodepairlist = new HashMap<String, NodePair>();
		ArrayList<NodePair> nodepairlist = new ArrayList<>();
		ArrayList<WorkandProtectRoute> wprlist = new ArrayList<>();
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		// 产生的节点对之间的容量(int)(Math.random()*(2*Constant.AVER_DEMAND-20));
		LinearRoute ipWorkRoute = new LinearRoute(null, 0, null);
		LinearRoute opWorkRoute = new LinearRoute(null, 0, null);
		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("D:/zyx/Topology/NSFNET.csv");
		network.copyNodes();
		network.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand
		// **(现在随机产生demand 已经注释)

		Layer iplayer = network.getLayerlist().get("Layer0");
		Layer oplayer = network.getLayerlist().get("Physical");
		
//		ReadDemand rd=new ReadDemand();
//		 nodepairlist=rd.readDemand(iplayer, "D:/10node.csv");
//		 for(NodePair nodepair:nodepairlist){
//			 System.out.println(nodepair.getName()+"  "+nodepair.getTrafficdemand());
//		 Readnodepairlist.put(nodepair.getName() , nodepair);
//		 }
//		 iplayer.setNodepairlist(Readnodepairlist);

		ArrayList<NodePair> demandlist = Rankflow(iplayer);

		for (int n = 0; n < demandlist.size(); n++) {
			boolean iproutingFlag = false;
			boolean ipproFlag = false;
			boolean opworkFlag = false;

			 NodePair nodepair = demandlist.get(n);
			// test
//			NodePair nodepair = new NodePair(null, 0, null, iplayer, null, null);
//			HashMap<String, NodePair> map2 = iplayer.getNodepairlist();
//			Iterator<String> iter2 = map2.keySet().iterator();
//			while (iter2.hasNext()) {
//				NodePair nodepair2 = (NodePair) (map2.get(iter2.next()));
//				if (nodepair2.getSrcNode().getName().equals("N5") && nodepair2.getDesNode().getName().equals("N9"))
//					nodepair = nodepair2;
//			}
			
			System.out.println();
			System.out.println();
			file_io.filewrite2(OutFileName, "");
			file_io.filewrite2(OutFileName, "");
			System.out.println("正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());
			file_io.filewrite2(OutFileName, "正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());
			
			// 先在IP层路由工作
			IPWorkingGrooming ipwg = new IPWorkingGrooming();
			iproutingFlag = ipwg.ipWorkingGrooming(nodepair, iplayer, oplayer, numOfTransponder, ipWorkRoute, wprlist);// 在ip层工作路由
			if (iproutingFlag) {// ip层工作路由成功 建立保护
				ipProGrooming ipprog = new ipProGrooming();
				ipproFlag = ipprog.ipprotectiongrooming(iplayer, oplayer, nodepair, ipWorkRoute, numOfTransponder, true,
						wprlist);
				if (!ipproFlag) {// 在ip层保护路由受阻 则在光层路由保护
					opProGrooming opg = new opProGrooming();
					opg.opprotectiongrooming(iplayer, oplayer, nodepair, ipWorkRoute, numOfTransponder, true, wprlist);
				}
			}

			// ip层工作路由不成功 在光层路由工作
			if (!iproutingFlag) {
				opWorkingGrooming opwg = new opWorkingGrooming();
				opworkFlag = opwg.opWorkingGrooming(nodepair, iplayer, oplayer, opWorkRoute, wprlist);
				if (opworkFlag) {// 在光层成功建立工作路径
					ipProGrooming ipprog = new ipProGrooming();
					ipproFlag = ipprog.ipprotectiongrooming(iplayer, oplayer, nodepair, opWorkRoute, numOfTransponder,
							false, wprlist);
					if (!ipproFlag) {// 在ip层保护路由受阻 则在光层路由保护
						opProGrooming opg = new opProGrooming();
						opg.opprotectiongrooming(iplayer, oplayer, nodepair, opWorkRoute, numOfTransponder, false,
								wprlist);
					}
				 
				}
			}
			System.out.println();
			System.out.println();
			file_io.filewrite2(OutFileName, "");
			file_io.filewrite2(OutFileName, "");
			System.out.println("业务个数：" + wprlist.size());
			file_io.filewrite2(OutFileName, "业务个数：" + wprlist.size());
			for (WorkandProtectRoute wpr : wprlist) {
				System.out.println("业务：" + wpr.getdemand().getName());
				file_io.filewrite2(OutFileName, "业务：" + wpr.getdemand().getName());
				System.out.print("工作路径：" );
				file_io.filewrite_without(OutFileName, "工作路径：");
				for (Link link : wpr.getworklinklist()) {
					System.out.print(link.getName() + "     ");
					file_io.filewrite_without(OutFileName, link.getName() + "     ");
				}
				System.out.println();
				file_io.filewrite2(OutFileName, "");
				System.out.print("保护路径：" );
				file_io.filewrite_without(OutFileName,"保护路径：");
				for (Link link : wpr.getprolinklist()) {
					System.out.print(link.getName() + "     ");
					file_io.filewrite_without(OutFileName,link.getName() + "     ");
				}
				System.out.println();
				file_io.filewrite2(OutFileName, "");
				System.out.print("放置共享再生器节点：" );
				file_io.filewrite_without(OutFileName,"放置共享再生器节点：");
				for (Regenerator reg : wpr.getsharereglist()) {
					System.out.print(reg.getnode().getName() + "     "+"再生器在节点上的序号: "+reg.getindex()+"   ");
					file_io.filewrite_without(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号: "+reg.getindex()+"   ");
				}
				System.out.println();
				file_io.filewrite2(OutFileName, "");
				System.out.print("放置新再生器节点：" );
				file_io.filewrite_without(OutFileName,"放置新再生器节点：");
				for (Regenerator reg : wpr.getnewreglist()) {
					System.out.print(reg.getnode().getName() + "     "+"再生器在节点上的序号:"+reg.getindex()+"   ");
					file_io.filewrite_without(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号:"+reg.getindex()+"   ");
				}
				System.out.println();
				file_io.filewrite2(OutFileName, "");
//				System.out.println("hashmap的大小："+wpr.getregthinglist().size());
				if(wpr.getregthinglist()!=null){
					for(int t:wpr.getregthinglist().keySet()){
						System.out.println("hashmap里面的键 "+t+" 对应的节点为："+wpr.getregthinglist().get(t).getnode().getName());
						file_io.filewrite2(OutFileName, "hashmap里面的键 "+t+" 对应的节点为："+wpr.getregthinglist().get(t).getnode().getName());
					}
					System.out.println();
					file_io.filewrite2(OutFileName, "");
				}
				else{
					System.out.println("该业务保护路径不需要再生器");
					file_io.filewrite2(OutFileName, "该业务保护路径不需要再生器");
				}
				System.out.println();
				file_io.filewrite2(OutFileName, "");
			}
			HashMap<String, Node> testmap2 = oplayer.getNodelist();
			Iterator<String> testiter2 = testmap2.keySet().iterator();
			while (testiter2.hasNext()) {
				Node node = (Node) (testmap2.get(testiter2.next()));
				System.out.println(node.getName()+"上面再生器的个数："+node.getregnum());
				file_io.filewrite2(OutFileName, node.getName()+"上面再生器的个数："+node.getregnum());
			}
			
			HashMap<String, Link> testmap3 = oplayer.getLinklist();
			Iterator<String> testiter3 = testmap3.keySet().iterator();
			while (testiter3.hasNext()) {
				Link link = (Link) (testmap3.get(testiter3.next()));
				file_io.filewrite2(OutFileName, "链路 "+link.getName()+"上面max slot为"+link.getMaxslot()+"slotarray的大小"+link.getSlotsarray().size());
			}
		}
		System.out.println("Finish");
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

	public ArrayList<Integer> spectrumallocationOneRoute(Boolean routeflag, LinearRoute route, ArrayList<Link> linklist,
			int slotnum) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		if (routeflag) {
			linklistOnroute = route.getLinklist();
		} else {
			linklistOnroute = linklist;
		}
		for (Link link : linklistOnroute) {
			link.getSlotsindex().clear();
			// slotarray和slotindex的区别？？
			for (int start = 0; start < link.getSlotsarray().size() - slotnum; start++) {
				int flag = 0;
				for (int num = start; num < slotnum + start; num++) {
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
				sameindex.add(index); // 挑选出该路径上所有link共同的slot start数
			}
		}
		// 测试频谱分配问题
		// for (Link link : linklistOnroute) {
		// System.out.println("");
		// System.out.println("测试频谱分配：");
		// System.out.println("链路： "+link.getName()+"
		// "+link.getSlotsindex().size());
		// }
		return sameindex;
	}
}
