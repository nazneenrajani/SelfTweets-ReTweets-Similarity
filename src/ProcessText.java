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


public class ProcessText {
	static ArrayList<String> tweetAuthor = new ArrayList<String>();
	public static void main(String[] args) throws IOException{
		File folder = new File ("/Users/nrajani/Downloads/core_user_timelines_text_filtered");
		String folder_name = "/Users/nrajani/Downloads/core_user_timelines_text_filtered/";
		for (final File fileEntry : folder.listFiles()) {
			createVector(folder_name+fileEntry.getName(),folder_name+"/processed/"+fileEntry.getName());
		}
	}
	private static void createVector(String input, String output) throws IOException {
		List<String> text = new ArrayList<String>();
		String turl = "http://t.co/[a-zA-Z0-9]+";
		String url = "(www.)?[a-z0-1]+.(com|net|co.uk|hub|co)";
		String reg = "(\\d+)?[-\\.;,\\\\?!()'\\[\\]\"/\\|:-@_$%+*`&#~\\{\\}]+";
		String digit = "\\b[^ ]*[0-9][^ ]*\\b";
		String aps = "([A-Za-z]+)(')([a-z])";
		String letter = "\\b[a-z]\\b";
		String line;
		Set<String> stopWords = new LinkedHashSet<String>();
		BufferedReader br =new BufferedReader(new FileReader(input));
		PorterStemmer stemmer = new PorterStemmer();
		File file = new File(output);
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
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
			smallLine=smallLine.replaceAll(turl,"").replaceAll(url,"").replace("?","").replaceAll(reg,"").replaceAll(digit,"")
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
					bw.write(token);
					bw.write(" ");
				}
			}
			bw.write("\n");
		}
		br.close();
		bw.close();
	}
}
