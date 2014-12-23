package index;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;


public class SecIndex 
{
	
	String indexesPath;
	String prim_index;
	String sec_index;
	
	public SecIndex() {
		// TODO Auto-generated constructor stub
		//Configurations cc = new Configurations();
		indexesPath = "H:\\ErLovlean\\iiith\\sem2\\ire_mini\\";
		//prim_index = "prim_index.txt";
		sec_index = "sec_index.txt";
	}
	
	public static void main(String[] args) {
		new SecIndex().create_secondary_index();
	}
	public void create_secondary_index() 
	{
		try
		{
			BufferedWriter secondary_index_file = new BufferedWriter(new FileWriter(new File(indexesPath + sec_index))); //
			int j=0;
			//long old=0;
		
			//for(int i=0;i<26;i++)
			//{
				RandomAccessFile primary_index_file = new RandomAccessFile(indexesPath + "sorted_s.txt", "r");
				String line = primary_index_file.readLine();
				secondary_index_file.write(line.split(":")[0]+":"+(primary_index_file.getFilePointer()-line.length()-1)+""+"\n");
				
				j=0;
//				old=
				while(line != null)
				{
					if(j==15)
					{
						j=0;
						secondary_index_file.write(line.split(":")[0]+":"+(primary_index_file.getFilePointer()-line.length()-1)+""+"\n");
					}
	             // primary_index_file.seek(primary_index_file.getFilePointer()+1000);
					j++;
					line=primary_index_file.readLine();
	            //  line = primary_index_file.readLine();
				}
	//			secondary_index_file.write(line.split(":")[0]+":"+(primary_index_file.getFilePointer()-line.length()-1)+""+"\n");
				
				primary_index_file.close();
		//	}
			secondary_index_file.close();
	
		}
		catch(Exception e)
		{
			System.out.println("My Error  : " + e.getMessage());
		}
	}

}
