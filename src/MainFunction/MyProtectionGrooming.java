
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class MyProtectionGrooming {

	public void myprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder) {
		RouteSearching Dijkstra = new RouteSearching();

		ArrayList<Link> DelLinkList = new ArrayList<Link>();
		ArrayList<Link> SumDelLinkList = new ArrayList<Link>();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();

		HashMap<String, Link> linklist = iplayer.getLinklist();
		Iterator<String> linkitor = linklist.keySet().iterator();
		while (linkitor.hasNext()) {
			Link link = (Link) (linklist.get(linkitor.next()));
				DelLinkList.add(link);
			}
				DelLinkList.add(link);
			}
			/*
			 */
					for (Link LinkInIPlayer : link.getPhysicallink()) {
							DelLinkList.add(link);
						}
					}
				}
			}
		}
		for (Link dellink : DelLinkList) {
			if (!SumDelLinkList.contains(dellink))
				SumDelLinkList.add(dellink);
		}
		for (Link dellink2 : SumDelLinkList) {
			iplayer.removeLink(dellink2.getName());

		LinearRoute newRoute = new LinearRoute(null, 0, null);

			iplayer.addLink(addlink);
		}
		SumDelLinkList.clear();
		DelLinkList.clear();

		if (newRoute.getNodelist().size() != 0) {
			newRoute.OutputRoute_node(newRoute);

				LinkOnNewRoute.setFlow(LinkOnNewRoute.getFlow() + nodepair.getTrafficdemand());
			}
		} else {
			ArrayList<Link> opDelLink = new ArrayList<Link>();
					HashMap<String, Link> oplinklist = oplayer.getLinklist();
					Iterator<String> oplinkitor = oplinklist.keySet().iterator();
					while (oplinkitor.hasNext()) {
						Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
						if (oplink.getName().equals(LinkOnPhy.getName())) {
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

			for (Link opdellink : opDelLink) {
				oplayer.addLink(opdellink);
			opDelLink.clear();
			if (opPrtectRoute.getLinklist().size() == 0) {
			} else {
				opPrtectRoute.OutputRoute_node(opPrtectRoute);
				int slotnum = 0;
				int IPflow = nodepair.getTrafficdemand();
				double X = 1;// 2000-4000 BPSK,1000-2000
				double routelength = opPrtectRoute.getlength();
				if (routelength > 2000 && routelength <= 4000) {
					X = 12.5;
				} else if (routelength > 1000 && routelength <= 2000) {
					X = 25.0;
				} else if (routelength > 500 && routelength <= 1000) {
					X = 37.5;
				} else if (routelength > 0 && routelength <= 500) {
					X = 50.0;
				}

				opPrtectRoute.setSlotsnum(slotnum);
				ArrayList<Integer> index_wave = new ArrayList<Integer>();
				WorkingGrooming wg = new WorkingGrooming();
				index_wave = wg.spectrumallocationOneRoute(opPrtectRoute);
				if (index_wave.size() == 0) {
				} else {
					double length = 0;
					double cost = 0;
					for (Link link : opPrtectRoute.getLinklist()) {
						length = length + link.getLength();
						cost = cost + link.getCost();
						Request request = null;
						ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);

						link.setMaxslot(slotnum + link.getMaxslot());
					}
					String name = opsrcnode.getName() + "-" + opdesnode.getName();
					Link newlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
					iplayer.addLink(newlink);
					newlink.setNature(1);
					newlink.setFlow(nodepair.getTrafficdemand());
					newlink.setIpremainflow(newlink.getSumflow() - newlink.getFlow());
					numOfTransponder = numOfTransponder + 2;
					newlink.setPhysicallink(opPrtectRoute.getLinklist());
				}
			}
		}
	}
}
