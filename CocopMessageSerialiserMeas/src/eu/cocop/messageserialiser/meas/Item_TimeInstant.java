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

import net.opengis.gml._3.TimeInstantPropertyType;
import net.opengis.gml._3.TimeInstantType;
import net.opengis.gml._3.TimePositionType;
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.TimeType;
import net.opengis.swe._2.UnitReference;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;

/**
 * Represents a timestamp either in the past or in the future.
 * 
 * In this module, the code has been derived from OpenGIS(r)
 * Geography Markup Language (GML) Encoding Standard 
 * (OGC 07-036; please see the file "ref_and_license_ogc_gml.txt") and
 * OGC(r) SWE Common Data Model Encoding Standard
 * (OGC 08-094r1; please see the file "ref_and_license_ogc_swecommon.txt").
 * @see Item_TimeRange
 * @author Petri Kannisto
 */
public final class Item_TimeInstant extends Item
{
	/*
	This should be OK based on O&M 2.0 XML schemata and XML examples (but not the only possible way):
	<om:result xsi:type="gml:TimeInstantPropertyType">
	  <gml:TimeInstant gml:id="tm876">
	    <gml:timePosition>2005-01-11T17:22:25.00</gml:timePosition>
	  </gml:TimeInstant>
	</om:result>
	*/
	
	private final DateTime m_dateTime;
	private final boolean m_hasExplicitZone;
	
	
	
	// ### Constructors and related ###
	
