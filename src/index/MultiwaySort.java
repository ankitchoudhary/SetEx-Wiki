package index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

public class MultiwaySort 
{
	private static List<File> tempFileList;
	private static BufferedWriter refFileInner;
	private static BufferedReader fbr;
	private static File outputDir;
	private static long blocksize;
	private static PrintWriter errorWriter;
	private static int totDocCount;
	private static int counter=0;
	private static TreeMap<Float,TreeSet<String>> perStringMap=new TreeMap<Float,TreeSet<String>>(Collections.reverseOrder());//docid,count
	private static TreeSet<String> innerSet;
    private static int MAXINDEX=20;//Lovlean
    private static int MAXCLASSIFIER=20;//Lovlean
    private static int aug;
	public static void initiate(String file,int aug1,String outputDest,String tempName)
	{
		System.out.println(file);
		try
		{
			aug=aug1;
			File temp=new File(file);
			blocksize=Runtime.getRuntime().freeMemory()/2;
			errorWriter = new PrintWriter(outputDest+"/error_"+tempName+aug+".txt");
			outputDir=new File(outputDest);
			System.out.println(file);
			fbr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			tempFileList = createPartial(temp.length()*2);
			mergeSortedFiles(tempFileList,tempName);
		}
		catch(Exception e)
		{
			System.out.println("in seffinitiate");
			e.printStackTrace();
			e.printStackTrace();
			errorWriter.close();
		}
		catch(Error e)
		{
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
	}
		
	private static List<File> createPartial(final long datalength) throws IOException
	{
		List<File> files = new ArrayList<File>();
        List<String> stringList = new ArrayList<String>();
        String line = "";
        try 
        {
        	long currentblocksize;
        	while (line != null) 
            {
            	
            	currentblocksize = 0;
            	while ((currentblocksize < blocksize)&& ((line = fbr.readLine()) != null)) 
            	{
            		if(line.length()==0)
            			System.out.println("sfdgsdsgdg");
			else
			{	
            			stringList.add(line);
                    		currentblocksize += line.length()*2;
                	}
		}
                files.add(sortPartial(stringList));
                stringList.clear();
            }
        }
        catch (EOFException eof) 
        {
        	if (stringList.size() > 0)
        	{
        		files.add(sortPartial(stringList));
        		stringList.clear();
         	}
        }
        catch(Exception e)
		{
			System.out.println("in create partial");
			e.printStackTrace();
		}
		catch(Error e)
		{
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
        finally
        {
                fbr.close();
        }
        return files;
	}
	
	private static File sortPartial(List<String> tmplist)throws IOException
	{
		  Collections.sort(tmplist);
          File newtmpfile = File.createTempFile("sortPartial","tmp",outputDir);
          newtmpfile.deleteOnExit();
          OutputStream out = new FileOutputStream(newtmpfile);
          BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter( out));
          try 
          {
                  for (String r : tmplist) 
                  {
                         fbw.write(r);
                         fbw.newLine();
                  }
          } 
        catch(Exception e)
  		{
  			System.out.println("in sort partial");
  			e.printStackTrace();
  		}
  		catch(Error e)
  		{
  			e.printStackTrace();
  			errorWriter.close();
  			System.exit(1);
  		}
          finally 
          {
              fbw.close();
          }
          return newtmpfile;
  }

	public static void mergeSortedFiles(List<File> files,String tempName) throws IOException //lovlean
	{
            ArrayList<MyFileBuffer> bfbs = new ArrayList<MyFileBuffer>();
            for (File f : files)
            {
                    InputStream in = new FileInputStream(f);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                          
                    MyFileBuffer bfb = new MyFileBuffer(br);
                    bfbs.add(bfb);
            }
            
            PriorityQueue<MyFileBuffer> pq = new PriorityQueue<MyFileBuffer>(
                    11, new Comparator<MyFileBuffer>() {
                            @Override
                            public int compare(MyFileBuffer i,
                                    MyFileBuffer j) {
                                    return i.peek().compareTo( j.peek());
                            }
                    });
            for (MyFileBuffer bfb: bfbs)
                    if(!bfb.empty())
                            pq.add(bfb);
            MyFileBuffer bfb;
            String r;
            File tt=new File(outputDir+"/"+tempName+aug+".txt");          
            BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tt)));
			 try {
                    while (pq.size() > 0) 
                    {
                            bfb = pq.poll();
                            r = bfb.pop();
                            fbw.write(r+"\n");
                            
                            if (bfb.empty()) 
                            {
                            	bfb.fbr.close();
                            }
                            else 
                            {
                            	pq.add(bfb); // add it back
                            }
                    }
            } 
            finally
            {
            	
            	fbw.close();
                
                 for (MyFileBuffer bfb1 : pq)
                	 bfb1.close();
            }
          
      }
	
	public static void createPostingList(String outputDest) throws IOException
	{
	
		totDocCount=XMLHandler.getTotDocCount();
		errorWriter = new PrintWriter(outputDest+"/error_sort_merge.txt");
		File f=new File(outputDest+"/refIndexInner.txt");
		if(f.exists())
			f.delete();
		//refFile=new PrintWriter(outputDest+"/refIndex.txt");
		refFileInner = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
		
		ArrayList<MyFileBuffer> bfbs = new ArrayList<MyFileBuffer>();
		int countt=XMLHandler.getFileCount();//Lovlean
        for(int i=0;i<=countt;i++) //Lovlean
        {
              InputStream in = new FileInputStream(outputDest+"/tempIndex"+i+".txt");
              BufferedReader br = new BufferedReader(new InputStreamReader(in));
              MyFileBuffer bfb = new MyFileBuffer(br);
              bfbs.add(bfb);
        }
        
        PriorityQueue<MyFileBuffer> pq = new PriorityQueue<MyFileBuffer>(
                    11, new Comparator<MyFileBuffer>() {
                            @Override
                            public int compare(MyFileBuffer i,
                                    MyFileBuffer j) {
                                    return i.peek().compareTo( j.peek());
                            }
                    });
        for (MyFileBuffer bfb: bfbs)
                 if(!bfb.empty())
                        pq.add(bfb);
        String lastLine = "";
        String current[]=null;
        MyFileBuffer bfb;
        String r;
        String cc="";
        boolean sizeMore=false;
        File tt=new File(outputDest+"/sorted_a.txt");          
        RandomAccessFile fbw=new RandomAccessFile(tt,"rw");
        char chOffset='a'-1;
        try {
               while (pq.size() > 0) 
               {
                      bfb = pq.poll();
                      r = bfb.pop();
                      current=r.split(":");
                      cc=current[0];
                      if(cc.length()>0 && cc.charAt(0)>chOffset)
                      {
                          	if(sizeMore)
                            {
                          		write2File(lastLine,fbw);
                   				lastLine = cc;
                   				sizeMore=false;
                   				        
                            }
                       		chOffset=cc.charAt(0);
                       		fbw.close();
                       		tt=new File(outputDest+"/sorted_"+chOffset+".txt");
                            if(tt.exists())
                               	tt.delete();
                       		fbw = new RandomAccessFile(tt,"rw");
                       	    //refAddress[chOffset-'a']=refFileInner.getFilePointer();
                       	    counter=0;
                    }
                    if (!cc.equals(lastLine))
                    {
                        	if(sizeMore)
                            {
                           		write2File(lastLine,fbw);
                           		lastLine = cc;
                           		sizeMore=false;
                           	}
                    }
                    lastLine = cc;
                    Float temp;
                    try{
	                    temp=Float.parseFloat(current[2]);//Lovlean
	                    
	                    if(perStringMap.containsKey(temp))
	                    {
	                    	innerSet=perStringMap.get(temp);
	                    	innerSet.add(current[1]);//Lovlean
	                    }
	                    else
	                    {
	                    	innerSet=new TreeSet<String>();
	                    	innerSet.add(current[1]);//Lovlean
	                    	perStringMap.put(temp,innerSet);
	                    }
                    }catch(Exception e)
                    {
                    	
                    }
                    sizeMore=true;
                    if (bfb.empty()) 
                    {
                         	bfb.fbr.close();
                    }
                    else 
                    {
                          	pq.add(bfb); // add it back
                    }
                }
            } 
            finally
            {
            	
            	if(sizeMore)
                {
            		write2File(lastLine,fbw);
    				lastLine = cc;
    				sizeMore=false;
    				        
                }
                 fbw.close();
               refFileInner.close();
                 for (MyFileBuffer bfb1 : pq)
                	 bfb1.close();
             
            }
          
      }
	
	
	private static void write2File(String lastLine,  RandomAccessFile fbw) throws IOException
	{
		counter++;
		if(counter==MAXINDEX)
		{
			counter=0;
			refFileInner.write(lastLine+":"+fbw.getFilePointer()+"\n");
		}
		StringBuilder sb=new StringBuilder();
		int dft;
		sb.append(lastLine);
		fbw.writeBytes(sb.toString());
		sb.setLength(0);
    	dft=perStringMap.size();
    	float wt=(float)(1000*(Math.log10(totDocCount/dft)));
    	int countForString=0;
	  	int writeOffsetAfter=dft/10;
	  	if(writeOffsetAfter>100)
	  		writeOffsetAfter=100;
	  	else if(writeOffsetAfter<=20)
	  		writeOffsetAfter=dft;
	  	
	  	for(Float c: perStringMap.keySet())
    	{
	  		for(String s : perStringMap.get(c))
	  		{
	  			sb.append(":");
	  			sb.append(s);
	  			sb.append(":");//Lovlean
	    		sb.append((int)(c*wt));
	    		countForString++;
	    		if(countForString==writeOffsetAfter)
	    		{
	    			countForString=0;
	    			fbw.writeBytes(":"+sb.length()+sb);
	    			sb.setLength(0);
	    		}
	  		}
    		
    	}
	  	if(countForString>0)
	  		fbw.writeBytes(":"+sb.length()+sb);
	  	fbw.writeBytes("\n");
		perStringMap.clear();
		
		
	}

	//lovlean
	public static void createClassifierList(String outputDest) throws IOException
	{
	
		counter=0;
		TreeSet<Integer> perKeySet= new TreeSet<Integer>();
		//totDocCount=XMLHandler.getTotDocCount();
		errorWriter = new PrintWriter(outputDest+"/error_classifier_merge.txt");
		File f=new File(outputDest+"/refClassfierInner.txt");
		if(f.exists())
			f.delete();
		//refFile=new PrintWriter(outputDest+"/refIndex.txt");
		refFileInner = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
		
		ArrayList<MyFileBuffer> bfbs = new ArrayList<MyFileBuffer>();
		int countt=XMLHandler.getCount_classifier();//Lovlean
        for(int i=0;i<=countt;i++) //Lovlean
        {
              InputStream in = new FileInputStream(outputDest+"/tempClassifier"+i+".txt");
              BufferedReader br = new BufferedReader(new InputStreamReader(in));
              MyFileBuffer bfb = new MyFileBuffer(br);
              bfbs.add(bfb);
        }
        
        PriorityQueue<MyFileBuffer> pq = new PriorityQueue<MyFileBuffer>(
                    11, new Comparator<MyFileBuffer>() {
                            @Override
                            public int compare(MyFileBuffer i,
                                    MyFileBuffer j) {
                                    return i.peek().compareTo( j.peek());
                            }
                    });
        for (MyFileBuffer bfb: bfbs)
                 if(!bfb.empty())
                        pq.add(bfb);
        String lastLine = "";
        String current[]=null;
        MyFileBuffer bfb;
        String r;
		int l=0;
        String cc="";
        boolean sizeMore=false;
        File tt=new File(outputDest+"/sorted_classifier.txt");          
        RandomAccessFile fbw=new RandomAccessFile(tt,"rw");
        try {
               while (pq.size() > 0) 
               {
                      bfb = pq.poll();
                      r = bfb.pop();
                      current=r.split(":");
					  cc="";
                     /* if(current.length==3){
                    	  cc=current[0]+":"+current[1];  
                      }
                      else{
                    	  System.out.println(r);
                      }*/
					  if(current.length<3)
						  System.out.println("fail");
					  else
					  {
						  l=current.length-1;
						  if(l>=0){
							  cc=current[0];
							} 
	                      for(int i=1;i<l;i++)
	                      {
	                    	  cc+=(":"+current[i]);
	                      }
	                      if (!cc.equals(lastLine))
	                      {
	                          	if(sizeMore)
	                              {
	                          		counter++;
	                          		if(counter==MAXCLASSIFIER)
	                          		{
	                          			counter=0;
	                          			refFileInner.write(lastLine+":"+fbw.getFilePointer()+"\n");
	                          		}
	                          		StringBuilder sb=new StringBuilder();
	                          		sb.append(lastLine);
	                          		for(Integer s : perKeySet)
	                      	  		{
	                      	  			sb.append(":");
	                      	  			sb.append(s);
	                      	  		}
	                          		fbw.writeBytes(sb+"\n");
	                  	    		sb.setLength(0);
	                  	    		perKeySet.clear();
	                  	    		lastLine = cc;
	                             		sizeMore=false;     		
	                          	}
	                      	  	
	                      }
	                      lastLine = cc;
	                      Integer temp;
	                      try{
	  	                    temp=Integer.parseInt(current[l]);//Lovlean
	  	                    perKeySet.add(temp);
	                      }catch(Exception e)
	                      {
	                      	
	                      }
	                      sizeMore=true;      
                     
					  }
                     
                    
                    if (bfb.empty()) 
                    {
                         	bfb.fbr.close();
                    }
                    else 
                    {
                          	pq.add(bfb); // add it back
                    }
                }
            } 
            finally
            {
            	
            	if(sizeMore)
                {
            		counter++;
            		if(counter==MAXCLASSIFIER)
            		{
            			counter=0;
            			refFileInner.write(lastLine+":"+fbw.getFilePointer()+"\n");
            		}
            		StringBuilder sb=new StringBuilder();
            		sb.append(lastLine);
            		for(Integer s : perKeySet)
        	  		{
        	  			sb.append(":");
        	  			sb.append(s);
        	  		}
            		fbw.writeBytes(sb+"\n");
    	    		sb.setLength(0);
    	    		lastLine = cc;
               		sizeMore=false;
    				        
                }
                 fbw.close();
               refFileInner.close();
                 for (MyFileBuffer bfb1 : pq)
                	 bfb1.close();
             
            }
          
          
      }
	
	
