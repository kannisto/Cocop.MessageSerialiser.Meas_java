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

import javax.xml.bind.JAXBElement;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.om._2.OMObservationType;
import net.opengis.sos._2.GetObservationResponseType;
import net.opengis.sos._2.ObjectFactory;
import net.opengis.sos._2.GetObservationResponseType.ObservationData;

/**
 * A class to process "get observation" responses.
 * 
 * In this module, the code has been derived from OGC(r) Sensor Observation
 * Service Interface Standard (OGC 12-006; please see the file
 * "ref_and_license_ogc_sos.txt").
 * @author Petri Kannisto
 */
public final class GetObservationResponse
{
	// TODO: Implement ExtensibleResponse similar to the C#/.NET API.
	// That is, use a data record to communicate request status.
	
	private List<Observation> m_observations = new ArrayList<>();
	
	
	/**
	 * Constructor. Use this to create a response to be submitted.
	 */
	public GetObservationResponse()
	{
		// Empty ctor body
	}
	
	/**
	 * Constructor. Use this to process an incoming response object.
	 * @param xmlBytes XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	public GetObservationResponse(byte[] xmlBytes) throws InvalidMessageException
	{
		try
		{
			@SuppressWarnings("unchecked")
			JAXBElement<GetObservationResponseType> proxy = (JAXBElement<GetObservationResponseType>)XmlHelper.deserialiseFromXml(xmlBytes);
			populateFromProxy(proxy.getValue());
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Unexpected message type", e);
		}
	}
	
	/**
	 * Observations.
	 * @return Observations.
	 */
	public List<Observation> getObservations()
	{
		return m_observations;
	}
	
	/**
	 * Serialises the object to XML.
	 * @return XML data.
	 */
	public byte[] toXmlBytes()
	{
		GetObservationResponseType proxy = new GetObservationResponseType();
        
        // These enable unique identifiers within the XML document
        String idPrefix = "GetObsResp_i";
        int idCounter = 1;

        // Adding observations
        if (m_observations.size() > 0)
        {
            for (Observation o : m_observations)
            {
                // Getting observation proxy
                String idPrefWithCounter = buildIdPrefix(idPrefix, idCounter);
                OMObservationType observationProxy = o.toXmlProxy(idPrefWithCounter);
                
                // Adding to response proxy
                ObservationData observationData = new ObservationData();
                observationData.setOMObservation(observationProxy);
                proxy.getObservationData().add(observationData);

                ++idCounter;
            }
        }
        
        // Serialising
        ObjectFactory objectFactorySos = new ObjectFactory();
        Object finalProxy = objectFactorySos.createGetObservationResponse(proxy);
        return XmlHelper.toXmlBytes(finalProxy);
	}
	
	private String buildIdPrefix(String idPrefix, int idCounter)
    {
        // Would give, e.g., "GetObsResp_i3-"
        return idPrefix + idCounter + "-";
    }
	
	private void populateFromProxy(GetObservationResponseType proxy) throws InvalidMessageException
	{
		m_observations = new ArrayList<>();
		
		if (proxy.getObservationData() != null)
		{
			for (ObservationData od : proxy.getObservationData())
			{
				Observation observation = new Observation(od.getOMObservation());
				m_observations.add(observation);
			}
		}
	}
}
