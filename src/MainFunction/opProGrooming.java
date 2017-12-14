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
import MainFunction.FSshareOnlink;

public class opProGrooming {// 光层路由保护
	String OutFileName = Mymain.OutFileName;

	public boolean opprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag, ArrayList<WorkandProtectRoute> wprlist) throws IOException {// flag=true表示保护IP层建立的工作路径
		// flag=flase表示光层建立的工作路径
		RouteSearching Dijkstra = new RouteSearching();
		Request request = new Request(nodepair);
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
		boolean success = false;
		double routelength = 0;
		opProGrooming opg = new opProGrooming();
		file_out_put file_io = new file_out_put();
		ArrayList<VirtualLink> provirtuallinklist = new ArrayList<>();
		ArrayList<FSshareOnlink> FSuseOnlink = new ArrayList<FSshareOnlink>();
		ArrayList<Link> opDelLink = new ArrayList<Link>();
		ArrayList<Double> ProLengthList=new ArrayList<Double>();
		
		System.out.println("************保护路由在IP层不能路由，需要在光层新建");
		file_io.filewrite2(OutFileName, "************保护路由在IP层不能路由，需要在光层新建");

		// 删除该节点对的工作路由经过的所有物理链路
		for (Link LinkOnRoute : route.getLinklist()) {// 取出工作路由中的链路
			// System.out.println("工作路径链路：" + LinkOnRoute.getName());
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
						// System.out.println("删除的光层链路： " + oplink.getName());
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
			file_io.filewrite2(OutFileName, "保护路由光层无法建立");
		} else {
			System.out.println("光层找到路由:");
			file_io.filewrite_without(OutFileName, "光层找到路由:");
			opPrtectRoute.OutputRoute_node(opPrtectRoute);
			LinearRoute route_out = new LinearRoute(null, 0, null);
			route_out.OutputRoute_node(opPrtectRoute, OutFileName);
			file_io.filewrite2(OutFileName, " ");
			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000，8QAM,0-500 16QAM

			for (Link link : opPrtectRoute.getLinklist()) {
				routelength = routelength + link.getLength();
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
				file_io.filewrite2(OutFileName, "该路由上每段链路所需slot数： " + slotnum);
				// FIX

				ArrayList<Integer> index_wave = new ArrayList<Integer>();
				index_wave = opg.FSassignOnlink(opPrtectRoute.getLinklist(), wprlist, nodepair, slotnum, oplayer);

				if (index_wave.size() == 0) {
					System.out.println("路径堵塞 ，不分配频谱资源");
					file_io.filewrite2(OutFileName, "路径堵塞 ，不分配频谱资源");
				} else {
					success = true;
					double length = 0;
					double cost = 0;

					for (Link link : opPrtectRoute.getLinklist()) {
						ArrayList<Integer> index_wave1 = new ArrayList<Integer>();
						length = length + link.getLength();
						cost = cost + link.getCost();
						ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
						link.setMaxslot(slotnum + link.getMaxslot());
						// System.out.println("链路 " + link.getName() +
						// "的最大slot是： " + link.getMaxslot()+" 可用频谱窗数：
						// "+link.getSlotsindex().size());
						int m = index_wave.get(0);
						for (int n = 0; n < slotnum; n++) {
							index_wave1.add(m);
							// System.out.print(m);
//							file_io.filewrite_without(OutFileName, m + "  ");
							m++;
						}
						FSshareOnlink fsonLink = new FSshareOnlink(link, index_wave1);
						FSuseOnlink.add(fsonLink);
					}
					file_io.filewrite2(OutFileName, "  ");
					String name = opsrcnode.getName() + "-" + opdesnode.getName();
					int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的
																// 故这样设置index
					Link finlink = iplayer.findLink(srcnode, desnode);
					Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
					boolean findflag = false;
					try {
						System.out.println("IP层中找到链路" + finlink.getName());
						file_io.filewrite2(OutFileName, "IP层中找到链路" + finlink.getName());
						findflag = true;
					} catch (java.lang.NullPointerException ex) {
						System.out.println("IP 层没有该链路需要新建链路");
						file_io.filewrite2(OutFileName, "IP 层没有该链路需要新建链路");
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
					// numOfTransponder = numOfTransponder + 2;

					if (findflag) {// 如果在IP层中已经找到该链路
						// System.out.println("虚拟链路条数：" +
						// finlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName, "虚拟链路条数：" + finlink.getVirtualLinkList().size());
						finlink.getVirtualLinkList().add(Vlink);
						System.out.println("IP层已存在的链路 " + finlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
								+ Vlink.getUsedcapacity() + "\n" + "共有的flow:  " + Vlink.getFullcapacity()
								+ "    预留的flow：  " + Vlink.getRestcapacity() );
						file_io.filewrite2(OutFileName,
								"IP层已存在的链路 " + finlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: " + Vlink.getUsedcapacity()
										+ "\n " + "共有的flow:  " + Vlink.getFullcapacity() + "    预留的flow：  "
										+ Vlink.getRestcapacity() );
						System.out.println("*********已存在IP层链路：  " + finlink.getName() + "  上的虚拟链路条数： "
								+ finlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName, "*********已存在IP层链路：  " + finlink.getName() + "  上的虚拟链路条数： "
								+ finlink.getVirtualLinkList().size());
					} else {
						System.out.println("虚拟链路条数：" + createlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName, "虚拟链路条数：" + createlink.getVirtualLinkList().size());
						createlink.getVirtualLinkList().add(Vlink);
						System.out.println("IP层上新建链路 " + createlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
								+ Vlink.getUsedcapacity() + "\n " + "共有的flow:  " + Vlink.getFullcapacity()
								+ "    预留的flow：  " + Vlink.getRestcapacity() );
						file_io.filewrite2(OutFileName,
								"IP层上新建链路 " + createlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: " + Vlink.getUsedcapacity()
										+ "\n " + "共有的flow:  " + Vlink.getFullcapacity() + "    预留的flow：  "
										+ Vlink.getRestcapacity() );
						System.out.println("*********新建IP链路：  " + createlink.getName() + "  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName, "*********新建IP链路：  " + createlink.getName() + "  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
					}
				}
			}
			if (routelength > 4000) {
				ProregeneratorPlace rgp = new ProregeneratorPlace();
				success = rgp.ProRegeneratorPlace(nodepair, opPrtectRoute, wprlist, routelength, oplayer, iplayer,
						IPflow, request,ProLengthList);
			}
		}

		if (success && routelength < 4000) {
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					wpr.setproroute(opPrtectRoute);
					ArrayList<Link> totallink = new ArrayList<>();
					totallink = opPrtectRoute.getLinklist();
					wpr.setrequest(request);
					wpr.setprolinklist(totallink);
					wpr.setproroute(opPrtectRoute);
					wpr.setFSoneachLink(FSuseOnlink);
					wpr.setprovirtuallinklist(provirtuallinklist);
					wpr.setregthinglist(null);
				}
			}
		}
		ArrayList<WorkandProtectRoute> FailWpr=new ArrayList<WorkandProtectRoute>();
		if(!success){//在光层无法建立保护路由
			for(WorkandProtectRoute wpr: wprlist){
				if (wpr.getdemand().equals(nodepair)) {
					FailWpr.add(wpr);
				}
			}
			for(WorkandProtectRoute removewpr: FailWpr){
				wprlist.remove(removewpr);
			}
		}
		return success;
	}

	public ArrayList<Integer> FSassignOnlink(ArrayList<Link> linklist, ArrayList<WorkandProtectRoute> wprlist,
			NodePair nodePair, int slotnum, Layer oplayer) {
		// 本方法实现在给定一个保护路由的链路集合时候，通过判断能否与之前建立的保护路径共享 然后对其分配频谱 实现最大化共享FS
		file_out_put file_io = new file_out_put();
		Test t = new Test();
		ArrayList<Integer> RemoveslotIndex = new ArrayList<>();
		HashMap<WorkandProtectRoute, ArrayList<Integer>> shareslotWPR = new HashMap<WorkandProtectRoute, ArrayList<Integer>>();
		HashMap<WorkandProtectRoute, ArrayList<Integer>> NoShareWPR = new HashMap<WorkandProtectRoute, ArrayList<Integer>>();
		WorkandProtectRoute nowwpr = new WorkandProtectRoute(null);
		ArrayList<FSshareOnlink> fsonLinklist = new ArrayList<>();

		for (WorkandProtectRoute wpr : wprlist) {
			if (wpr.getdemand().equals(nodePair)) {
				nowwpr = wpr;
			}
		}

		for (Link link : linklist) {
			ArrayList<Integer> shareslotIndex = new ArrayList<>();
			ArrayList<Integer> NoShareslotIndex = new ArrayList<>();
			shareslotWPR.clear();
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodePair))
					continue;
				// System.out.println("此时的节点对为 "+wpr.getdemand().getName()+"
				// 此时的link 为"+ link.getName());
				// file_io.filewrite2(OutFileName,"此时的节点对为
				// "+wpr.getdemand().getName()+" 此时的link 为"+ link.getName());
				if (wpr.getprolinklist().contains(link)) {
					int cross = t.linklistcompare(nowwpr.getworklinklist(), wpr.getworklinklist());
					if (cross == 0) {// 表示该链路上面的FS可以共享
						ArrayList<FSshareOnlink> FSShareOnlink = wpr.getFSoneachLink();
						// file_io.filewrite2(OutFileName,"此时的WPR 为
						// "+wpr.getdemand().getName());
						if (FSShareOnlink != null) {
							for (FSshareOnlink FSOnoneLink : FSShareOnlink) {
								if (FSOnoneLink.getlink().equals(link)) {
									for (int share : FSOnoneLink.getslotIndex()) {
										if (!shareslotIndex.contains(share))
											// System.out.println("可以共享的FS 为
											// "+share);
											// file_io.filewrite2(OutFileName,"可以共享的FS
											// 为 "+share);
											shareslotIndex.add(share);
									}
								}
							}
						}
					}
					if (cross == 1) {// 表示该链路上面的FS不可以共享
						ArrayList<FSshareOnlink> FSShareOnlink = wpr.getFSoneachLink();
						if (FSShareOnlink != null) {
							for (FSshareOnlink FSOnoneLink : FSShareOnlink) {
								if (FSOnoneLink.getlink().equals(link)) {
									for (int NOshare : FSOnoneLink.getslotIndex()) {
										if (!NoShareslotIndex.contains(NOshare)) {
											// System.out.println("不可以共享的FS 为
											// "+NOshare);
											// file_io.filewrite2(OutFileName,"不可以共享的FS
											// 为 "+NOshare);
											NoShareslotIndex.add(NOshare);
										}
									}
								}
							}

						}
					}

					if (shareslotIndex.size() != 0)
						shareslotWPR.put(wpr, shareslotIndex);
					if (NoShareslotIndex.size() != 0)
						NoShareWPR.put(wpr, NoShareslotIndex);
				}
			} // 对每一段link上面的可共享和不可共享FS进行统计保存
			// file_io.filewrite2(OutFileName,"");
			// file_io.filewrite2(OutFileName,"FS移除测试");
			for (WorkandProtectRoute wpr : wprlist) {
				RemoveslotIndex.clear();
				if (shareslotWPR.keySet().contains(wpr)) {

					for (int re : shareslotWPR.get(wpr)) {// 取出可以共享的FS
						// file_io.filewrite2(OutFileName," ");
						// file_io.filewrite2(OutFileName,"可以共享的FS "+re);
						for (WorkandProtectRoute comwpr : wprlist) {
							// file_io.filewrite2(OutFileName,"用来比较的WPR
							// "+comwpr.getdemand().getName());
							if (NoShareWPR.keySet().contains(comwpr)) {
								if (NoShareWPR.get(comwpr).contains(re)) {// 说明该FS在其他业务上是不可以共享的
									// file_io.filewrite2(OutFileName,"该FS不可以共享
									// "+re);
									if (!RemoveslotIndex.contains(re)) {
										RemoveslotIndex.add(re);
										break;
									}
								}
							}
						}
					}
					// test

					// for (int remove : RemoveslotIndex) {
					// file_io.filewrite_without(OutFileName,"需要移除的FS为"+remove+" ");
					// }
					// file_io.filewrite2(OutFileName,"");
					// for(int share:shareslotWPR.get(wpr)){
					// file_io.filewrite_without(OutFileName,"可以共享的FS为 "+share+"
					// ");
					// }
					// file_io.filewrite2(OutFileName,"");
					if (RemoveslotIndex.size() != 0 && RemoveslotIndex != null) {
						for (int remove : RemoveslotIndex) {

							// file_io.filewrite2(OutFileName,"可以共享的FS数目还剩
							// "+shareslotWPR.get(wpr).size());
							// for(int last:shareslotWPR.get(wpr)){
							// file_io.filewrite_without(OutFileName,last+" ");
							// }
							// file_io.filewrite2(OutFileName,"");
							int index = shareslotWPR.get(wpr).indexOf(remove);
							// file_io.filewrite2(OutFileName," "+remove);
							// file_io.filewrite2(OutFileName,"需要移除的链路index为
							// "+index);
							shareslotWPR.get(wpr).remove(index);
							// file_io.filewrite2(OutFileName,"已经移除的FS为
							// "+remove);
						} // 将每个WPR上面不可以共享的FS去掉
					}
					FSshareOnlink fsol = new FSshareOnlink(link, shareslotIndex);
					fsol.setwpr(wpr);
					fsonLinklist.add(fsol);
				}
			}
			
			for (WorkandProtectRoute wpr : wprlist) {
				if (shareslotWPR.keySet().contains(wpr)) {
					if (shareslotWPR.get(wpr).size() != 0) {
						file_io.filewrite2(OutFileName, " ");
						file_io.filewrite_without(OutFileName, "链路 " + link.getName() + " 上可以共享的slot为 ");
						System.out.print("链路 " + link.getName() + " 上可以共享的slot为 ");
						for (int release : shareslotWPR.get(wpr)) {// 释放可共享资源
							Request request = wpr.getrequest();

							// System.out.println("可共享链路的业务为"+request.getNodepair().getName()+"
							// 可共享的链路为："+link.getName()+" 链路上的FS为："+release);//test);
							// file_io.filewrite2(OutFileName,"可共享链路的业务为 "+request.getNodepair().getName()+"
							// 可共享的链路为："+link.getName()+" 链路上的FS为："+release);//test
							// test

							// System.out.println("该链路上的request个数"+link.getSlotsarray().get(release).getoccupiedreqlist().size());
							// file_io.filewrite2(OutFileName,"该链路上的request个数
							// "+link.getSlotsarray().get(release).getoccupiedreqlist().size());
							// for(Request
							// re:link.getSlotsarray().get(release).getoccupiedreqlist()){
							// System.out.println("占用该链路该FS的节点对为
							// "+re.getNodepair().getName());
							// file_io.filewrite2(OutFileName,"占用该链路该FS的节点对为
							// "+re.getNodepair().getName());
							// }

							
							// link.getSlotsarray().get(release).getoccupiedreqlist().get(0);
							// link.getSlotsarray().get(release).getoccupiedreqlist().remove(res);
							link.getSlotsarray().get(release).getoccupiedreqlist().remove(request);
							// test
							file_io.filewrite_without(OutFileName, release + "    ");
							System.out.print(release + "    ");
						}
						file_io.filewrite2(OutFileName, " ");
						}
				}
			}
		
		} // 每一段link上面的FS均释放完毕
