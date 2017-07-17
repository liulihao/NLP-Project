import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class Score {
	
	static int TAG_NUM = 20;
	
    public static void main (String[] args) throws IOException {
    	compute("evalList.txt");
    }
    
    public static void compute(String evalList) throws IOException {

		try {
			//////////////////////////////////////////////////////////////////////////////
			// file IO
			StringTokenizer stk;
			String buf, workdir, flist, summary, responseFileName, tagFileName;
			
			workdir = System.getProperty("user.dir");
			flist = System.getProperty("user.dir")+"/result/"+evalList;

			// open file
			BufferedReader fl= new BufferedReader(new FileReader(flist));
			//////////////////////////////////////////////////////////////////////////////
			
			while(fl.ready()){
		    	buf = fl.readLine();
		    	stk=new StringTokenizer(buf," \n\t");
				String restaurant="";
		    	restaurant=stk.nextToken();
				System.out.println("Start Reading Restaurant "+restaurant);
		    	
		    	int r1, rn, offset, isF;
		    	r1 = Integer.parseInt(stk.nextToken());
		    	rn = Integer.parseInt(stk.nextToken());
		    	offset = Integer.parseInt(stk.nextToken());
		    	isF = Integer.parseInt(stk.nextToken());
		
		    	summary = workdir+"/data/"+restaurant+"_summary.txt";
				BufferedReader fr= new BufferedReader(new FileReader(summary));
		    	double rating = 0, total=0;
		    	
		    	while(fr.ready()){
		    		buf = fr.readLine();
		    		String[] split = buf.split(",");
		    		rating += Double.parseDouble(split[2]);
		    		++total;
		    	}
		    	fr.close();
		    	System.out.printf ("  average rating from YELP: %8.2f\n", rating/total);
		
				double correct = 0, incorrect = 0;
				double s_correct[] = new double [TAG_NUM];
				double s_incorrect[] = new double [TAG_NUM];
				double tp[] = new double [TAG_NUM]; 
				double tn[] = new double [TAG_NUM]; 
				double fp[] = new double [TAG_NUM]; 
				double fn[] = new double [TAG_NUM];
				for(int c=0;c<TAG_NUM;++c){
					s_correct[c] = s_incorrect[c] = tp[c] = tn[c] = fp[c] = fn[c] = 0;
				}
				double precision = 0;
				double recall = 0;
				double F = 0;
				
				String state[] = new String[TAG_NUM];
				state[0] = "Overall";
				state[1] = "Taste";
				state[2] = "Service";
				state[3] = "Price";
				state[4] = "Sanitation";
				state[5] = "Location";
				state[6] = "Other";
				
				String tag_state[] = new String [TAG_NUM];
				tag_state[0] = "LOVE";  	 tag_state[1] = "HATE"; // overall
				tag_state[2] = "YUMMY"; 	 tag_state[3] = "GROSS"; // taste
				tag_state[4] = "FRIENDLY"; 	 tag_state[5] = "INHOSPITABLE"; // service
				tag_state[6] = "CHEAP"; 	 tag_state[7] = "EXPENSIVE"; // price
				tag_state[8] = "CLEAN"; 	 tag_state[9] = "DIRTY"; // sanitation
				tag_state[10] = "CONVENIENT";tag_state[11] = "INCONVENIENT"; // location
				tag_state[12] = "OTHER";
				int tag_num = 13;
				
				double category[] = new double[13];
				for(int i=0;i<13;++i) category[i] = 0;
				
				for(int f=r1;f<=rn;f+=offset){

					responseFileName = workdir+"/result/"+restaurant+"/"+f+"_response.txt";
					File responseFile = new File(responseFileName);
					List<String> response = Files.readAllLines(responseFile.toPath(), StandardCharsets.UTF_8);
					List<String> tag = response;
					if(isF == 1){
						tagFileName = workdir+"/tag/"+restaurant+"/"+f+"_tag.txt";
						File tagFile = new File(tagFileName);
						tag = Files.readAllLines(tagFile.toPath(), StandardCharsets.UTF_8);

					    //System.out.println(tagFileName);
					}
					
					
					for(int i=0;i<response.size();++i){
						
					    String responseLine = response.get(i).trim();
					    if (responseLine.equals("")) {
					    	continue;
					    }
					    String[] responseFields = responseLine.split("\t");
					    String responseToken = responseFields[0];
					    String responseTag = responseFields[1];
					    
					    int find_tag = -1;
					    for(int j=0;j<tag_num;++j){
					    	if(responseTag.equals(tag_state[j])){
					    		find_tag = j;
					    		break;
					    	}
					    }
					    if(find_tag != -1){
					    	++category[find_tag];
					    }
					    

					    if(isF == 1){
						    String tagLine = tag.get(i).trim();
						    //System.out.println(tag);
						    String[] tagFields = tagLine.split(" ");
						    String tagToken = tagFields[0];
						    String tagTag = tagFields[1];
						    //System.out.println(tagToken);
						    //System.out.println(tagTag);
						    
						    if (!tagToken.equals(responseToken)) {
								System.err.println ("token mismatch at: " + restaurant + " " + f + " " + i);
								System.exit(1);
						    }

					    	//System.out.printf("token %s tag %s response %s\n", tagToken, tagTag, responseTag);
						    // precision & recall
						    for(int c=0;c<6;++c){
							    if((tagTag.equals(tag_state[2*c]) && responseTag.equals(tag_state[2*c])) ||
							       (tagTag.equals(tag_state[2*c]) && responseTag.equals(tag_state[2*c+1])) ||
							       (tagTag.equals(tag_state[2*c+1]) && responseTag.equals(tag_state[2*c])) ||
							       (tagTag.equals(tag_state[2*c+1]) && responseTag.equals(tag_state[2*c+1]))){
							    	
							    	if(tagTag.equals(tag_state[2*c]) && responseTag.equals(tag_state[2*c])){
								    	++tp[c];
								    }
								    else if(tagTag.equals(tag_state[2*c+1]) && responseTag.equals(tag_state[2*c+1])){
								    	++tn[c];
								    }
								    else if(tagTag.equals(tag_state[2*c]) && responseTag.equals(tag_state[2*c+1])){
								    	++fn[c];
								    }
								    else{
								    	++fp[c];
								    }
							    }
						    }
						    for(int c=0;c<12;++c){
						    	if(c==0 && restaurant.equals("tim-ho-wan-new-york-2") && tagTag.equals("LOVE")) 
						    		System.out.printf("token %s  tag %s  response %s  %d %d\n", tagToken, tagTag, responseTag, f, i);
						    	if(tagTag.equalsIgnoreCase(tag_state[c])){
						    		if(responseTag.equalsIgnoreCase(tagTag))
						    			++s_correct[c];
						    		else
								    	++s_incorrect[c];
						    	}
						    }
						    
						    
						    if(tagTag.equals("OTHER") && responseTag.equals("OTHER")){
						    	++tp[6];
						    }
						    else if(!tagTag.equals("OTHER") && !responseTag.equals("OTHER")){
						    	++tn[6];
						    }
						    else if(tagTag.equals("OTHER") && !responseTag.equals("OTHER")){
						    	++fn[6];
						    }
						    else{
						    	++fp[6];
						    }
						    
						    
						    if (responseTag.equals(tagTag)) {
						    	correct = correct + 1;
						    } else {
						    	incorrect = incorrect + 1;
						    }
					    }
					}
				}
				
				for(int i=0;i<6;++i){
					if(category[2*i]+category[2*i+1] > 0){
						double score;
						if(category[2*i] >= category[2*i+1]){
							score = category[2*i]/(category[2*i]+category[2*i+1])*5.0f;
							System.out.printf ("  %s: %s %8.2f\n", state[i], tag_state[2*i], score);
						}
						else{
							score = category[2*i+1]/(category[2*i]+category[2*i+1])*5.0f;
							System.out.printf ("  %s: %s %8.2f\n", state[i], tag_state[2*i+1], score);
						}
					}
					else{
						System.out.printf ("  %s: NA\n", state[i]);
					}
				}
				
				if(isF == 1){
					System.out.println (correct + " out of " + (correct + incorrect) + " tags correct");
					double accuracy = (double) 100.0 * correct / (correct + incorrect);
					System.out.printf ("  accuracy: %8.2f\n", accuracy);
					
					double total_precision = 0;
					double total_recall = 0;
					double total_F = 0;
					for(int c=0;c<6;++c){
						precision = tp[c]/(tp[c]+fp[c]);
						total_precision += precision;
						recall = tp[c]/(tp[c]+fn[c]);
						total_recall += recall;
						F = 2*precision*recall/(precision+recall);
						System.out.printf ("  %s\n", state[c]);
						System.out.printf ("  precision: %8.2f\n", precision*100);
						System.out.printf ("  recall: %8.2f\n", recall*100);
						System.out.printf ("  F-score: %8.2f\n\n", F*100);

						//System.out.printf ("  %s\n", state[c]);
						//System.out.println (s_correct[c] + " out of " + (s_correct[c] + s_incorrect[c]) + " tags correct");
						//accuracy = (double) 100.0 * s_correct[c] / (s_correct[c] + s_incorrect[c]);
						//System.out.printf ("  accuracy: %8.2f\n", accuracy);
					}
					for(int c=0;c<12;++c){
						System.out.printf ("  %s\n", tag_state[c]);
						System.out.println ("\t\t"+s_correct[c] + " out of " + (s_correct[c] + s_incorrect[c]) + " tags correct");
						accuracy = (double) 100.0 * s_correct[c] / (s_correct[c] + s_incorrect[c]);
						System.out.printf ("  \taccuracy: %8.2f\n", accuracy);
					}
					
					total_precision /= 6.0f;
					total_recall /= 6;
					total_F = 2*total_precision*total_recall/(total_precision+total_recall);
					
					System.out.printf ("  \tprecision: %8.2f\n", total_precision*100);
					System.out.printf ("  \trecall: %8.2f\n", total_recall*100);
					System.out.printf ("  \tF-score: %8.2f\n\n", total_F*100);
				}
				System.out.println();
			}
			fl.close();
		}
		catch (Exception e) {
		    System.out.println(e);
		}
    }
}
