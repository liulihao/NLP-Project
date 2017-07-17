import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;

public class FeatureBuilder {

	static int TAG_NUM = 20;
	static int POS_NUM = 60;
	static int WORD_NUM = 100000;
	static int TRAIN_NUM = 100000;

	static int TOKEN_LEN = 0;
	static int POS_LEN = 3;
	static int TAG_LEN = 0;
	static boolean verbose = false;
	static boolean useList = true;
	static boolean useAppearance = true;
	

	static Map<String, String> overall1, overall2;
	static Map<String, String> taste1, taste2;
	static Map<String, String> service1, service2;
	static Map<String, String> price1, price2;
	static Map<String, String> sanitation1, sanitation2;
	static Map<String, String> location1, location2;
	
	static String overall1_corpus[];
	static String taste1_corpus[];
	static String service1_corpus[];
	static String price1_corpus[];
	static String sanitation1_corpus[];
	static String location1_corpus[];
	static String overall2_corpus[];
	static String taste2_corpus[];
	static String service2_corpus[];
	static String price2_corpus[];
	static String sanitation2_corpus[];
	static String location2_corpus[];
	
	
	public void BuildFeature(String reviewList, String reviewFeature, String folder) throws IOException {
		// read/write files
		String workdir, workdirTrain, workdirTest, flist, fout;
		// temporarily record words
		String token="", pos="", tag="", buf="", tmp="";
		StringTokenizer stk, posStk, tagStk;

		
		//////////////////////////////////////////////////////////////////////////////
		// file IO
		workdir = System.getProperty("user.dir")+"/"+folder+"/";
		workdirTrain = System.getProperty("user.dir")+"/train/";
		workdirTest = System.getProperty("user.dir")+"/test/";

		flist = workdir+reviewList;
		fout = workdir+reviewFeature;

		// open file
		BufferedReader fl= new BufferedReader(new FileReader(flist));
		BufferedWriter fw= new BufferedWriter(new FileWriter(fout));
		//////////////////////////////////////////////////////////////////////////////
		
		readList();
		
		String pos_state[] = new String [POS_NUM];
		pos_state[0] = "START"; pos_state[1] = "END";
		int pos_num = 2;
		
		String tag_state[] = new String [TAG_NUM];
		int tag_num = 0;
		
		// use hashmap to search the dictionary
		Map<String, String> map = new HashMap<String, String>();
		String corpus[] = new String [WORD_NUM];
		int token_num = 0;
		
		System.out.println("Start Building Features");
		//////////////////////////////////////////////////////////////////////////////
		
		while(fl.ready()){
	    	buf = fl.readLine();
	    	stk=new StringTokenizer(buf," \n\t");
			String restaurant="";
	    	restaurant=stk.nextToken();
	    	
	    	int r1, rn, offset, isTrain;
	    	r1 = Integer.parseInt(stk.nextToken());
	    	rn = Integer.parseInt(stk.nextToken());
	    	offset = Integer.parseInt(stk.nextToken());
	    	isTrain = Integer.parseInt(stk.nextToken());
	    	
			
			int mid;
	    	for(int i=r1;i<=rn;i+=offset){


				// record the training data
				String tokens[] = new String [TRAIN_NUM];
				int train_token_id[] = new int [TRAIN_NUM];
				int train_token_num = 0;
				
				int train_pos_id[] = new int [TRAIN_NUM];
				int train_pos_num = 0;
				
				int train_tag_id[] = new int [TRAIN_NUM];
				int train_tag_num = 0;
	    		
	    		
	    		if(isTrain == 1) workdir = workdirTrain;
	    		else	         workdir = workdirTest;
	    		
	    		String freviewTag, freviewPOS;
	    		if(isTrain == 1){
	    			freviewTag = workdir+"/tag/"+restaurant+"/"+Integer.toString(i)+"_tag.txt";
	    			freviewPOS = workdir+"/pos/"+restaurant+"/"+Integer.toString(i)+"_pos.txt";
	    		}
	    		else{
	    			freviewTag = workdir+"/"+restaurant+"/"+Integer.toString(i)+".txt";
	    			freviewPOS = workdir+"/pos/"+restaurant+"/"+Integer.toString(i)+"_pos.txt";
	    		}
	    		String freviewFeature = workdirTest+restaurant+"/"+Integer.toString(i)+"_feature.txt";
	    		
	    		BufferedReader frtag= new BufferedReader(new FileReader(freviewTag));
	    		BufferedReader frpos= new BufferedReader(new FileReader(freviewPOS));
	    		
	    		while(frpos.ready()){
	    			String posBuf = frpos.readLine();
	    	    	if(posBuf.equals("")){
	    	    		continue;
	    	    	}
	    			posStk=new StringTokenizer(posBuf," \n\t");
	    			
	    	    	String tagBuf;
	    			if(isTrain == 1){
	    				tagBuf = frtag.readLine();
		    			tagStk=new StringTokenizer(tagBuf," \n\t");
	    			}
	    			else{
	    				tagStk=new StringTokenizer(""," \n\t"); // empty initialization
	    			}
	    			
			    	try{
						token=posStk.nextToken();
						tokens[train_token_num] = token;
						pos=posStk.nextToken();
						if(isTrain == 1){
							//try{
								tag=tagStk.nextToken(); // token
								if(!tag.equals(token)){
									System.out.println(token);
									System.out.println(freviewTag);
								}
								tag=tagStk.nextToken();
							//}
							//catch(Exception e){
							//    System.out.println(freviewTag);
							//    continue;
					    	//}
						}
						
						
						// find the token's index
						// Hash Map Search
						int find_token;
						String s_find_token = (String)map.get(token);
						if(s_find_token == null){
							map.put(token, String.valueOf(token_num));
							find_token = token_num;
							++token_num;
						}
						else{
							find_token = Integer.valueOf(s_find_token);
						}
						corpus[find_token] = token;
						train_token_id[train_token_num++] = find_token;
						///////////////////////////////////
						
						// find the pos's index
						int find_pos = -1;
						for(int j=0;j<pos_num;++j){
							if(pos_state[j].equals(pos)){
								find_pos = j;
							}
						}
						if(find_pos == -1){
							pos_state[pos_num] = pos;
							find_pos = pos_num;
							++pos_num;
						}
						train_pos_id[train_pos_num++] = find_pos;
						///////////////////////////////////
						
						// find the tag's index
						if(isTrain == 1){
							int find_tag = -1;
							for(int j=0;j<tag_num;++j){
								if(tag_state[j].equals(tag)){
									find_tag = j;
								}
							}
							if(find_tag == -1){
								tag_state[tag_num] = tag;
								find_tag = tag_num;
								++tag_num;
							}
							train_tag_id[train_tag_num++] = find_tag;
						}
						///////////////////////////////////
			    	}
			    	catch(NumberFormatException e){
					    System.out.println(buf+" error");
					    continue;
			    	}
	    		} //while frpos.ready()
	    		

	    		BufferedWriter fwfeature= new BufferedWriter(new FileWriter(freviewFeature));
	    		
	    		

	    		// check NN, NNS, NNP, NNPS
	    		int m_isNN[] = new int [TRAIN_NUM];
	    		for(int j=0;j<train_token_num;++j){
	    			m_isNN[j] = 0;
	    		}
	    		for(int j=0;j<train_token_num;++j){
	    			if(pos_state[train_pos_id[j]].equals("NN") ||
    				   pos_state[train_pos_id[j]].equals("NNS") ||
    				   pos_state[train_pos_id[j]].equals("NNP") ||
    				   pos_state[train_pos_id[j]].equals("NNPS")){
	    				m_isNN[j] = 1;
	    			}
	    		}
	    		
	    		// check VB
	    		int m_isVB[] = new int [TRAIN_NUM];
	    		for(int j=0;j<train_token_num;++j){
	    			m_isVB[j] = 0;
	    		}
	    		for(int j=0;j<train_token_num;++j){
//	    			if(pos_state[train_pos_id[j]].equals("VBZ") ||
//    				   pos_state[train_pos_id[j]].equals("VBD") ||
//    				   pos_state[train_pos_id[j]].equals("VBP")){
//	    				m_isVB[j] = 1;
//	    			}
	    			if(tokens[j].equals("is") ||
	    			   tokens[j].equals("was") ||
	    			   tokens[j].equals("are") ||
	    			   tokens[j].equals("were")){
	    				m_isVB[j] = 1;
	    			}
	    		}
	    		
	    		// check JJ
	    		int m_isJJ[] = new int [TRAIN_NUM];
	    		for(int j=0;j<train_token_num;++j){
	    			m_isJJ[j] = 0;
	    		}
	    		for(int j=0;j<train_token_num;++j){
	    			if(pos_state[train_pos_id[j]].equals("JJ") ||
    				   pos_state[train_pos_id[j]].equals("JJR") ||
    				   pos_state[train_pos_id[j]].equals("JJS")){
	    				m_isJJ[j] = 1;
	    			}
	    		}
	    		
	    		// check DT
	    		int m_isDT[] = new int [TRAIN_NUM];
	    		for(int j=0;j<train_token_num;++j){
	    			m_isDT[j] = 0;
	    		}
	    		for(int j=0;j<train_token_num;++j){
	    			if(pos_state[train_pos_id[j]].equals("DT")){
	    				m_isDT[j] = 1;
	    			}
	    		}	    		
	    		
	    		// check CC
	    		int m_isCC[] = new int [TRAIN_NUM];
	    		for(int j=0;j<train_token_num;++j){
	    			m_isCC[j] = 0;
	    		}
	    		for(int j=0;j<train_token_num;++j){
	    			if(pos_state[train_pos_id[j]].equals("CC")){
	    				m_isCC[j] = 1;
	    			}
	    		}
	    		
	    		for(int j=0;j<train_token_num;++j){
	    			fw.write(
	    					tokens[j]+"\t"
	    			);
	    			fwfeature.write(
	    					tokens[j]+"\t"
	    			);
	    			
	    			
	    			if(j < train_token_num-1 && tokens[j+1].charAt(0) == '!'){
						fw.write(
								"isExclamation\t"
						);
						fwfeature.write(
								"isExclamation\t"
						);
	    			}
	    			
	    			if(useList){
	    				//boolean isTaste = false, isOverall = false, isService = false, isPrice = false, isSanitation = false, isLocation = false;
		    			String name=tokens[j].toLowerCase();
		    			
		    			

		    			String s_find_name = (String)overall1.get(name);
		    			if(!(s_find_name == null) && overall1_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isLove\t"
			    			);
		    				fwfeature.write(
			    					"isLove\t"
			    			);
		    			}
		    			s_find_name = (String)overall2.get(name);
		    			if(!(s_find_name == null) && overall2_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isHate\t"
			    			);
		    				fwfeature.write(
			    					"isHate\t"
			    			);
		    			}
		    			s_find_name = (String)taste1.get(name);
		    			if(!(s_find_name == null) && taste1_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isYummy\t"
			    			);
		    				fwfeature.write(
			    					"isYummy\t"
			    			);
		    			}
		    			s_find_name = (String)taste2.get(name);
		    			if(!(s_find_name == null) && taste2_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isGross\t"
			    			);
		    				fwfeature.write(
			    					"isGross\t"
			    			);
		    			}
		    			s_find_name = (String)service1.get(name);
		    			if(!(s_find_name == null) && service1_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isFriendly\t"
			    			);
		    				fwfeature.write(
			    					"isFriendly\t"
			    			);
		    			}
		    			s_find_name = (String)service2.get(name);
		    			if(!(s_find_name == null) && service2_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isInhospitable\t"
			    			);
		    				fwfeature.write(
			    					"isInhospitable\t"
			    			);
		    			}
		    			s_find_name = (String)price1.get(name);
		    			if(!(s_find_name == null) && price1_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isCheap\t"
			    			);
		    				fwfeature.write(
			    					"isCheap\t"
			    			);
		    			}
		    			s_find_name = (String)price2.get(name);
		    			if(!(s_find_name == null) && price2_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isExpensive\t"
			    			);
		    				fwfeature.write(
			    					"isExpensive\t"
			    			);
		    			}
		    			s_find_name = (String)sanitation1.get(name);
		    			if(!(s_find_name == null) && sanitation1_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isClean\t"
			    			);
		    				fwfeature.write(
			    					"isClean\t"
			    			);
		    			}
		    			s_find_name = (String)sanitation2.get(name);
		    			if(!(s_find_name == null) && sanitation2_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isDirty\t"
			    			);
		    				fwfeature.write(
			    					"isDirty\t"
			    			);
		    			}
		    			s_find_name = (String)location1.get(name);
		    			if(!(s_find_name == null) && location1_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isConvenient\t"
			    			);
		    				fwfeature.write(
			    					"isConvenient\t"
			    			);
		    			}
		    			s_find_name = (String)location2.get(name);
		    			if(!(s_find_name == null) && location2_corpus[Integer.valueOf(s_find_name)].equals(name)){
		    				fw.write(
			    					"isInconvenient\t"
			    			);
		    				fwfeature.write(
			    					"isInconvenient\t"
			    			);
		    			}
	    			}
	    			
	    			
	    			// token feature
	    			int len_token = TOKEN_LEN;
	    			String seq_token[] = new String [len_token];
	    			for(int k=0;k<len_token;++k){
	    				seq_token[k] = "@@";
	    			}
	    			mid = (len_token-1)/2;
	    			for(int k=0;k<len_token;++k){
	    				if((k < mid && j-mid+k >= 0) || (k > mid && j < train_token_num-(-mid+k)) || k == mid){
	    					seq_token[k] = tokens[j-mid+k];
	    				}
	    				if((k < mid && j-mid+k == -1)){
	    					seq_token[k] = "START";
	    				}
	    				if((k > mid && j == train_token_num-(-mid+k))){
	    					seq_token[k] = "END";
	    				}
	    			}
	    			for(int k=0;k<len_token;++k){
	    				//if(k == mid) continue;
	    				fw.write(
	   						     "token_seq"+k+"="+seq_token[k]+"\t"
	    						 );
	    				fwfeature.write(
	   						     "token_seq"+k+"="+seq_token[k]+"\t"
	    						 );
	    			}

	    			// pos feature
	    			int len_pos = POS_LEN;
	    			String seq_pos[] = new String [len_pos];
	    			for(int k=0;k<len_pos;++k){
	    				seq_pos[k] = "@@";
	    			}
	    			mid = (len_pos-1)/2;
	    			for(int k=0;k<len_pos;++k){
	    				if((k < mid && j-mid+k >= 0) || (k > mid && j < train_token_num-(-mid+k)) || k == mid){
	    					seq_pos[k] = pos_state[train_pos_id[j-mid+k]];
	    				}
	    				if((k < mid && j-mid+k == -1)){
	    					seq_pos[k] = "START"; 					
	    				}
	    				if(k > mid && j == train_token_num-(-mid+k)){
	    					seq_pos[k] = "END";
	    				}
	    			}
	    			for(int k=0;k<len_pos;++k){
	    				fw.write(
	   						     "pos_seq"+k+"="+seq_pos[k]+"\t"
	    						 );
	    				fwfeature.write(
	   						     "pos_seq"+k+"="+seq_pos[k]+"\t"
	    						 );
	    			}
    				
	    			// tag feature
	    			if(isTrain == 1){
		    			int len_tag = TAG_LEN;
		    			String seq_tag[] = new String [len_tag];
		    			for(int k=0;k<len_tag;++k){
		    				seq_tag[k] = "@@";
		    			}
		    			mid = (len_tag-1)/2;
		    			for(int k=0;k<len_tag;++k){
		    				if((k < mid && j-mid+k >= 0) || (k > mid && j < train_token_num-(-mid+k)) || k == mid){
		    					seq_tag[k] = tag_state[train_tag_id[j-mid+k]];
		    				}
		    				if((k < mid && j-mid+k == -1) || (k > mid && j == train_token_num-(-mid+k))){
		    					seq_tag[k] = "@@";
		    				}
		    			}
		    			for(int k=0;k<len_tag;++k){
		    				if(k == mid) continue;
		    				fw.write(
		   						     "tag_seq"+k+"="+seq_tag[k]+"\t"
		    						 );
		    				fwfeature.write(
		   						     "tag_seq"+k+"="+seq_tag[k]+"\t"
		    						 );
		    			}
	    			}
	    			
	    			
	    			// feature (NN + VB + JJ)
	    			if(j>1 &&
	    			   m_isNN[j-2] > 0 && m_isVB[j-1] > 0 && m_isJJ[j] > 0){
	    				fw.write("NNVBJJ"+"\t");
	    				fwfeature.write("NNVBJJ"+"\t");
	    			}
	    			// feature (JJ + NN)
	    			if(j < train_token_num-1 &&
	    			   m_isJJ[j] > 0 && m_isNN[j+1] > 0){
	    				fw.write("JJNN"+"\t");
	    				fwfeature.write("JJNN"+"\t");
	    			}
