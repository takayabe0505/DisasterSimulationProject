package KsymSimulation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import Tools.Correlation;

/* Made by T.Yabe
 * since 2014/11/24
 */

public class Simulator2ndPartFujisawa {

	/* 
	 * This program:
	 * 1. calculates & outputs RMSE and likelihood of all scenarios to "result" file
	 * 2. calculates best parameter set based on likelihood
	 * 3. outputs all parameter sets for next timestep 
	 */

	public static void main(String args[]){
		
		int h = 4; //何時間後か！！

		int time = h + 14;
		int filenumber = 144; // ENTER number of scenarios!!
		File obs = new File("C:/Users/yabec_000/Desktop/ZDCKonzatsuToukeiData/ZDC_20110311_5_"+time+".csv"); // define file of observation data
		File meshcodes = new File("C:/Users/yabec_000/Desktop/Fujisawa_meshcodes_5.csv"); //define meshcode file for area of study
		File parafile  = new File("C:/Users/yabec_000/Desktop/Fujisawa0203/parameter.csv");

		/* phases 1 and 2 ... */
		getresultFile(filenumber, parafile, obs, meshcodes, h);

		/*get cor. coeff.*/

		File results = new File ("C:/Users/yabec_000/Desktop/"+filenumber+"results.csv");
		getBestPara(results);
		//		System.out.println(optpara);

		/* phase 3 */
		//		int hour = 3; // ENTER next hour!!
		//		getNextPara(hour);

	}

	// input:parameter scnenario para file, output:parameters
	public static String[] getPara(File infile, int i){
		String[] parameters = new String[4];
		try{
			BufferedReader br = new BufferedReader(new FileReader(infile));
			String line = null;
			while ((line = br.readLine()) != null){
				String tokens[] = line.split(",");
				String scenum = tokens[0];
				if (scenum.equals(i)){
					parameters[0] = tokens[1];
					parameters[1] = tokens[2];
					parameters[2] = tokens[3];
					parameters[3] = tokens[4];
				}
				else {continue;}
			}
			br.close();
		}
		catch(FileNotFoundException xx) {
			System.out.println("File not found 1");
		}
		catch(IOException xxx) {
			System.out.println(xxx);
		}
		return parameters;
	}

