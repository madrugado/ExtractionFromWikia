package wikiaStatistics.controller;

import wikiaStatistics.util.WikiaStatisticsTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * This class represents the implementation of MetadataGetter threads.
 */
public class MetadataGetterImpl {

    private static Logger logger = Logger.getLogger(MetadataGetterImpl.class.getName());


    public static void downloadWikiaMetadata() {

        String directoryPath = ResourceBundle.getBundle("config").getString("directory");

        File file = new File(directoryPath + "/wikiaOverviewIndividualFiles");

        if (!file.isDirectory()) {
            try {
                Files.createDirectory(file.toPath());
            } catch (IOException e) {
                logger.severe(e.toString());
            }
        }

        // files will be saved in the newly created subdirectory
        String filePath1 = directoryPath + "/wikiaOverviewIndividualFiles/p1_wikis_1_to_500000.csv";
        String filePath2 = directoryPath + "/wikiaOverviewIndividualFiles/p2_wikis_500000_to_1000000.csv";
        String filePath3 = directoryPath + "/wikiaOverviewIndividualFiles/p3_wikis_1000000_to_1500000.csv";
        String filePath4 = directoryPath + "/wikiaOverviewIndividualFiles/p4_wikis_1500000_to_2000000.csv";

        Thread t1 = new Thread(new MetadataGetter(filePath1,1, 500000), "Thread 1");
        Thread t2 = new Thread(new MetadataGetter(filePath2,500000, 1000000), "Thread 2");
        Thread t3 = new Thread(new MetadataGetter(filePath3,1000000, 1500000), "Thread 3");
        Thread t4 = new Thread(new MetadataGetter(filePath4,1500000, 2000000), "Thread 4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        // wait until all threads are finished before merging files
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();

            logger.info("Download process finished.");
            logger.info("Concatenating files...");

        } catch(InterruptedException ie){
            logger.severe(ie.toString());
        }

        WikiaStatisticsTools.mergeFiles(filePath1, filePath2, filePath3, filePath4);

    }

}
