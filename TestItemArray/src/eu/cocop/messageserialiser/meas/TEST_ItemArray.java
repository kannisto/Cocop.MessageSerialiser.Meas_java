//
// Please make sure to read and understand the files README.md and LICENSE.txt.
// 
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
//
// Author: Petri Kannisto, Tampere University, Finland
// File created: 7/2019
// Last modified: 4/2020

package eu.cocop.messageserialiser.meas;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

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

import net.opengis.om._2.OMObservationType;
import net.opengis.swe._2.DataArrayType;

public class TEST_ItemArray
{
	/*
	 * This test focuses on the Item_Array class and its XML serialisation.
	 * 
	 * An appropriate XML validation is easiest with the enclosing Observation class.
	 * Due to the serialisation complexity of the observation class, it is included as 
	 * such instead of creating a redundant stub class.
	 */
	
	private static Validator m_validator = null;
	
	
	// ### ArrayColumn only ###
	
	@Test
	public void arrayColumn_Create_RecogniseSupportedTypes()
	{
		// Testing if the supported data types are recognised for columns
        
        // 1) Expecting success
        new Item_Array.ArrayColumn("Foo", Double.class, "m");
        new Item_Array.ArrayColumn("Foo", Double.class);
        new Item_Array.ArrayColumn("Foo", Boolean.class);
        new Item_Array.ArrayColumn("Foo", Item_TimeInstant.class);
        new Item_Array.ArrayColumn("Foo", Long.class);
        new Item_Array.ArrayColumn("Foo", String.class);

        // 2) Expecting failure
        ExpectExceptionFromType(Byte.class);
        ExpectExceptionFromType(Character.class);
        ExpectExceptionFromType(DateTime.class);
        ExpectExceptionFromType(Float.class);
        ExpectExceptionFromType(Integer.class);
        ExpectExceptionFromType(Object.class);
        ExpectExceptionFromType(Short.class);
        // Java has neither uint nor ulong -> cannot test with them! :)
	}
	
	private void ExpectExceptionFromType(Class<?> columnType)
    {
		assertIllegalArgumentException(() ->
		{
			new Item_Array.ArrayColumn("Foo", columnType);
		},
		"Unsupported column type");
    }
	
	@Test
	public void arrayColumn_Create_UnitConflict()
	{
		// Only a "measure" (or double) column can take a unit of measure.

        // 1) Expecting success with the double type (no exception expected)
        new Item_Array.ArrayColumn("Foo", Double.class, "m");

        // 2) Checking that the others fail
        ExpectExceptionFromUnit(Boolean.class);
        ExpectExceptionFromUnit(Item_TimeInstant.class);
        ExpectExceptionFromUnit(Long.class);
        ExpectExceptionFromUnit(String.class);
	}
	
	private void ExpectExceptionFromUnit(Class<?> columnType)
    {
		assertIllegalArgumentException(() ->
		{
			new Item_Array.ArrayColumn("Foo", columnType, "m");
		},
		"Only measurements (doubles) can have a unit");
    }
	
	@Test
	public void arrayColumn_Create_InvalidName()
	{
		// 1) Name is empty
		ExpectExceptionFromName(null);
		ExpectExceptionFromName("");
		
		// 2) Name contains spaces
		ExpectExceptionFromName("f f");
		ExpectExceptionFromName("f\nf");
		
		// 3) Name contains a colon
		ExpectExceptionFromName(":");
		ExpectExceptionFromName(":sf");
		ExpectExceptionFromName("sf:");
		ExpectExceptionFromName("sf:sf");
	}
	
	private void ExpectExceptionFromName(String name)
	{
		assertIllegalArgumentException(() ->
		{
			new Item_Array.ArrayColumn(name, Double.class);
		},
		"Column name is mandatory and must be valid NCName");
	}
	
	
	// ### Array and ArrayColumn ###
	
