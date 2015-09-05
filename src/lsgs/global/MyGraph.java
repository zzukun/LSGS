package lsgs.global;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author likun@stu.zzu.edu.cn
 * @date 2015 - 09 - 04
 * @Description
 * To construct the graph
 * read node's relate information from file
 * with this data,construct the graph structure with GNode
 */
public class MyGraph {
	ArrayList<GNode> nodes;
	
	public ArrayList<GNode> constructGraph(){
		nodes = new ArrayList<GNode>();
		
		//add nodes
		GNode gNode;
		for(int i=1;i<=10;i++){
			gNode=new GNode(i);
			nodes.add(gNode);
		}

		//add weights
		File f = new File("./data/result.positive");
		try {
			Scanner sc = new Scanner(f);
			while(sc.hasNext()){
				String line = sc.nextLine();
				String[] cutLine = line.split("\\s");
				int p			=		Integer.valueOf(cutLine[0]); 
				int q			=		Integer.valueOf(cutLine[1]); 
				double w	=		Double.valueOf(cutLine[2]); 
				
				for(int i=0;i<nodes.size();i++){
					if(nodes.get(i).name == p){
						nodes.get(i).neighbors.put(getNode(q), w);
					}
				}
				
				for(int i=0;i<nodes.size();i++){
					if(nodes.get(i).name == q){
						nodes.get(i).neighbors.put(getNode(p), w);
					}
				}
			}
			sc.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return nodes;
	}
	
	private GNode getNode(int name){
		for(int i=0;i<nodes.size();i++){
			if(nodes.get(i).name == name)
				return nodes.get(i);
		}
		return null;
	}
	
	public static void main(String[] args) {
		new MyGraph().constructGraph();
	}
}
