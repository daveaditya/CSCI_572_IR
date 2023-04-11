package edu.usc.csci572.bigram;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class BigramIndexJob {

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
