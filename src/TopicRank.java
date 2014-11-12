import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;	
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import snowballstemmer.PorterStemmer;
import Jama.Matrix;

public class TopicRank {
	public static int topics = 3;
	public static List<String> user_list = new ArrayList<String>();
	static double[][] user_words;
	static double[][] topic_words;
	static double[][] user_topic_score;
	static List<Integer> index;
	static Set<Integer> interesting;
	public static void main(String[] args){
		String folder_name = "/Users/nrajani/Downloads/tweets/core_user_timelines_text_filtered/";
		unigram(folder_name,"3_topics",10);
		similarity("sim_score_3");
	}
	private static void similarity(String output) {
		user_topic_score = new double[user_list.size()][topics];

		for(int j = 0; j <user_list.size();j++){
			for(int i = 0; i < topics; i ++){
				user_topic_score[j][i] = 0.0;
			}
		}
		for(int k = 0;k < user_list.size(); k++){
			double[] B = user_words[k];
			for(int i = 0; i<topics ; i++){
				double[] A = topic_words[i];
				user_topic_score[k][i] = computeSimilarity(new Matrix(A,topic_words[0].length),new Matrix(B,user_words[0].length));
			}
		}
		try {
			File fil = new File(output);
			FileWriter f = new FileWriter(fil,true);
			BufferedWriter b = new BufferedWriter(f);
			for(int i =0; i<user_list.size();i++){
				for(int j =0;j<topics;j++){
					b.write(String.valueOf(user_topic_score[i][j]));
					b.write(" ");
				}
				b.write("\n");
			}
			b.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void unigram(String folder_name,String topic_file,int topWords) {
		PorterStemmer stemmer = new PorterStemmer();
		Map<String,Integer> wordMap;
		BufferedReader SW;
		Set<String> stopWords = new LinkedHashSet<String>();
		try {
			SW= new BufferedReader(new FileReader("stopwords"));
			for(String lin;(lin = SW.readLine()) != null;)
				stopWords.add(lin.trim());
			SW.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader br;
		String url = "www.[a-z0-1]+.(com|net|co.uk|hub)";
		String reg = "(\\d+)?[-\\.;,\\\\?!()'\\[\\]\"/\\|:-@_$%+*`&#~\\{\\}]+";
		String digit = "\\b[^ ]*[0-9][^ ]*\\b";
		String aps = "([A-Za-z]+)(')([a-z])";
		String letter = "\\b[a-z]\\b";
		String ebay="\\b(new|used|NIB|NWT|Pinterest|nwt|nib|NEW|USED)\\b";
		int user =0;
		File folder = new File (folder_name);
		for (final File fileEntry : folder.listFiles()) {
			try{			 
				String line;
				br = new BufferedReader(new FileReader(folder_name+fileEntry.getName()));
				String user_tweet = "";
				while ((line = br.readLine()) != null) {
					String smallLine = line.toLowerCase();
					smallLine=smallLine.replaceAll(url,"").replace("?","").replaceAll(reg,"").replaceAll(digit,"")
							.replaceAll(aps,"$1").replaceAll("\\s+"," ").replaceAll(letter,"").replaceAll(ebay,"").replaceAll("\\?","").replaceAll("[^\\x00-\\x7F]", ""); 
					if(smallLine != null && !smallLine.isEmpty()){
						user_tweet = user_tweet+" "+smallLine;
					}
				}
				user++;
				user_list.add(user_tweet.trim());
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(user);
		wordMap = new HashMap<String,Integer>();
		int count = 0;
		for(String line: user_list) {
			String[] st = line.split(" ");
			for(int i = 0; i<st.length;i++) {
				String tk=st[i];
				String token = tk.replaceAll("\\?+([a-zA-Z])+\\?+","$1");
				if (token.matches(reg) || token.matches(aps)|| token.matches("\\d+") || token.equals("")||token.contains("?"))
					continue;
				if(stopWords.contains(token)||token.length()<2)
					continue;
				stemmer.setCurrent(token);
				if(stemmer.stem())
					token = stemmer.getCurrent();
				if(!wordMap.containsKey(token)){
					wordMap.put(token,count);
					count +=1;

				}
			}
		}
		System.out.println(user_list.size());
		System.out.println(wordMap.size());
		user_words = new double[user_list.size()][wordMap.size()];
		for(int j = 0; j <user_list.size();j++)
			for(int i = 0; i < wordMap.size(); i ++)
				user_words[j][i] = 0.0;
		int line_number =-1; 
		for(String line: user_list) {
			line_number++;
			String[] st = line.split(" ");
			for(int i = 0; i<st.length;i++) {
				String token = st[i];
				if(wordMap.containsKey(token))
					user_words[line_number][wordMap.get(token)]+=(1.0/st.length);
			}
		}
		topic_words = new double[topics][wordMap.size()];
		for(int j = 0; j <topics;j++)
			for(int i = 0; i < wordMap.size(); i ++)
				topic_words[j][i] = 0.0;	
		try {
			br= new BufferedReader(new FileReader(topic_file));
			String number,tmp;
			String[] line;
			int row = 0;
			while((number=br.readLine())!=null){
				if(number.equals(""))
					continue;
				if(number.replaceAll(" ", "").startsWith("TOPIC")){
					number=br.readLine();
					for(int k = 0; k<topWords; k++){
						number = br.readLine();
						line =number.replaceAll(" ","").split("\t");
						tmp = line[0].toLowerCase();
						if(!wordMap.containsKey(tmp))
							continue;
						topic_words[row][wordMap.get(tmp)] = Double.parseDouble(line[1]);
					}
					row++;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double computeSimilarity(Matrix sourceDoc, Matrix targetDoc) {
		double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
		double eucledianDist = sourceDoc.normF() * targetDoc.normF();
		return dotProduct / eucledianDist;
	}
	public static double jaccard(Set<Integer> h1, Set<Integer> h2){
		int sizeh1 = h1.size();
		h1.retainAll(h2);
		h2.removeAll(h1);
		int union = sizeh1 + h2.size();
		int intersection = h1.size();	
		return intersection*1.0/union;
	}
	public static double rescaledDotProduct(Matrix sourceDoc, Matrix targetDoc, Matrix Pdash, Matrix Qdash, Matrix Qreverse) {
		double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
		double eucledianDist = sourceDoc.normF() * targetDoc.normF();
		double dot =  dotProduct / eucledianDist;
		double dmin = Pdash.arrayTimes(Qreverse).norm1();
		double dminDist = Pdash.normF() * Qreverse.normF();
		double dmax = Pdash.arrayTimes(Qdash).norm1();
		double dmaxDist = Pdash.normF() * Qdash.normF();
		double dmi = dmin/dminDist;
		double dma = dmax/dmaxDist;
		//System.out.println(dot);
		double num = dot-dmi;
		double den = dma-dmi;
		double result = num/den;
		//System.out.println(result);
		return result;
	}
}