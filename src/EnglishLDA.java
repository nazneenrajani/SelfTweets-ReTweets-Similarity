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


public class EnglishLDA {
	static ArrayList<String> tweetAuthor = new ArrayList<String>();
	public static void main(String[] args) throws IOException{
		/*File folder = new File ("/Users/nrajani/Downloads/core_user_timelines_text_filtered");
		String folder_name = "/Users/nrajani/Downloads/core_user_timelines_text_filtered/";
		for (final File fileEntry : folder.listFiles()) {
			createVector(folder_name+fileEntry.getName(),folder_name+fileEntry.getName()+"bagofwords",folder_name+fileEntry.getName()+"vocab");
		}
*/		createVector("/Users/nrajani/Documents/workspace/Maytal/doc","bow_doc","vocab_doc");
	}
	private static void createVector(String file, String bow, String vocab) throws IOException {
		Map<String,Integer> wordMap = new HashMap<String,Integer>();
		Map<Integer,Integer> wordFreq;
		List<String> text = new ArrayList<String>();
		List<String> wordList = new ArrayList<String>();
		String url = "(www.)?[a-z0-1]+.(com|net|co.uk|hub)";
		String reg = "(\\d+)?[-\\.;,\\\\?!()'\\[\\]\"/\\|:-@_$%+*`&#~\\{\\}]+";
		String digit = "\\b[^ ]*[0-9][^ ]*\\b";
		String aps = "([A-Za-z]+)(')([a-z])";
		String letter = "\\b[a-z]\\b";
		//String ebay="\\b(new|used|NIB|NWT|Pinterest|nwt|nib|NEW|USED)\\b";
		String line;
		Set<String> stopWords = new LinkedHashSet<String>();
		BufferedReader br =new BufferedReader(new FileReader(file));
		PorterStemmer stemmer = new PorterStemmer();
		int count=1;
		try {
			BufferedReader SW= new BufferedReader(new FileReader("stopwords"));
			for(String lin;(lin = SW.readLine()) != null;)
				stopWords.add(lin.trim());
			SW.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while((line = br.readLine()) != null) {
			String smallLine = line.toLowerCase();
			smallLine=smallLine.replaceAll(url,"").replace("?","").replaceAll(reg,"").replaceAll(digit,"")
					.replaceAll(aps,"$1").replaceAll("\\s+"," ").replaceAll(letter,"").replaceAll("\\?","").replaceAll("[^\\x00-\\x7F]", "");
			if(smallLine != null && !smallLine.isEmpty()){
				text.add(smallLine.trim().replaceAll(" +", " "));
			}
			String[] st = smallLine.split(" ");
			int stopCount=0;
			for(int i=0; i < st.length;i++) {
				if(stopWords.contains(st[i]))
					stopCount++;
			}
			if(stopCount < 2)
				continue;
			else{
				for(int i=0; i < st.length;i++) {
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
						wordList.add(token);
						count +=1;
					}
				}
			}
		}
		br.close();
		if(wordList.size()>0){
			try {
				File fil = new File(vocab);
				FileWriter f = new FileWriter(fil,true);
				BufferedWriter b = new BufferedWriter(f);
				for(int i = 0;i<wordList.size();i++){
					b.write(wordList.get(i));
					b.write("\n");
				}
				b.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			int line_number =0;
			System.out.println(text.size());
			for(String line1: text) {
				line_number++;
				wordFreq = new HashMap<Integer,Integer>();
				String[] st1 = line1.split(" ");
				for(int i = 0; i<st1.length;i++) {
					String tk=st1[i];
					String token = tk.replaceAll("\\?+([a-zA-Z])+\\?+","$1");
					if(wordMap.get(token)==null)
						continue;
					if (token.matches(reg) || token.matches(aps)|| token.matches("\\d+") || token.equals("")||token.contains("?"))
						continue;
					if(stopWords.contains(token)||token.length()<2)
						continue;
					stemmer.setCurrent(token);
					if(stemmer.stem())
						token = stemmer.getCurrent();
					if(!wordFreq.containsKey(wordMap.get(token)))
						wordFreq.put(wordMap.get(token),1);
					else
						wordFreq.put(wordMap.get(token),wordFreq.get(wordMap.get(token))+1);
				}
				try {
					File fil = new File(bow);
					FileWriter f = new FileWriter(fil,true);
					BufferedWriter b = new BufferedWriter(f);
					for(Integer token : wordFreq.keySet()){
						b.write(String.valueOf(line_number));
						b.write(" ");
						b.write(String.valueOf(token));
						b.write(" ");
						b.write(String.valueOf(wordFreq.get(token)));
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
		}
	}
}
