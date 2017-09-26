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
		
		// part1 �ҵ��ñ�����·�����Ѵ��ڵĹ���������
		for (WorkandProtectRoute wpr : wprlist) {// ���Ѵ��ڵ�ҵ���� �ҳ���ҵ�����Ѵ��ڵĹ���������
			ArrayList<Node> regnodelist = new ArrayList<>();
			regnodelist = wpr.getregnodelist(); // �Ѵ���ҵ���е�һ��ҵ�񱣻�·�����������ڵ�
			for (Node node : regnodelist) {
				if (newRoute.getNodelist().contains(node)) {// ���֮ǰ��ҵ����ĳһ�ڵ����Ѿ�������������
					// �жϸ�ҵ������ҵ��ɷ���������������ҵ��Ĺ�����·��Ӧ��������·�Ƿ񽻲棩
					for(WorkandProtectRoute nowwpr:wprlist){
						if(nowwpr.getdemand().equals(nodepair))
							nowdemand=nowwpr;
					}
					int cross = t.linklistcompare(nowdemand.getworklinklist(), wpr.getworklinklist());
					if (cross == 0) {
						haveRegRoute.add(wpr);// ������Թ����ҵ��
						int po = t.nodeindexofroute(node, newRoute);// ��������·�Ͽ��Թ������������λ��
						if (po != 0 && po != newRoute.getNodelist().size() - 1) {
							if (!ShareReg.contains(po))
								ShareReg.add(po); // �������µ�ҵ������Щ�ڵ�������������
						}
					}
				}
			}
		}
		// part1 finish �洢�����и���·�Ͽɹ�����������λ��
		boolean partworkflag = false, RSAflag = false, regflag = false, success = false, haveflag = false,
				passflag = false;
		double length = 0;
		ArrayList<Link> linklist = new ArrayList<>();

		int minRegNum = (int) Math.floor(routelength / 4000);
		int internode = newRoute.getNodelist().size() - 2, FStotal = 0, n = 0;
		// part2 ��·���Ϲ����������ĸ���С����������������С����ʱ ����set����RSA ����regplaceoption
		if (ShareReg.size() <= minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (partworkflag)
					break;
				passflag = false;
				Test nOfm = new Test(s, internode); // �������м�ڵ������ѡȡm����������������
				while (nOfm.hasNext()) {
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
						break;// ���еĹ��������� �Ѿ��ڶ��������в����Ŀ�������Ҫ������Щ������

					// �����������ڵ�֮�����RSA ����optionѡ���·��
					partworkflag = rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption);
				}
			}
		}

		// part3 ��·���Ϲ����������ĸ���������������������С����ʱ ����set����RSA����regplaceoption
		if (ShareReg.size() > minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (partworkflag)
					break;
				passflag = false;
				Test nOfm = new Test(s, internode); // �������м�ڵ������ѡȡm����������������
				while (nOfm.hasNext()) {
					int[] set = nOfm.next(); // �������������������λ��
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
					} // ������ҪΪ�˲���set
						// �����������ڵ�֮�����RSA
					partworkflag = rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption);
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
			rgp2.FinalRouteRSA(nodepair, finalRoute, oplayer, ipLayer, IPflow, wprlist, provirtuallinklist,ShareReg);
		}
		if (regplaceoption.size() == 0) {
			success = false;
		}
		System.out.println();
		if (success) {
			System.out.print("����·�����������óɹ�����RSA,���õ�����������Ϊ");
			for (WorkandProtectRoute wpr : wprlist) {
				if (wpr.getdemand().equals(nodepair)) {
					System.out.println(wpr.getregnodelist().size());
				}
			}

		} else
			System.out.println("����·���������������ɹ���·��������");
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
				System.out.println("�ҵ�����·���֣�" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// δ�������һ��·����RSA
					// if (count != finalRoute.getregnode().get(i)) {
					// if (count == finalRoute.getRoute().getNodelist().size() -
					// 1) {
					// Prolinkcapacitymodify(IPflow, length2, linklist2,
					// oplayer, ipLayer);// ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
					// for(Link addlink:linklist2){
					// alllinklist.add(addlink);
					// }
					// linklist2.clear();
					// }
					// }
					if (count == finalRoute.getregnode().get(i)) {
						Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist);// ��ʱ��n�����������
						for (Link addlink : linklist2) {
							alllinklist.add(addlink);
						}
						Node node = finalRoute.getRoute().getNodelist().get(count);
						if(ShareReg.contains(count)){//��ʾ�ýڵ��ϵ����������Թ���
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
					Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist);// ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
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
			System.out.println("��·�����޷�RSA");
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

			System.out.println("����·����slot���� " + slotnum);
			newFS = slotnum;
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa = new Mymain();
			index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			if (index_wave.size() == 0) {
				System.out.println("·������ ��������Ƶ����Դ");
			} else {
				opworkflag = true;
				double length1 = 0;
				double cost = 0;
				for (Link link : linklist) {// ������link
					length1 = length1 + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
					link.setMaxslot(slotnum + link.getMaxslot());
					// System.out.println("��· " + link.getName() + "�����slot�ǣ� "
					// + link.getMaxslot()+" ����Ƶ�״�����
					// "+link.getSlotsindex().size());
				}

				for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// ��IP����Ѱ��transparent��·������
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
				int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ�Ĺ���������index

				Link finlink = iplayer.findLink(srcnode, desnode);
				Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
				boolean findflag = false;
				try {
					System.out.println(finlink.getName());
					findflag = true;
				} catch (java.lang.NullPointerException ex) {
					System.out.println("IP ��û�и���·��Ҫ�½���·");
					createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
					iplayer.addLink(createlink);
				}

				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
				Vlink.setnature(1);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
				Vlink.setFullcapacity(slotnum * X);// �������flow�Ǵ����������
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setlength(length1);
				Vlink.setcost(cost);
				Vlink.setPhysicallink(linklist);
				provirtuallinklist.add(Vlink);
				if (findflag) {// �����IP�����Ѿ��ҵ�����·
					// System.out.println(finlink.getVirtualLinkList().size());
					finlink.getVirtualLinkList().add(Vlink);
					// System.out.println(finlink.getVirtualLinkList().size());
					System.out.println(
							"IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					System.out.println("������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ "
							+ finlink.getVirtualLinkList().size());
				} else {
					createlink.getVirtualLinkList().add(Vlink);
					// System.out.println(createlink.getVirtualLinkList().size());
					System.out.println("IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					System.out.println("������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ "
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
		// ��һ�ֱȽϣ��Ƚ�ÿ����·��ʹ�õ�FS���� ѡȡ���ٵ�//����������ʹ�õ�FS��û�п��ǹ����FS
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
		// �ڶ���Ƚ� �Ƚ�������Ϊ��������·���ṩ�˱��� ѡ����
		int max = 0;
		for (RouteAndRegPlace route : regplaceoption) {// ����ȡ����ҵ���һ����ѡ·��
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

	public boolean RSAunderSet(int[] set, LinearRoute newRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<RouteAndRegPlace> regplaceoption) {
		boolean partworkflag = false, RSAflag = false, regflag = false;
		double length = 0;
		ArrayList<Link> linklist = new ArrayList<>();
		int FStotal = 0, n = 0;
		RegeneratorPlace rp = new RegeneratorPlace();

		for (int i = 0; i < set.length + 1; i++) {// RSA�Ĵ������������ĸ�����1
			if (!partworkflag && RSAflag || regplaceoption.size() != 0)
				break;
			if (i < set.length)
				System.out.println("****************��������λ��Ϊ��" + set[i]); // set�������Ӧ���ǽڵ��λ��+1��
			else {
				System.out.println("************���һ�����������ս��֮���RSA ");
				regflag = true;
			}
			do {// ͨ��һ��
				Node nodeA = newRoute.getNodelist().get(n);
				Node nodeB = newRoute.getNodelist().get(n + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println(link.getName());
				length = length + link.getLength();
				linklist.add(link);
				n = n + 1;
				if (!regflag) {// δ�������һ��·����RSA
					if (n != set[i]) {
						if (n == newRoute.getNodelist().size() - 1) {
							partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer);// ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
							FStotal = FStotal + newFS;
						}
					}
					if (n == set[i]) {
						partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer);// ��ʱ��n�����������
						FStotal = FStotal + newFS;
						length = 0;
						RSAflag = true;
						linklist.clear();
						break;
					}
				}
				if (n == newRoute.getNodelist().size() - 1) {
					partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer);// ��ʱ��n�����������
					FStotal = FStotal + newFS;
				}
				if (!partworkflag && RSAflag)
					break;
			} while (n != newRoute.getNodelist().size() - 1);
			// ���·�ɳɹ��򱣴��·�ɶ����������ķ���
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
			System.out.println("��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());// �������ĸ����ӽ�ȥ
		}
		return partworkflag;

	}
}
