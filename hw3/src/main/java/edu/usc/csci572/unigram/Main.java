package edu.usc.csci572.unigram;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class Main {

    @Parameter(names = {"--in"}, description = "Input directory of text files.")
    private String inputPath = "in";

    @Parameter(names = {"--out"}, description = "Output directory for Hadoop.")
    private String outputPath = "out";

    @Parameter(names = {"--auto-remove"}, description = "Deletes the output directory, if present")
    private boolean autoRemove = false;

    @Parameter(names = {"--help", "-h"}, help = true, description = "Prints the usage of this program.")
    private boolean help = false;

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Main main = new Main();

        JCommander jct = JCommander.newBuilder()
                .addObject(main)
                .build();
        jct.setProgramName("hw3-1.0.jar edu.usc.csci572.unigram.Main");
        jct.parse(args);
        if(main.isHelp()) {
            jct.usage();
            System.exit(0);
        }

        main.run();
    }

    public boolean isHelp() {
        return help;
    }

    public void run() throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Inverted Index Job");

        job.setJarByClass(Main.class);
        job.setMapperClass(UnigramIndexJob.UnigramMapper.class);
        job.setReducerClass(UnigramIndexJob.UnigramReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // In and Out Paths
        Path inPath = new Path(this.inputPath);
        Path outPath = new Path(this.outputPath);

        if(this.autoRemove) {
            // Delete output path if already exists
            FileSystem fs = outPath.getFileSystem(conf);
            if (fs.exists(outPath)) {
                fs.delete(outPath, true);
            }
        }

        FileInputFormat.addInputPath(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}