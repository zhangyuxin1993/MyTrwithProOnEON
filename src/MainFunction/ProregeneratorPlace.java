package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import general.file_out_put;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class ProregeneratorPlace {
	String OutFileName = Mymain.OutFileName;
	static int totalregNum = 0;

	public boolean proregeneratorplace(NodePair nodepair, LinearRoute newRoute, ArrayList<WorkandProtectRoute> wprlist,
			double routelength, Layer oplayer, Layer ipLayer, int IPflow,Request request) {
		WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);
		ArrayList<VirtualLink> provirtuallinklist = new ArrayList<>();
		ProregeneratorPlace rgp2 = new ProregeneratorPlace();
		Test t = new Test();
		ArrayList<Integer> ShareReg = new ArrayList<>();
		ArrayList<Node> comnodelist = new ArrayList<>();
		ArrayList<Regenerator> sharereglist = new ArrayList<>();
		ArrayList<Regenerator> removereglist = new ArrayList<>();
		ArrayList<Regenerator> addreglist = new ArrayList<>();
		ArrayList<RouteAndRegPlace> regplaceoption = new ArrayList<>();
		ProregeneratorPlace rgp = new ProregeneratorPlace();
		file_out_put file_io = new file_out_put();
	 
		// part1 找到该保护链路上面已存在的共享再生器
		for (WorkandProtectRoute nowwpr : wprlist) {
			if (nowwpr.getdemand().equals(nodepair))
				nowdemand = nowwpr;
		}
		for (WorkandProtectRoute wpr : wprlist) {// 在已存在的业务中 找出新业务上已存在的共享再生器
		
			for (Regenerator newreg : wpr.getnewreglist()) {// 只看该链路上有没有新建的再生器
				Node node = newreg.getnode();
				if (newRoute.getNodelist().contains(node)) {// 如果之前的业务在某一节点上已经放置了再生器

					// 判断该业务与新业务可否共享再生器（两个业务的工作链路对应的物理链路是否交叉）
					int already = 0, newregg = 0;
					int cross = t.linklistcompare(nowdemand.getworklinklist(), wpr.getworklinklist());
					if (cross == 0) {//首先判断了这个新产生的再生器是否可以共享
						//fix
						boolean noshareFlag=false;
						for (WorkandProtectRoute comwpr : wprlist) {
							if(wpr.getdemand().equals(comwpr.getdemand())) continue;
							for (Regenerator haveshareReg : comwpr.getsharereglist()) {
								if(haveshareReg.equals(newreg)){//其他业务上曾经共享该再生器
									file_io.filewrite2(OutFileName, "已有业务"+comwpr.getdemand().getName()+"共享该再生器,"+
								haveshareReg.getnode().getName()+"上的第"+haveshareReg.getindex()+"个再生器");
								 
								int cross_second = t.linklistcompare(nowdemand.getworklinklist(), comwpr.getworklinklist());
								if(cross_second==1){
									noshareFlag=true;
									break;
								}
								}
							}
						}
						if(!noshareFlag){//表示该再生器在业务上也可以共享
							int po = t.nodeindexofroute(node, newRoute);// 保存新链路上可以共享的再生器的位置
							if (po != 0 && po != newRoute.getNodelist().size() - 1) {// 判断新链路上已存在的再生器是否在链路的两端
								if (comnodelist.contains(node)) {// 说明该节点上已存在可共享的再生器  此时需要选择用哪个共享再生器
									for (Regenerator alreadyReg : sharereglist) {
										if (alreadyReg.getnode().equals(node)) {// 此时alreadyReg表示共享列表中已存在的reg
											for (WorkandProtectRoute comwpr : wprlist) {// 一下比较哪个再生器使用的多
												if (comwpr.getRegeneratorlist().contains(alreadyReg)) {
													already++;
												}
												if (comwpr.getRegeneratorlist().contains(newreg)) {
													newregg++;
												}
											}
										}
										if (already < newregg) {// 说明新增加的reg共享的保护链路比较多
											removereglist.add(alreadyReg);
											addreglist.add(newreg);
										}
									}
									for (Regenerator remoReg : removereglist) {
										sharereglist.remove(remoReg);
									}
									for (Regenerator addReg : addreglist) {
										if(!sharereglist.contains(addReg))
										sharereglist.add(addReg);
									}
									
								} else {// 新产生的再生器
									comnodelist.add(node);
									sharereglist.add(newreg);
								}
								// System.out.println("再生器的个数："+sharereglist.size());
								// for(Regenerator reg:sharereglist){
								// System.out.println(reg.getnode().getName());
								// }
								if (!ShareReg.contains(po))
									ShareReg.add(po); // 保存了新的业务上哪些节点上面有再生器
							}
						}
					}
				}
			}
		}
		// part1 finish 存储了所有该链路上可共享再生器的位置
		boolean success = false, passflag = false;
		int minRegNum = (int) Math.floor(routelength / 4000);
		int internode = newRoute.getNodelist().size() - 2;
		// part2 当路由上共享再生器的个数小于所需再生器的最小个数时 给定set进行RSA 产生regplaceoption
		System.out.println();
		file_io.filewrite2(OutFileName, "");
		System.out.println("可共享再生器的个数：" + ShareReg.size() + "需要的最少再生器个数：" + minRegNum);
		file_io.filewrite2(OutFileName, "可共享再生器的个数：" + ShareReg.size() + "需要的最少再生器个数：" + minRegNum);
		file_io.filewrite_without(OutFileName, "可共享再生器的位置：");
		for (int a : ShareReg)
			file_io.filewrite_without(OutFileName, a + "  ");
		    file_io.filewrite2(OutFileName, "   ");

		if (ShareReg.size() <= minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (regplaceoption.size() != 0)
					break;
				Test nOfm = new Test(s, internode); // 在所有中间节点中随机选取m个点来放置再生器
				while (nOfm.hasNext()) {
					passflag = false;
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
						continue;// 已有的共享再生器 已经内定所以所有产生的可能性中要包含这些再生器

					// 给定再生器节点之后进行RSA 产生option选项的路径
					rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption, wprlist, nodepair);
				}
			}
		}

		// part3 当路由上共享再生器的个数大于所需再生器的最小个数时 给定set进行RSA产生regplaceoption
		if (ShareReg.size() > minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (regplaceoption.size() != 0)
					break;
				Test nOfm = new Test(s, internode); // 在所有中间节点中随机选取m个点来放置再生器
				while (nOfm.hasNext()) {
					passflag = false;
					int[] set = nOfm.next(); // 随机产生的再生器放置位置
					if (s <= ShareReg.size()) { // 此时再生器必须从可共享的再生器里面选择
						for (int p = 0; p < set.length; p++) {
							int p1 = set[p];
							if (!ShareReg.contains(p1)) {
								passflag = true;
								break;
							}
						}
						if (passflag)
							continue;
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
							continue;
					} // 以上主要为了产生set
						// 给定再生器节点之后进行RSA
					rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption, wprlist, nodepair);
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
			rgp2.FinalRouteRSA(nodepair, finalRoute, oplayer, ipLayer, IPflow, wprlist, provirtuallinklist, ShareReg,sharereglist,request);
			// 对于finalroute进行再生器节点存储！！
		}
		if (regplaceoption.size() == 0) {
			success = false;
		}
		System.out.println();
		file_io.filewrite2(OutFileName, "");
		if (success) {
			System.out.print("保护路径再生器放置成功并且RSA,放置的再生器个数为");
			file_io.filewrite_without(OutFileName, "保护路径再生器放置成功并且RSA,放置的再生器个数为");
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					wpr.setrequest(request);
					System.out.println(wpr.getRegeneratorlist().size());
					file_io.filewrite(OutFileName, wpr.getRegeneratorlist().size());
				}
			}

		} else {
			System.out.println("保护路径放置再生器不成功改路径被堵塞");
			file_io.filewrite2(OutFileName, "保护路径放置再生器不成功改路径被堵塞");
		}
		return success;
	}// 主函数结束

	public void RSAunderSet(int[] set, LinearRoute newRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<RouteAndRegPlace> regplaceoption, ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair) {
		boolean partworkflag = false, RSAflag = false, regflag = false;
		double length = 0;
		file_out_put file_io = new file_out_put();
		ArrayList<Link> linklist = new ArrayList<>();
		int FStotal = 0, n = 0;
		ProregeneratorPlace rp = new ProregeneratorPlace();
		ArrayList<Float> RemainRatio=new ArrayList<>();//记录每段链路上剩余的flow
		
		for (int i = 0; i < set.length + 1; i++) {// RSA的次数比再生器的个数多1
			if (!partworkflag && RSAflag)
				break;
			if (i < set.length) {
				System.out.println("****************再生器的位置为：" + set[i]); // set里面的数应该是节点的位置+1！
				file_io.filewrite2(OutFileName, "****************再生器的位置为：" + set[i]);
			} else {
				System.out.println("************最后一个再生器与终结点之间的RSA ");
				file_io.filewrite2(OutFileName, "************最后一个再生器与终结点之间的RSA ");
				regflag = true;
			}
			do {// 通过一个
				Node nodeA = newRoute.getNodelist().get(n);
				Node nodeB = newRoute.getNodelist().get(n + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println(link.getName());
				file_io.filewrite2(OutFileName, link.getName());
				length = length + link.getLength();
				linklist.add(link);
				n = n + 1;
				if (!regflag) {// 未到达最后一段路径的RSA
					if (n == set[i]) {
						float remainFlow=0;
						partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair,remainFlow);// 此时在n点放置再生器
						RemainRatio.add(remainFlow);
						FStotal = FStotal + nodepair.getSlotsnum();
						length = 0;
						RSAflag = true;
						linklist.clear();
						break;
					}
				}
				if (n == newRoute.getNodelist().size() - 1) {
					float remainFlow=0;
					partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair,remainFlow);// 此时在n点放置再生器
					RemainRatio.add(remainFlow);
					FStotal = FStotal + nodepair.getSlotsnum();
				}
				if (!partworkflag && RSAflag)// 如果之前的链路已经RSA失败 剩下的链路也没有RSA的必要
					break;
			} while (n != newRoute.getNodelist().size() - 1);
			// 如果路由成功则保存该路由对于再生器的放置
		}
		if (partworkflag) {
			RouteAndRegPlace rarp = new RouteAndRegPlace(newRoute, 1);
			rarp.setnewFSnum(FStotal);
			ArrayList<Integer> setarray = new ArrayList<>();
			ArrayList<Integer> IPRegarray = new ArrayList<>();
			for (int k = 0; k < set.length; k++) {
				setarray.add(set[k]);
				if(RemainRatio.get(k)>0.1||RemainRatio.get(k+1)>0.1){// 只要再生器前面或者后面有一段未充分使用则放置IP再生器
					IPRegarray.add(set[k]);//存储IP再生器放置节点
				}
			}
			rarp.setregnode(setarray);
			rarp.setregnum(setarray.size());
			regplaceoption.add(rarp);
			System.out.println("该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());// 再生器的个数加进去
			file_io.filewrite2(OutFileName, "该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());
		}
	}

	public Boolean vertify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer, Layer iplayer,
			ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair,double RemainRatio) {
		// 判断某一段transparent链路是否能够成功RSA 并且记录新使用的FS数量
		// workOrproflag=true的时候表示是工作 false的时候表示保护
		file_out_put file_io = new file_out_put();
		nodepair.setSlotsnum(0);
		double X = 1;
		opProGrooming opg = new opProGrooming();
		int slotnum = 0;
		boolean opworkflag = false;
		if (routelength > 4000) {
			System.out.println("链路过长无法RSA");
			file_io.filewrite2(OutFileName, "链路过长无法RSA");
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
			System.out.println("每段链路所需的slot数为： " + slotnum);
			file_io.filewrite2(OutFileName, "每段链路所需的slot数为： " + slotnum);
			
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			index_wave = opg.FSassignOnlink(linklist, wprlist, nodepair, slotnum,oplayer);// 在考虑共享的情况下分配频谱
			if (index_wave.size() != 0) {
				opworkflag = true;
				System.out.println("可以RSA,start为 "+index_wave.get(0));
				file_io.filewrite2(OutFileName, "可以RSA,start为 "+index_wave.get(0));
			} else {
				System.out.println("频谱不够无法RSA");
				file_io.filewrite2(OutFileName, "频谱不够无法RSA");
			}

		}

		return opworkflag;
	}

	public void FinalRouteRSA(NodePair nodepair, RouteAndRegPlace finalRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<WorkandProtectRoute> wprlist, ArrayList<VirtualLink> provirtuallinklist,
			ArrayList<Integer> ShareReg, ArrayList<Regenerator> sharereglist,Request request) {
		file_out_put file_io = new file_out_put();
		ArrayList<Link> alllinklist = new ArrayList<>();
		ArrayList<Regenerator> regthinglist = new ArrayList<>();
		Test t = new Test();
		file_io.filewrite2(OutFileName, "");
		System.out.println("" );
		System.out.println("对最终路径进行RSA：" );
		file_io.filewrite2(OutFileName, "对最终路径进行RSA：" );
		
		finalRoute.getRoute().OutputRoute_node(finalRoute.getRoute());
		int count = 0;
		double length2 = 0;
		boolean regflag2 = false;
		ArrayList<Link> linklist2 = new ArrayList<>();
		ArrayList<FSshareOnlink> FSoneachLink = new ArrayList<FSshareOnlink>();

		for (int i = 0; i < finalRoute.getregnum() + 1; i++) {
			if (i >= finalRoute.getregnum())
				regflag2 = true;
			do {
				Node nodeA = finalRoute.getRoute().getNodelist().get(count);
				Node nodeB = finalRoute.getRoute().getNodelist().get(count + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println();
				file_io.filewrite2(OutFileName,"");
				System.out.println("最终路径上RSA的链路：" + link.getName());
				file_io.filewrite2(OutFileName, "最终路径上RSA的链路：" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// 未到达最后一段路径的RSA
					if (count == finalRoute.getregnode().get(i)) {
						Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist, wprlist,nodepair, FSoneachLink,request);// 此时在n点放置再生器
						for (Link addlink : linklist2) {
							alllinklist.add(addlink);
						}
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {
					Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist, wprlist,nodepair, FSoneachLink,request);// 为目的节点前的剩余链路进行RSA
					for (Link addlink : linklist2) {
						alllinklist.add(addlink);
					}
					linklist2.clear();
				}
			} while (count != finalRoute.getRoute().getNodelist().size() - 1);
		}

		ArrayList<Regenerator> shareReg = new ArrayList<>();
		ArrayList<Regenerator> newReg = new ArrayList<>();
		HashMap<Integer, Regenerator> hashregthinglist = new HashMap<Integer, Regenerator>();
		System.out.println("！！！！！最终路径上再生器节点的数量：" + finalRoute.getregnode().size());
		file_io.filewrite2(OutFileName, "！！！！！最终路径上再生器节点的数量：" + finalRoute.getregnode().size());

		for (int i : finalRoute.getregnode()) {// 取出路径上所有再生器节点
			Node regnode = finalRoute.getRoute().getNodelist().get(i);// 考虑可共享和不可共享
			if (ShareReg.contains(i)) {// 该再生器可以共享
				for (Regenerator r : sharereglist) {
					if (r.getnode().equals(regnode)) {
						regthinglist.add(r);// 找出可共享的再生器 加入再生器集合
						hashregthinglist.put(t.nodeindexofroute(regnode, finalRoute.getRoute()), r); // 建立Hashmap!!!
						shareReg.add(r);// 加入针对于该链路的可共享再生器集合
					}
				}
			} else {// 表示不可以共享 此时要建立新的再生器 并且改变node上面再生器的个数
				regnode.setregnum(regnode.getregnum() + 1);
				int index = regnode.getregnum();
				Regenerator reg = new Regenerator(regnode);
				reg.setindex(index);
				regthinglist.add(reg);
				hashregthinglist.put(t.nodeindexofroute(regnode, finalRoute.getRoute()), reg); // 建立Hashmap!!!
				newReg.add(reg);
			}
		}

		for (WorkandProtectRoute wpr : wprlist) {
			if (wpr.getdemand().equals(nodepair)) {
				wpr.setFSoneachLink(FSoneachLink);
				wpr.setregthinglist(hashregthinglist);
				wpr.setRegeneratorlist(regthinglist);
				wpr.setprolinklist(alllinklist);
				wpr.setnewreglist(newReg);
				wpr.setsharereglist(shareReg);
				wpr.setprovirtuallinklist(provirtuallinklist);
			}
		}
	}

	public boolean Prolinkcapacitymodify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer,
			Layer iplayer, ArrayList<VirtualLink> provirtuallinklist, ArrayList<WorkandProtectRoute> wprlist,
			NodePair nodepair, ArrayList<FSshareOnlink> FSoneachLink,Request request) {
		// 建立虚拟链路 更改容量 RSA
		double X = 1;
		opProGrooming opg = new opProGrooming();
		int slotnum = 0, shareFS = 0;
		boolean opworkflag = false, shareFlag = true;
		Node srcnode = new Node(null, 0, null, iplayer, 0, 0);
		Node desnode = new Node(null, 0, null, iplayer, 0, 0);
		Test t = new Test();
		file_out_put file_io = new file_out_put();
//		Request request = new Request(nodepair);
		
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

		opworkflag = true;
		double length1 = 0;
		double cost = 0;
		ArrayList<Integer> index_wave = new ArrayList<Integer>();
		index_wave = opg.FSassignOnlink(linklist, wprlist, nodepair, slotnum,oplayer);// 在考虑共享的情况下分配频谱

		
		for (Link link : linklist) {
			ArrayList<Integer> index_wave1=new ArrayList<Integer>();
			length1 = length1 + link.getLength();
			cost = cost + link.getCost();
			ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
			link.setMaxslot(slotnum + link.getMaxslot());
			file_io.filewrite_without(OutFileName, "!!!request为 "+request.getNodepair().getName());
			System.out.print("这段链路上分配的FS为 ");
			file_io.filewrite_without(OutFileName, "这段链路上分配的FS为 "+"   index起始"+index_wave.get(0)+"  "+slotnum);
			file_io.filewrite2(OutFileName,"");
			int m=index_wave.get(0);
			for(int n=0;n<slotnum;n++){
				index_wave1.add(m);
				System.out.print(m);
				file_io.filewrite_without(OutFileName, m+"  ");
				m++;
			}
			System.out.println();
			file_io.filewrite2(OutFileName, " ");
			FSshareOnlink fsonLink = new FSshareOnlink(link, index_wave1);
			FSoneachLink.add(fsonLink);
		}
		
		// 首先取出linklist里面的前两个链路和最后两个链路
		Node startnode = new Node(null, 0, null, iplayer, 0, 0);
		Node endnode = new Node(null, 0, null, iplayer, 0, 0);
		if (linklist.size() != 1) {
			Link link1 = linklist.get(0);
			Link link2 = linklist.get(1);
			Link link3 = linklist.get(linklist.size() - 2);
			Link link4 = linklist.get(linklist.size() - 1);
			Node nodeA = link1.getNodeA();
			Node nodeB = link1.getNodeB();
			Node nodeC = link4.getNodeA();
			Node nodeD = link4.getNodeB();
			file_io.filewrite2(OutFileName, "取出的链路为" + link1.getName() + "  " + link2.getName() + "   "
					+ link3.getName() + "  " + link4.getName());

			if (link2.getNodeA().equals(nodeA) || link2.getNodeB().equals(nodeA))
				startnode = nodeB;
			if (link2.getNodeA().equals(nodeB) || link2.getNodeB().equals(nodeB))
				startnode = nodeA;// 找到起始端点
			if (link3.getNodeA().equals(nodeC) || link3.getNodeB().equals(nodeC))
				endnode = nodeD;
			if (link3.getNodeA().equals(nodeD) || link3.getNodeB().equals(nodeD))
				endnode = nodeC;// 找到终止端点
			file_io.filewrite2(OutFileName, "找到的节点：" + startnode.getName() + "  " + endnode.getName());
		}
		if (linklist.size() == 1) {
			startnode = linklist.get(0).getNodeA();
			endnode = linklist.get(0).getNodeB();
		}

		for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// 在IP层中寻找transparent链路的两端
			boolean srcflag = false, desflag = false;
			// System.out.println(iplayer.getNodelist()..get(0).getName());
			HashMap<String, Node> map = iplayer.getNodelist();
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				Node node = (Node) (map.get(iter.next()));
				if (node.getName().equals(startnode.getName())) {
					srcnode = node;
					srcflag = true;
				}
				if (node.getName().equals(endnode.getName())) {
					desnode = node;
					desflag = true;
				}
			}
			if (srcflag && desflag)
				break;
		}

		int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的故这样设置index

		// file_io.filewrite2(OutFileName,"src的节点度:"+srcnode.getIndex()+"
		// des节点度："+desnode.getIndex());
		if (srcnode.getIndex() > desnode.getIndex()) {
			Node internode = srcnode;
			srcnode = desnode;
			desnode = internode;
		}
		String name = srcnode.getName() + "-" + desnode.getName();
		// file_io.filewrite2(OutFileName,"此时的原节点为:"+srcnode.getName()+"
		// 终结点为"+desnode.getName());
		Link finlink = iplayer.findLink(srcnode, desnode);
		Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
		boolean findflag = false;
		try {
			System.out.println(finlink.getName());
			file_io.filewrite2(OutFileName, finlink.getName());
			findflag = true;
		} catch (java.lang.NullPointerException ex) {
			System.out.println("IP 层没有该链路需要新建链路");
			file_io.filewrite2(OutFileName, "IP 层没有该链路需要新建链路");
			file_io.filewrite2(OutFileName, "此时的原节点为:" + srcnode.getName() + "  终结点为" + desnode.getName());
			createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
			iplayer.addLink(createlink);
		}

		VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
		if (!shareFlag || shareFS <= slotnum) {// 表示该linklist中有链路不能共享FS或者均可以共享时共享的FS小于需要的FS
			Vlink.setnature(1);
			Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
			Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
			Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
			Vlink.setlength(length1);
			Vlink.setcost(cost);
			Vlink.setPhysicallink(linklist);
			provirtuallinklist.add(Vlink);
		}
		if (shareFS > slotnum) {// 表示该linklist中有链路不能共享FS或者均可以共享时共享的FS小于需要的FS
			Vlink.setnature(1);
			Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
			Vlink.setFullcapacity(shareFS * X);// 多出来的flow是从这里产生的
			Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
			Vlink.setlength(length1);
			Vlink.setcost(cost);
			Vlink.setPhysicallink(linklist);
			provirtuallinklist.add(Vlink);
		}

		if (findflag) {// 如果在IP层中已经找到该链路
			finlink.getVirtualLinkList().add(Vlink);
			System.out.println("IP层已存在的链路 " + finlink.getName() + "\n " + "    预留的flow：  " + Vlink.getRestcapacity());
			System.out.println(
					"保护链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： " + finlink.getVirtualLinkList().size());
			file_io.filewrite2(OutFileName,
					"IP层已存在的链路 " + finlink.getName() + "\n " + "    预留的flow：  " + Vlink.getRestcapacity());
			file_io.filewrite2(OutFileName,
					"保护链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： " + finlink.getVirtualLinkList().size());
		} else {
			createlink.getVirtualLinkList().add(Vlink);
			System.out.println("IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
			System.out.println(
					"保护链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： " + createlink.getVirtualLinkList().size());
			file_io.filewrite2(OutFileName,
					"IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
			file_io.filewrite2(OutFileName,
					"保护链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： " + createlink.getVirtualLinkList().size());
		}

		return opworkflag;
	}

	public RouteAndRegPlace optionRouteSelect(ArrayList<RouteAndRegPlace> regplaceoption,
			ArrayList<WorkandProtectRoute> wprlist) {
		RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 1);
		int FS = 10000;
		ArrayList<RouteAndRegPlace> RemoveRoute = new ArrayList<>();
		// 第一种比较：比较每个链路新使用的FS个数 选取最少的
		for (RouteAndRegPlace route : regplaceoption) {
			if (route.getnewFSnum() <= FS) {
				FS = route.getnewFSnum();
				// route.setnewFSnum(newFSnum);
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
					for (Regenerator reg1 : wpr.getRegeneratorlist()) {
						Node node = reg1.getnode();
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

}
