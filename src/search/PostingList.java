package search;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

class PostObject
{
	private int docId;
	int weight;
	private String fields;
	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getFields() {
		return fields;
	}
	
	public void setFields(String fields) {
		this.fields = fields;
	}
	
}

public class PostingList
{
	//LinkedHashMap<Integer,HashMap<Integer,String>>postMap;
	private LinkedHashMap<Integer,HashMap<Integer,String>> postList;
	private boolean end;//set
	private long offset;//set
	private String searchedIn;//populated
	
	public PostingList()
	{
		this.end = false;
		this.postList=new LinkedHashMap<Integer,HashMap<Integer,String>>();
	}
	
	
	public String getSearchedIn() {
		return searchedIn;
	}
	public void setSearchedIn(String searchedIn) {
		this.searchedIn = searchedIn;
	}
	
	public void setPostList(String postStr)
	{
		char c;
		int len,i;
		//PostObject ob;
		String f="";
		int docId=0;
		int wt=0;
		boolean flag=false;
		String temp[]=postStr.split(":");
		postList.clear();
		for(String s: temp)
		{
			f="";
			docId=0;
			wt=0;
			flag=false;
			len=s.length();
			for(i=0;i<len;i++)
			{ //docid fields wt
				c=s.charAt(i);
				if(c>='a'&&c<='z')
				{
					f+=c;
					flag=true;
				}
				else if(flag)
				{
					wt=wt*10+(c-'0');
				}
				else
				{
					docId=docId*10+(c-'0');
				}
			}
			/*ob=new PostObject();
			ob.setDocId(docId);
			ob.setFields(f);
			ob.setWeight(wt);
			postList.add(ob);*/
			HashMap<Integer,String> inner;
			
			inner=new HashMap<Integer,String>();
			inner.put(wt,f);
			postList.put(docId,inner);
		}
	}
	public LinkedHashMap<Integer,HashMap<Integer,String>> getPostList() {
		return postList;
	}

	public boolean isEnd() {
		return end;
	}
	public void setEnd(boolean end) {
		this.end = end;
	}
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	public void printPostingList()
	{
		//StringBuilder sb= new StringBuilder();
		for(Integer ob:postList.keySet())
		{
			System.out.print(ob);
			for(Integer ob1: postList.get(ob).keySet())
			{
				System.out.print(ob1);
				System.out.println(postList.get(ob).get(ob1));
				
				
			}
			
			System.out.print(":");
		}
	}


}
