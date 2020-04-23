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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigInteger;
import java.time.ZonedDateTime;
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

import net.opengis.gml._3.MeasureType;
import net.opengis.gml._3.ReferenceType;
import net.opengis.gml._3.TimeInstantPropertyType;
import net.opengis.gml._3.TimePeriodPropertyType;
import net.opengis.om._2.OMObservationType;
import net.opengis.swe._2.DataRecordPropertyType;
import net.opengis.tsml._1.TimeseriesDomainRangeType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TEST_Items
{
	/*
	 * This test has its focus on Item_* classes and their XML serialisation.
	 * 
	 * An appropriate XML validation is easiest with the enclosing Observation class.
	 * Due to the serialisation complexity of the observation class, it is included as 
	 * such instead of creating a redundant stub class.
	 * 
	 * Each item type has its own read and serialisation test.
	 */
	
	private static Validator m_validator = null;
	
	
	@Test
	public void boolean_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_Boolean.xml");
		
		// Getting result element
		boolean resultEl = (boolean)parseRawResult(filepath);
		
		Item_Boolean testObject = new Item_Boolean(resultEl);
		
		// Asserting
		assertFalse(testObject.getValue());
	}
	
	@Test
	public void boolean_create() throws Exception
	{
		Item_Boolean testObject1 = new Item_Boolean(true);
		Item_Boolean testObject2 = new Item_Boolean(false);
		
		// Serialising and deserialising the test object
		Item_Boolean testObjectIn1 = (Item_Boolean)serialiseAndReadResultObj(testObject1, XmlHelper.TYPEURI_TRUTH);
		Item_Boolean testObjectIn2 = (Item_Boolean)serialiseAndReadResultObj(testObject2, XmlHelper.TYPEURI_TRUTH);
		
		// Asserting
		assertTrue(testObjectIn1.getValue());
		assertFalse(testObjectIn2.getValue());
	}
	
	@Test
	public void category_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_Category.xml");
		
		// Getting result element
		ReferenceType resultEl = (ReferenceType)parseRawResult(filepath);
		
		Item_Category testObject = new Item_Category(resultEl);
		
		// Checking results
		assertEquals("some<&<<ctg", testObject.getValue());
	}
	
	@Test
	public void category_create() throws Exception
	{
		String categoryRef = "http://somecategory><sf>";
		Item_Category testObject = new Item_Category(categoryRef);
		
		// Serialising and deserialising the test object
		Item_Category testObjectIn = (Item_Category)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_CATEGORY);
		
		// Checking results
		assertEquals(categoryRef, testObjectIn.getValue());
	}
	
	@Test
	public void count_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_Count.xml");
		
		// Getting result element
		BigInteger resultEl = (BigInteger)parseRawResult(filepath);
		
		Item_Count testObject = new Item_Count(resultEl);
		
		// Asserting
		assertEquals(20, testObject.getValue());
	}
	
	@Test
	public void count_create() throws Exception
	{
		Item_Count testObject = new Item_Count(313);
		
		// Serialising and deserialising the test object
		Item_Count testObjectIn = (Item_Count)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_COUNT);
		
		// Asserting
		assertEquals(313, testObjectIn.getValue());
	}
	
	@Test
	public void measurement_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_Measurement.xml");
		
		// Getting result element
		MeasureType resultEl = (MeasureType)parseRawResult(filepath);
		
		Item_Measurement testObject = new Item_Measurement(resultEl);
		
		// Checking unit of measure
		String unitOfMeas = testObject.getUnitOfMeasure();
		assertEquals("unitOfMeas", "t", unitOfMeas);
		
		// Checking measurement value
		double measValue = testObject.getValue();
		assertEquals("Measurement value", 20.3, measValue, 0.0001);
	}
	
	@Test
	public void measurement_create() throws Exception
	{
		Item_Measurement testObject = new Item_Measurement("t", 22.7);
		
		// Serialising and deserialising the test object
		Item_Measurement testObjectIn = (Item_Measurement)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_MEASUREMENT);
		
		// Asserting
		assertEquals("Unit of measure", testObject.getUnitOfMeasure(), testObjectIn.getUnitOfMeasure());
		assertEquals("Value", testObject.getValue(), testObjectIn.getValue(), 0.0001);
	}
	
	@Test
	public void text_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_Text.xml");
		
		// Getting result element
		String resultEl = (String)parseRawResult(filepath);
		
		Item_Text testObject = new Item_Text(resultEl);
		
		assertEquals("This is my string", testObject.getValue());
	}
	
	@Test
	public void text_create() throws Exception
	{
		Item_Text testObject = new Item_Text("my-text");
		
		// Serialising and deserialising the test object
		Item_Text testObjectIn = (Item_Text)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_TEXT);
		
		// Asserting
		assertEquals("my-text", testObjectIn.getValue());
	}
	
	@Test
	public void timeRange_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_TimeRange.xml");
		
		// Getting result element
		TimePeriodPropertyType resultEl = (TimePeriodPropertyType)parseRawResult(filepath);
		
		Item_TimeRange testObject = new Item_TimeRange(resultEl);
		
		// Asserting
		assertTimeInstantExplUtc(getUtcTime("2020-02-21T12:43:00Z"), testObject.getStart());
		assertTimeInstantExplUtc(getUtcTime("2020-02-21T12:44:00Z"), testObject.getEnd());
	}
	
	@Test
	public void timeRange_create() throws Exception
	{
		DateTime stt = getUtcTime("2020-01-20T10:00:00Z");
		DateTime end = stt.plusSeconds(13);
		Item_TimeRange testObject = new Item_TimeRange(new Item_TimeInstant(stt), new Item_TimeInstant(end));
		
		// Serialising and deserialising the test object
		Item_TimeRange testObjectIn = (Item_TimeRange)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_TEMPORAL);
		
		// Asserting
		assertTimeInstantExplUtc(stt, testObjectIn.getStart());
		assertTimeInstantExplUtc(end, testObjectIn.getEnd());
	}
	
	@Test
	public void timeInstant_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_TimeInstant.xml");
		
		// Getting result element
		TimeInstantPropertyType resultEl = (TimeInstantPropertyType)parseRawResult(filepath);
		
		Item_TimeInstant testObject = new Item_TimeInstant(resultEl.getTimeInstant());
		
		// Asserting
		assertTimeInstantExplUtc(getUtcTime("2020-02-21T12:44:00Z"), testObject);
	}
	
	@Test
	public void timeInstant_create() throws Exception
	{
		DateTime dateTime = getUtcTime("2020-02-24T10:00:00Z").withZone(DateTimeZone.UTC);
		Item_TimeInstant testObject = new Item_TimeInstant(dateTime);
		
		// Serialising and deserialising the test object
		Item_TimeInstant testObjectIn = (Item_TimeInstant)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_TEMPORAL);
		
		// Asserting
		assertTimeInstantExplUtc(dateTime, testObjectIn);
	}
	
	@Test
	public void timeInstant_createNotUtc() throws Exception
	{
		// Expecting an exception if the DateTime object is not in UTC
		
		assertIllegalDateTimeException(() ->
		{
			DateTime dt = DateTime.now(); // This has a local time zone
			new Item_TimeInstant(dt);
		},
		"DateTime must have UTC as time zone");
	}
	
	@Test
	public void timeInstant_parseXsdDateTime() throws Exception
	{
		// Testing the parsing of a string rather than processing XML nodes
		
		DateTime expectedUtc = getUtcTime("2019-07-05T08:30:00Z");
		
		// Expecting that when a timestamp has no zone specified, Joda time assumes the local time zone
		Integer currentOffset  = ZonedDateTime.now().getOffset().getTotalSeconds();
		DateTime expectedNoZone = expectedUtc.minusSeconds(currentOffset);
		
		// UTC
		Item_TimeInstant parsedExplicitUtc = new Item_TimeInstant("2019-07-05T08:30:00Z");
		assertTimeInstantExplUtc(expectedUtc, parsedExplicitUtc);
		
		// Same as above but originally not UTC. Expecting a conversion.
		Item_TimeInstant parsedConvertedToUtc = new Item_TimeInstant("2019-07-05T11:30:00+03:00");
		assertTimeInstantExplUtc(expectedUtc, parsedConvertedToUtc);
		
		// No time zone specified
		Item_TimeInstant dtNoZoneInfo = new Item_TimeInstant("2019-07-05T08:30:00"); // No time zone specified
		assertTimeInstant(expectedNoZone, false, false, dtNoZoneInfo);
		
		// Expecting errors
		assertIllegalArgumentException(() ->
		{
			new Item_TimeInstant("2019-07-05S11:30:00+03:00");
		}
		, "Failed to parse DateTime");
		assertIllegalArgumentException(() ->
		{
			new Item_TimeInstant("");
		}
		, "Failed to parse DateTime");
		assertIllegalArgumentException(() ->
		{
			new Item_TimeInstant("  ");
		}
		, "Failed to parse DateTime");
		assertIllegalArgumentException(() ->
		{
			new Item_TimeInstant(" T ");
		}
		, "Failed to parse DateTime");
	}
	
	@Test
	public void timeInstant_toXsdDateTime() throws Exception
	{
		// Testing serialisation to XML Schema DateTime.
		
		DateTime dt = getUtcTime("2019-07-05T11:39:02+03:00");
		Item_TimeInstant instant = new Item_TimeInstant(dt);
		
		assertEquals("2019-07-05T08:39:02.000Z", instant.toXsdDateTime());
	}
	
	@Test
	public void dataRecord_readEmpty() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_DataRecord_empty.xml");
		
		// Getting result element
		DataRecordPropertyType resultEl = (DataRecordPropertyType)parseRawResult(filepath);
		
		Item_DataRecord testObject = new Item_DataRecord(resultEl);
		
		assertEquals(0, testObject.getItemNames().size());
	}
	
	@Test
	public void dataRecord_read() throws Exception
	{
		final String filepath = getPathOfTestFile("Item_DataRecord.xml");
		
		// Getting result element
		DataRecordPropertyType resultEl = (DataRecordPropertyType)parseRawResult(filepath);
		
		Item_DataRecord testObject = new Item_DataRecord(resultEl);
		
		// Getting fields
		Item_Array fieldArray = (Item_Array)testObject.getItem("SomeArray");
		Item_Boolean fieldBool1 = (Item_Boolean)testObject.getItem("SomeBoolean1");
		Item_Boolean fieldBool2 = (Item_Boolean)testObject.getItem("SomeBoolean2");
		Item_Category fieldCategory1 = (Item_Category)testObject.getItem("SomeCategory1");
		Item_Count fieldCount1 = (Item_Count)testObject.getItem("SomeCount1");
		Item_Measurement fieldMeas1 = (Item_Measurement)testObject.getItem("SomeMeasurement1");
		Item_Text fieldText1 =(Item_Text)testObject.getItem("SomeText1");
		Item_TimeInstant fieldTimeInstant1 = (Item_TimeInstant)testObject.getItem("SomeTimeInstant1");
		Item_TimeRange fieldTimePeriod1 = (Item_TimeRange)testObject.getItem("SomeTimePeriod1");
		
		// Array. Not asserting thoroughly because Item_Array has its own tests.
		assertEquals(2, fieldArray.getColumns().size());
		assertEquals("MeasurementCol", fieldArray.getColumns().get(0).getName());
		assertEquals(3, fieldArray.getRowCount());
		assertEquals(-0.053, (double)fieldArray.get(0)[0], 0.001);
		assertEquals("Hello 3", fieldArray.get(2)[1]);
		
		// Boolean (two values in case of a default value that would match the assertion)
		assertFalse(fieldBool1.getValue());
		assertTrue(fieldBool2.getValue());
		
		// Category
		assertEquals("process_step_a", fieldCategory1.getValue());
		
		// Count
		assertEquals(442, fieldCount1.getValue());
		
		// Measurement
		assertEquals("kg", fieldMeas1.getUnitOfMeasure());
		assertEquals(5.6, fieldMeas1.getValue(), 0.0001);
		
		// Text
		assertEquals("Some text", fieldText1.getValue());
		
		// TimeInstant
		assertTimeInstantExplUtc(getUtcTime("2018-03-02T10:17:00Z"), fieldTimeInstant1);
		
		// TimePeriod
		assertTimeInstantExplUtc(getUtcTime("2018-03-09T15:17:00Z"), fieldTimePeriod1.getStart());
		assertTimeInstantExplUtc(getUtcTime("2018-03-09T15:18:09Z"), fieldTimePeriod1.getEnd());
		
		// Nested DataRecord
		Item_DataRecord nestedDataRecord = (Item_DataRecord)testObject.getItem("NestedDataRecord");
		Item_Measurement nestedMeasurement = (Item_Measurement)nestedDataRecord.getItem("NestedMeasurement");
		assertEquals("m", nestedMeasurement.getUnitOfMeasure());
		assertEquals(22.44, nestedMeasurement.getValue(), 0.0001);
		
		// Asserting data quality
		assertDataQualityForItem(testObject, "SomeBoolean1", true);
		assertDataQualityForItem(testObject, "SomeCategory1", true);
		assertDataQualityForItem(testObject, "SomeCount1", true);
		assertDataQualityForItem(testObject, "SomeMeasurement1", true);
		assertDataQualityForItem(testObject, "SomeText1", true);
		assertDataQualityForItem(testObject, "SomeTimeInstant1", true);
		assertDataQualityForItem(testObject, "SomeTimePeriod1", true);
		assertDataQualityForItem(testObject, "SomeBoolean2", false);
		assertDataQualityForItem(testObject, "SomeCategory2", false);
		assertDataQualityForItem(testObject, "SomeCount2", false);
		assertDataQualityForItem(testObject, "SomeMeasurement2", false);
		assertDataQualityForItem(testObject, "SomeText2", false);
		assertDataQualityForItem(testObject, "SomeTimeInstant2", false);
		assertDataQualityForItem(testObject, "SomeTimePeriod2", false);
	}
	
	@Test
	public void dataRecord_createEmpty() throws Exception
	{
		// Serialising and deserialising the test object
		Item_DataRecord testObjectIn = (Item_DataRecord)serialiseAndReadResultObj(new Item_DataRecord(), XmlHelper.TYPEURI_COMPLEX);
		
		assertEquals(0, testObjectIn.getItemNames().size());
	}
	
	@Test
	public void dataRecord_create() throws Exception
	{
		// TODO: Also implement and test time series and range types (if there is time)
		
		// Getting the test object
		Item_DataRecord testObjectIn = createAndSerialiseDataRecordForTest();
		
		
		// Asserting
		
		assertTrue(testObjectIn.getItemNames().contains("FBool1"));
		assertTrue(testObjectIn.getItemNames().contains("FBool2"));
		assertTrue(testObjectIn.getItemNames().contains("FCategory"));
		assertTrue(testObjectIn.getItemNames().contains("FCount"));
		assertTrue(testObjectIn.getItemNames().contains("FMeasurement"));
		assertTrue(testObjectIn.getItemNames().contains("FText"));
		assertTrue(testObjectIn.getItemNames().contains("FTimeInstant"));
		assertTrue(testObjectIn.getItemNames().contains("FNestedRecord"));
		
		// Array
		Item_Array fieldArray = (Item_Array)testObjectIn.getItem("FArray");
		assertEquals(1, fieldArray.getColumns().size());
		assertEquals("Col1", fieldArray.getColumns().get(0).getName());
		assertEquals("t", fieldArray.getColumns().get(0).getUnitOfMeasure());
		assertEquals(2, fieldArray.getRowCount());
		assertEquals(4e15, (double)fieldArray.get(1)[0], 0.001);
		
		// Boolean (two values in case of a default value that would match the assertion)
		Item_Boolean fieldBool1 = (Item_Boolean)testObjectIn.getItem("FBool1");
		Item_Boolean fieldBool2 = (Item_Boolean)testObjectIn.getItem("FBool2");
		assertFalse(fieldBool1.getValue());
		assertTrue(fieldBool2.getValue());
		
		// Category
		Item_Category fieldCategory = (Item_Category)testObjectIn.getItem("FCategory");
		assertEquals("ctg_xyz", fieldCategory.getValue());
		
		// Count
		Item_Count fieldCount = (Item_Count)testObjectIn.getItem("FCount");
		assertEquals(42, fieldCount.getValue());
		
		// Measurement
		Item_Measurement fieldMeas = (Item_Measurement)testObjectIn.getItem("FMeasurement");
		assertEquals("s", fieldMeas.getUnitOfMeasure());
		assertEquals(45.3, fieldMeas.getValue(), 0.0001);
		
		// Text
		Item_Text fieldText = (Item_Text)testObjectIn.getItem("FText");
		assertEquals("Hello world", fieldText.getValue());
		
		// TimeInstant
		Item_TimeInstant fieldTimeInstant = (Item_TimeInstant)testObjectIn.getItem("FTimeInstant");
		assertTimeInstantExplUtc(getUtcTime("2018-03-02T14:22:05Z"), fieldTimeInstant);
		
		// TimePeriod
		Item_TimeRange fieldTimePeriod = (Item_TimeRange)testObjectIn.getItem("FTimePeriod");
		assertTimeInstantExplUtc(getUtcTime("2018-03-08T23:01:44Z"), fieldTimePeriod.getStart());
		assertTimeInstantExplUtc(getUtcTime("2018-03-09T07:32:30Z"), fieldTimePeriod.getEnd());
		
		// Nested DataRecord
		Item_DataRecord nestedDataRecord = (Item_DataRecord)testObjectIn.getItem("FNestedRecord");
		Item_Measurement nestedMeasurement = (Item_Measurement)nestedDataRecord.getItem("FNestedMeasurement");
		assertEquals("m", nestedMeasurement.getUnitOfMeasure());
		assertEquals(-0.34, nestedMeasurement.getValue(), 0.0001);
		
		// Asserting data quality. The value "bad" appears only once as
		// (1) reading quality information from various types is tested in the "read" test and
		// (2) quality information is written in the Item base class for all item types
		assertDataQualityForItem(testObjectIn, "FBool1", true);
		assertDataQualityForItem(testObjectIn, "FBool2", false);
		assertDataQualityForItem(testObjectIn, "FCategory", true);
		assertDataQualityForItem(testObjectIn, "FCount", true);
		assertDataQualityForItem(testObjectIn, "FMeasurement", true);
		assertDataQualityForItem(testObjectIn, "FText", true);
		assertDataQualityForItem(testObjectIn, "FTimeInstant", true);
		assertDataQualityForItem(testObjectIn, "FTimePeriod", true);
	}
	
	private Item_DataRecord createAndSerialiseDataRecordForTest() throws Exception
	{
		// This function was implemented to restrict the scope of the objects created.
		// There is no way to accidentally try assert the field objects before serialisation
		// because they are only created here.
		
		Item_DataRecord testObject = new Item_DataRecord();
		
		// Array
		ArrayList<Item_Array.ArrayColumn> arrayColumns = new ArrayList<>();
		arrayColumns.add(new Item_Array.ArrayColumn("Col1", Double.class, "t"));
		Item_Array arrayItem = new Item_Array(arrayColumns);
		arrayItem.add(2e15);
		arrayItem.add(4e15);
		testObject.addItem("FArray", arrayItem);
		
		// Boolean (including bad data quality)
		Item_Boolean truthValue1 = new Item_Boolean(false);
		Item_Boolean truthValue2 = new Item_Boolean(true);
		testObject.addItem("FBool1", truthValue1);
		testObject.addItem("FBool2", truthValue2, DataQuality.createBad());
		
		// Category
		Item_Category categoryItem = new Item_Category("ctg_xyz");
		testObject.addItem("FCategory", categoryItem);
		
		// Count (with explicitly good data quality)
		Item_Count countItem = new Item_Count(42);
		testObject.addItem("FCount", countItem, DataQuality.createGood());
		
		// Measurement
		Item_Measurement measItem = new Item_Measurement("s", 45.3);
		testObject.addItem("FMeasurement", measItem);
		
		// Text
		Item_Text textItem = new Item_Text("Hello world");
		testObject.addItem("FText", textItem);
		
		// Time instant
		Item_TimeInstant timeInstItem = new Item_TimeInstant(getUtcTime("2018-03-02T14:22:05Z"));
		testObject.addItem("FTimeInstant", timeInstItem);
		
		// Time period
		Item_TimeInstant stt = new Item_TimeInstant(getUtcTime("2018-03-08T23:01:44Z"));
		Item_TimeInstant end = new Item_TimeInstant(getUtcTime("2018-03-09T07:32:30Z"));
		Item_TimeRange timePeriodItem = new Item_TimeRange(stt, end);
		testObject.addItem("FTimePeriod", timePeriodItem);
		
		// Nested data record
		Item_DataRecord nestedRecord = new Item_DataRecord();
		Item_Measurement nestedMeas = new Item_Measurement("m", -0.34);
		nestedRecord.addItem("FNestedMeasurement", nestedMeas);
		testObject.addItem("FNestedRecord", nestedRecord);
		
		// Serialising and deserialising the test object
		return (Item_DataRecord)serialiseAndReadResultObj(testObject, XmlHelper.TYPEURI_COMPLEX);
	}
	
	@Test
	public void dataRecord_qualityCheck()
	{
		// Trying to associate data quality to a data record field that does not support it
		
		Item_DataRecord record = new Item_DataRecord();
		Item_DataRecord nestedRecord = new Item_DataRecord();
		
		try
		{
			record.addItem("SomeItem", nestedRecord, DataQuality.createGood());
			fail("Expected an exception");
		}
		catch (Exception ignore)
		{}
	}
	
	@Test
	public void timeSeries_read() throws Exception
	{
		// This test case concentrates only on the Item_TimeSeries base class

		final String filepath = getPathOfTestFile("Item_TimeSeriesFlexible.xml");

        // Getting result element
		TimeseriesDomainRangeType resultEl = (TimeseriesDomainRangeType)parseRawResult(filepath);

        // Processing the proxy
		Item_TimeSeriesFlexible testObject_init = new Item_TimeSeriesFlexible(resultEl);
		Item_TimeSeries testObject = (Item_TimeSeries)testObject_init;

        // Checking the measurement unit
        assertEquals("m", testObject.getUnitOfMeasure());

        // Asserting description
        assertEquals("Example", testObject.getDescription());

        // Assert item count
        assertEquals(4, testObject.getValueCount());

        // Checking measurement values
        assertEquals(2.03, testObject.getValue(0), 0.0001);
        assertEquals(2.06, testObject.getValue(1), 0.0001);
        assertEquals(2.42, testObject.getValue(2), 0.0001);
        assertEquals(2.23, testObject.getValue(3), 0.0001);

        // Checking data qualities
        assertTrue(testObject.getDataQuality(0).isGood());
        assertFalse(testObject.getDataQuality(1).isGood());
        assertTrue(testObject.getDataQuality(2).isGood());
        assertFalse(testObject.getDataQuality(3).isGood());
	}
	
	@Test
	public void timeSeries_create() throws Exception
	{
		// This test case concentrates only on the Item_TimeSeries base class

        Item_TimeInstant timestamp1 = new Item_TimeInstant(getUtcTime("2018-03-16T08:30:00Z"));
        Item_TimeInstant timestamp2 = new Item_TimeInstant(getUtcTime("2018-03-16T08:40:00Z"));
        Item_TimeInstant timestamp3 = new Item_TimeInstant(getUtcTime("2018-03-16T08:50:00Z"));
        Item_TimeInstant timestamp4 = new Item_TimeInstant(getUtcTime("2018-03-16T09:00:00Z"));

        // Creating the actual test object for serialisation
        Item_TimeSeriesFlexible originalObj = new Item_TimeSeriesFlexible("Cel");
        originalObj.setDescription("Hello");
        
        originalObj.addValue(timestamp1, -9.4, DataQuality.createGood());
        originalObj.addValue(timestamp2, -8.3, DataQuality.createGood());
        originalObj.addValue(timestamp3, -7, DataQuality.createBad());
        originalObj.addValue(timestamp4, -6.9, DataQuality.createGood());

        // Serialising and deserialising the test object
        Item_TimeSeries parsedObj = (Item_TimeSeries)serialiseAndReadResultObj(originalObj, XmlHelper.TYPEURI_TIMESERIESFLEXIBLE);

        // Asserting item count and unit of measure
        assertEquals("Cel", parsedObj.getUnitOfMeasure());
        assertEquals(4, parsedObj.getValueCount());

        // Asserting description
        assertEquals("Hello", parsedObj.getDescription());

        // Checking measurement values
        assertEquals(-9.4, parsedObj.getValue(0), 0.0001);
        assertEquals(-8.3, parsedObj.getValue(1), 0.0001);
        assertEquals(-7, parsedObj.getValue(2), 0.0001);
        assertEquals(-6.9, parsedObj.getValue(3), 0.0001);
        
        // Checking data qualities
        assertTrue(parsedObj.getDataQuality(0).isGood());
        assertTrue(parsedObj.getDataQuality(1).isGood());
        assertFalse(parsedObj.getDataQuality(2).isGood());
        assertTrue(parsedObj.getDataQuality(3).isGood());
	}
	
	@Test
	public void timeSeriesFlexible_read() throws Exception
	{
		// Only testing the features specific to Item_TimeSeriesFlexible

		final String filepath = getPathOfTestFile("Item_TimeSeriesFlexible.xml");

		// Getting result element
		TimeseriesDomainRangeType resultEl = (TimeseriesDomainRangeType)parseRawResult(filepath);
        
        // Reading the proxy
		Item_TimeSeriesFlexible testObject = new Item_TimeSeriesFlexible(resultEl);
        
		ArrayList<DateTime> expectedTimeEntries = new ArrayList<>();
		expectedTimeEntries.add(getUtcTime("2020-01-01T00:00:00Z"));
		expectedTimeEntries.add(getUtcTime("2020-01-02T00:00:00Z"));
		expectedTimeEntries.add(getUtcTime("2020-01-03T00:00:00Z"));
		expectedTimeEntries.add(getUtcTime("2020-01-04T00:00:00Z"));
        
        // Checking that all datetime entries were parsed correctly
        for (int a = 0; a < expectedTimeEntries.size(); ++a)
        {
        	assertTimeInstantExplUtc(expectedTimeEntries.get(a), testObject.getTimestamp(a));
        }
	}
	
	@Test
	public void timeSeriesFlexible_create() throws Exception
	{
		// Only testing the features specific to Item_TimeSeriesFlexible

		DateTime timestamp1 = getUtcTime("2018-03-16T08:30:00Z");
        DateTime timestamp2 = getUtcTime("2018-03-16T08:40:00Z");
        DateTime timestamp3 = getUtcTime("2018-03-16T08:50:00Z");
        DateTime timestamp4 = getUtcTime("2018-03-16T09:00:00Z");

        // Creating the actual test object for serialisation
        Item_TimeSeriesFlexible originalObj = new Item_TimeSeriesFlexible("Cel");
        
        originalObj.addValue(new Item_TimeInstant(timestamp1), -9.4);
        originalObj.addValue(new Item_TimeInstant(timestamp2), -8.3);
        originalObj.addValue(new Item_TimeInstant(timestamp3), -7, DataQuality.createBad());
        originalObj.addValue(new Item_TimeInstant(timestamp4), -6.9);
        
        // Serialising and deserialising the test object
        Item_TimeSeriesFlexible parsedObj = (Item_TimeSeriesFlexible)serialiseAndReadResultObj(originalObj, XmlHelper.TYPEURI_TIMESERIESFLEXIBLE);
        
        ArrayList<DateTime> expectedTimeEntries = new ArrayList<>();
        expectedTimeEntries.add(timestamp1);
        expectedTimeEntries.add(timestamp2);
        expectedTimeEntries.add(timestamp3);
        expectedTimeEntries.add(timestamp4);
        
        // Checking that all datetime entries were parsed correctly
        for (int a = 0; a < expectedTimeEntries.size(); ++a)
        {
        	assertTimeInstantExplUtc(expectedTimeEntries.get(a), parsedObj.getTimestamp(a));
        }
	}
	
	@Test
	public void timeSeriesConstant_read() throws Exception
	{
		// Only testing the features specific to Item_TimeSeriesConstant, not the base class.

		final String filepath = getPathOfTestFile("Item_TimeSeriesConstant.xml");

        // Getting result element
		TimeseriesDomainRangeType resultEl = (TimeseriesDomainRangeType)parseRawResult(filepath);

        // Reading the proxy
		Item_TimeSeriesConstant testObject = new Item_TimeSeriesConstant(resultEl);

		// Asserting basetime value
		DateTime expectedBaseTime = getUtcTime("2020-02-21T12:00:00Z");
        assertTimeInstantExplUtc(expectedBaseTime, testObject.getBaseTime());

        // Asserting spacing value
        long expectedSpacing = Period.hours(1).getMillis();
        assertEquals(expectedSpacing, testObject.getSpacing().getMillis());
	}
	
	@Test
	public void timeSeriesConstant_create() throws Exception
	{
		// Only testing the features specific to Item_TimeSeriesConstant, not the base class.

        DateTime baseTime = getUtcTime("2018-06-26T09:13:44Z");
        Period spacing = Period.minutes(30);
        Item_TimeSeriesConstant originalObj = new Item_TimeSeriesConstant("m", new Item_TimeInstant(baseTime), spacing);

        // Adding items.
        // Not adding many because the base class already implements this.
        originalObj.addValue(4);
        originalObj.addValue(3.2, DataQuality.createBad());

        // Serialising and deserialising the test object
        Item_TimeSeriesConstant parsedObj = (Item_TimeSeriesConstant)serialiseAndReadResultObj(originalObj, XmlHelper.TYPEURI_TIMESERIESCONSTANT);

        // Asserting basetime value
        assertTimeInstantExplUtc(baseTime, parsedObj.getBaseTime());

        // Asserting spacing value
        assertEquals(spacing.getMillis(), parsedObj.getSpacing().getMillis());

        // Asserting items
        assertEquals(2, parsedObj.getValueCount());
        assertEquals(4, parsedObj.getValue(0), 0.001);
        assertEquals(3.2, parsedObj.getValue(1), 0.001);
        assertTrue(parsedObj.getDataQuality(0).isGood());
        assertFalse(parsedObj.getDataQuality(1).isGood());
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
		
		//String xmlString = observation.toXmlString();
		
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
	
	private void assertDataQualityForItem(Item_DataRecord record, String item, boolean good)
	{
		if (good)
		{
			assertTrue(record.getQualityOfItem(item).isGood());
		}
		else
		{
			assertFalse(record.getQualityOfItem(item).isGood());
		}
	}
	
	private void assertTimeInstantExplUtc(DateTime expDt, Item_TimeInstant actual)
	{
		// Assert time instant. Expect explicit time zone UTC.
		assertTimeInstant(expDt, true, true, actual);
	}
	
	private void assertTimeInstant(DateTime expDt, boolean explZone, boolean expectUtc, Item_TimeInstant actual)
	{
		DateTime actDt = actual.getValue();
		
		// Expecting UTC as the zone?
		assertEquals(expectUtc, actDt.getZone().equals(DateTimeZone.UTC));
		
		// Assert offset, difference and whether an explicit timezone was defined
		assertEquals(DateTimeZone.UTC.getOffset(expDt), DateTimeZone.UTC.getOffset(actDt));
		assertEquals("Expected " + expDt.toString() + ", got " + actDt.toString(), 0, expDt.compareTo(actDt));
		assertEquals(explZone, actual.getHasExplicitUtcOffset());
	}
	
	// Interface to allow function parameters
	interface ITestInterface
	{
		void invoke() throws Exception;
	}
	
	// This method asserts an exception
	private void assertIllegalDateTimeException(ITestInterface testFcn, String expectedErrorStart)
	{
		try
		{
			testFcn.invoke();
			fail("Expected exception");
		}
		catch (IllegalDateTimeException e)
		{
			String msg = e.getMessage();
			assertTrue("Unexpected message '" + msg + "'", msg.startsWith(expectedErrorStart));
		}
		catch (Exception e)
		{
			fail("Unexpected exception " + e.getClass().getName());
		}
	}
	
	// This method asserts an exception
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
	
	private DateTime getUtcTime(String xsdDateTime)
	{
		return DateTime.parse(xsdDateTime).withZone(DateTimeZone.UTC);
	}
	
	private String getPathOfTestFile(String filename)
	{
		return System.getProperty("user.dir") + "/../common/testfiles/" + filename;
	}
}
