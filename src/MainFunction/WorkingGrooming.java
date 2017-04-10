package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class WorkingGrooming {
	
	public void WorkingGrooming( Network network, Layer iplayer, Layer oplayer) {
		RouteSearching Dijkstra = new RouteSearching();
		int numOfTransponder = 0;

		ArrayList<Link> DelLinklist = new ArrayList<Link>();
		ArrayList<Link> SumDelLinklist = new ArrayList<Link>();
		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		// for (NodePair nodepair : demandlist) {
		// System.out.println(nodepair.getName() + " " +
		// nodepair.getTrafficdemand());
		// }
		// 操作list里面的节点对
		for (int n = 0; n < demandlist.size(); n++) {
			NodePair nodepair = demandlist.get(n);

			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();

			System.out.println("正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());

			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link link = (Link) (linklist.get(linkitor.next()));
				System.out.println("链路名字： " + link.getName());
				if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {
					System.out.println("link上的总流量：" + link.getSumflow());
					System.out.println("link上的已使用流量：" + link.getFlow());
					DelLinklist.add(link);
				} // 移除容量不够的链路
				if (link.getNature() == 1) {// 保护是1 工作是0
					DelLinklist.add(link);
				} // 移除属性不对的链路
			}
			for (Link nowlink : DelLinklist) {
				if (!SumDelLinklist.contains(nowlink)) {
					SumDelLinklist.add(nowlink);
				}
			}
			for (Link nowlink : SumDelLinklist) {
				iplayer.removeLink(nowlink.getName());
			}

			LinearRoute newRoute = new LinearRoute(null, 0, null);
			Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);

			// 恢复iplayer里面删除的link
			for (Link nowlink : SumDelLinklist) {
				iplayer.addLink(nowlink);
			}
			SumDelLinklist.clear();
			DelLinklist.clear();

			// 储存dijkstra经过的链路 并且改变这些链路上的容量
			if (newRoute.getLinklist().size() != 0) {// 工作路径路由成功
				System.out.println("********在IP层找到路由！");
				newRoute.OutputRoute_node(newRoute);

				ArrayList<Link> newrouteLinklist = new ArrayList<Link>();
				for (int c = 0; c < newRoute.getLinklist().size(); c++) {
					Link link = newRoute.getLinklist().get(c);

					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));
						if (link.getNodeA().getName().equals(link1.getNodeA().getName())
								&& link.getNodeB().getName().equals(link1.getNodeB().getName())
								&& link1.getNature() == 0) {
							link1.setFlow(link1.getFlow() + nodepair.getTrafficdemand());
							link1.setIpremainflow(link1.getSumflow()-link1.getFlow());
							System.out.println("链路 " + link1.getName() + "上已经使用的流量" + link1.getFlow() + "  链路上剩余容量 ="
									+ link1.getIpremainflow());
							newrouteLinklist.add(link);
						}
					}
				}
			}

			// 以上工作路由路由成功
			else {
				System.out.println("IP层工作路由不成功，需要新建光路");
				Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
				Node opdesnode = oplayer.getNodelist().get(desnode.getName());
				// System.out.println("源点： " + opsrcnode.getName() + " 终点： " +
				// opdesnode.getName());

				// 在光层新建光路的时候不需要考虑容量的问题
				LinearRoute opnewRoute = new LinearRoute(null, 0, null);
				Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opnewRoute, null);

				if (opnewRoute.getLinklist().size() == 0) {
					System.out.println("工作无路径");
				} else {
					System.out.println("在物理层路由经过的节点如下：------");
					opnewRoute.OutputRoute_node(opnewRoute);

					int slotnum = 0;
					int IPflow = nodepair.getTrafficdemand();
					double X = 1;// 2000-4000 BPSK,1000-2000
									// QBSK,500-1000，8QAM,0-500 16QAM
					double routelength = opnewRoute.getlength();
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

					opnewRoute.setSlotsnum(slotnum);
					System.out.println("该链路所需slot数： " + slotnum);
					ArrayList<Integer> index_wave = new ArrayList<Integer>();
					index_wave = spectrumallocationOneRoute(opnewRoute);
					if (index_wave.size() == 0) {
						System.out.println("路径堵塞 ，不分配频谱资源");
					} else {
						double length = 0;
						double cost = 0;
						for (Link link : opnewRoute.getLinklist()) {
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
						Link newlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
						iplayer.addLink(newlink);
						newlink.setNature(0);
						newlink.setFlow(nodepair.getTrafficdemand());
						newlink.setSumflow(slotnum * X);// 多出来的flow是从这里产生的
						newlink.setIpremainflow(newlink.getSumflow()-newlink.getFlow());
						System.out.println(newlink.getName() + " 上面的已用flow: " + newlink.getFlow() + "    共有的flow:  "
								+ newlink.getSumflow() + "    预留的flow：  " + newlink.getIpremainflow());
						numOfTransponder = numOfTransponder + 2;
						newlink.setPhysicallink(opnewRoute.getLinklist());
					}
				}
			}
		}
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
	private ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		linklistOnroute = route.getLinklist();
		route.OutputRoute_node(route);// debug
		for (Link link : linklistOnroute) {
			if (route.getSlotsnum() == 0) {
				System.out.println("路径上没有slot需要分配");
				break;
			}
			link.getSlotsindex().clear();
			// slotarray和slotindex的区别？？
			for (int start = 0; start < link.getSlotsarray().size() - route.getSlotsnum(); start++) {// 查找可用slot的起点
				int flag = 0;
				for (int num = start; num < route.getSlotsnum() + start; num++) {
					if (link.getSlotsarray().get(num).getoccupiedreqlist().size() != 0) {// 该波长已经被占用
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					link.getSlotsindex().add(start);
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
				sameindex.add(index);
			}
		}
		return sameindex;
	}
}
 
