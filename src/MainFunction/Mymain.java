package MainFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import general.Constant;
import general.file_out_put;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class Mymain {
	public static String OutFileName = "D:\\zyx\\programFile\\RegwithProandTrgro\\6.dat";
//	public static String OutFileName = "F:\\zyx\\programFile\\6.dat";
	public static void main(String[] args) throws IOException {
		String TopologyName = "D:/zyx/Topology/6.csv";
//		String TopologyName = "F:/zyx/Topology/6.csv";
		int numOfTransponder = 0;
		file_out_put file_io=new file_out_put();
		Mymain mm=new Mymain();
		int average=0;
		ArrayList<NodePair> RadomNodepairlist=new ArrayList<NodePair>();
		Network network_base = new Network("ip over EON", 0, null);
		network_base.readPhysicalTopology(TopologyName);
		network_base.copyNodes();
		network_base.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand
		DemandRadom dr=new DemandRadom();
		Layer iplayer_base = network_base.getLayerlist().get("Layer0");
		RadomNodepairlist=dr.NodePairRadom(15,TopologyName,iplayer_base);//随机产生结对
		dr.TrafficNumRadom(RadomNodepairlist);
		for(NodePair np:RadomNodepairlist){
			System.out.println(np.getName()+"  "+ np.getTrafficdemand());
		}
		/*
		 * 计算average的大小
		 */
		//以下可以读取表格中的业务
//		ReadDemand rd=new ReadDemand();
//		HashMap<String, NodePair> Readnodepairlist = new HashMap<String, NodePair>();
//		 nodepairlist=rd.readDemand(iplayer, "D:/10node.csv");
//		 for(NodePair nodepair:nodepairlist){
//			 System.out.println(nodepair.getName()+"  "+nodepair.getTrafficdemand());
//		 Readnodepairlist.put(nodepair.getName() , nodepair);
//		 }
//		 iplayer.setNodepairlist(Readnodepairlist);
		
		
		for(int shuffle=0;shuffle<3;shuffle++){//打乱次序100次
			double TotalWorkCost=0,TotalProCost=0;
			file_io.filewrite2(OutFileName, "  ");
			file_io.filewrite2(OutFileName, "shuffle="+shuffle);
			
			Collections.shuffle(RadomNodepairlist);//打乱产生的业务100次
			for(NodePair nodepair: RadomNodepairlist){
				file_io.filewrite2(OutFileName, "节点对  "+nodepair.getName()+"  流量：" + nodepair.getTrafficdemand());
			}
			
			// 产生的节点对之间的容量(int)(Math.random()*(2*Constant.AVER_DEMAND-20));
			ArrayList<WorkandProtectRoute> wprlist = new ArrayList<>();
			ArrayList<NodePair> SmallNodePairList = new ArrayList<NodePair>();
		
			Network network = new Network("ip over EON", 0, null);
			network.readPhysicalTopology(TopologyName);
			network.copyNodes();
			network.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand
			
			Layer iplayer = network.getLayerlist().get("Layer0");
			Layer oplayer = network.getLayerlist().get("Physical");
		
			mm.NodepairListset(iplayer, RadomNodepairlist);//在IP层设置nodepairList
			ArrayList<NodePair> demandlist = mm.getDemandList(iplayer,RadomNodepairlist);
			
			for (int n = 0; n < demandlist.size(); n++) {
				NodePair nodepair = demandlist.get(n);
				
				System.out.println();
				System.out.println();
				file_io.filewrite2(OutFileName, "");
				file_io.filewrite2(OutFileName, "");
				System.out.println("正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());
				file_io.filewrite2(OutFileName, "正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());
				
				if(nodepair.getTrafficdemand()<5 ){
					SmallNodePairList.add(nodepair);
					continue;
				}

				mm.mainMethod(nodepair, iplayer, oplayer, numOfTransponder, wprlist);
				
			}
				if(SmallNodePairList!=null&&SmallNodePairList.size()!=0){
					for(NodePair smallnodepair:SmallNodePairList){
						file_io.filewrite2(OutFileName, "");
						file_io.filewrite2(OutFileName, "正在操作的节点对： " + smallnodepair.getName() + "  他的流量需求是： " + smallnodepair.getTrafficdemand());
						mm.mainMethod(smallnodepair, iplayer, oplayer, numOfTransponder, wprlist);
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
//					if(FinalRoute.getIPRegnode()!=null){
//						file_io.filewrite_without(OutFileName, "工作路径放置IP再生器的位置为：");
//						for(int reg: FinalRoute.getIPRegnode()){
//							TotalWorkIPReg++;
//							file_io.filewrite_without(OutFileName, reg +"  ");
//						}
//					}
				}
				else{
					file_io.filewrite2(OutFileName, "该工作链路不需要放置再生器");
				}
				
				//工作的cost （全部为OEO再生器）
				double WorkCost=0;
				for(int count=0;count<wpr.getRegWorkLengthList().size()-1;count++){
					double cost=0;
					file_io.filewrite2(OutFileName,"工作路径上第"+count+"个再生器两端的cost");
					for(int num=count;num<=count+1;num++){
						double length=	wpr.getRegWorkLengthList().get(num);
						file_io.filewrite2(OutFileName,"距离为 "+length);
						if (length > 2000 && length <= 4000) {
							cost=Constant.Cost_OEO_reg_BPSK;
							file_io.filewrite2(OutFileName,"采用BPSK,cost为："+ cost);
						} else if (length > 1000 && length <= 2000) {
							cost=Constant.Cost_OEO_reg_QPSK;
							file_io.filewrite2(OutFileName,"采用QPSK,cost为："+ cost);
						} else if (length > 500 && length <= 1000) {
							cost=Constant.Cost_OEO_reg_8QAM;
							file_io.filewrite2(OutFileName,"采用8QAM,cost为："+ cost);
						} else if (length > 0 && length <= 500) {
							cost=Constant.Cost_OEO_reg_16QAM;
							file_io.filewrite2(OutFileName,"采用16QAM,cost为："+ cost);
						}
						WorkCost=WorkCost+cost;
						}
				}
				file_io.filewrite2(OutFileName,"工作再生器总的cost为："+ WorkCost);
				file_io.filewrite2(OutFileName, " ");
				TotalWorkCost=TotalWorkCost+WorkCost;
				
				file_io.filewrite_without(OutFileName,"保护路径：");
				if(wpr.getproroute()!=null)
				wpr.getproroute().OutputRoute_node(wpr.getproroute(), OutFileName);
				else file_io.filewrite_without(OutFileName,"保护路径在IP层grooming成功");
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
				
				//计算保护路径的cost
				double ProEachcost=0;
				if(wpr.getnewreglist().size()!=0){
					ProEachcost=mm.ProCostCalculate(wpr);
				}
					file_io.filewrite2(OutFileName,"保护路径再生器的cost= " +ProEachcost);
					TotalProCost=TotalProCost+ProEachcost;
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
			file_io.filewrite2(OutFileName, "工作路径再生器cost为："+ TotalWorkCost);
			
			file_io.filewrite2(OutFileName, "保护路径放置的再生器个数为："+ TotalProRegNum);
			file_io.filewrite2(OutFileName, "保护路径放置的IP再生器个数为："+ TotalProIPReg);
			file_io.filewrite2(OutFileName, "工作路径再生器cost为："+ TotalProCost);
			double TotalCost=TotalProCost+TotalWorkCost;
			file_io.filewrite2(OutFileName, "Total cost of reg in network："+ TotalCost);
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
			 
		}
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
	
	public double ProCostCalculate(WorkandProtectRoute wpr) {
		double TotalProCost=0;
		file_out_put file_io=new file_out_put();
		
		for(int count=0;count<wpr.getRegProLengthList().size()-1;count++){
			Regenerator reg= wpr.getRegeneratorlist().get(count);
			if(wpr.getnewreglist().contains(reg)){//该再生器为新建的再生器
				if(reg.getNature()==0){//OEO再生器
					double cost=0;
					file_io.filewrite2(OutFileName,"保护路径上第"+count+"个OEO再生器两端的cost");
					for(int num=count;num<=count+1;num++){
					double length=	wpr.getRegProLengthList().get(num);
					file_io.filewrite2(OutFileName,"距离为 "+length);
					if (length > 2000 && length <= 4000) {
						cost=Constant.Cost_OEO_reg_BPSK;
						file_io.filewrite2(OutFileName,"采用BPSK,cost为："+ cost);
					} else if (length > 1000 && length <= 2000) {
						cost=Constant.Cost_OEO_reg_QPSK;
						file_io.filewrite2(OutFileName,"采用QPSK,cost为："+ cost);
					} else if (length > 500 && length <= 1000) {
						cost=Constant.Cost_OEO_reg_8QAM;
						file_io.filewrite2(OutFileName,"采用8QAM,cost为："+ cost);
					} else if (length > 0 && length <= 500) {
						cost=Constant.Cost_OEO_reg_16QAM;
						file_io.filewrite2(OutFileName,"采用16QAM,cost为："+ cost);
					}
						TotalProCost = TotalProCost + cost;
					}
				}
				if(reg.getNature()==1){//IP再生器
					double cost=0;
					file_io.filewrite2(OutFileName,"保护路径上第"+count+"个IP再生器两端的cost");
					for(int num=count;num<=count+1;num++){
					double length=	wpr.getRegProLengthList().get(num);
					file_io.filewrite2(OutFileName,"距离为 "+length);
					if (length > 2000 && length <= 4000) {
						cost=Constant.Cost_IP_reg_BPSK;
						file_io.filewrite2(OutFileName,"采用BPSK,cost为："+ cost);
					} else if (length > 1000 && length <= 2000) {
						cost=Constant.Cost_IP_reg_QPSK;
						file_io.filewrite2(OutFileName,"采用QPSK,cost为："+ cost);
					} else if (length > 500 && length <= 1000) {
						cost=Constant.Cost_IP_reg_8QAM;
						file_io.filewrite2(OutFileName,"采用8QAM,cost为："+ cost);
					} else if (length > 0 && length <= 500) {
						cost=Constant.Cost_IP_reg_16QAM;
						file_io.filewrite2(OutFileName,"采用16QAM,cost为："+ cost);
					}
						TotalProCost = TotalProCost + cost;
					}
				}
				}
			}
		return TotalProCost;
	}
public void NodepairListset(Layer ipLayer,ArrayList<NodePair> nodepairlist) {
	HashMap<String, NodePair> IPnodePairList =  new HashMap<String, NodePair>();
	
	HashMap<String, NodePair> map3 = ipLayer.getNodepairlist();
	Iterator<String> iter3 = map3.keySet().iterator();

	while (iter3.hasNext()) {
		NodePair NodePair = (NodePair) (map3.get(iter3.next()));
		for(int n=0;n<nodepairlist.size();n++){
			NodePair nodePairinList=nodepairlist.get(n);
			if(nodePairinList.getName().equals(NodePair.getName())){
				NodePair.setTrafficdemand(nodePairinList.getTrafficdemand());
				IPnodePairList.put(NodePair.getName(), NodePair);
				break;
			}
		}
	}
	ipLayer.setNodepairlist(IPnodePairList);
}
public ArrayList<NodePair> getDemandList(Layer ipLayer,ArrayList<NodePair> RadomNodepairlist) {
	ArrayList<NodePair> demandList=new ArrayList<NodePair>();
	
	ArrayList<NodePair> NewFormatnodePairList=new ArrayList<NodePair>();
	HashMap<String,NodePair> nodepairlist=ipLayer.getNodepairlist();
     Iterator<String> iter1=nodepairlist.keySet().iterator();
     while(iter1.hasNext()){
    	 NodePair nodepair=(NodePair)(nodepairlist.get(iter1.next()));
    	 NewFormatnodePairList.add(nodepair);
     }
     	for(NodePair np: RadomNodepairlist){
     		for(NodePair nodepairInIP: NewFormatnodePairList){
     			if(np.getName().equals(nodepairInIP.getName())){
     				demandList.add(nodepairInIP);
     				break;
     			}
     		}
     	}
	return demandList;
}

public void mainMethod(NodePair nodepair, Layer iplayer, Layer oplayer,int numOfTransponder, ArrayList<WorkandProtectRoute> wprlist) throws IOException {
	// 先在IP层路由工作
	boolean iproutingFlag = false;
	boolean ipproFlag = false;
	boolean opworkFlag = false;
	LinearRoute ipWorkRoute = new LinearRoute(null, 0, null);
	LinearRoute opWorkRoute = new LinearRoute(null, 0, null);
	
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
}
