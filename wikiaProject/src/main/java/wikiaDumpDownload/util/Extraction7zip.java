package wikiaDumpDownload.util;

import java.io.File;
import java.io.FileOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import utils.Utils;


public class Extraction7zip {
    private static Logger logger = Logger.getLogger(Extraction7zip.class.getName());
    private String directoryExtracted;
    private String directoryPath;


    /**
     * Constructor for the 7zip Extraction.
     *
     */
    public Extraction7zip() {
        directoryPath = ResourceBundle.getBundle("config").getString("directory");
        directoryExtracted = directoryPath + "/wikiaDumps/downloaded/extracted7z";
        Utils.createDirectory(directoryExtracted);
    }


    /**
     * Receives a filepath of a 7zip file as input string and extracts it into the wikiaDumps/extracted7z folder
     * @param fileName fileName of 7Zip file to extract
     * @return true if extraction was successful, else false.
     */
    public boolean extract7ZipFile(String fileName) {

        try {

            // make sure the file really is a .7z file:
            if(! (fileName.endsWith(".7z")) ){
                logger.severe("Not a .7z file.");
                return false;
            }

            SevenZFile sevenZFile = new SevenZFile(new File(fileName));
            SevenZArchiveEntry zipContents = sevenZFile.getNextEntry();
            File extractedFile=null;
            FileOutputStream fileStream=null;
            byte[] extractedFileContents=null;

            while (zipContents != null) {
                String extractedFileName = fileName.substring(0,fileName.indexOf(".7z"));
                extractedFile = new File(extractedFileName.replace("\\wikiaDumps\\downloaded\\7z\\",
                                                                   "\\wikiaDumps\\downloaded\\extracted7z\\"));
                if (!extractedFile.exists()) {
                    extractedFile.createNewFile();
                }
                fileStream = new FileOutputStream(extractedFile, false);
                extractedFileContents = new byte[(int) zipContents.getSize()];

                sevenZFile.read(extractedFileContents, 0, extractedFileContents.length);

                fileStream.write(extractedFileContents);
                fileStream.close();
                zipContents = sevenZFile.getNextEntry();
            }
            sevenZFile.close();

            logger.info("The file was extracted successfully.");
            return true;
        }
        catch(Exception ex){
            logger.severe("The file could not be downloaded. " + ex.toString());
            return false;
        }

    }

    /**
     * Unzips all 7zip files of the wikiaDumps/downloaded/7zip folder into the wikiaDumps/extracted7z folder
     *
     */
    public void extractAll7ZipFiles() {
        String folder7z = directoryPath + "/wikiaDumps/downloaded/7z";
        File folder = new File(folder7z);

        for (File file7z : folder.listFiles()) {
            logger.info("Extraction for following 7z file is started: " + file7z.getAbsolutePath());
            extract7ZipFile(file7z.getAbsolutePath());
        }
    }

}