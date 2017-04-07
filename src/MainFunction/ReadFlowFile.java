package MainFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import network.Layer;
import network.NodePair;

public class ReadFlowFile {
	
	 public int Readflow(Layer layer, String writename)
	    {
			String[] data = new String[10];
			File file = new File(writename);
			BufferedReader bufRdr = null;
			try {
				bufRdr = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String line = null;
			int col = 0;
			try {
				line = bufRdr.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//read the first title line
			//read each line of text file
			try {
				boolean Nodepair = false;
				while((line = bufRdr.readLine()) != null){
					StringTokenizer st = new StringTokenizer(line,",");
					while (st.hasMoreTokens()){
						data[col] = st.nextToken();
						col++;
						
					}
					col=0;
					String name = data[0];
				//	System.out.println(data[0]);
					if(name.equals("Nodepair")){
						Nodepair=true;						
					}
					//read nodes
					if(Nodepair)//node operation
					{ //link operation
						if(!(name.equals("Nodepair"))){
						    NodePair currentnodepair=layer.getNodepairlist().get(data[0]);
							int flow=Integer.parseInt(data[1]);
					//		System.out.println(data[0]+"  "+slotNum);
							currentnodepair.setTrafficdemand(flow);
						}					
					}				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			try {
				bufRdr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
	    	
	    	
	    	return 0;
	    }
	
	
	
	
	
	
	

}
