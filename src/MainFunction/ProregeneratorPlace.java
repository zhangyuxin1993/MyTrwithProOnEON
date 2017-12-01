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

	// 在RSAunderSet 里面控制阈值
	public boolean ProRegeneratorPlace(NodePair nodepair, LinearRoute newRoute, ArrayList<WorkandProtectRoute> wprlist,
			double routelength, Layer oplayer, Layer ipLayer, int IPflow, Request request) {
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
		for (WorkandProtectRoute nowwpr : wprlist) {// 首先找本次nodepair对应的 wpr
			if (nowwpr.getdemand().equals(nodepair)) {
				nowdemand = nowwpr;
				break;
			}
		}

		for (WorkandProtectRoute wpr : wprlist) {// 在已存在的业务中 找出新业务上已存在的共享再生器

			int cross = t.linklistcompare(nowdemand.getworklinklist(), wpr.getworklinklist());
			if (cross == 0) {// 首先判断了这个新产生的再生器是否可以共享

				for (Regenerator newreg : wpr.getnewreglist()) {// 只看该链路上有没有新建的再生器
					Node node = newreg.getnode();
					if (newRoute.getNodelist().contains(node)) {// 如果之前的业务在某一节点上已经放置了再生器

						// 判断该业务与新业务可否共享再生器（两个业务的工作链路对应的物理链路是否交叉）
						int already = 0, newregg = 0;
						boolean noshareFlag = false;
						for (WorkandProtectRoute comwpr : wprlist) {
							if (wpr.getdemand().equals(comwpr.getdemand()))
								continue;
							for (Regenerator haveshareReg : comwpr.getsharereglist()) {
								if (haveshareReg.equals(newreg)) {// 其他业务上曾经共享该再生器
									file_io.filewrite2(OutFileName,
											"已有业务" + comwpr.getdemand().getName() + "共享该再生器,"
													+ haveshareReg.getnode().getName() + "上的第" + haveshareReg.getindex()
													+ "个再生器");

									int cross_second = t.linklistcompare(nowdemand.getworklinklist(),
											comwpr.getworklinklist());
									if (cross_second == 1) {
										noshareFlag = true;
										break;
									}
								}
							}
						} // 以上判断之前业务某一节点的再生器可否在本次业务节点上共享

						if (!noshareFlag) {// 表示该节点上再生器在本业务上也可以共享
							/*
							 * 在以下修改 优先选择IP再生器 有多个IP再生器时选择保护路径多的再生器 OEO再生器同理
							 */
							int po = t.nodeindexofroute(node, newRoute);// 保存新链路上可以共享的再生器的位置
							if (po != 0 && po != newRoute.getNodelist().size() - 1) {// 判断新链路上已存在的再生器是否在链路的两端
								/*
								 * comnodelist存储的是已经存在可共享再生器的节点 sharereglist
								 * 存储的是经判断可以共享的再生器 一下进行一个节点上多个共享再生器的筛选
								 */
								if (comnodelist.contains(node)) {// 说明该节点上已存在可共享的再生器
																	// 此时需要选择用哪个共享再生器
									for (Regenerator alreadyReg : sharereglist) {
										// sharereglist里面放置已经判断可以共享的再生器
										// sharelist里面每个节点上只有一个最优的共享再生器
										if (alreadyReg.getnode().equals(node)) {// 此时alreadyReg表示共享列表中已存在的reg

											if (alreadyReg.getNature() == 0 && newreg.getNature() == 1) {// 新来的再生器是IP再生器
																											// 原来的再生器是纯OEO再生
																											// 此时选择新到再生器
												removereglist.add(alreadyReg);
												addreglist.add(newreg);
											} else if (alreadyReg.getNature() == 1 && newreg.getNature() == 0) {// 新来的再生器是OEO再生器
																												// 原来的再生器是IP再生
																												// 此时选择原来的再生器
											} else {// 表示原来的再生器和新到的再生器的属性一致
													// 此时需要比较他们保护路径条数

												for (WorkandProtectRoute comwpr : wprlist) {// 一下比较哪个再生器使用的多
													if (comwpr.getRegeneratorlist().contains(alreadyReg)) {
														already++;
													}
													if (comwpr.getRegeneratorlist().contains(newreg)) {
														newregg++;
													}
												}
												if (already < newregg) {// 说明新增加的reg共享的保护链路比较多
													removereglist.add(alreadyReg);
													addreglist.add(newreg);
												}
												newregg = 0;
												already = 0;
												break;
											}
										}
									}
									for (Regenerator remoReg : removereglist) {
										sharereglist.remove(remoReg);
									}
									removereglist.clear();// 原来不需要吗？？？
									for (Regenerator addReg : addreglist) {
										if (!sharereglist.contains(addReg))
											sharereglist.add(addReg);
									}
									addreglist.clear();// 原来不需要吗？？？

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
		// debug
		System.out.println();
		file_io.filewrite2(OutFileName, "");
		System.out.println("可共享再生器的个数：" + ShareReg.size() + "需要的最少再生器个数：" + minRegNum);
		file_io.filewrite2(OutFileName, "可共享再生器的个数：" + ShareReg.size() + "需要的最少再生器个数：" + minRegNum);

		for (Regenerator reg : sharereglist) {
			System.out.print("可共享再生器在 " + reg.getnode().getName() + "节点上,  ");
			file_io.filewrite_without(OutFileName, "可共享再生器在 " + reg.getnode().getName() + "节点上,  ");
			if (reg.getNature() == 0) {
				System.out.println("他是纯OEO再生器 ");
				file_io.filewrite2(OutFileName, "他是纯OEO再生器 ");
			}
			if (reg.getNature() == 1) {
				System.out.print("他是IP再生器 ");
				file_io.filewrite2(OutFileName, "他是IP再生器 ");
			}
		}

		/*
		 * // part2 当路由上共享再生器的个数小于所需再生器的最小个数时 给定set进行RSA 产生regplaceoption
		 * 如果可共享再生器的个数小于最少需要再生的个数时
		 */
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
					rgp.RSAunderSet(sharereglist, ShareReg, set, newRoute, oplayer, ipLayer, IPflow, regplaceoption,
							wprlist, nodepair);
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
					rgp.RSAunderSet(sharereglist, ShareReg, set, newRoute, oplayer, ipLayer, IPflow, regplaceoption,
							wprlist, nodepair);
				}
			}
		}
		// debug 在选择备选路由之前先观察其各个性能指标
		if (regplaceoption.size() != 0) {
			for (RouteAndRegPlace DebugRegRoute : regplaceoption) {
				ArrayList<Integer> NewRegList = new ArrayList<>();
				file_io.filewrite2(OutFileName, " ");
				System.out.println();
				// 先输出可共享再生器
				if (DebugRegRoute.getUsedShareReg() != null) {
					file_io.filewrite2(OutFileName, "可共享的再生器  ");
					for (Regenerator reg : DebugRegRoute.getUsedShareReg()) {
						if (reg.getNature() == 0) {
							file_io.filewrite2(OutFileName, reg.getnode().getName() + "  OEO再生器");
						}
						if (reg.getNature() == 1) {
							file_io.filewrite2(OutFileName, reg.getnode().getName() + "  IP再生器");
						}
					}
				}
				// 首先判断可否共享不可共享则为新建
				System.out.println("新建再生器： ");
				file_io.filewrite2(OutFileName, "新建再生器： ");
				for (int Reg : DebugRegRoute.getregnode()) {
					boolean share = false;
					Node NewRegNode = DebugRegRoute.getRoute().getNodelist().get(Reg);
					for (Regenerator ShareReg2 : DebugRegRoute.getUsedShareReg()) {
						if (ShareReg2.getnode().getName().equals(NewRegNode.getName())) {// 改点上的再生器可以共享无需新建
							share = true;
							break;
						}
					}
					if (!share) {
						NewRegList.add(Reg);
						if (DebugRegRoute.getIPRegnode().contains(Reg)) { // 新建的再生器是IP再生器
							System.out.print(NewRegNode.getName() + "  IP再生器  ");
							file_io.filewrite_without(OutFileName, NewRegNode.getName() + "  IP再生器  ");
						} else {
							System.out.print(NewRegNode.getName() + "  OEO再生器  ");
							file_io.filewrite_without(OutFileName, NewRegNode.getName() + "  OEO再生器  ");
						}
					}
				}
				file_io.filewrite2(OutFileName, " ");
				System.out.println("剩余的流量： " + DebugRegRoute.getNumRemainFlow());
				file_io.filewrite2(OutFileName, "剩余的流量： " + DebugRegRoute.getNumRemainFlow());
				System.out.println("使用的newFS个数： " + DebugRegRoute.getnewFSnum());
				file_io.filewrite2(OutFileName, "使用的newFS个数： " + DebugRegRoute.getnewFSnum());
				DebugRegRoute.setNewRegList(NewRegList); // 统计每个备选路径中使用的新再生器个数
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
			rgp2.FinalRouteRSA(nodepair, finalRoute, oplayer, ipLayer, IPflow, wprlist, provirtuallinklist, ShareReg,
					sharereglist, request);
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

	public void RSAunderSet(ArrayList<Regenerator> sharereglist, ArrayList<Integer> ShareReg, int[] set,
			LinearRoute newRoute, Layer oplayer, Layer ipLayer, int IPflow, ArrayList<RouteAndRegPlace> regplaceoption,
			ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair) {
		// 建立新的再生器 并且控制阈值
		float threshold = (float) 0.03;// 在这里控制阈值
		boolean partworkflag = false, RSAflag = false, regflag = false;
		double length = 0;
		file_out_put file_io = new file_out_put();
		ArrayList<Link> linklist = new ArrayList<>();
		int FStotal = 0, n = 0;
		ProregeneratorPlace rp = new ProregeneratorPlace();
		ArrayList<Float> RemainRatio = new ArrayList<>();// 记录每段链路上剩余的flow
		float NumRemainFlow = 0;
		ArrayList<Regenerator> UseShareReg = new ArrayList<>();

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
						ParameterTransfer pt = new ParameterTransfer();
						partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair, pt);
						RemainRatio.add(pt.getRemainFlowRatio());
						NumRemainFlow = NumRemainFlow + pt.getNumremainFlow();
						FStotal = FStotal + nodepair.getSlotsnum();
						length = 0;
						RSAflag = true;
						linklist.clear();
						break;
					}
				}
				if (n == newRoute.getNodelist().size() - 1) {
					ParameterTransfer pt = new ParameterTransfer();
					partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair, pt);
					NumRemainFlow = NumRemainFlow + pt.getNumremainFlow();
					RemainRatio.add(pt.getRemainFlowRatio());
					FStotal = FStotal + nodepair.getSlotsnum();
				}
				if (!partworkflag && RSAflag)// 如果之前的链路已经RSA失败 剩下的链路也没有RSA的必要
					break;
			} while (n != newRoute.getNodelist().size() - 1);
			// 如果路由成功则保存该路由对于再生器的放置
		}
		if (partworkflag) {// 说明该set下可以RSA 此时需要建立新的再生器
			RouteAndRegPlace rarp = new RouteAndRegPlace(newRoute, 1);
			rarp.setnewFSnum(FStotal);
			ArrayList<Integer> setarray = new ArrayList<>();
			ArrayList<Integer> IPRegarray = new ArrayList<>();
			for (int k = 0; k < set.length; k++) {
				setarray.add(set[k]);
				if (!ShareReg.contains(set[k])) {// 该再生器是新建的 需要判断其类型 如果是共享的
													// 那么他的类型已经确定 不需要判断
					if (RemainRatio.get(k) >= threshold || RemainRatio.get(k + 1) >= threshold) {// 只要再生器前面或者后面有一段未充分使用则放置IP再生器
						IPRegarray.add(set[k]);// 存储IP再生器放置节点
					}
				} else {// 使用了该可共享的再生器
					for (Regenerator UsedShareReg : sharereglist) {
						if (UsedShareReg.getnode().getName().equals(newRoute.getNodelist().get(set[k]))) {
							UseShareReg.add(UsedShareReg);// 保存使用了的共享再生器
							break;
						}
					}
				}
			}
			file_io.filewrite2(OutFileName, " ");
			rarp.setUsedShareReg(UseShareReg); // 记录使用的共享再生器
			rarp.setIPRegnode(IPRegarray);// 注意这里的IP再生器并不是全部的再生器 而是新建的IP再生器
			rarp.setregnode(setarray);
			rarp.setregnum(setarray.size());
			rarp.setNumRemainFlow(NumRemainFlow);
			regplaceoption.add(rarp);
			System.out.println("该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());// 再生器的个数加进去
			file_io.filewrite2(OutFileName, "该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());
		}
	}

	public Boolean vertify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer, Layer iplayer,
			ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair, ParameterTransfer RemainRatio) {
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
			// System.out.println("每段链路所需的slot数为： " + slotnum);
			// file_io.filewrite2(OutFileName, "每段链路所需的slot数为： " + slotnum);

			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			index_wave = opg.FSassignOnlink(linklist, wprlist, nodepair, slotnum, oplayer);// 在考虑共享的情况下分配频谱
			if (index_wave.size() != 0) {
				opworkflag = true;
				RemainRatio.setRemainFlowRatio((float) ((slotnum * X - IPflow) / (slotnum * X)));
				RemainRatio.setNumremainFlow((float) (slotnum * X - IPflow));
				System.out.println("建立通道的总容量 " + slotnum * X + "   业务容量 " + IPflow + "   剩余的容量比例 "
						+ RemainRatio.getRemainFlowRatio() + "   剩余的业务量：" + RemainRatio.getNumremainFlow());
				file_io.filewrite2(OutFileName,
						"建立通道的总容量 " + slotnum * X + "   业务容量 " + IPflow + "   剩余的容量比例 "
								+ RemainRatio.getRemainFlowRatio() + "   剩余的业务量：" + RemainRatio.getNumremainFlow()
								+ "  需要的FS数量：" + slotnum);
			} else {
				System.out.println("频谱不够无法RSA");
				file_io.filewrite2(OutFileName, "频谱不够无法RSA");
			}

		}

		return opworkflag;
	}

	public void FinalRouteRSA(NodePair nodepair, RouteAndRegPlace finalRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<WorkandProtectRoute> wprlist, ArrayList<VirtualLink> provirtuallinklist,
			ArrayList<Integer> ShareReg, ArrayList<Regenerator> sharereglist, Request request) {
		// 对于最终路由 通过其再生器的类型 进行RSA（是否建立IP光路） 并且建立Wpr 存储其再生器的位置 类型
		ParameterTransfer pt = new ParameterTransfer();
		file_out_put file_io = new file_out_put();
		ArrayList<Link> alllinklist = new ArrayList<>();
		ArrayList<Regenerator> regthinglist = new ArrayList<>();
		Test t = new Test();
		file_io.filewrite2(OutFileName, "");
		System.out.println("");
		System.out.println("对最终路径进行RSA：");
		file_io.filewrite2(OutFileName, "对最终路径进行RSA：");
		file_io.filewrite2(OutFileName, " ");
		pt.setStartNode(finalRoute.getRoute().getNodelist().get(0));// 首先设置该链路的起始节点
		pt.setMinRemainFlowRSA(10000);// 首先初始化

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
				System.out.println("保护最终路径上RSA的链路：" + link.getName());
				file_io.filewrite2(OutFileName, "保护最终路径上RSA的链路：" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// 未到达最后一段路径的RSA
					if (count == finalRoute.getregnode().get(i)) {
						pt.setEndNode(finalRoute.getRoute().getNodelist().get(count));// 设置终止节点

						if (ShareReg.contains(count)) {// 先判断该再生器是否是共享的
							for (Regenerator reg : sharereglist) {
								if (reg.getnode().getName()
										.equals(finalRoute.getRoute().getNodelist().get(count).getName())) {
									if (reg.getNature() == 0) {// OEO再生器
										Prolinkcapacitymodify(false, IPflow, length2, linklist2, oplayer, ipLayer,
												provirtuallinklist, wprlist, nodepair, FSoneachLink, request,
												sharereglist, pt);// 此时在n点放置再生器
									} else if (reg.getNature() == 1) {
										Prolinkcapacitymodify(true, IPflow, length2, linklist2, oplayer, ipLayer,
												provirtuallinklist, wprlist, nodepair, FSoneachLink, request,
												sharereglist, pt);// 此时在n点放置再生器
									}
								}
							}
						} else {// 该再生器不是共享再生器
							if (finalRoute.getIPRegnode().contains(count)) {// 新建的再生器是IP再生器
								Prolinkcapacitymodify(true, IPflow, length2, linklist2, oplayer, ipLayer,
										provirtuallinklist, wprlist, nodepair, FSoneachLink, request, sharereglist, pt);// 此时在n点放置再生器
							} else {// 新建的再生器是纯OEO再生器
								Prolinkcapacitymodify(false, IPflow, length2, linklist2, oplayer, ipLayer,
										provirtuallinklist, wprlist, nodepair, FSoneachLink, request, sharereglist, pt);// 此时在n点放置再生器
							}
						}

						for (Link addlink : linklist2) {
							alllinklist.add(addlink);
						}
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {// 最后一段链路的RSA
					pt.setEndNode(finalRoute.getRoute().getNodelist().get(count));// 设置终止节点
					Prolinkcapacitymodify(true, IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist,
							wprlist, nodepair, FSoneachLink, request, sharereglist, pt);// 为目的节点前的剩余链路进行RSA
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
		System.out.println("保护链路最终路径上再生器节点的数量：" + finalRoute.getregnode().size());
		file_io.filewrite2(OutFileName, "保护最终路径上再生器节点的数量：" + finalRoute.getregnode().size());

		for (int i : finalRoute.getregnode()) {// 取出路径上所有再生器节点
			Node regnode = finalRoute.getRoute().getNodelist().get(i);// 判断再生器是共享来的还是新建的
			file_io.filewrite_without(OutFileName, regnode.getName() + " 节点上 放置了再生器");

			if (ShareReg.contains(i)) {// 该再生器是通过共享得到的
				for (Regenerator r : sharereglist) {
					if (r.getnode().equals(regnode)) {
						if (r.getNature() == 0)
							file_io.filewrite_without(OutFileName, "是通过共享得到的纯OEO再生器");
						else if (r.getNature() == 1)
							file_io.filewrite_without(OutFileName, "是通过共享得到的IP再生器");
						regthinglist.add(r);// 找出可共享的再生器 加入再生器集合
						hashregthinglist.put(t.nodeindexofroute(regnode, finalRoute.getRoute()), r); // 建立Hashmap!!!
						shareReg.add(r);// 加入针对于该链路的可共享再生器集合
					}
				}
			} else {// 表示不可以共享 此时要建立新的再生器 并且改变node上面再生器的个数
				regnode.setregnum(regnode.getregnum() + 1);
				int index = regnode.getregnum();
				Regenerator reg = new Regenerator(regnode);
				if (finalRoute.getIPRegnode().contains(i)) {
					reg.setNature(1);// 设置新建的再生器是IP再生器
					file_io.filewrite_without(OutFileName, "是新建的IP再生器");
				} else {
					reg.setNature(0); // 设置新建的再生器是OEO再生器
					file_io.filewrite_without(OutFileName, "是新建纯OEO再生器");
				}
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

	public boolean Prolinkcapacitymodify(Boolean IPorOEO, int IPflow, double routelength, ArrayList<Link> linklist,
			Layer oplayer, Layer iplayer, ArrayList<VirtualLink> provirtuallinklist,
			ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair, ArrayList<FSshareOnlink> FSoneachLink,
			Request request, ArrayList<Regenerator> sharereglist, ParameterTransfer pt) {
		// 建立虚拟链路 更改容量 RSA
		double X = 1;
		opProGrooming opg = new opProGrooming();
		int slotnum = 0, shareFS = 0;
		boolean opworkflag = false, shareFlag = true;
		Node srcnode = new Node(null, 0, null, iplayer, 0, 0);
		Node desnode = new Node(null, 0, null, iplayer, 0, 0);
		Test t = new Test();
		file_out_put file_io = new file_out_put();

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
		index_wave = opg.FSassignOnlink(linklist, wprlist, nodepair, slotnum, oplayer);// 在考虑共享的情况下分配频谱
																						// 但未实施占用

		for (Link link : linklist) {
			ArrayList<Integer> index_wave1 = new ArrayList<Integer>();
			length1 = length1 + link.getLength();
			cost = cost + link.getCost();
			ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
			link.setMaxslot(slotnum + link.getMaxslot());
			System.out.print("链路 " + link.getName() + "上分配的FS为 ");
			file_io.filewrite_without(OutFileName, "链路 " + link.getName() + "上分配的FS为 ");
			file_io.filewrite2(OutFileName, "");
			int m = index_wave.get(0);
			for (int n = 0; n < slotnum; n++) {
				index_wave1.add(m);
				System.out.print(m);
				file_io.filewrite_without(OutFileName, m + "  ");
				m++;
			}
			System.out.println();
			file_io.filewrite2(OutFileName, " ");
			FSshareOnlink fsonLink = new FSshareOnlink(link, index_wave1);
			FSoneachLink.add(fsonLink);
		}

		// 以上链路频谱分配完毕 下面开始建立IP层光路
		// 首先取出linklist里面的前两个链路和最后两个链路
		if (IPorOEO) {
			Node startnode = pt.getStartNode();
			Node endnode = pt.getEndNode();

			for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// 在IP层中寻找transparent链路的两端
				boolean srcflag = false, desflag = false;
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
			pt.setStartNode(desnode);
			int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的故这样设置index

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
				// Vlink.setlength(length1);
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
			file_io.filewrite2(OutFileName, "");
			if (findflag) {// 如果在IP层中已经找到该链路
				finlink.getVirtualLinkList().add(Vlink);
				System.out.println("IP层已存在的链路 " + finlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
				System.out.println(
						"保护链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： " + finlink.getVirtualLinkList().size());
				file_io.filewrite2(OutFileName,
						"IP层已存在的链路 " + finlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
				file_io.filewrite2(OutFileName,
						"保护链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： " + finlink.getVirtualLinkList().size());
			} else {
				createlink.getVirtualLinkList().add(Vlink);
				System.out.println("IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
				System.out.println("保护链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： "
						+ createlink.getVirtualLinkList().size());
				file_io.filewrite2(OutFileName,
						"IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
				file_io.filewrite2(OutFileName, "保护链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： "
						+ createlink.getVirtualLinkList().size());
			}
			pt.setMinRemainFlowRSA(10000);
		}
		return opworkflag;
	}

	public RouteAndRegPlace optionRouteSelect(ArrayList<RouteAndRegPlace> regplaceoption,ArrayList<WorkandProtectRoute> wprlist) {
		// 本算法的核心思想 这里还未改正
		// 通过更改该子算法调节不同的再生器选择 来达到网络性能的最优
		// 周一可以写一下final routeRSA + 不同再生器下建立IP链路 并且更改本算法
		RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 1);
		if (regplaceoption.size() == 1) {
			finalRoute = regplaceoption.get(0);
		} else if (regplaceoption.size() != 0) {
			ArrayList<RouteAndRegPlace> RemoveRoute = new ArrayList<>();

			for (int standard = 0; standard < regplaceoption.size() - 1; standard++) {// 标准
				RouteAndRegPlace StandardRoute = regplaceoption.get(standard);
				int StandardIP = 0, CompareIP = 0;
				if (RemoveRoute.contains(StandardRoute))
					continue;

				for (int k = standard + 1; k < regplaceoption.size(); k++) {// 比较
					RouteAndRegPlace CompareRoute = regplaceoption.get(k);
					if (RemoveRoute.contains(CompareRoute))
						continue;

					for (Regenerator shareReg : StandardRoute.getUsedShareReg()) {
						if (shareReg.getNature() == 1)
							StandardIP++;
					}
					System.out.print("第一层筛选 标准路由共享的IP再生器个数为 ：");
					System.out.println(StandardIP);
					for (Regenerator shareReg : CompareRoute.getUsedShareReg()) {
						if (shareReg.getNature() == 1)
							CompareIP++;
					}
					System.out.print("第一层筛选 比较路由共享的IP再生器个数为：");
					System.out.println(CompareIP);

					if (StandardRoute.getNewRegList().size() == 0) {// 再生器全是靠共享得到的
																	// 则此时优先选用IP再生器多的路径
						if (StandardIP < CompareIP) {
							RemoveRoute.add(StandardRoute);// 删去共享IP再生器少的路径
							break;
						}
						if (StandardIP > CompareIP) {
							RemoveRoute.add(CompareRoute);// 比较的没有标准好
						}
					} else {// 有新建的再生器 说明共享再生器全部使用 此时不需要判断共享再生器的选取
						// 在新建的再生器中使用OEO再生器多的路径
						if (StandardIP > CompareIP) {
							RemoveRoute.add(StandardRoute);// 删去新建IP再生器多的路径
							break;
						}
						if (StandardIP < CompareIP) {
							RemoveRoute.add(CompareRoute);// 比较的没有标准好
						}
					}
				}
			}
			for (RouteAndRegPlace rag : RemoveRoute) {
				regplaceoption.remove(rag);
			}
			RemoveRoute.clear();
			// 第二层比较
			if (regplaceoption.size() == 1) {
				finalRoute = regplaceoption.get(0);
			} else {
				for (int standard = 0; standard < regplaceoption.size() - 1; standard++) {
					RouteAndRegPlace StandardRoute_2 = regplaceoption.get(standard);
					if (RemoveRoute.contains(StandardRoute_2))
						continue;
					System.out.print("第二层筛选 标准路由剩余流量为：");
					System.out.println(StandardRoute_2.getNumRemainFlow());

					for (int k = standard + 1; k < regplaceoption.size(); k++) {
						RouteAndRegPlace CompareRoute_2 = regplaceoption.get(k);
						if (RemoveRoute.contains(CompareRoute_2))
							continue;
						System.out.print("第二层筛选 比较路由剩余流量为：");
						System.out.println(CompareRoute_2.getNumRemainFlow());
						if (StandardRoute_2.getNumRemainFlow() < CompareRoute_2.getNumRemainFlow()) {
							RemoveRoute.add(StandardRoute_2);// 删去剩余流量少的路由
							break;
						}
						if (StandardRoute_2.getNumRemainFlow() > CompareRoute_2.getNumRemainFlow()) {
							RemoveRoute.add(CompareRoute_2);// 比较的没有标准好
						}
					}
				}
				for (RouteAndRegPlace rag : RemoveRoute) {
					regplaceoption.remove(rag);
				}
				RemoveRoute.clear();

				// 第三层选择 选择新使用FS较少的路由
				if (regplaceoption.size() == 1) {
					finalRoute = regplaceoption.get(0);
				} else {
					for (int standard = 0; standard < regplaceoption.size() - 1; standard++) {
						RouteAndRegPlace StandardRoute_3 = regplaceoption.get(standard);
						if (RemoveRoute.contains(StandardRoute_3))
							continue;
						System.out.print("第三层筛选 标准路由使用的FS为：");
						System.out.println(StandardRoute_3.getnewFSnum());

						for (int k = standard + 1; k < regplaceoption.size(); k++) {
							RouteAndRegPlace CompareRoute_3 = regplaceoption.get(k);
							if (RemoveRoute.contains(CompareRoute_3))
								continue;
							System.out.print("第三层筛选 比较路由使用的FS为：");
							System.out.println(CompareRoute_3.getnewFSnum());
							if (StandardRoute_3.getnewFSnum() > CompareRoute_3.getnewFSnum()) {
								RemoveRoute.add(StandardRoute_3);// 删去使用FS较多的路由
								break;
							}
							if (StandardRoute_3.getnewFSnum() < CompareRoute_3.getnewFSnum()) {
								RemoveRoute.add(CompareRoute_3);// 比较的没有标准好
							}
						}
					}
					for (RouteAndRegPlace rag : RemoveRoute) {
						regplaceoption.remove(rag);
					}
					RemoveRoute.clear();
				}
				finalRoute = regplaceoption.get(0);// 最终不管是否只剩一条链路 都选择第一条作为最终链路

				// file_io.filewrite2(OutFileName, "！！！！！！此时的nodepair为"+nodepair.getName());
				// if(nodepair.getFinalRoute()!=null){
				// file_io.filewrite2(OutFileName, "！！！！！！ 该工作链路需要再生器");
				// }
			}
		}
		return finalRoute;
	}
}
