package networkdesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import MainFunction.ReadFlowFile;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class trafficgrooming {

	// 这里的保护是IP层worklink和protectionlink 分开grooming
	public void grooming(Network network, Layer optlayer, Layer iplayer) {

		ReadFlowFile myfile = new ReadFlowFile();
		myfile.Readflow(iplayer,  "G:/Topology/6.csv");

		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		int num_of_transponders = 0;
		int sumslotnum = 0;
		ArrayList<Link> tempdel_LinkList = new ArrayList<Link>();
		ArrayList<Link> tempdel_LinkList_1 = new ArrayList<Link>();
		ArrayList<Link> tempdel_LinkList_2 = new ArrayList<Link>();
		ArrayList<Link> tempdel_LinkList_3 = new ArrayList<Link>();
		ArrayList<Link> tempdel_LinkList_4 = new ArrayList<Link>();
		// tempdel_LinkList.clear();
		ArrayList<Link> optempdel_LinkList = new ArrayList<Link>();
		// optempdel_LinkList.clear();

		for (int i = 0; i < iplayer.getNodepairNum(); i++)
		{
			NodePair nodepair = demandlist.get(i);

			System.out.println("现在正在操作的节点对是：" + nodepair.getName() + "它的流量需求是：" + nodepair.getTrafficdemand());

			// 遍历所有的链路，将容量小，保护路径所经过的链路全部加入到tempdel_LinkList列表中
			System.out.println(iplayer.getLinklist().size());
			HashMap<String, Link> map6 = iplayer.getLinklist();
			Iterator<String> iter6 = map6.keySet().iterator();
			while (iter6.hasNext()) {
				Link link = (Link) (map6.get(iter6.next()));
				System.out.println("here");
				if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {
					tempdel_LinkList.add(link);
					 System.out.println("******由于虚链路上容量不足而删除当前链路"+link.getName());
				}
				if (link.getNature() == 1) {
					tempdel_LinkList.add(link);
					 System.out.println("*********由于链路是保护属性而删除当前链路"+link.getName());
				}
			}
			// System.out.println("********逐个输出删除的链路,共有"+tempdel_LinkList.size()+"条,依次为：");

			for (int k = 0; k < tempdel_LinkList.size(); k++) {
				System.out.println(tempdel_LinkList.get(k).getName());
			}

			for (int o = 0; o < tempdel_LinkList.size(); o++) {
				if (!tempdel_LinkList_3.contains(tempdel_LinkList.get(o)))
					tempdel_LinkList_3.add(tempdel_LinkList.get(o));
			}
			// System.out.println("合并后实际有"+tempdel_LinkList_3.size()+"条：");

			for (Link link : tempdel_LinkList_3) {
				// System.out.println("由于容量不足或者保护属性而删除的链路是："+link.getName());//删除流量不够和属性为保护的link
				iplayer.removeLink(link.getName());
			}

			Layer ipcopylayer = network.getLayerlist().get("ipcopylayer");

			HashMap<String, Link> map = iplayer.getLinklist();
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				Link link = (Link) (map.get(iter.next()));
				Node srcnodec = link.getNodeA();
				Node desnodec = link.getNodeB();

				Node srcnodec1 = ipcopylayer.getNodelist().get(srcnodec.getName());
				Node desnodec1 = ipcopylayer.getNodelist().get(desnodec.getName());
				int index = 0;
				index = ipcopylayer.getLinklist().size();
				String name = srcnodec.getName() + "-" + desnodec.getName();
				double length = 0;
				double cost = 0;
				length = length + link.getLength();
				cost = cost + link.getCost();
				Link newlink = new Link(name, index, "", ipcopylayer, srcnodec1, desnodec1, length, cost);
				// System.out.println("add new
				// ipcopylayer的link名字是："+newlink.getName());
				ipcopylayer.addLink(newlink);
			}
			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();

			Node srcnodecc = ipcopylayer.getNodelist().get(srcnode.getName());
			Node desnodecc = ipcopylayer.getNodelist().get(desnode.getName());

			// System.out.println("ipcopylayer源节点："+srcnodecc.getName()+"ipcpoylayer目的节点："+desnodecc.getName());
			LinearRoute newroutew = new LinearRoute("", 0, "");
			RouteSearching routesearching = new RouteSearching();
			routesearching.Dijkstras(srcnodecc, desnodecc, ipcopylayer, newroutew, null);

			// 恢复
			for (Link link : tempdel_LinkList_3) {
				iplayer.addLink(link);
			}
			tempdel_LinkList.clear();
			tempdel_LinkList_3.clear();

			HashMap<String, Link> map0 = ipcopylayer.getLinklist();
			Iterator<String> iter0 = map0.keySet().iterator();
			while (iter0.hasNext()) {
				Link link = (Link) (map0.get(iter0.next()));
				tempdel_LinkList_4.add(link);
			}
			for (Link link : tempdel_LinkList_4) {
				ipcopylayer.removeLink(link.getName());
			}
			// System.out.println("ipcopylayer
			// link:"+ipcopylayer.getLinklist().size());
			tempdel_LinkList_4.clear();

			if (newroutew.getLinklist().size() != 0) {
				// 路由成功
				System.out.println("工作路径ip层上路由成功！");
				System.out.println("在ip层上的路由是：");
				newroutew.OutputRoute_node(newroutew);
				System.out.println("---");
				ArrayList<Link> newrouteww = new ArrayList<Link>();// 存储工作路径所经过的link
				for (int i1 = 0; i1 < newroutew.getLinklist().size(); i1++) {
					HashMap<String, Link> map1 = iplayer.getLinklist();
					Iterator<String> iter1 = map1.keySet().iterator();
					while (iter1.hasNext()) {
						Link link = (Link) (map1.get(iter1.next()));
						if ((link.getNodeA().getName().equals(newroutew.getLinklist().get(i1).getNodeA().getName()))
								&& (link.getNodeB().getName()
										.equals(newroutew.getLinklist().get(i1).getNodeB().getName()))
								&& (link.getNature() == 0)) {
							link.setFlow(link.getFlow() + nodepair.getTrafficdemand());
							newrouteww.add(link);// iplayer路由工作路径所经过的iplink
						}
						System.out.println(
								"iplayer 各link的剩余容量为：" + link.getName() + ":" + (link.getSumflow() - link.getFlow()));
					}
				}

				// 工作路径路由成功，路由保护路径！
				// 所有保护link中与工作路径的link有重合的物理link都要删除！
				HashMap<String, Link> map60 = iplayer.getLinklist();
				Iterator<String> iter60 = map60.keySet().iterator();
				while (iter60.hasNext()) {
					Link link = (Link) (map60.get(iter60.next()));
					if (link.getNature() == 0) {
						tempdel_LinkList.add(link); // 工作属性link
						// System.out.println("*********由于链路是工作属性而删除当前链路"+link.getName());
					}
					if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {
						tempdel_LinkList.add(link);// 流量不够的link
						// System.out.println("*********由于链路容量不足而删除当前链路"+link.getName());
					}
					for (int u = 0; u < newrouteww.size(); u++) {

						for (int b = 0; b < newrouteww.get(u).getPhysicallink().size(); b++) {
							for (int c = 0; c < link.getPhysicallink().size(); c++) {
								if (link.getPhysicallink().get(c).getName()
										.equals(newrouteww.get(u).getPhysicallink().get(b).getName()))
									tempdel_LinkList.add(link);
								// System.out.println("*********由于物理路径重合而删除当前链路"+link.getName());
							}
						}
					}
				}
				// 与工作路径有重合物理link的ip层link

				for (int r = 0; r < tempdel_LinkList.size(); r++) {
					if (!tempdel_LinkList_2.contains(tempdel_LinkList.get(r)))
						tempdel_LinkList_2.add(tempdel_LinkList.get(r));
				}

				for (Link link4 : tempdel_LinkList_2) {

					// System.out.println("合并后路由保护路径删除的链路是："+link4.getName());//删除流量不够和属性为工作的link
					iplayer.removeLink(link4.getName());
				}
				HashMap<String, Link> map3 = iplayer.getLinklist();
				Iterator<String> iter3 = map3.keySet().iterator();
				while (iter3.hasNext()) {
					Link link = (Link) (map3.get(iter3.next()));
					// System.out.println("iplayer的剩余link名字是："+link.getName());
					Node srcnodec = link.getNodeA();
					Node desnodec = link.getNodeB();

					Node srcnodec1 = ipcopylayer.getNodelist().get(srcnodec.getName());
					Node desnodec1 = ipcopylayer.getNodelist().get(desnodec.getName());
					int index = 0;
					index = ipcopylayer.getLinklist().size();
					String name = srcnodec.getName() + "-" + desnodec.getName();
					double length = 0;
					double cost = 0;
					length = length + link.getLength();
					cost = cost + link.getCost();
					Link newlink = new Link(name, index, "", ipcopylayer, srcnodec1, desnodec1, length, cost);
					System.out.println("add new ipcopylayer的link名字是：" + newlink.getName());
					ipcopylayer.addLink(newlink);
				}
				// HashMap<String, Link> map13 = ipcopylayer.getLinklist();
				// Iterator<String> iter13 = map13.keySet().iterator();
				// while(iter13.hasNext()){
				// Link link=(Link)(map13.get(iter13.next()));
				// System.out.println("ipcopy所有link："+link.getName());
				// }
				Node srcnodecp = ipcopylayer.getNodelist().get(srcnode.getName());
				Node desnodecp = ipcopylayer.getNodelist().get(desnode.getName());
				System.out.println("ipcopy源节点是：" + srcnodecp.getName());
				System.out.println("ipcopy目的点是：" + desnodecp.getName());
				LinearRoute newroutep = new LinearRoute("", 0, "");
				RouteSearching routesearching1 = new RouteSearching();
				routesearching1.Dijkstras(srcnodecp, desnodecp, ipcopylayer, newroutep, null);

				for (Link link : tempdel_LinkList_2) {// 恢复link
					iplayer.addLink(link);
				}
				tempdel_LinkList.clear();
				tempdel_LinkList_2.clear();

				HashMap<String, Link> map00 = ipcopylayer.getLinklist();
				Iterator<String> iter00 = map00.keySet().iterator();
				while (iter00.hasNext()) {
					Link link = (Link) (map00.get(iter00.next()));
					tempdel_LinkList_4.add(link);
				}
				for (Link link : tempdel_LinkList_4) {
					ipcopylayer.removeLink(link.getName());
				}
				// System.out.println("ipcopylayer
				// link:"+ipcopylayer.getLinklist().size());
				tempdel_LinkList_4.clear();

				if (newroutep.getLinklist().size() != 0) {
					System.out.println("保护路径ip层上路由成功！");
					System.out.println("在ip层上的路由是：");
					newroutep.OutputRoute_node(newroutep);
					System.out.println("---");

					for (int i1 = 0; i1 < newroutep.getLinklist().size(); i1++) {
						HashMap<String, Link> map1 = iplayer.getLinklist();
						Iterator<String> iter1 = map1.keySet().iterator();
						while (iter1.hasNext()) {
							Link link = (Link) (map1.get(iter1.next()));
							if ((link.getNodeA().getName().equals(newroutep.getLinklist().get(i1).getNodeA().getName()))
									&& (link.getNodeB().getName()
											.equals(newroutep.getLinklist().get(i1).getNodeB().getName()))
									&& (link.getNature() == 1)) {
								link.setFlow(link.getFlow() + nodepair.getTrafficdemand());

							}
							System.out.println("iplayer 各link的剩余容量为：" + link.getName() + ":"
									+ (link.getSumflow() - link.getFlow()));
						}
					}
				} else {
					// 在光层中将newrouteww所经过的物理link都删除

					HashMap<String, Link> map8 = optlayer.getLinklist();
					Iterator<String> iter8 = map8.keySet().iterator();
					while (iter8.hasNext()) {
						Link link = (Link) (map8.get(iter8.next()));
						for (int f = 0; f < newrouteww.size(); f++) {
							for (int g = 0; g < newrouteww.get(f).getPhysicallink().size(); g++) {
								if (link.getName().equals(newrouteww.get(f).getPhysicallink().get(g).getName()))
									optempdel_LinkList.add(link);
							}
						}
					}
					for (Link link : optempdel_LinkList) {
						// System.out.println("3本次删除的链路是："+link.getName());//删除worklink
						optlayer.removeLink(link.getName());
					}

					System.out.println("保护路由不成功！新建一条光路！++++++++++");
					Node srcnodep = optlayer.getNodelist().get(srcnode.getName());
					Node desnodep = optlayer.getNodelist().get(desnode.getName());

					LinearRoute newroutep1 = new LinearRoute("", 0, "");
					RouteSearching routesearchingp1 = new RouteSearching();
					routesearchingp1.Dijkstras(srcnodep, desnodep, optlayer, newroutep1, null);

					for (Link link : optempdel_LinkList) {// 恢复link
						optlayer.addLink(link);
					}
					optempdel_LinkList.clear();
					if (newroutep1.getLinklist().size() == 0) {
						System.out.println("无保护路径");
					} else {
						System.out.println("保护路径建立成功，在物理层路由经过的节点如下：------");
						newroutep1.OutputRoute_node(newroutep1);
						int slotnum = 0;
						int IPflow = nodepair.getTrafficdemand();
						double X = 1;// 2000-4000 BPSK,1000-2000 QBSK,500-1000
										// 8QAM,0-500 16QAM
						double routelength = newroutep1.getlength();
						// System.out.println("物理路径的长度是："+routelength);
						if (routelength > 2000 && routelength <= 4000) {
							X = 12.5;
						} else if (routelength > 1000 && routelength <= 2000) {
							X = 25.0;
						} else if (routelength > 500 && routelength <= 1000) {
							X = 37.5;
						} else if (routelength > 0 && routelength <= 500) {
							X = 50.0;
						}

						slotnum = (int) Math.ceil(IPflow / X);
						// sumslotnum=sumslotnum+slotnum;
						newroutep1.setSlotsnum(slotnum);
						System.out.println("所需的slots数是：" + slotnum);

						ArrayList<Integer> index_w = new ArrayList<Integer>();

						index_w = spectrumallocationOneRoute(newroutep1);
						// System.out.println("size of index_w
						// is:"+index_w.size());
						// System.out.println("可用频谱窗：");
						// for(int k=0;k<index_w.size();k++)
						// System.out.print(index_w.get(k));
						// System.out.print("\n");

						if (index_w.size() == 0) {
							System.out.println("保护路径堵塞，不分配频谱资源");
						}

						else {
							System.out.println("不堵塞，分配频谱资源");
							// nodepair.getindexlist_w().add(index_w.get(0));
							// //工作路径采用首次命中方式
							for (Link link : newroutep1.getLinklist()) {
								Request request = null;
								// request.setSlots(slotnum);
								ResourceOnLink resourceonlink = new ResourceOnLink(request, link, index_w.get(0),
										slotnum);
								// SA.getRollist_w().add(resourceonlink);
								// System.out.println("the name of
								// link:"+link.getName()+"the start index of
								// working path:"+index_w.get(0));
							}
							for (int t = 0; t < newroutep1.getLinklist().size(); t++) {
								newroutep1.getLinklist().get(t)
										.setMaxslot(slotnum + newroutep1.getLinklist().get(t).getMaxslot());
								System.out.println("最大slot是：" + newroutep1.getLinklist().get(t).getName() + ":"
										+ newroutep1.getLinklist().get(t).getMaxslot());
							}
						}
						// sumslotnum=sumslotnum+slotnum;
						// System.out.println("总slot数是："+sumslotnum);

						// 新建保护光路
						int index = iplayer.getLinklist().size();
						String name = srcnodep.getName() + "-" + 1 + "-" + desnodep.getName();
						double length = 0;
						double cost = 0;
						for (Link link2 : newroutep1.getLinklist()) {
							length = length + link2.getLength();
							cost = cost + link2.getCost();
						}
						Link newlink = new Link(name, index, "", iplayer, srcnode, desnode, length, cost);
						System.out.println("新光路的名字是：" + newlink.getName());
						// System.out.println("新光路的属性是："+newlink.getNature());
						// System.out.println("新光路的序号是："+newlink.getIndex());
						// System.out.println("新光路的长度是："+newlink.getLength());
						// System.out.println("新光路的cost是："+newlink.getCost());
						iplayer.addLink(newlink);
						newlink.setNature(1);
						newlink.setFlow(nodepair.getTrafficdemand());
						newlink.setSumflow(slotnum * X);
						newlink.setIpremainflow(newlink.getSumflow() - newlink.getFlow());
						System.out.println("光通道剩余容量是：" + (newlink.getSumflow() - newlink.getFlow()));
						num_of_transponders = num_of_transponders + 2;
						System.out.println("使用的transponders数是：" + num_of_transponders);
						// this.setNumTR(num_of_transponders);
						newlink.setPhysicallink(newroutep1.getLinklist());// 将新路径所经过的所有link存储
					}
				}
			}

			// second part add link to G
			else {
				System.out.println("直接在IP层建立工作路由不成功！在光层新建一条光路！++++++++++");
				// 光层路由
				Node srcnode1 = optlayer.getNodelist().get(srcnode.getName());
				Node desnode1 = optlayer.getNodelist().get(desnode.getName());
				LinearRoute newroute1 = new LinearRoute("", 0, "");
				RouteSearching routesearching1 = new RouteSearching();
				// System.out.println("源节点是："+srcnode1.getName());
				// System.out.println("目的点是："+desnode1.getName());
				routesearching1.Dijkstras(srcnode1, desnode1, optlayer, newroute1, null);
				if (newroute1.getLinklist().size() == 0) {
					System.out.println("工作路径无路径");
				} else {
					System.out.println("在物理层路由经过的节点如下：------");
					newroute1.OutputRoute_node(newroute1);

					int slotnum = 0;

					int IPflow = nodepair.getTrafficdemand();
					double X = 1;// 2000-4000 BPSK,1000-2000 QBSK,500-1000
									// 8QAM,0-500 16QAM
					double routelength = newroute1.getlength();
					// System.out.println("物理路径的长度是："+routelength);
					if (routelength > 2000 && routelength <= 4000) {
						X = 12.5;
					} else if (routelength > 1000 && routelength <= 2000) {
						X = 25.0;
					} else if (routelength > 500 && routelength <= 1000) {
						X = 37.5;
					} else if (routelength > 0 && routelength <= 500) {
						X = 50.0;
					}

					slotnum = (int) Math.ceil(IPflow / X);
					// sumslotnum=sumslotnum+slotnum;
					newroute1.setSlotsnum(slotnum);
					System.out.println("所需的slots数是：" + slotnum);

					ArrayList<Integer> index_w = new ArrayList<Integer>();

					index_w = spectrumallocationOneRoute(newroute1);
					// System.out.println("size of index_w is:"+index_w.size());
					// System.out.println("可用频谱窗：");
					// for(int k=0;k<index_w.size();k++)
					// System.out.print(index_w.get(k));
					// System.out.print("\n");

					if (index_w.size() == 0) {
						// total_block_bandwidth=total_block_bandwidth+IPflow;
						// 更新阻塞资源
						System.out.println("工作路径堵塞，不分配频谱资源");
					}
					// System.out.println("-----");
					else {
						System.out.println("不堵塞，分配频谱资源");
						// nodepair.getindexlist_w().add(index_w.get(0));
						// //工作路径采用首次命中方式
						for (Link link : newroute1.getLinklist()) {
							Request request = null;
							// request.setSlots(slotnum);
							ResourceOnLink resourceonlink = new ResourceOnLink(request, link, index_w.get(0), slotnum);
							// System.out.println("the name of
							// link:"+link.getName()+"the start index of working
							// path:"+index_w.get(0));
						}
						for (int t = 0; t < newroute1.getLinklist().size(); t++) {
							newroute1.getLinklist().get(t)
									.setMaxslot(slotnum + newroute1.getLinklist().get(t).getMaxslot());
							System.out.println("最大slot是：" + newroute1.getLinklist().get(t).getName() + ":"
									+ newroute1.getLinklist().get(t).getMaxslot());
						}
					}
					// sumslotnum=sumslotnum+slotnum;
					// System.out.println("总slot数是："+sumslotnum);

					// 新建光路
					int index = iplayer.getLinklist().size();
					String name = srcnode1.getName() + "-" + 0 + "-" + desnode1.getName();
					double length = 0;
					double cost = 0;
					for (Link link2 : newroute1.getLinklist()) {
						length = length + link2.getLength();
						cost = cost + link2.getCost();
					}
					Link newlink = new Link(name, index, "", iplayer, srcnode, desnode, length, cost);
					System.out.println("新光路的名字是：" + newlink.getName());
					// System.out.println("新光路的属性是："+newlink.isNature());
					// System.out.println("新光路的序号是："+newlink.getIndex());
					// System.out.println("新光路的长度是："+newlink.getLength());
					// System.out.println("新光路的cost是："+newlink.getCost());
					iplayer.addLink(newlink);
					newlink.setNature(0);
					newlink.setFlow(nodepair.getTrafficdemand());
					newlink.setSumflow(slotnum * X);
					newlink.setIpremainflow(newlink.getSumflow() - newlink.getFlow());
					System.out.println("光通道剩余容量是：" + (newlink.getSumflow() - newlink.getFlow()));
					num_of_transponders = num_of_transponders + 2;
					System.out.println("使用的transponders数是：" + num_of_transponders);
					newlink.setPhysicallink(newroute1.getLinklist());
					// this.setNumTR(num_of_transponders);

					// second part的first part
					// 工作路径建立好，开始路由保护路径
					// 所有保护link中与工作路径的link有重合的物理link都要删除!

					HashMap<String, Link> map11 = iplayer.getLinklist();
					Iterator<String> iter11 = map11.keySet().iterator();
					while (iter11.hasNext()) {
						Link link = (Link) (map11.get(iter11.next()));
						if (link.getNature() == 0)
							tempdel_LinkList.add(link);
						if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand())
							tempdel_LinkList.add(link);
						for (int q = 0; q < newroute1.getLinklist().size(); q++) {
							for (int p = 0; p < link.getPhysicallink().size(); p++) {
								if (link.getPhysicallink().get(p).getName()
										.equals(newroute1.getLinklist().get(q).getName()))
									tempdel_LinkList.add(link); // IP层与工作路径newroute1有重合的link
							}
						}
					}

					for (int x = 0; x < tempdel_LinkList.size(); x++) {
						if (!tempdel_LinkList_1.contains(tempdel_LinkList.get(x)))
							tempdel_LinkList_1.add(tempdel_LinkList.get(x));
					}

					for (Link link4 : tempdel_LinkList_1) {
						// System.out.println("路由保护路径删除的链路是："+link4.getName());//删除流量不够和属性为工作的link
						iplayer.removeLink(link4.getName());
					}

					HashMap<String, Link> map3 = iplayer.getLinklist();
					Iterator<String> iter3 = map3.keySet().iterator();
					while (iter3.hasNext()) {
						Link link = (Link) (map3.get(iter3.next()));
						Node srcnodec = link.getNodeA();
						Node desnodec = link.getNodeB();

						Node srcnodec1 = ipcopylayer.getNodelist().get(srcnodec.getName());
						Node desnodec1 = ipcopylayer.getNodelist().get(desnodec.getName());
						int index1 = 0;
						index1 = ipcopylayer.getLinklist().size();
						String name1 = srcnodec.getName() + "-" + desnodec.getName();
						double length1 = 0;
						double cost1 = 0;
						length1 = length1 + link.getLength();
						cost1 = cost1 + link.getCost();
						Link newlink1 = new Link(name1, index1, "", ipcopylayer, srcnodec1, desnodec1, length1, cost1);
						System.out.println("add new ipcopylayer的link名字是：" + newlink1.getName());
						ipcopylayer.addLink(newlink1);
					}

					Node srcnode11 = nodepair.getSrcNode();
					Node desnode11 = nodepair.getDesNode();
					Node srcnodecc1 = ipcopylayer.getNodelist().get(srcnode11.getName());
					Node desnodecc1 = ipcopylayer.getNodelist().get(desnode11.getName());
					// System.out.println("源节点是："+srcnode11.getName());
					// System.out.println("目的点是："+desnode11.getName());
					LinearRoute newroutepp = new LinearRoute("", 0, "");
					RouteSearching routesearchingpp = new RouteSearching();
					routesearchingpp.Dijkstras(srcnodecc1, desnodecc1, ipcopylayer, newroutepp, null);

					// 恢复
					for (Link link : tempdel_LinkList_1) {
						iplayer.addLink(link);
					}
					tempdel_LinkList_1.clear();
					tempdel_LinkList.clear();

					HashMap<String, Link> map01 = ipcopylayer.getLinklist();
					Iterator<String> iter01 = map01.keySet().iterator();
					while (iter01.hasNext()) {
						Link link = (Link) (map01.get(iter01.next()));
						tempdel_LinkList_4.add(link);
					}
					for (Link link : tempdel_LinkList_4) {
						ipcopylayer.removeLink(link.getName());
					}
					// System.out.println("ipcopylayer
					// link:"+ipcopylayer.getLinklist().size());
					tempdel_LinkList_4.clear();

					if (newroutepp.getLinklist().size() != 0) {
						System.out.println("保护路径ip层上路由成功！");
						System.out.println("在ip层上的路由是：");
						newroutepp.OutputRoute_node(newroutepp);
						System.out.println("---");

						// ArrayList<Link> newrouteww=new
						// ArrayList<Link>();//存储工作路径所经过的link
						for (int i1 = 0; i1 < newroutepp.getLinklist().size(); i1++) {
							HashMap<String, Link> map1 = iplayer.getLinklist();
							Iterator<String> iter1 = map1.keySet().iterator();
							while (iter1.hasNext()) {
								Link link = (Link) (map1.get(iter1.next()));
								if ((link.getNodeA().getName()
										.equals(newroutepp.getLinklist().get(i1).getNodeA().getName()))
										&& (link.getNodeB().getName()
												.equals(newroutepp.getLinklist().get(i1).getNodeB().getName()))
										&& (link.getNature() == 1)) {
									link.setFlow(link.getFlow() + nodepair.getTrafficdemand());
									// newrouteww.add(link);//iplayer路由工作路径所经过的iplink
								}
								System.out.println("iplayer 各link的剩余容量为：" + link.getName() + ":"
										+ (link.getSumflow() - link.getFlow()));
							}
						}
					} else {

						// second part 的second part
						System.out.println("保护路由不成功！新建一条光路！++++++++++");

						ArrayList<Link> temopLinkList = new ArrayList<Link>();
						HashMap<String, Link> mapop = optlayer.getLinklist();
						Iterator<String> iterop = mapop.keySet().iterator();
						while (iterop.hasNext()) {
							Link linkop = (Link) (mapop.get(iterop.next()));
							for (int m = 0; m < newroute1.getLinklist().size(); m++) {
								if (linkop.getName().equals(newroute1.getLinklist().get(m).getName()))
									temopLinkList.add(linkop);

							}
						}
						for (Link link5 : temopLinkList) {
							// System.out.println("在光层删除的链路是："+link5.getName());
							optlayer.removeLink(link5.getName());
						}
						Node srcnodep = optlayer.getNodelist().get(srcnode.getName());
						Node desnodep = optlayer.getNodelist().get(desnode.getName());

						LinearRoute newroutepp1 = new LinearRoute("", 0, "");
						RouteSearching routesearchingpp1 = new RouteSearching();
						routesearchingpp1.Dijkstras(srcnodep, desnodep, optlayer, newroutepp1, null);

						for (Link link : temopLinkList) {
							optlayer.addLink(link);
						}
						temopLinkList.clear();

						if (newroutepp1.getLinklist().size() == 0)
							System.out.println("保护路径在光层建立失败！");
						else {
							System.out.println("在物理层路由经过的节点如下：------");
							newroutepp1.OutputRoute_node(newroutepp1);

							int slotnum1 = 0;
							int IPflow1 = nodepair.getTrafficdemand();
							double X1 = 1;// 2000-4000 BPSK,1000-2000
											// QBSK,500-1000 8QAM,0-500 16QAM
							double routelength1 = newroutepp1.getlength();
							// System.out.println("物理路径的长度是："+routelength1);
							if (routelength1 > 2000 && routelength1 <= 4000) {
								X1 = 12.5;
							} else if (routelength1 > 1000 && routelength1 <= 2000) {
								X1 = 25.0;
							} else if (routelength1 > 500 && routelength1 <= 1000) {
								X1 = 37.5;
							} else if (routelength1 > 0 && routelength1 <= 500) {
								X1 = 50.0;
							}
							slotnum1 = (int) Math.ceil(IPflow1 / X1);
							// sumslotnum=sumslotnum+slotnum;
							newroutepp1.setSlotsnum(slotnum1);
							System.out.println("所需的slots数是：" + slotnum1);

							ArrayList<Integer> index_w1 = new ArrayList<Integer>();

							index_w1 = spectrumallocationOneRoute(newroutepp1);
							// System.out.println("size of index_w
							// is:"+index_w1.size());
							// System.out.println("可用频谱窗：");
							// for(int k=0;k<index_w1.size();k++)
							// System.out.print(index_w1.get(k));
							// System.out.print("\n");

							if (index_w1.size() == 0) {
								// total_block_bandwidth=total_block_bandwidth+IPflow;
								// 更新阻塞资源
								System.out.println("工作路径堵塞，不分配频谱资源");
							}
							// System.out.println("-----");
							else {
								System.out.println("不堵塞，分配频谱资源");
								// nodepair.getindexlist_w().add(index_w.get(0));
								// //工作路径采用首次命中方式
								for (Link link : newroutepp1.getLinklist()) {
									Request request = null;
									// request.setSlots(slotnum);
									ResourceOnLink resourceonlink = new ResourceOnLink(request, link, index_w1.get(0),
											slotnum1);
									// SA.getRollist_w().add(resourceonlink);
									// System.out.println("the name of
									// link:"+link.getName()+"the start index of
									// working path:"+index_w.get(0));

								}
								for (int t = 0; t < newroutepp1.getLinklist().size(); t++) {
									newroutepp1.getLinklist().get(t)
											.setMaxslot(slotnum1 + newroutepp1.getLinklist().get(t).getMaxslot());
									System.out.println("最大slot是：" + newroutepp1.getLinklist().get(t).getName() + ":"
											+ newroutepp1.getLinklist().get(t).getMaxslot());
								}
							}
							// sumslotnum=sumslotnum+slotnum1;

							// 新建光路
							int index1 = iplayer.getLinklist().size();
							String name1 = srcnodep.getName() + "-" + 1 + "-" + desnodep.getName();
							double length1 = 0;
							double cost1 = 0;
							int nature1 = 1;// 保护路径
							for (Link linkp : newroutepp1.getLinklist()) {
								length1 = length1 + linkp.getLength();
								cost1 = cost1 + linkp.getCost();
							}
							Link newlink1 = new Link(name1, index1, "", iplayer, srcnode, desnode, length1, cost1);
							System.out.println("新光路的名字是：" + newlink1.getName());
							// System.out.println("新光路的属性是："+newlink1.getNature());
							// System.out.println("新光路的序号是："+newlink.getIndex());
							// System.out.println("新光路的长度是："+newlink.getLength());
							// System.out.println("新光路的cost是："+newlink.getCost());
							iplayer.addLink(newlink1);
							newlink1.setNature(1);
							newlink1.setFlow(nodepair.getTrafficdemand());
							newlink1.setSumflow(slotnum1 * X1);
							newlink.setIpremainflow(newlink.getSumflow() - newlink.getFlow());
							System.out.println("光通道剩余容量是：" + (newlink1.getSumflow() - newlink.getFlow()));
							num_of_transponders = num_of_transponders + 2;
							System.out.println("使用的transponders数是：" + num_of_transponders);
							// this.setNumTR(num_of_transponders);
							newlink1.setPhysicallink(newroutepp1.getLinklist());
						}
					}
				}
			}
//		*/
		}
		/*
		int maxslot = 0;
		HashMap<String, Link> maps = optlayer.getLinklist();
		Iterator<String> iters = maps.keySet().iterator();
		while (iters.hasNext()) {
			Link link = (Link) (maps.get(iters.next()));
			if (link.getMaxslot() > maxslot)
				maxslot = link.getMaxslot();
		}
		System.out.println("工程中最大使用slot：" + maxslot);
		*/
	}

	// 返回值是nodepairlist,按照流量的大小进行排序过的
	public static ArrayList<NodePair> Rankflow(Layer IPlayer)

	{
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

	public static ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route) {
		ArrayList<Link> routelink = route.getLinklist();
		for (Link link : routelink) {
			if (route.getSlotsnum() == 0) {
				System.out.println("noslots");
				break;
			}
			link.getSlotsindex().clear();

		 
			for (int r = 0; r <= link.getSlotsarray().size() - route.getSlotsnum(); r++) {
				// System.out.println(link.getName() + "\t" +
				// link.getSlotsarray().size());
				// System.out.println(link.getName() + "\t" +
				// link.getSlotsindex().size());

				// System.out.println(link.getSlotsarray().get(i));
				int s = 1;
				for (int k = r; k < route.getSlotsnum() + r; k++) {//getSlotsnum==得出该路径上的需要slot数

					if (link.getSlotsarray().get(k).getoccupiedreqlist().size() != 0) {
						s = 0;
						break;
					}

				}
				if (s != 0) {
					link.getSlotsindex().add(r);

				}
			}
		}

		Link link = routelink.get(0);
		ArrayList<Integer> sameindex = new ArrayList<Integer>();
		sameindex.clear();
		for (int i = 0; i < link.getSlotsindex().size(); i++) {
			int index = link.getSlotsindex().get(i);
			int flag = 1;
			for (Link link2 : routelink) {
				if (!link2.getSlotsindex().contains(index)) {
					flag = 0;
					break;
				}
			}
			if (flag != 0) {
				sameindex.add(link.getSlotsindex().get(i));
			}
		}
		return sameindex;
	}
}
