/*
 *By: Qikai, Chen
 *ID: 500654338
 *Last modified: 3/16/2018 
 */
package fileProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/*
 * this is alpha version of indexSearch on hadoop index
 * formal version is in serach function
 */
public class SearchIndex {
	public static void search(String keywords) {
		String[] words = keywords.split("\\W+");
		HashMap<String, Double> searchResults = new HashMap<String, Double>();
		for (int i = 0; i < words.length; i++) {
			HashMap<String, Double> keyResult = searchIndex("keyIndex", words[i]);
			// HashMap<String, Double> commentResult = new HashMap<String, Double>();
			HashMap<String, Double> commentResult = searchIndex("commentIndex", words[i]);
			mapCombine(searchResults, 1D, mapCombine(keyResult, 100D, commentResult, 1D), 1D);
		}
		System.out.println("Size: " + searchResults.size());
		JSONObject resultDocs = searchDocs("data", searchResults);
		// System.out.println(resultDocs.toString());
	}

	public static HashMap<String, Double> searchIndex(String indexPath, String keyword) {
		HashMap<String, Double> result = new HashMap<String, Double>();
		String path = indexPath + "/" + keyword.charAt(0);
		File file = new File(path);
		try {
			JSONParser parser = new JSONParser();
			JSONObject index = (JSONObject) parser.parse(new FileReader(file));
			Object indexObject = index.get(keyword);
			JSONObject indexes = new JSONObject();
			if (indexObject != null) {
				indexes = (JSONObject) parser.parse(indexObject.toString());
			}
			for (Iterator iterator = indexes.keySet().iterator(); iterator.hasNext();) {
				String doc = iterator.next().toString();
				Double freq = Double.parseDouble(indexes.get(doc).toString());
				result.put(doc, freq);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * use the index information to select the source docs from the source file
	 * input: <"1-1",1> <"filename-line",score> output: JSONObject of restaurants
	 * with an additional field--score
	 */
	public static JSONObject searchDocs(String dataPath, HashMap<String, Double> indexResult) {
		JSONObject docResult = new JSONObject();
		JSONParser parser = new JSONParser();
		Integer cnt = 0;
		for (Iterator iterator = indexResult.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String docID = entry.getKey().toString();
			String score = entry.getValue().toString();
			String[] docInfo = docID.split("-");
			String docName = docInfo[0];
			String docLine = docInfo[1];
			int line = Integer.parseInt(docInfo[1]);
			String path = dataPath + "/" + docName;
			File file = new File(path);
			try {
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
				int i = -1;
				String json = "";
				while ((json = br.readLine()) != null) {
					i++;
					if (i == line) {
						JSONObject restaurant = (JSONObject) parser.parse(json);
						restaurant.put("Score", score);
						restaurant.remove("Comment");
						docResult.put(cnt.toString(), restaurant);
						cnt++;
					}
				}
				br.close();
				isr.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		System.out.println("Search Finish");
		return docResult;
	}

	// add result2 to result1
	public static HashMap<String, Double> mapCombine(HashMap<String, Double> map1, Double weight1,
			HashMap<String, Double> map2, Double weight2) {
		System.out.println("map1: " + map1.size());
		HashMap<String, Double> combineResult = map1;
		System.out.println("combine: " + combineResult.size());
		for (Iterator iterator = map2.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next().toString();
			Double freq2 = map2.get(key);
			if (combineResult.get(key) != null) {
				Double freq1 = combineResult.get(key);
				combineResult.remove(key);
				combineResult.put(key, freq1 + freq2);
			} else {
				combineResult.put(key, freq2);
			}
		}
		return combineResult;
	}
}
