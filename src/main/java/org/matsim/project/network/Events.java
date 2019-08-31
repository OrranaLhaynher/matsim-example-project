package org.matsim.project.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
 
public class Events{
    
    public static final String xmlFilePath = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\California\\networkChangeEvents.xml";
    public static final String csvFile = "C:\\Users\\orran\\OneDrive\\Documentos\\GitHub\\matsim-example-project\\original-input-data\\California\\linksCalifornia.csv";
 
    public static void main(String argv[]) {
 
        try {
 
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
 
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
 
            Document document = documentBuilder.newDocument();
 
            // root element
            Element root = document.createElement("networkChangeEvents");
            document.appendChild(root);
 
            // events element
            Element events = document.createElement("networkChangeEvent");
 
            root.appendChild(events);
 
            // set an attribute to staff element
            Attr attr = document.createAttribute("startTime");
            attr.setValue("03:06:00");
            events.setAttributeNode(attr);
 
            //you can also use staff.setAttribute("id", "1") for this
 
            // link element
            
            String[][] id = new String[8556][1];
            id = getCSVData(csvFile);

            for (int i=0; i<6994; i++){
                    Element link = document.createElement("link");
                    Attr attr1 = document.createAttribute("refId");
                    attr1.setValue(id[i][0]);
                    link.setAttributeNode(attr1);
                    events.appendChild(link);
            }

            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
 
            // If you use
            // StreamResult result = new StreamResult(System.out);
            // the output will be pushed to the standard output ...
            // You can use that for debugging 
 
            transformer.transform(domSource, streamResult);
 
            System.out.println("Done creating XML File");
 
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public static String[][] getCSVData(String csvFile) {
		
		BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] position = null;
        String link[][] = new String[8556][1];

        try {

            br = new BufferedReader(new FileReader(csvFile));
            int i = 0;
            while ((line = br.readLine()) != null) {
            	
                // use comma as separator
               position = line.split(cvsSplitBy);

               link[i][0]=position[0];
                
               i++; 
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return link;
    }

}