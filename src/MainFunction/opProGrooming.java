package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class opProGrooming {
	public void opprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag) {// flag=true表示保护IP层建立的工作路径
													// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();

		ArrayList<Link> opDelLink = new ArrayList<Link>();
		System.out.println("************保护路由在IP层不能路由，需要在光层新建");
		// 删除该节点对的工作路由经过的所有物理链路

		for (Link LinkOnRoute : route.getLinklist()) {// 取出工作路由中的链路
			System.out.println("物理层上的工作路径链路：" + LinkOnRoute.getName());
			if (flag) {//// flag=true表示保护 IP层建立的工作路径
				for (VirtualLink Vlink : LinkOnRoute.getVirtualLinkList()) {

					for (Link LinkOnPhy : Vlink.getPhysicallink()) {// 取出某一工作链路上对应的物理链路

						HashMap<String, Link> oplinklist = oplayer.getLinklist();
						Iterator<String> oplinkitor = oplinklist.keySet().iterator();
						while (oplinkitor.hasNext()) {
							Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
							// System.out.println("物理层链路遍历：" +
							// oplink.getName());
							if (oplink.getName().equals(LinkOnPhy.getName())) {
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
						System.out.println("删除的光层链路： " + oplink.getName());
						opDelLink.add(oplink);

						break;
					}
				}
			}
		}

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
		} else {
			System.out.println("新建的光层保护路径为:");
			opPrtectRoute.OutputRoute_node(opPrtectRoute);
			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000，8QAM,0-500 16QAM
			double routelength = opPrtectRoute.getlength();
			// System.out.println("物理路径的长度是："+routelength);
			// 通过路径的长度来变化调制格式
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
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain mm = new Mymain();
			index_wave = mm.spectrumallocationOneRoute(opPrtectRoute);
			if (index_wave.size() == 0) {
				System.out.println("路径堵塞 ，不分配频谱资源");
			} else {
				double length = 0;
				double cost = 0;
				for (Link link : opPrtectRoute.getLinklist()) {
					length = length + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);

					link.setMaxslot(slotnum + link.getMaxslot());
					// System.out.println("链路 " + link.getName() + "
					// 的最大slot是： " + link.getMaxslot()+
					// " 可用频谱窗数： "+link.getSlotsindex().size());
				}
				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
				Vlink.setnature(1);
				Vlink.setlength(length);
				Vlink.setcost(cost);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
				Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setPhysicallink(opPrtectRoute.getLinklist());
                                         
				numOfTransponder = numOfTransponder + 2;

				// 从IP层中找出需要添加虚拟链路的link
				HashMap<String, Link> linklist3 = iplayer.getLinklist();
				Iterator<String> linkitor3 = linklist3.keySet().iterator();
				while (linkitor3.hasNext()) {
					Link link = (Link) (linklist3.get(linkitor3.next()));
					if (link.getNodeA().getName().equals(Vlink.getSrcnode())
							&& link.getNodeB().getName().equals(Vlink.getDesnode())) {
						link.getVirtualLinkList().add(Vlink);

						System.out.println("新建的保护光路 " + link.getName() + " 其对应的虚拟链路上面的已用flow: "
								+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
								+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
								+"   "+"虚拟链路cost： "+ Vlink.getcost());
					}
				}

				// debug
				// HashMap<String, Link> linklist4 = iplayer.getLinklist();
				// Iterator<String> linkitor4 = linklist4.keySet().iterator();
				// while (linkitor4.hasNext()) {
				// Link link1 = (Link) (linklist4.get(linkitor4.next()));//
				// IPlayer里面的link
				// System.out.println("IP LINK:" + link1.getName() + "
				// 链路上面的虚拟链路数：" + link1.getVirtualLinkList().size());

				// }
			}
		}
	}
}
