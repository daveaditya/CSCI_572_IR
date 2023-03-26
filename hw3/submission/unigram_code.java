import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class unigram_code {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Inverted Index Job");

        job.setJarByClass(unigram_code.class);
        job.setMapperClass(unigram_code.UnigramMapper.class);
        job.setReducerClass(unigram_code.UnigramReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // In and Out Paths
        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        // Delete output path if already exists
        FileSystem fs = outPath.getFileSystem(conf);
        if (fs.exists(outPath)) {
            fs.delete(outPath, true);
        }

        FileInputFormat.addInputPath(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public void run() throws IOException, InterruptedException, ClassNotFoundException {

    }

    public static class UnigramMapper extends Mapper<Object, Text, Text, Text> {
        private final Text word = new Text();
        private final Text documentId = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] data = value.toString().split("\\t", 2); // Split document id and content

            documentId.set(data[0]); // get document id

            String documentContent = data[1].replaceAll("[^a-zA-Z]+", " ").replaceAll("\\s+", " ").toLowerCase(Locale.ROOT); // get document content

            StringTokenizer itr = new StringTokenizer(documentContent, " ");

            while (itr.hasMoreTokens()) {
                String wordStr = itr.nextToken();
                if (!wordStr.trim().isEmpty()) {
                    word.set(wordStr);

                    context.write(word, documentId);
                }
            }
        }
    }

    public static class UnigramReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> documentIds, Context context) throws IOException, InterruptedException {
            Map<String, Integer> frequencies = new LinkedHashMap<>();

            // Go over all the document ids and create a HashMap with counts
            for (Text documentId : documentIds) {
                String docId = documentId.toString();
                frequencies.put(docId, frequencies.getOrDefault(docId, 0) + 1);
            }

            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
                if (result.length() > 0) {
                    result.append("\t");
                }

                String docIdWordFrequency = String.format("%s:%d", entry.getKey(), entry.getValue());
                result.append(docIdWordFrequency);
            }

            context.write(key, new Text(result.toString()));
        }
    }

}