	public static File getresultFile(int filenumber, File parafile, File obs, File meshcodes, int h){
		// record rmse and likelihood for each scenario
		File rmse_like = new File("C:/Users/yabec_000/Desktop/" + filenumber + "results.csv"); 
		File[] scenarioresults = new File[filenumber];

		for (int i=0; i<=143; i++){
			//System.out.println(ii);
			scenarioresults[i] = new File ("C:/Users/yabec_000/Desktop/Fujisawa0203/Fujisawa"+ h +"Hour/output1/mesh_" + i + ".csv");
		}
		//		for (int q=0; q<=35; q++){
		//			scenarioresults[q+36] = new File ("C:/Users/yabec_000/Desktop/Fujisawa0126/Fujisawa5Hour/output2/mesh_" + q + ".csv");
		//		}

		try{
			File newdir = new File("c:/users/yabec_000/Desktop/tempresults");
			newdir.mkdir();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(rmse_like, true));
			String[] parameters = {"0" , "0" , "0" , "0"};
			for (int j=0; j<filenumber; j++){
				File TempResult = new File("c:/users/yabec_000/Desktop/tempresults/tempresultfile"+ j +".csv");
				//				String[] parameters = getPara(parafile,j);			
				BufferedReader br = new BufferedReader(new FileReader(parafile));
				String line = null;
				while ((line = br.readLine()) != null){
					String tokens[] = line.split(",");
					String scenum = tokens[0];
					if (scenum.equals(String.valueOf(j+1))){
						parameters[0] = tokens[1];
						parameters[1] = tokens[2];
						parameters[2] = tokens[3];
						parameters[3] = tokens[4];
					}
					else {continue;}
				}
				br.close();

				double RMSE = Simulation_ver2.getRMSE(scenarioresults[j], obs, meshcodes, TempResult);
				double likelihood = Simulation_ver2.getlikelihood(RMSE,75, 0);
				double correl = getCorrelation(scenarioresults[j],obs,meshcodes);
				double error = getErrorPercentage(scenarioresults[j],obs,meshcodes);
				String rmse = String.valueOf(RMSE);
				bw.write(j+1 	      + "," + 
						parameters[0] + "," + 
						parameters[1] + "," + 
						parameters[2] + "," + 
						parameters[3] + "," + 
						rmse		  + "," + 
						likelihood	  + "," +
						correl        + "," +
						error
						);
				bw.newLine();
			}
			bw.close();
		}
		catch(FileNotFoundException xx) {
			System.out.println("File not found 1");
		}
		catch(IOException xxx) {
			System.out.println(xxx);
		}
		return rmse_like;
	}

	public static Double getCorrelation(File input1, File input2, File meshcodefile){
		//観測データと比較
		Map<String,Integer> ptmap = new HashMap<String, Integer>();
		Map<String,Double> zdcmap = new HashMap<String, Double>();
		String temp = ("c:/Users/yabec_000/Desktop/tempforCorrel.csv");
		Set<String> meshcodeset = new HashSet<String>();

		try{
			BufferedReader brm = new BufferedReader(new FileReader(meshcodefile));
			String linemesh = null;
			while((linemesh = brm.readLine()) != null){
				String[] tokens = linemesh.split("\t");
				meshcodeset.add(tokens[0]);
			}
			brm.close();
		}
		catch(FileNotFoundException z) {System.out.println("File not found 1");}
		catch(IOException e) {System.out.println(e);}

		try{
			BufferedReader br3 = new BufferedReader(new FileReader(input1));
			String line = null;
			while( (line = br3.readLine()) != null ) {
				String[] zdctokens = line.split(",");
				String meshcode = zdctokens[0];
				Double count   = Double.valueOf(zdctokens[1]);
				zdcmap.put(meshcode, count);
			}
			br3.close();
		}
		catch(FileNotFoundException z) {
			System.out.println("File not found 2");}
		catch(IOException e) {System.out.println(e);}

		try{
			BufferedReader br4 = new BufferedReader(new FileReader(input2));
			String line4 = null;
			while( (line4 = br4.readLine()) != null ) {
				String[] pttokens = line4.split("\t");
				String meshcode = pttokens[0];
				Double counts   = Double.valueOf(pttokens[1]);
				Integer intcount = (int)Math.floor(counts);
				ptmap.put(meshcode, intcount);
				//				meshcodeset.add(meshcodes);
			}
			br4.close();
		}
		catch(FileNotFoundException z) {
			System.out.println("File not found 3");}
		catch(IOException e) {System.out.println(e);}

		try{
			BufferedWriter tempwriter = new BufferedWriter(new FileWriter(temp));
			for(String mc:meshcodeset){
				int countpt = 0;
				int countds = 0;
				if(ptmap.containsKey(mc)){countpt = ptmap.get(mc);}
				if(zdcmap.containsKey(mc)){countds = (int)Math.floor(zdcmap.get(mc));}
				tempwriter.write(countpt + "," + countds);
				tempwriter.newLine();
			}
			tempwriter.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found 2");
		}
		catch(IOException e) {
			System.out.println(e);
		}

		int counter = meshcodeset.size();
		//観測データとの相関を計算
		Vector v1 = new Vector();
		Vector v2 = new Vector();
		Correlation.readTextFromFile_AndSetVector(temp,v1);
		Correlation.KataHenkan(v1,v2);
		double cor  = Correlation.getCorrelationCoefficient(v2);
		return cor;
	}

	// calculate best parameter from likelihood
	public static Double[] getBestPara(File infile){
		Double[] optpara = new Double[4];
		try{
			BufferedReader br = new BufferedReader(new FileReader(infile));
			String line = null;
			ArrayList<Double> like = new ArrayList<Double>();
			ArrayList<Double> alist = new ArrayList<Double>();
			ArrayList<Double> blist = new ArrayList<Double>();
			ArrayList<Double> clist = new ArrayList<Double>();
			ArrayList<Double> dlist = new ArrayList<Double>();

			while((line = br.readLine()) != null){
				String tokens[] = line.split(",");
				double aa = (Double.parseDouble(tokens[1]))*(Double.parseDouble(tokens[6]));
				alist.add(aa);
				//				System.out.println("aa is " + aa);
				double bb = (Double.parseDouble(tokens[2]))*(Double.parseDouble(tokens[6]));
				blist.add(bb);
				//				System.out.println("bb is " + bb);
				double cc = (Double.parseDouble(tokens[3]))*(Double.parseDouble(tokens[6]));
				clist.add(cc);
				//				System.out.println("cc is " + cc);
				double dd = (Double.parseDouble(tokens[4]))*(Double.parseDouble(tokens[6]));
				dlist.add(dd);
				//				System.out.println("dd is " + dd);
				like.add(Double.parseDouble(tokens[6]));
				//				System.out.println("like is " + like);
			}
			br.close();

			double likesum = 0d;
			for (double ele : like){
				likesum = likesum + ele;
			}
			System.out.println("sum of like is " + likesum);

			double opta = 0d;
			double suma = 0d;
			double optb = 0d;
			double sumb = 0d;
			double optc = 0d;
			double sumc = 0d;
			double optd = 0d;
			double sumd = 0d;

			for (double elea : alist){
				suma = elea + suma;
				opta = suma/likesum;
			}
			System.out.println("opta is " + opta);
			for (double eleb : blist){
				sumb = eleb + sumb;
				optb = sumb/likesum;
			}			
			System.out.println("optb is " + optb);
			for (double elec : clist){
				sumc = elec + sumc;
				optc = sumc/likesum;
			}			
			System.out.println("optc is " + optc);
			for (double eled : dlist){
				sumd = eled + sumd;
				optd = sumd/likesum;
			}
			System.out.println("optd is " + optd);

			optpara[0] = opta;
			optpara[1] = optb;
			optpara[2] = optc;
			optpara[3] = optd;
		}
		catch(FileNotFoundException z) {
			System.out.println("File not found 3");
		}
		catch(IOException e) {
			System.out.println(e);
		}
		return optpara;
	}

	public static Double getErrorPercentage(File pt, File zdc, File meshlist){ 
		Map<String, Double> ptmap = Simulation_ver2.intomap(pt);
		Map<String, Double> zdcmap = Simulation_ver2.intomap2(zdc);
		ArrayList<String> meshes = Simulation_ver2.getMeshlist(meshlist);
		//		System.out.println("meshes: " + meshes);
		ArrayList<Double> templist = new ArrayList<Double>();
		for(String mc:meshes){
			double temp= 0;
			double countpt = 0d;
			double countds = 0d;
			if(ptmap.containsKey(mc)){countpt = ptmap.get(mc);}
			if(zdcmap.containsKey(mc)){countds = zdcmap.get(mc);}
			if((countds != 0)&&(countpt != 0)){
				double diff = Math.abs(countpt - countds);
				temp = (diff/countds)*100;
			}
			else{
				temp = 0;
			}
			templist.add(temp);
		}
		Double sum = 0d;
		for(Double num:templist){
			sum = sum + num;
		}
		Double ErrorPercent = sum/templist.size();
		//		System.out.println("rmse: " + RMSE);
		return ErrorPercent;
	}


	public static File getNextPara(int hour){
		File nextparafile = new File("c:/Users/yabec_000/Desktop/Tokyo_nextpara" + hour + ".csv");

		double[] a = new double[4];
		double[] b = new double[3];
		double[] c = new double[4];
		double[] d = new double[3];

		if (hour == 2){
			a[0]=0; a[1]=0.2; a[2]=0.4; a[3] = 0.1;
			b[0]= 0.7; b[1]=1.3; b[2]=1;
			c[0]=0; c[1]=0.2; c[2]=0.4; c[3] = 0.1;
			d[0]=0; d[1]=0.2; d[2]=0.4;
		}
		else if (hour == 3){
			a[0]=0; a[1]=0.2; a[2]= 0.1; a[3]= 0.4;
			b[0]= 0.7; b[1]=1.3; b[2]=1;
			c[0] = 0; c[1]= 0.1; c[2]=0.2; c[3]= 0.4;
			d[0] = 0; d[1]= 0.2; d[2]=0.4;
		}
		else {
			System.out.println("hour is wrong, dipshit.");
		}

		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(nextparafile, true));
			for (int i=0; i<=3; i++){
				for (int j=0; j<=2; j++){
					for (int k=0; k<=3; k++){
						for (int l=0; l<=2; l++){
							bw.write(a[i] + "," + b[j] + "," + c[k] + "," + d[l]);
							bw.newLine();
						}
					}
				}
			}
			bw.close();
		}
		catch(FileNotFoundException z) {
			System.out.println("File not found 3");
		}
		catch(IOException e) {
			System.out.println(e);
		}
		return nextparafile;
	}
}