	@Test
	public void array_Read_NoRows() throws Exception
	{
		// Testing if reading succeeds when the document has no rows
		
		String filepath = getPathOfTestFile("Item_Array_NoRows.xml");
		DataArrayType resultEl = (DataArrayType)parseRawResult(filepath);
		Item_Array testObject = new Item_Array(resultEl);
		
		// Asserting counts
        assertEquals(2, testObject.getColumns().size());
        assertEquals(0, testObject.getRowCount());
	}
	
	@Test
	public void array_Read_NoColumns() throws Exception
	{
		// Testing if reading succeeds when the document has no columns
		
		String filepath = getPathOfTestFile("Item_Array_NoColumns.xml");
		DataArrayType resultEl = (DataArrayType)parseRawResult(filepath);
		Item_Array testObject = new Item_Array(resultEl);
		
		// Asserting counts
        assertEquals(0, testObject.getColumns().size());
        assertEquals(2, testObject.getRowCount());
	}
	
	@Test
	public void array_Read() throws Exception
	{
		String filepath = getPathOfTestFile("Item_Array.xml");
		DataArrayType resultEl = (DataArrayType)parseRawResult(filepath);
		Item_Array testObject = new Item_Array(resultEl);
		
		// Asserting counts
        assertEquals(6, testObject.getColumns().size());
        assertEquals(3, testObject.getRowCount());
        
        // Asserting column types
        assertEquals("java.lang.Boolean", getColumnType(testObject, 0));
        assertEquals("eu.cocop.messageserialiser.meas.Item_TimeInstant", getColumnType(testObject, 1));
        assertEquals("java.lang.Long", getColumnType(testObject, 2));
        assertEquals("java.lang.Double", getColumnType(testObject, 3));
        assertEquals("java.lang.String", getColumnType(testObject, 4));
        // Column 5 is of type "category range", which is unsupported and presented as strings.
        assertEquals("java.lang.String", getColumnType(testObject, 5));

        // Asserting column names
        assertEquals("BooleanCol", getColumnName(testObject, 0));
        assertEquals("TimeCol", getColumnName(testObject, 1));
        assertEquals("CountCol", getColumnName(testObject, 2));
        assertEquals("MeasurementCol", getColumnName(testObject, 3));
        assertEquals("TextCol", getColumnName(testObject, 4));
        assertEquals("CategoryRangeCol", getColumnName(testObject, 5));

        // Asserting the "data type supported" flag
        assertTrue(getDataTypeSupported(testObject, 0));
        assertTrue(getDataTypeSupported(testObject, 1));
        assertTrue(getDataTypeSupported(testObject, 2));
        assertTrue(getDataTypeSupported(testObject, 3));
        assertTrue(getDataTypeSupported(testObject, 4));
        assertFalse(getDataTypeSupported(testObject, 5));
        
        // Asserting boolean values
        assertTrue((boolean)testObject.get(0)[0]);
        assertFalse((boolean)testObject.get(1)[0]);

        // Asserting DateTime values
        assertDateTime(getUtcTime("2018-02-06T09:58:44.00Z"), (Item_TimeInstant)testObject.get(0)[1]);
        assertDateTime(getUtcTime("2018-02-06T09:58:45.00Z"), (Item_TimeInstant)testObject.get(1)[1]);
        
        // Asserting long values
        assertEquals(3, (long)testObject.get(0)[2]);
        assertEquals(-5, (long)testObject.get(1)[2]);

        // Asserting double values
        assertEquals(-0.12, (double)testObject.get(0)[3], 0.00001);
        assertEquals(14, (double)testObject.get(1)[3], 0.00001);

        // Asserting unit of measure
        assertEquals("cm", testObject.getColumns().get(3).getUnitOfMeasure());

        // Asserting string values
        assertEquals("abc", testObject.get(0)[4]);
        
        // Asserting category range values (unsupported data types are presented as string)
        assertEquals("a o", testObject.get(0)[5]);

        // Asserting empty (null) values
        assertNull(testObject.get(1)[4]);
        assertNull(testObject.get(2)[0]);
        assertNull(testObject.get(2)[1]);
        assertNull(testObject.get(2)[2]);
        assertNull(testObject.get(2)[3]);
        assertNull(testObject.get(2)[4]);
        assertNull(testObject.get(2)[5]);
	}
	