//		file_io.filewrite2(OutFileName, "    ");

		// link上面可以共享的资源释放完毕 之后进行RSA
		ArrayList<Integer> index_wave = new ArrayList<Integer>();
		Mymain mm = new Mymain();
		// file_io.filewrite2(OutFileName,"每段链路上需要的FS数为： "+slotnum );
		index_wave = mm.spectrumallocationOneRoute(false, null, linklist, slotnum); // 每个link上面均占用这么多

		if(index_wave!=null&& index_wave.size()!=0){
			file_io.filewrite2(OutFileName, "此次RSA分配的slot起点为 " + index_wave.get(0) + " ,长度为 " + slotnum);
			System.out.println("此次RSA分配的slot起点为 " + index_wave.get(0) + " ,长度为 " + slotnum);
				int share = 0, newFS = 0;
				for (Link link : linklist) {// 恢复之前占用的
					for (FSshareOnlink fl : fsonLinklist) {// 对于每一段link要遍历之前所有的业务
						if (fl.getlink().equals(link)) {
							Request request = fl.getwpr().getrequest();
							for (int recovery : fl.getslotIndex()) {
								link.getSlotsarray().get(recovery).getoccupiedreqlist().add(request);
								
								for (int co = index_wave.get(0); co < index_wave.get(0) + slotnum; co++) {
										if (co == recovery) {
											share++;
											break;
										}
								}
							 
							}
						}
					}
					if (slotnum < share) {
						share = slotnum;
					}
					newFS = newFS + slotnum - share;
					
				}
				nodePair.setSlotsnum(newFS);
				file_io.filewrite2(OutFileName, "此次RSA需要的新slot数为 " + newFS);
			}
		
		// file_io.filewrite2(OutFileName,"");
		// file_io.filewrite2(OutFileName,"恢复占用之后");
		// for(Link link:linklist){
		// for(int n=0;n<link.getSlotsarray().size();n++){
		// if (link.getSlotsarray().get(n).getoccupiedreqlist().size() !=
		// 0){//说明该FS有占用
		// System.out.println("链路"+link.getName()+"上FS "+n+" 已被
		// "+link.getSlotsarray().get(n).getoccupiedreqlist().size()+" 个业务占用");
		// file_io.filewrite2(OutFileName,"链路"+link.getName()+"上FS "+n+" 已被
		// "+link.getSlotsarray().get(n).getoccupiedreqlist().size()+" 个业务占用");

		// for(Request re:link.getSlotsarray().get(n).getoccupiedreqlist()){
		// if(re!=null){
		// System.out.println("链路"+link.getName()+"上FS "+n+"
		// 已被业务占用"+re.getNodepair().getName());
		// file_io.filewrite2(OutFileName,"链路"+link.getName()+"上FS "+n+" 已被业务
		// "+re.getNodepair().getName()+"占用");
		// }
		// }
		// }
		// }
		// }

		return index_wave;

	}
}
