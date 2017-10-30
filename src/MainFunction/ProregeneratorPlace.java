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
	 
		// part1 �ҵ��ñ�����·�����Ѵ��ڵĹ���������
		for (WorkandProtectRoute nowwpr : wprlist) {
			if (nowwpr.getdemand().equals(nodepair))
				nowdemand = nowwpr;
		}
		for (WorkandProtectRoute wpr : wprlist) {// ���Ѵ��ڵ�ҵ���� �ҳ���ҵ�����Ѵ��ڵĹ���������
		
			for (Regenerator newreg : wpr.getnewreglist()) {// ֻ������·����û���½���������
				Node node = newreg.getnode();
				if (newRoute.getNodelist().contains(node)) {// ���֮ǰ��ҵ����ĳһ�ڵ����Ѿ�������������

					// �жϸ�ҵ������ҵ��ɷ���������������ҵ��Ĺ�����·��Ӧ��������·�Ƿ񽻲棩
					int already = 0, newregg = 0;
					int cross = t.linklistcompare(nowdemand.getworklinklist(), wpr.getworklinklist());
					if (cross == 0) {//�����ж�������²������������Ƿ���Թ���
						//fix
						boolean noshareFlag=false;
						for (WorkandProtectRoute comwpr : wprlist) {
							if(wpr.getdemand().equals(comwpr.getdemand())) continue;
							for (Regenerator haveshareReg : comwpr.getsharereglist()) {
								if(haveshareReg.equals(newreg)){//����ҵ�������������������
									file_io.filewrite2(OutFileName, "����ҵ��"+comwpr.getdemand().getName()+"�����������,"+
								haveshareReg.getnode().getName()+"�ϵĵ�"+haveshareReg.getindex()+"��������");
								 
								int cross_second = t.linklistcompare(nowdemand.getworklinklist(), comwpr.getworklinklist());
								if(cross_second==1){
									noshareFlag=true;
									break;
								}
								}
							}
						}
						if(!noshareFlag){//��ʾ����������ҵ����Ҳ���Թ���
							int po = t.nodeindexofroute(node, newRoute);// ��������·�Ͽ��Թ������������λ��
							if (po != 0 && po != newRoute.getNodelist().size() - 1) {// �ж�����·���Ѵ��ڵ��������Ƿ�����·������
								if (comnodelist.contains(node)) {// ˵���ýڵ����Ѵ��ڿɹ����������  ��ʱ��Ҫѡ�����ĸ�����������
									for (Regenerator alreadyReg : sharereglist) {
										if (alreadyReg.getnode().equals(node)) {// ��ʱalreadyReg��ʾ�����б����Ѵ��ڵ�reg
											for (WorkandProtectRoute comwpr : wprlist) {// һ�±Ƚ��ĸ�������ʹ�õĶ�
												if (comwpr.getRegeneratorlist().contains(alreadyReg)) {
													already++;
												}
												if (comwpr.getRegeneratorlist().contains(newreg)) {
													newregg++;
												}
											}
										}
										if (already < newregg) {// ˵�������ӵ�reg����ı�����·�Ƚ϶�
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
									
								} else {// �²�����������
									comnodelist.add(node);
									sharereglist.add(newreg);
								}
								// System.out.println("�������ĸ�����"+sharereglist.size());
								// for(Regenerator reg:sharereglist){
								// System.out.println(reg.getnode().getName());
								// }
								if (!ShareReg.contains(po))
									ShareReg.add(po); // �������µ�ҵ������Щ�ڵ�������������
							}
						}
					}
				}
			}
		}
		// part1 finish �洢�����и���·�Ͽɹ�����������λ��
		boolean success = false, passflag = false;
		int minRegNum = (int) Math.floor(routelength / 4000);
		int internode = newRoute.getNodelist().size() - 2;
		// part2 ��·���Ϲ����������ĸ���С����������������С����ʱ ����set����RSA ����regplaceoption
		System.out.println();
		file_io.filewrite2(OutFileName, "");
		System.out.println("�ɹ����������ĸ�����" + ShareReg.size() + "��Ҫ������������������" + minRegNum);
		file_io.filewrite2(OutFileName, "�ɹ����������ĸ�����" + ShareReg.size() + "��Ҫ������������������" + minRegNum);
		file_io.filewrite_without(OutFileName, "�ɹ�����������λ�ã�");
		for (int a : ShareReg)
			file_io.filewrite_without(OutFileName, a + "  ");
		    file_io.filewrite2(OutFileName, "   ");

		if (ShareReg.size() <= minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (regplaceoption.size() != 0)
					break;
				Test nOfm = new Test(s, internode); // �������м�ڵ������ѡȡm����������������
				while (nOfm.hasNext()) {
					passflag = false;
					int[] set = nOfm.next(); // �������������������λ��
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
						continue;// ���еĹ��������� �Ѿ��ڶ��������в����Ŀ�������Ҫ������Щ������

					// �����������ڵ�֮�����RSA ����optionѡ���·��
					rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption, wprlist, nodepair);
				}
			}
		}

		// part3 ��·���Ϲ����������ĸ���������������������С����ʱ ����set����RSA����regplaceoption
		if (ShareReg.size() > minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (regplaceoption.size() != 0)
					break;
				Test nOfm = new Test(s, internode); // �������м�ڵ������ѡȡm����������������
				while (nOfm.hasNext()) {
					passflag = false;
					int[] set = nOfm.next(); // �������������������λ��
					if (s <= ShareReg.size()) { // ��ʱ����������ӿɹ��������������ѡ��
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
					} // ������ҪΪ�˲���set
						// �����������ڵ�֮�����RSA
					rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption, wprlist, nodepair);
				}
			}
		}
		// part4 �Բ����ı�ѡ��·����ɸѡ���Ҷ�ѡ����·����IP��·
		if (regplaceoption.size() > 0) {
			success = true;
			RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 1);
			if (regplaceoption.size() > 1)
				finalRoute = rgp2.optionRouteSelect(regplaceoption, wprlist);// �ڷ��������ļ���·����ѡȡ��ѵ�·����Ϊfinaroute
			else
				finalRoute = regplaceoption.get(0);
			// �������Ը�������·����RSA
			rgp2.FinalRouteRSA(nodepair, finalRoute, oplayer, ipLayer, IPflow, wprlist, provirtuallinklist, ShareReg,sharereglist,request);
			// ����finalroute�����������ڵ�洢����
		}
		if (regplaceoption.size() == 0) {
			success = false;
		}
		System.out.println();
		file_io.filewrite2(OutFileName, "");
		if (success) {
			System.out.print("����·�����������óɹ�����RSA,���õ�����������Ϊ");
			file_io.filewrite_without(OutFileName, "����·�����������óɹ�����RSA,���õ�����������Ϊ");
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					wpr.setrequest(request);
					System.out.println(wpr.getRegeneratorlist().size());
					file_io.filewrite(OutFileName, wpr.getRegeneratorlist().size());
				}
			}

		} else {
			System.out.println("����·���������������ɹ���·��������");
			file_io.filewrite2(OutFileName, "����·���������������ɹ���·��������");
		}
		return success;
	}// ����������

	public void RSAunderSet(int[] set, LinearRoute newRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<RouteAndRegPlace> regplaceoption, ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair) {
		boolean partworkflag = false, RSAflag = false, regflag = false;
		double length = 0;
		file_out_put file_io = new file_out_put();
		ArrayList<Link> linklist = new ArrayList<>();
		int FStotal = 0, n = 0;
		ProregeneratorPlace rp = new ProregeneratorPlace();
		ArrayList<Float> RemainRatio=new ArrayList<>();//��¼ÿ����·��ʣ���flow
		
		for (int i = 0; i < set.length + 1; i++) {// RSA�Ĵ������������ĸ�����1
			if (!partworkflag && RSAflag)
				break;
			if (i < set.length) {
				System.out.println("****************��������λ��Ϊ��" + set[i]); // set�������Ӧ���ǽڵ��λ��+1��
				file_io.filewrite2(OutFileName, "****************��������λ��Ϊ��" + set[i]);
			} else {
				System.out.println("************���һ�����������ս��֮���RSA ");
				file_io.filewrite2(OutFileName, "************���һ�����������ս��֮���RSA ");
				regflag = true;
			}
			do {// ͨ��һ��
				Node nodeA = newRoute.getNodelist().get(n);
				Node nodeB = newRoute.getNodelist().get(n + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println(link.getName());
				file_io.filewrite2(OutFileName, link.getName());
				length = length + link.getLength();
				linklist.add(link);
				n = n + 1;
				if (!regflag) {// δ�������һ��·����RSA
					if (n == set[i]) {
						float remainFlow=0;
						partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair,remainFlow);// ��ʱ��n�����������
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
					partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair,remainFlow);// ��ʱ��n�����������
					RemainRatio.add(remainFlow);
					FStotal = FStotal + nodepair.getSlotsnum();
				}
				if (!partworkflag && RSAflag)// ���֮ǰ����·�Ѿ�RSAʧ�� ʣ�µ���·Ҳû��RSA�ı�Ҫ
					break;
			} while (n != newRoute.getNodelist().size() - 1);
			// ���·�ɳɹ��򱣴��·�ɶ����������ķ���
		}
		if (partworkflag) {
			RouteAndRegPlace rarp = new RouteAndRegPlace(newRoute, 1);
			rarp.setnewFSnum(FStotal);
			ArrayList<Integer> setarray = new ArrayList<>();
			ArrayList<Integer> IPRegarray = new ArrayList<>();
			for (int k = 0; k < set.length; k++) {
				setarray.add(set[k]);
				if(RemainRatio.get(k)>0.1||RemainRatio.get(k+1)>0.1){// ֻҪ������ǰ����ߺ�����һ��δ���ʹ�������IP������
					IPRegarray.add(set[k]);//�洢IP���������ýڵ�
				}
			}
			rarp.setregnode(setarray);
			rarp.setregnum(setarray.size());
			regplaceoption.add(rarp);
			System.out.println("��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());// �������ĸ����ӽ�ȥ
			file_io.filewrite2(OutFileName, "��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());
		}
	}

	public Boolean vertify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer, Layer iplayer,
			ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair,double RemainRatio) {
		// �ж�ĳһ��transparent��·�Ƿ��ܹ��ɹ�RSA ���Ҽ�¼��ʹ�õ�FS����
		// workOrproflag=true��ʱ���ʾ�ǹ��� false��ʱ���ʾ����
		file_out_put file_io = new file_out_put();
		nodepair.setSlotsnum(0);
		double X = 1;
		opProGrooming opg = new opProGrooming();
		int slotnum = 0;
		boolean opworkflag = false;
		if (routelength > 4000) {
			System.out.println("��·�����޷�RSA");
			file_io.filewrite2(OutFileName, "��·�����޷�RSA");
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
			slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��
			System.out.println("ÿ����·�����slot��Ϊ�� " + slotnum);
			file_io.filewrite2(OutFileName, "ÿ����·�����slot��Ϊ�� " + slotnum);
			
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			index_wave = opg.FSassignOnlink(linklist, wprlist, nodepair, slotnum,oplayer);// �ڿ��ǹ��������·���Ƶ��
			if (index_wave.size() != 0) {
				opworkflag = true;
				System.out.println("����RSA,startΪ "+index_wave.get(0));
				file_io.filewrite2(OutFileName, "����RSA,startΪ "+index_wave.get(0));
			} else {
				System.out.println("Ƶ�ײ����޷�RSA");
				file_io.filewrite2(OutFileName, "Ƶ�ײ����޷�RSA");
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
		System.out.println("������·������RSA��" );
		file_io.filewrite2(OutFileName, "������·������RSA��" );
		
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
				System.out.println("����·����RSA����·��" + link.getName());
				file_io.filewrite2(OutFileName, "����·����RSA����·��" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// δ�������һ��·����RSA
					if (count == finalRoute.getregnode().get(i)) {
						Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist, wprlist,nodepair, FSoneachLink,request);// ��ʱ��n�����������
						for (Link addlink : linklist2) {
							alllinklist.add(addlink);
						}
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {
					Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist, wprlist,nodepair, FSoneachLink,request);// ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
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
		System.out.println("��������������·�����������ڵ��������" + finalRoute.getregnode().size());
		file_io.filewrite2(OutFileName, "��������������·�����������ڵ��������" + finalRoute.getregnode().size());

		for (int i : finalRoute.getregnode()) {// ȡ��·���������������ڵ�
			Node regnode = finalRoute.getRoute().getNodelist().get(i);// ���ǿɹ���Ͳ��ɹ���
			if (ShareReg.contains(i)) {// �����������Թ���
				for (Regenerator r : sharereglist) {
					if (r.getnode().equals(regnode)) {
						regthinglist.add(r);// �ҳ��ɹ���������� ��������������
						hashregthinglist.put(t.nodeindexofroute(regnode, finalRoute.getRoute()), r); // ����Hashmap!!!
						shareReg.add(r);// ��������ڸ���·�Ŀɹ�������������
					}
				}
			} else {// ��ʾ�����Թ��� ��ʱҪ�����µ������� ���Ҹı�node�����������ĸ���
				regnode.setregnum(regnode.getregnum() + 1);
				int index = regnode.getregnum();
				Regenerator reg = new Regenerator(regnode);
				reg.setindex(index);
				regthinglist.add(reg);
				hashregthinglist.put(t.nodeindexofroute(regnode, finalRoute.getRoute()), reg); // ����Hashmap!!!
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
		// ����������· �������� RSA
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
		slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��

		opworkflag = true;
		double length1 = 0;
		double cost = 0;
		ArrayList<Integer> index_wave = new ArrayList<Integer>();
		index_wave = opg.FSassignOnlink(linklist, wprlist, nodepair, slotnum,oplayer);// �ڿ��ǹ��������·���Ƶ��

		
		for (Link link : linklist) {
			ArrayList<Integer> index_wave1=new ArrayList<Integer>();
			length1 = length1 + link.getLength();
			cost = cost + link.getCost();
			ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
			link.setMaxslot(slotnum + link.getMaxslot());
			file_io.filewrite_without(OutFileName, "!!!requestΪ "+request.getNodepair().getName());
			System.out.print("�����·�Ϸ����FSΪ ");
			file_io.filewrite_without(OutFileName, "�����·�Ϸ����FSΪ "+"   index��ʼ"+index_wave.get(0)+"  "+slotnum);
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
		
		// ����ȡ��linklist�����ǰ������·�����������·
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
			file_io.filewrite2(OutFileName, "ȡ������·Ϊ" + link1.getName() + "  " + link2.getName() + "   "
					+ link3.getName() + "  " + link4.getName());

			if (link2.getNodeA().equals(nodeA) || link2.getNodeB().equals(nodeA))
				startnode = nodeB;
			if (link2.getNodeA().equals(nodeB) || link2.getNodeB().equals(nodeB))
				startnode = nodeA;// �ҵ���ʼ�˵�
			if (link3.getNodeA().equals(nodeC) || link3.getNodeB().equals(nodeC))
				endnode = nodeD;
			if (link3.getNodeA().equals(nodeD) || link3.getNodeB().equals(nodeD))
				endnode = nodeC;// �ҵ���ֹ�˵�
			file_io.filewrite2(OutFileName, "�ҵ��Ľڵ㣺" + startnode.getName() + "  " + endnode.getName());
		}
		if (linklist.size() == 1) {
			startnode = linklist.get(0).getNodeA();
			endnode = linklist.get(0).getNodeB();
		}

		for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// ��IP����Ѱ��transparent��·������
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

		int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ�Ĺ���������index

		// file_io.filewrite2(OutFileName,"src�Ľڵ��:"+srcnode.getIndex()+"
		// des�ڵ�ȣ�"+desnode.getIndex());
		if (srcnode.getIndex() > desnode.getIndex()) {
			Node internode = srcnode;
			srcnode = desnode;
			desnode = internode;
		}
		String name = srcnode.getName() + "-" + desnode.getName();
		// file_io.filewrite2(OutFileName,"��ʱ��ԭ�ڵ�Ϊ:"+srcnode.getName()+"
		// �ս��Ϊ"+desnode.getName());
		Link finlink = iplayer.findLink(srcnode, desnode);
		Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
		boolean findflag = false;
		try {
			System.out.println(finlink.getName());
			file_io.filewrite2(OutFileName, finlink.getName());
			findflag = true;
		} catch (java.lang.NullPointerException ex) {
			System.out.println("IP ��û�и���·��Ҫ�½���·");
			file_io.filewrite2(OutFileName, "IP ��û�и���·��Ҫ�½���·");
			file_io.filewrite2(OutFileName, "��ʱ��ԭ�ڵ�Ϊ:" + srcnode.getName() + "  �ս��Ϊ" + desnode.getName());
			createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
			iplayer.addLink(createlink);
		}

		VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
		if (!shareFlag || shareFS <= slotnum) {// ��ʾ��linklist������·���ܹ���FS���߾����Թ���ʱ�����FSС����Ҫ��FS
			Vlink.setnature(1);
			Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
			Vlink.setFullcapacity(slotnum * X);// �������flow�Ǵ����������
			Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
			Vlink.setlength(length1);
			Vlink.setcost(cost);
			Vlink.setPhysicallink(linklist);
			provirtuallinklist.add(Vlink);
		}
		if (shareFS > slotnum) {// ��ʾ��linklist������·���ܹ���FS���߾����Թ���ʱ�����FSС����Ҫ��FS
			Vlink.setnature(1);
			Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
			Vlink.setFullcapacity(shareFS * X);// �������flow�Ǵ����������
			Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
			Vlink.setlength(length1);
			Vlink.setcost(cost);
			Vlink.setPhysicallink(linklist);
			provirtuallinklist.add(Vlink);
		}

		if (findflag) {// �����IP�����Ѿ��ҵ�����·
			finlink.getVirtualLinkList().add(Vlink);
			System.out.println("IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
			System.out.println(
					"������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ " + finlink.getVirtualLinkList().size());
			file_io.filewrite2(OutFileName,
					"IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
			file_io.filewrite2(OutFileName,
					"������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ " + finlink.getVirtualLinkList().size());
		} else {
			createlink.getVirtualLinkList().add(Vlink);
			System.out.println("IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
			System.out.println(
					"������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ " + createlink.getVirtualLinkList().size());
			file_io.filewrite2(OutFileName,
					"IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
			file_io.filewrite2(OutFileName,
					"������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ " + createlink.getVirtualLinkList().size());
		}

		return opworkflag;
	}

	public RouteAndRegPlace optionRouteSelect(ArrayList<RouteAndRegPlace> regplaceoption,
			ArrayList<WorkandProtectRoute> wprlist) {
		RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 1);
		int FS = 10000;
		ArrayList<RouteAndRegPlace> RemoveRoute = new ArrayList<>();
		// ��һ�ֱȽϣ��Ƚ�ÿ����·��ʹ�õ�FS���� ѡȡ���ٵ�
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
		// �ڶ���Ƚ� �Ƚ�������Ϊ��������·���ṩ�˱��� ѡ����
		int max = 0;
		for (RouteAndRegPlace route : regplaceoption) {// ����ȡ����ҵ���һ����ѡ·��
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
				finalRoute = route;// ����Ӧ�÷������һ��Ƚ�
			}
			if (max > share)
				RemoveRoute.add(route);
		}
		// for(RouteAndRegPlace route: RemoveRoute){
		// regplaceoption.remove(route);
		// }
		// ������Ƚ�δ�����

		return finalRoute;
	}

}
