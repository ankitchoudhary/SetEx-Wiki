package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

public class testing
{
	
	public static void main(String[] args) throws Exception
	{
		String line;
		String linearr[];
		File temp=new File("index/tempClassifier0.txt");
		//blocksize=Runtime.getRuntime().freeMemory()/2;
		//errorWriter = new PrintWriter(outputDest+"/error_"+tempName+aug+".txt");
		//outputDir=new File(outputDest);
		System.out.println("sdsfdfdsd");
		BufferedReader fbr = new BufferedReader(new FileReader(temp));
		line=fbr.readLine();
		int i=0;
		while (line != null) 
		{
			
		//	System.out.println(line+"      "+line.length());
			if(line.length()==0)
			{
				System.out.println(line);
				System.out.println(i);
			}
			else 
			{
				linearr=line.split(":");
				if(linearr.length!=3)
				{
					System.out.println(line +"  "+i);
				}
				
			}
			i++;
			line=fbr.readLine();
			//stringList.add(line);
	       // currentblocksize += line.length()*2;
	    }
		System.out.println("dgfg");
		
		fbr.close();
	}
	
}