	/**
	 * Constructor.
	 * @param dt Timestamp value.
	 * @exception IllegalDateTimeException Thrown if the time zone of dt is not UTC.
	 */
	public Item_TimeInstant(DateTime dt) throws IllegalDateTimeException
	{
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		if (!dt.getZone().equals(DateTimeZone.UTC))
		{
			throw new IllegalDateTimeException("DateTime must have UTC as time zone");
		}
		
		m_hasExplicitZone = true;
		m_dateTime = dt;
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (observation result).
	 * @param el XML contents.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeInstant(TimeInstantPropertyType el) throws InvalidMessageException
	{
		// The time-related metadata structures of O&M make this almost
		// redundant constructor necessary.
		
		// ResultTime field uses this ctor, as well as Results of type TimeInstant.
		
		this(el.getTimeInstant());
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (phenomenon time).
	 * @param el XML contents.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeInstant(TimeInstantType el) throws InvalidMessageException
	{
		// PhenomenonTime field uses this ctor.
		
		this(el.getTimePosition());
	}
	
	/**
	 * Constructor.
	 * @param el XML content.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeInstant(TimePositionType pos) throws InvalidMessageException
	{
		// Item_TimeRange and Item_TimeSeriesConstant use this ctor.
		
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		try
		{
			if (pos.getValue() == null || pos.getValue().size() != 1)
			{
				throw new InvalidMessageException("Invalid time position structure");
			}
			
			String valueRaw = pos.getValue().get(0);
			
			XsdDateTimeParser parser = new XsdDateTimeParser(valueRaw); // throws IllegalArgumentException
			m_hasExplicitZone = parser.explicitTimeZone;
			m_dateTime = parser.parsed;
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException("Failed to read time value from XML - something missing?", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidMessageException("Failed to parse DateTime string", e);
		}
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (data record field).
	 * @param el XML contents.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeInstant(TimeType el) throws InvalidMessageException
	{
		// DataRecord uses this ctor.
		
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		try
		{
			String valueRaw = el.getValue().get(0);
			
			XsdDateTimeParser parser = new XsdDateTimeParser(valueRaw); // throws IllegalArgumentException
			m_hasExplicitZone = parser.explicitTimeZone;
			m_dateTime = parser.parsed;
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException("Failed to read time value from XML - something missing?", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidMessageException("Failed to parse DateTime string", e);
		}
	}
	
	/**
	 * Constructor.
	 * @param xsdDateTime DateTime in XML schema format.
	 * @throws IllegalArgumentException Thrown if parsing fails.
	 */
	Item_TimeInstant(String xsdDateTime) throws IllegalArgumentException
	{
		super(XmlHelper.TYPEURI_TEMPORAL);
		
		try
		{
			XsdDateTimeParser parser = new XsdDateTimeParser(xsdDateTime); // throws IllegalArgumentException
			m_hasExplicitZone = parser.explicitTimeZone;
			m_dateTime = parser.parsed;
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Failed to parse DateTime string", e);
		}
	}
	
	// Parser class for XML schema datetimes
	private class XsdDateTimeParser
	{
		public final boolean explicitTimeZone;
		public final DateTime parsed;
		
		XsdDateTimeParser(String xsdDateTime)
		{
			explicitTimeZone = xsdDateTimeHasTimeZone(xsdDateTime);
			parsed = parseXsdDateTime(xsdDateTime, explicitTimeZone);
		}
		
		private boolean xsdDateTimeHasTimeZone(String input)
		{
			// Expecting <date> "T" <time>
			String[] parts = input.split("T");
			
			if (parts.length != 2) throw new IllegalArgumentException("Failed to parse date and time from string");
			
			String timepart = parts[1];
			
			// If the time zone is known, it is either "Z" for UTC or [+|-]hr:min offset
			return
					// UTC time?
					timepart.endsWith("Z") ||
					// Offset specified?
					timepart.contains("+") || timepart.contains("-");
		}
		
		private DateTime parseXsdDateTime(String xsdDateTime, boolean explicitZone)
		{
			try
			{
				DateTime dt = DateTime.parse(xsdDateTime); // throws IllegalArgumentException?
				
				if (explicitZone)
				{
					return dt.withZone(DateTimeZone.UTC); // Time zone is known -> convert to UTC
				}
				else
				{
					return dt; // Unknown time zone -> leave as such
				}
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Failed to parse DateTime string", e);
			}
		}
	}
	
	
	
	// ### Other methods ###
	
	/**
	 * Generates an XML proxy.
	 * @param idPrefix ID prefix to enable the generation of unique IDs within the document.
	 * @return Proxy.
	 */
	TimeInstantType toXmlProxy(String idPrefix)
	{
		TimePositionType resultTimePos = new TimePositionType();
		resultTimePos.getValue().add(toXsdDateTime());
		TimeInstantType resultTimeInstant = new TimeInstantType();
		resultTimeInstant.setTimePosition(resultTimePos);
		
		// An ID is required per the XML schema
		resultTimeInstant.setId(idPrefix + "TimeInst");
		
		return resultTimeInstant;
	}
	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		// Wrapping the basic structure to a TimeInstantPropertyType
		
		TimeInstantPropertyType instantProp = new TimeInstantPropertyType();
		instantProp.setTimeInstant(toXmlProxy(idPrefix));
		
		return instantProp;
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		TimeType retval = new TimeType();
		retval.getValue().add(toXsdDateTime());
		
		// Adding an empty UOM element (the schema requires it although it is obviously needless for timestamps)
		UnitReference unitRef = new UnitReference();
		retval.setUom(unitRef);
		
		return fact.createTime(retval);
	}
	
	
	/**
	 * The value of the timestamp.
	 * @return The value of the timestamp.
	 */
	public DateTime getValue()
	{
		return m_dateTime;
	}
	
	/**
	 * Whether the object has an explicitly defined UTC offset. The offset is not
	 * always explicit in XML. In such a case, the DateTime type assumes an
	 * offset (presumably local time). However, such implicit actions can lead to
	 * errors. Check this flag to make sure you can rely on the offset value.
	 * @return True if explicit, otherwise false.
	 */
	public boolean getHasExplicitUtcOffset()
	{
		return m_hasExplicitZone;
	}
	
	/**
	 * Serialises the object using the XML Schema DateTime format.
	 * @return Value serialised as string.
	 */
	String toXsdDateTime()
	{
		return m_dateTime.toString();
	}
}
