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

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.gml._3.AbstractTimeObjectType;
import net.opengis.gml._3.CodeType;
import net.opengis.gml._3.FeaturePropertyType;
import net.opengis.gml._3.ReferenceType;
import net.opengis.gml._3.StringOrRefType;
import net.opengis.gml._3.TimeInstantPropertyType;
import net.opengis.gml._3.TimeInstantType;
import net.opengis.om._2.OMObservationType;
import net.opengis.om._2.OMProcessPropertyType;
import net.opengis.om._2.ObjectFactory;
import net.opengis.om._2.TimeObjectPropertyType;

import org.isotc211._2005.gmd.DQElementPropertyType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;

/**
 * Observation class to associate metadata to actual measurement objects.
 * 
 * In this module, the code has been derived from Observations and
 * Measurements - XML Implementation (OGC 10-025r1; please see the file
 * "ref_and_license_ogc_om.txt").
 * @author Petri Kannisto
 */
public final class Observation
{
	private String m_description = null;
	private String m_name = null;
	
	private Item_TimeInstant m_phenomenonTime = null;
	private Item_TimeInstant m_resultTime = null;
	
	private String m_procedure = null;
	private String m_observedProperty = null;
	private String m_featureOfInterest = null;
	private DataQuality m_resultQuality = null;
	
	private Item m_result = null;
	
	
	// ### Constructors ###
	
	/**
	 * Constructor. Use this populate the observation manually.
	 * @param r Result object.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	public Observation(Item r) throws InvalidMessageException
	{
		setDefaults();
		
		m_result = r;
	}
	
	/**
	 * Constructor to populate the information from XML.
	 * @param xmlBytes Serialised XML document.
	 * @exception InvalidMessageException Thrown if an error is encountered.
	 */
	public Observation(byte[] xmlBytes) throws InvalidMessageException
	{
		setDefaults();
		
		try
		{
			@SuppressWarnings("unchecked")
			JAXBElement<OMObservationType> observationJaxb = (JAXBElement<OMObservationType>)XmlHelper.deserialiseFromXml(xmlBytes);
			
			// Reading other values from XML
			readFieldValuesFromXmlDoc(observationJaxb.getValue());
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Failed to parse XML", e);
		}
	}
	
	/**
	 * Constructor to populate the observation from a proxy object.
	 * @param proxy Proxy.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Observation(OMObservationType proxy) throws InvalidMessageException
	{
		readFieldValuesFromXmlDoc(proxy);
	}
	
	private void setDefaults()
	{
		// For the sake of consistency, all defaults are now set here.
		m_description = null;
		m_name = null;
		m_phenomenonTime = null;
		m_resultTime = new Item_TimeInstant(DateTime.now().withZone(DateTimeZone.UTC));
		m_procedure = "";
		m_observedProperty = "";
		m_featureOfInterest = "";
		m_resultQuality = DataQuality.createGood();
		m_result = null;
	}
	
	
	// ### Getters ###
	
	/**
	 * Gets name.
	 * @return Name.
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Gets description.
	 * @return Description.
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Gets phenomenon time.
	 * @return Phenomenon time.
	 */
	public Item_TimeInstant getPhenomenonTime()
	{
		return m_phenomenonTime;
	}
	
	/**
	 * Gets result time.
	 * @return Result time.
	 */
	public Item_TimeInstant getResultTime()
	{
		return m_resultTime;
	}
	
	/**
	 * Gets procedure.
	 * @return Procedure.
	 */
	public String getProcedure()
	{
		return m_procedure;
	}
	
	/**
	 * Gets observed property.
	 * @return Observed property.
	 */
	public String getObservedProperty()
	{
		return m_observedProperty;
	}
	
	/**
	 * Gets feature of interest.
	 * @return Feature of interest.
	 */
	public String getFeatureOfInterest()
	{
		return m_featureOfInterest;
	}
	
	/**
	 * Gets result quality.
	 * @return Result quality.
	 */
	public DataQuality getResultQuality()
	{
		return m_resultQuality;
	}
	
	/**
	 * Gets result.
	 * @return Result.
	 */
	public Item getResult()
	{
		return m_result;
	}
	
	
	// ### Setters ###
	
	/**
	 * Sets name.
	 * @param s Name.
	 */
	public void setName(String s)
	{
		m_name = s;
	}
	
	/**
	 * Sets description.
	 * @param s Description.
	 */
	public void setDescription(String s)
	{
		m_description = s;
	}
	
	/**
	 * Sets phenomenon time.
	 * @param dt Phenomenon time.
	 */
	public void setPhenomenonTime(Item_TimeInstant dt)
	{
		m_phenomenonTime = dt;
	}
	
	/**
	 * Sets result time.
	 * @param dt Result time.
	 */
	public void setResultTime(Item_TimeInstant dt)
	{
		m_resultTime = dt;
	}
	
	/**
	 * Sets procedure.
	 * @param s Procedure.
	 */
	public void setProcedure(String s)
	{
		m_procedure = s;
	}
	
	/**
	 * Sets observed property.
	 * @param s Observed property.
	 */
	public void setObservedProperty(String s)
	{
		m_observedProperty = s;
	}
	
	/**
	 * Sets feature of interest.
	 * @param s Feature of interest.
	 */
	public void setFeatureOfInterest(String s)
	{
		m_featureOfInterest = s;
	}
	
	/**
	 * Sets result quality.
	 * @param d Result quality.
	 */
	public void setResultQuality(DataQuality d)
	{
		m_resultQuality = d;
	}
	
	
	// ### Other public functions ###
	
