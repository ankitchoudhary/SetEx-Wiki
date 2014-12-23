/**
 * @author lovlean
 *
 */

package index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {

	private File filePartial;
	private static final long blockSize = 8589934592l;// 1024*1024*1024*8
	private static int fileCount = 0;
	private static int count_classifier = 0;

	public static int getCount_classifier() {
		return count_classifier;
	}

	public static void setCount_classifier(int count_classifier) {
		XMLHandler.count_classifier = count_classifier;
	}

	private String oPath;
	private BufferedWriter writer;
	private BufferedWriter writerTitleRef;
	private BufferedWriter classifier_write;

	// infocat_writer is used for creating index on infobox and category
	private BufferedWriter infocat_writer;
	private RandomAccessFile writerTitle;
	private PrintWriter errorWriter;
	private TreeMap<String, HashMap<Character, Integer>> freqCount;
	private HashMap<Character, Integer> innerMap;
	private boolean firstId = false;
	private boolean pageId = false;
	private boolean textSet = false;
	private boolean titleSet = false;
	private String docId;
	private StemmingPorter s = new StemmingPorter();
	private StringBuilder sb = new StringBuilder();
	private StringBuilder temp = new StringBuilder();
	private StringBuilder textString = new StringBuilder();
	private StringBuilder titleString = new StringBuilder();
	private StringBuilder infoboxString = new StringBuilder();
	private StringBuilder buffer_text = new StringBuilder();
	private static int totDocCount = 0;
	TreeSet<String> word_set = new TreeSet<String>();

	public static int getTotDocCount() {
		return totDocCount;
	}

	public static void setTotDocCount(int x) {
		totDocCount = x;
	}

	public static void setFileCount(int x) {
		fileCount = x;
	}

	public static int getFileCount() {
		return fileCount;
	}

	private int count = 0;

	public void startDocument() throws SAXException {
		oPath = MainIndex.getoPath();
		freqCount = new TreeMap<String, HashMap<Character, Integer>>();
		try {
			filePartial = new File(oPath + "/index" + fileCount + ".txt");
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePartial)));
			writerTitleRef = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(oPath + "/titleRef.txt")));
			infocat_writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(oPath + "/infocat_writer.txt")));
			classifier_write = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(oPath + "/classifier0.txt")));
			File titleFile = new File(oPath + "/title.txt");
			if (titleFile.exists())
				titleFile.delete();
			writerTitle = new RandomAccessFile(titleFile, "rw");
			errorWriter = new PrintWriter(oPath + "/error.txt");
		} catch (IOException e) {
			System.err.println("Error creating output index file");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(docId);
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		try {
			if ("id".equalsIgnoreCase(localName) && pageId == false) {
				firstId = true;
				pageId = true;
			} else if ("text".equalsIgnoreCase(localName)) {
				textSet = true;
				textString.setLength(0);
			} else if ("title".equalsIgnoreCase(localName)) {
				titleSet = true;
				titleString.setLength(0);
			}

		} catch (Exception e) {
			System.out.println(docId);
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
	}

	public void characters(char ch[], int start, int length)
			throws SAXException {
		try {
			if (firstId) {
				docId = new String(ch, start, length);// TODO handle noise
				firstId = false;
			} else if (titleSet)
				titleString.append(ch, start, length);
			else if (textSet)
				textString.append(ch, start, length);
		} catch (Exception e) {
			System.out.println(docId);
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
	}

	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		try {
			if ("page".equalsIgnoreCase(localName)) {

				pageId = false;
				totDocCount++;
				if (totDocCount % 500 == 0) {
					writerTitleRef.write(docId + ":"
							+ writerTitle.getFilePointer() + "\n");
				}
				writerTitle.writeBytes(docId + ":" + titleString + "\n");
				int noOfTokens = 0;
				int termFrequency;
				noOfTokens = freqCount.size();
				for (String a : freqCount.keySet()) {
					termFrequency = 0;
					sb.append(a);
					sb.append(":");
					sb.append(docId);
					sb.append(":");
					innerMap = freqCount.get(a);
					for (Character l : innerMap.keySet()) {
						// sb.append(l);
						if (l == 't')
							termFrequency += innerMap.get(l) * 7;
						else if (l == 'b')
							termFrequency += innerMap.get(l) * 2;
						else
							termFrequency += innerMap.get(l);

					}
					// sb.append(":");
					sb.append((double) termFrequency / noOfTokens);
					sb.append("\n");
				}
				if (filePartial.length() + sb.length() > blockSize) {
					writer.close();
					filePartial = new File(oPath + "/index" + ++fileCount
							+ ".txt");
					writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(filePartial)));
				}
				writer.write(sb.toString());
				sb.setLength(0);
				freqCount.clear();
			} else if (textSet) {
				textSet = false;
				if (!Normalizer.isNormalized(textString, Form.NFD)) {
					textString = new StringBuilder(Normalizer.normalize(
							textString, Form.NFD).replaceAll(
							"\\p{InCombiningDiacriticalMarks}+", ""));
				}
				parseText(textString, 'b');

			} else if (titleSet) {
				titleSet = false;
				if (!Normalizer.isNormalized(titleString, Form.NFD)) {
					titleString = new StringBuilder(Normalizer.normalize(
							titleString, Form.NFD).replaceAll(
							"\\p{InCombiningDiacriticalMarks}+", ""));
				}
				parseString(titleString, 't');
			}
		} catch (Exception e) {
			System.out.println(docId);
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
	}

	private void parseText(StringBuilder str, char value) {
		// comment, citation reference, file
		str = new StringBuilder(
				Pattern.compile(
						"<(\\s)*!(\\s)*-(\\s)*-(\\s)*(.)*?-(\\s)*-(\\s)*>|<(.)*?>|<ref>(.)*?</ref>|[Ff](\\s)*[Ii](\\s)*[Ll](\\s)*[Ee](\\s)*:(.)*?\\|")
						.matcher(str).replaceAll(""));
		Pattern infoP = Pattern
				.compile("\\{(\\s)*\\{(\\s)*[iI](\\s)*[nN](\\s)*[fF](\\s)*[oO](\\s)*b(\\s)*o(\\s)*x");
		Matcher infoM = infoP.matcher(str);
		int countOfCurly;
		int len = str.length();
		char c;
		int x = 0, start = 0;
		if (infoM.find()) {
			countOfCurly = 2;
			for (x = infoM.end(); x < len; x++) {
				c = str.charAt(x);
				if (c == '{')
					countOfCurly++;
				else if (c == '}')
					countOfCurly--;
				infoboxString.append(c);
				if (countOfCurly == 0)
					break;

			}
			str = str.replace(infoM.start(), x + 1, "");
			parseString(infoboxString, 'i');
			infoboxString.setLength(0);
		}
		len = str.length();
		Pattern linkP = Pattern
				.compile("=(\\s)*=(\\s)*[Ee](\\s)*[Xx](\\s)*[Tt](\\s)*[Ee](\\s)*[Rr](\\s)*[Nn](\\s)*[Aa](\\s)*[Ll](\\s)+[Ll](\\s)*[Ii](\\s)*[Nn](\\s)*[Kk](\\s)*[Ss](\\s)*=(\\s)*=");
		Matcher linkM = linkP.matcher(str);

		if (linkM.find()) {
			start = linkM.start();
			boolean flag = true;
			Pattern linkP1 = Pattern.compile("http(s)?:(.)*?(\\s)+");
			Matcher linkM1 = linkP1.matcher(str);
			while (linkM1.find(start)) {
				countOfCurly = 1;
				for (x = linkM1.end(); x < len; x++) {
					flag = false;
					c = str.charAt(x);
					if (c == '[')
						countOfCurly++;
					else if (c == ']')
						countOfCurly--;
					infoboxString.append(c);
					if (countOfCurly == 0)
						break;
				}
				infoboxString.append(" ");
				start = x;

			}
			if (flag == false) {
				str = str.replace(linkM.start(), x + 1, "");
				parseString(infoboxString, 'l');
				infoboxString.setLength(0);
			}
		}

		len = str.length();
		Pattern categoryP = Pattern
				.compile("\\[(\\s)*\\[(\\s)*[Cc](\\s)*[Aa](\\s)*[Tt](\\s)*[Ee](\\s)*[Gg](\\s)*[Oo](\\s)*[Rr](\\s)*[Yy](\\s)*:(\\s)*");
		Matcher categoryM = categoryP.matcher(str);

		boolean flag = true;
		while (categoryM.find()) {
			if (flag) {
				start = categoryM.start();
				flag = false;
			}
			countOfCurly = 2;
			for (x = categoryM.end(); x < len; x++) {
				// int lastStart=0;
				c = str.charAt(x);
				if (c == '[')
					countOfCurly++;
				else if (c == ']')
					countOfCurly--;
				infoboxString.append(c);
				if (countOfCurly == 0)
					break;
			}
			infoboxString.append(" ");
		}

		if (flag == false) {
			str = str.replace(start, x + 1, "");
			parseString(infoboxString, 'c');
			infoboxString.setLength(0);
		}
		StringBuilder text = new StringBuilder();
		StringBuilder text_word = new StringBuilder();
		StringBuilder word = new StringBuilder();
		int len_text = str.length();
		for (int i = 0; i + 1 < len_text; ++i) {

			if (str.charAt(i) == '=' && str.charAt(i + 1) == '=') {
				break;
			}
			text.append(str.charAt(i));
		}

		text = new StringBuilder(text.toString().toLowerCase());
		int flag3 = 0;
		word_set.clear();
		for (int i = 0; i < text.length(); ++i) {
			if (text.charAt(i) >= 'a' && text.charAt(i) <= 'z') {
				flag3 = 1;
				word.append(text.charAt(i));
			} else {
				if (flag3 == 1) {
					flag3 = 0;
					if (word.length() > 2) {
						word_set.add(word.toString());
						// text_word.append(word);

						// text_word.append(":");
						// String faltu = new String();
						// faltu = word + ":" + "t" + ":" + docId + "\n";
						// if (faltu.length() + buffer_text.length() <=
						// blockSize) {
						// buffer_text.append(faltu);
						// } else {
						// try {
						// classifier_write.write(buffer_text + "");
						// } catch (IOException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						// buffer_text.setLength(0);
						// try {
						// classifier_write.flush();
						// classifier_write.close();
						// } catch (IOException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						//
						// count_classifier++;
						// setCount_classifier(count_classifier);
						// try {
						// classifier_write = new BufferedWriter(
						// new OutputStreamWriter(
						// new FileOutputStream(oPath
						// + "/classifier"
						// + count_classifier + ".txt")));
						// } catch (FileNotFoundException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						// if (faltu.length() > 0) {
						// buffer_text.append(faltu);
						// }
						//
						// }

					}
					word.setLength(0);
				}
			}
		}
		try {
			// if(buffer_text.length()>0){
			//
			// try {
			// //System.out.println(buffer_text);
			// classifier_write.write(buffer_text+"");
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// buffer_text.setLength(0);
			// }
			//
			// infocat_writer.write(docId + ":" + "t" + ":" + text_word + "\n");
			text_word.setLength(0);

			for (String i : word_set) {
				text_word.append(":" + i);

				buffer_text.append(i + ":" + "t" + ":" + docId + "\n");

			}
			if (buffer_text.length() > blockSize) {
				classifier_write.write(buffer_text + "");
				buffer_text.setLength(0);
				classifier_write.flush();
				classifier_write.close();

				count_classifier++;

				classifier_write = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(oPath + "/classifier"
								+ count_classifier + ".txt")));

			}
			infocat_writer.write(docId + ":" + "t" + text_word + "\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		str = new StringBuilder(Pattern.compile("=(\\s)*=(\\s)*(.)*?=(\\s)*=")
				.matcher(str).replaceAll(""));
		parseString(str, 'b');
		infoboxString.setLength(0);

	}

	private void parseString(StringBuilder str, char value) {
		word_set.clear();
		char c;
		int len2;
		temp.setLength(0);

		str = new StringBuilder(str.toString().toLowerCase());
		len2 = str.length();
		String faltu = new String();
		if (value == 'i') {
			StringBuilder temp1 = new StringBuilder(temp);
			try {
				infocat_writer.write(docId + ":" + "i" + ":");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int flag = 0;
			int flag1 = 0;
			for (int i = 0; i < len2; ++i) {
				c = str.charAt(i);
				if (c == '|' && i>0 && str.charAt(i-1)=='\n') {
					flag = 1;
					temp1.setLength(0);
					continue;
				}
				if (flag == 1) {
					
					if (c == '=' || c==' ' || c=='\n') {
						try {

							if(temp1.length()>0)
							{
								flag=0;
							}
							flag1 = 1;
							if(temp1.length()>0 && temp1.charAt(0)!='\n'){
								faltu = temp1 + ":" + "i" + ":" + docId + "\n";
								infocat_writer.write(temp1 + ":");
							}
							
							
							else 
							{
								faltu="";
							}
							
							temp1.setLength(0);
							if (faltu.length() + buffer_text.length() <= blockSize) {
								if(faltu.length()>0){
									buffer_text.append(faltu);
								}
							}
							else 
							{
								if(buffer_text.length()>0){
									classifier_write.write(buffer_text + "");
									buffer_text.setLength(0);
									classifier_write.flush();
									classifier_write.close();

									count_classifier++;
									setCount_classifier(count_classifier);
									classifier_write = new BufferedWriter(
											new OutputStreamWriter(
													new FileOutputStream(oPath
															+ "/classifier"
															+ count_classifier
															+ ".txt")));
									if (faltu.length() > 0) {
										buffer_text.append(faltu);
									}

								}
								
								
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						flag = 0;

						// add2Map(value);
						continue;
					}
					if((c>='0' && c<='9')||(c>='a' && c<='z')||c=='_')
						temp1.append(c);

				}

			}
			if (buffer_text.length() > 0) {
				try {
					classifier_write.write(buffer_text + "");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer_text.setLength(0);
			}
			if (flag1 == 1) {
				try {
					infocat_writer.write("\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if (value == 'c') {
				StringBuilder temp1 = new StringBuilder(temp);
				try {
					infocat_writer.write(docId + ":" + "c" + ":");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int flag2 = 0;
				for (int i = 0; i < len2; ++i) {
					c = str.charAt(i);
					if (c == ']') {
						try {

							if (temp1.length() > 0) {
								flag2 = 1;
								faltu = temp1 + ":" + "c" + ":" + docId;
								faltu = faltu.trim();
								faltu = faltu + "\n";
								infocat_writer.write(temp1 + ":");
								temp1.setLength(0);
								if (faltu.length() + buffer_text.length() <= blockSize) {
									if(faltu.length()>0){
										buffer_text.append(faltu);
									}
									
								} else {
									classifier_write.write(buffer_text + "");
									buffer_text.setLength(0);
									classifier_write.flush();
									classifier_write.close();

									count_classifier++;
									setCount_classifier(count_classifier);
									classifier_write = new BufferedWriter(
											new OutputStreamWriter(
													new FileOutputStream(oPath
															+ "/classifier"
															+ count_classifier
															+ ".txt")));
									if (faltu.length() > 0) {
										buffer_text.append(faltu);
									}

								}

							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// add2Map(value);
						continue;
					} else {
						temp1.append(c);
					}
				}
				if (buffer_text.length() > 0) {
					try {
						classifier_write.write(buffer_text + "");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					buffer_text.setLength(0);
				}
				try {
					if (flag2 == 1) {
						infocat_writer.write("\n");
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; i < len2; i++) {
			c = str.charAt(i);
			if (c >= 'a' && c <= 'z') {
				temp.append(c);
			} else {
				add2Map(value);
			}

		}
		add2Map(value);
	}

	private void add2Map(char value) {

		int len = temp.length();
		String u;
		if (len > 1 && Utils.stopWordSet.contains(temp.toString()) == false)
		// ignore
		// <2
		{
			s.add(temp, len);
			s.stem();
			u = s.toString();
			if (u.length() > 1 && Utils.stopWordSet.contains(u) == false)// TODO
																			// ignore
																			// <2
			{
				count = 0;
				if (freqCount.containsKey(u)) {
					innerMap = freqCount.get(u);
					if (innerMap.containsKey(value)) {
						count = innerMap.get(value);
					}
					innerMap.put(value, count + 1);
				} else {
					innerMap = new HashMap<Character, Integer>();
					innerMap.put(value, 1);
					freqCount.put(u, innerMap);
				}
			}
		}
		temp.setLength(0);

	}

	public void endDocument() throws SAXException {
		try {
			writer.flush();
			writer.close();
			if (buffer_text.length() > 0) {
				classifier_write.write(buffer_text + "");
				buffer_text.setLength(0);

			}
			writerTitle.close();

			infocat_writer.flush();
			infocat_writer.close();
			writerTitleRef.flush();
			writerTitleRef.close();
			classifier_write.flush();
			classifier_write.close();

		} catch (Exception e) {
			//System.out.println(docId);
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
			errorWriter.close();
			System.exit(1);
		}
	}

}
