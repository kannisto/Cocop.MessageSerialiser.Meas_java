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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Node;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import eu.cocop_spire.om_custom._1_1.AbstractGmlAsSweDataComponentType;
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.AbstractSimpleComponentType;
import net.opengis.swe._2.BooleanType;
import net.opengis.swe._2.CategoryType;
import net.opengis.swe._2.CountType;
import net.opengis.swe._2.DataArrayType;
import net.opengis.swe._2.DataRecordPropertyType;
import net.opengis.swe._2.DataRecordType;
import net.opengis.swe._2.DataRecordType.Field;
import net.opengis.swe._2.ObjectFactory;
import net.opengis.swe._2.QualityPropertyType;
import net.opengis.swe._2.QuantityType;
import net.opengis.swe._2.TextType;
import net.opengis.swe._2.TimeRangeType;
import net.opengis.swe._2.TimeType;

/**
 * Represents a record that consists of multiple measurements.
 * 
 * In this module, the code has been derived from OGC(r) SWE Common Data Model
 * Encoding Standard (OGC 08-094r1; please see the file
 * "ref_and_license_ogc_swecommon.txt").
 * @see Item_Array
 * @author Petri Kannisto
 */
public final class Item_DataRecord extends Item
{
	private final String emptyRecordItem = "__cocop-empty-value";
	
	private final TreeMap<String, Item> m_items = new TreeMap<String, Item>();
	private final TreeMap<String, DataQuality> m_itemQualities = new TreeMap<>();
	
	
	/**
	 * Constructor.
	 */
	public Item_DataRecord()
	{
		super(XmlHelper.TYPEURI_COMPLEX);
		
		// Otherwise, empty ctor body
	}
	
	/**
	 * Constructor. Use this to populate the object from an XML node.
	 * This constructor was created particularly to enable scheduling
	 * parameters with the "biz" library.
	 * @param xmlNode XML node to unmarshal.
	 * @throws InvalidMessageException Thrown if an error occurs.
	 */
	public Item_DataRecord(Node xmlNode) throws InvalidMessageException
	{
		this(unmarshalFromNodes(xmlNode));
	}
	
