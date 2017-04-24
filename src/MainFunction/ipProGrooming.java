package MainFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class ipProGrooming {
	public boolean ipprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag) {// flag=true表示保护IP层建立的工作路径
													// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
		boolean ipproflag=false;
		boolean delflag = false;

		System.out.println("节点对：" + nodepair.getName() + "   flag=" + flag);
		ArrayList<VirtualLink> DelLinkList = new ArrayList<VirtualLink>();
		ArrayList<VirtualLink> SumDelLinkList = new ArrayList<VirtualLink>();
		ArrayList<Link> DelIPLinkList = new ArrayList<Link>();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
//		System.out.println("IP层的链路条数： " + iplayer.getLinklist().size());

		HashMap<String, Link> linklist = iplayer.getLinklist();
		Iterator<String> linkitor = linklist.keySet().iterator();
		while (linkitor.hasNext()) {// 第一部分 第一步
			Link link = (Link) (linklist.get(linkitor.next()));
//			System.out.println("当前IP链路：" + link.getName());
			for (VirtualLink Vlink : link.getVirtualLinkList()) {// 第一部分 第二步 // 取出虚拟链路
//				System.out.println("虚拟链路条数 ：" + link.getVirtualLinkList().size());												
				if (Vlink.getNature() == 0) {// 删去属性为工作的链路
					DelLinkList.add(Vlink);
					continue;
				}
				if (Vlink.getRestcapacity() < nodepair.getTrafficdemand()) {// 删去流量不够的链路
					DelLinkList.add(Vlink);
					continue;
				}
				/*
				 * 在IP层保护路径与工作路径对应光层的链路不能重合 故要在ip层路由保护时应先删除 工作路由对应的物理链路
				 */
				delflag = false;
//				System.out.println("该IPlink上虚拟链路: "+Vlink.getSrcnode()+"  "+Vlink.getDesnode());
				for (Link linkOnphy : Vlink.getPhysicallink()) {// 第一部分 第三步
																// 取出虚拟链路对应的光路
//					System.out.println("该IPlink上虚拟链路对应的光层链路： "+linkOnphy.getName());
					if (delflag)
						break;
					for (Link LinkOnRoute : route.getLinklist()) {// 第二三部分 共用 取出工作路由中的链路
					
						if (delflag)
							break;
//						System.out.println("工作路径上的链路： " + LinkOnRoute.getName());

						if (flag) {// flag为true则表示保护 IP层建立的工作路径
//							System.out.println("工作路径上的IPlink对应的虚拟链路条数： "+LinkOnRoute.getVirtualLinkList().size());
							for (VirtualLink WorkLinkVritual : LinkOnRoute.getVirtualLinkList()) {// 第二部分第二步
																									// 取出在IP层路由的工作路径上链路对应的虚拟链路（此时只剩保护虚拟链路）

//								System.out.println("工作路径上的虚拟链路： " + WorkLinkVritual.getSrcnode() + "  "
//										+ WorkLinkVritual.getDesnode() + "   nature:  " + WorkLinkVritual.getNature());
								if (delflag)
									break;
								for (Link WorkLinkOnPhy : WorkLinkVritual.getPhysicallink()) {// 第二部分第三步
//									System.out.println("在IP层建立的工作路径上的虚拟链路对应的光层链路： " + WorkLinkOnPhy.getName());
//									System.out.println("该IP链路上虚拟链路经过的物理链路：  "+linkOnphy.getName());
									if (linkOnphy.getName().equals(WorkLinkOnPhy.getName())) {
										DelLinkList.add(Vlink);
										delflag = true;
									}
									if (delflag)
										break;
								}
							}
						} else {// flag为false则表示保护 光层建立的工作路径
							System.out.println("该条IP链路上虚拟链路对应的光路：   " + linkOnphy.getName());
							System.out.println("工作路径对应的光路：   " + LinkOnRoute.getName());
							if (linkOnphy.getName().equals(LinkOnRoute.getName())) {
								DelLinkList.add(Vlink);
								delflag = true;
							}
						}
					}
				}
			} // 这个大循环为了删除不可用的虚拟链路
			
			for (VirtualLink dellink : DelLinkList) {
				if (!SumDelLinkList.contains(dellink))
					SumDelLinkList.add(dellink);
			}
			for (VirtualLink dellink2 : SumDelLinkList) {
				link.getVirtualLinkList().remove(dellink2);
			} // 移除去所有不符合要求的link
			if (link.getVirtualLinkList().size() == 0) {
				DelIPLinkList.add(link);
			}
		}

		for (Link link : DelIPLinkList) {
//			 System.out.println("删除的IP层链路为："+link.getName());
			iplayer.removeLink(link.getName());
		}
   //以上为判断ip层中的链路那些需要删除
		
		HashMap<String, Link> Dijlinklist = iplayer.getLinklist();
		Iterator<String> Dijlinkitor = Dijlinklist.keySet().iterator();
		while (Dijlinkitor.hasNext()) {
			double mincost=10000;
			double Dijlength=0;
			Link Dijlink = (Link) (Dijlinklist.get(Dijlinkitor.next()));
			for(VirtualLink vlink:Dijlink.getVirtualLinkList()){
//				System.out.println(vlink.getSrcnode()+"   "+vlink.getDesnode());
				if(vlink.getcost()<mincost){
					mincost=vlink.getcost();
					Dijlength=vlink.getlength();
				}
			}
			Dijlink.setCost(mincost);
			Dijlink.setLength(Dijlength);
			System.out.println("!!!!!!!!改变长度的链路： "+Dijlink.getName()+"   长度为："+Dijlink.getLength()+"    cost:  "+Dijlink.getCost());
		}
	 
		LinearRoute newRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);// 在iplayer里面找寻最短保护路径

		for (Link addlink : DelIPLinkList) {// 恢复iplayer
			iplayer.addLink(addlink);
		}
		DelIPLinkList.clear();
		
		if (newRoute.getNodelist().size() != 0) {
			ipproflag=true;
			System.out.println("**************保护路由在IP层上路由成功  ");
			newRoute.OutputRoute_node(newRoute);

			for (int c = 0; c < newRoute.getLinklist().size(); c++) {
				Link link = newRoute.getLinklist().get(c); // 找到的路由上面的link
				System.out.println("光层路由上的链路：" + link.getName());
				/*
				 * 如果路由成功 则需要找到IP层上的link对应的虚拟链路 改变其容量
				 */
				boolean delflag_pro = false;
				double minCapacity = 100000;
				HashMap<String, Link> linklist2 = iplayer.getLinklist();
				Iterator<String> linkitor2 = linklist2.keySet().iterator();
				while (linkitor2.hasNext()) {
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
					if (link1.getNodeA().getName().equals(link.getNodeA().getName())
							&& link1.getNodeB().getName().equals(link.getNodeB().getName())) {
						System.out.println("找到路由经过的链路： " + link1.getName());
						for (VirtualLink Vlink : link1.getVirtualLinkList()) {
							if (Vlink.getNature() == 1) {
								/*
								 * 若有多条virtuallink在link上面 则找到剩余容量最少的那条虚拟link使用
								 * 改变其剩余容量值
								 */
//								System.out.println(Vlink.getSrcnode() + "  " + Vlink.getDesnode());
								if (Vlink.getRestcapacity() < minCapacity) {// 找出剩余容量最少的虚拟链路（如果有多条虚拟链路可用
																			// 可以在这里修改选择）
									minCapacity = Vlink.getRestcapacity();
									System.out.println(minCapacity);
								}
							}
						}
						for (VirtualLink Vlink : link1.getVirtualLinkList()) {
							if (Vlink.getNature() == 1) {
								if (Vlink.getRestcapacity() == minCapacity) { // 修改路由之后虚拟链路上的链路容量
//									System.out.println(Vlink.getSrcnode() + "  " + Vlink.getDesnode() + "  "
//											+ Vlink.getRestcapacity());
									Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
									Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
//									System.out.println(Vlink.getRestcapacity());
									delflag_pro = true;
									break;
								}
							}
						}
						if (delflag_pro)
							break;
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
		}
		for (VirtualLink link : SumDelLinkList) {
//			 System.out.println(link.getSrcnode()+"-"+link.getDesnode());
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
		return ipproflag;	
	}

}
