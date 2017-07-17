import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;

public class ParseCSV {
	public void readCSV(String s) throws IOException {
		// read/write files
		String workdir, fin_name, fout_name, folder_name, sub_file_name;
		// temporarily record words
		String buf="", author="", rating="", date="", review="";
		StringTokenizer stk;
		
		// file IO
		workdir = System.getProperty("user.dir")+"/data/";
		fin_name = workdir+s+".csv";
		fout_name = workdir+s+"_summary.txt";
		folder_name = workdir+s;

		BufferedReader fin= new BufferedReader(new FileReader(fin_name));
		PrintWriter fout = new PrintWriter (new FileWriter (fout_name));
		File folder = new File(folder_name);
		folder.mkdir();
		
		// read first line
		buf = fin.readLine();
		System.out.println("Start Parsing: "+s);
		
		
		// create review corpus
		//review_map = new HashMap<String, String>();
		//review_corpus = new String [WORD_NUM];
		int review_num = 1;
		while(fin.ready()){
			buf = fin.readLine();
			if(buf.equals("")) continue;
			try{
				
				String[] split = buf.split(",");
				
				author=split[0];
				stk=new StringTokenizer(author,"\"");
				author = stk.nextToken();
				rating=split[1];
				stk=new StringTokenizer(rating,"\"");
				rating = stk.nextToken();
				date=split[2];
				stk=new StringTokenizer(date,"\"");
				date = stk.nextToken();
				//System.out.println("buf: "+buf);
				//System.out.println("author: "+author);
				//System.out.println("rating: "+rating);
				//System.out.println("date: "+date);
				//System.out.println("split number: "+split.length);
				
				fout.println(review_num+","+author+","+rating+","+date);

				
				sub_file_name = folder+"/"+Integer.toString(review_num)+".txt";
				PrintWriter sub_fout = new PrintWriter (new FileWriter (sub_file_name));
				
				review = split[3];
				for(int i=4;i<split.length;++i)
					review = review + ","+ split[i];
				String[] review_split = review.split(" ");
				
				if(review_split.length <= 1){
					if(review.substring(review.length()-1).equals("\"")){
						parseSubReview(review_split[0].substring(1,review_split[0].length()-1), sub_fout);
					}
					else{
						parseSubReview(review_split[0].substring(1), sub_fout);
					}
				}
				else{
					if(review_split.length >= 0){
						parseSubReview(review_split[0].substring(1), sub_fout);
					}
					for(int i=1;i<review_split.length-1;++i){
						parseSubReview(review_split[i], sub_fout);
					}
					if(review.substring(review.length()-1).equals("\"")){
						parseSubReview(review_split[review_split.length-1].substring(0, review_split[review_split.length-1].length()-1), sub_fout);
					}
					else{
						parseSubReview(review_split[review_split.length-1], sub_fout);
					}
				}
				
				while((!review.substring(review.length()-1).equals("\"") || review.substring(review.length()-2).equals("\"\"")) 
						&& fin.ready()){
					buf = fin.readLine();
					if(buf.equals("")) continue;
					review=buf;
					//System.out.println(review);
					review_split = review.split(" ");
					if(review.substring(review.length()-1).equals("\"") && !review.substring(review.length()-2).equals("\"\"")){
						for(int i=0;i<review_split.length-1;++i){
							parseSubReview(review_split[i], sub_fout);
						}
						parseSubReview(review_split[review_split.length-1].substring(0, review_split[review_split.length-1].length()-1), sub_fout);
					}
					else{
						for(int i=0;i<review_split.length;++i){
							parseSubReview(review_split[i], sub_fout);
						}
					}
				}
				sub_fout.println();
				sub_fout.close();
				++review_num;
				
				//System.out.println(test.length);
				//for(int x=0;x<test.length;++x){
				//System.out.println(test[x]);
				//}			
				
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fin.close();
		fout.close();
	}
	

	public static void parseSubReview(String review_split, PrintWriter sub_fout) {
		
		int len = review_split.length();
		
		
		char last1=' ';
		if(len-1 >= 0) last1 = review_split.charAt(len-1);
		
		if((int)last1 == 8203){ //special space
			review_split = review_split.substring(0, review_split.length()-1);
			len = review_split.length();
			if(len-1 >= 0) last1 = review_split.charAt(len-1);
		}
		
		char last2=' ';
		if(len-2 >= 0) last2 = review_split.charAt(len-2);
		char last3=' '; 
		if(len-3 >= 0) last3 = review_split.charAt(len-3);

		if(len == 1){
			sub_fout.println(review_split);
		}
		else{
			if(len-1 >= 0 && !Character.isLetter(last1)){
				int not_letter_len = 0;
				for(int i=len-1;i>=0;--i){
					char tmp = review_split.charAt(i);
					if(Character.isLetter(tmp) || Character.isDigit(tmp)){
						break;
					}
					++not_letter_len;
				}
				parseQuote(review_split.substring(0, len-not_letter_len), sub_fout);
				sub_fout.println(review_split.substring(len-not_letter_len, len));
			}
			else if(len-2 >= 0 && Character.isLetter(last1) && !Character.isLetter(last2)){
				parseQuote(review_split.substring(0, len-2), sub_fout);
				parseQuote(review_split.substring(len-2, len), sub_fout);
			}
			else if(len-3 >= 0 && Character.isLetter(last1) && Character.isLetter(last2) && !Character.isLetter(last3)){
				parseQuote(review_split.substring(0, len-3), sub_fout);
				parseQuote(review_split.substring(len-3, len), sub_fout);
			}
			else{
				parseQuote(review_split, sub_fout);
			}
		}
	}
	
	public static void parseQuote(String review_split, PrintWriter sub_fout) {
		
		int len = review_split.length();
		boolean isFirstQuote = false;
		boolean isLastQuote = false;
		char first1 = ' ';
		if(len > 0) first1 = review_split.charAt(0);
		char first2 = ' ';
		if(len > 1) first2 = review_split.charAt(1);
		char last1 = ' ';
		if(len-1 >= 0) last1 = review_split.charAt(len-1);
		char last2 = ' ';
		if(len-2 >= 0) last2 = review_split.charAt(len-2);
		if(first1 == '\"' && first2 == '\"'){
			isFirstQuote = true;
		}
		if(last1 == '\"' && last2 == '\"'){
			isLastQuote = true;
		}
		
		if(isFirstQuote && isLastQuote){
			if(len > 2){
				sub_fout.println(review_split.substring(0, 1));
				parseBracket(review_split.substring(2, len-2), sub_fout);
				sub_fout.println(review_split.substring(len-1, len));
			}
			else{
				sub_fout.println(review_split.substring(0, 1));
				sub_fout.println(review_split.substring(len-1, len));
			}
		}
		else if(isFirstQuote && !isLastQuote){
			sub_fout.println(review_split.substring(0, 1));
			parseBracket(review_split.substring(2, len), sub_fout);
		}
		else if(!isFirstQuote && isLastQuote){
			parseBracket(review_split.substring(0, len-2), sub_fout);
			sub_fout.println(review_split.substring(len-1, len));
		}
		else{
			parseBracket(review_split, sub_fout);
		}
	}
	
	public static void parseBracket(String review_split, PrintWriter sub_fout) {
		
		int len = review_split.length();
		boolean isFirstBracket = false;
		boolean isLastBracket = false;
		char first = ' ';
		if(len > 0) first = review_split.charAt(0);
		char last = ' ';
		if(len-1 >= 0) last = review_split.charAt(len-1);
		if(first == '('){
			isFirstBracket = true;
		}
		if(last == ')'){
			isLastBracket = true;
		}
		
		if(isFirstBracket && isLastBracket){
			sub_fout.println(review_split.substring(0, 1));
			sub_fout.println(review_split.substring(1, len-1));
			sub_fout.println(review_split.substring(len-1, len));
		}
		else if(isFirstBracket && !isLastBracket){
			sub_fout.println(review_split.substring(0, 1));
			sub_fout.println(review_split.substring(1, len));
		}
		else if(!isFirstBracket && isLastBracket){
			sub_fout.println(review_split.substring(0, len-1));
			sub_fout.println(review_split.substring(len-1, len));
		}
		else {
			if(len > 0) sub_fout.println(review_split);
		}
	}
}
