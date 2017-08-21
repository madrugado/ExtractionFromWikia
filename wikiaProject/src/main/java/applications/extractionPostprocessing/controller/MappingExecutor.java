package applications.extractionPostprocessing.controller;

import applications.extractionPostprocessing.controller.classmapper.ClassMapper;
import applications.extractionPostprocessing.controller.propertymapper.PropertyMapper;
import applications.extractionPostprocessing.controller.resourcemapper.ResourceMapper;
import applications.extractionPostprocessing.model.*;

import utils.IOoperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for the creation of the mappings files.
 * Given a resourceMapper, this class performs the mapping for all wikis.
 */
public class MappingExecutor {

    private static Logger logger = Logger.getLogger(MappingExecutor.class.getName());
    private ResourceMapper resourceMapper;
    private PropertyMapper propertyMapper;
    private ClassMapper classMapper;

    // for the statistics
    private int totalNumberOfResources = 0;
    private int totalNumberOfProperties = 0;
    private int totalNumberOfClasses = 0;


    /**
     * Constructor
     * @param resourceMapper
     */
    public MappingExecutor(ResourceMapper resourceMapper, PropertyMapper propertyMapper, ClassMapper classMapper) {
        this.resourceMapper = resourceMapper;
        this.classMapper = classMapper;
        this.propertyMapper = propertyMapper;
    }

    /**
     * Loops over the root directory and creates the mapping files.
     */
    public void createMappingFilesForAllWikis() {

        String pathToRootDirectory = ResourceBundle.getBundle("config").getString("pathToRootDirectory") + "/postProcessedWikis";
        File root = new File(pathToRootDirectory);
        boolean includeNullMappings;

        if (root.isDirectory()) {

            HashSet<String> classesForDefinition = new HashSet<String>();

            // loop over all wikis
            for (File directory : root.listFiles()) {

                if (directory.isDirectory()) {
                    // we have a wiki file

                    WikiToMap wikiToMap = getMappingInformationOfWikiAndUpdateFiles(directory);
                    String targetNameSpace = ResourceBundle.getBundle("config").getString("targetnamespace") + "/" + directory.getName();

                    // increment the statistics
                    totalNumberOfClasses += wikiToMap.classesToMap.size();
                    totalNumberOfProperties += wikiToMap.propertiesToMap.size();
                    totalNumberOfResources += wikiToMap.resourcesToMap.size();

                    // set whether null mappings should be included for evaluation
                    includeNullMappings = Boolean.parseBoolean(ResourceBundle.getBundle("config").getString("includeNullMappings"));

                    resourceMapper.writeResourceMappingsFile(directory, targetNameSpace, wikiToMap.resourcesToMap, includeNullMappings);
                    propertyMapper.writePropertiesMappingsFile(directory, targetNameSpace, wikiToMap.propertiesToMap, includeNullMappings);
                    classMapper.writeClassMappingsFile(directory, targetNameSpace, wikiToMap.classesToMap, includeNullMappings);

                    Iterator iterator = wikiToMap.classesToMap.iterator();

                    while(iterator.hasNext()){
                        classesForDefinition.add( classMapper.transformTemplateToClass( (String) iterator.next()));
                    }

                    // OutputOperations.printSet(classesForDefinition);


                } // end of check whether file is a directory
            } // end of loop over files


            // create the ontology file
            OntologyCreator ontologyCreator = new OntologyCreator(classesForDefinition);
            ontologyCreator.createOntologyOption();


            // output the statistics and write them into file
            String statisticsText = "Total number of resources found: " + totalNumberOfResources + "\n" +
                    "Total number of properties found: " + totalNumberOfProperties + "\n" +
                    "Total number of classes found: " + totalNumberOfClasses;

            logger.info(statisticsText);

            String pathToRoot = ResourceBundle.getBundle("config").getString("pathToRootDirectory");

            File statisticsDirectory = new File(pathToRoot + "/statistics");
            if(!statisticsDirectory.exists()){
                IOoperations.createDirectory(pathToRoot + "/statistics");
            }

            // write to file
            IOoperations.writeContentToFile(new File(pathToRoot + "/statistics/found_resources_properties_classes.txt"), statisticsText);


        } // end of check whether PostProcessedWikis is a directory
    }



