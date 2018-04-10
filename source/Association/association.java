/*
 *By: QC
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
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonObject;
import com.zackehh.outputformat.JsonOutputFormat;

public class WordCount {
	////////////////////////////////////////////////////
	/*
	 * UserRest
	 */
	// mapper: <rest,users> --> <user,rest>
		public static class userRestMapper extends Mapper<Object, Text, Text, Text> {
			private Text mapperOutKey = new Text();
			private Text mapperOutVal = new Text();

			public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
				String users = "";
				JSONObject jObject = null;
				String id = "";
				try {
					JSONParser parser = new JSONParser();
					jObject = (JSONObject) parser.parse(value.toString());
					JSONObject comment = (JSONObject) parser.parse(jObject.get("Comment").toString());
					for (Iterator iterator = (Iterator) comment.keySet().iterator(); iterator.hasNext();) {
						String person = (String) iterator.next();
						if (person.length() < 1)
							continue;
						users += person + ";";
					}
					//
					id = (String) jObject.get("ID");
				} catch (Exception e) {
					e.printStackTrace();
				}
				String[] words = users.split(";");
				for (int i = 0; i < words.length; i++) {
					String keyStr = words[i];
					mapperOutKey.set(keyStr);
					mapperOutVal.set(id);
					// System.out.println("MApper---------------- "+"Key: "+keyStr+" Value: "+"1");
					context.write(mapperOutKey, mapperOutVal);
				}
			}
		}

		/////////////////////////////////////////////////////////////////
		// reducer:<user,rest> --><json,null>(<user,rests>)
		public static class userRestReducer extends Reducer<Text, Text, Text, NullWritable> {
			private Text reducerOutKey = new Text();
			private NullWritable NULL = NullWritable.get();
			// JSONObject word = new JSONObject();
			// JSONObject docs = new JSONObject();

			public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
				JSONObject user = new JSONObject();
				String ids = "";
				for (Text val : values) {
					ids += val.toString() + ";";
				}
				user.put(key.toString(), ids);
				reducerOutKey.set(user.toString());
				// System.out.println("Reducer---------------- "+"Key: "+word.toString()+"
				// Value: ");
				context.write(reducerOutKey, NULL);
			}
		}
		
		///////////////////////////////////////////////////////////
		/*
		 * RestSum
		 */
		// mapper: <json,null>(<user,rests>) --> <<rest1,rest2>,sum> 1
		public static class restSumMapper extends Mapper<Object, Text, Text, IntWritable> {
			private Text mapperOutKey1 = new Text();
			private Text mapperOutKey2 = new Text();
			private IntWritable mapperOutVal = new IntWritable();

			public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
				String users = "";
				JSONObject jObject = null;
				String id = "";
				try {
					JSONParser parser = new JSONParser();
					jObject = (JSONObject) parser.parse(value.toString());
					String idStr = "";
					for (Iterator iterator = (Iterator) jObject.keySet().iterator(); iterator.hasNext();) {
						String person = (String) iterator.next();
						idStr += jObject.get(person);
					}
					String[] ids = idStr.split(";");
					for (int i = 0; i < ids.length; i++) {
						for (int j = i + 1; j < ids.length; j++) {
							mapperOutKey1.set(ids[i] + ";" + ids[j]);
							mapperOutKey2.set(ids[j] + ";" + ids[i]);
							mapperOutVal.set(1);
							context.write(mapperOutKey1, mapperOutVal);
							context.write(mapperOutKey2, mapperOutVal);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/////////////////////////////////////////////////////////////////
		// mapper: <<rest1,rest2>,sum> --> <<rest1,rest2>,sum> sum
		public static class restSumReducer extends Reducer<Text, IntWritable, Text, NullWritable> {
			private Text reducerOutKey = new Text();
			private NullWritable NULL = NullWritable.get();

			public void reduce(Text key, Iterable<IntWritable> values, Context context)
					throws IOException, InterruptedException {
				JSONObject res = new JSONObject();
				Integer sum = 0;
				for (IntWritable val : values) {
					String id = val.toString();
					sum += val.get();
				}
				res.put(key.toString(), sum.toString());
				reducerOutKey.set(res.toString());
				// System.out.println("Reducer---------------- "+"Key: "+word.toString()+"
				// Value: ");
				context.write(reducerOutKey, NULL);
			}
		}
		
		////////////////////////////////////////////////////////////
		/*
		 * RestRest
		 */
		// mapper: <json,null>(<<rest1,rest2>,sum>) --> <rest1,rest2>
		public static class restRestMapper extends Mapper<Object, Text, Text, Text> {
			private Text mapperOutKey1 = new Text();
//			private Text mapperOutKey2 = new Text();
			private Text mapperOutVal1 = new Text();
//			private Text mapperOutVal2 = new Text();

			public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
				String users = "";
				JSONObject jObject = null;
				String id = "";
				try {
					JSONParser parser = new JSONParser();
					jObject = (JSONObject) parser.parse(value.toString());
					String idStr = "";
					String sumStr = "";
					for (Iterator iterator = (Iterator) jObject.keySet().iterator(); iterator.hasNext();) {
						idStr = iterator.next().toString();
						sumStr = jObject.get(idStr).toString();
					}
					Integer sumInt = Integer.parseInt(sumStr);
					String[] ids = idStr.split(";");
					for (int i = 0; i < ids.length; i++) {
						for (int j = i + 1; j < ids.length; j++) {
							mapperOutKey1.set(ids[i]);
//							mapperOutKey2.set(ids[j]);
							mapperOutVal1.set(ids[j]);
//							mapperOutVal2.set(ids[i]);
							if (sumInt >= 5) {
								context.write(mapperOutKey1, mapperOutVal1);
//								context.write(mapperOutKey2, mapperOutVal2);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public static class restRestReducer extends Reducer<Text, Text, Text, NullWritable> {
			private Text combinerOutKey = new Text();
			private NullWritable NULL = NullWritable.get();
			public void reduce(Text key, Iterable<Text> values, Context context)
					throws IOException, InterruptedException {
				JSONObject res = new JSONObject();
				String rests="";
				for (Text val : values) {
					rests+=val.toString()+";";
				}
				res.put(key, rests);
				combinerOutKey.set(res.toString());
				context.write(combinerOutKey, NULL);
			}
		}

	public static void main(String[] args) throws Exception {
		// Configuration
//		Configuration conf = new Configuration();
//		Job job = Job.getInstance(conf, "word count");
//		job.setJarByClass(WordCount.class);
//		job.setMapperClass(TokenizerMapper.class);
//		job.setReducerClass(indexReducer.class);
//
//		job.setMapOutputKeyClass(Text.class);
//		job.setMapOutputValueClass(Text.class);
//		job.setOutputKeyClass(Text.class);
//		job.setOutputValueClass(NullWritable.class);
//		// job.setOutputFormatClass(IntegerJsonOutputFormat.class);
//		FileInputFormat.addInputPath(job, new Path(args[0]));
//		FileOutputFormat.setOutputPath(job, new Path(args[1]));
//		System.exit(job.waitForCompletion(true) ? 0 : 1);
		

		Configuration conf = new Configuration();
		Job job1 = Job.getInstance(conf, "UserRest");
		job1.setJarByClass(WordCount.class);
		job1.setMapperClass(userRestMapper.class);
		job1.setReducerClass(userRestReducer.class);
		job1.setMapOutputKeyClass(Text.class);
		job1.setMapOutputValueClass(Text.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(NullWritable.class);
		ControlledJob ctrlJob1=new ControlledJob(conf);
		ctrlJob1.setJob(job1);
		FileInputFormat.addInputPath(job1, new Path(args[0]));
		FileOutputFormat.setOutputPath(job1, new Path(args[1]));
		
		Job job2 = Job.getInstance(conf, "RestSum");
		job2.setJarByClass(WordCount.class);
		job2.setMapperClass(restSumMapper.class);
		job2.setReducerClass(restSumReducer.class);
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(IntWritable.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(NullWritable.class);
		ControlledJob ctrlJob2=new ControlledJob(conf);
		ctrlJob2.setJob(job2);
		ctrlJob2.addDependingJob(ctrlJob1);
		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));
		
		Job job3 = Job.getInstance(conf, "RestRest");
		job3.setJarByClass(WordCount.class);
		job3.setMapperClass(restRestMapper.class);
		job3.setReducerClass(restRestReducer.class);
		job3.setMapOutputKeyClass(Text.class);
		job3.setMapOutputValueClass(Text.class);
		job3.setOutputKeyClass(Text.class);
		job3.setOutputValueClass(NullWritable.class);
		ControlledJob ctrlJob3=new ControlledJob(conf);
		ctrlJob3.setJob(job3);
		ctrlJob3.addDependingJob(ctrlJob2);
		FileInputFormat.addInputPath(job3, new Path(args[2]));
		FileOutputFormat.setOutputPath(job3, new Path(args[3]));
		
		//control

		

		
		JobControl jobControl=new JobControl("WordCount");
		jobControl.addJob(ctrlJob1);
		jobControl.addJob(ctrlJob2);
		jobControl.addJob(ctrlJob3);
		
		Thread thread=new Thread(jobControl);
		thread.start();
		while(true) {
			if(jobControl.allFinished()) {
				System.out.println(jobControl.getSuccessfulJobList());
				jobControl.stop();
				break;
			}
		}
	}
}