	private boolean getDataTypeSupported(Item_Array testObject, int index)
	{
		return testObject.getColumns().get(index).getDataTypeSupported();
	}
	
	private String getColumnType(Item_Array testObject, int index)
	{
		return testObject.getColumns().get(index).getDataType().getCanonicalName();
	}
	
	private String getColumnName(Item_Array testObject, int index)
	{
		return testObject.getColumns().get(index).getName();
	}
	
	@Test
	public void array_Read_LabelAndDescription() throws Exception
	{
		// Testing the reading of label and description of columns
		
		String filepath = getPathOfTestFile("Item_Array_LabelAndDesc.xml");
		
		DataArrayType resultEl = (DataArrayType)parseRawResult(filepath);
        Item_Array testObject = new Item_Array(resultEl);
		
        // Testing description and label for each supported column type
        assertEquals("Boolean col", testObject.getColumns().get(0).getLabel());
        assertEquals("Time col", testObject.getColumns().get(1).getLabel());
        assertEquals("Count col", testObject.getColumns().get(2).getLabel());
        assertEquals("Measurement col", testObject.getColumns().get(3).getLabel());
        assertEquals("Text col", testObject.getColumns().get(4).getLabel());
        assertEquals("Boolean desc", testObject.getColumns().get(0).getDescription());
        assertEquals("Time desc", testObject.getColumns().get(1).getDescription());
        assertEquals("Count desc", testObject.getColumns().get(2).getDescription());
        assertEquals("Measurement desc", testObject.getColumns().get(3).getDescription());
        assertEquals("Text desc", testObject.getColumns().get(4).getDescription());
	}
	
	@Test
	public void array_Read_CellParsingFails()
	{
		// Testing the failure of parsing a cell value
		
		assertInvalidMessageException(() ->
		{
			String filepath = getPathOfTestFile("Neg_Item_Array_CellParsingFails.xml");

            // Getting result element
			DataArrayType resultEl = (DataArrayType)parseRawResult(filepath);
            new Item_Array(resultEl);
		},
		"Failed to parse value in array");
	}
	
	@Test
	public void array_Read_CellCountConflict()
	{
		// Testing a conflict in the cell count of a row

		assertInvalidMessageException(() ->
		{
			String filepath = getPathOfTestFile("Neg_Item_Array_CellCountConflict.xml");

            // Getting result element
			DataArrayType resultEl = (DataArrayType)parseRawResult(filepath);
            new Item_Array(resultEl);
		},
		"Inconsistent cell count in rows");
	}
	
	@Test
	public void array_Create_NoColumns() throws Exception
	{
		// Testing the creation of an array without columns
        
		Item_Array arrayItem = new Item_Array(new ArrayList<Item_Array.ArrayColumn>());

        // Adding 3 empty rows
        arrayItem.add();
        arrayItem.add();
        arrayItem.add();

        Item_Array arrayItemIn = (Item_Array)serialiseAndReadResultObj(arrayItem, XmlHelper.TYPEURI_COMPLEX);

        assertEquals(0, arrayItemIn.getColumns().size());
        assertEquals(3, arrayItemIn.getRowCount());
	}
	
	@Test
	public void array_Create_NoRows() throws Exception
	{
		// Testing the creation of an array without rows

		ArrayList<Item_Array.ArrayColumn> columns = new ArrayList<>();
        columns.add(new Item_Array.ArrayColumn("BoolCol", Boolean.class));
        columns.add(new Item_Array.ArrayColumn("CountCol", Long.class));
        Item_Array arrayItem = new Item_Array(columns);
        
        Item_Array arrayItemIn = (Item_Array)serialiseAndReadResultObj(arrayItem, XmlHelper.TYPEURI_COMPLEX);

        assertEquals(2, arrayItemIn.getColumns().size());
        assertEquals(0, arrayItemIn.getRowCount());
	}
	
