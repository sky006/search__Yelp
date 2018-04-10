/*
 *By: QC
 *Last modified: 3/16/2018 
 */
package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class indexBuilder {
	public static void buildIndex(String indexDir, String dataDir) {
		IndexWriter writer = null;
		try {
			Directory directory = FSDirectory.open(Paths.get(indexDir));
			// set special Analyzer for special field
			Map<String, Analyzer> analyzerPerField = new HashMap<>();
			analyzerPerField.put("Name", new KeywordAnalyzer());
			analyzerPerField.put("URL", new KeywordAnalyzer());
			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(aWrapper);
			writer = new IndexWriter(directory, indexWriterConfig);
			Document document = null;
			int cnt = 0;
			File files = new File(dataDir);
			for (File file : files.listFiles()) {
				// File Reader
//				System.out.println(file.getName());
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
				String json = null;
				while ((json = br.readLine()) != null) {
					// JSONParser
					JSONParser parser = new JSONParser();
					JSONObject restaurant = (JSONObject) parser.parse(json);
//					System.out.println(restaurant.get("Name"));
					document = new Document();
					document.add(new IntPoint("ID", cnt));
					document.add(new TextField("Name", (String) restaurant.get("Name"), Field.Store.YES));
					document.add(new TextField("Name_Seperate", (String) restaurant.get("Name"), Field.Store.YES));
					document.add(new TextField("URL", (String) restaurant.get("URL"), Field.Store.YES));
					document.add(new TextField("Address", (String) restaurant.get("Address"), Field.Store.YES));
					document.add(new TextField("Category", (String) restaurant.get("Category"), Field.Store.YES));
					document.add(new TextField("Picture", (String) restaurant.get("Picture"), Field.Store.YES));
					document.add(new TextField("Rate", (String) restaurant.get("Rate"), Field.Store.YES));
					document.add(new TextField("Price", (String) restaurant.get("Price"), Field.Store.YES));
					document.add(new TextField("longitude", (String) restaurant.get("longitude"), Field.Store.YES));
					document.add(new TextField("latitude", (String) restaurant.get("latitude"), Field.Store.YES));
					if (restaurant.get("FromBusiness") != null) {
						document.add(new StringField("FromBusiness", (String) restaurant.get("FromBusiness"),
								Field.Store.YES));
					} else {
						document.add(new StringField("FromBusiness", "", Field.Store.YES));
					}
					if (restaurant.get("Moreinfo") != null) {
						document.add(new StringField("Moreinfo", (String) restaurant.get("Moreinfo"), Field.Store.YES));
					} else {
						document.add(new StringField("Moreinfo", "", Field.Store.YES));
					}
					// comment
					JSONObject comment = (JSONObject) restaurant.get("Comment");
					String comments = "";
					for (Iterator iterator = comment.keySet().iterator(); iterator.hasNext();) {
						comments += ((String) comment.get(iterator.next()));
					}
					document.add(new TextField("Comment", comments, Field.Store.YES));

					writer.addDocument(document);
				}
				br.close();
				isr.close();
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
