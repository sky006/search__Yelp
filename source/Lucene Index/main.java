/*
 *By: Qikai, Chen
 *ID: 500654338
 *Last modified: 3/16/2018 
 */
package index;

import java.util.Iterator;

import org.json.simple.JSONObject;

public class main {
	public static void main(String args[]) {
//		System.out.println("Hello world");
//		long startTime=System.currentTimeMillis();
//		long endTime=System.currentTimeMillis();
//		System.out.println(endTime-startTime);
//		System.out.println("build finish");
		/*search engine
		 * where index is the directory of index
		 * new is the keyword
		 */
//		JSONObject results=indexSearcher.searchIndex("luceneIndex","new");
//		for(Iterator iterator =results.keySet().iterator();iterator.hasNext();) {
//			String id=(String)iterator.next();
//			JSONObject result=(JSONObject) results.get(id);
//			System.out.println(result.get("Name"));
//		}
		if(args[0].equals("index")) {
			indexBuilder.buildIndex(args[1],args[2]);
		}else {
			System.out.println("Wrong arguments");
		}
	}
}
