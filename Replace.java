

import java.io.*;
import java.util.*;

import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.CorefChain.CorefMention;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
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

public class Replace {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		demoAPI(lp);
	}
	
	
	//it的替换不是非常严谨，是遇到问题修改问题一点点加起来的，所以漏洞还有，应该还有不少修改、调整之处
	public static void demoAPI(LexicalizedParser lp) throws IOException {
	    // This option shows parsing a list of correctly tokenized words
	    /*String[] sent = { "This", "is", "an", "easy", "sentence", "." };
	    List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
	    Tree parse = lp.apply(rawWords);
	    parse.pennPrint();
	    System.out.println();*/

	    // This option shows loading and using an explicit tokenizer
		String destination = "summerpalace";
		InputStream instream = new FileInputStream(destination+".txt");
		InputStreamReader reader = new InputStreamReader(instream,"UTF-8");
		BufferedReader br = new BufferedReader(reader);
		
		FileWriter fw = new FileWriter("replaceit_"+destination+".txt");
		
		int countrep = 0;
		int countrep2 = 0;
		int countrep3 = 0;
		//StanfordLemmatizer slem = new StanfordLemmatizer();
		
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");

	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    Annotation annotation;
	    
	    String str_line;
	    int count = 0;
	    while((str_line = br.readLine())!=null){
	    	System.out.println(count++); 
			annotation = new Annotation(str_line.split("\t")[3]);
	    	//annotation = new Annotation("I visited Summer_Palace this morning, and although it is historically & culturally very interesting, I would not really recommend a Winter visit, especially if you have a misty morning, as it spoils your views and even some of the experience.");
			fw.write(str_line.split("\t")[0]+"\t"+str_line.split("\t")[1]+"\t"+str_line.split("\t")[2]+"\t");
		    pipeline.annotate(annotation);
		    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		    
			TokenizerFactory<CoreLabel> tokenizerFactory =
			        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			String subject = "";
			
			for(int p=0;p<sentences.size();p++){
				int flag1 = 0;//ÊÇ·ñ´æÔÚnsubj£¬ÇÒÎªÐÎÈÝ´Ê£¬´ËÊ±itÌæ»»ÎªÇ°Ò»¾äµÄÖ÷Óï
				int flag2 = 0;//ÊÇ·ñ´æÔÚnsubj£¬ÇÒÎªÃû´Ê£¬´ËÊ±¶Ô±¾¾äµÄit²»½øÐÐÌæ»»

				String str = sentences.get(p).toString();
				String replace = "";
			    Tokenizer<CoreLabel> tok =
			        tokenizerFactory.getTokenizer(new StringReader(str));
			    List<CoreLabel> rawWords2 = tok.tokenize();
			    Tree parse = lp.apply(rawWords2);
			    
			    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
			    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
			    
			    //It位于整个评论的句首，直接判定为景点
			    if(p==0&&(str.substring(0,3).equals("It ")||str.substring(0,4).equals("It's"))){
			    	for(int i=0;i<tdl.size();i++){
			    		if(tdl.get(i).reln().toString().equals("nsubj")&&(tdl.get(i).gov().toString().contains("JJ")||tdl.get(i).gov().toString().contains("NN"))&&tdl.get(i).dep().toString().split("/")[0].equals("It")){
			    			flag1 = 1;
			    		}
			    		/*else if(tdl.get(i).reln().toString().equals("nsubj")&&!(tdl.get(i).gov().toString().contains("JJ")||tdl.get(i).gov().toString().contains("NN"))&&tdl.get(i).dep().toString().split("/")[0].equals("It")){
			    			flag2 = 1;
			    		}*/
			    		else if(tdl.get(i).reln().toString().equals("mark")&&tdl.get(i).dep().toString().split("/")[0].equals("to"))
			    			flag2 = 1;
			    		if(tdl.get(i).dep().toString().split("/")[0].equals("It")&&tdl.get(i).gov().toString().split("/")[0].equals("day"))
			    			flag2 = 1;
			    		else if(tdl.get(i).gov().toString().split("/")[0].equals("It")&&tdl.get(i).dep().toString().split("/")[0].equals("day"))
			    			flag2 = 1;
			    	}		
			    	if(flag1==1&&flag2==0){//It¸úÐÎÈÝ´Ê´îÅä£¬ÇÒÃ»ÓÐÆäËûÓÃ·¨
			    		if(str.substring(0,3).equals("It ")){
				    		replace = str.replace("It ", "Summer_Palace ");
				    		subject = "Summer_Palace";
				    		countrep3++;
			    		}
			    		else{
			    			replace = str.replace("It", "Summer_Palace");
				    		subject = "Summer_Palace";
				    		countrep3++;
			    		}
			    	}
			    	else replace = str;
			    }
			    //This位于整个评论的句首，直接判定为景点
			    else if(p==0&&(str.substring(0,4).equals("This"))){
			    	int flag = 1;
		    		for(int i=0;i<tdl.size();i++){
		    			if(tdl.get(i).reln().toString().contains("det"))
		    				if(tdl.get(i).gov().toString().split("/")[0].equals("This")||tdl.get(i).dep().toString().split("/")[0].equals("This"))
		    					flag = 0;
		    		}
		    		if(flag==1){
		    			replace = str.replace("This ", "Summer_Palace ");
		    			subject = "Summer_Palace";
		    		}
		    		else replace = str;
		    	}
			    
			    //it存在于第一句，判定为景点
			    if((p==0&&str.contains(" it ")||(p==0&&str.contains(" this ")))){
			    	if(str.contains(" it ")){
				    	int flag = 1;
				    	for(int i=0;i<tdl.size();i++){
				    		if(tdl.get(i).reln().toString().contains("nsubj")){
				    			/*if(tdl.get(i).gov().toString().split("/")[0].equals("it")&&!(tdl.get(i).dep().toString().contains("JJ")||tdl.get(i).dep().toString().contains("NN"))){
				    				flag = 0;
				    			}
				    			else if(tdl.get(i).dep().toString().split("/")[0].equals("it")&&!(tdl.get(i).gov().toString().contains("JJ")||tdl.get(i).gov().toString().contains("NN"))){
				    				flag = 0;
				    			}*/
				    			if(tdl.get(i).reln().toString().equals("mark")&&tdl.get(i).dep().toString().split("/")[0].equals("to"))
					    			flag = 0;
				    			if(tdl.get(i).gov().toString().split("/")[0].equals("it")&&tdl.get(i).dep().toString().split("/")[0].equals("day"))
				    				flag = 0;
				    			else if(tdl.get(i).dep().toString().split("/")[0].equals("it")&&tdl.get(i).gov().toString().split("/")[0].equals("day"))
				    				flag = 0;
				    		}
				    	}
				    	if(flag==1){
				    		replace = str.replace(" it " , " Summer_Palace ");
				    		subject = "Summer_Palace";
				    	}
				    	else replace = str;
			    	}
			    	if(str.contains(" this ")){
			    		int flag = 1;
			    		for(int i=0;i<tdl.size();i++){
			    			if(tdl.get(i).reln().toString().contains("det"))
			    				if(tdl.get(i).gov().toString().split("/")[0].equals("this")||tdl.get(i).dep().toString().split("/")[0].equals("this"))
			    					flag = 0;
			    		}
			    		if(flag==1){
			    			replace = str.replace(" this ", " Summer_Palace ");
			    			subject = "Summer_Palace";
			    		}
			    		else replace = str;
			    	}
			    }
			    //It不在评论第一句，但在句首，将其替换为前一句的主语
			    else if(p>0&&str.length()>3&&(str.substring(0,3).equals("It ")||str.substring(0,4).equals("It's"))){//¶ÔÓÚitÎ»ÓÚ¾äÊ×µÄÇé¿ö
			    	int flag = 1;
			    	for(int i=0;i<tdl.size();i++){
			    		/*if(tdl.get(i).reln().toString().equals("nsubj")&&!(tdl.get(i).gov().toString().contains("JJ")||tdl.get(i).gov().toString().contains("NN"))&&tdl.get(i).dep().toString().split("/")[0].equals("It")){
			    			flag = 0;
			    		}*/
			    		if(tdl.get(i).reln().toString().equals("mark")&&tdl.get(i).dep().toString().split("/")[0].equals("to"))
			    			flag = 0;
			    		if(tdl.get(i).gov().toString().split("/")[0].equals("It")&&tdl.get(i).dep().toString().split("/")[0].equals("day"))
		    				flag = 0;
		    			else if(tdl.get(i).dep().toString().split("/")[0].equals("It")&&tdl.get(i).gov().toString().split("/")[0].equals("day"))
		    				flag = 0;
			    	}		
			    	if(str.substring(0,3).equals("It ")){
				    	if(flag==1){//It¸úÐÎÈÝ´Ê´îÅä£¬ÇÒÃ»ÓÐÆäËûÓÃ·¨
				    		if(!subject.equals("")){
				    			replace = str.replace("It ", subject+" ");
				    		}
				    		else replace = str.replace("It ", "Summer_Palace ");
				    		countrep2++;
				    	}
				    	else replace = str;
			    	}
			    	else if(str.substring(0,4).equals("It's")){
			    		if(flag==1){//It¸úÐÎÈÝ´Ê´îÅä£¬ÇÒÃ»ÓÐÆäËûÓÃ·¨
				    		if(!subject.equals("")){
				    			replace = str.replace("It", subject);
				    		}
				    		else replace = str.replace("It", "Summer_Palace");
				    		countrep2++;
				    	}
				    	else replace = str;
			    	}
			    }
			    //it不在句首，也不在第一句，将其替换为提取的主语
			    //主语的提取：如果本句存在主语名词，则提取本句的主语。如果本句的主语不为名词（如代词），则将it替换为之前提取的主语
			    else{//it²»Î»ÓÚ¾äÊ×
			    	TreeGraphNode[] subject_list = gs.root().children()[0].children();
			    	int findnp = 0;
			    	int flagnp = 0;
			    	String positionnp = "";
			    	while(findnp==0){
			    		for(int j=0;j<subject_list.length;j++)
			    		if(subject_list[j].toString().contains("NP"))
			    			for(int m=0;m<subject_list[j].children().length;m++)
			    				if(subject_list[j].children()[m].toString().contains("NN")){
			    					subject = subject_list[j].children()[m].headWordNode().toString().split("-")[0];//ÕÒ±¾¾äµÄÖ÷Óï
			    					findnp = 1;
			    					flagnp = 1;
			    				}
			    		if(subject_list[0].children().length>0)
			    			subject_list = subject_list[0].children();
			    		else break;
			    	}
			    	
			    	int flagit = 0;//ÊÇ·ñ´æÔÚit
			    	for(int j=0;j<tdl.size();j++){
			    		if(tdl.get(j).reln().toString().contains("nsubj")){//´æ´¢ËùÓÐnsubj¹ØÏµ
			    			
			    			String add1 = tdl.get(j).gov().toString().split("/")[0];
			    			String add2 = tdl.get(j).dep().toString().split("/")[0];
			    			int test = tdl.get(j).gov().beginPosition();
			    			if(add1.equals("it")){//itÓëÐÎÈÝ´ÊÅä¶Ô
			    				flagit = 1;
			    			}
			    			else if(add2.equals("it")){
			    				flagit = 1;
			    			}
			    			if(tdl.get(j).gov().toString().split("/")[0].equals("it")&&tdl.get(j).dep().toString().split("/")[0].equals("day")){
			    				flagit = 0;
			    				break;
			    			}
			    			else if(tdl.get(j).dep().toString().split("/")[0].equals("it")&&tdl.get(j).gov().toString().split("/")[0].equals("day")){
			    				flagit = 0;
			    				break;
			    			}
			    		}
			    		//to
			    		/*if(tdl.get(j).reln().toString().equals("mark")&&(tdl.get(j).gov().toString().split("/")[0].equals("to")||tdl.get(j).dep().toString().split("/")[0].equals("to"))){
			    			flagit = 0;
			    			break;
			    		}*/
			    		
			    	}
			    	if(flagit==1){//Ö»ÓÐ1¸öit
			    		if(!subject.equals(""))
			    			replace = str.replace(" it ", " "+subject+" ");
			    		else replace = str.replace(" it ", " Summer_Palace ");
			    	}
			    	else replace = str;
			    }
			    fw.write(replace);
			}
			fw.write("\r\n");
	    }
	    
	    br.close();
	    fw.close();
	    System.out.println(countrep);
	    System.out.println(countrep2);
	}

}