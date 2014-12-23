package search;


import index.StemmingPorter;
import index.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;


public class copy 
{
	private static TreeMap<String, Long> refIndex;
	private static Map<Integer, Map<Integer,Integer>> finalList;//docid,weight,count
	private static Map<Integer,Integer> finalListInner;//wt count
	private static Map<Integer,Integer> ansList;//<docid,wt>
	private static String outDir;
	private static StemmingPorter stemmer = new StemmingPorter();
	private static RandomAccessFile indexReader[]=null;
	private static BufferedReader queryReader=null;
	private static String strObj;
	private static TreeSet<String> validQueryTerms;
	private static TreeMap<String,PostingList> queryTerms;//queryTerm (character,postin
	public static void main(String[] args) 
	{
		try
		{
			refIndex=new TreeMap<String,Long>();
			finalList=new LinkedHashMap<Integer, Map<Integer,Integer>>();
			ansList=new HashMap<Integer,Integer>();
			outDir=args[0];
			bringInMemory();
			openFiles();
			queryTerms=new TreeMap<String,PostingList>();
			validQueryTerms=new TreeSet<String>();
			searchPrepare();
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		finally
		{
			try
			{
				for(int i=0;i<26;i++)
				{
					indexReader[i].close();
				}
			}
			catch (Exception e)
			{
				System.exit(1);
			}
			
		}
	}
	
	
	private static void bringInMemory() throws Exception
	{
		BufferedReader br=null;
		try
		{
			File f=new File(outDir+"/refIndexInner.txt");
			br=new BufferedReader(new FileReader(f));
			String line;
			String []arrLine;
			while((line=br.readLine())!=null)
			{
				
				arrLine=line.split(":");
				refIndex.put(arrLine[0], Long.parseLong(arrLine[1]));
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		finally
		{
			br.close();
		}
	}
	
	private static void openFiles() throws IOException
	{
		indexReader=new RandomAccessFile[26];
		for(int i=0;i<26;i++)
		{
			indexReader[i]=new RandomAccessFile(outDir+"/sorted_"+(char)(i+97)+".txt", "r");
		}
		
	}
	
	private static void searchPrepare()throws Exception
	{
		try{
			queryReader=new BufferedReader(new InputStreamReader(System.in));
			String line;
			int counter=Integer.parseInt(queryReader.readLine());
			for(int x=0;x<counter&&((line=queryReader.readLine())!=null);x++)
			{
				queryTerms.clear();
				finalList.clear();
				ansList.clear();
				validQueryTerms.clear();
				handleNoise(line.toLowerCase());
				searchAns();
				ansList=sortByComparator(ansList);
				for(Integer docid:ansList.keySet())
				{					
					System.out.println(docid+":\t"+ansList.get(docid));//queryTerms.get(docid).printPostingList();
				}
				int count=ansList.size();
				if(count!=10)
				{
					ansList.clear();
					
					//finalList=sortByComparator2(finalList);
					for(Integer docid:finalList.keySet())
					{					
						for(Integer wt:finalList.get(docid).keySet())
						{
							
							ansList.put(docid, wt);
							
						}
						
					}
					ansList=sortByComparator(ansList);
					
					for(Integer docid:ansList.keySet())
					{	
						count++;
						System.out.println(docid+":\t"+ansList.get(docid));//queryTerms.get(docid).printPostingList();
						if(count==10)
							break;
					}
					
				}
				System.out.println("Done");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		finally
		{
			queryReader.close();
		}
	}
	
	private static void handleNoise(String line)
	{
		char l;
		String field="";
		strObj="";
		for(int k=0;k<line.length();k++)
		{
			l=line.charAt(k);
			if(l<='z'&&l>='a')
				strObj+=l;
			else if(l==':')
			{
				field=strObj;
				strObj="";
			}
			else if(l==' ')
			{

				PostingList ob;
				if(queryTerms.containsKey(strObj))
				{
					ob=queryTerms.get(strObj);
					field+=ob.getSearchedIn();
					ob.setSearchedIn(field);
				}
				else
				{
					ob=new PostingList();
					ob.setSearchedIn(field);
					queryTerms.put(strObj,ob);
				}
				
				strObj="";
				field="";
			}
		}
		if(strObj.length()>0)
		{
			PostingList ob;
			if(queryTerms.containsKey(strObj))
			{
				ob=queryTerms.get(strObj);
				field+=ob.getSearchedIn();
				ob.setSearchedIn(field);
			}
			else
			{
				ob=new PostingList();
				ob.setSearchedIn(field);
				queryTerms.put(strObj,ob);
			}
		}
	}
	
	private static void searchAns()
	{
		for(String temp : queryTerms.keySet())
		{
			//System.out.println(temp  + ":"+ queryTerms.get(temp));
			if(temp.length()>=2&&!(Utils.stopWordSet.contains(temp)))
			{
				stemmer.add(new StringBuilder(temp),temp.length());
				stemmer.stem();
                temp= stemmer.toString();
                if(temp.length()>=2&&!(Utils.stopWordSet.contains(temp)))
                	startPostingList(temp);
            }
		}
		
		if(validQueryTerms.size()==1)//some has info
		{
			PostingList listOb;
			LinkedHashMap<Integer,HashMap<Integer,String>> postListPerTerm;//doc wt fields
			HashMap<Integer,String> tempInner;//wt fields
			String fieldsGiven,fieldsExists;
			int len;
			boolean flagMatched;
			listOb=queryTerms.get(validQueryTerms.first());//for all terms get postinglist
			fieldsGiven=listOb.getSearchedIn();
			postListPerTerm=listOb.getPostList();
			while(true)
			{
				for(Integer docid : postListPerTerm.keySet())
				{
					tempInner=postListPerTerm.get(docid);
					for(Integer wt:tempInner.keySet())
					{
						flagMatched=true;
						fieldsExists=tempInner.get(wt);
						len=fieldsGiven.length();
						for(int i=0;i<len;i++)
						{
							if(fieldsExists.indexOf(fieldsGiven.charAt(i))==-1)
							{
								flagMatched=false;
								break;
							}
							
						}//flagMatching
						if(flagMatched)
						{
							ansList.put(docid,wt);
							if(ansList.size()==10)
								return;
						}
					}
				}
				//not found ten.
				if(listOb.isEnd())
					return;//no more entries
				nextPostingList(validQueryTerms.first());
				if(validQueryTerms.size()==0)
					return;//u ought to get something. should never come here
			}
		}
		else if(validQueryTerms.size()>1)
		{
				doAnd();
		}
		
		/*System.out.println("finding next");
		nextPostingList("indiana");
		queryTerms.get("indiana").printPostingList();*/
		//System.out.println("final print:"+queryTerms.get("indiana").printPostingList());
		//System.out.println(queryTerms.get("indiana").getPostList().size());
		
		
	}
	
	private static void doAnd() 
	{
		PostingList listOb;
		LinkedHashMap<Integer,HashMap<Integer,String>> postListPerTerm;//doc wt fields
		HashMap<Integer,String> tempInner;//wt fields
		String fieldsGiven,fieldsExists;
		int len,count;
		boolean flagMatched;
		int size=validQueryTerms.size();
		//LinkedHashMap<Integer, HashMap<Integer,Integer>>
		for(String s: validQueryTerms)
		{
			listOb=queryTerms.get(s);//for all terms get postinglist
			fieldsGiven=listOb.getSearchedIn();
			postListPerTerm=listOb.getPostList();
			for(Integer docid : postListPerTerm.keySet())
			{
				tempInner=postListPerTerm.get(docid);
				for(Integer wt:tempInner.keySet())
				{
					flagMatched=true;
					fieldsExists=tempInner.get(wt);
					len=fieldsGiven.length();
					for(int i=0;i<len;i++)
					{
						if(fieldsExists.indexOf(fieldsGiven.charAt(i))==-1)
						{
							flagMatched=false;
							break;
						}
						
					}//flagMatching
					if(flagMatched)
					{
						if(finalList.containsKey(docid))
						{
							finalListInner=finalList.get(docid);
							for(Integer wtStored:finalListInner.keySet())
							{
								count=finalListInner.get(wtStored);
								count++;
								if(count==size)
								{
									ansList.put(docid,wtStored+wt);
									if(ansList.size()==10)
									{
										return;
									}
									finalList.remove(docid);
								}
								else
								{
									finalListInner.put(wtStored+wt,count);
									finalListInner.remove(wtStored);
								}
								break;
								
							}//for wtsorted
						}//if contains
						else
						{
							finalListInner=new HashMap<Integer, Integer>();
							finalListInner.put(wt,1);
							finalList.put(docid, finalListInner);
						}
					}
					
				}//only once for tempInner
				
							
				//finalList.put(ob.getDocId(),)
			}//next docid
		}//all terms over
	}


	private static void nextPostingList(String temp)
	{
		try
		{
			char firstChar;
			firstChar=temp.charAt(0);
			int fileOpen=firstChar-97;
			String stringKey;
			byte key[]=new byte[20];
			String arr[];
			int lengthTillOffset=0;
			PostingList ob=queryTerms.get(temp);
			indexReader[fileOpen].seek(ob.getOffset());
			if((indexReader[fileOpen].read(key, 0, 20))!=-1)
			{
				
				stringKey=new String(key);
				arr=stringKey.split(":");
				if(arr.length>1)//means we got offset too
				{
					lengthTillOffset=arr[0].length()+1;
					//ob.getPostList().add(postingList+"");
					addPostingList(temp,arr[0],20,lengthTillOffset,stringKey,fileOpen);
				}
				else//offest dint come
				{
					System.err.println("******invalid case offsets******");
					validQueryTerms.remove(temp);
				}
			}
			else
			{
				validQueryTerms.remove(temp);
				System.err.println("******************error reading**********");
			}
		}
		catch(Exception e)
		{
			validQueryTerms.remove(temp);
			e.printStackTrace();
		}
	}
	private static void startPostingList(String temp)//throws Exception
	{
		try
		{
			Entry<String, Long> entryOb;
			
			char firstChar;
			firstChar=temp.charAt(0);
			int fileOpen=firstChar-97;
			long start=0;
			long end=indexReader[fileOpen].length();
			entryOb=refIndex.floorEntry(temp);
			if(entryOb!=null&&entryOb.getKey().charAt(0)==firstChar)
			{
				start=entryOb.getValue();
			}
			entryOb=refIndex.ceilingEntry(temp);
			if(entryOb!=null&&entryOb.getKey().charAt(0)==firstChar)
			{
				end=entryOb.getValue();
			}
			//System.out.println(start +   "  "+end);
			long mid;
		
			int comparison;
			String stringKey;
			byte key[]=new byte[150];
			String arr[];
			int lengthTillOffset=0;
			while(start<=end)
			{
				mid=(start+end)/2;
				indexReader[fileOpen].seek(mid);
				indexReader[fileOpen].readLine();
				if((indexReader[fileOpen].read(key, 0, 150))!=-1)
				{
					stringKey=new String(key);
					arr=stringKey.split(":");
					comparison=arr[0].compareTo(temp);
					if(comparison==0)
					{
						
						if(arr.length>2)//means we got offset too
						{
							lengthTillOffset=arr[0].length()+2+arr[1].length();
							addPostingList(temp,arr[1],150,lengthTillOffset,stringKey,fileOpen);
							validQueryTerms.add(temp);
							return;
						}
						else//offest dint come
						{
							System.err.println("******invalid case offsets******");
							return;
						}
						
					}
					else if(comparison<0)
					{
						start=mid+1;//indexReader.getFilePointer();
					}
					else
					{
						end=mid-1;
					}
				}
				else
				{
					end=mid-1;
				}
			}
			return;
			
		}
		catch(Exception e)
		{
			return;
		}
		
	}


	private static void addPostingList(String temp, String offset, int i,
			int lengthTillOffset, String stringKey,int fileOpen) throws IOException
	{
		PostingList ob=queryTerms.get(temp);
		int remainingToRead=0,enterOffset=0;
		StringBuilder postingList=new StringBuilder();
		byte leftData[];
		remainingToRead=Integer.parseInt(offset)-(i-lengthTillOffset);
		enterOffset=stringKey.indexOf('\n');
		if(enterOffset==-1)
		{
			postingList.append(stringKey, lengthTillOffset, stringKey.length());
			if(remainingToRead>0)
			{
				leftData=new byte[remainingToRead];
				if((indexReader[fileOpen].read(leftData, 0, remainingToRead))!=remainingToRead)
				{
					System.err.println("******invalid parsing of offsets******");
				}
				postingList.append(new String(leftData));
				
			}
		}
		else
		{
			ob.setEnd(true);
			postingList.append(stringKey, lengthTillOffset, enterOffset+1);//to keep enter
		}
		
		postingList.deleteCharAt(postingList.length()-1);//to remove enter or :
		//System.out.println(postingList);
		ob.setPostList(postingList+"");
		ob.setOffset(indexReader[fileOpen].getFilePointer());
		
	}
	
	private static Map<Integer, Integer> sortByComparator(Map<Integer, Integer> unsortMap)
	{

	    List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(unsortMap.entrySet());

	    // Sorting the list based on values
	    Collections.sort(list, new Comparator<Entry<Integer, Integer>>()
	    {
	        public int compare(Entry<Integer, Integer> o1,
	                Entry<Integer, Integer> o2)
	        {
	           
	                int c= o2.getValue().compareTo(o1.getValue());
	                if(c==0)
	                	return o2.getKey().compareTo(o1.getKey());
	                else
	                	return c;
	        }
	    });
	    

	    // Maintaining insertion order with the help of LinkedList
	    Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
	    for (Entry<Integer, Integer> entry : list)
	    {
	        sortedMap.put(entry.getKey(), entry.getValue());
	        entry.setValue(null);
	    }

	    return sortedMap;
	}
	
	private static Map<Integer, Map<Integer,Integer>> sortByComparator2(Map<Integer, Map<Integer,Integer>> unsortMap)
	{

	    List<Entry<Integer, Map<Integer,Integer>>> list = new LinkedList<Entry<Integer, Map<Integer,Integer>>>(unsortMap.entrySet());

	    // Sorting the list based on values
	    Collections.sort(list, new Comparator<Entry<Integer, Map<Integer,Integer>>>()
	    {
	        public int compare(Entry<Integer, Map<Integer,Integer>> o1,
	                Entry<Integer, Map<Integer,Integer>> o2)
	        {
	           
	                Map<Integer,Integer> m1=o1.getValue();//wt count
	                Map<Integer,Integer> m2=o2.getValue();
	                m1=sortByComparator(m1);
	                m2=sortByComparator(m2);
	                o1.setValue(m1);
	                o2.setValue(m2);
	          //      o1.getValue().e
	                Integer key1=null,key2=null;
	                for(Integer k : m1.keySet())
	                	key1=m1.get(k);
	                for(Integer k : m2.keySet())
	                	key2=m2.get(k);
	                int c= key2.compareTo(key1);
	                if(c==0)
	                	return m2.get(key2).compareTo(m1.get(key1));
	                else
	                	return c;

	        }
	    });
	    

	    // Maintaining insertion order with the help of LinkedList
	    Map<Integer, Map<Integer,Integer>> sortedMap = new LinkedHashMap<Integer,Map<Integer,Integer>>();
	    for (Entry<Integer,Map<Integer,Integer>> entry : list)
	    {
	        sortedMap.put(entry.getKey(), entry.getValue());
	        entry.setValue(null);
	    }

	    return sortedMap;
	}
		
}
	