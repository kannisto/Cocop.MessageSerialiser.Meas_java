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

package observationtests;

import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.cocop.messageserialiser.meas.DataQuality;
import eu.cocop.messageserialiser.meas.Item_Measurement;
import eu.cocop.messageserialiser.meas.Item_TimeInstant;
import eu.cocop.messageserialiser.meas.Observation;

public class TEST_Observation
{
	/*
	 * This test has its focus on the Observation class and its serialisation.
	 * 
	 * Stub classes are utilised for some related classes, but some are included as such.
	 * XML validation sets various requirements, so using the actual implemented
	 * class (such as Item_TimePeriod) makes testing easier.
	 * 
	 * The facilitate focusing on observation metadata, all "result" items are serialised 
	 * as a measurement, although not all results are actually of that type.
	 */
	
	private static Validator m_validator = null;
	
	
	// Testing XML reading. This test case should be run first.
	@Test
	public void obs_10_ReadXml() throws Exception
	{
		final String filepath = getPathOfTestFile("Observation_typical.xml");
		
		// Processing XML data
		Observation testObject = new Observation(readFile(filepath));
		
		// Getting field values from the object
		TreeMap<String, Object> fieldValues = getFieldvalues(testObject);
		
		// Checking string values
		assertStringInFieldValueMap(fieldValues, "name", "FSF batch mass");
		assertStringInFieldValueMap(fieldValues, "description", "The mass of a batch a crane has received from FSF");
		
		// Checking URI values
		assertStringInFieldValueMap(fieldValues, "procedure", "cocop/copper/crane/loadmassmeasurement");
		assertStringInFieldValueMap(fieldValues, "observedProperty", "mass");
		assertStringInFieldValueMap(fieldValues, "featureOfInterest", "cocop/copper/fsf/batch");
		
		// Checking datetime values
		assertTimeInstant("2018-02-05T12:10:13.00Z", testObject.getPhenomenonTime());
		assertDateTimeInFieldValueMap(fieldValues, "resultTime", DateTime.parse("2018-02-05T12:31:53.00Z"));
		
		// Asserting good data quality
		assertTrue(testObject.getResultQuality().isGood());
	}
	
	@Test
	public void obs_11_ReadXml_MinimalXmlDoc() throws Exception
	{
		final String filepath = getPathOfTestFile("Observation_minimal.xml");

        // Processing XML data
		Observation testObject = new Observation(readFile(filepath));

        // Checking string values
        assertNull(testObject.getName());
        assertNull(testObject.getDescription());

        // Checking URI values
        assertTrue(testObject.getProcedure() == null || testObject.getProcedure().isEmpty());
        assertTrue(testObject.getObservedProperty() == null || testObject.getObservedProperty().isEmpty());
        assertTrue(testObject.getFeatureOfInterest() == null || testObject.getFeatureOfInterest().isEmpty());

        // Checking datetime values
        assertTimeInstant("2018-02-05T12:10:13.00Z", testObject.getPhenomenonTime());
        assertTimeInstant("2018-02-05T12:31:53.00Z", testObject.getResultTime());

        // Asserting good data quality
        assertTrue(testObject.getResultQuality().isGood());
	}
	
	@Test
	public void obs_12_ReadXml_ComplexFeature() throws Exception
	{
		final String filepath = getPathOfTestFile("Observation_complexfeature.xml");

        // Processing XML data
		Observation testObject = new Observation(readFile(filepath));
		
		// Just checking that parsing the document did not fail as a whole.
		// If there is time, the actual parsing of the "complex feature of interest"
		// can be implemented.
		
		// Assert procedure
		assertEquals("cocop/copper/crane/loadmassmeasurement", testObject.getProcedure());
		
		// Assuming null because the parsing of complex feature is not supported
		String feature = testObject.getFeatureOfInterest();
		assertTrue(feature == null || feature.length() == 0);
		
		// TODO: Implement the parsing of complex feature if there is time
	}
	
	@Test
	public void obs_20_CreateXml_DefaultValues() throws Exception
	{
		DateTime startTime = DateTime.now();
        Observation originalObj = new Observation(new Item_Measurement(null));

        // Serializing the XML document
 		byte[] xmlBytes = originalObj.toXmlBytes();
 		
 		// Validating XML output
 		validateXmlDoc(xmlBytes);
 		
 		// Parsing the document; exception expected if the doc is not well-formed (?)
 		Observation parsedObj = new Observation(xmlBytes);

        // Checking string values
        assertNull(parsedObj.getDescription());
        assertNull(parsedObj.getName());

        // Checking URI values
        assertEquals("", parsedObj.getFeatureOfInterest());
        assertEquals("", parsedObj.getObservedProperty());
        assertEquals("", parsedObj.getProcedure());

        // Checking result time by comparing to start time
        long timespan_result = parsedObj.getResultTime().getValue().toInstant().getMillis() - startTime.toInstant().getMillis();
        assertTrue(timespan_result < 200);
        
        // Checking phenomenon time by comparing to start time
        long timespan_phenomenonTime = parsedObj.getPhenomenonTime().getValue().toInstant().getMillis() - startTime.toInstant().getMillis();
        assertTrue(timespan_phenomenonTime < 200);

        // Asserting good data quality
        assertTrue(parsedObj.getResultQuality().isGood());
	}
	
