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

public class MyProtectionGrooming {

	public void myprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag) {// flag=true表示保护IP层建立的工作路径
													// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
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
			System.out.println("当前IP链路：" + link.getName());
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
		LinearRoute newRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);// 在iplayer里面找寻最短保护路径

		for (Link addlink : DelIPLinkList) {// 恢复iplayer
			iplayer.addLink(addlink);
		}
		DelIPLinkList.clear();
		// SumDelLinkList.clear();
		// DelLinkList.clear();

		if (newRoute.getNodelist().size() != 0) {
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
								System.out.println(Vlink.getSrcnode() + "  " + Vlink.getDesnode());
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
									System.out.println(Vlink.getSrcnode() + "  " + Vlink.getDesnode() + "  "
											+ Vlink.getRestcapacity());
									Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
									Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
									System.out.println(Vlink.getRestcapacity());
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
		} else {// IP层路由不成功 要新建保护光路
				// 先恢复虚拟链路
			for (VirtualLink link : SumDelLinkList) {
//				System.out.println("恢复虚拟链路： " + link.getSrcnode() + "-" + link.getDesnode());
				HashMap<String, Link> linklist2 = iplayer.getLinklist();
				Iterator<String> linkitor2 = linklist2.keySet().iterator();
				while (linkitor2.hasNext()) {
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link

					if (link1.getNodeA().getName().equals(link.getSrcnode())
							&& link1.getNodeB().getName().equals(link.getDesnode())) {
						link1.getVirtualLinkList().add(link);
//						System.out.println("IP LINK:" + link1.getName() + "********恢复之后的虚拟链路条数："
//								+ link1.getVirtualLinkList().size());
					}
				}
			}

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
							System.out.println("删除的光层链路： "+oplink.getName());
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
					VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
					Vlink.setnature(1);
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
									+ Vlink.getUsedcapacity() + "\n " + "共有的flow:  " + Vlink.getFullcapacity()
									+ "    预留的flow：  " + Vlink.getRestcapacity());

						}
					}

					// debug
//					HashMap<String, Link> linklist4 = iplayer.getLinklist();
//					Iterator<String> linkitor4 = linklist4.keySet().iterator();
//					while (linkitor4.hasNext()) {
//						Link link1 = (Link) (linklist4.get(linkitor4.next()));// IPlayer里面的link
//						System.out.println("IP LINK:" + link1.getName() + "   链路上面的虚拟链路数：" + link1.getVirtualLinkList().size());


//					}
				}
			}
		}
	}
}
