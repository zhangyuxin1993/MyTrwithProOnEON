package MainFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import general.file_out_put;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class opProGrooming {// 光层路由保护
	String OutFileName =Mymain.OutFileName;
	public boolean opprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag,ArrayList<WorkandProtectRoute> wprlist) throws IOException {// flag=true表示保护IP层建立的工作路径
													// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
		boolean success=false;
		double routelength = 0;
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		ArrayList<VirtualLink> provirtuallinklist=new ArrayList<>();
		HashMap<Link, Integer> FSuseOnlink=new  HashMap<Link, Integer>();
		ArrayList<Link> opDelLink = new ArrayList<Link>();
		System.out.println("************保护路由在IP层不能路由，需要在光层新建");
		file_io.filewrite2(OutFileName,"************保护路由在IP层不能路由，需要在光层新建");
		
		// 删除该节点对的工作路由经过的所有物理链路
		for (Link LinkOnRoute : route.getLinklist()) {// 取出工作路由中的链路
//			System.out.println("工作路径链路：" + LinkOnRoute.getName());
			if (flag) {//// flag=true表示保护 IP层建立的工作路径
				for (VirtualLink Vlink : LinkOnRoute.getVirtualLinkList()) {
					for (Link LinkOnPhy : Vlink.getPhysicallink()) {// 取出某一工作链路上对应的物理链路

						HashMap<String, Link> oplinklist = oplayer.getLinklist();
						Iterator<String> oplinkitor = oplinklist.keySet().iterator();
						while (oplinkitor.hasNext()) {
							Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
							// System.out.println("物理层链路遍历：" +oplink.getName());
							if (oplink.getName().equals(LinkOnPhy.getName())) {
								if (!opDelLink.contains(oplink))
									opDelLink.add(oplink);
								break;
							}
						}
					}
				}
			} else {// flag=false表示保护 光层建立的工作路径
				HashMap<String, Link> oplinklist = oplayer.getLinklist();
				Iterator<String> oplinkitor = oplinklist.keySet().iterator();
				while (oplinkitor.hasNext()) {
					Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
					// System.out.println("物理层链路遍历：" + oplink.getName());
					if (oplink.getName().equals(LinkOnRoute.getName())) {
//						System.out.println("删除的光层链路： " + oplink.getName());
						opDelLink.add(oplink);
						break;
					}
				}
			}
		}
		// 以上为第一部分 删除光层上所有工作链路经过的物理链路

		for (Link opdellink : opDelLink) {
			oplayer.removeLink(opdellink.getName());
		}

		Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
		Node opdesnode = oplayer.getNodelist().get(desnode.getName());

		LinearRoute opPrtectRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opPrtectRoute, null);// 在iplayer里面找寻最短保护路径

		for (Link opdellink : opDelLink) {
			oplayer.addLink(opdellink);
		} // 恢复oplayer里面的link
		opDelLink.clear();

		if (opPrtectRoute.getLinklist().size() == 0) {
			System.out.println("保护路由光层无法建立");
			file_io.filewrite2(OutFileName,"保护路由光层无法建立");
		} else {
			System.out.println("光层找到路由:");
			file_io.filewrite2(OutFileName,"光层找到路由:");
			opPrtectRoute.OutputRoute_node(opPrtectRoute);
			LinearRoute route_out=new LinearRoute(null, 0, null);
			route_out.OutputRoute_node(opPrtectRoute, OutFileName);
			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000，8QAM,0-500 16QAM
			
			for(Link link:opPrtectRoute.getLinklist()){
				routelength=routelength+link.getLength();
			}
			// System.out.println("物理路径的长度是："+routelength);
			// 通过路径的长度来变化调制格式
			if (routelength <= 4000) {
				if (routelength > 2000 && routelength <= 4000) {
					X = 12.5;
				} else if (routelength > 1000 && routelength <= 2000) {
					X = 25.0;
				} else if (routelength > 500 && routelength <= 1000) {
					X = 37.5;
				} else if (routelength > 0 && routelength <= 500) {
					X = 50.0;
				}
				slotnum = (int) Math.ceil(IPflow / X);// 向上取整

				opPrtectRoute.setSlotsnum(slotnum);
				System.out.println("该链路所需slot数： " + slotnum);
				file_io.filewrite2(OutFileName,"该链路所需slot数： " + slotnum);
				ArrayList<Integer> index_wave = new ArrayList<Integer>();
				Mymain mm = new Mymain();
				index_wave = mm.spectrumallocationOneRoute(true, opPrtectRoute, null, slotnum);
				if (index_wave.size() == 0) {
					System.out.println("路径堵塞 ，不分配频谱资源");
					file_io.filewrite2(OutFileName,"路径堵塞 ，不分配频谱资源");
				} else {
					success=true;
					double length = 0;
					double cost = 0;
					for (Link link : opPrtectRoute.getLinklist()) {
						length = length + link.getLength();
						cost = cost + link.getCost();
						Request request = null;
						ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
						FSuseOnlink.put(link, slotnum);
						link.setMaxslot(slotnum + link.getMaxslot());
						// System.out.println("链路 " + link.getName() + " 的最大slot是： " + link.getMaxslot()+" 可用频谱窗数：
						// "+link.getSlotsindex().size());
					}
					
					String name = opsrcnode.getName() + "-" + opdesnode.getName();
					int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的
																// 故这样设置index
					Link finlink=iplayer.findLink(srcnode, desnode);
					Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
					boolean findflag=false;
					try{
						System.out.println("IP层中找到链路"+finlink.getName());
						file_io.filewrite2(OutFileName,"IP层中找到链路"+finlink.getName());
						findflag=true;
					}catch(java.lang.NullPointerException ex){
						System.out.println("IP 层没有该链路需要新建链路");
						file_io.filewrite2(OutFileName,"IP 层没有该链路需要新建链路");
						createlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
						iplayer.addLink(createlink);
					}
					
					VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
					Vlink.setnature(1);
					Vlink.setlength(length);
					Vlink.setcost(cost);
					Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
					Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
					Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
					Vlink.setPhysicallink(opPrtectRoute.getLinklist());
					provirtuallinklist.add(Vlink);
//					numOfTransponder = numOfTransponder + 2;

					if(findflag){//如果在IP层中已经找到该链路
						System.out.println("虚拟链路条数："+finlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName,"虚拟链路条数："+finlink.getVirtualLinkList().size());
						finlink.getVirtualLinkList().add(Vlink);
						System.out.println("IP层已存在的链路 " + finlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
								+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
								+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
								+"   "+"虚拟链路cost： "+ Vlink.getcost());
						file_io.filewrite2(OutFileName,"IP层已存在的链路 " + finlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
								+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
								+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
								+"   "+"虚拟链路cost： "+ Vlink.getcost());
						System.out.println("*********已存在IP层链路：  "+finlink.getName()+"  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName,"*********已存在IP层链路：  "+finlink.getName()+"  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
					}
						else{
							System.out.println("虚拟链路条数："+createlink.getVirtualLinkList().size());
							file_io.filewrite2(OutFileName,"虚拟链路条数："+createlink.getVirtualLinkList().size());
							createlink.getVirtualLinkList().add(Vlink);
							System.out.println("IP层上新建链路 " + createlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
									+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
									+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
									+"   "+"虚拟链路cost： "+ Vlink.getcost());
							file_io.filewrite2(OutFileName,"IP层上新建链路 " + createlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
									+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
									+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
									+"   "+"虚拟链路cost： "+ Vlink.getcost());
							System.out.println("*********新建IP链路：  "+createlink.getName()+"  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
							file_io.filewrite2(OutFileName,"*********新建IP链路：  "+createlink.getName()+"  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
						}
				}
			}
			if (routelength > 4000) {
				ProregeneratorPlace rgp=new ProregeneratorPlace();
				success=rgp.proregeneratorplace(nodepair, opPrtectRoute, wprlist, routelength, oplayer, iplayer, IPflow);
			}
		}
		 for(WorkandProtectRoute wpr0:wprlist){
			 if(wpr0.getdemand().equals(nodepair)){
				wpr0.setproroute(opPrtectRoute);  
			 }
		 }
		if(success&&routelength<4000) {
		 for(WorkandProtectRoute wpr:wprlist){
			 if(wpr.getdemand().equals(nodepair)){
				 ArrayList<Link> totallink=new ArrayList<>();
				totallink=opPrtectRoute.getLinklist();
				wpr.setprolinklist(totallink);
				wpr.setFSuseOnlink(FSuseOnlink);
				wpr.setprovirtuallinklist(provirtuallinklist);
				wpr.setregthinglist(null);
			 }
		 }
		}
		return success;
	}
}
