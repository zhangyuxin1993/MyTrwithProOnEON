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

	static int totalregNum = 0;

	public boolean proregeneratorplace(NodePair nodepair, LinearRoute newRoute, ArrayList<WorkandProtectRoute> wprlist,
			double routelength, Layer oplayer, Layer ipLayer, int IPflow) {
		WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);
		ArrayList<VirtualLink> provirtuallinklist = new ArrayList<>();
		ProregeneratorPlace rgp2 = new ProregeneratorPlace();
		Test t = new Test();
		ArrayList<Integer> ShareReg = new ArrayList<>();
		ArrayList<Node> comnodelist = new ArrayList<>();
		ArrayList<Regenerator> sharereglist = new ArrayList<>();
		ArrayList<Regenerator> removereglist=new ArrayList<>();
		ArrayList<Regenerator> addreglist=new ArrayList<>();
		ArrayList<RouteAndRegPlace> regplaceoption = new ArrayList<>();
		ProregeneratorPlace rgp = new ProregeneratorPlace();
		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		
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
					if (cross == 0) {
						int po = t.nodeindexofroute(node, newRoute);// ��������·�Ͽ��Թ������������λ��
						if (po != 0 && po != newRoute.getNodelist().size() - 1) {// �ж�����·���Ѵ��ڵ��������Ƿ�����·������
							if (comnodelist.contains(node)) {// ˵���ýڵ����Ѵ��ڿɹ����������
																// ��ʱ��Ҫѡ�����ĸ�����������
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
//										sharereglist.remove(alreadyReg);
//										sharereglist.add(newreg);
									}
								}
								for(Regenerator remoReg:removereglist){
									sharereglist.remove(remoReg);
								}
								for(Regenerator addReg:addreglist){
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
		// part1 finish �洢�����и���·�Ͽɹ�����������λ��
		boolean partworkflag = false, success = false, passflag = false;
		int minRegNum = (int) Math.floor(routelength / 4000);
		int internode = newRoute.getNodelist().size() - 2;
		// part2 ��·���Ϲ����������ĸ���С����������������С����ʱ ����set����RSA ����regplaceoption
		System.out.println();
		file_io.filewrite2(OutFileName, "");
		System.out.println("�ɹ����������ĸ�����" + ShareReg.size() + "��Ҫ������������������" + minRegNum);
		file_io.filewrite2(OutFileName, "�ɹ����������ĸ�����" + ShareReg.size() + "��Ҫ������������������" + minRegNum);
		if (ShareReg.size() <= minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (regplaceoption.size()!=0)
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
					partworkflag = rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption, wprlist,nodepair);
				}
			}
		}

		// part3 ��·���Ϲ����������ĸ���������������������С����ʱ ����set����RSA����regplaceoption
		if (ShareReg.size() > minRegNum) {
			for (int s = minRegNum; s <= internode; s++) {
				if (regplaceoption.size()!=0)
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
					partworkflag = rgp.RSAunderSet(set, newRoute, oplayer, ipLayer, IPflow, regplaceoption, wprlist,
							nodepair);
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
			rgp2.FinalRouteRSA(nodepair, finalRoute, oplayer, ipLayer, IPflow, wprlist, provirtuallinklist, ShareReg,sharereglist);
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
					System.out.println(wpr.getRegeneratorlist().size());
					file_io.filewrite(OutFileName, wpr.getRegeneratorlist().size());
				}
			}

		} else{
			System.out.println("����·���������������ɹ���·��������");
			file_io.filewrite2(OutFileName, "����·���������������ɹ���·��������");
		}
		return success;

	}

	public Boolean vertify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer, Layer iplayer,
			boolean workOrproflag, ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair) {
		// �ж�ĳһ��transparent��·�Ƿ��ܹ��ɹ�RSA ���Ҽ�¼��ʹ�õ�FS����
		// workOrproflag=true��ʱ���ʾ�ǹ��� false��ʱ���ʾ����
		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		nodepair.setSlotsnum(0);
		double X = 1;
		Mymain spa = new Mymain();
		int slotnum = 0, newFS = 0;
		boolean opworkflag = false, partworkflag=false;
		ArrayList<Link> removeLinklist=new ArrayList<>();
		
		if (routelength > 4000) {
			System.out.println("��·�����޷�RSA");
			file_io.filewrite2(OutFileName,"��·�����޷�RSA");
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
			// System.out.println("����·����slot���� " + slotnum);

			WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);
			Test t = new Test();

			newFS = slotnum * linklist.size();
			if (!workOrproflag) {// ��ʱ����������������·
				for (WorkandProtectRoute wpr : wprlist) {
					if (wpr.getdemand().equals(nodepair)) {
						nowdemand = wpr;
						continue;
					}
				}
				HashMap<Link, Integer> FSuseOnlink = new HashMap<>();
				
				for (Link nowlink : linklist) {
					int max=0;
					for (WorkandProtectRoute wpr : wprlist) {
						if (wpr.getdemand().equals(nodepair)) {
							continue;
						}
						if (wpr.getprolinklist().contains(nowlink)) {
							int cross = t.linklistcompare(wpr.getworklinklist(), nowdemand.getworklinklist());
							if (cross == 0) {// ����������·�ཻ��ʾ����·��FS�����Թ���
								FSuseOnlink = wpr.getFSuseOnlink();
								int useFS = FSuseOnlink.get(nowlink);//�ҳ�֮ǰҵ���ϸ���·�Ѿ�ʹ�õ�FS
								System.out.println("�ɹ�����·Ϊ�� "+nowlink.getName());
								file_io.filewrite2(OutFileName,"�ɹ�����·Ϊ�� "+nowlink.getName());
								if(useFS>max){//�ҳ�֮ǰ���пɹ���ҵ���� ʹ�õ����FS
									max=useFS;
								}
							}
						}
					}//�Ƚ������Ѿ�������ҵ���Ƿ���п��Թ����FS���ұ�����Թ���FS�����ֵ
					
					  partworkflag=false;
					  if(max!=0){//˵�����ڿ��Թ����FS
			
					if(max<slotnum){//ֻ��һ����FS���Թ��� ����Ҫ�µ�FS����RSA
						ArrayList<Link> newlinklist=new ArrayList<>();
						ArrayList<Integer> index_wave1 = new ArrayList<Integer>();
						removeLinklist.add(nowlink);
						newlinklist.add(nowlink);
						index_wave1 = spa.spectrumallocationOneRoute(false, null, newlinklist, slotnum-max);//��Ϊ��Щ��·�����п��Թ����FS���Է����FS��ͬ
						if (index_wave1.size() == 0) {
							System.out.println("·��"+nowlink.getName()+" ���� ���޷�Ƶ����Դ");
							file_io.filewrite2(OutFileName,"·��"+nowlink.getName()+" ���� ���޷�Ƶ����Դ");
						} else {
							partworkflag = true;
							System.out.println("���Խ���RSA");
							file_io.filewrite2(OutFileName,"���Խ���RSA");
						}
					}
					if(max>=slotnum){//�Ѵ��ڵ�FS������Ҫ��FS����ҵ���ϵ�FSȫ�����Թ���
						max=slotnum;
						removeLinklist.add(nowlink);
						partworkflag=true;
					}
						newFS = newFS - max; // ���ĳһ����·�ϵ�Ƶ�׿��Թ���
				if(!partworkflag) 
					break;
				}
					  else 
						  partworkflag=true;
				}
			}
			for(Link link:removeLinklist){
				linklist.remove(link);
			}
			if(linklist.size()==0){
				 System.out.println("��Ҫ����FS��Ϊ��" + newFS);
				 file_io.filewrite2(OutFileName,"��Ҫ����FS��Ϊ��" + newFS);
				 nodepair.setSlotsnum(newFS);
				 opworkflag = true;
				 System.out.println("���Խ���RSA");
				 file_io.filewrite2(OutFileName,"���Խ���RSA");
			}
		 if(partworkflag&&linklist.size()!=0){//������Թ������·RSA���
			 System.out.println("��Ҫ����FS��Ϊ��" + newFS);
			 file_io.filewrite2(OutFileName,"��Ҫ����FS��Ϊ��" + newFS);
			 nodepair.setSlotsnum(newFS);
			 ArrayList<Integer> index_wave = new ArrayList<Integer>();
			 index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			 if (index_wave.size() == 0) {
				 System.out.println("·������ ��������Ƶ����Դ");
				 file_io.filewrite2(OutFileName,"·������ ��������Ƶ����Դ");
			 } else {
				 opworkflag = true;
				 System.out.println("���Խ���RSA");
				 file_io.filewrite2(OutFileName,"���Խ���RSA");
			 }
		 }
		}
		return opworkflag;
	}

	public void FinalRouteRSA(NodePair nodepair, RouteAndRegPlace finalRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<WorkandProtectRoute> wprlist, ArrayList<VirtualLink> provirtuallinklist,ArrayList<Integer> ShareReg, ArrayList<Regenerator> sharereglist) {
		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		ArrayList<Link> alllinklist = new ArrayList<>();
		ArrayList<Regenerator> regthinglist = new ArrayList<>();
		Test t = new Test();

		finalRoute.getRoute().OutputRoute_node(finalRoute.getRoute());
		int count = 0;
		double length2 = 0;
		boolean regflag2 = false;
		ArrayList<Link> linklist2 = new ArrayList<>();
		HashMap<Link, Integer> setFSuseOnlink = new HashMap<>();
		
		for (int i = 0; i < finalRoute.getregnum() + 1; i++) {
			if (i >= finalRoute.getregnum())
				regflag2 = true;
			do {
				Node nodeA = finalRoute.getRoute().getNodelist().get(count);
				Node nodeB = finalRoute.getRoute().getNodelist().get(count + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println("�ҵ�����·���֣�" + link.getName());
				file_io.filewrite2(OutFileName,"�ҵ�����·���֣�" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// δ�������һ��·����RSA
					if (count == finalRoute.getregnode().get(i)) {
						Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist, wprlist, nodepair, setFSuseOnlink);// ��ʱ��n�����������
						for (Link addlink : linklist2) {
							alllinklist.add(addlink);
						}
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {
					Prolinkcapacitymodify(IPflow, length2, linklist2, oplayer, ipLayer, provirtuallinklist, wprlist, nodepair,setFSuseOnlink);// ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
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
		file_io.filewrite2(OutFileName,"��������������·�����������ڵ��������" + finalRoute.getregnode().size());
		
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
				wpr.setFSuseOnlink(setFSuseOnlink);
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
			NodePair nodepair,HashMap<Link, Integer> setFSuseOnlink) {
		double X = 1;
		int slotnum = 0;
		boolean opworkflag = false;
		Mymain spa = new Mymain();
		ArrayList<Link> removeLinklist=new ArrayList<>();
		HashMap<Link, Integer> FSuseOnlink = new HashMap<>();
		Node srcnode = new Node(null, 0, null, iplayer, 0, 0);
		Node desnode = new Node(null, 0, null, iplayer, 0, 0);
		Test t = new Test();
		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
//		if (routelength > 4000) {
//			System.out.println("��·�����޷�RSA");
//		}
//		if (routelength < 4000) {
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

			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Request request = null;
				opworkflag = true;
				double length1 = 0;
				double cost = 0;
				
				WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);

				for (WorkandProtectRoute wpr : wprlist) {
					if (wpr.getdemand().equals(nodepair)) {
						nowdemand = wpr;
						continue;
					}
				}
				
				for (Link link : linklist) {// Ϊ�����link�ı���FS��
					int max=0;
					length1 = length1 + link.getLength();
					cost = cost + link.getCost();
				
					ArrayList<Link> linklistOnly=new ArrayList<>();
					for (WorkandProtectRoute wpr : wprlist) {
						if (wpr.getdemand().equals(nodepair)) {
							continue;
						}
						if (wpr.getprolinklist().contains(link)) {
							int cross = t.linklistcompare(wpr.getworklinklist(), nowdemand.getworklinklist());
							if (cross == 0) {// ����������·���ཻ��ʾ����·��FS���Թ���
								FSuseOnlink = wpr.getFSuseOnlink();
								int useFS = FSuseOnlink.get(link);//�ҳ�֮ǰҵ���ϸ���·�Ѿ�ʹ�õ�FS
								removeLinklist.add(link);
								if(useFS>max){
									max=useFS;
								}
							}
						}
					}//�ҳ�֮ǰ���пɹ���ҵ���� ʹ�õ����FS
					linklistOnly.add(link);
								if (max < slotnum) {//���max>slotnum��ô����Ҫ����RSA
									int newFS = slotnum - max;
									index_wave = spa.spectrumallocationOneRoute(false, null, linklistOnly, newFS);
									ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), newFS);
									link.setMaxslot(newFS + link.getMaxslot());
									setFSuseOnlink.put(link, newFS);
									// System.out.println("��· " + link.getName() + "�����slot�ǣ� " + link.getMaxslot()+" ����Ƶ�״�����
									// "+link.getSlotsindex().size());
								}
					if(max==0){
						index_wave = spa.spectrumallocationOneRoute(false, null, linklistOnly, slotnum);
						ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
						link.setMaxslot(slotnum + link.getMaxslot());
						setFSuseOnlink.put(link, slotnum);
					}
					if(max>=slotnum){
						setFSuseOnlink.put(link, 0);
					}
				}

	for(int num = 0;num<iplayer.getNodelist().size()-1;num++){// ��IP����Ѱ��transparent��·������
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
	boolean findflag = false;try
	{
		System.out.println(finlink.getName());
		file_io.filewrite2(OutFileName,finlink.getName());
		findflag = true;
	}catch(java.lang.NullPointerException ex)
	{
		System.out.println("IP ��û�и���·��Ҫ�½���·");
		file_io.filewrite2(OutFileName,"IP ��û�и���·��Ҫ�½���·");
		createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
		iplayer.addLink(createlink);
	}

	VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1,0);
	Vlink.setnature(1);Vlink.setUsedcapacity(Vlink.getUsedcapacity()+IPflow);Vlink.setFullcapacity(slotnum*X);// �������flow�Ǵ����������
	Vlink.setRestcapacity(Vlink.getFullcapacity()-Vlink.getUsedcapacity());Vlink.setlength(length1);Vlink.setcost(cost);Vlink.setPhysicallink(linklist);provirtuallinklist.add(Vlink);
	
	if(findflag)
	{// �����IP�����Ѿ��ҵ�����·
		// System.out.println(finlink.getVirtualLinkList().size());
		finlink.getVirtualLinkList().add(Vlink);
		// System.out.println(finlink.getVirtualLinkList().size());
		System.out.println("IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
		System.out.println("������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ " + finlink.getVirtualLinkList().size());
		file_io.filewrite2(OutFileName,"IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
		file_io.filewrite2(OutFileName,"������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ " + finlink.getVirtualLinkList().size());
	}else
	{
		createlink.getVirtualLinkList().add(Vlink);
		// System.out.println(createlink.getVirtualLinkList().size());
		System.out.println("IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
		System.out.println("������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ " + createlink.getVirtualLinkList().size());
		file_io.filewrite2(OutFileName,"IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
		file_io.filewrite2(OutFileName,"������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ " + createlink.getVirtualLinkList().size());
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

	public boolean RSAunderSet(int[] set, LinearRoute newRoute, Layer oplayer, Layer ipLayer, int IPflow,
			ArrayList<RouteAndRegPlace> regplaceoption, ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair) {
		boolean partworkflag = false, RSAflag = false, regflag = false;
		double length = 0;
		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		ArrayList<Link> linklist = new ArrayList<>();
		int FStotal = 0, n = 0;
		ProregeneratorPlace rp = new ProregeneratorPlace();
		for (int i = 0; i < set.length + 1; i++) {// RSA�Ĵ������������ĸ�����1
			if (!partworkflag && RSAflag)
				break;
			if (i < set.length){
				System.out.println("****************��������λ��Ϊ��" + set[i]); // set�������Ӧ���ǽڵ��λ��+1��
				file_io.filewrite2(OutFileName,"****************��������λ��Ϊ��" + set[i]); 
			}
			else {
				System.out.println("************���һ�����������ս��֮���RSA ");
				file_io.filewrite2(OutFileName,"************���һ�����������ս��֮���RSA ");
				regflag = true;
			}
			do {// ͨ��һ��
				Node nodeA = newRoute.getNodelist().get(n);
				Node nodeB = newRoute.getNodelist().get(n + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println(link.getName());
				file_io.filewrite2(OutFileName,link.getName());
				length = length + link.getLength();
				linklist.add(link);
				n = n + 1;
				if (!regflag) {// δ�������һ��·����RSA
					if (n == set[i]) {
						partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, false, wprlist, nodepair);// ��ʱ��n�����������
						FStotal = FStotal + nodepair.getSlotsnum();
						length = 0;
						RSAflag = true;
						linklist.clear();
						break;
					}
				}
				if (n == newRoute.getNodelist().size() - 1) {
					partworkflag = rp.vertify(IPflow, length, linklist, oplayer, ipLayer, false, wprlist, nodepair);// ��ʱ��n�����������
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
			for (int k = 0; k < set.length; k++) {
				setarray.add(set[k]);
			}
			rarp.setregnode(setarray);
			rarp.setregnum(setarray.size());
			regplaceoption.add(rarp);
			System.out.println("��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());// �������ĸ����ӽ�ȥ
			file_io.filewrite2(OutFileName,"��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());
		}
		return partworkflag;

	}
}
