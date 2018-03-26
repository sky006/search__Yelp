/*
 *By: Qikai, Chen
 *ID: 500654338
 *Last modified: 3/16/2018 
 */
package fileProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/*
 * 1. change the file name to 1.....n
 * 2. add id to fileName*100+line
 * 3. add distance 0
 */
public class Preprocess {
	public static void process(String dataPath, String foodWordsFile){
		HashMap<String,Integer> urlList=new HashMap<String,Integer>();
		
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		//get the food words
		File fileFoodWords=new File(foodWordsFile);
		HashMap<String, Integer> foodWords=new HashMap<String,Integer>();
		try {
			fis=new FileInputStream(fileFoodWords);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			String line=br.readLine();
			String[] words=line.split(" ");
			for(String word:words) {
				foodWords.put(word, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//set the file names
		File file = new File(dataPath);
		File[] files = file.listFiles();
		Integer i=-1;
		for (File f : files) {
			i++;
			JSONParser parser = new JSONParser();
			JSONArray jObjects = new JSONArray();
			try {
				String json = null;
				fis = new FileInputStream(f);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);
				Integer id = -1;
				while ((json = br.readLine()) != null) {
					id++;
					// System.out.println("Hello");
					JSONObject jObject = (JSONObject) parser.parse(json);
					JSONObject commentJson=(JSONObject) parser.parse(jObject.get("Comment").toString());
					JSONObject newCommentJson=new JSONObject();
					for (Iterator iterator = commentJson.keySet().iterator(); iterator.hasNext();) {
						String name = iterator.next().toString();
						String[] comment=commentJson.get(name).toString().split("\\W+");
						HashSet<String> foods=new HashSet<String>();
						for(String word:comment) {
							if(foodWords.get(word)!=null) {
								foods.add(word);
							}
						}
						String newComment="";
						for(Iterator<String> iterator2=foods.iterator();iterator2.hasNext();) {
							newComment+=iterator2.next()+",";
						}
						newCommentJson.put(name, newComment);
					}
					jObject.remove("Comment");
					jObject.put("Comment", newCommentJson);
					jObject.put("ID", i.toString()+"-"+id.toString());
					jObject.put("Distance", "0");
					String url=jObject.get("URL").toString();
					if(urlList.get(url)==null) {
						urlList.put(url, 1);
						jObjects.add(jObject);
					}else {
						id--;
					}
				}
				br.close();
				isr.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
			try {
				File newfile = new File(i.toString());
				FileWriter fWriter = new FileWriter(dataPath+"/"+newfile);
				for (Object object : jObjects) {
					JSONObject jObject = (JSONObject) object;
					fWriter.write(jObject.toString());
					fWriter.write("\n");
				}
				fWriter.close();
			} catch (Exception e) {
				System.out.println("Exception 2");
				// TODO: handle exception
			}
			f.delete();
		}
//		System.out.println("Preprocess Finished!");
	}
}
