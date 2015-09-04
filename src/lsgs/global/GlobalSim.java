package lsgs.global;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * 
 * @author likun@stu.zzu.edu.cn
 * @date 2015 - 09 - 03
 * @Description
 * This class is for calculate the global similarity described in paper
 * "A collaborative filtering framework based on both local user similarity and global similarity"
 * "H. Luo, C. Niu, R. shen, C. Ullrich"
 * 
 */
public class GlobalSim {
	MyGraph myGraph;
	ArrayList<GNode> G;
	private Stack<GNode> stack =  new Stack<GNode>();
	private ArrayList<Object[]> sers = new ArrayList<Object[]>();
	
	private double run(int userIdP,int userIdQ){
		myGraph = new MyGraph();
		
		//construct the user graph
		G = myGraph.constructGraph();
		
		//show the graph's information
		for(int i=0;i<G.size();i++){
			System.out.print(G.get(i).name + " : ");
			for(Map.Entry<GNode, Double> entry : G.get(i).neighbors.entrySet()){
				System.out.print("[ " + entry.getKey().name +" , " + entry.getValue()+" ]");
			}
			System.out.println();
		}
		
		//find paths
		System.out.println("Finding path ... ");
		GNode startNode = getNode(userIdP);
		GNode endNode = getNode(userIdQ);
		getPaths(startNode, null, startNode, endNode);
		
//		for(int i=0;i<sers.size();i++){
//			for(int j=0;j<sers.get(i).length;j++){
//				GNode tmp = (GNode)sers.get(i)[j];
//				System.out.print(tmp.name + "\t");
//			}
//			System.out.println();
//		}
		double ret = maximin();
		System.out.println("The maximin is : "+ ret);
		return ret;
	}
	
	/**
	 * To judge if a node is in the stack
	 * @param node
	 * @return true if in the stack
	 */
	private boolean isNodeInStack(GNode node){
		Iterator<GNode> it = stack.iterator();
		while (it.hasNext()) {
			GNode tmp = (GNode)it.next();
			if(node == tmp){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * As the nodes in the stack make up a feasible path
	 * This function could show the path and store it in a predefined array
	 */
	private void savePath(){
		Object[] o = stack.toArray();
		for(int i=0;i<o.length;i++){
			GNode tmp = (GNode)o[i];
			if( i < (o.length -1) ){
				System.out.print(tmp.name + "->");
			}else{
				System.out.print(tmp.name);
			}
		}
		sers.add(o);
		System.out.println();
	}
	
	/**
	 * Use this function to find paths
	 * @param current			current node we inspect
	 * @param previous		previous node we inspect
	 * @param start				start node of path
	 * @param end				end node of path
	 * @return
	 */
	private boolean getPaths(GNode current,GNode previous,GNode start,GNode end){
		GNode nNode = null;
		//loop is appear if match conditions , so can't continue finding , return false
		if(current != null && previous != null && current ==previous)
			return false;
		
		if(current!=null){
			//push the start node into stack
			stack.push(current);
			//if the start node is the "end" , that means we have find a path
			if(current == end){
				savePath();
				return true;
			}else{
				//traverse all nodes that have relations with the CURRENT Node ,make it as the 'start node' of the next recursion
				Iterator<GNode> iterator = current.neighbors.keySet().iterator();
				
				nNode =  iterator.next() ;
				
				while(nNode != null){
					if(previous != null && 
							(nNode == start || nNode == previous || isNodeInStack(nNode))    ){
						if(!iterator.hasNext()){
							nNode =null;
						}else{
							nNode = (GNode)iterator.next();
						}
						continue;
					}
					// take nNode as the new start node, take the current as the previous node , recurse the function getPaths
					if(getPaths(nNode, current, start, end)){
						//if find a path ,pop the stack's top node
						stack.pop();
					}
					//continue testing nNode in the neighbors of current node
					if(!iterator.hasNext()){
						nNode = null;
					}else{
						nNode = (GNode)iterator.next();
					}
				}
				//after recurse all neighbors of current node, that means all path have been found that from current to end 
				stack.pop();
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * Find Node with name
	 * @param name
	 * @return corresponding node
	 */
	private GNode getNode(int name){
		for(int i=0;i<G.size();i++){
			if(G.get(i).name == name){
				return G.get(i);
			}
		}
		return null;
	}
	
	/**
	 * calculate the 'MAXIMIN'
	 * with weights
	 * find the minimal weight of a path
	 * from this minimal value, pick the maximal one
	 * @return the MAXIMIN
	 */
	private double maximin(){
		double max = -1;
		for(int i=0;i<sers.size();i++){
			double min = 9999;
			for(int j=0;j<sers.get(i).length;j++){
				GNode tmp = (GNode)sers.get(i)[j];
				System.out.print(tmp.name + "\t");
				if(j+1<sers.get(i).length){
					double weight = tmp.neighbors.get((GNode)sers.get(i)[j+1]);
					if(weight < min)
						min=weight;
					System.out.print("<"+weight + ">\t");
				}
			}
			System.out.println();
			if(min>max)
				max = min;
			System.out.println("this line's min :"+min + "\t\tMax until now : " + max);
		}
		return max;
	}
	
	public static void main(String[] args) {
		int userIdP = 1;
		int userIdQ = 4;
		double maximin = new GlobalSim().run(userIdP,userIdQ);
		System.out.println("The Global Similarity of user ["+userIdP+"] and [" + userIdQ+"] is : "+maximin);
	}
}
