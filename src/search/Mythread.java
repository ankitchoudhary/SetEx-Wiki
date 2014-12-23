package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import java.util.Map.Entry;

import index.StemmingPorter;
import index.Utils;

public class Mythread extends Thread 
{
	PostingList ob;
	String line;
	StemmingPorter stemmer = new StemmingPorter();
	RandomAccessFile indexReader;
	String outDir;
	TreeMap<String, Long> refIndex;
	boolean valid=false;
	
	public boolean isValid() {
		return valid;
	}


	public PostingList getOb() {
		return ob;
	}

	
	public String getLine() {
		return line;
	}

	
	
	
	Mythread(String s,String d,TreeMap<String, Long> ref)
	{
		line=s;
		outDir=d;
		refIndex=ref;
	}
	
	public void run()
	{
		char l;
		String field="";
		String temp="";
		line=line.toLowerCase();
		for(int k=0;k<line.length();k++)
		{
			l=line.charAt(k);
			if(l<='z'&&l>='a')
				temp+=l;
			else if(l==':')
			{
				field=temp;
				temp="";
			}
			ob=new PostingList();
			ob.setSearchedIn(field);
			if(temp.length()>=2&&!(Utils.stopWordSet.contains(temp)))
			{
				stemmer.add(new StringBuilder(temp),temp.length());
				stemmer.stem();
	            temp= stemmer.toString();
	            if(temp.length()>=2&&!(Utils.stopWordSet.contains(temp)))
	            	startPostingList(temp);
			}	
		}
	}
	
	private void startPostingList(String temp)//throws Exception
	{
		try
		{
			Entry<String, Long> entryOb;
			indexReader=new RandomAccessFile(outDir+"/sorted_"+temp.charAt(0)+".txt", "r");
			char firstChar;
			firstChar=temp.charAt(0);
			int fileOpen=firstChar-97;
			long start=0;
			long end=indexReader.length();
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
				indexReader.seek(mid);
				indexReader.readLine();
				if((indexReader.read(key, 0, 150))!=-1)
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
							valid=true;
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
	

	private void addPostingList(String temp, String offset, int i,
			int lengthTillOffset, String stringKey,int fileOpen) throws IOException
	{
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
				if((indexReader.read(leftData, 0, remainingToRead))!=remainingToRead)
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
		ob.setOffset(indexReader.getFilePointer());
		
	}
	
}
