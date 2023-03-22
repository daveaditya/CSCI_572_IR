package edu.usc.csci572.unigram;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class UnigramIndexJob {

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
            Map<String, Integer> frequencies = new HashMap<>();

            // Go over all the document ids and create a HashMap with counts
            for (Text documentId : documentIds) {
                String docId = documentId.toString();
                frequencies.put(docId, frequencies.getOrDefault(docId, 0) + 1);
            }

            StringBuilder result = new StringBuilder();
            for(Map.Entry<String, Integer> entry: frequencies.entrySet()) {
                if(result.length() > 0) {
                    result.append("\t");
                }

                String docIdWordFrequency = String.format("%s:%d", entry.getKey(), entry.getValue());
                result.append(docIdWordFrequency);
            }

            context.write(key, new Text(result.toString()));
        }
    }

}
