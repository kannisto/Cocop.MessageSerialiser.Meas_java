//
// Please make sure to read and understand the files README.md and LICENSE.txt.
// 
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
//
// Author: Petri Kannisto, Tampere University, Finland
// File created: 2/2018
// Last modified: 4/2020

package eu.cocop.messageserialiser.meas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.joda.time.Period;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import eu.cocop_spire.om_custom._1_2.ArrayType;
import net.opengis.fes._2.TemporalOpsType;
import net.opengis.gml._3.ObservationType;
import net.opengis.sos._2.GetObservationResponseType;
import net.opengis.sos._2.GetObservationType;
import net.opengis.sos._2.GetObservationType.TemporalFilter;
import net.opengis.swe._2.DataRecordType;
import net.opengis.tsml._1.TimeseriesDomainRangeType;

/**
 * Contains methods to help processing XML documents.
 * @author Petri Kannisto
 */
class XmlHelper
{
	public static final String TYPEURI_START               = "http://www.opengis.net/def/observationType/OGC-OM/2.0/";
	public static final String TYPEURI_CATEGORY            = TYPEURI_START + "OM_CategoryObservation";
	public static final String TYPEURI_COMPLEX             = TYPEURI_START + "OM_ComplexObservation";
	public static final String TYPEURI_COUNT               = TYPEURI_START + "OM_CountObservation";
	public static final String TYPEURI_MEASUREMENT         = TYPEURI_START + "OM_Measurement";
	public static final String TYPEURI_TEMPORAL            = TYPEURI_START + "OM_TemporalObservation";
	public static final String TYPEURI_TIMESERIES_DISCRETE = TYPEURI_START + "OM_DiscreteTimeSeriesObservation";
	public static final String TYPEURI_TRUTH               = TYPEURI_START + "OM_TruthObservation";
	
	public static final String TYPEURI_CUSTOMSTART        = "cocop/observationType/";
	public static final String TYPEURI_TEXT               = TYPEURI_CUSTOMSTART + "Text";
	public static final String TYPEURI_TIMESERIESFLEXIBLE = TYPEURI_CUSTOMSTART + "TimeSeriesFlexible";
    public static final String TYPEURI_TIMESERIESCONSTANT = TYPEURI_CUSTOMSTART + "TimeSeriesRegular";
	
	private static Object m_jaxbContextLock = new Object();
	private static JAXBContext m_jaxbContext = null; // This will be created as needed
	
	
	private XmlHelper()
	{
		// Private ctor -> "static" class
	}
	
	/**
	 * Gets a JAXB context object.
	 * @return JAXB context.
	 * @throws JAXBException (Not expected in normal conditions.)
	 */
	static JAXBContext getJaxbContext() throws JAXBException
	{
		// JAXBContext is thread-safe, but protecting the static JAXB context variable
		synchronized (m_jaxbContextLock)
		{
			if (m_jaxbContext == null)
			{
				@SuppressWarnings("rawtypes")
				Class[] classes = new Class[8];
				classes[0] = ObservationType.class;
				classes[1] = DataRecordType.class;
				classes[2] = TimeseriesDomainRangeType.class;
				classes[3] = TemporalFilter.class;
				classes[4] = TemporalOpsType.class;
				classes[5] = GetObservationType.class;
				classes[6] = GetObservationResponseType.class;
				classes[7] = ArrayType.class;
				m_jaxbContext = JAXBContext.newInstance(classes);
			}
			
			return m_jaxbContext;
		}
	}
	
	/**
	 * Parses a period value from XML.
	 * @param v Value as XML string.
	 * @return Period.
	 * @throws Thrown if the parsing fails.
	 */
	static Period parseXmlPeriod(String v) throws IllegalArgumentException
	{
		return Period.parse(v);
	}
	
	/**
	 * Serialises a period value to XML.
	 * @param v Period.
	 * @return String presentation.
	 */
	static String serialiseXmlPeriod(Period v)
	{
		return v.toString();
	}
	