	@Test
	public void array_Create_DataTypeConflictInCell()
	{
		// Testing an attempt to set a conflicting data type to a cell
		
		ArrayList<Item_Array.ArrayColumn> columns = new ArrayList<>();
		columns.add(new Item_Array.ArrayColumn("MeasCol1", Double.class));
		columns.add(new Item_Array.ArrayColumn("MeasCol2", Double.class));
		
		assertIllegalArgumentException(() ->
		{
            Item_Array myArray = new Item_Array(columns);
            myArray.add(4.5, (long)2);
		},
		"Type mismatch");
	}
	
	@Test
	public void array_Create_CellCountConflict() throws Exception
	{
		// Testing an attempt to add a conflicting count of cells in a row
		
		ArrayList<Item_Array.ArrayColumn> columns = new ArrayList<>();
		columns.add(new Item_Array.ArrayColumn("BoolCol", Boolean.class));
		columns.add(new Item_Array.ArrayColumn("CountCol", Long.class));
		Item_Array myArray = new Item_Array(columns);
		
		assertIllegalArgumentException(() ->
		{
            myArray.add(true, (long)4, (long)2);
		},
		"Received cell count does not match");
	}
	
	@Test
	public void array_Create() throws Exception
	{
		// Testing the basic creation of an array with all data types included.
        // - Testing label and description too
        // - Testing both boolean values
        // - Testing empty values (null, empty string)
		
		Item_Array arrayItemIn = createAndDeserialiseArray();

		DateTime dateTime1 = getUtcTime("2019-01-11T00:11:19Z");
		DateTime dateTime2 = getUtcTime("2019-01-12T00:11:19Z");
		
		// Asserting counts
        assertEquals(6, arrayItemIn.getColumns().size());
        assertEquals(4, arrayItemIn.getRowCount());

        // Asserting row 1
        assertTrue((boolean)arrayItemIn.get(0)[0]);
        assertDateTime(dateTime1, (Item_TimeInstant)arrayItemIn.get(0)[1]);
        assertEquals(-4.2, arrayItemIn.get(0)[2]);
        assertEquals((long)68, arrayItemIn.get(0)[3]);
        assertEquals("string 1", arrayItemIn.get(0)[4]);
        assertEquals("emptyname 1", arrayItemIn.get(0)[5]);

        // Asserting row 2
        assertFalse((boolean)arrayItemIn.get(1)[0]);
        assertDateTime(dateTime2, (Item_TimeInstant)arrayItemIn.get(1)[1]);
        assertEquals(0.0, (double)arrayItemIn.get(1)[2], 0.00001);
        assertEquals((long)0, arrayItemIn.get(1)[3]);
        assertEquals("string 2", arrayItemIn.get(1)[4]);
        assertEquals("emptyname 2", arrayItemIn.get(1)[5]);

        // Asserting row 3 (nulls in input)
        assertNull(arrayItemIn.get(2)[0]);
        assertNull(arrayItemIn.get(2)[1]);
        assertNull(arrayItemIn.get(2)[2]);
        assertNull(arrayItemIn.get(2)[3]);
        assertNull(arrayItemIn.get(2)[4]);
        assertNull(arrayItemIn.get(2)[5]);

        // Asserting row 4 (empty strings in input)
        assertNull(arrayItemIn.get(3)[0]);
        assertNull(arrayItemIn.get(3)[1]);
        assertNull(arrayItemIn.get(3)[2]);
        assertNull(arrayItemIn.get(3)[3]);
        assertNull(arrayItemIn.get(3)[4]);
        assertNull(arrayItemIn.get(3)[5]);

        // Asserting names
        assertEquals("BoolCol", arrayItemIn.getColumns().get(0).getName());
        assertEquals("TimeInstantCol", arrayItemIn.getColumns().get(1).getName());
        assertEquals("DoubleCol", arrayItemIn.getColumns().get(2).getName());
        assertEquals("LongCol", arrayItemIn.getColumns().get(3).getName());
        assertEquals("StringCol", arrayItemIn.getColumns().get(4).getName());
        assertEquals("AnotherCol", arrayItemIn.getColumns().get(5).getName());
        
        // Asserting units of measure
        String uomIn0 = arrayItemIn.getColumns().get(0).getUnitOfMeasure();
        String uomIn2 = arrayItemIn.getColumns().get(2).getUnitOfMeasure();
        assertTrue(uomIn0 == null || uomIn0.isEmpty());
        assertEquals("t/h", uomIn2);
        
        // Asserting labels and descriptions
        assertEquals("Label for bool", arrayItemIn.getColumns().get(0).getLabel());
        assertEquals("Label for TimeInstant", arrayItemIn.getColumns().get(1).getLabel());
        assertEquals("Label for double", arrayItemIn.getColumns().get(2).getLabel());
        assertEquals("Label for long", arrayItemIn.getColumns().get(3).getLabel());
        assertEquals("Label for string", arrayItemIn.getColumns().get(4).getLabel());
        assertNull(arrayItemIn.getColumns().get(5).getLabel());
        assertEquals("Desc for bool", arrayItemIn.getColumns().get(0).getDescription());
        assertEquals("Desc for TimeInstant", arrayItemIn.getColumns().get(1).getDescription());
        assertEquals("Desc for double", arrayItemIn.getColumns().get(2).getDescription());
        assertEquals("Desc for long", arrayItemIn.getColumns().get(3).getDescription());
        assertEquals("Desc for string", arrayItemIn.getColumns().get(4).getDescription());
        assertNull(arrayItemIn.getColumns().get(5).getDescription());
	}
	