    /**
     * This method looks for resources, properties and templates for a given wiki. It will collect those in sets and return them.
     * Additionally all files will be updated with the correct domain name.
     *
     * @param directoryOfWiki The directory where the files of a single wiki are stored.
     * @return
     */
    private WikiToMap getMappingInformationOfWikiAndUpdateFiles(File directoryOfWiki) {

        //get list of extracted files in a folder
        File[] listOfFiles = directoryOfWiki.listFiles();

        String mappingFileName = ResourceBundle.getBundle("config").getString("mappingfilename");
        String targetNameSpace = ResourceBundle.getBundle("config").getString("targetnamespace") + "/" + directoryOfWiki.getName();

        HashSet<String> resourcesToMap = new HashSet<>();
        HashSet<String> propertiesToMap = new HashSet<>();
        HashSet<String> classesToMap = new HashSet<>();


        try {

            // Loop over all ttl files in the directory and create the mappings.
            for (int i = 0; i < listOfFiles.length; i++) {

                if ( // file is relevant
                        listOfFiles[i].isFile()
                                && listOfFiles[i].toString().endsWith(".ttl")
                                && !listOfFiles[i].toString().endsWith("_evaluation.ttl") // do not use resources from the evaluation file
                                && !listOfFiles[i].toString().endsWith(mappingFileName)   // do not use resources from the mapping file
                        ) {

                    String line = ""; // line to be read
                    String languageCode = "";

                    // content of the new file with updated domain to be overwritten
                    StringBuffer contentOfNewFile = new StringBuffer();

                    FileReader fr = new FileReader(listOfFiles[i].getAbsolutePath());
                    BufferedReader br = new BufferedReader(fr);

                    // regex to find tags
                    Matcher matcher = null;
                    Pattern pattern = Pattern.compile("<[^<]*>");
                    // regex: <[^<]*>
                    // this regex captures everything between tags including the tags: <...>
                    // there are three tags in every line


                    // read relevant file line by line
                    while ((line = br.readLine()) != null) {


                        // if the line is a comment -> continue with the next line
                        if (!line.trim().toLowerCase().startsWith("#") || !line.trim().toLowerCase().startsWith("#")) {
                            // -> line is not a comment

                            // rewrite line for updating the file
                            contentOfNewFile.append(line.replaceAll("dbpedia.org", targetNameSpace) + "\n");

                            matcher = pattern.matcher(line);

                            while (matcher.find()) {

                                // do not do for wikipedia and wikimedia resources, wikipedia resources and categories
                                if (matcher.group().toLowerCase().contains("wikipedia.org") ||
                                        matcher.group().toLowerCase().contains("commons.wikimedia.org") ||
                                        matcher.group().toLowerCase().contains("category:")
                                        ) {
                                    // do nothing
                                } else {
                                    // -> not a wikipedia or dbpedia resource

                                    // sort into proper map
                                    if (matcher.group().contains("/Template:")) {
                                        if(matcher.group().toLowerCase().contains("infobox")){
                                            classesToMap.add(matcher.group());
                                        }
                                    } else if (matcher.group().contains("/resource/")) {
                                        resourcesToMap.add(matcher.group());
                                    } else if (matcher.group().contains("/property/")) {
                                        propertiesToMap.add(matcher.group());
                                    }

                                }

                            } // end of while matcher find

                        } // end of if (!comment)

                    } // end of read line loop

                    br.close();
                    fr.close();

                    // update the file, i.e. rewrite the file where the dbpedia domain is replaced with the actual domain
                    IOoperations.updateFile(contentOfNewFile.toString(), listOfFiles[i]);

                } // end of if relevant file
            }// end of loop over all files of that particular wiki

        } catch (IOException ioe) {
            logger.severe(ioe.toString());
            ioe.printStackTrace();
        }

        return new WikiToMap(directoryOfWiki.getName(), resourcesToMap, propertiesToMap, classesToMap);
    }




   /*
   ONLY GETTERS AND SETTERS BELOW.
    */

    public void setResourceMapper(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    public void setPropertyMapper(PropertyMapper propertyMapper) {
        this.propertyMapper = propertyMapper;
    }

    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public ResourceMapper getResourceMapper() {
        return resourceMapper;
    }

    public PropertyMapper getPropertyMapper() {
        return propertyMapper;
    }

    public ClassMapper getClassMapper() {
        return classMapper;
    }
}
