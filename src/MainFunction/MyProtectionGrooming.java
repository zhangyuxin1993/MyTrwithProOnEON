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
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class MyProtectionGrooming {

	public void myprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag) {// flag=true表示保护IP层建立的工作路径
													// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
		boolean delflag = false;

		System.out.println("节点对：" + nodepair.getName() + "   flag=" + flag);
		ArrayList<Link> DelLinkList = new ArrayList<Link>();
		ArrayList<Link> SumDelLinkList = new ArrayList<Link>();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
		System.out.println("IP层的链路条数： "+iplayer.getLinklist().size());
		HashMap<String, Link> linklist = iplayer.getLinklist();
		Iterator<String> linkitor = linklist.keySet().iterator();
		while (linkitor.hasNext()) {
			Link link = (Link) (linklist.get(linkitor.next()));
			System.out.println("当前链路：" + link.getName());
			if (link.getNature() == 0) {// 删去属性为工作的链路
				DelLinkList.add(link);
				continue;
			}
			if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {// 删去流量不够的链路
				DelLinkList.add(link);
				continue;
			}
			/*
			 * 在IP层保护路径与工作路径对应光层的链路不能重合 故要在ip层路由保护时应先删除 工作路由对应的
			 */
			delflag = false;
			for (Link LinkOnRoute : route.getLinklist()) {// 取出工作路由中的链路
				if (delflag)
					break;
				System.out.println("工作路径上的链路： " + LinkOnRoute.getName());
				if (flag) {// flag为true则表示保护 IP层建立的工作路径
					for (Link LinkOnPhy : LinkOnRoute.getPhysicallink()) {// 取出某一工作链路上对应的物理链路
						if (delflag)
							break;
						for (Link LinkInIPlayer : link.getPhysicallink()) {
							if (LinkOnPhy.getName().equals(LinkInIPlayer.getName())) {// 这里可不可以不加getname??
								DelLinkList.add(link);
								delflag = true;
							}
							if (delflag)
								break;
						}

					}
				} else {// flag为false则表示保护 光层建立的工作路径
					for (Link LinkInIPlayer : link.getPhysicallink()) {
						if (delflag)
							break;
						System.out.println("IP层的link对应的物理层的链路：  " + LinkInIPlayer.getName());
						if (LinkOnRoute.getName().equals(LinkInIPlayer.getName())) {// link上两个节点位置会不会影响？？
							DelLinkList.add(link);
							delflag = true;
						}
						if (delflag)
							break;
					}
				}
			}
		}
		for (Link dellink : DelLinkList) {
			if (!SumDelLinkList.contains(dellink))
				SumDelLinkList.add(dellink);
		}
		for (Link dellink2 : SumDelLinkList) {
			iplayer.removeLink(dellink2.getName());
		} // 移除去所有不符合要求的link

		LinearRoute newRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);// 在iplayer里面找寻最短保护路径

		for (Link addlink : SumDelLinkList) {// 恢复iplayer
			iplayer.addLink(addlink);
		}
		SumDelLinkList.clear();
		DelLinkList.clear();

		if (newRoute.getNodelist().size() != 0) {
			System.out.println("保护路由在IP层上路由成功  ");
			newRoute.OutputRoute_node(newRoute);

			for (Link LinkOnNewRoute : newRoute.getLinklist()) {// 路由成功改变link上面的流量值
				LinkOnNewRoute.setFlow(LinkOnNewRoute.getFlow() + nodepair.getTrafficdemand());
			}
		} else {
			ArrayList<Link> opDelLink = new ArrayList<Link>();
			System.out.println("保护路由在IP层不能路由，需要在光层新建");
			// 删除工作路由经过的所有物理链路
			for (Link LinkOnRoute : route.getLinklist()) {// 取出工作路由中的链路
				System.out.println("物理层上的工作路径链路："+LinkOnRoute.getName());
				if (flag) {
					for (Link LinkOnPhy : LinkOnRoute.getPhysicallink()) {// 取出某一工作链路上对应的物理链路
						HashMap<String, Link> oplinklist = oplayer.getLinklist();
						Iterator<String> oplinkitor = oplinklist.keySet().iterator();
						while (oplinkitor.hasNext()) {
							Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
							System.out.println("物理层链路遍历："+oplink.getName());
							if (oplink.getName().equals(LinkOnPhy.getName())) {
								opDelLink.add(oplink);
								break;
							}
						}
					}
				} else {
					HashMap<String, Link> oplinklist = oplayer.getLinklist();
					Iterator<String> oplinkitor = oplinklist.keySet().iterator();
					while (oplinkitor.hasNext()) {
						Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
						System.out.println("物理层链路遍历："+oplink.getName());
						if (oplink.getName().equals(LinkOnRoute.getName())) {
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
				WorkingGrooming wg = new WorkingGrooming();
				index_wave = wg.spectrumallocationOneRoute(opPrtectRoute);
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
					String name = opsrcnode.getName() + "-" + opdesnode.getName();
					int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的
																// 故这样设置index
					Link newlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost,1);
					iplayer.addLink(newlink);
					newlink.setNature(1);
					newlink.setFlow(nodepair.getTrafficdemand());
					newlink.setSumflow(slotnum * X);// 多出来的flow是从这里产生的
					newlink.setIpremainflow(newlink.getSumflow() - newlink.getFlow());
					System.out.println(newlink.getName() + " 上面的已用flow: " + newlink.getFlow() + "    共有的flow:  "
							+ newlink.getSumflow() + "    预留的flow：  " + newlink.getIpremainflow());
					numOfTransponder = numOfTransponder + 2;
					newlink.setPhysicallink(opPrtectRoute.getLinklist());
				}
			}
		}
	}
}
