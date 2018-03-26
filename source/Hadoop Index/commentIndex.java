/*
 *By: Qikai, Chen
 *ID: 500654338
 *Last modified: 3/16/2018 
 */
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonObject;
import com.zackehh.outputformat.JsonOutputFormat;

public class WordCount {
	// mapper: words--><word:id,1>
	public static class TokenizerMapper extends Mapper<Object, Text, Text, Text> {
		private Text mapperOutKey = new Text();
		private Text mapperOutVal = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String str = "";
			JSONObject jObject = null;
			String id = "";
			try {
				JSONParser parser = new JSONParser();
				jObject = (JSONObject) parser.parse(value.toString());
//				System.out.println(jObject.get("Name"));
//				str += jObject.get("Name")+" ";
//				str+=jObject.get("Category")+" ";
//				str+=jObject.get("Address")+" ";
//				str+=jObject.get("FromBusiness")+" ";
//				str+=jObject.get("Moreinfo")+" ";
				//get comments
				String comments=jObject.get("Comment").toString();
				JSONObject commentJson=(JSONObject) parser.parse(comments);
				for (Iterator iterator = (Iterator) commentJson.keySet().iterator(); iterator.hasNext();) {
					String person = (String) iterator.next();
					String comment = (String) jObject.get(person);
					str+=comment+" ";
				}
				//
				id = (String) jObject.get("ID");
			} catch (Exception e) {
				e.printStackTrace();
			}
			String[] words = str.split("\\W+");
			for (int i = 0; i < words.length; i++) {
				String keyStr = words[i].toLowerCase() + ":" + id;
				mapperOutKey.set(keyStr);
				mapperOutVal.set("1");
//				System.out.println("MApper---------------- "+"Key: "+keyStr+"  Value: "+"1");
				context.write(mapperOutKey, mapperOutVal);
			}
		}
	}

	/////////////////////////////////////////////////////////////////
	// combiner:<word:id,1>--><word,id:sum>
	public static class indexCombiner extends Reducer<Text, Text, Text, Text> {
		private Text comBinerOutKey = new Text();
		private Text comBinerOutValue = new Text();

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String[] keys = key.toString().split(":");
			Integer sum = 0;
			for (Text val : values) {
				sum += Integer.parseInt(val.toString());
			}
			comBinerOutKey.set(keys[0]);
			comBinerOutValue.set(keys[keys.length - 1] + ":" + sum.toString());
//			System.out.println("Combiner---------------- "+"Key: "+keys[0]+"  Value: "+keys[keys.length - 1] + ":" + sum.toString());
			context.write(comBinerOutKey, comBinerOutValue);
		}
	}

	/////////////////////////////////////////////////////////////////
	// reducer:<word,id:sum>--><Json,NUllWritable>
	public static class indexReducer extends Reducer<Text, Text, Text, NullWritable> {
		private Text reducerOutKey = new Text();
		private NullWritable NULL = NullWritable.get();
//		JSONObject word = new JSONObject();
//		JSONObject docs = new JSONObject();

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			JSONObject word = new JSONObject();
			JSONObject docs = new JSONObject();
			int sum = 0;
			for (Text val : values) {
				String[] doc = val.toString().split(":");
				docs.put(doc[0], doc[1]);
			}
			word.put(key.toString(), docs.toString());
			reducerOutKey.set(word.toString());
//			System.out.println("Reducer---------------- "+"Key: "+word.toString()+"  Value: ");
			context.write(reducerOutKey, NULL);
		}
	}

	public static void main(String[] args) throws Exception {
		// Configuration
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(indexCombiner.class);
		job.setReducerClass(indexReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		// job.setOutputFormatClass(IntegerJsonOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

