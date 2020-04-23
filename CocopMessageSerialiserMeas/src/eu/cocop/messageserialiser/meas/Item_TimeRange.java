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

import javax.xml.bind.JAXBElement;

import net.opengis.gml._3.TimePeriodPropertyType;
import net.opengis.gml._3.TimePeriodType;
import net.opengis.gml._3.TimePositionType;
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.TimeRangeType;
import net.opengis.swe._2.UnitReference;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;

/**
 * Represents a time period or range.
 * 
 * In this module, the code has been derived from OpenGIS(r)
 * Geography Markup Language (GML) Encoding Standard 
 * (OGC 07-036; please see the file "ref_and_license_ogc_gml.txt") and
 * OGC(r) SWE Common Data Model Encoding Standard
 * (OGC 08-094r1; please see the file "ref_and_license_ogc_swecommon.txt").
 * @see Item_TimeInstant
 * @author Petri Kannisto
 */
public final class Item_TimeRange extends Item
{
	/*
	This should be OK based on O&M 2.0 XML schemata and XML examples (but not the only possible way):
	<om:result xsi:type="gml:TimePeriodPropertyType">
	  <gml:TimePeriod gml:id="tm876">
	    <gml:beginPosition>2010-03-24T10:00:00</gml:beginPosition>
	    <gml:endPosition>2010-03-25T08:50:00</gml:endPosition>
	  </gml:TimePeriod>
	</om:result>
	*/
	
	private final Item_TimeInstant m_start;
	private final Item_TimeInstant m_end;
	
	
	/**
	 * Constructor.
	 * @param start Start time of the period.
	 * @param end End time of the period.
	 */
	public Item_TimeRange(Item_TimeInstant start, Item_TimeInstant end)
	{
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		if (end.getValue().isBefore(start.getValue()))
		{
			throw new IllegalArgumentException("Time period: end time must not be before start");
		}
		
		m_start = start;
		m_end = end;
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (phenomenon time).
	 * @param el XML contents.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeRange(TimePeriodType el) throws InvalidMessageException
	{
		// PhenomenonTime uses this ctor.
		
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		try
		{
			m_start = new Item_TimeInstant(el.getBeginPosition());
			m_end = new Item_TimeInstant(el.getEndPosition());
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException(e.getMessage(), e);
		}
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (observation result).
	 * @param el XML contents.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeRange(TimePeriodPropertyType el) throws InvalidMessageException
	{
		// The funny time-related metadata structures of O&M make this almost
		// redundant constructor about necessary.
		// TimeSeries uses this as well as TimePeriods as the observation result.
		
		this(el.getTimePeriod());
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (data record field).
	 * @param el XML contents.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeRange(TimeRangeType el) throws InvalidMessageException
	{
		// DataRecord uses this ctor.
		
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		try
		{
			String sttRaw = el.getValue().get(0);
			String endRaw = el.getValue().get(1);
			
			m_start = new Item_TimeInstant(sttRaw);
			m_end = new Item_TimeInstant(endRaw);
		}
		catch (Exception e)
		{
			throw new InvalidMessageException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * Generates an XML proxy.
	 * @param idPrefix ID prefix to enable the generation of unique IDs within the document.
	 * @return Proxy.
	 */
	TimePeriodType toXmlProxy(String idPrefix)
	{
		TimePositionType startPosition = createTimePositionForMarshal(m_start);
		TimePositionType endPosition = createTimePositionForMarshal(m_end);
		TimePeriodType timePeriod = new TimePeriodType();
		timePeriod.setBeginPosition(startPosition);
		timePeriod.setEndPosition(endPosition);
		
		// An ID is required per the XML schema
		timePeriod.setId(idPrefix + "TimeRange");
		
		return timePeriod;
	}
	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		// Wrapping the basic structure to a TimePeriodPropertyType
		
		TimePeriodPropertyType prop = new TimePeriodPropertyType();
		prop.setTimePeriod(toXmlProxy(idPrefix));
		
		return prop;
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		TimeRangeType retval = new TimeRangeType();
		retval.getValue().add(m_start.toXsdDateTime());
		retval.getValue().add(m_end.toXsdDateTime());
		
		// Adding an empty UOM element (the schema requires it although it is obviously needless for timestamps)
		UnitReference unitRef = new UnitReference();
		retval.setUom(unitRef);
		
		return fact.createTimeRange(retval);
	}
	
	
	/**
	 * The start time of the period.
	 * @return The start time of the period.
	 */
	public Item_TimeInstant getStart()
	{
		return m_start;
	}
	
	/**
	 * The end time of the period.
	 * @return The end time of the period.
	 */
	public Item_TimeInstant getEnd()
	{
		return m_end;
	}
	
	private TimePositionType createTimePositionForMarshal(Item_TimeInstant dateTime)
	{
		TimePositionType positionObj = new TimePositionType();
		positionObj.getValue().add(dateTime.toXsdDateTime());
		return positionObj;
	}
}
