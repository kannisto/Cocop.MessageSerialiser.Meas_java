//
// Please make sure to read and understand the files README.md and LICENSE.txt.
// 
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
//
// Author: Petri Kannisto, Tampere University, Finland
// File created: 7/2018
// Last modified: 4/2020

package eu.cocop.messageserialiser.meas;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.xml.sax.SAXException;

import net.opengis.sos._2.GetObservationType;
import net.opengis.sos._2.ObjectFactory;

public class TEST_TemporalFilter
{
	private static Validator m_validator = null;
	
	@Test
	public void tempF_100_create()
	{
		// Testing how the constructor works with invalid input

		DateTime rangeSttDt = DateTime.now().withZone(DateTimeZone.UTC);
		Item_TimeInstant rangeStt = new Item_TimeInstant(rangeSttDt);
		Item_TimeInstant rangeEnd = new Item_TimeInstant(rangeSttDt.plusHours(1));
		Item_TimeRange timeRange1 = new Item_TimeRange(rangeStt, rangeEnd);
		
		Item_TimeInstant timeInstant1 = new Item_TimeInstant(DateTime.now().withZone(DateTimeZone.UTC));

        // These operators require a time instant as the operand
        assertCtorException(TemporalFilter.OperatorType.After, timeRange1);
        assertCtorException(TemporalFilter.OperatorType.Before, timeRange1);

        // These operators require a time range as the operand
        assertCtorException(TemporalFilter.OperatorType.During, timeInstant1);
	}
	
	@Test
	public void tempF_210_readAfter() throws Exception
	{
		// Testing the "after" operator

        String filepath = getPathOfTestFile("TemporalFilter_after.xml");

        testTimeInstantFromFile(
            filepath,
            TemporalFilter.ValueReferenceType.ResultTime,
            TemporalFilter.OperatorType.After, 
            getUtcTime("2018-03-28T12:01:59Z")
            );
	}
	
	@Test
	public void tempF_220_readBefore() throws Exception
	{
		// Testing the "before" operator

		String filepath = getPathOfTestFile("TemporalFilter_before.xml");

        testTimeInstantFromFile(
            filepath,
            TemporalFilter.ValueReferenceType.PhenomenonTime,
            TemporalFilter.OperatorType.Before,
            getUtcTime("2018-04-18T03:44:11Z")
            );
	}
	
	@Test
	public void tempF_230_readDuring() throws Exception
	{
		// Testing the "during" operator

		String filepath = getPathOfTestFile("TemporalFilter_during.xml");

        // Getting the proxy
        net.opengis.sos._2.GetObservationType.TemporalFilter proxy = parseRaw(filepath);

        // Creating an object from the proxy
        TemporalFilter testObject = new TemporalFilter(proxy);

        // Asserting value reference
        assertEquals(TemporalFilter.ValueReferenceType.PhenomenonTime, testObject.getValueReference());

        // Asserting operator
        assertEquals(TemporalFilter.OperatorType.During, testObject.getOperator());

        // Expecting a time range
        Item_TimeRange timeRange = (Item_TimeRange)testObject.getTime();
        assertDateTime(getUtcTime("2018-05-18T03:23:11Z"), timeRange.getStart());
        assertDateTime(getUtcTime("2018-05-18T03:23:29Z"), timeRange.getEnd());
	}
	
	@Test
	public void tempF_310_createAndReadXml_after() throws Exception
	{
		// Testing the "after" operator
        testCreateAndReadTimeInstant(TemporalFilter.ValueReferenceType.ResultTime, TemporalFilter.OperatorType.After);
	}
	
	@Test
	public void tempF_320_createAndReadXml_before() throws Exception
	{
		// Testing the "before" operator
        testCreateAndReadTimeInstant(TemporalFilter.ValueReferenceType.PhenomenonTime, TemporalFilter.OperatorType.Before);
	}
	
	@Test
	public void tempF_330_createAndReadXml_during() throws Exception
	{
		// Testing the "during" operator

        // Creating a time range
		DateTime stt = getUtcTime("2018-04-18T03:23:11Z");
		DateTime end = getUtcTime("2018-05-18T03:23:11Z");
		Item_TimeRange timeRange = new Item_TimeRange(new Item_TimeInstant(stt), new Item_TimeInstant(end));

		TemporalFilter testObject = new TemporalFilter(TemporalFilter.ValueReferenceType.PhenomenonTime, TemporalFilter.OperatorType.During, timeRange);

        // Serialise and read
		TemporalFilter testObjectIn = serialiseAndRead(testObject);

        // Asserting value reference
        assertEquals(TemporalFilter.ValueReferenceType.PhenomenonTime, testObjectIn.getValueReference());

        // Asserting operator
        assertEquals(TemporalFilter.OperatorType.During, testObjectIn.getOperator());

        // Expecting a time range
        Item_TimeRange timeRangeIn = (Item_TimeRange)testObjectIn.getTime();
        assertDateTime(stt, timeRangeIn.getStart());
        assertDateTime(end, timeRangeIn.getEnd());
	}
	
