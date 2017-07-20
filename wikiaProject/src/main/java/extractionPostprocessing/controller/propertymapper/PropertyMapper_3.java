package extractionPostprocessing.controller.propertymapper;

import extractionPostprocessing.util.DBpediaResourceServiceOffline;
/**
 * Algorithm of property mapper
 * - check whether ontology with the same name exists -> map to ontology
 * - if not check whether property with the same name exists -> map to property
 * - if not map to <null>
 */
public class PropertyMapper_3 extends PropertyMapper {

    @Override
    public String mapSingleProperty(String propertyToMap) {

        //Equivalent Ontology class
        String ontologyClass = propertyToMap.replace("/property/","/ontology/");

        if(DBpediaResourceServiceOffline.getDBpediaResourceServiceOfflineObject().
                ontologyClassExistInDBpedia(ontologyClass)){
            return ontologyClass;

        }
         if(DBpediaResourceServiceOffline.
                getDBpediaResourceServiceOfflineObject().propertyExistInDBPedia(propertyToMap.toLowerCase())
                ){
            return propertyToMap.toLowerCase();

        }
        else return "<null>";
    }
}