	private Item_Array createAndDeserialiseArray() throws Exception
	{
		// This is in a separate method to avoid asserting the wrong object in test.
		
		Item_Array.ArrayColumn boolCol = createColumn(
				"BoolCol",
				Boolean.class,
				"Label for bool",
				"Desc for bool"
				);
		Item_Array.ArrayColumn dateTimeCol = createColumn(
				"TimeInstantCol",
				Item_TimeInstant.class,
				"Label for TimeInstant",
				"Desc for TimeInstant"
				);
		Item_Array.ArrayColumn doubleCol = new Item_Array.ArrayColumn("DoubleCol", Double.class, "t/h");
		doubleCol.setLabel("Label for double");
		doubleCol.setDescription("Desc for double");
		Item_Array.ArrayColumn longCol = createColumn(
				"LongCol",
				Long.class,
				"Label for long",
				"Desc for long"
				);
		Item_Array.ArrayColumn stringCol = createColumn(
				"StringCol",
				String.class,
				"Label for string",
				"Desc for string"
				);
		Item_Array.ArrayColumn stringColNoName = createColumn(
				"AnotherCol",
				String.class,
				null,
				null
				);
		
		ArrayList<Item_Array.ArrayColumn> columns = new ArrayList<>();
		columns.add(boolCol);
		columns.add(dateTimeCol);
		columns.add(doubleCol);
		columns.add(longCol);
		columns.add(stringCol);
		columns.add(stringColNoName);
		
		DateTime dateTime1 = getUtcTime("2019-01-11T00:11:19Z");
		DateTime dateTime2 = getUtcTime("2019-01-12T00:11:19Z");
		
		Item_Array arrayItem = new Item_Array(columns);
		arrayItem.add(true, new Item_TimeInstant(dateTime1), -4.2, (long)68, "string 1", "emptyname 1");
		arrayItem.add(false, new Item_TimeInstant(dateTime2), 0.0, (long)0, "string 2", "emptyname 2");
		arrayItem.add(null, null, null, null, null, null);
		arrayItem.add("", "", "", "", "", "");

		// Serialising and deserialising
        return (Item_Array)serialiseAndReadResultObj(arrayItem, XmlHelper.TYPEURI_COMPLEX);
	}
	