	// Testing XML creation with (1) "good" data quality and (2) time instant as the phenomenon time.
	@Test
	public void obs_21_CreateXml() throws Exception
	{
		Observation originalObj = new Observation(new Item_Measurement(null));
		
		// Populating the object
		originalObj.setName("Some name");
		originalObj.setDescription("Some description");
		originalObj.setFeatureOfInterest("somefeature");
		originalObj.setObservedProperty("someproperty");
		DateTime phenoDt = DateTime.parse("2018-02-23T10:00:00Z").withZone(DateTimeZone.UTC);
		originalObj.setPhenomenonTime(new Item_TimeInstant(phenoDt));
		originalObj.setProcedure("someprocedure");
		DateTime resultDt = DateTime.parse("2018-02-23T10:00:00Z").withZone(DateTimeZone.UTC);
		originalObj.setResultTime(new Item_TimeInstant(resultDt));
		originalObj.setResultQuality(DataQuality.createGood());
		
		// Serializing the XML document
		byte[] xmlBytes = originalObj.toXmlBytes();
		
		// Validating XML output
		validateXmlDoc(xmlBytes);
		
		// Parsing the document; exception expected if the doc is not well-formed (?)
		Observation parsedObj = new Observation(xmlBytes);
		
		// Checking string values
		assertEquals("Some description", parsedObj.getDescription());
		assertEquals("Some name", parsedObj.getName());
		
		// Checking URI values
		assertEquals("somefeature", parsedObj.getFeatureOfInterest());
		assertEquals("someproperty", parsedObj.getObservedProperty());
		assertEquals("someprocedure", parsedObj.getProcedure());
		
		// Checking result time
		assertTimeInstant("2018-02-23T10:00:00Z", parsedObj.getResultTime());
		
		// Comparing phenomenon time
		assertTimeInstant("2018-02-23T10:00:00Z", parsedObj.getPhenomenonTime());
		
		// Asserting good data quality
		assertTrue(parsedObj.getResultQuality().isGood());
	}
	
	@Test
	public void obs_22_CreateXml_BadQuality() throws Exception
	{
		Observation originalObj = new Observation(new Item_Measurement(null));
		
		// Populating the object
		originalObj.setResultQuality(DataQuality.createBad());
		
		// Serializing the XML document
		byte[] xmlBytes = originalObj.toXmlBytes();
		
		// Validating XML output
		validateXmlDoc(xmlBytes);
		
		// Parsing the document; exception expected if the doc is not well-formed (?)
		Observation parsedObj = new Observation(xmlBytes);
		
		// Asserting bad data quality
		assertFalse(parsedObj.getResultQuality().isGood());
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
	
	private void assertStringInFieldValueMap(TreeMap<String, Object> fieldValues, String field, String expected)
	{
		assertTrue("Field not found: " + field, fieldValues.containsKey(field));
		
		String actual = (String)fieldValues.get(field);
		assertEquals("Field " + field, expected, actual);
	}
	
	private void assertTimeInstant(String expected, Item_TimeInstant actual)
	{
		DateTime expectedDt = DateTime.parse(expected).withZone(DateTimeZone.UTC); 
		DateTime actualDt = actual.getValue();
		
		// Always expecting UTC
		assertEquals(DateTimeZone.UTC, actualDt.getZone());
		
		DateTimeComparator comparator = DateTimeComparator.getInstance();
		assertEquals(0, comparator.compare(expectedDt, actualDt));
	}
	
	private void assertDateTimeInFieldValueMap(TreeMap<String, Object> fieldValues, String field, DateTime expected) throws Exception
	{
		assertTrue("Field not found: " + field, fieldValues.containsKey(field));
		
		Item_TimeInstant actual = (Item_TimeInstant)fieldValues.get(field);
		DateTime actualDt = actual.getValue();
		
		// Always expecting UTC
		assertEquals(DateTimeZone.UTC, actualDt.getZone());
		
		DateTimeComparator comparator = DateTimeComparator.getInstance();
		assertEquals("Field " + field, 0, comparator.compare(expected, actualDt));
	}
	
	private static TreeMap<String, Object> getFieldvalues(Observation testObject) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Observation.class).getPropertyDescriptors();
		TreeMap<String, Object> retval = new TreeMap<String, Object>();
		
		for (int a = 0; a < propertyDescriptors.length; ++a)
		{
			PropertyDescriptor pd = propertyDescriptors[a];
			
			retval.put(pd.getName(), pd.getReadMethod().invoke(testObject));
		}
		
		return retval;
	}
	
	private static byte[] readFile(String path) throws IOException 
	{
		return Files.readAllBytes(Paths.get(path));
	}
	
	private static String getPathOfTestFile(String filename)
	{
		return System.getProperty("user.dir") + "/../common/testfiles/" + filename;
	}
}
