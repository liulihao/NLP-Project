// Wrapper for maximum-entropy tagging

// NYU - Natural Language Processing - Prof. Grishman

// invoke by:  java  MEtag dataFile  model  responseFile

import java.io.*;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import opennlp.maxent.*;
import opennlp.maxent.io.*;

// reads line with tab separated features
// writes feature[0] (token) and predicted tag

public class MEtag {
	
	static int WORD_NUM = 100000;
	static int TEST_NUM = 1000;
	static int TAG_NUM = 20;
	
	static double ZERO = -1000000;
	static double TRAN_PARAMETER = 0;
	static double EMIS_PARAMETER = 0;
	
	static boolean useViterbi = false;
	
    public static void main (String[] args) throws IOException {
    	tagging("reviewList.txt", "testList.txt", "modelTrain.txt");
    }
    
    public static void tagging(String trainList, String testList, String modelFile) throws IOException {		

    	
	    // read/write files
		String workdir, flist, fmodel;
		// temporarily record words
		String token="", buf="", tmp="", tag="";
		StringTokenizer stk, tagStk, reviewStk;
		
		
		//////////////////////////////////////////////////////////////////////////////
		// initialization
		String tag_state[] = new String [TAG_NUM];
		tag_state[0] = "LOVE";  	 tag_state[1] = "HATE"; // overall
		tag_state[2] = "YUMMY"; 	 tag_state[3] = "GROSS"; // taste
		tag_state[4] = "FRIENDLY"; 	 tag_state[5] = "INHOSPITABLE"; // service
		tag_state[6] = "CHEAP"; 	 tag_state[7] = "EXPENSIVE"; // price
		tag_state[8] = "CLEAN"; 	 tag_state[9] = "DIRTY"; // sanitation
		tag_state[10] = "CONVENIENT";tag_state[11] = "INCONVENIENT"; // location
		tag_state[12] = "OTHER";     tag_state[13] = "START";
		tag_state[14] = "END";
		int tag_num = 13;
		
		
		double transition[][]=new double [TAG_NUM][TAG_NUM];
		double emission[][]=new double [TAG_NUM][WORD_NUM];
		double p_start[]=new double [TAG_NUM];
		double total[]=new double [TAG_NUM];
		for(int i=0;i<TAG_NUM;++i){
			for(int j=0;j<TAG_NUM;++j){
				transition[i][j] = ZERO;
			}
			for(int j=0;j<WORD_NUM;++j){
				emission[i][j] = ZERO;
			}
			p_start[i] = 0;
			total[i] = 0;
		}
		int start_state = 13;
		int end_state = 14;
		p_start[start_state] = 1;
		

		// open training files
		flist = System.getProperty("user.dir")+"/train/"+trainList;
		BufferedReader fll= new BufferedReader(new FileReader(flist));
		
		while(fll.ready()){
	    	buf = fll.readLine();
	    	stk=new StringTokenizer(buf," \n\t");
			String restaurant="", start="", end="";
	    	restaurant=stk.nextToken();
	    	start=stk.nextToken();
	    	end=stk.nextToken();
	    	tmp=stk.nextToken();
	    	
	    	int r1, rn;
	    	r1 = Integer.parseInt(start);
	    	rn = Integer.parseInt(end);
	    	
			for(int i=r1;i<=rn;i+=2){
	    		
	    		String freviewTag;
    			freviewTag = System.getProperty("user.dir")+"/tag/"+restaurant+"/"+Integer.toString(i)+"_tag.txt";
	    		
	    		BufferedReader frtag= new BufferedReader(new FileReader(freviewTag));

		    	int pre_tag = 0;
	    		while(frtag.ready()){
	    			String tagBuf = frtag.readLine();
	    	    	if(tagBuf.equals("")){
	    	    		continue;
	    	    	}
	    	    	tagStk=new StringTokenizer(tagBuf," \n\t");
	    			
			    	try{
						token=tagStk.nextToken(); // token
						tag=tagStk.nextToken();   // tag
						
						// find the tag's index
						int find_tag = -1;
						for(int j=0;j<tag_num;++j){
							if(tag_state[j].equals(tag)){
								find_tag = j;
							}
						}
						
						if(pre_tag >= 0){
							++transition[pre_tag][find_tag];
							++total[find_tag];
						}
						pre_tag = find_tag;
						///////////////////////////////////
			    	}
			    	catch(NumberFormatException e){
					    System.out.println(buf+" error");
					    continue;
			    	}
	    		} //while frpos.ready()
	    		++total[end_state];
	    		++transition[pre_tag][end_state];

		    	frtag.close();
	    	} // for r1-rn
		}
		fll.close();
		
		for(int i=0;i<TAG_NUM;++i){
			for(int j=0;j<TAG_NUM;++j){
				if(transition[i][j] > 0 && total[j] > 0){
					transition[i][j] = Math.log10(transition[i][j])-Math.log10(total[j])+TRAN_PARAMETER;
					//transition[i][j] = transition[i][j]/total[j];
				}
				else{
					transition[i][j] = ZERO;
					//transition[i][j] = 0;
				}
			}
		}
		
		
		try {
			//////////////////////////////////////////////////////////////////////////////
			// file IO
			flist = System.getProperty("user.dir")+"/test/"+testList;
			fmodel = System.getProperty("user.dir")+"/train/"+modelFile;

			// open file
			BufferedReader fl= new BufferedReader(new FileReader(flist));
		    GISModel m = (GISModel) new SuffixSensitiveGISModelReader(new File(fmodel)).getModel();
			//////////////////////////////////////////////////////////////////////////////
			
			while(fl.ready()){
		    	buf = fl.readLine();
		    	stk=new StringTokenizer(buf," \n\t");
				String restaurant="", start="", end="";
		    	restaurant=stk.nextToken();
		    	start=stk.nextToken();
		    	end=stk.nextToken();
		    	tmp=stk.nextToken();
		    

				System.out.println("Start Tagging Restaurant "+restaurant);
		    	
		    	int r1, rn, offset;
		    	r1 = Integer.parseInt(start);
		    	rn = Integer.parseInt(end);
				offset = Integer.parseInt(tmp);
		    	
		    	for(int r=r1;r<=rn;r+=offset){
		    		
		    		String ftest = System.getProperty("user.dir")+"/test/"+restaurant+"/"+Integer.toString(r)+"_feature.txt";
		    		String fout = System.getProperty("user.dir")+"/result/"+restaurant+"/"+Integer.toString(r)+"_response.txt";
		    		
		    		BufferedReader ft= new BufferedReader(new FileReader(ftest));
				    PrintWriter response = new PrintWriter(new FileWriter(fout));		    		
		    			
				    String priorTag = "#";
				    String line;
				    
				    String testword[] = new String [TEST_NUM];
				    int num_word = 0;
				    while(ft.ready() && (line = ft.readLine()) != null){
						if(line.equals("")){
							continue;
						}
						else{
						    line = line.replaceAll("@@", Matcher.quoteReplacement(priorTag));
						    String[] features = line.split("\t");
						    tag = m.getBestOutcome(m.eval(features));
						    
						    if(!useViterbi){
							    response.println(features[0] + "\t" + tag);
							    priorTag = tag;
						    }
						    else{
						    	token="";
							    buf="";
							    String tt[] = new String[TAG_NUM];
							    String pp[] = new String[TAG_NUM];
							    
							    String[] f = line.split("\t");
							    buf = m.getAllOutcomes(m.eval(f));
							    //System.out.println(buf);
							    
							    
							    stk=new StringTokenizer(buf," []");		    
							    for(int i=0;i<tag_num && stk.hasMoreTokens();++i){
								    token=stk.nextToken(); // tag
								    tt[i] = token;
								    token=stk.nextToken(); // p
								    pp[i] = token;
							    }
								
							    for(int i=0;i<tag_num;++i){
							    	int find_tag = -1;
							    	float p = 0;
							    	if(pp[i] != null){
							    		for(int j=0;j<tag_num;++j){
							    			if(tt[i].equals(tag_state[j])){
							    				find_tag = j;
							    				p = Float.parseFloat(pp[i]);
							    				break;
							    			}
							    		}
							    		if(find_tag >= 0){
									    	if(p > 1e-8)
									    		emission[find_tag][num_word] = Math.log10(p)+EMIS_PARAMETER;
									    	else
									    		emission[find_tag][num_word] = ZERO;
							    		}
							    		else{
							    			System.out.println("Wrong tag "+tt[i]);
							    		}
							    	}
							    }
							    testword[num_word] = features[0];
							    ++num_word;
						    }
						}
				    }
				    
				    if(!useViterbi){
						response.println();
						priorTag = "#";
					}
					else{
						if(num_word > 0){
						    int trace[][] = new int [TEST_NUM][TAG_NUM];
				    		double p[][] = new double [TEST_NUM][TAG_NUM];
				    		for(int i=0;i<num_word;++i){
				    			for(int j=0;j<tag_num;++j){
				    				trace[i][j] = 0;
				    				p[i][j] = 0;
				    			}
				    		}
						    
						    
							//////////////////////////////////////////////////////////////////////////////
							// Viterbi algorithm
							// first round
							// 0 means START state
							for(int cur=0;cur<tag_num;++cur){
								p[0][cur] = transition[start_state][cur]+emission[cur][0];
								trace[0][cur] = start_state;
							}
							
							// main loop
							for(int j=1;j<num_word;++j){
								for(int cur=0;cur<tag_num;++cur){
									int pre = 0; // Start state
									int p_id = 0;
									double p_max;
									
									p_max = p[j-1][pre]+transition[pre][cur]+emission[cur][j];
							
									for(pre=1;pre<tag_num;++pre){
										double p_tmp;
										p_tmp = p[j-1][pre]+transition[pre][cur]+emission[cur][j];
									
										if(p_max < p_tmp){
											p_max = p_tmp;
											p_id = pre;
										}
										//System.out.printf("%s %s %f tra %f emi %f\n", tag_state[pre], tag_state[cur], p_tmp, transition[pre][cur], emission[cur][j]);
									}
									//System.out.printf("max %f\n", p_max);
							
									p[j][cur] = p_max;
									trace[j][cur] = p_id;
								}
							}
							//////////////////////////////////////////////////////////////////////////////
						    
							//////////////////////////////////////////////////////////////////////////////
							// backtrace states
							int p_id = 0;
							double p_max;
							p_max = p[num_word-1][0]+transition[0][end_state];
							for(int pre=1;pre<tag_num;++pre){
								if(p_max < p[num_word-1][pre]+transition[pre][end_state]){
									p_max = p[num_word-1][pre]+transition[pre][end_state];
									p_id = pre;
								}
							}
							
							int testtag_id[] = new int [TEST_NUM];
							testtag_id[num_word-1] = p_id;
							for(int i=num_word-2;i>=0;--i){
								testtag_id[i] = trace[i+1][testtag_id[i+1]];
							}
							for(int i=0;i<num_word;++i){
								response.println(testword[i]+"\t"+tag_state[testtag_id[i]]);
							}
							response.println();
						    System.out.printf("Finish running Viterbi!\n", fout);
							
							//////////////////////////////////////////////////////////////////////////////
							
						    num_word = 0;
					    }
					    else{
					    	response.println();
					    }
					}
				    
				    ft.close();
				    response.close();
				    System.out.printf("Finish tagging %s!\n", fout);
	    		}
			}
			fl.close();
		} catch (Exception e) {
		    System.out.print("Error in data tagging: ");
		    e.printStackTrace();
		}
    }
}
