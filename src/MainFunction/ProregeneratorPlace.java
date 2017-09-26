package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class ProregeneratorPlace {
	public int newFS = 0;
	static int totalregNum = 0;
	public boolean proregeneratorplace(NodePair nodepair, LinearRoute newRoute, ArrayList<WorkandProtectRoute> wprlist,
			double routelength, Layer oplayer, Layer ipLayer, int IPflow) {
		ArrayList<WorkandProtectRoute> haveRegRoute = new ArrayList<>();
		WorkandProtectRoute nowdemand=new WorkandProtectRoute(null);
		ArrayList<VirtualLink> provirtuallinklist = new ArrayList<>();
		ProregeneratorPlace rgp2 = new ProregeneratorPlace();
		Test t = new Test();
		ArrayList<Integer> ShareReg = new ArrayList<>();
		ArrayList<RouteAndRegPlace> regplaceoption = new ArrayList<>();
		ProregeneratorPlace rgp = new ProregeneratorPlace();
		
		// part1 找到该保护链路上面已存在的共享再生器
		for (WorkandProtectRoute wpr : wprlist) {// 在已存在的业务中 找出新业务上已存在的共享再生器
			ArrayList<Node> regnodelist = new ArrayList<>();
			regnodelist = wpr.getregnodelist(); // 已存在业务中的一条业务保护路径上再生器节点
			for (Node node : regnodelist) {
				if (newRoute.getNodelist().contains(node)) {// 如果之前的业务在某一节点上已经放置了再生器
					// 判断该业务与新业务可否共享再生器（两个业务的工作链路对应的物理链路是否交叉）
					for(WorkandProtectRoute nowwpr:wprlist){
						if(nowwpr.getdemand().equals(nodepair))
							nowdemand=nowwpr;
					}
					int cross = t.linklistcompare(nowdemand.getworklinklist(), wpr.getworklinklist());
					if (cross == 0) {
						haveRegRoute.add(wpr);// 保存可以共享的业务
						int po = t.nodeindexofroute(node, newRoute);// 保存新链路上可以共享的再生器的位置
						if (po != 0 && po != newRoute.getNodelist().size() - 1) {
							if (!ShareReg.contains(po))
								ShareReg.add(po); // 保存了新的业务上哪些节点上面有再生器
						}
					}
				}
			}
		}
		// part1 finish 存储了所有该链路上可共享再生器的位置
		boolean partworkflag = false, RSAflag = false, regflag = false, success = false, haveflag = false,
				passflag = false;
		double length = 0;
		ArrayList<Link> linklist = new ArrayList<>();

		int minRegNum = (int) Math.floor(routelength / 4000);
		int internode = newRoute.getNodelist().size() - 2, FStotal = 0, n = 0;
		// part2 当路由上共享再生器的个数小于所需再生器的最小个数时 给定set进行RSA 产生regplaceoption
		if (ShareReg.size() <= minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (partworkflag)
					break;
				passflag = false;
				Test nOfm = new Test(s, internode); // 在所有中间节点中随机选取m个点来放置再生器
				while (nOfm.hasNext()) {
					int[] set = nOfm.next(); // 随机产生的再生器放置位置
					for (int num : ShareReg) {
						for (int k = 0; k < set.length; k++) {
							if (num == set[k]) {
								break;
							}
							if (k == set.length - 1 && num != set[k]) {
								passflag = true;
							}
						}
						if (passflag)
							break;
					}
					if (passflag)
						break;// 已有的共享再生器 已经内定所以所有产生的可能性中要包含这些再生器

					// 给定再生器节点之后进行RSA 产生option选项的路径
					partworkflag = rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption);
				}
			}
		}

		// part3 当路由上共享再生器的个数大于所需再生器的最小个数时 给定set进行RSA产生regplaceoption
		if (ShareReg.size() > minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (partworkflag)
					break;
				passflag = false;
				Test nOfm = new Test(s, internode); // 在所有中间节点中随机选取m个点来放置再生器
				while (nOfm.hasNext()) {
					int[] set = nOfm.next(); // 随机产生的再生器放置位置
					if (s <= ShareReg.size()) {
						for (int p = 0; p < set.length; p++) {
							int p1 = set[p];
							if (!ShareReg.contains(p1)) {
								passflag = true;
								break;
							}
						}
						if (passflag)
							break;
					}
					if (s > ShareReg.size()) {
						for (int num : ShareReg) {
							for (int k = 0; k < set.length; k++) {
								if (num == set[k]) {
									break;
								}
								if (k == set.length - 1 && num != set[k]) {
									passflag = true;
								}
							}
							if (passflag)
								break;
						}
						if (passflag)
							break;
					} // 以上主要为了产生set
						// 给定再生器节点之后进行RSA
					partworkflag = rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption);
				}
			}
		}
		// part4 对产生的备选链路进行筛选并且对选中链路建立IP链路
		if (regplaceoption.size() > 0) {
			success = true;
			RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 1);
			if (regplaceoption.size() > 1)
				finalRoute = rgp2.optionRouteSelect(regplaceoption, wprlist);// 在符合条件的几条路由中选取最佳的路由作为finaroute
			else
				finalRoute = regplaceoption.get(0);
			// 接下来对该最终链路进行RSA
			rgp2.FinalRouteRSA(nodepair, finalRoute, oplayer, ipLayer, IPflow, wprlist, provirtuallinklist,ShareReg);
		}
		if (regplaceoption.size() == 0) {
			success = false;
		}
		System.out.println();
		if (success) {
			System.out.print("保护路径再生器放置成功并且RSA,放置的再生器个数为");
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					System.out.println(wpr.getregnodelist().size());
				}
			}

		} else
			System.out.println("保护路径放置再生器不成功改路径被堵塞");
		return success;

	}

	public void FinalRouteRSA(NodePair nodepair, RouteAndRegPlace finalRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<WorkandProtectRoute> wprlist, ArrayList<VirtualLink> provirtuallinklist,ArrayList<Integer> ShareReg ) {
		ArrayList<Link> alllinklist = new ArrayList<>();
		ArrayList<Node> sharenodelist = new ArrayList<>();
		ArrayList<Node> newregnodelist = new ArrayList<>();
		
		finalRoute.getRoute().OutputRoute_node(finalRoute.getRoute());
		int count = 0;
		double length2 = 0;
		boolean regflag2 = false;
		ArrayList<Link> linklist2 = new ArrayList<>();
		for (int i = 0; i < finalRoute.getregnum() + 1; i++) {
			if (i >= finalRoute.getregnum())
				regflag2 = true;
			do {
				Node nodeA = finalRoute.getRoute().getNodelist().get(count);
				Node nodeB = finalRoute.getRoute().getNodelist().get(count + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println("找到的链路名字：" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// 未到达最后一段路径的RSA
					// if (count != finalRoute.getregnode().get(i)) {
					// if (count == finalRoute.getRoute().getNodelist().size() -
					// 1) {
					// Prolinkcapacitymodify(IPflow, length2, linklist2,
					// oplayer, ipLayer);// 为目的节点前的剩余链路进行RSA
					// for(Link addlink:linklist2){
					// alllinklist.add(addlink);
					// }
					// linklist2.clear();
					// }
					// }
					if (count == finalRoute.getregnode().get(i)) {
						Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist);// 此时在n点放置再生器
						for (Link addlink : linklist2) {
							alllinklist.add(addlink);
						}
						Node node = finalRoute.getRoute().getNodelist().get(count);
						if(ShareReg.contains(count)){//表示该节点上的再生器可以共享
							sharenodelist.add(node);
						}
						else{
							newregnodelist.add(node);
						}
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {
					Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist);// 为目的节点前的剩余链路进行RSA
					for (Link addlink : linklist2) {
						alllinklist.add(addlink);
					}
					linklist2.clear();
				}
			} while (count != finalRoute.getRoute().getNodelist().size() - 1);
		}
		for (WorkandProtectRoute wpr : wprlist) {
			if (wpr.getdemand().equals(nodepair)) {
				wpr.setprolinklist(alllinklist);
				wpr.setsharenodelist(sharenodelist);
				wpr.setnewregnodelist(newregnodelist);
				ArrayList<Node> regnodelist = new ArrayList<>();
				regnodelist.addAll(sharenodelist);
				regnodelist.addAll(newregnodelist);
				wpr.setregnodelist(regnodelist);
				wpr.setprovirtuallinklist(provirtuallinklist);
			}
		}
	}

	public boolean Prolinkcapacitymodify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer,
			Layer iplayer, ArrayList<VirtualLink> provirtuallinklist) {
		double X = 1;
		int slotnum = 0;
		boolean opworkflag = false;
		Node srcnode = new Node(null, 0, null, iplayer, 0, 0);
		Node desnode = new Node(null, 0, null, iplayer, 0, 0);
		if (routelength > 4000) {
			System.out.println("链路过长无法RSA");
		}
		if (routelength < 4000) {
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

			System.out.println("该链路所需slot数： " + slotnum);
			newFS = slotnum;
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa = new Mymain();
			index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			if (index_wave.size() == 0) {
				System.out.println("路径堵塞 ，不分配频谱资源");
			} else {
				opworkflag = true;
				double length1 = 0;
				double cost = 0;
				for (Link link : linklist) {// 物理层的link
					length1 = length1 + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
					link.setMaxslot(slotnum + link.getMaxslot());
					// System.out.println("链路 " + link.getName() + "的最大slot是： "
					// + link.getMaxslot()+" 可用频谱窗数：
					// "+link.getSlotsindex().size());
				}

				for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// 在IP层中寻找transparent链路的两端
					boolean srcflag = false, desflag = false;
					// System.out.println(iplayer.getNodelist()..get(0).getName());
					HashMap<String, Node> map = iplayer.getNodelist();
					Iterator<String> iter = map.keySet().iterator();
					while (iter.hasNext()) {
						Node node = (Node) (map.get(iter.next()));
						if (node.getName().equals(linklist.get(0).getNodeA().getName())) {
							srcnode = node;
							srcflag = true;
						}
						if (node.getName().equals(linklist.get(linklist.size() - 1).getNodeB().getName())) {
							desnode = node;
							desflag = true;
						}
					}
					if (srcflag && desflag)
						break;
				}

				String name = srcnode.getName() + "-" + desnode.getName();
				int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的故这样设置index

				Link finlink = iplayer.findLink(srcnode, desnode);
				Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
				boolean findflag = false;
				try {
					System.out.println(finlink.getName());
					findflag = true;
				} catch (java.lang.NullPointerException ex) {
					System.out.println("IP 层没有该链路需要新建链路");
					createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
					iplayer.addLink(createlink);
				}

				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
				Vlink.setnature(1);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
				Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setlength(length1);
				Vlink.setcost(cost);
				Vlink.setPhysicallink(linklist);
				provirtuallinklist.add(Vlink);
				if (findflag) {// 如果在IP层中已经找到该链路
					// System.out.println(finlink.getVirtualLinkList().size());
					finlink.getVirtualLinkList().add(Vlink);
					// System.out.println(finlink.getVirtualLinkList().size());
					System.out.println(
							"IP层已存在的链路 " + finlink.getName() + "\n " + "    预留的flow：  " + Vlink.getRestcapacity());
					System.out.println("工作链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： "
							+ finlink.getVirtualLinkList().size());
				} else {
					createlink.getVirtualLinkList().add(Vlink);
					// System.out.println(createlink.getVirtualLinkList().size());
					System.out.println("IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
					System.out.println("工作链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： "
							+ createlink.getVirtualLinkList().size());
				}
			}
		}
		return opworkflag;
	}

	public RouteAndRegPlace optionRouteSelect(ArrayList<RouteAndRegPlace> regplaceoption,
			ArrayList<WorkandProtectRoute> wprlist) {
		RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 1);
		int FS = 10000;
		ArrayList<RouteAndRegPlace> RemoveRoute = new ArrayList<>();
		// 第一种比较：比较每个链路新使用的FS个数 选取最少的//但是这里新使用的FS并没有考虑共享的FS
		for (RouteAndRegPlace route : regplaceoption) {
			if (route.getnewFSnum() <= FS) {
				FS = route.getnewFSnum();
			}
			if (route.getnewFSnum() > FS) {
				RemoveRoute.add(route);
			}
		}
		for (RouteAndRegPlace route : RemoveRoute) {
			regplaceoption.remove(route);
		}
		// 第二层比较 比较再生器为几条保护路径提供了保护 选择多的
		int max = 0;
		for (RouteAndRegPlace route : regplaceoption) {// 首先取出新业务的一条备选路由
			int share = 0;
			for (WorkandProtectRoute wpr : wprlist) {
				for (int u = 0; u < route.getregnode().size(); u++) {
					String name = route.getRoute().getNodelist().get(u).getName();
					for (Node node : wpr.getregnodelist()) {
						if (node.getName().equals(name)) {
							share++;
							break;
						}
					}
				}
			}
			if (max <= share) {
				max = share;
				finalRoute = route;// 这条应该放在最后一层比较
			}
			if (max > share)
				RemoveRoute.add(route);
		}
		// for(RouteAndRegPlace route: RemoveRoute){
		// regplaceoption.remove(route);
		// }
		// 第三层比较未完待续

		return finalRoute;
	}

	public boolean RSAunderSet(int[] set, LinearRoute newRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<RouteAndRegPlace> regplaceoption) {
		boolean partworkflag = false, RSAflag = false, regflag = false;
		double length = 0;
		ArrayList<Link> linklist = new ArrayList<>();
		int FStotal = 0, n = 0;
		RegeneratorPlace rp = new RegeneratorPlace();

		for (int i = 0; i < set.length + 1; i++) {// RSA的次数比再生器的个数多1
			if (!partworkflag && RSAflag || regplaceoption.size() != 0)
				break;
			if (i < set.length)
				System.out.println("****************再生器的位置为：" + set[i]); // set里面的数应该是节点的位置+1！
			else {
				System.out.println("************最后一个再生器与终结点之间的RSA ");
				regflag = true;
			}
			do {// 通过一个
				Node nodeA = newRoute.getNodelist().get(n);
				Node nodeB = newRoute.getNodelist().get(n + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println(link.getName());
				length = length + link.getLength();
				linklist.add(link);
				n = n + 1;
				if (!regflag) {// 未到达最后一段路径的RSA
					if (n != set[i]) {
						if (n == newRoute.getNodelist().size() - 1) {
							partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer);// 为目的节点前的剩余链路进行RSA
							FStotal = FStotal + newFS;
						}
					}
					if (n == set[i]) {
						partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer);// 此时在n点放置再生器
						FStotal = FStotal + newFS;
						length = 0;
						RSAflag = true;
						linklist.clear();
						break;
					}
				}
				if (n == newRoute.getNodelist().size() - 1) {
					partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer);// 此时在n点放置再生器
					FStotal = FStotal + newFS;
				}
				if (!partworkflag && RSAflag)
					break;
			} while (n != newRoute.getNodelist().size() - 1);
			// 如果路由成功则保存该路由对于再生器的放置
		}
		if (partworkflag) {
			RouteAndRegPlace rarp = new RouteAndRegPlace(newRoute, 1);
			rarp.setnewFSnum(FStotal);
			ArrayList<Integer> setarray = new ArrayList<>();
			for (int k = 0; k < set.length; k++) {
				setarray.add(set[k]);
			}
			rarp.setregnode(setarray);
			rarp.setregnum(setarray.size());
			regplaceoption.add(rarp);
			System.out.println("该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());// 再生器的个数加进去
		}
		return partworkflag;

	}
}
