package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import general.file_out_put;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class ipProGrooming {
	String OutFileName =Mymain.OutFileName;
	public boolean ipprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag, ArrayList<WorkandProtectRoute> wprlist) {// flag=true表示保护IP层建立的工作路径
		// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
		boolean ipproflag = false;
		Test t = new Test();
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		
		file_io.filewrite2(OutFileName,"  ");
		file_io.filewrite2(OutFileName,"开始保护路由" );
		ArrayList<VirtualLink> DelLinkList = new ArrayList<VirtualLink>();
		ArrayList<VirtualLink> SumDelLinkList = new ArrayList<VirtualLink>();
		ArrayList<Link> DelIPLinkList = new ArrayList<Link>();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
//		System.out.println("IP层的链路条数： " + iplayer.getLinklist().size());
//		file_io.filewrite2(OutFileName,"IP层的链路条数： " + iplayer.getLinklist().size());
		
		//test
//		for(WorkandProtectRoute wprpro:wprlist){
//			System.out.println(wprpro.getdemand().getName());
//			for(VirtualLink vprolink:wprpro.getprovirtuallinklist()){
//				System.out.println(vprolink.getSrcnode()+"  "+vprolink.getDesnode());
//			}
//		}
		
//		HashMap<String, Link> testlinklist = iplayer.getLinklist();
//		Iterator<String> testlinkitor = testlinklist.keySet().iterator();
//		while (testlinkitor.hasNext()) {// 第一部分 第一步
//			Link link = (Link) (testlinklist.get(testlinkitor.next()));// IP层上的链路
//			System.out.println(link.getName());
//		}

		HashMap<String, Link> linklist = iplayer.getLinklist();
		Iterator<String> linkitor = linklist.keySet().iterator();
		while (linkitor.hasNext()) {// 第一部分 第一步
			Link link = (Link) (linklist.get(linkitor.next()));// IP层上的链路
			for (VirtualLink Vlink : link.getVirtualLinkList()) { // IP层上链路对应的虚拟链路
				if (Vlink.getNature() == 0) {// 0为工作
//					System.out.println("因为属性不同删除的链路：" + link.getName());
					DelLinkList.add(Vlink);
					continue;
				}
				if (Vlink.getRestcapacity() < nodepair.getTrafficdemand()) {// 删去流量不够的链路
					DelLinkList.add(Vlink);
//					System.out.println("因为容量不够删除的链路：" + link.getName());
					continue;
				}
			}
			
			WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);

			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					nowdemand = wpr;// 先从所有建立的业务中找出本次业务
				}
			}
			// 删除与当前业务的工作路径的物理链路相交的虚拟保护路径
			for (VirtualLink Vlink : link.getVirtualLinkList()) {
				for (Link phylink : Vlink.getPhysicallink()) {
					if (nowdemand.getworklinklist().contains(phylink)) {
						DelLinkList.add(Vlink);
//						System.out.println("与当前业务相交而删除的虚拟链路 " + link.getName());
					}
				}
			}
		}
		
		WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);
		// 删除与当前业务的工作链路相交的业务的保护路径对应的虚拟链路
		for (WorkandProtectRoute wpr : wprlist) {
			if (wpr.getdemand().equals(nodepair)) {
				nowdemand = wpr;// 先从所有建立的业务中找出本次业务
			}
		}
		for (WorkandProtectRoute wpr : wprlist) {
			if (wpr.getdemand().equals(nodepair)) {
				continue;
			}
		
			int cross = t.linklistcompare(wpr.getworklinklist(), nowdemand.getworklinklist());// 两个工作业务相交删去之前建立业务的保护链路
			if (cross == 1) {
//				System.out.println("当前业务 ");
//				for (Link link1 : nowdemand.getworklinklist()) {
//					System.out.print(link1.getName() + "  ");
//				}
//				System.out.println();
//				System.out.println("之前建立的业务 " + wpr.getdemand().getName());
//
//				for (Link link1 : wpr.getworklinklist()) {
//					System.out.print(link1.getName() + "  ");
//				}
//				System.out.println();
				
				for (VirtualLink vlink : wpr.getprovirtuallinklist()) {
					if (!DelLinkList.contains(vlink)) {
//						System.out.println(
//								"删除与当前业务的工作链路相交的业务的保护路径对应的虚拟链路" + vlink.getSrcnode() + "  " + vlink.getDesnode());
						DelLinkList.add(vlink);
					}
				}
			}
		}
		for (VirtualLink dellink2 : DelLinkList) {
			if (!SumDelLinkList.contains(dellink2))
				SumDelLinkList.add(dellink2);
		}

		for (VirtualLink dellink2 : SumDelLinkList) {
			HashMap<String, Link> list = iplayer.getLinklist();
			Iterator<String> itor = list.keySet().iterator();
			while (itor.hasNext()) {// 第一部分 第一步
				Link link = (Link) (list.get(itor.next()));// IP层上的链路
//				System.out.println("虚拟链路："+dellink2.getSrcnode()+"-"+dellink2.getDesnode());
//				System.out.println("IP层链路："+link.getName());
				if (link.getNodeA().getName().equals(dellink2.getSrcnode())
						&& link.getNodeB().getName().equals(dellink2.getDesnode())) {// 找到虚拟链路对应的IP层链路
					link.getVirtualLinkList().remove(dellink2);// 删除对应的虚拟链路
				break;
				}
			}
		}
		HashMap<String, Link> list = iplayer.getLinklist();
		Iterator<String> itor = list.keySet().iterator();
		while (itor.hasNext()) {
			Link link = (Link) (list.get(itor.next()));
			if (link.getVirtualLinkList().size() == 0) {
				DelIPLinkList.add(link);
			}
		}
		// 找出所有需要删除的虚拟链路

		for (Link link : DelIPLinkList) {
//			System.out.println("删除的IP层链路为：" + link.getName());
			iplayer.removeLink(link.getName());
		}
		// 以上为判断ip层中的链路那些需要删除

		// 删除IP层上容量过剩的虚拟链路
		ArrayList<VirtualLink> DelhighcapVlink = new ArrayList<>();
		HashMap<String, Link> Dijlinklist = iplayer.getLinklist();
		Iterator<String> Dijlinkitor = Dijlinklist.keySet().iterator();
		while (Dijlinkitor.hasNext()) {
			double minrescapacity = 10000;
			Link Dijlink = (Link) (Dijlinklist.get(Dijlinkitor.next()));
			for (VirtualLink vlink : Dijlink.getVirtualLinkList()) {
				// System.out.println(vlink.getSrcnode()+"
				// "+vlink.getDesnode());
				if (vlink.getRestcapacity() < minrescapacity) {
					minrescapacity = vlink.getRestcapacity();
				}
			} // 找出一条IP链路上所有虚拟链路中剩余容量最少的数量

			for (VirtualLink vlink : Dijlink.getVirtualLinkList()) {
				if (vlink.getRestcapacity() > minrescapacity) {
					DelhighcapVlink.add(vlink);
				}
			}
			for (VirtualLink vlink : DelhighcapVlink) {
				Dijlink.getVirtualLinkList().remove(vlink);
			} // 将所有剩余容量大于最小剩余容量的链路全部放入集合 并删除
		}
		// test
		HashMap<String, Link> linklistt = iplayer.getLinklist();
		Iterator<String> linkitorr = linklistt.keySet().iterator();
		while (linkitorr.hasNext()) {
			Link link = (Link) (linklistt.get(linkitorr.next()));
			System.out.println("剩余的IP层链路为" + link.getName() + "  剩余的虚拟链路条数为：" + link.getVirtualLinkList().size()
					+ "虚拟链路上剩余的容量为：" + link.getVirtualLinkList().get(0).getRestcapacity());
			file_io.filewrite2(OutFileName,"剩余的IP层链路为" + link.getName() + "  剩余的虚拟链路条数为：" + link.getVirtualLinkList().size()
					+ "虚拟链路上剩余的容量为：" + link.getVirtualLinkList().get(0).getRestcapacity());
		}

		LinearRoute newRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);// 在iplayer里面找寻最短保护路径

		for (Link addlink : DelIPLinkList) {// 恢复iplayer
			iplayer.addLink(addlink);
		}
		DelIPLinkList.clear();

		if (newRoute.getNodelist().size() != 0) {
			ipproflag = true;
			System.out.print("**************保护路径在IP层找到路由  ");
			file_io.filewrite_without(OutFileName,"**************保护路径在IP层找到路由  ");
			newRoute.OutputRoute_node(newRoute);
			ArrayList<Link> totallink = new ArrayList<>();
			file_io.filewrite2(OutFileName,"IP路由上的链路：" );
			for (int c = 0; c < newRoute.getLinklist().size(); c++) {
				Link link = newRoute.getLinklist().get(c); // 找到的路由上面的link
				System.out.println("IP路由上的链路：" + link.getName());
				file_io.filewrite_without(OutFileName, link.getName()+"  ");
				/*
				 * 如果路由成功 则需要找到IP层上的link对应的虚拟链路 改变其容量
				 */

				HashMap<String, Link> linklist2 = iplayer.getLinklist();
				Iterator<String> linkitor2 = linklist2.keySet().iterator();
				while (linkitor2.hasNext()) {
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
					if (link1.getNodeA().getName().equals(link.getNodeA().getName())
							&& link1.getNodeB().getName().equals(link.getNodeB().getName())) {
				
						VirtualLink Vlink = link1.getVirtualLinkList().get(0);
						// System.out.println(Vlink.getSrcnode() + " " +Vlink.getDesnode() + " "+ Vlink.getRestcapacity());
						Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
						Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
						// System.out.println(Vlink.getRestcapacity());
						for (Link worklink : Vlink.getPhysicallink()) {// 将业务虚拟链路上对应的物理链路全部放在totallink中
							totallink.add(worklink);
						}
					}
				}
			}
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					wpr.setprolinklist(totallink);
				}
			}

		}
		// 恢复链路上对应的虚拟链路
		for (VirtualLink link : SumDelLinkList) {
			// System.out.println(link.getSrcnode()+"-"+link.getDesnode());
			HashMap<String, Link> linklist2 = iplayer.getLinklist();
			Iterator<String> linkitor2 = linklist2.keySet().iterator();
			while (linkitor2.hasNext()) {
				Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
				if (link1.getNodeA().getName().equals(link.getSrcnode())
						&& link1.getNodeB().getName().equals(link.getDesnode())) {
					link1.getVirtualLinkList().add(link);
				}
			}
		}
		SumDelLinkList.clear();

		for (VirtualLink link : DelhighcapVlink) {// 恢复因为容量过剩删除的虚拟链路
//			System.out.println("删除容量过剩的虚拟链路： " + link.getRestcapacity());
			// System.out.println(link.getSrcnode()+"-"+link.getDesnode());
			HashMap<String, Link> linklist2 = iplayer.getLinklist();
			Iterator<String> linkitor2 = linklist2.keySet().iterator();
			while (linkitor2.hasNext()) {
				Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
				if (link1.getNodeA().getName().equals(link.getSrcnode())
						&& link1.getNodeB().getName().equals(link.getDesnode())) {
					link1.getVirtualLinkList().add(link);
				}
			}
		}
		DelhighcapVlink.clear();
		return ipproflag;
	}

}
