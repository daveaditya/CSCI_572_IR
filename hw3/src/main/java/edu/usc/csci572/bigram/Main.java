package edu.usc.csci572.bigram;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Usage: Bigram Inverted Index <input path> <output path>");
            System.exit(-1);
        }
        String inputFile = args[0];
        String outputFile = args[1];

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Bigram Inverted Index Job");
        job.setJarByClass(BigramIndexJob.class);
        job.setMapperClass(BigramIndexJob.BigramIndexMapper.class);
        job.setReducerClass(BigramIndexJob.BigramIndexReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        Path inputFilePath = new Path(inputFile);
        Path outputFilePath = new Path(outputFile);
        FileSystem fileSystem = outputFilePath.getFileSystem(conf);
        if (fileSystem.exists(outputFilePath)) {
            fileSystem.delete(outputFilePath, true);
        }
        FileInputFormat.addInputPath(job, inputFilePath);
        FileOutputFormat.setOutputPath(job, outputFilePath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
