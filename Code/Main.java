/***********************************************************
Natural Language Processing
Term Project

Instuctor: Prof. Ralph Grishman 
Student: Ching-Hsiang Hsu, Li-Hao Liu
Create: Apr. 11 2017

Update: Apr. 18 2017
        parse CSV files
        fix !!! bug
        fix 4, bug
        fix starting only a single word
        
***********************************************************/

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;

public class Main {
	public static void main(String args[]) throws IOException {
		ParseCSV p = new ParseCSV();
		//p.readCSV("club-a-steakhouse-new-york");
		//p.readCSV("keens-steakhouse-new-york");
		//p.readCSV("peter-luger-brooklyn-2");
		
		p.readCSV("burger-and-lobster-new-york");
		p.readCSV("din-tai-fung-seattle");
		p.readCSV("asian-jewels-seafood-restaurant-flushing");
		p.readCSV("porter-house-bar-and-grill-new-york");
		p.readCSV("pho-vietnam-87-new-york-2");
		
		
		FeatureBuilder f = new FeatureBuilder();
		f.BuildFeature("reviewList.txt", "reviewFeature.txt", "train");
		f.BuildFeature("testList.txt", "testFeature.txt", "test");
	}
}