	/**
	 * Parses a boolean value from XML.
	 * @param v Value as string.
	 * @return Boolean.
	 * @throws IllegalArgumentException Thrown if parsing fails.
	 */
	static boolean parseXmlBoolean(String v)
	{
		// DatatypeConverter.parseBoolean works incorrectly!
		// Therefore, implemented a custom function.
		// Legal values: booleanRep ::= 'true' | 'false' | '1' | '0'
		// See https://www.w3.org/TR/xmlschema11-2/#boolean
		
		String str = v.trim();
		
		if (str.equals("0") || str.equals("false"))
		{
			return false;
		}
		else if (str.equals("1") || str.equals("true"))
		{
			return true;
		}
		else
		{
			throw new IllegalArgumentException("Failed to parse xsd:boolean from \"" + str + "\"");
		}
	}
	
	/**
	 * Serialises a boolean value to XML.
	 * @param v Value.
	 * @return Value as string.
	 */
	static String serialiseXmlBoolean(boolean v)
	{
		return DatatypeConverter.printBoolean(v);
	}
	
	/**
	 * Parses an int value from XML.
	 * @param v Value as string.
	 * @return Int.
	 * @throws NumberFormatException Thrown if parsing fails.
	 */
	static int parseXmlInt(String v)
	{
		return DatatypeConverter.parseInt(v);
	}
	
	/**
	 * Serialises an int value to XML.
	 * @param v Value.
	 * @return Value as string.
	 */
	static String serialiseXmlInt(int v)
	{
		return DatatypeConverter.printInt(v);
	}
	
	/**
	 * Parses a long value from XML.
	 * @param v Value as string.
	 * @return Long.
	 * @throws NumberFormatException Thrown if parsing fails.
	 */
	static long parseXmlLong(String v)
	{
		return DatatypeConverter.parseLong(v);
	}
	
	/**
	 * Serialises a long value to XML.
	 * @param v Value.
	 * @return Value as string.
	 */
	static String serialiseXmlLong(long v)
	{
		return DatatypeConverter.printLong(v);
	}
	
	/**
	 * Parses a double value from XML.
	 * @param v Value as string.
	 * @return Double.
	 * @throws IllegalArgumentException Thrown if parsing fails.
	 */
	static double parseXmlDouble(String v)
	{
		try
		{
			return DatatypeConverter.parseDouble(v);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Failed to parse double from \"" + v + "\"", e);
		}
	}
	
	/**
	 * Serialises a double value to XML.
	 * @param v Value.
	 * @return Value as string.
	 */
	static String serialiseXmlDouble(double v)
	{
		return DatatypeConverter.printDouble(v);
	}
	
	/**
	 * Serialises an object to XML.
	 * @param proxy Proxy to be serialised.
	 * @return Serialised presentation.
	 */
	static byte[] toXmlBytes(Object proxy)
	{
		ByteArrayOutputStream stream = null;
		OutputStreamWriter writer = null;
		
		try
		{
			stream = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(stream);
			
			// Do marshalling
			Marshaller marshaller = XmlHelper.getJaxbContext().createMarshaller();
			marshaller.marshal(proxy, writer);
			
			writer.flush();
			return stream.toByteArray();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
		finally
		{
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception ignore) {}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ignore) {}
			}
		}
	}
	
	/**
	 * Deserialises an object from XML.
	 * @param xmlBytes XML data.
	 * @return Proxy object.
	 * @throws InvalidMessageException Thrown if a message-related error is found.
	 */
	static Object deserialiseFromXml(byte[] xmlBytes) throws InvalidMessageException
	{
		try
		{
			// Get JAXB context
			JAXBContext jaxbContext = XmlHelper.getJaxbContext();
			
			// Do JAXB unmarshalling
			ByteArrayInputStream reader = null;
			Object proxy = null;
			
			try
			{
				reader = new ByteArrayInputStream(xmlBytes);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				proxy = unmarshaller.unmarshal(reader);
			}
			finally
			{
				if (reader != null) reader.close();
			}
			
			return proxy;
		}
		catch (IOException e)
		{
			// This exception is not expected
			throw new RuntimeException("Failed to parse XML", e);
		}
		catch (JAXBException e)
		{
			throw new InvalidMessageException("Failed to deserialise from XML", e);
		}
	}
}
