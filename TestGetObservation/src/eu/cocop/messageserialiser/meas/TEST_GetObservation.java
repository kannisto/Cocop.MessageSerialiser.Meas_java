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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TEST_GetObservation
{
	// This test focuses on:
    // - The creation of a "get" request (110) with default values
    // - Reading a "get" request (120)
    // - The creation of a "get" request (130) with non-default values
    // - Reading a "get" response (210)
    // - The creation of a "get" response (220)
    // - Inclusion of the actual payload (observations, time series...) (210, 220)
    //
    // This test does *not* focus on:
    // - Temporal filters, observations or any other included items (they are tested elsewhere)
	
	private static Validator m_validator = null;
	
	
	@Test
	public void getObsReq_110_createWithDefault() throws Exception
	{
		// This test exists mainly to make sure that no errors occur the default values

		GetObservationRequest testObject = new GetObservationRequest();

        // Serialising and deserialising request object
		byte[] xmlBytes = testObject.toXmlBytes();
		validateXmlDoc(xmlBytes);
        GetObservationRequest testObjectIn = new GetObservationRequest(xmlBytes);

        // Asserting
        assertEquals(0, testObjectIn.getFeaturesOfInterest().size());
        assertEquals(0, testObjectIn.getItems().size());
        assertEquals(0, testObjectIn.getObservedProperties().size());
        assertEquals(0, testObjectIn.getTemporalFilters().size());
	}
	
	@Test
	public void getObsReq_120_readXml() throws Exception
	{
		String filepath = getPathOfTestFile("GetObservationRequest.xml");
		GetObservationRequest testObject = new GetObservationRequest(readFile(filepath));

        // Asserting
		assertEquals(1, testObject.getFeaturesOfInterest().size());
		assertEquals(1, testObject.getObservedProperties().size());
        assertTrue(testObject.getObservedProperties().contains("304-TI-101"));
        assertTrue(testObject.getFeaturesOfInterest().contains("cocop/somefeature"));

        // Asserting extension (a data record)
        assertEquals(1, testObject.getItems().size());
        Item_DataRecord extension = (Item_DataRecord)testObject.getItems().get(0);
        Item_Measurement measurementItem = (Item_Measurement)extension.getItem("MyDataRecordItem");
        assertEquals(324.23, measurementItem.getValue(), 0.0001);


        assertEquals(2, testObject.getTemporalFilters().size());

        // Asserting temporal filter 1 (the assertion has a low coverage because filters are tested elsewhere)
        TemporalFilter filter1 = testObject.getTemporalFilters().get(0);
        assertEquals(TemporalFilter.ValueReferenceType.ResultTime, filter1.getValueReference());
        assertEquals(TemporalFilter.OperatorType.After, filter1.getOperator());

        // Asserting temporal filter 2
        TemporalFilter filter2 = testObject.getTemporalFilters().get(1);
        assertEquals(TemporalFilter.ValueReferenceType.PhenomenonTime, filter2.getValueReference());
        assertEquals(TemporalFilter.OperatorType.During, filter2.getOperator());
	}
	
	@Test
	public void getObsReq_130_createXml() throws Exception
	{
		// Creating request object
		GetObservationRequest testObject = new GetObservationRequest();
        testObject.getFeaturesOfInterest().add("myfeature");
        testObject.getObservedProperties().add("myproperty");

        // Adding a data record
        Item_DataRecord extensionObj = new Item_DataRecord();
        extensionObj.addItem("MyMeasurement", new Item_Measurement("s", 0.453));
        testObject.getItems().add(extensionObj);

        // Adding temporal filters
        DateTime rangeStt = DateTime.now().withZone(DateTimeZone.UTC);
        DateTime rangeEnd = rangeStt.plusHours(2);
        TemporalFilter tempFilter1 = new TemporalFilter(
            TemporalFilter.ValueReferenceType.ResultTime,
            TemporalFilter.OperatorType.During,
            new Item_TimeRange(new Item_TimeInstant(rangeStt), new Item_TimeInstant(rangeEnd))
            );
        testObject.getTemporalFilters().add(tempFilter1);
        TemporalFilter tempFilter2 = new TemporalFilter(
            TemporalFilter.ValueReferenceType.PhenomenonTime,
            TemporalFilter.OperatorType.Before,
            new Item_TimeInstant(DateTime.now().withZone(DateTimeZone.UTC))
            );
        testObject.getTemporalFilters().add(tempFilter2);

        // Serialising and deserialising request object
        byte[] xmlBytes = testObject.toXmlBytes();
        //String xmlString = new String(xmlBytes);
		validateXmlDoc(xmlBytes);
        GetObservationRequest testObjectIn = new GetObservationRequest(xmlBytes);

        // Asserting
        assertEquals(1, testObjectIn.getFeaturesOfInterest().size());
        assertEquals(1, testObjectIn.getObservedProperties().size());
        assertTrue(testObjectIn.getFeaturesOfInterest().contains("myfeature"));
        assertTrue(testObjectIn.getObservedProperties().contains("myproperty"));

        // Asserting extension (a data record)
        assertEquals(1, testObjectIn.getItems().size());
        Item_DataRecord extension = (Item_DataRecord)testObjectIn.getItems().get(0);
        Item_Measurement measurementItem = (Item_Measurement)extension.getItem("MyMeasurement");
        assertEquals(0.453, measurementItem.getValue(), 0.0001);


        assertEquals(2, testObject.getTemporalFilters().size());

        // Asserting temporal filter 1 (the assertion has a low coverage because filters are tested elsewhere)
        TemporalFilter filter1 = testObjectIn.getTemporalFilters().get(0);
        assertEquals(TemporalFilter.ValueReferenceType.ResultTime, filter1.getValueReference());
        assertEquals(TemporalFilter.OperatorType.During, filter1.getOperator());

        // Asserting temporal filter 2
        TemporalFilter filter2 = testObjectIn.getTemporalFilters().get(1);
        assertEquals(TemporalFilter.ValueReferenceType.PhenomenonTime, filter2.getValueReference());
        assertEquals(TemporalFilter.OperatorType.Before, filter2.getOperator());
	}
	
	@Test
	public void getObsResp_210_readXml() throws Exception
	{
		String filepath = getPathOfTestFile("GetObservationResponse.xml");
		GetObservationResponse testObject = new GetObservationResponse(readFile(filepath));

        // Asserting observations
        assertEquals(2, testObject.getObservations().size());
        Observation obs1 = testObject.getObservations().get(0);
        Observation obs2 = testObject.getObservations().get(1);
        Item_Measurement meas1 = (Item_Measurement)obs1.getResult();
        Item_Measurement meas2 = (Item_Measurement)obs2.getResult();
        assertEquals(20.3, meas1.getValue(), 0.0001);
        assertEquals(20.5, meas2.getValue(), 0.0001);
	}
	
	@Test
	public void getObsResp_220_createXml() throws Exception
	{
		GetObservationResponse objectOut = new GetObservationResponse();

        // Setting observation data
		Observation observation1 = new Observation(new Item_Measurement("s", 2.2));
		Observation observation2 = new Observation(new Item_Measurement("s", 2.4));
        objectOut.getObservations().add(observation1);
        objectOut.getObservations().add(observation2);
        
        // Serialising and deserialising the object
        byte[] xmlBytes = objectOut.toXmlBytes();
        validateXmlDoc(xmlBytes);
        GetObservationResponse objectIn = new GetObservationResponse(xmlBytes);

        // Asserting observations (only some values)
        Item_Measurement resultIn1 = (Item_Measurement)objectIn.getObservations().get(0).getResult();
        Item_Measurement resultIn2 = (Item_Measurement)objectIn.getObservations().get(1).getResult();
        assertEquals(2.2, resultIn1.getValue(), 0.0001);
        assertEquals(2.4, resultIn2.getValue(), 0.0001);
	}
	
	private String getPathOfTestFile(String filename)
	{
		return System.getProperty("user.dir") + "/../common/testfiles/" + filename;
	}
	
	private static byte[] readFile(String path) throws IOException 
	{
		return Files.readAllBytes(Paths.get(path));
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
