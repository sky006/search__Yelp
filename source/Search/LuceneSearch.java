/*
 *By: Qikai, Chen
 *ID: 500654338
 *Last modified: 3/16/2018 
 */
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class LuceneSearch {
	public static JSONObject search(String keywords) {
		//windows
		String indexDir="D:\\sourceData\\luceneIndex";
		//linux
		//String indexDir="/usr/local/sourceDataluceneIndex";
//		String res = "";
		JSONObject results = new JSONObject();
		DirectoryReader reader = null;
		Integer id = 0;
		try {
			Directory directory = FSDirectory.open(Paths.get(indexDir));
			reader = DirectoryReader.open(directory);
			IndexSearcher searcher = new IndexSearcher(reader);

			// set special Analyzer for special field
			Map<String, Analyzer> analyzerPerField = new HashMap<>();
			analyzerPerField.put("Name", new KeywordAnalyzer());
			analyzerPerField.put("URL", new KeywordAnalyzer());
			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
			String[] fields = { "Name", "Comment", "Moreinfo", "FromBusiness", "Category" };
			// boots={10000,0,50,50,100}
			HashMap<String, Float> boosts = new HashMap<String, Float>();
			boosts.put("Name", (float) 10000);
			boosts.put("Moreinfo", (float) 50);
			boosts.put("FromBusiness", (float) 50);
			boosts.put("Category", (float) 100);

			MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, aWrapper, boosts);
			Query query = parser.parse(keywords);
			// pick the top 100 results
			TopDocs topDocs = searcher.search(query, 1000);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println("Result: " + scoreDocs.length);

//			for (ScoreDoc sd : scoreDocs) {
//				id++;
//				Document d = searcher.doc(sd.doc);
//				res += id + ". " + d.get("Name") + "-------------- " + d.get("Address") + "\n";
//			}
			////////////////////////////////////////////////
			for (ScoreDoc sd : scoreDocs) {
//				if(id>100)break;
				Document document = searcher.doc(sd.doc);
				JSONObject result = new JSONObject();
				//transfer document to JSONObject
				result.put("ID", document.get("ID"));
				result.put("Name", document.get("Name"));
				result.put("URL", document.get("URL"));
				result.put("Address", document.get("Address"));
				result.put("Category", document.get("Category"));
				result.put("FromBusiness", document.get("FromBusiness"));
				result.put("Moreinfo", document.get("Moreinfo"));
				result.put("Picture", document.get("Picture"));
				result.put("latitude", document.get("latitude"));
				result.put("longitude", document.get("longitude"));
				result.put("Comment", document.get("Comment"));
				result.put("Rate", document.get("Rate"));
				result.put("Distance", document.get("Distance"));
				result.put("Price", document.get("Price"));
				results.put(id.toString(), result);
				id++;
			}
			////////////////////////////////////////////////
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return res;
		JSONObject returnRes=new JSONObject();
		returnRes.put("size", id.toString());
		returnRes.put("results", results.toString());
		return returnRes;
	}
}
