package MainFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import general.file_out_put;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class Mymain {
	public static String OutFileName = "D:\\zyx\\programFile\\RegwithProandTrgro\\USNET.dat";
	public static void main(String[] args) throws IOException {
		String TopologyName = "D:/zyx/Topology/USNET.csv";
		int numOfTransponder = 0;
		Onlyfortest ot=new Onlyfortest();
		HashMap<String, NodePair> Readnodepairlist = new HashMap<String, NodePair>();
		HashMap<String, NodePair> RadomNodepairlist=new HashMap<String, NodePair>();
		ArrayList<NodePair> nodepairlist = new ArrayList<>();
		ArrayList<WorkandProtectRoute> wprlist = new ArrayList<>();
		
		
		file_out_put file_io=new file_out_put();
		// 产生的节点对之间的容量(int)(Math.random()*(2*Constant.AVER_DEMAND-20));
		LinearRoute ipWorkRoute = new LinearRoute(null, 0, null);
		LinearRoute opWorkRoute = new LinearRoute(null, 0, null);
		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology(TopologyName);
		network.copyNodes();
		network.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand
		// **(现在随机产生demand 已经注释)

		
		Layer iplayer = network.getLayerlist().get("Layer0");
		Layer oplayer = network.getLayerlist().get("Physical");
		//以下可以读取表格中的业务
//		ReadDemand rd=new ReadDemand();
//		 nodepairlist=rd.readDemand(iplayer, "D:/10node.csv");
//		 for(NodePair nodepair:nodepairlist){
//			 System.out.println(nodepair.getName()+"  "+nodepair.getTrafficdemand());
//		 Readnodepairlist.put(nodepair.getName() , nodepair);
//		 }
//		 iplayer.setNodepairlist(Readnodepairlist);
		 
		//以下可以随机产生节点对
		DemandRadom dr=new DemandRadom();
		RadomNodepairlist=dr.demandradom(100,TopologyName,iplayer);//随机产生结对对并且产生业务量
		iplayer.setNodepairlist(RadomNodepairlist);
		int p=0;
		HashMap<String, NodePair> testmap3 = iplayer.getNodepairlist();
		Iterator<String> testiter3 = testmap3.keySet().iterator();
		while (testiter3.hasNext()) {
			p++;
			NodePair node = (NodePair) (testmap3.get(testiter3.next()));
			file_io.filewrite2(OutFileName, "随机产生节点对为 "+p+"  "+node.getName()+"   流量为 "+ node.getTrafficdemand());
		}
		
		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		
//		for(NodePair no:demandlist){
//			file_io.filewrite2(OutFileName, "demand "+no.getName());
//		}
		for (int n = 0; n < demandlist.size(); n++) {
			boolean iproutingFlag = false;
			boolean ipproFlag = false;
			boolean opworkFlag = false;

			 NodePair nodepair = demandlist.get(n);
			
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
				ipproFlag = ipprog.ipprotectiongrooming(iplayer, oplayer, nodepair, ipWorkRoute, numOfTransponder, true,wprlist);
				
				if (!ipproFlag) {// 在ip层保护路由受阻 则在光层路由保护
					opProGrooming opg = new opProGrooming();
					opg.opprotectiongrooming(iplayer, oplayer, nodepair, ipWorkRoute, numOfTransponder, true, wprlist);
				}
			}

			// ip层工作路由不成功 在光层路由工作
			if (!iproutingFlag) {
				opWorkingGrooming opwg = new opWorkingGrooming();
				opworkFlag = opwg.opWorkingGrooming(nodepair, iplayer, oplayer, opWorkRoute, wprlist);
				if (opworkFlag) {// 在光层成功建立工作路径后建立保护路径
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
		}
			System.out.println();
			System.out.println();
			file_io.filewrite2(OutFileName, "");
			file_io.filewrite2(OutFileName, "");
			System.out.println("业务个数：" + wprlist.size());
			file_io.filewrite2(OutFileName, "业务个数：" + wprlist.size());
			
			int demandnum=0,TotalWorkRegNum=0,TotalWorkIPReg=0,
					TotalProRegNum=0,TotalProIPReg=0;
			ArrayList<Regenerator> reglist=new ArrayList<>();
			for (WorkandProtectRoute wpr : wprlist) {
				demandnum++;
				file_io.filewrite2(OutFileName, "业务：" + demandnum+"  "+wpr.getdemand().getName());
				file_io.filewrite_without(OutFileName, "工作路径：");
				for (Link link : wpr.getworklinklist()) {
					file_io.filewrite_without(OutFileName, link.getName() + "     ");
				}
				file_io.filewrite2(OutFileName, " " );
//				工作路径放置再生器
				if(wpr.getdemand().getFinalRoute()!=null){//说明该链路需要放置再生器
					RouteAndRegPlace FinalRoute= wpr.getdemand().getFinalRoute();
					file_io.filewrite_without(OutFileName, "工作路径放置再生器的位置为：");
					for(int reg: FinalRoute.getregnode()){
						TotalWorkRegNum++;
						file_io.filewrite_without(OutFileName, reg +"  ");
					}
					file_io.filewrite2(OutFileName, "");
					if(FinalRoute.getIPRegnode()!=null){
						file_io.filewrite_without(OutFileName, "工作路径放置IP再生器的位置为：");
						for(int reg: FinalRoute.getIPRegnode()){
							TotalWorkIPReg++;
							file_io.filewrite_without(OutFileName, reg +"  ");
						}
					}
				}
				else{
					file_io.filewrite2(OutFileName, "该工作链路不需要放置再生器");
				}
				
				file_io.filewrite2(OutFileName, " ");
				file_io.filewrite_without(OutFileName,"保护路径：");
				for (Link link : wpr.getprolinklist()) {
					file_io.filewrite_without(OutFileName,link.getName() + "     ");
				}
				
				file_io.filewrite2(OutFileName, "");
				file_io.filewrite_without(OutFileName,"保护路径放置共享再生器节点：");
				for (Regenerator reg : wpr.getsharereglist()) {
					reg.setPropathNum(reg.getPropathNum()+1);
					if(!reglist.contains(reg)){
						reglist.add(reg);
					}
					if(reg.getNature()==0)
					file_io.filewrite_without(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号: "+reg.getindex()+" 是OEO再生器  ");
					
					if(reg.getNature()==1)
						file_io.filewrite_without(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号: "+reg.getindex()+" 是IP再生器  ");
				}

				
				file_io.filewrite2(OutFileName, "");
				file_io.filewrite_without(OutFileName,"保护路径放置新再生器节点：");
			 
				for (Regenerator reg : wpr.getnewreglist()) {
					reg.setPropathNum(reg.getPropathNum()+1);
					if(!reglist.contains(reg)){
						TotalProRegNum++;
						reglist.add(reg);
					}
					if(reg.getNature()==0)
						file_io.filewrite_without(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号: "+reg.getindex()+" 是OEO再生器  ");
						
						if(reg.getNature()==1){
							file_io.filewrite_without(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号: "+reg.getindex()+" 是IP再生器  ");
							TotalProIPReg++;
						}

				}
				file_io.filewrite2(OutFileName," ");
				
//				测试共享个数				
//				for(Regenerator reg:reglist){
//					file_io.filewrite2(OutFileName,reg.getnode().getName() + "     "+"再生器在节点上的序号:"+reg.getindex()+"   "+"该再生器已经被"+reg.getpropathNum()+"条路径共享");
//				}
				
				 
				file_io.filewrite2(OutFileName, "");
//				if(wpr.getregthinglist()!=null){
//					for(int t:wpr.getregthinglist().keySet()){
//						file_io.filewrite2(OutFileName, "hashmap里面的键 "+t+" 对应的节点为："+wpr.getregthinglist().get(t).getnode().getName());
//					}
//					file_io.filewrite2(OutFileName, "");
//				}
//				else{
//					file_io.filewrite2(OutFileName, "该业务保护路径不需要再生器");
//				}
//				file_io.filewrite2(OutFileName, "");
		
//				ArrayList<FSshareOnlink> FSassignOneachLink=wpr.getFSoneachLink();
//				file_io.filewrite2(OutFileName, "此时的request为"+ wpr.getrequest().getNodepair().getName()+"分配保护路径FS如下");

//				if(FSassignOneachLink!=null){
//				for(FSshareOnlink fsassignoneachlink: FSassignOneachLink){
//					file_io.filewrite_without(OutFileName, "链路"+fsassignoneachlink.getlink().getName()+"上分配的FS为   ");
//					for(int fs:fsassignoneachlink.getslotIndex()){
//						file_io.filewrite_without(OutFileName, fs+"   ");
//					}
//					file_io.filewrite2(OutFileName, "");
//				}
//			}
//				file_io.filewrite2(OutFileName, "");
//				if(FSassignOneachLink==null){
//					file_io.filewrite2(OutFileName, "该保护路径在IP层grooming成功");
//				}
				
//				HashMap<String, Node> testmap2 = oplayer.getNodelist();
//				Iterator<String> testiter2 = testmap2.keySet().iterator();
//				while (testiter2.hasNext()) {
//					Node node = (Node) (testmap2.get(testiter2.next()));
//					file_io.filewrite2(OutFileName, node.getName()+"上面再生器的个数："+node.getregnum());
//				}
				
			}
			file_io.filewrite2(OutFileName, "   ");
			file_io.filewrite2(OutFileName, "工作路径放置的再生器个数为："+ TotalWorkRegNum);
			file_io.filewrite2(OutFileName, "工作路径放置的IP再生器个数为："+ TotalWorkIPReg);
			float TotalWorkCost=10*(TotalWorkRegNum-TotalWorkIPReg)+13*TotalWorkIPReg;
			file_io.filewrite2(OutFileName, "工作路径再生器cost为："+ TotalWorkCost);
			file_io.filewrite2(OutFileName, "保护路径放置的再生器个数为："+ TotalProRegNum);
			file_io.filewrite2(OutFileName, "保护路径放置的IP再生器个数为："+ TotalProIPReg);
			float TotalProCost=10*(TotalProRegNum-TotalProIPReg)+13*TotalProIPReg;
			file_io.filewrite2(OutFileName, "工作路径再生器cost为："+ TotalProCost);
//			file_io.filewrite2(OutFileName, "");
//			file_io.filewrite2(OutFileName, "grooming的检测");
//			HashMap<String, Link> testmap4 = iplayer.getLinklist();
//			Iterator<String> testiter4 = testmap4.keySet().iterator();
//			while (testiter4.hasNext()) {
//				Link link=(Link) (testmap4.get(testiter4.next()));
//				file_io.filewrite2(OutFileName, "IP层上的链路："+link.getName());
//				ArrayList<VirtualLink> vlinklist=link.getVirtualLinkList();
//				for(VirtualLink vlink:vlinklist){
//					file_io.filewrite2(OutFileName, "对应的虚拟链路："+vlink.getSrcnode()+"-"+vlink.getDesnode()+"  性质为："+ vlink.getNature());
//					file_io.filewrite2(OutFileName, "虚拟链路上剩余的容量："+vlink.getRestcapacity());
//				}
//			}
		
//			file_io.filewrite2(OutFileName, "");
//			HashMap<String, Node> testmap2 = oplayer.getNodelist();
//			Iterator<String> testiter2 = testmap2.keySet().iterator();
//			while (testiter2.hasNext()) {
//				Node node = (Node) (testmap2.get(testiter2.next()));
//				file_io.filewrite2(OutFileName, node.getName()+"上面再生器的个数："+node.getregnum());
//			}
		
		System.out.println();
		System.out.println("Finish");
		file_io.filewrite2(OutFileName, "");
		file_io.filewrite2(OutFileName, "Finish");
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
		
			for (int start = 0; start < link.getSlotsarray().size() - slotnum; start++) {
				int flag = 0;
				for (int num = start; num < slotnum + start; num++) {//分配的FS必须是连续的
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
