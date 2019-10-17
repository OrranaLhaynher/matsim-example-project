/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project.events;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author nagel
 *
 */
public class EventsTeste {

	/**
	 * @param args
	 */

    private static final String exampleDirectory = "C:\\Users\\orran\\Desktop\\TCC\\areaScheduling\\";
    final static String NETWORKFILE = exampleDirectory + "area4.xml";
    public static final String xmlFilePath = exampleDirectory + "networkChangeEvents.xml";

    public static void main(String[] args) {

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);

        try {
 
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
 
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
 
            Document document = documentBuilder.newDocument();
 
            // root element
            Element root = document.createElement("networkChangeEvents");
            document.appendChild(root);

            Attr attrN = document.createAttribute("xmlns");
            attrN.setValue("http://www.matsim.org/files/dtd");
            root.setAttributeNode(attrN);
            Attr attrC = document.createAttribute("xmlns:xsi");
            attrC.setValue("http://www.w3.org/2001/XMLSchema-instance");
            root.setAttributeNode(attrC);
            Attr attrE = document.createAttribute("xsi:schemaLocation");
            attrE.setValue("http://www.matsim.org/files/dtd http://www.matsim.org/files/dtd/networkChangeEvents.xsd");
            root.setAttributeNode(attrE);
 
            // events element
            Element events = document.createElement("networkChangeEvent");
 
            root.appendChild(events);
 
            // set an attribute to staff element
            Attr attr = document.createAttribute("startTime");
            attr.setValue("09:00:00");
            events.setAttributeNode(attr);
 
            for ( Link link1 : scenario.getNetwork().getLinks().values() ) {
                    Element link = document.createElement("link");
                    Attr attr1 = document.createAttribute("refId");
                    attr1.setValue(link1.getId().toString());
                    link.setAttributeNode(attr1);
                    events.appendChild(link);
            }

            Element link2 = document.createElement("flowCapacity");
            Attr attr1 = document.createAttribute("type");
            Attr attr2 = document.createAttribute("value");
            attr1.setValue("absolute");
            attr2.setValue("0");
            link2.setAttributeNode(attr1);
            link2.setAttributeNode(attr2);
            events.appendChild(link2);

            Element link3 = document.createElement("freespeed");
            Attr attr3 = document.createAttribute("type");
            Attr attr4 = document.createAttribute("value");
            attr3.setValue("absolute");
            attr4.setValue("0.0");
            link3.setAttributeNode(attr3);
            link3.setAttributeNode(attr4);
            events.appendChild(link3);

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

}