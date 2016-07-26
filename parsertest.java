package test_parser;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.*;


import java.io.FileInputStream;   
import java.io.InputStream;
import java.io.InputStreamReader;



public class parsertest {
	
	public static void main(String[] args) throws IOException {
		
	    String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	    if (args.length > 0) {
	      parserModel = args[0];
	    }
	    LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
	    //getpairs("replaceit_summerpalace.txt",lp);
	    //fileprocess("summer_palace.txt","summerpalace.txt");
	    if (args.length == 0) {
	      demoAPI(lp);
	    } else {
	      String textFile = (args.length > 1) ? args[1] : args[0];
	      //demoDP(lp, textFile);
	    }
	  }
	
	  //提取phrase
	  //只考虑由两个、三个词组成的词组，更多的不考虑
	  //进行提取时，大小写分开考虑，对于所有词都是大写的词组，说明是专有名词，如果其在数据集中出现次数超过2，则判定为词组
	  //进行提取时，大小写分开考虑，对于所有词都是小写的词组，如果其在数据集中出现次数超过3，则判定为词组。如果第一个词是大写，后两个词是小写，则先将第一个词转换为小写词（考虑词组位于句首的情况）
	  //基本方法是，对每句话进行tagger，只提取所有词都是名词的词组。其他情况暂时没有考虑。
	  public static void getpairs(String filename,LexicalizedParser lp) throws IOException{
		  InputStream in = new FileInputStream(filename);
		  InputStreamReader reader = new InputStreamReader(in,"UTF-8");
		  BufferedReader br = new BufferedReader(reader);
		  String str;
		  StanfordLemmatizer slem = new StanfordLemmatizer();
		  HashMap<String,Integer> dic= new HashMap<String,Integer>();
		  HashMap<String,Integer> dic_lower = new HashMap<String,Integer>();
		  MaxentTagger tagger = new MaxentTagger("lib/tagger/taggers/english-left3words-distsim.tagger");
		  int count = 0;
		  while((str = br.readLine())!=null){
			  System.out.println(count++);
			  String[] review = str.split("\t")[3].split("\\.");
			  for(int i=0;i<review.length;i++){
			      String tagged = tagger.tagString(review[i]);
				  String[] split = tagged.split(" ");
				  for(int j=0;j<split.length-2;j++){
					  if(!split[j].split("_")[0].equals(",")&&!split[j+1].split("_")[0].equals(",")&&!split[j+2].split("_")[0].equals(",")){
						  //三个词，都是大写
						  if(!Character.isLowerCase(split[j].toCharArray()[0])&&!Character.isLowerCase(split[j+1].toCharArray()[0])&&!Character.isLowerCase(split[j+2].toCharArray()[0])){
							  //词性都是名词
							  if((split[j].split("_")[1].contains("NN")&&split[j+1].split("_")[1].contains("NN")&&split[j+2].split("_")[1].contains("NN"))){//名词词组
								  String phrase = split[j].split("_")[0].replace("[", "").replace("]", "") + "_" + split[j+1].split("_")[0].replace("[", "").replace("]", "") + "_" + split[j+2].split("_")[0].replace("[", "").replace("]", "");
								  //进行计数
								  if(dic.containsKey(phrase)){
									  dic.put(phrase, dic.get(phrase) + 1);
								  }
								  else
									  dic.put(phrase, 1);
							  }
						  }
					  }

					  if(!split[j].split("_")[0].equals(",")&&!split[j+1].split("_")[0].equals(",")&&!split[j+2].split("_")[0].equals(",")){
						  //都是小写
						  if(Character.isLowerCase(split[j].toCharArray()[0])&&Character.isLowerCase(split[j+1].toCharArray()[0])&&Character.isLowerCase(split[j+2].toCharArray()[0])){
							  if((split[j].split("_")[1].contains("NN")&&split[j+1].split("_")[1].contains("NN")&&split[j+2].split("_")[1].contains("NN"))){//名词词组
								  String phrase = slem.lemmatize(split[j].split("_")[0]).toString().replace("[", "").replace("]", "") + "_" + slem.lemmatize(split[j+1].split("_")[0]).toString().replace("[", "").replace("]", "") + "_" + slem.lemmatize(split[j+2].split("_")[0]).toString().replace("[", "").replace("]", "");
								  if(dic_lower.containsKey(phrase)){
									  dic_lower.put(phrase, dic_lower.get(phrase) + 1);
								  }
								  else
									  dic_lower.put(phrase, 1);
							  }
						  }
						  //第一个是大写，第二个是小写
						  else if(!Character.isLowerCase(split[j].toCharArray()[0])&&Character.isLowerCase(split[j+1].toCharArray()[0])&&Character.isLowerCase(split[j+2].toCharArray()[0])){
							  if((split[j].split("_")[1].contains("NN")&&split[j+1].split("_")[1].contains("NN")&&split[j+2].split("_")[1].contains("NN"))){//名词词组
								  String phrase = slem.lemmatize(split[j].split("_")[0]).toString().replace("[", "").replace("]", "") + "_" + slem.lemmatize(split[j+1].split("_")[0]).toString().replace("[", "").replace("]", "") + "_" + slem.lemmatize(split[j+2].split("_")[0]).toString().replace("[", "").replace("]", "");
								  if(dic_lower.containsKey(phrase)){
									  dic_lower.put(phrase, dic_lower.get(phrase) + 1);
								  }
								  else
									  dic_lower.put(phrase, 1);
							  }
						  }
					  }
				  }
				  
				  //两个词的情况
				  for(int j=0;j<split.length-1;j++){
					  if(!split[j].split("_")[0].equals(",")&&!split[j+1].split("_")[0].equals(",")){
						  if(!Character.isLowerCase(split[j].toCharArray()[0])&&!Character.isLowerCase(split[j+1].toCharArray()[0])){
							  if((split[j].split("_")[1].contains("NN")&&split[j+1].split("_")[1].contains("NN"))){//名词词组
								  String phrase = split[j].split("_")[0].replace("[", "").replace("]", "") + "_" + split[j+1].split("_")[0].replace("[", "").replace("]", "");
								  if(dic.containsKey(phrase)){
									  dic.put(phrase, dic.get(phrase) + 1);
								  }
								  else
									  dic.put(phrase, 1);
							  }
						  }
						  if(Character.isLowerCase(split[j].toCharArray()[0])&&Character.isLowerCase(split[j+1].toCharArray()[0])){
							  if((split[j].split("_")[1].contains("NN")&&split[j+1].split("_")[1].contains("NN"))){//名词词组
								  String phrase = slem.lemmatize(split[j].split("_")[0]).toString().replace("[", "").replace("]", "") + "_" + slem.lemmatize(split[j+1].split("_")[0]).toString().replace("[", "").replace("]", "");
								  if(dic_lower.containsKey(phrase)){
									  dic_lower.put(phrase, dic_lower.get(phrase) + 1);
								  }
								  else
									  dic_lower.put(phrase, 1);
							  }
						  }
						  else if(!Character.isLowerCase(split[j].toCharArray()[0])&&Character.isLowerCase(split[j+1].toCharArray()[0])){
							  if((split[j].split("_")[1].contains("NN")&&split[j+1].split("_")[1].contains("NN"))){//名词词组
								  String phrase = slem.lemmatize(split[j].split("_")[0]).toString().replace("[", "").replace("]", "") + "_" + slem.lemmatize(split[j+1].split("_")[0]).toString().replace("[", "").replace("]", "");
								  if(dic_lower.containsKey(phrase)){
									  dic_lower.put(phrase, dic_lower.get(phrase) + 1);
								  }
								  else
									  dic_lower.put(phrase, 1);
							  }
						  }
					  }
				  }
			  }
		  }
		  
		  List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(dic.entrySet()); 
			
		  Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {     
		      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		          return (int)(o2.getValue() - o1.getValue());
		      }  
		  }); 
		  
		  List<Map.Entry<String, Integer>> entryList_lower = new ArrayList<Map.Entry<String, Integer>>(dic_lower.entrySet()); 
			
		  Collections.sort(entryList_lower, new Comparator<Map.Entry<String, Integer>>() {     
		      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		          return (int)(o2.getValue() - o1.getValue());
		      }  
		  }); 
		  
		  //排序后输出
		  String filename2 = "phrase.txt";
		  FileWriter fw = new FileWriter(filename2);
		  String filename3 = "phrase_lower.txt";
		  FileWriter fw2 = new FileWriter(filename3);
		  for(int i=0;i<entryList.size();i++){
			  fw.write(entryList.get(i).getKey()+" " + entryList.get(i).getValue()+"\r\n");
		  }
		  for(int i=0;i<entryList_lower.size();i++){
			  fw2.write(entryList_lower.get(i).getKey()+" " + entryList_lower.get(i).getValue()+"\r\n");
		  }
		  fw.close();
		  fw2.close();
		  
	  }

	  //将提取的词组间加下划线，对文档进行二次处理
	  public static void fileprocess(String filename,String filename2) throws IOException{
		  InputStream in = new FileInputStream("phrase.txt");
		  InputStreamReader reader = new InputStreamReader(in,"UTF-8");
		  BufferedReader br = new BufferedReader(reader);
		  String str;
		  HashMap<String,String> phrase = new HashMap<String,String>();
		  StanfordLemmatizer slem = new StanfordLemmatizer();
		  while((str = br.readLine())!=null){
			  if(Integer.valueOf(str.split(" ")[1]).intValue()>1){
				  phrase.put(str.split(" ")[0], "");
			  }
		  }
		  br.close();
		  in.close();
		  reader.close();
		  
		  in = new FileInputStream("phrase_lower.txt");
		  reader = new InputStreamReader(in,"UTF-8");
		  br = new BufferedReader(reader);
		  while((str = br.readLine())!=null){
			  if(Integer.valueOf(str.split(" ")[1]).intValue()>2){
				  phrase.put(str.split(" ")[0], "");
			  }
		  }
		  br.close();
		  in.close();
		  reader.close();
		  
		  in = new FileInputStream(filename);
		  reader = new InputStreamReader(in,"UTF-8");
		  br = new BufferedReader(reader);
		  FileWriter fw = new FileWriter(filename2);
		  while((str = br.readLine())!=null){
			  fw.write(str.split("\t")[0]+"\t"+str.split("\t")[1]+"\t"+str.split("\t")[2]+"\t");
			  String[] review = str.split("\t")[3].split("\\.");
			  for(int i=0;i<review.length;i++){
				  String[] split = review[i].split(" ");
				  for(int k=0;k<split.length;k++){
					  if(split[k].length()>0){
						  if(k<split.length-2&&!Character.isLowerCase(split[k].toCharArray()[0])&&!Character.isLowerCase(split[k+1].toCharArray()[0])&&!Character.isLowerCase(split[k+2].toCharArray()[0])){
							  String phr = split[k]+"_"+split[k+1]+"_"+split[k+2].replace(",", "");
							  if(phrase.containsKey(phr)){
								  fw.write(phr+" ");
								  k = k + 2;
								  continue;
							  }
						  }
						  else if(k<split.length-2&&Character.isLowerCase(split[k].toCharArray()[0])&&Character.isLowerCase(split[k+1].toCharArray()[0])&&Character.isLowerCase(split[k+2].toCharArray()[0])){
							  String phr = slem.lemmatize(split[k]).toString().replace("[", "").replace("]", "")+"_"+slem.lemmatize(split[k+1]).toString().replace("[", "").replace("]", "")+"_"+slem.lemmatize(split[k+2].replace(",", "")).toString().replace("[", "").replace("]", "");
							  if(phrase.containsKey(phr)){
								  fw.write(phr+" ");
								  k = k + 2;
								  continue;
							  }
						  }
						  else if(k<split.length-2&&!Character.isLowerCase(split[k].toCharArray()[0])){
							  String phr = slem.lemmatize(split[k]).toString().replace("[", "").replace("]", "")+"_"+slem.lemmatize(split[k+1]).toString().replace("[", "").replace("]", "")+"_"+slem.lemmatize(split[k+2].replace(",", "")).toString().replace("[", "").replace("]", "");
							  if(phrase.containsKey(phr)){
								  fw.write(phr+" ");
								  k = k + 2;
								  continue;
							  }
						  }
						  if(k<split.length-1&&!Character.isLowerCase(split[k].toCharArray()[0])&&!Character.isLowerCase(split[k+1].toCharArray()[0])){
							  String phr = split[k]+"_"+split[k+1].replace(",", "");
							  if(phrase.containsKey(phr)){
								  fw.write(phr+" ");
								  k = k + 1;
							  }
							  else fw.write(split[k]+" ");
						  }
						  else if(k<split.length-1&&Character.isLowerCase(split[k].toCharArray()[0])&&Character.isLowerCase(split[k+1].toCharArray()[0])){
							  String phr = slem.lemmatize(split[k]).toString().replace("[", "").replace("]", "")+"_"+slem.lemmatize(split[k+1].replace(",", "")).toString().replace("[", "").replace("]", "");
							  if(phrase.containsKey(phr)){
								  fw.write(phr+" ");
								  k = k + 1;
							  }
							  else fw.write(split[k]+" ");
						  }
						  else if(k<split.length-1&&!Character.isLowerCase(split[k].toCharArray()[0])){
							  String phr = slem.lemmatize(split[k]).toString().replace("[", "").replace("]", "")+"_"+slem.lemmatize(split[k+1].replace(",", "")).toString().replace("[", "").replace("]", "");
							  if(phrase.containsKey(phr)){
								  fw.write(phr+" ");
								  k = k + 1;
							  }
							  else fw.write(split[k]+" ");
						  }
						  else fw.write(split[k]+" ");
					  }
				   }
				  fw.write(".");
			  }
			  fw.write("\r\n");
		  }
		  
		  fw.close();
		  br.close();
		  in.close();
		  reader.close();	  
	  }
	  /**
	   * demoDP demonstrates turning a file into tokens and then parse
	   * trees.  Note that the trees are printed by calling pennPrint on
	   * the Tree object.  It is also possible to pass a PrintWriter to
	   * pennPrint if you want to capture the output.
	   * This code will work with any supported language.
	   */
	  public static void demoDP(LexicalizedParser lp, String filename) {
	    // This option shows loading, sentence-segmenting and tokenizing
	    // a file using DocumentPreprocessor.
	    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
	    GrammaticalStructureFactory gsf = null;
	    if (tlp.supportsGrammaticalStructures()) {
	      gsf = tlp.grammaticalStructureFactory();
	    }
	    // You could also create a tokenizer here (as below) and pass it
	    // to DocumentPreprocessor
	    for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
	      Tree parse = lp.apply(sentence);
	      parse.pennPrint();
	      System.out.println();

	      if (gsf != null) {
	        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	        Collection tdl = gs.typedDependenciesCCprocessed();
	        System.out.println(tdl);
	        System.out.println();
	      }
	    }
	  }

	  /**
	   * demoAPI demonstrates other ways of calling the parser with
	   * already tokenized text, or in some cases, raw text that needs to
	   * be tokenized as a single sentence.  Output is handled with a
	   * TreePrint object.  Note that the options used when creating the
	   * TreePrint can determine what results to print out.  Once again,
	   * one can capture the output by passing a PrintWriter to
	   * TreePrint.printTree. This code is for English.
	 * @throws IOException 
	 * @throws BiffException 
	   */
	  
	  
	  
	  //进行Pair的提取，基本步骤是，对句子进行Parser，利用dependencies的结果（stanford parser online上有展示），提取有用的词语依赖关系
	  //现在提取的两种依赖关系是：
	  //1.amod：例：Summer_Palace is a beautiful place. 这种直接修饰，beautiful和place之间，就是amod关系
	  //2.nsubj:例：Summer_Palace is beautiful. 这种通过be动词修饰，beautiful和summer_palace之间，是nsubj关系
	  //2.nsubj:例：Summer_palace is a beautiful place. Summer_Palace和place之间，也是nsubj关系，以此进行名词间替代性的分析，以及进行形容词修饰的传递
	  public static void demoAPI(LexicalizedParser lp) throws IOException {
	    // This option shows parsing a list of correctly tokenized words
	    /*String[] sent = { "This", "is", "an", "easy", "sentence", "." };
	    List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
	    Tree parse = lp.apply(rawWords);
	    parse.pennPrint();
	    System.out.println();*/

	    // This option shows loading and using an explicit tokenizer
		String destination = "summer_palace";
		String filename = "replaceit_summerpalace";
		InputStream instream = new FileInputStream(filename+".txt");
		InputStreamReader reader = new InputStreamReader(instream,"UTF-8");
		BufferedReader br = new BufferedReader(reader);
		
		HashMap<String,Integer>amod = new HashMap<String,Integer>();
		HashMap<String,Integer>nsubj = new HashMap<String,Integer>();
		HashMap<String,Integer>all = new HashMap<String,Integer>();
		
		HashMap<String,Integer>amod_target = new HashMap<String,Integer>();
		HashMap<String,Integer>nsubj_target = new HashMap<String,Integer>();
		
		HashMap<String,Integer>n_n = new HashMap<String,Integer>();
		HashMap<String,Integer>n_n_modifier = new HashMap<String,Integer>();
		
		StanfordLemmatizer slem = new StanfordLemmatizer();
		
		System.out.println(slem.lemmatize("crowded"));
		FileWriter fwnn = new FileWriter("day.txt");
		
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");

	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    Annotation annotation;
	    
	    HashMap<String,String>replace = new HashMap<String,String>();
	    replace.put("place", "summer_palace");
	    replace.put("area", "summer_palace");
	    replace.put("park", "summer_palace");
	    replace.put("attraction", "summer_palace");
	    replace.put("palace", "summer_palace");
	    replace.put("garden", "summer_palace");
	    List<Map.Entry<String, String>> replacelist = new ArrayList<Map.Entry<String, String>>(replace.entrySet()); 
	    

		String str_line="";
		int count = 0;
		while((str_line=br.readLine()) != null){
			count++;
			System.out.println(count); 
			if(str_line.split("\t").length>3){
				annotation = new Annotation(str_line.split("\t")[3]);
			    pipeline.annotate(annotation);
			    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			    
				TokenizerFactory<CoreLabel> tokenizerFactory =
				        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	
				for(int p=0;p<sentences.size();p++){//按句划分
					String str = sentences.get(p).toString();
				    Tokenizer<CoreLabel> tok =
				        tokenizerFactory.getTokenizer(new StringReader(str));
				    List<CoreLabel> rawWords2 = tok.tokenize();
				    Tree parse = lp.apply(rawWords2);
			
				    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
				    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
				    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
				    
				    TypedDependency tempdep;
				    List<String> target_list = new ArrayList<String>();
				    List<String> modifier_list = new ArrayList<String>();
				    List<String> mode_list = new ArrayList<String>();
				    
				    List<String> target_list_pro = new ArrayList<String>();//传递用的
				    List<String> modifier_list_pro = new ArrayList<String>();
				    
				    HashMap<String,String> migrate = new HashMap<String,String>();
				    for(int j=0;j<tdl.size();j++){
				    	tempdep = tdl.get(j);
				    	if(tempdep.toString().indexOf("+")==-1){
					    	if(tempdep.reln().toString()=="amod"){//amod关系
					    		//只考虑存在形容词情况下的amod关系. dep和gov分别是pair中的两个词,dep是形容词
					    		if((tempdep.dep().toString().indexOf("JJ")!=-1)||(tempdep.dep().toString().indexOf("JJR")!=-1)||(tempdep.dep().toString().indexOf("JJS")!=-1)){
					    			if((tempdep.gov().toString().indexOf("PRP")==-1)&&(tempdep.gov().toString().indexOf("PRP$")==-1)&&(tempdep.gov().toString().indexOf("WDT")==-1)&&(tempdep.dep().toString().indexOf("DT")==-1)&&(tempdep.dep().toString().indexOf("FW")==-1)){
					    				if(modifier_list_pro.contains(tempdep.toString().split(",")[0].split("\\(")[1])){ //传递的情况
					    					String add_amod = "";
								    		String str_gov = target_list_pro.get(modifier_list_pro.indexOf(tempdep.toString().split(",")[0].split("\\(")[1])).split("-")[0].replace(" ","").toLowerCase();
								    		String str_dep = tempdep.dep().toString().split("/")[0].toLowerCase();
								    		String str_1 = str_dep;
								    		String str_2 = slem.lemmatize(str_gov).toString().replace("[","").replace("]","");
								    		String str_3 = slem.lemmatize(tempdep.toString().split(",")[0].split("\\(")[1]).toString().split("-")[0].replace("[", "");
								    		//存下str1+str3,当存在传递的情况时，比如place，要把place相关的pair减去
								    		String delete = str_1+"+"+str_3;
								    		target_list.add(tempdep.toString().split(",")[0].split("\\(")[1]);
								    		modifier_list.add(tempdep.toString().split(",")[1].split("\\)")[0].replace(" ", ""));
								    		mode_list.add("amod");
								    		if(str_1.length()>1&&str_2.length()>1&&str_1.indexOf("!")==-1&&str_2.indexOf("?")==-1&&str_2.indexOf("!")==-1&&str_1.indexOf("?")==-1&&str_2.indexOf("+")==-1&&str_1.indexOf("+")==-1){
									    		add_amod = str_1+"+"+str_2;
									    		if(amod_target.containsKey(str_2)){
									    			int temp = amod_target.get(str_2);
									    			amod_target.put(str_2, temp+1);
									    		}
									    		else
									    			amod_target.put(str_2, 1);
									    		if(amod.containsKey(add_amod)){
									    			int temp = amod.get(add_amod);
									    			amod.put(add_amod, temp+1);//包含此二元关系，增加次数
									    		}
									    		else{
									    			amod.put(add_amod, 1);
									    		}
									    		
									    		if(all.containsKey(add_amod)){
									    			int temp = all.get(add_amod);
									    			all.put(add_amod, temp+1);//包含此二元关系，增加次数
									    		}
									    		else{
									    			all.put(add_amod, 1);
									    		}
									    		
									    		if(amod.containsKey(delete)){
									    			if(amod.get(delete)>1){
									    				amod.put(delete, amod.get(delete)-1);
									    				amod_target.put(str_3, amod_target.get(str_3)-1);
									    			}
									    			else if(amod_target.containsKey(str_3)){
									    				if(amod_target.get(str_3)>1){
										    				amod.remove(delete);
										    				amod_target.put(str_3, amod_target.get(str_3)-1);
									    				}
									    				else{
										    				amod.remove(delete);
										    				amod_target.remove(str_3);
										    			}
									    			}
									    		}
									    		
									    	}
					    				}
					    				else{
								    		String add_amod = "";
								    		String str_gov = tempdep.gov().toString().split("/")[0].toLowerCase();
								    		String str_dep = tempdep.dep().toString().split("/")[0].toLowerCase();
								    		String str_1 = str_dep;
								    		String str_2 = slem.lemmatize(str_gov).toString().replace("[","").replace("]","");
								    		target_list.add(tempdep.toString().split(",")[0].split("\\(")[1]);
								    		modifier_list.add(tempdep.toString().split(",")[1].split("\\)")[0].replace(" ", ""));
								    		mode_list.add("amod");
								    		if(str_1.length()>1&&str_2.length()>1&&str_1.indexOf("!")==-1&&str_2.indexOf("?")==-1&&str_2.indexOf("!")==-1&&str_1.indexOf("?")==-1&&str_2.indexOf("+")==-1&&str_1.indexOf("+")==-1){
								    			//处理place
									    		if(replace.containsKey(str_2)){
									    			if(!migrate.containsKey(str_2)){
									    				migrate.put(str_2, str_1+"/amod ");
									    			}
									    			else{
									    				migrate.put(str_2, migrate.get(str_2)+str_1+"/amod ");
									    			}
									    		}
									    		else{
										    		add_amod = str_1+"+"+str_2;
										    		if(amod_target.containsKey(str_2)){
										    			int temp = amod_target.get(str_2);
										    			amod_target.put(str_2, temp+1);
										    		}
										    		else
										    			amod_target.put(str_2, 1);
										    		if(amod.containsKey(add_amod)){
										    			int temp = amod.get(add_amod);
										    			amod.put(add_amod, temp+1);//包含此二元关系，增加次数
										    		}
										    		else{
										    			amod.put(add_amod, 1);
										    		}
										    		
										    		if(all.containsKey(add_amod)){
										    			int temp = all.get(add_amod);
										    			all.put(add_amod, temp+1);//包含此二元关系，增加次数
										    		}
										    		else{
										    			all.put(add_amod, 1);
										    		}
									    		}	
									    	}
					    				}
					    			}
					    		}
					    	}
					    	
					    	if(tempdep.reln().toString()=="nsubj"){
					    		if((tempdep.gov().toString().indexOf("JJ")!=-1)||(tempdep.gov().toString().indexOf("JJR")!=-1)||(tempdep.gov().toString().indexOf("JJS")!=-1)){
					    			if((tempdep.dep().toString().indexOf("PRP")==-1)&&(tempdep.dep().toString().indexOf("PRP$")==-1)&&(tempdep.dep().toString().indexOf("WDT")==-1)&&(tempdep.dep().toString().indexOf("DT")==-1)&&(tempdep.dep().toString().indexOf("FW")==-1)){
							    		String add_nsubj = "";
							    		String str_gov = tempdep.gov().toString().split("/")[0].toLowerCase();
							    		String str_dep = tempdep.dep().toString().split("/")[0].toLowerCase();
							    		String str_1 = str_gov;
							    		String str_2 = slem.lemmatize(str_dep).toString().replace("[","").replace("]","");
							    		modifier_list.add(tempdep.toString().split(",")[0].split("\\(")[1]);
							    		target_list.add(tempdep.toString().split(",")[1].split("\\)")[0].replace(" ", ""));
							    		mode_list.add("nsubj");
							    		if(str_1.length()>1&&str_2.length()>1&&str_1.indexOf("!")==-1&&str_2.indexOf("?")==-1&&str_2.indexOf("!")==-1&&str_1.indexOf("?")==-1&&str_2.indexOf("+")==-1&&str_1.indexOf("+")==-1){
							    			//处理place
								    		if(replace.containsKey(str_2)){
								    			if(!migrate.containsKey(str_2)){
								    				migrate.put(str_2, str_1+"/nsubj ");
								    			}
								    			else{
								    				migrate.put(str_2, migrate.get(str_2)+str_1+"/nsubj ");
								    			}
								    		}
								    		else{
									    		add_nsubj = str_1+"+"+str_2;
									    		if(nsubj_target.containsKey(str_2)){
									    			int temp = nsubj_target.get(str_2);
									    			nsubj_target.put(str_2, temp+1);
									    		}
									    		else
									    			nsubj_target.put(str_2, 1);
									    		if(nsubj.containsKey(add_nsubj)){
									    			int temp = nsubj.get(add_nsubj);
									    			nsubj.put(add_nsubj, temp+1);//包含此二元关系，增加次数
									    		}
									    		else{
									    			nsubj.put(add_nsubj, 1);
									    		}
									    		
									    		if(all.containsKey(add_nsubj)){
									    			int temp = all.get(add_nsubj);
									    			all.put(add_nsubj, temp+1);//包含此二元关系，增加次数
									    		}
									    		else{
									    			all.put(add_nsubj, 1);
									    		}
								    		}	
							    		}
					    			}
					    		}
					    		
					    		//替代的情况
					    		if((tempdep.gov().toString().indexOf("NN")!=-1)&&tempdep.dep().toString().indexOf("NN")!=-1){
					    			target_list_pro.add(tempdep.toString().split(",")[1].replace(")", ""));
					    			modifier_list_pro.add(tempdep.toString().split(",")[0].split("\\(")[1]);
					    			String modi = tempdep.toString().split(",")[0].split("\\(")[1].split("-")[0];
					    			String targ = tempdep.toString().split(",")[1].replace(")", "").split("-")[0].replace(" ", "");
					    			if(targ.contains("day")||modi.contains("day")){
					    				fwnn.write(str+"\r\n");
					    			}
					    			String saven_n = modi+"+"+targ;
					    			if(n_n.containsKey(saven_n)){
					    				n_n.put(saven_n, n_n.get(saven_n)+1);
					    			}
					    			else n_n.put(saven_n, 1);
					    			if(n_n_modifier.containsKey(modi)){
					    				n_n_modifier.put(modi, n_n_modifier.get(modi)+1);
					    			}
					    			else n_n_modifier.put(modi,1);
					    		}
					    	}
					    	
					    	if(tempdep.reln().toString().indexOf("conj")!=-1){//出现连词情况
					    		if(tempdep.gov().toString().indexOf("JJ")!=-1&&tempdep.dep().toString().indexOf("JJ")!=-1){//两个形容词修饰同一个对象
					    			String str_1 = tempdep.toString().split(",")[0].split("\\(")[1];
					    			String str_2 = tempdep.toString().split(",")[1].split("\\)")[0].replace(" ","");
					    			if(str_2.length()>1&&str_1.length()>1&&str_2.indexOf("!")==-1&&str_1.indexOf("!")==-1&&str_2.indexOf("?")==-1&&str_1.indexOf("?")==-1&&str_2.indexOf("+")==-1&&str_1.indexOf("+")==-1){
					    				str_2 = str_2.toLowerCase();
					    				str_1 = str_1.toLowerCase();
						    			for(int m=0;m<modifier_list.size();m++){//对本句话之前出现的所有修饰词
						    				if(modifier_list.get(m).indexOf(str_1)!=-1&&modifier_list.contains(str_2)){//找到相应的修饰词修饰的对象，把另一个修饰词与该对象的搭配存入二元组
						    					String target = target_list.get(m).split("-")[0];//找到该形容词修饰的对象
						    					//处理place
						    					if(!replace.containsKey(target)){
							    					if(mode_list.get(m).equals("amod")){
							    						String save = str_2.split("-")[0]+"+"+target;
							    						if(!amod.containsKey(save))
							    							amod.put(save, 1);
							    						else
							    							amod.put(save, amod.get(save)+1);
							    					}
							    					else{
							    						String save = str_2.split("-")[0]+"+"+target;
							    						if(!nsubj.containsKey(save))
							    							nsubj.put(save, 1);
							    						else
							    							nsubj.put(save, nsubj.get(save)+1);
							    					}
						    					}
						    					
						    				}
						    				else if(modifier_list.get(m).indexOf(str_2)!=-1&&modifier_list.contains(str_1)){
						    					String target = target_list.get(m).split("-")[0];//找到该形容词修饰的对象
						    					//处理place
						    					if(!replace.containsKey(target)){
							    					if(mode_list.get(m).equals("amod")){
							    						String save = str_1.split("-")[0]+"+"+target;
							    						if(!amod.containsKey(save))
							    							amod.put(save, 1);
							    						else
							    							amod.put(save, amod.get(save)+1);
							    					}
							    					else{
							    						String save = str_1.split("-")[0]+"+"+target;
							    						if(!nsubj.containsKey(save))
							    							nsubj.put(save, 1);
							    						else
							    							nsubj.put(save, nsubj.get(save)+1);
							    					}
						    					}
						    				}
						    			}
					    			}
					    		}
					    		else if(tempdep.gov().toString().indexOf("NN")!=-1&&tempdep.dep().toString().indexOf("NN")!=-1){//名词被同一个形容词修饰
					    			String str_1 = tempdep.toString().split(",")[0].split("\\(")[1];
					    			String str_2 = tempdep.toString().split(",")[1].split("\\)")[0].replace(" ","");
					    			if(str_2.indexOf("!")==-1&&str_1.indexOf("!")==-1&&str_2.indexOf("?")==-1&&str_1.indexOf("?")==-1&&str_2.indexOf("+")==-1&&str_1.indexOf("+")==-1){
					    				str_2 = str_2.toLowerCase();
					    				str_1 = str_1.toLowerCase();
						    			for(int m=0;m<target_list.size();m++){
						    				if(target_list.get(m).indexOf(str_1)!=-1&&!target_list.contains(str_2)){
						    					String modifier = modifier_list.get(m).split("-")[0];
						    					//处理place
						    					if(!replace.containsKey(str_2)){
							    					if(mode_list.get(m).equals("amod")&&str_2.split("-")[0].length()>1){
							    						String save = modifier+"+"+str_2.split("-")[0];
							    						if(!amod.containsKey(save))
							    							amod.put(save, 1);
							    						else
							    							amod.put(save, amod.get(save)+1);
							    					}
							    					else if(str_2.split("-")[0].length()>1){
							    						String save = modifier+"+"+str_2.split("-")[0];
							    						if(!amod.containsKey(save))
							    							amod.put(save, 1);
							    						else
							    							amod.put(save, amod.get(save)+1);
							    					}
						    					}
						    				}
						    				else if(target_list.get(m).indexOf(str_2)!=-1&&!target_list.contains(str_1)){
						    					String modifier = modifier_list.get(m).split("-")[0];
						    					if(!replace.containsKey(str_1)){
							    					if(mode_list.get(m).equals("amod")&&str_1.split("-")[0].length()>1){
							    						String save = modifier+"+"+str_1.split("-")[0];
							    						if(!amod.containsKey(save))
							    							amod.put(save, 1);
							    						else
							    							amod.put(save, amod.get(save)+1);
							    					}
							    					else if(str_1.split("-")[0].length()>1){
							    						String save = modifier+"+"+str_1.split("-")[0];
							    						if(!amod.containsKey(save))
							    							amod.put(save, 1);
							    						else
							    							amod.put(save, amod.get(save)+1);
							    					}
						    					}
						    				}
						    			}
					    			}
					    		}
					    	}
				    	}
				    }
				    List<Integer> flag_nn_list = new ArrayList<Integer>();
				    for(int l=0;l<replacelist.size();l++){
				    	String temp = replacelist.get(l).getKey();
				    	if(str.indexOf(temp)!=-1&&str.indexOf(temp)==str.lastIndexOf(temp)&&target_list_pro.size()==0&&migrate.containsKey(temp))
				    		flag_nn_list.add(l);
				    }
				    if(flag_nn_list.size()>0){
				    	for(int nn=0;nn<flag_nn_list.size();nn++){
				    		int flag_nn = flag_nn_list.get(nn);
					    	String concept = replacelist.get(flag_nn).getKey();
					    	String[] modifier = migrate.get(concept).split(" ");
					    	for(int l=0;l<modifier.length;l++){
					    		if(modifier[l].contains("amod")){ 
					    			String savepairs = modifier[l].split("/")[0]+"+"+replace.get(concept);
					    			if(amod_target.containsKey(replace.get(concept))){
					    				amod_target.put(replace.get(concept), amod_target.get(replace.get(concept))+1);
					    			}
					    			else amod_target.put(replace.get(concept),1);
					    			if(amod.containsKey(savepairs)){
					    				amod.put(savepairs, amod.get(savepairs)+1);
					    			}
					    			else
					    				amod.put(savepairs, 1);
					    		}
					    		else{
					    			String savepairs = modifier[l].split("/")[0]+"+"+replace.get(concept);
					    			if(nsubj_target.containsKey(replace.get(concept))){
					    				nsubj_target.put(replace.get(concept), nsubj_target.get(replace.get(concept))+1);
					    			}
					    			else nsubj_target.put(replace.get(concept),1);
					    			if(nsubj.containsKey(savepairs)){
					    				nsubj.put(savepairs, nsubj.get(savepairs)+1);
					    			}
					    			else
					    				nsubj.put(savepairs, 1);
					    		}
					    	}
				    	}
				    }
				}
			}
		}
		
		List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(amod.entrySet()); 
		
		Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); 
		
		List<Map.Entry<String, Integer>> entryList_nsubj = new ArrayList<Map.Entry<String, Integer>>(nsubj.entrySet()); 
		
		Collections.sort(entryList_nsubj, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); 
		
		List<Map.Entry<String, Integer>> amod_target_rank = new ArrayList<Map.Entry<String, Integer>>(amod_target.entrySet()); 
		
		Collections.sort(amod_target_rank, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); 
		
		List<Map.Entry<String, Integer>> nsubj_target_rank = new ArrayList<Map.Entry<String, Integer>>(nsubj_target.entrySet()); 
		
		Collections.sort(nsubj_target_rank, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); 
		
		List<Map.Entry<String, Integer>> n_n_rank = new ArrayList<Map.Entry<String, Integer>>(n_n.entrySet()); 
		
		Collections.sort(n_n_rank, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); 
		
		List<Map.Entry<String, Integer>> n_n_modi_rank = new ArrayList<Map.Entry<String, Integer>>(n_n_modifier.entrySet()); 
		
		Collections.sort(n_n_modi_rank, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); 
		/*List<Map.Entry<String, Integer>> entryList_all = new ArrayList<Map.Entry<String, Integer>>(all.entrySet()); 
		
		Collections.sort(entryList_all, new Comparator<Map.Entry<String, Integer>>() {     
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {        
		        return (int)(o2.getValue() - o1.getValue());
		    }  
		}); */
		
		br.close();
		reader.close();
		instream.close();
		
		HashMap<String,String> pair = new HashMap<String,String>();
		for(int i=0;i<entryList_nsubj.size();i++){
			if(entryList_nsubj.get(i).getKey().split("\\+").length>1){
				String key = entryList_nsubj.get(i).getKey().split("\\+")[1];
				int times = entryList_nsubj.get(i).getValue();
				String value = entryList_nsubj.get(i).getKey().split("\\+")[0] + "+" + String.valueOf(times) +"\t";
				if(!pair.containsKey(key)){
					pair.put(key,value);
				} 
				else{
					String newvalue = pair.get(key) + value;
					pair.put(key, newvalue);
				}	
			}
		}
		
		String filename_nsubj = filename+"_nsubj_pair.txt";
		FileWriter writer_nsubj=new FileWriter(filename_nsubj);
		for(int i=0;i<nsubj_target_rank.size();i++){
			System.out.println(i);
			String key = nsubj_target_rank.get(i).getKey().toString();
			String val = pair.get(key);
			String[] value = val.split("\t");
			for(int j=0;j<value.length;j++){
				String modifier = value[j].split("\\+")[0];
				String times = value[j].split("\\+")[1];
				writer_nsubj.write(key+"+"+modifier+" "+times+"\r\n");
			}
		}
		
		writer_nsubj.close();
		
		
		HashMap<String,String> pair_amod = new HashMap<String,String>();
		for(int i=0;i<entryList.size();i++){
			System.out.println(i);
			if(entryList.get(i).getKey().split("\\+").length>1){
				String key = entryList.get(i).getKey().split("\\+")[1];
				int times = entryList.get(i).getValue();
				String value = entryList.get(i).getKey().split("\\+")[0] + "+" + String.valueOf(times) +"\t";
				if(!pair_amod.containsKey(key)){
					pair_amod.put(key,value);
				}
				else{
					String newvalue = pair_amod.get(key) + value;
					pair_amod.put(key, newvalue);
				}	
			}
		}
		
		String filename_amod = filename+"_amod_pair.txt";
		FileWriter writer_amod=new FileWriter(filename_amod);
		for(int i=0;i<amod_target_rank.size();i++){
			System.out.println(i);
			String key = amod_target_rank.get(i).getKey().toString();
			String val = pair_amod.get(key);
			String[] value = val.split("\t");
			for(int j=0;j<value.length;j++){
				String modifier = value[j].split("\\+")[0];
				String times = value[j].split("\\+")[1];
				writer_amod.write(key+"+"+modifier+" "+times+"\r\n");
			}
		}
		
		writer_amod.close();
		
		HashMap<String,String> pair_n_n = new HashMap<String,String>();
		for(int i=0;i<n_n_rank.size();i++){
			String key = n_n_rank.get(i).getKey().toString().split("\\+")[0];
			int times = n_n_rank.get(i).getValue();
			String value = n_n_rank.get(i).getKey().split("\\+")[1] + "+" + String.valueOf(times) +"\t";
			if(!pair_n_n.containsKey(key)){
				pair_n_n.put(key,value);
			}
			else{
				String newvalue = pair_n_n.get(key) + value;
				pair_n_n.put(key, newvalue);
			}	
		}
		
		String filename_nn = filename+"_nn.txt";
		FileWriter writer_nn=new FileWriter(filename_nn);
		for(int i=0;i<n_n_modi_rank.size();i++){
			System.out.println(i);
			String key = n_n_modi_rank.get(i).getKey().toString();
			String val = pair_n_n.get(key);
			String[] value = val.split("\t");
			for(int j=0;j<value.length;j++){
				String modifier = value[j].split("\\+")[0];
				String times = value[j].split("\\+")[1];
				writer_nn.write(key+"+"+modifier+" "+times+"\r\n");
			}
		}
		writer_nn.close();
		fwnn.close();
			
	  }

}
