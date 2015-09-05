package lsgs.global;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author likun@stu.zzu.edu.cn
 * @date 2015 - 09 - 04
 * @Description
 *	It's the graph node's structure
 * name is the node's name
 * neighbors are the node's neighbors,this store with a Map<GNode,Double>
 * Double represent the weight between the two nodes
 */
public class GNode {
	int name;
	Map<GNode, Double> neighbors;
	
	public GNode(int name){
		this.name = name;
		neighbors = new HashMap<GNode, Double>();
	}
}