	private Item_Array.ArrayColumn createColumn(String name, Class<?> type, String label, String desc)
	{
		Item_Array.ArrayColumn column = new Item_Array.ArrayColumn(name, type);
		column.setLabel(label);
		column.setDescription(desc);
		return column;
	}
	
	
	// ### Helper methods ###
	
	interface ITestInterface
	{
		void invoke() throws Exception;
	}
	
	private void assertIllegalArgumentException(ITestInterface testFcn, String expectedErrorStart)
	{
		try
		{
			testFcn.invoke();
			fail("Expected exception");
		}
		catch (IllegalArgumentException e)
		{
			String msg = e.getMessage();
			assertTrue("Unexpected message '" + msg + "'", msg.startsWith(expectedErrorStart));
		}
		catch (Exception e)
		{
			fail("Unexpected exception " + e.getClass().getName());
		}
	}
	
	private void assertInvalidMessageException(ITestInterface testFcn, String expectedErrorStart)
	{
		try
		{
			testFcn.invoke();
			fail("Expected exception");
		}
		catch (InvalidMessageException e)
		{
			String msg = e.getMessage();
			assertTrue("Unexpected message '" + msg + "'", msg.startsWith(expectedErrorStart));
		}
		catch (Exception e)
		{
			fail("Unexpected exception " + e.getClass().getName());
		}
	}
	
	
	// ### Helper methods ###
	
	private Object parseRawResult(String filepath) throws Exception
	{
		// Get JAXB context
		JAXBContext jaxbContext = XmlHelper.getJaxbContext();
		
		// Do JAXB unmarshalling
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		File fileToParse = new File(filepath);
		@SuppressWarnings("unchecked")
		JAXBElement<OMObservationType> observationJaxb = (JAXBElement<OMObservationType>)unmarshaller.unmarshal(fileToParse);
		
		return observationJaxb.getValue().getResult();
	}
	
	private Item serialiseAndReadResultObj(Item testObject, String obsTypeUri) throws Exception
	{
		// Using Observation class in serialisation.
		// Otherwise, a lot of redundant code would be required here to enable XML validation.
		Observation observation = new Observation(testObject);
		byte[] xmlBytes = observation.toXmlBytes();
		
		//String xmlString = new String(xmlBytes);
		
		// Validating the document
		validateXmlDoc(xmlBytes);
		
		// Get JAXB context
		JAXBContext jaxbContext = XmlHelper.getJaxbContext();
		
		// Do JAXB unmarshalling
		ByteArrayInputStream reader = new ByteArrayInputStream(xmlBytes);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		@SuppressWarnings("unchecked")
		JAXBElement<OMObservationType> observationJaxb = (JAXBElement<OMObservationType>)unmarshaller.unmarshal(reader);
		reader.close();
		
		// Testing that result mapping after URI works as expected
		Object resultObj = observationJaxb.getValue().getResult();
		return ResultTypeManager.buildResultFromXml(obsTypeUri, resultObj);
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
	
	private void assertDateTime(DateTime expected, Item_TimeInstant actual)
	{
		// Always expecting UTC
		assertEquals(DateTimeZone.UTC, actual.getValue().getZone());
		
		assertEquals(expected.toString(), actual.getValue().toString());
	}
	
	private String getPathOfTestFile(String filename)
	{
		return System.getProperty("user.dir") + "/../common/testfiles/" + filename;
	}
	
	private DateTime getUtcTime(String xsdDateTime)
	{
		return DateTime.parse(xsdDateTime).withZone(DateTimeZone.UTC);
	}
}
