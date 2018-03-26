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
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class seperateIndex {
	public static void seperate(String indexPath,String outIndexPath) {
		JSONObject indexObjects = new JSONObject();
		File file = new File(indexPath);
		File[] files = file.listFiles();
		for (File f : files) {
			JSONParser parser = new JSONParser();
			FileInputStream fis = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {
				String json = null;
				fis = new FileInputStream(f);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);
				while ((json = br.readLine()) != null) {
					JSONObject jObject = (JSONObject) parser.parse(json);
					for (Iterator iterator = (Iterator) jObject.keySet().iterator(); iterator.hasNext();) {
						String word = (String) iterator.next();
						String index = (String) jObject.get(word);
						// System.out.println(word + "---" + String.valueOf(word.charAt(0)));
						if (word.length() < 1)
							continue;
						JSONObject indexObject = (JSONObject) indexObjects.get(String.valueOf(word.charAt(0)));
						if (indexObject == null) {
							indexObject = new JSONObject();
						}
						indexObject.put(word, index);
						indexObjects.remove(String.valueOf(word.charAt(0)));
						indexObjects.put(String.valueOf(word.charAt(0)), indexObject);
					}
				}
				br.close();
				isr.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (Iterator iterator = (Iterator) indexObjects.keySet().iterator(); iterator.hasNext();) {
			try {
				String name = (String) iterator.next();
				// System.out.println(name);
				File newfile = new File(name);
				FileWriter fWriter = new FileWriter(outIndexPath+"/" + newfile);
				JSONObject index = (JSONObject) indexObjects.get(name);
				fWriter.write(index.toString());
				fWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
