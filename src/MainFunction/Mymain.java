﻿package MainFunction;

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

public class Mymain {

	public static void main(String[] args) {

		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("G:/Topology/6.csv");
		network.copyNodes();//
		network.createNodepair();// 每个layer都生成节点对 产生节点对的时候会自动生成nodepair之间的demand

		Layer iplayer = network.getLayerlist().get("Layer0");
		Layer oplayer = network.getLayerlist().get("Physical");

		Mymain mm = new Mymain();
		mm.grooming(network, iplayer, oplayer);
	}

	public void grooming(Network network, Layer iplayer, Layer oplayer) {
		int numOfTransponder=0;
		ReadFlowFile rff = new ReadFlowFile();
		rff.Readflow(iplayer, "G:/Topology/6.csv");// Q1 这里是干嘛用的
		// /*
		ArrayList<Link> DelLinklist = new ArrayList<Link>();
		// ArrayList<Link> NatureDelLinklist=new ArrayList<Link>();
		ArrayList<Link> SumDelLinklist = new ArrayList<Link>();
		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		// 操作list里面的节点对
		for (int n = 0; n < demandlist.size(); n++) {
			NodePair nodepair = demandlist.get(n);
			System.out.println("正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());

			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link link = (Link) (linklist.get(linkitor.next()));
				System.out.println("链路名字： " + link.getName());
				if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {
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

			// 将iplayer里面的link copy到copylayer里面去
			Layer ipcopylayer = network.getLayerlist().get("ipcopylayer");
			HashMap<String, Link> linklist1 = iplayer.getLinklist();
			Iterator<String> linkitor1 = linklist1.keySet().iterator();
			while (linkitor1.hasNext()) {
				Link link = (Link) (linklist1.get(linkitor1.next()));

				Node srcnode = ipcopylayer.getNodelist().get(link.getNodeA().getName());
				Node desnode = ipcopylayer.getNodelist().get(link.getNodeB().getName());

				String name = srcnode.getName() + "-" + desnode.getName();
				int index = iplayer.getLinkNum();
				double length = link.getLength();
				double cost = link.getCost();
				Link addlink = new Link(name, index, null, ipcopylayer, srcnode, desnode, length, cost);
				ipcopylayer.addLink(addlink);
			}

			// 在ipcopylayer里面找寻最短路径
			Node srcnode = ipcopylayer.getNodelist().get(nodepair.getSrcNode().getName());
			Node desnode = ipcopylayer.getNodelist().get(nodepair.getDesNode().getName());
			LinearRoute newRoute = new LinearRoute(null, 0, null);
			RouteSearching Dijkstra = new RouteSearching();
			Dijkstra.Dijkstras(srcnode, desnode, ipcopylayer, newRoute, null);

			// 恢复iplayer里面删除的link
			for (Link nowlink : SumDelLinklist) {
				iplayer.addLink(nowlink);
			}
			SumDelLinklist.clear();
			DelLinklist.clear();

			// 清空ipcopylayer里面的link
			ArrayList<Link> CopyDelLinklist = new ArrayList<Link>();
			HashMap<String, Link> copylinklist = ipcopylayer.getLinklist();
			Iterator<String> copylinkitor = copylinklist.keySet().iterator();
			while (copylinkitor.hasNext()) {
				Link link = (Link) (copylinklist.get(copylinkitor.next()));
				CopyDelLinklist.add(link);
			}
			for (Link nowlink : CopyDelLinklist) {
				ipcopylayer.removeLink(nowlink.getName());
			}
			CopyDelLinklist.clear();

			// 储存dijkstra经过的链路 并且改变这些链路上的容量
			if (newRoute.getLinklist().size() != 0) {// 工作路径路由成功
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
							newrouteLinklist.add(link);
						}
					}
				}
			}
			// 以上工作路由路由成功
			else {
				System.out.println("IP层工作路由不成功，需要新建光路");
				Node opsrcnode = oplayer.getNodelist().get(nodepair.getSrcNode().getName());
				Node opdesnode = oplayer.getNodelist().get(nodepair.getDesNode().getName());
				System.out.println("源点： " + opsrcnode.getName() + " 终点：　" + opdesnode.getName());

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

					ArrayList<Integer> index_wave = new ArrayList<Integer>();
					index_wave = spectrumallocationOneRoute(opnewRoute);
					if (index_wave.size() == 0) {
						System.out.println("路径堵塞 ，不分配频谱资源");
					}
					else{
						double length=0;
						double cost=0;	
						for(Link link:opnewRoute.getLinklist()){
							length=length+link.getLength();
							cost=cost+link.getCost();
							Request request=null;
							ResourceOnLink ro=new ResourceOnLink(request, link, index_wave.get(0), slotnum);
							//这里与原版不一致 将两个循环写在一起
							link.setMaxslot(slotnum+link.getMaxslot());
							System.out.println("链路 "+link.getName()+" 的最大slot是： "+link.getMaxslot());		
						}					
						String name=opsrcnode.getName() + "-" + 0 + "-" + opdesnode.getName();//为什么这样设置name
						int index=iplayer.getLinklist().size();//因为iplayer里面的link是一条一条加上去的 故这样设置index
						Link newlink=new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
						iplayer.addLink(newlink);
						newlink.setNature(0);
						newlink.setFlow(nodepair.getTrafficdemand());
						newlink.setSumflow(slotnum*X);//多出来的flow是从这里产生的
						newlink.setIpremainflow(newlink.getSumflow() - newlink.getFlow());
						System.out.println(newlink.getIpremainflow());//与原版不一致
						numOfTransponder=numOfTransponder+2;
						newlink.setPhysicallink(opnewRoute.getLinklist());
					}
				}

			}

		}
		// */
	}

	private ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		linklistOnroute = route.getLinklist();
		route.OutputRoute_node(route);// debug
		for (Link link : linklistOnroute) {
			System.out.println("链路： " + link.getName());
			System.out.println("route上所需的slot: " + route.getSlotsnum());
			if (route.getSlotsnum() == 0) {
				System.out.println("路径上没有slot需要分配");
				break;
			}
			link.getSlotsindex().clear();
			int flag = 0;// slotarray和slotindex的区别？？
			for (int start = 0; start < link.getSlotsarray().size() - route.getSlotsnum(); start++) {// 查找可用slot的起点
				System.out.println(link.getSlotsarray().size());
				for (int num = start; num < route.getSlotsnum(); num++) {
					if (link.getSlotsarray().get(num).getoccupiedreqlist().size() != 0) {// 该波长已经被占用
						flag = 1;
						break;
					}

					if (flag == 0) {
						link.getSlotsindex().add(start);
					}
				}
			}
		} // 以上所有的link分配完

		Link firstlink = linklistOnroute.get(0);
		System.out.println(firstlink.getName());
		ArrayList<Integer> sameindex = new ArrayList<Integer>();
		sameindex.clear();

		for (int s = 0; s < firstlink.getSlotsindex().size(); s++) {
			System.out.println(firstlink.getSlotsindex().size());
			int index = firstlink.getSlotsindex().get(s);
			int flag = 1;

			for (Link otherlink : linklistOnroute) {
				if (!otherlink.getSlotsindex().contains(index)) {
					flag = 0;
					break;
				}
			}
			if (flag == 1) {
				sameindex.add(s);
			}
		}
		return sameindex;
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
}
