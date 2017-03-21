package wikiaStatistics;

import wikiaStatistics.controller.MetadataGetter;
import wikiaStatistics.model.MetadataStatistics;
import wikiaStatistics.util.WikiaStatisticsTools;
import java.util.logging.Logger;
import java.io.*;

/**
 * This class represents the application to download metadata of all Wikia wikis and receive statistics.
 */
public class wikiaStatisticsApplication {


    public static void main(String[] args) {

        Logger logger = Logger.getLogger(wikiaStatisticsApplication.class.getName());

        // files will be saved in the 'resources' directory
        String filePath1 = "./wikiaProject/src/main/resources/wikiaOverviewIndividualFiles/p1_wikis_1_to_500000.csv";
        String filePath2 = "./wikiaProject/src/main/resources/wikiaOverviewIndividualFiles/p2_wikis_500000_to_1000000.csv";
        String filePath3 = "./wikiaProject/src/main/resources/wikiaOverviewIndividualFiles/p3_wikis_1000000_to_1500000.csv";
        String filePath4 = "./wikiaProject/src/main/resources/wikiaOverviewIndividualFiles/p4_wikis_1500000_to_2000000.csv";

        Thread t1 = new Thread(new MetadataGetter(filePath1,1, 500000), "Thread 1");
        Thread t2 = new Thread(new MetadataGetter(filePath2,500000, 1000000), "Thread 2");
        Thread t3 = new Thread(new MetadataGetter(filePath3,1000000, 1500000), "Thread 3");
        Thread t4 = new Thread(new MetadataGetter(filePath4,1500000, 2000000), "Thread 4");

//         t1.start();
        // t2.start();
        // t3.start();
        // t4.start();

        logger.info("Download process finished.");
        logger.info("Concatenating files...");

        // wait until all threads are finished before merging files
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch(InterruptedException ie){
            logger.severe(ie.toString());
        }

        // WikiaStatisticsTools.mergeFiles(filePath1, filePath2, filePath3, filePath4);

        MetadataStatistics statistics = WikiaStatisticsTools.getMetadataStatistics(new File("./wikiaProject/src/main/resources/wikiaAllOverview.csv"));
        statistics.limitTopLanguages(1000);
        System.out.println(statistics);

    }

}