/*	private static Map<String, Float> sortByComparator(Map<String, Float> unsortMap)
	{

	    List<Entry<String, Float>> list = new LinkedList<Entry<String, Float>>(unsortMap.entrySet());

	    // Sorting the list based on values
	    Collections.sort(list, new Comparator<Entry<String, Float>>()
	    {
	        public int compare(Entry<String, Float> o1,
	                Entry<String, Float> o2)
	        {
	           
	                return o2.getValue().compareTo(o1.getValue());
	        }
	    });

	    // Maintaining insertion order with the help of LinkedList
	    Map<String, Float> sortedMap = new LinkedHashMap<String, Float>();
	    for (Entry<String, Float> entry : list)
	    {
	        sortedMap.put(entry.getKey(), entry.getValue());
	        entry.setValue(null);
	    }

	    return sortedMap;
	}
	      */ 

}

final class MyFileBuffer
{
    public BufferedReader fbr;
    private String cache;

    public MyFileBuffer(BufferedReader r)throws IOException 
    {
            fbr = r;
            reload();
    }

    public boolean empty() 
    {
            return cache == null;
    }

    private void reload() throws IOException 
    {
           cache = this.fbr.readLine();
    }

    public void close() throws IOException 
    {
            fbr.close();
    }

    public String peek()
    {
            return cache;
    }

    public String pop() throws IOException 
    {
            String answer = peek().toString();// make a copy
            reload();
            return answer;
    }

}

