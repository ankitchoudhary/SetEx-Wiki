/**
 * @author lovlean
 *
 */

package index;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class MainIndex 
{
	private static String oPath;
	public static String getoPath()
	{
		return oPath;
	}
	public static void main(String[] args) 
	{
			try
		    {
				long start=System.currentTimeMillis();
		    	
				oPath=args[1];
		    	SAXParserFactory spf = SAXParserFactory.newInstance();
		        spf.setNamespaceAware(true);
		        SAXParser saxParser = spf.newSAXParser();
		        XMLReader xmlReader = saxParser.getXMLReader();
		        xmlReader.setContentHandler(new XMLHandler());
//		        xmlReader.parse(args[0]);
		        System.out.println("**************Please note these values***********");
		        System.out.println(XMLHandler.getFileCount() +  "    "+XMLHandler.getTotDocCount()+"     "+XMLHandler.getCount_classifier());
//		     int tempIndex = XMLHandler.getFileCount();
		        
//		      for(int i=1;i<=4;i++)
//		      {
		       	//MultiwaySort.initiate(args[1]+"/index4"+".txt",4, args[1],"tempIndex"); //lovlean
//		      }
//		      MultiwaySort.createPostingList(args[1]);
		       
		       //MultiwaySort.initiate(args[1]+"/index"+5 +".txt",5, args[1]);
		        //System.out.println("DOC"+XMLHandler.getTotDocCount());
		    XMLHandler.setTotDocCount(14041179); //TODO UNCOMMENT
		       
		     XMLHandler.setFileCount(4);//TODO UNCOMMENT
		       
		    MultiwaySort.createPostingList(args[1]); //TODO UNCOMMENT
//		      int tempClassifier = XMLHandler.getCount_classifier();
//int tempClassifier =0;		        
		       
	//	       for(int i=0;i<=tempClassifier;i++)
		//       {
		    	   
		        	//MultiwaySort.initiate("/media/rocko/rocko_room/iiit/sem6/ire/classifier0.txt",0, args[1],"tempClassifier"); //lovlean
		  //     }
		      //XMLHandler.setCount_classifier(0);//TODO UNCOMMENT LT
		      // MultiwaySort.createClassifierList(args[1]);//TODO UNCOMMENT LT
long mid=System.currentTimeMillis();		       
		       System.out.println(mid-start);
			     System.out.println("finish");
		        //long end=System.currentTimeMillis();
		        //System.out.println(end-mid);
		        //System.out.println(end-start);
		       /*
		        for(int i=0;i<=tempIndex;i++)
		        {
		        	File f=new File(args[1]+"/index"+i+".txt");
		        	f.delete();
		        	f=new File(args[1]+"/tempIndex"+i+".txt");
			       f.delete();
			       f=new File(args[1]+"/error_sort"+i+".txt");
			       if(f.length()==0)
			    	   f.delete();
			       f=new File(args[1]+"/error.txt");
			       if(f.length()==0)
			    	   f.delete();
			       f=new File(args[1]+"/error_sort_merge.txt");
			       if(f.length()==0)
			    	   f.delete();
			    }*/
		        
		       
		    }
		    catch(Exception e)
		    {
		    	if(e instanceof IOException)
		    	{
		    		System.err.println("************ERROR IN ACCESSING I/P | O/P FILE************");
		    		
		    	}
		    	else if(e instanceof SAXException)
		    	{
		    		System.err.println("************ERROR IN SAX PARSING************");
		    	}
		    	else
		    	{
		    		System.err.println("************Some other critical error************");
		    	}
		    	e.printStackTrace();
		    	
		    }
	}
}