	@SuppressWarnings("unchecked")
	private static DataRecordPropertyType unmarshalFromNodes(Node xmlNode)
			throws InvalidMessageException
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
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				proxy = unmarshaller.unmarshal(xmlNode);
			}
			finally
			{
				if (reader != null) reader.close();
			}
			
			return ((JAXBElement<DataRecordPropertyType>)proxy).getValue(); // throws ClassCastException
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Expected a data record", e);
		}
		catch (JAXBException e)
		{
			throw new InvalidMessageException("Failed to unmarshal XML", e);
		}
		catch (IOException e)
		{
			// This exception is not expected
			throw new RuntimeException("Failed to parse XML", e);
		}
	}
	
	/**
	 * Constructor. Used to instantiate an item from XML data (observation result).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_DataRecord(DataRecordPropertyType el) throws InvalidMessageException
	{
		this(el.getDataRecord());
	}
	
	/**
	 * Constructor. Used to instantiate an item from XML data (data record field).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_DataRecord(DataRecordType el) throws InvalidMessageException
	{
		this();
		
		readDataRecord(el);
	}
	
	private void readDataRecord(DataRecordType el) throws InvalidMessageException
	{
		String fieldForMsg = "(unknown)";
		
		try
		{
			for (Field field : el.getField())
			{
				String fieldName = field.getName();
				
				if (fieldName.equals(emptyRecordItem))
				{
					// Empty item -> skip it.
					// Empty items can exist, because there are use cases for
					// empty data records but the XML schema does not allow
					// empty records.
					continue;
				}
				
				fieldForMsg = fieldName;
				AbstractDataComponentType fieldObj = field.getAbstractDataComponent().getValue();
				
				Item itemRead = null;
				DataQuality dataQuality = null;
				
				// Data record?
				if (fieldObj instanceof DataRecordType)
				{
					itemRead = new Item_DataRecord((DataRecordType)fieldObj);
				}
				// Simple content?
				else if (fieldObj instanceof AbstractSimpleComponentType)
				{
					AbstractSimpleComponentType simpleComp = (AbstractSimpleComponentType)fieldObj;
					
					// Trying to get data quality
					if (simpleComp.getQuality() != null && simpleComp.getQuality().size() > 0)
					{
						QualityPropertyType qualityProp = simpleComp.getQuality().get(0);
						dataQuality = new DataQuality(qualityProp.getTitleAttr());
					}
					
					itemRead = readSimpleComponent(simpleComp);
				}
				// Array?
				else if (fieldObj instanceof DataArrayType)
				{
					itemRead = new Item_Array((DataArrayType)fieldObj);
				}
				// Time series?
				else if (fieldObj instanceof AbstractGmlAsSweDataComponentType)
				{
					// To have time series working here, the proxy class
					// "eu.cocop_spire.om_custom._1_1.SweDataComponentAsFeatureType" was
					// generated with XJC. The proxies referring to the object have
					// been modified manually, as the default namespace conflicted
					// with the namespace of COCOP custom 1.2.
					
					// For robustness, just skipping unsupported field type.
					// TODO: Implement support for time series in a data record
					continue;
				}
				
				if (itemRead == null)
				{
					// For robustness, just skipping an unknown field type
					continue;
				}
				
				// Quality information found? This only applies to simple content.
				if (dataQuality == null)
				{
					this.addItem(fieldName, itemRead);
				}
				else
				{
					this.addItem(fieldName, itemRead, dataQuality);
				}
			}
		}
		catch (Exception e)
		{
			throw new InvalidMessageException("Failed to process field " + fieldForMsg, e);
		}
	}
	
	private Item readSimpleComponent(AbstractSimpleComponentType fieldObj) throws InvalidMessageException
	{
		if (fieldObj instanceof BooleanType)
		{
			return new Item_Boolean((BooleanType)fieldObj);
		}
		else if (fieldObj instanceof CategoryType)
		{
			return new Item_Category((CategoryType)fieldObj);
		}
		else if (fieldObj instanceof CountType)
		{
			return new Item_Count((CountType)fieldObj);
		}
		else if (fieldObj instanceof QuantityType)
		{
			return new Item_Measurement((QuantityType)fieldObj);
		}
		else if (fieldObj instanceof TextType)
		{
			return new Item_Text((TextType)fieldObj);
		}
		else if (fieldObj instanceof TimeType)
		{
			return new Item_TimeInstant((TimeType)fieldObj);
		}
		else if (fieldObj instanceof TimeRangeType)
		{
			return new Item_TimeRange((TimeRangeType)fieldObj);
		}
		else
		{
			// Unknown type. For robustness, just skip the field.
			return null;
		}
	}
	
	
	@Override
	protected boolean supportsDataQualityInDataRecord()
	{
		return false;
	}
	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		return toDataRecordPropertyProxy();
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		return fact.createDataRecord(buildDataRecordForMarshal(fact));
	}
	
	private DataRecordType buildDataRecordForMarshal(net.opengis.swe._2.ObjectFactory fact)
	{
		DataRecordType retval = new DataRecordType();
		
		if (m_items.size() < 1)
		{
			// If the record is empty, adding an empty value.
			// The XML schema does not allow empty data record, but
			// there are uses cases for these.
			Field emptyField = createFieldElementForProxy(emptyRecordItem, new Item_Text(""), fact, true);
			retval.getField().add(emptyField);
		}
		else
		{
			for (String fieldName : m_items.keySet())
			{
				// Missing quality information is considered good quality
				boolean goodQuality = !m_itemQualities.containsKey(fieldName) || m_itemQualities.get(fieldName).isGood();
				
				// Create field element for the data record
				Field currentField = createFieldElementForProxy(fieldName, m_items.get(fieldName), fact, goodQuality);
				
				// Add the field element to the return value
				retval.getField().add(currentField);
			}
		}
		
		return retval;
	}
	
	private Field createFieldElementForProxy(String fieldName, Item item, net.opengis.swe._2.ObjectFactory fact, boolean goodQuality)
	{
		// Create field element for the data record
		Field currentField = new Field();
		currentField.setName(fieldName);
		
		// Set the item enclosed by the field
		JAXBElement<? extends AbstractDataComponentType> objectForMarshal = item.getObjectForXml_DataRecordField(fact);
		currentField.setAbstractDataComponent(objectForMarshal);
		
		// Adding data quality information if not good
		if (!goodQuality)
		{
			String qualityValue = m_itemQualities.get(fieldName).getValue();
			
			// Only simple components can have quality information
			AbstractSimpleComponentType simpleItem = (AbstractSimpleComponentType)objectForMarshal.getValue();
			QualityPropertyType qualityInfo = new QualityPropertyType();
			qualityInfo.setTitleAttr(qualityValue);
			simpleItem.getQuality().add(qualityInfo);
		}
		
		return currentField;
	}
	
	
	/**
	 * Gets the name of each item in the record.
	 * @return Names.
	 */
	public Set<String> getItemNames()
	{
		return m_items.keySet();
	}
	
	/**
	 * Gets an item by its name.
	 * @param n Name.
	 * @return Item.
	 */
	public Item getItem(String n)
	{
		return m_items.get(n);
	}
	
	/**
	 * Sets a field.
	 * @param n Name.
	 * @param i Item.
	 */
	public void addItem(String n, Item i)
	{
		if (m_items.containsKey(n))
		{
			throw new IllegalArgumentException("Duplicate item name \"" + n + "\"");
		}
		
		m_items.put(n, i);
	}
	
	/**
	 * Sets a field.
	 * @param n Name.
	 * @param i Field data.
	 * @param qual Data quality.
	 */
	public void addItem(String n, Item i, DataQuality qual)
	{
		// Data quality supported?
		if (!i.supportsDataQualityInDataRecord())
		{
			throw new RuntimeException("\"" + n + "\": the item type does not support data quality in data records");
		}
		
		// Add item and set its quality
		addItem(n, i);
		m_itemQualities.put(n, qual);
	}
	
	/**
	 * Gets the data quality of an item.
	 * @param n Item name.
	 * @return Quality information.
	 */
	public DataQuality getQualityOfItem(String n)
	{
		Item item = getItem(n);
		
		// Data quality supported?
		if (!item.supportsDataQualityInDataRecord())
		{
			throw new RuntimeException("\"" + n + "\": the item type does not support data quality in data records");
		}
		
		// Does quality information exist?
		if (m_itemQualities.containsKey(n))
		{
			return m_itemQualities.get(n);
		}
		else
		{
			// The default is good
			return DataQuality.createGood();
		}
	}
	
	/**
	 * Generates an XML proxy from the object. Use this if you want to include the
	 * object to another XML document.
	 * @return Proxy object.
	 */
	public DataRecordPropertyType toDataRecordPropertyProxy()
	{
		net.opengis.swe._2.ObjectFactory fact = new ObjectFactory();
		
		DataRecordPropertyType prop = fact.createDataRecordPropertyType();
		prop.setDataRecord(buildDataRecordForMarshal(fact));
		
		return prop;
	}
}