	/**
	 * Serialises the object to XML.
	 * @return Serialised presentation.
	 */
	public byte[] toXmlBytes()
	{
		// Create proxy
		OMObservationType proxy = toXmlProxy(""); // No ID prefix because document root is assumed
		ObjectFactory objectFactory = new ObjectFactory();
		Object actualProxy = objectFactory.createOMObservation(proxy);
		
		// Serialising
		return XmlHelper.toXmlBytes(actualProxy);
	}
	
	/**
	 * Generates an XML proxy.
	 * @param idPrefix ID prefix to enable the generation of unique IDs within the document.
	 * @return Proxy.
	 */
	OMObservationType toXmlProxy(String idPrefix)
	{
		// Creating an ID prefix to enable unique IDs within the XML doc
        String myUniqueId = idPrefix + "Obs";
		
		// Assigning values to a JAXB observation object
		OMObservationType obsToMarshal = new OMObservationType();
		
		// ID
		obsToMarshal.setId(myUniqueId);
		
		// Type
		ReferenceType typeRefType = new ReferenceType();
		typeRefType.setHref(m_result.getObservationTypeUri().toString());
		obsToMarshal.setType(typeRefType);
		
		// Name
		if (m_name != null && !m_name.isEmpty())
		{
			CodeType ct = new CodeType();
			ct.setValue(m_name);
			obsToMarshal.getName().add(ct);
		}
		
		// Description
		if (m_description != null && !m_description.isEmpty())
		{
			StringOrRefType desc = new StringOrRefType();
			desc.setValue(m_description);
			obsToMarshal.setDescription(desc);
		}
		
		// Phenomenon time
		TimeObjectPropertyType phenoProp = new TimeObjectPropertyType();
		
		if (m_phenomenonTime == null)
		{
			// Default: using result time as the phenomenon time
			m_phenomenonTime = m_resultTime;
		}
		
		TimeInstantType phenoInstantEl = m_phenomenonTime.toXmlProxy(myUniqueId + "_pheno_");
		net.opengis.gml._3.ObjectFactory gmlObjFactory = new net.opengis.gml._3.ObjectFactory();
		phenoProp.setAbstractTimeObject(gmlObjFactory.createTimeInstant(phenoInstantEl));
		
		obsToMarshal.setPhenomenonTime(phenoProp);
		
		// Result time
		TimeInstantPropertyType resultTimeProp = new TimeInstantPropertyType();
		TimeInstantType resultTimeInstant = m_resultTime.toXmlProxy(myUniqueId + "_res_");
		resultTimeProp.setTimeInstant(resultTimeInstant);
		obsToMarshal.setResultTime(resultTimeProp);
		
		// Procedure
		OMProcessPropertyType procedure = new OMProcessPropertyType();
		procedure.setTitleAttr(m_procedure.toString());
		obsToMarshal.setProcedure(procedure);
		
		// Observed property
		ReferenceType observedProp = new ReferenceType();
		observedProp.setTitleAttr(m_observedProperty.toString());
		obsToMarshal.setObservedProperty(observedProp);
		
		// Feature of interest
		FeaturePropertyType feature = new FeaturePropertyType();
		feature.setTitleAttr(m_featureOfInterest.toString());
		obsToMarshal.setFeatureOfInterest(feature);
		
		// Result quality
		DQElementPropertyType resultQuality = new DQElementPropertyType();
		resultQuality.setTitleAttr(m_resultQuality.getValue());
		obsToMarshal.getResultQuality().add(resultQuality);
		
		// Result
		obsToMarshal.setResult(m_result.getObjectForXml_Result(myUniqueId + "_result_"));
		
		return obsToMarshal;
	}
	
	
	// ### Private functions for read ###
	
	private void readFieldValuesFromXmlDoc(OMObservationType observationRaw) throws InvalidMessageException
	{
		try
		{
			// Getting observation type URI
			String typeUri = observationRaw.getType().getHref();
			
			// Name specified?
			List<CodeType> nameList = observationRaw.getName();
			
			if (nameList != null && nameList.size() > 0)
			{
				m_name = nameList.get(0).getValue();
			}
			
			// Description
			if (observationRaw.getDescription() != null)
			{
				m_description = observationRaw.getDescription().getValue();
			}
			
			AbstractTimeObjectType abstractPhenoTime = observationRaw.getPhenomenonTime().getAbstractTimeObject().getValue();
			
			m_phenomenonTime = new Item_TimeInstant((TimeInstantType)abstractPhenoTime);
			
			// Result time
			m_resultTime = new Item_TimeInstant(observationRaw.getResultTime());
			
			// These fields are required per the schema, but this processing enables non-specified values
			m_procedure = (observationRaw.getProcedure() != null && observationRaw.getProcedure().getTitleAttr() != null) ?
					observationRaw.getProcedure().getTitleAttr() : null;
			m_observedProperty = (observationRaw.getObservedProperty() != null && observationRaw.getObservedProperty().getTitleAttr() != null) ?
					observationRaw.getObservedProperty().getTitleAttr() : null;
			m_featureOfInterest = (observationRaw.getFeatureOfInterest() != null && observationRaw.getFeatureOfInterest().getTitleAttr() != null) ?
					observationRaw.getFeatureOfInterest().getTitleAttr() : null;
			
			// Explicit result quality defined?
			if (observationRaw.getResultQuality() != null && observationRaw.getResultQuality().size() > 0)
			{
				String qualityStringRaw = observationRaw.getResultQuality().get(0).getTitleAttr();
				m_resultQuality = new DataQuality(qualityStringRaw);
			}
			
			// Processing result information
			m_result = ResultTypeManager.buildResultFromXml(typeUri, observationRaw.getResult());
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException(e.getMessage(), e);
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException(e.getMessage(), e);
		}
	}
}