	private void testCreateAndReadTimeInstant(TemporalFilter.ValueReferenceType valueRef, TemporalFilter.OperatorType oper) throws Exception
    {
        // This general test method tests the serialisation and deserialisation of
        // a filter that has a time instant operand
        
        // Creating a time instant
		DateTime time = getUtcTime("2018-01-18T03:23:34Z");
		Item_TimeInstant timeInstant = new Item_TimeInstant(time);

		TemporalFilter testObject = new TemporalFilter(valueRef, oper, timeInstant);

        // Serialise and read
        TemporalFilter testObjectIn = serialiseAndRead(testObject);

        // Asserting value reference
        assertEquals(valueRef, testObjectIn.getValueReference());

        // Asserting operator
        assertEquals(oper, testObjectIn.getOperator());

        // Expecting a time instant
        Item_TimeInstant timeInstantIn = (Item_TimeInstant)testObjectIn.getTime();
        assertDateTime(time, timeInstantIn.getValue());
    }
	
	private TemporalFilter serialiseAndRead(TemporalFilter input) throws Exception
    {
		// Get proxy
		
		net.opengis.sos._2.GetObservationType.TemporalFilter filterProxy = input.toXmlProxy("test_");

        // Wrap the proxy in a GetObservationRequest
        GetObservationType requestProxy = new GetObservationType();
        requestProxy.setService("SOS");
        requestProxy.setVersion("2.0.0");
        requestProxy.getTemporalFilter().add(filterProxy);
        ObjectFactory objectFactorySos = new ObjectFactory();
        Object wrapperProxy = objectFactorySos.createGetObservation(requestProxy);

        // Serialise the proxy
        byte[] xmlBytes = XmlHelper.toXmlBytes(wrapperProxy);
        
        // Validating
        validateXmlDoc(xmlBytes);

        // Read XML data
        net.opengis.sos._2.GetObservationType.TemporalFilter proxyIn = deserialiseAndGetProxy(xmlBytes);
        return new TemporalFilter(proxyIn);
    }
	
	private net.opengis.sos._2.GetObservationType.TemporalFilter deserialiseAndGetProxy(byte[] xmlBytes) throws Exception
    {
        // The GetObservationType wraps the filter as using a bare filter in an XML document would have
        // required additional work without any value for the use cases.
        // In contrast, GetObservationType is utilised elsewhere anyway.
		@SuppressWarnings("unchecked")
		JAXBElement<GetObservationType> proxy = (JAXBElement<GetObservationType>)XmlHelper.deserialiseFromXml(xmlBytes);
        return proxy.getValue().getTemporalFilter().get(0);
    }
	
	private void testTimeInstantFromFile(String filepath, TemporalFilter.ValueReferenceType valRef, TemporalFilter.OperatorType expectedOperator, DateTime expDateTime) throws Exception
    {
        // Getting the proxy
		net.opengis.sos._2.GetObservationType.TemporalFilter proxy = parseRaw(filepath);

        // Creating an object from the proxy
		TemporalFilter testObject = new TemporalFilter(proxy);

        // Asserting value reference
        assertEquals(valRef, testObject.getValueReference());

        // Asserting operator
        assertEquals(expectedOperator, testObject.getOperator());

        // Expecting a time instant
        Item_TimeInstant timeInstant = (Item_TimeInstant)testObject.getTime();
        assertDateTime(expDateTime, timeInstant.getValue());
    }
	
	private void assertCtorException(TemporalFilter.OperatorType op, Item timeItem)
    {
        try
        {
            new TemporalFilter(TemporalFilter.ValueReferenceType.PhenomenonTime, op, timeItem);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {}
    }
	
	private net.opengis.sos._2.GetObservationType.TemporalFilter parseRaw(String filepath) throws Exception
	{
		// Get JAXB context
		JAXBContext jaxbContext = XmlHelper.getJaxbContext();
		
		// Do JAXB unmarshalling
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		File fileToParse = new File(filepath);
		@SuppressWarnings("unchecked")
		JAXBElement<GetObservationType> getObs = 
		    (JAXBElement<GetObservationType>)unmarshaller.unmarshal(fileToParse);
		
		return getObs.getValue().getTemporalFilter().get(0);
	}
	
	private void assertDateTime(DateTime expected, Item_TimeInstant actual)
	{
		assertDateTime(expected, actual.getValue());
	}
	
	private void assertDateTime(DateTime expected, DateTime actual)
	{
		// Always expecting UTC
		assertEquals(DateTimeZone.UTC, actual.getZone());
		
		assertEquals(expected.toString(), actual.toString());
	}
	
	private DateTime getUtcTime(String xsdDateTime)
	{
		return DateTime.parse(xsdDateTime).withZone(DateTimeZone.UTC);
	}
	
	private String getPathOfTestFile(String filename)
	{
		return System.getProperty("user.dir") + "/../common/testfiles/" + filename;
	}
	
	private void validateXmlDoc(byte[] xmlBytes) throws Exception
	{
		ByteArrayInputStream stream = null;
		
		try
		{
			stream = new ByteArrayInputStream(xmlBytes);
			Source xmlFile = new StreamSource(stream);
			
			if (m_validator == null)
			{
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				String schemaPath = System.getProperty("user.dir") + "\\..\\Schemata\\helper.xsd";
				Schema observationSchema = schemaFactory.newSchema(new File(schemaPath));
				m_validator = observationSchema.newValidator();
			}
			
			m_validator.validate(xmlFile);
		}
		catch (SAXException e)
		{
			fail("XML validation failed: " + e.getMessage());
		}
		finally
		{
			if (stream != null)
			{
				try {
					stream.close();
				} catch (Exception ignore) {}
			}
		}
	}
}
