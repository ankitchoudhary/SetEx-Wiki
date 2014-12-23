package index;

import java.util.SortedSet;
import java.util.TreeSet;


public class Utils 
{

	public static SortedSet<String> stopWordSet;
	
	static
	{
		stopWordSet=new TreeSet<String>();
		String stopWord="a,able,about,across,after,all,almost,also,am,among,amp,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,gt,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,lt,may,me,might,most,must,my,nbsp,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
		String[] stopWordList=stopWord.split(",");
		for(String s: stopWordList)
		{
			stopWordSet.add(s);
		}
	}
}