//	    			// feature (very + JJ)
//	    			if(j > 0 &&
//	    			   tokens[j-1].equals("very") && m_isJJ[j] > 0){
//	    				fw.write("veryJJ"+"\t");
//	    				fwfeature.write("veryJJ"+"\t");
//	    			}
	    			// feature (NN + VB + JJ)
//	    			if(j>1 &&
//	    			   tokens[j-2].equals("service") && m_isVB[j-1] > 0 && m_isJJ[j] > 0){
//	    				fw.write("serviceVBJJ"+"\t");
//	    				fwfeature.write("serviceVBJJ"+"\t");
//	    			}

	    			
	    			
	    			int len = tokens[j].length();
	    			boolean allDigits = true;
	    			boolean allCaps = true;
	    			boolean allLower = true;
	    			boolean bracket = false;
	    			if(len > 2){
	    				if(tokens[j].charAt(0) == '-' &&
						   tokens[j].charAt(len-1) == '-' &&
						   Character.isLetter(tokens[j].charAt(1)))
	    					bracket = true;
	    			}
	    			boolean hyphenated = true;
	    			boolean abbrev = false;
	    			if(len > 1){
	    				if(Character.isLetter(tokens[j].charAt(0)) &&
						   tokens[j].charAt(len-1) == '.')
	    					abbrev = true;
	    			}
	    			boolean firstCap = false;
	    			if(len > 1){
	    				if(Character.isUpperCase(tokens[j].charAt(0)) && Character.isLowerCase(tokens[j].charAt(1)))
	    					firstCap = true;
	    			}
	    			boolean noLetter = true;
	    			boolean hasDot = false;
	    			for(int k=0; k<len; ++k){
	    				char c = tokens[j].charAt(k);
	    				if(!Character.isDigit(c)) allDigits = false;
	    				if(!Character.isUpperCase(c)) allCaps = false;
	    				if(!Character.isLowerCase(c)) allLower = false;
	    				if(!(Character.isLetter(c) || c == '-')) hyphenated = false;
	    				if(Character.isLetter(c)) noLetter = false;
	    				if(c == '.') hasDot = false;
	    			}
	    			boolean hasCap = !tokens[j].toLowerCase().equals(tokens[j]);
	    			
	    			if(useAppearance){
		    			if(allDigits){
		    				fw.write("allDigits"+"\t");
		    				fwfeature.write("allDigits"+"\t");
		    			}
		    			else if(noLetter){
		    				fw.write("noLetter"+"\t");
		    				fwfeature.write("noLetter"+"\t");
		    			}
		    			else if(allCaps){
		    				fw.write("allCaps"+"\t");
		    				fwfeature.write("allCaps"+"\t");
		    			}
		    			else if(abbrev){
		    				fw.write("abbrev"+"\t");
		    				fwfeature.write("abbrev"+"\t");
		    				// for POS
		    			}
		    			else if(firstCap){
		    				fw.write("firstCap"+"\t");
		    				fwfeature.write("firstCap"+"\t");
		    			}
		    			else if(hasCap){
		    				fw.write("hasCap"+"\t");
		    				fwfeature.write("hasCap"+"\t");
		    			}
		    			else if(allLower){
		    				fw.write("allLower"+"\t");
		    				fwfeature.write("allLower"+"\t");
		    				// any mix of letters and periods counts as an abbrev
		    			}
		    			else if(hasDot){
		    				fw.write("hasDot"+"\t");
		    				fwfeature.write("hasDot"+"\t");
		    			}
		    			else if(bracket){
		    				fw.write("bracket"+"\t");
		    				fwfeature.write("bracket"+"\t");
		    			}
		    			else if(hyphenated){
		    				fw.write("hyphenated"+"\t");
		    				fwfeature.write("hyphenated"+"\t");
		    			}
		    			else{
		    				fw.write("other"+"\t");
		    				fwfeature.write("other"+"\t");
		    			}
	    			}
	    			
	    			
	    			// outcome
	    			if(isTrain == 1)
	    				fw.write(tag_state[train_tag_id[j]]);
		    		fw.newLine();
		    		fwfeature.newLine();
	    		} // for train_token_num

		    	frtag.close();
		    	frpos.close();
		    	fwfeature.close();
	    	} // for r1-rn
		} //while flist.ready()
		//////////////////////////////////////////////////////////////////////////////
		System.out.println("Finish Building Features");
		
		
		fl.close();
		fw.close();
	}
	
	public static void readList() throws IOException{
		// read/write files
		String workdir, flove, fhate, fyummy, fgross, ffri, finh, fclean, fdirty, fcheap, fexp, fcon, fincon;
		// temporarily record words
		String buf="", name="", Name="";
		StringTokenizer stk;
		
		// file IO
		workdir = System.getProperty("user.dir")+"/data/";

		flove = workdir+"love.txt";
		fhate = workdir+"hate.txt";
		fyummy = workdir+"yummy.txt";
		fgross = workdir+"gross.txt";
		ffri = workdir+"friendly.txt";
		finh = workdir+"inhospitable.txt";
		fclean = workdir+"clean.txt";
		fdirty = workdir+"dirty.txt";
		fcheap = workdir+"cheap.txt";
		fexp = workdir+"expensive.txt";
		fcon = workdir+"convenient.txt";
		fincon = workdir+"inconvenient.txt";
		

		// open file
		BufferedReader flo= new BufferedReader(new FileReader(flove));
		BufferedReader fha= new BufferedReader(new FileReader(fhate));
		BufferedReader fyu= new BufferedReader(new FileReader(fyummy));
		BufferedReader fgr= new BufferedReader(new FileReader(fgross));
		BufferedReader ffr= new BufferedReader(new FileReader(ffri));
		BufferedReader fin= new BufferedReader(new FileReader(finh));
		BufferedReader fcl= new BufferedReader(new FileReader(fclean));
		BufferedReader fdi= new BufferedReader(new FileReader(fdirty));
		BufferedReader fch= new BufferedReader(new FileReader(fcheap));
		BufferedReader fex= new BufferedReader(new FileReader(fexp));
		BufferedReader fco= new BufferedReader(new FileReader(fcon));
		BufferedReader finc= new BufferedReader(new FileReader(fincon));
		
		//////////////////////////////////////////////////////////////////////////////
		// create overall corpus
		overall1 = new HashMap<String, String>();
		overall1_corpus = new String [WORD_NUM];
		int overall_num = 0;
		while(flo.ready()){
			buf = flo.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)overall1.get(name);
				if(s_find_name == null){
					overall1.put(name, String.valueOf(overall_num));
					find_name = overall_num;
					++overall_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				overall1_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		flo.close();
		overall2 = new HashMap<String, String>();
		overall2_corpus = new String [WORD_NUM];
		overall_num = 0;
		while(fha.ready()){
			buf = fha.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)overall2.get(name);
				if(s_find_name == null){
					overall2.put(name, String.valueOf(overall_num));
					find_name = overall_num;
					++overall_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				overall2_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fha.close();
		
		
		// create taste corpus
		taste1 = new HashMap<String, String>();
		taste1_corpus = new String [WORD_NUM];
		int taste_num = 0;
		while(fyu.ready()){
			buf = fyu.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)taste1.get(name);
				if(s_find_name == null){
					taste1.put(name, String.valueOf(taste_num));
					find_name = taste_num;
					++taste_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				taste1_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fyu.close();
		taste2 = new HashMap<String, String>();
		taste2_corpus = new String [WORD_NUM];
		taste_num = 0;
		while(fgr.ready()){
			buf = fgr.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)taste2.get(name);
				if(s_find_name == null){
					taste2.put(name, String.valueOf(taste_num));
					find_name = taste_num;
					++taste_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				taste2_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fgr.close();
		
		
		// create service corpus
		service1 = new HashMap<String, String>();
		service1_corpus = new String [WORD_NUM];
		int service_num = 0;
		while(ffr.ready()){
			buf = ffr.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)service1.get(name);
				if(s_find_name == null){
					service1.put(name, String.valueOf(service_num));
					find_name = service_num;
					++service_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				service1_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		ffr.close();
		service2 = new HashMap<String, String>();
		service2_corpus = new String [WORD_NUM];
		service_num = 0;
		while(fin.ready()){
			buf = fin.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)service2.get(name);
				if(s_find_name == null){
					service2.put(name, String.valueOf(service_num));
					find_name = service_num;
					++service_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				service2_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fin.close();
		
		
		// create price corpus
		price1 = new HashMap<String, String>();
		price1_corpus = new String [WORD_NUM];
		int price_num = 0;
		while(fch.ready()){
			buf = fch.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)price1.get(name);
				if(s_find_name == null){
					price1.put(name, String.valueOf(price_num));
					find_name = price_num;
					++price_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				price1_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fch.close();
		price2 = new HashMap<String, String>();
		price2_corpus = new String [WORD_NUM];
		price_num = 0;
		while(fex.ready()){
			buf = fex.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)price2.get(name);
				if(s_find_name == null){
					price2.put(name, String.valueOf(price_num));
					find_name = price_num;
					++price_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				price2_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fex.close();
		
		
		// create sanitation corpus
		sanitation1 = new HashMap<String, String>();
		sanitation1_corpus = new String [WORD_NUM];
		int sanitation_num = 0;
		while(fcl.ready()){
			buf = fcl.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)sanitation1.get(name);
				if(s_find_name == null){
					sanitation1.put(name, String.valueOf(sanitation_num));
					find_name = sanitation_num;
					++sanitation_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				sanitation1_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fcl.close();
		sanitation2 = new HashMap<String, String>();
		sanitation2_corpus = new String [WORD_NUM];
		sanitation_num = 0;
		while(fdi.ready()){
			buf = fdi.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)sanitation2.get(name);
				if(s_find_name == null){
					sanitation2.put(name, String.valueOf(sanitation_num));
					find_name = sanitation_num;
					++sanitation_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				sanitation2_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fdi.close();
		
		// create location corpus
		location1 = new HashMap<String, String>();
		location1_corpus = new String [WORD_NUM];
		int location_num = 0;
		while(fco.ready()){
			buf = fco.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)location1.get(name);
				if(s_find_name == null){
					location1.put(name, String.valueOf(location_num));
					find_name = location_num;
					++location_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				location1_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		fco.close();
		location2 = new HashMap<String, String>();
		location2_corpus = new String [WORD_NUM];
		location_num = 0;
		while(finc.ready()){
			buf = finc.readLine();
			stk=new StringTokenizer(buf,"\n");
			try{
				Name=stk.nextToken();
				String tmp=Name.toLowerCase();
				//name = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
				name = tmp;
				
				int find_name = -1;
				String s_find_name = (String)location2.get(name);
				if(s_find_name == null){
					location2.put(name, String.valueOf(location_num));
					find_name = location_num;
					++location_num;
				}
				else{
					find_name = Integer.valueOf(s_find_name);
				}
				location2_corpus[find_name] = name;
			}
			catch(NumberFormatException e){
				System.out.println(buf+" error");
				continue;
			}
		}
		finc.close();
	}
}
