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

public class bigram_code {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Bigram Inverted Index Job");
        job.setJarByClass(bigram_code.class);

        job.setMapperClass(bigram_code.BigramIndexMapper.class);
        job.setReducerClass(bigram_code.BigramIndexReducer.class);

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


    public static class BigramIndexMapper extends Mapper<Object, Text, Text, Text> {
        private final Text bigram = new Text();
        private final Text docId = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] parts = value.toString().split("\\t", 2);

            docId.set(parts[0]);

            String wordsString = parts[1].replaceAll("[^a-zA-Z]+", " ").toLowerCase(Locale.ROOT);
            StringTokenizer tokenizer = new StringTokenizer(wordsString, " ");

            String first = null, second = null; // two pointers to hold words

            // loop over all the tokens and store bigram and the document id
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();

                if (!word.trim().isEmpty()) {
                    if (first == null) {
                        first = word;
                        continue;
                    } else if (second == null) {
                        second = word;
                    } else {
                        first = second;
                        second = word;
                    }

                    bigram.set(String.format("%s %s", first, second));
                    context.write(bigram, docId);
                }
            }
        }
    }

    public static class BigramIndexReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text bigram, Iterable<Text> docIds, Context context) throws IOException, InterruptedException {
            Map<String, Integer> frequencies = new LinkedHashMap<>();

            // sum all document frequency for bigram
            for (Text docId : docIds) {
                String docIdString = docId.toString();
                frequencies.put(docIdString, frequencies.getOrDefault(docIdString, 0) + 1);
            }

            // create result string with docId:count...
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
                if (result.length() > 0) {
                    result.append("\t");
                }
                result.append(String.format("%s:%d", entry.getKey(), entry.getValue()));
            }

            context.write(bigram, new Text(result.toString()));
        }
    }

}
