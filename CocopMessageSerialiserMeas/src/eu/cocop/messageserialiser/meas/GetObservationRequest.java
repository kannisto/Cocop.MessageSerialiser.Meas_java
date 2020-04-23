//
// Please make sure to read and understand README.md and LICENSE.txt.
//
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
// Author: Petri Kannisto, Tampere University, Finland
// Last modified: 4/2020
//
// This API has been derived from standards and XML schemata provided by the
// Open Geospatial Consortium (OGC(r)). Please make sure to read and understand
// the following legal conditions:
// (1) Copyright Notice and Disclaimers at https://www.ogc.org/ogc/legal
// (2) OGC(r) Document Notice; the most recent version is at
//     https://www.ogc.org/ogc/document and another enclosed in file
//     "ogc_document_notice.txt"
// (3) OGC(r) Software Notice; the most recent version is at
//     https://www.ogc.org/ogc/software and another enclosed in file
//     "ogc_software_notice.txt"
// (4) The license of each related standard referred to in this file.

package eu.cocop.messageserialiser.meas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.sos._2.GetObservationType;
import net.opengis.sos._2.ObjectFactory;
import net.opengis.swe._2.DataRecordPropertyType;

/**
 * A class to process "get observation" requests.
 * 
 * In this module, the code has been derived from OGC(r) Sensor Observation
 * Service Interface Standard (OGC 12-006; please see the file
 * "ref_and_license_ogc_sos.txt").
 * @author Petri Kannisto
 */
public final class GetObservationRequest
{
	private Set<String> m_featuresOfInterest = new TreeSet<>();
	private Set<String> m_observedProperties = new TreeSet<>();
	private List<TemporalFilter> m_temporalFilters = new ArrayList<>();
	private List<Item_DataRecord> m_items = new ArrayList<>();
	
	
	/**
	 * Constructor. Use to create requests to be submitted.
	 */
	public GetObservationRequest()
	{
		// Empty ctor body
	}
	
	/**
	 * Constructor. Use to process incoming requests.
	 * @param xmlBytes XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	public GetObservationRequest(byte[] xmlBytes) throws InvalidMessageException
	{
		try
		{
			@SuppressWarnings("unchecked")
			JAXBElement<GetObservationType> proxy = (JAXBElement<GetObservationType>)XmlHelper.deserialiseFromXml(xmlBytes);
			populateFromProxy(proxy.getValue());
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Unexpected message type", e);
		}
	}
	
	/**
	 * Features of interest.
	 * @return Features of interest.
	 */
	public Set<String> getFeaturesOfInterest()
	{
		return m_featuresOfInterest;
	}
	
	/**
	 * Observed properties.
	 * @return Observed properties.
	 */
	public Set<String> getObservedProperties()
	{
		return m_observedProperties;
	}
	
	/**
	 * Temporal filters.
	 * @return Temporal filters.
	 */
	public List<TemporalFilter> getTemporalFilters()
    {
        return m_temporalFilters;
    }
	
	/**
	 * Items.
	 * @return Items.
	 */
	public List<Item_DataRecord> getItems()
    {
        return m_items;
    }
	
	/**
	 * Serialises the object to XML.
	 * @return XML data.
	 */
	public byte[] toXmlBytes()
	{
		// Using this to enable unique IDs within the XML document
        String idPrefix = "GetObsReq_i";

        GetObservationType toSerialise = new GetObservationType();
        
        // Setting the required "service" and "version" attributes
        toSerialise.setService("SOS");
        toSerialise.setVersion("2.0.0");
        
        // Assigning features of interest
        for (String s : m_featuresOfInterest)
        {
        	toSerialise.getFeatureOfInterest().add(s);
        }
        
        // Assigning observed properties
        for (String s : m_observedProperties)
        {
        	toSerialise.getObservedProperty().add(s);
        }
        
        // Assigning extensions
        int prefixCounter = 0;
        
        for (Item_DataRecord i : m_items)
        {
        	String idWithCounter = idPrefix + (prefixCounter + 1) + "-"; // This gives, e.g., "GetObsReq_i3-"
        	
        	// Using the "getObjectForXml_Result" function to get a "property type" element that
        	// wraps the actual payload element
        	toSerialise.getExtension().add(i.getObjectForXml_Result(idWithCounter));
        	++prefixCounter;
        }
        
        // Assigning temporal filters
        for (TemporalFilter tf : m_temporalFilters)
        {
        	// Create a proxy object for serialisation
            String idWithCounter = idPrefix + (prefixCounter + 1) + "-"; // This gives, e.g., "GetObsReq_i3-"
            Object filterProxy = tf.toXmlProxy(idWithCounter);

            toSerialise.getTemporalFilter().add((net.opengis.sos._2.GetObservationType.TemporalFilter)filterProxy);

            ++prefixCounter;
        }
        
        // Serialising to XML
        ObjectFactory objectFactorySos = new ObjectFactory();
        Object actualProxy = objectFactorySos.createGetObservation(toSerialise);
        return XmlHelper.toXmlBytes(actualProxy);
	}
	
	private void populateFromProxy(GetObservationType proxy) throws InvalidMessageException
	{
		m_featuresOfInterest = new TreeSet<>();
		m_observedProperties = new TreeSet<>();
		m_items = new ArrayList<>();
		m_temporalFilters = new ArrayList<>();
		
		// Reading features of interest
        if (proxy.getFeatureOfInterest() != null)
        {
            for (String s : proxy.getFeatureOfInterest())
            {
            	m_featuresOfInterest.add(s);
            }
        }

        // Reading observed properties
        if (proxy.getObservedProperty() != null)
        {
        	for (String s : proxy.getObservedProperty())
        	{
        		m_observedProperties.add(s);
        	}
        }

        // Reading extensions
        if (proxy.getExtension() != null)
        {
            for (Object o : proxy.getExtension())
            {
            	if (!(o instanceof DataRecordPropertyType))
            	{
            		throw new InvalidMessageException("Unexpected extension type " + o.getClass().getName());
            	}
            	
            	DataRecordPropertyType dataRecProp = (DataRecordPropertyType)o;
            	m_items.add(new Item_DataRecord(dataRecProp));
            }
        }

        // Reading temporal filters
        if (proxy.getTemporalFilter() != null)
        {
        	for (net.opengis.sos._2.GetObservationType.TemporalFilter filterEl : proxy.getTemporalFilter())
            {
        		TemporalFilter filterObj = new TemporalFilter(filterEl);
                m_temporalFilters.add(filterObj);
            }
        }
	}
}
