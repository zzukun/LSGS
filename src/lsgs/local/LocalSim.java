package lsgs.local;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author likun@stu.zzu.edu.cn
 * @date 2015 - 09 - 02
 * @Description
 * This class is for calculate the local similarity described in paper
 * "A collaborative filtering framework based on both local user similarity and global similarity"
 * "H. Luo, C. Niu, R. shen, C. Ullrich"
 * 
 */
public class LocalSim {
	static final int r = 30;
	int commonNumber;
	
	ArrayList<Rate> rates = null;
	ArrayList<Param> params = null;
	
	/**
	 * read file u.data and get the format
	 * @param path
	 */
	private void dataReader(String path) {
		File f = new File(path);
		rates = new ArrayList<Rate>();
		
		try {
			Scanner sc = new Scanner(f);
			while(sc.hasNext()){
				String line = sc.nextLine();
				String[] lineCut = line.split("\\s");
				
				Rate r = new Rate();
				r.setUserId(Integer.valueOf(lineCut[0]));
				r.setItemId(Integer.valueOf(lineCut[1]));
				r.setRating(Integer.valueOf(lineCut[2]));
				
				rates.add(r);
			}
			sc.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * calculate u_hat
	 */
	private void calcul_u_hat() {
		DecimalFormat df = new DecimalFormat("#.00");  
		params = new ArrayList<Param>();
		if(rates != null){
			for( int i=1 ; i<=1682 ; i++ ){
				int M = 0;
				int rateSum = 0;
				for( int j=0 ; j<rates.size() ; j++){
					Rate r = rates.get(j);
					if(r.getItemId() == i){
						M++;
						rateSum = rateSum + r.getRating();
					}
				}
				Param param = new Param();
				param.itemId=i;
				param.u_hat = Double.parseDouble(df.format((double)rateSum/M));
					//System.out.println(i + "\t\t" +(double)rateSum/M);
				params.add(param);
			}
		}
	}
	
	/**
	 * calculate b_hat
	 */
	private void calcul_b_hat() {
		DecimalFormat df = new DecimalFormat("#.00");
		
		if(rates != null){
			for( int i=1 ; i<=1682 ; i++ ){
				int M = 0;
				double rateDiffSum = 0;
				for( int j=0 ; j<rates.size() ; j++){
					Rate r = rates.get(j);
					if(r.getItemId() == i){
						M++;
						rateDiffSum = rateDiffSum + Math.abs(r.getRating() - params.get(i-1).u_hat);
					}
				}
				params.get(i-1).b_hat = Double.parseDouble(df.format((double)rateDiffSum/M));
			}
		}
	}
	
	/**
	 * calculate Sp and Sq ,that for the user p and user q
	 * @param userId_p
	 * @param userId_q
	 */
	private void calcul_s(int userId_p , int userId_q) {
		DecimalFormat df = new DecimalFormat("#.00");
		
		for(int i=1;i<=1682;i++){
			for(int j=0;j<rates.size();j++){
				Rate r = rates.get(j);
				if(r.getItemId() == i && r.getUserId() == userId_p){
					params.get(i-1).Sp =Math.signum(r.getRating() - params.get(i-1).u_hat) * surprisal(r.getRating(),params.get(i-1).u_hat , params.get(i-1).b_hat);
					params.get(i-1).Sp = Double.parseDouble(df.format(params.get(i-1).Sp));
				}else if(r.getItemId() == i && r.getUserId() == userId_q) {
					params.get(i-1).Sq =Math.signum(r.getRating() - params.get(i-1).u_hat) * surprisal(r.getRating(),params.get(i-1).u_hat , params.get(i-1).b_hat);
					
					params.get(i-1).Sq = Double.parseDouble(df.format(params.get(i-1).Sq));
				}
			}
		}
	}
	
	/**
	 * the surprisal function --- the quantity of information of the rating 
	 */
	private double surprisal(int r , double  u_hat ,double b_hat) {
		if(b_hat ==0 )
			return 0;
		else
			return Math.log(2 * b_hat)  +  Math.abs(r-u_hat)/b_hat;
	}
	
	/**
	 * write {params} into file
	 * @param path
	 */
	private void writeToFile(String path){
		File f = new File(path);
		try {
			FileWriter fw = new FileWriter(f);
			for(int i=0;i<params.size();i++){
				Param p = params.get(i);
				fw.write(p.itemId + "\t\t" + p.u_hat + "\t\t" + p.b_hat + "\t\t" +p.Sp + "\t\t" + p.Sq+"\n");
			}
			fw.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * calculate the local similarity
	 * @return
	 */
	private double local_sim() {
		double sumSpSq = 0;
		double sumSp2 = 0;
		double sumSq2 = 0;
		commonNumber =0;
		for(int i=0 ; i<params.size() ; i++){
			Param p = params.get(i);
			if(p.Sp != 0 && p.Sq !=0){
				sumSpSq = sumSpSq + p.Sp * p.Sq;
				sumSp2 = sumSp2 + p.Sp * p.Sp;
				sumSq2 = sumSq2 + p.Sq * p.Sq;
				commonNumber++;
			}
		}
		//error
		double sim = sumSpSq /  ( Math.sqrt(sumSp2) * Math.sqrt(sumSq2) );
		return sim;
	}
	
	/**
	 * calculate the local similarity with [ weight ]
	 * @param localSim
	 * @return
	 */
	private double local_sim_weight(int user_p, int user_q,double localSim) {
		
		int smaller = commonNumber;
		if(r<smaller)
			smaller = r;
		return ((double)smaller/r) * localSim;
	}
	
	public void run (int userId_p , int userId_q) {
		DecimalFormat df = new DecimalFormat("#.0000");
		
		dataReader("./data/u.data");
		calcul_u_hat();
		calcul_b_hat();
		calcul_s(userId_p, userId_q);
		writeToFile("./data/u.item.param");
		
		double localSim = local_sim();
		double localSimWeight = local_sim_weight(userId_p,userId_q,localSim);
		
		localSim = Double.parseDouble(df.format(localSim));
		localSimWeight = Double.parseDouble(df.format(localSimWeight));
		
		System.out.println("lcoalSim is :" + localSim);
		System.out.println("localSimWeight is :" + localSimWeight);
		
		if (localSimWeight > 0) {
			File out = new File("./data/result.positive");
			try {
				FileWriter fw = new FileWriter(out, true);
				
//				fw.write("---" + userId_p + "&" + userId_q + "---\n");
//				fw.write("lcoalSim is :\t\t" + localSim + "\n");
//				fw.write("localSimWeight is :\t\t" + localSimWeight + "\n\n");
				
				fw.write(userId_p + " " + userId_q);
				fw.write(" " + localSimWeight + "\n");
				
				fw.flush();
				fw.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public static void main(String[] args) {
		//int userId_p = 3;
		//int userId_q = 6;
		LocalSim ls;
		for(int i=1;i<=10;i++){
			for(int j=i+1;j<=10;j++){
				ls = new LocalSim();
				ls.run(i, j);
			}
		}
		//LocalSim ls = new LocalSim();
		//ls.run(userId_p, userId_q);
	
	}
}
