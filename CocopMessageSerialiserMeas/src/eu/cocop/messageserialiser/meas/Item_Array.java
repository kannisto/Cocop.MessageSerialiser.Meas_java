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

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import eu.cocop_spire.om_custom._1_2.ArrayRowType;
import eu.cocop_spire.om_custom._1_2.ArrayType;
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.AbstractSimpleComponentType;
import net.opengis.swe._2.BooleanType;
import net.opengis.swe._2.CountPropertyType;
import net.opengis.swe._2.CountType;
import net.opengis.swe._2.DataArrayType;
import net.opengis.swe._2.DataRecordType;
import net.opengis.swe._2.DataRecordType.Field;
import net.opengis.swe._2.EncodedValuesPropertyType;
import net.opengis.swe._2.ObjectFactory;
import net.opengis.swe._2.QuantityType;
import net.opengis.swe._2.TextType;
import net.opengis.swe._2.TimeType;
import net.opengis.swe._2.UnitReference;

/**
 * Represents an array of data.
 * 
 * In this module, the code has been derived from OGC(r) SWE Common Data
 * Model Encoding Standard 
 * (OGC 08-094r1; please see the file "ref_and_license_ogc_swecommon.txt").
 * @see Item_DataRecord
 * @see Item_TimeSeriesConstant
 * @see Item_TimeSeriesFlexible
 * @author Petri Kannisto
 */
public final class Item_Array extends Item
{
	private final ArrayList<Object[]> m_rows; 
	private final ArrayList<ArrayColumn> m_columns;
	
	
	/**
	 * Constructor.
	 * @param columns The columns of the array.
	 */
	public Item_Array(ArrayList<ArrayColumn> columns)
	{
		super(XmlHelper.TYPEURI_COMPLEX);
		
		m_columns = columns;
		m_rows = new ArrayList<>();
	}
	
	/**
	 * Constructor. Use this when populating the object from XML.
	 * @param proxy XML proxy.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_Array(DataArrayType proxy) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_COMPLEX);
		
		m_columns = readColumnsFromProxy(proxy); // throws InvalidMessageException
		m_rows = readRowsFromProxy(proxy); // throws InvalidMessageException
	}
	
	/**
	 * The columns of the array.
	 * @return The columns of the array.
	 */
	public ArrayList<ArrayColumn> getColumns()
	{
		return m_columns;
	}
	
	/**
	 * The count of rows in the array.
	 * @return The count of rows in the array.
	 */
	public int getRowCount()
	{
		return m_rows.size();
	}
	
	/**
	 * Returns the row at given index.
	 * @param index Index.
	 * @return Row.
	 * @throws IndexOutOfBoundsException Thrown if the index is out of bounds.
	 */
	public Object[] get(int index) throws IndexOutOfBoundsException
	{
		return m_rows.get(index);
	}
	
	/**
	 * Adds a row to the array.
	 * @param items Row.
	 * @throws IllegalArgumentException Thrown if the items types do not match the specified columns or if the cell count does not match the column count.
	 */
	public void add(Object... items) throws IllegalArgumentException
	{
		// Checking item count
        if (items.length != m_columns.size())
        {
            throw new IllegalArgumentException("Received cell count does not match with column count");
        }

        // Checking typing
        for (int a = 0; a < items.length; ++a)
        {
            Object item = items[a];

            if (valueIsEmpty(item))
            {
                // No type check for empty
                continue;
            }

            Class<?> columnType = m_columns.get(a).getDataType();
            Class<?> cellType = item.getClass();

            if (!typesAreEqual(columnType, cellType))
            {
                throw new IllegalArgumentException("Type mismatch: expected " + columnType.getCanonicalName() + ", got " + cellType.getCanonicalName());
            }
        }

        m_rows.add(items);
	}
	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		return createProxy();
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(ObjectFactory fact)
	{
		DataArrayType proxy = createProxy();
		return fact.createDataArray(proxy);
	}
	
	private DataArrayType createProxy()
	{
		DataArrayType proxy = new DataArrayType();
		
		// The schema requires the "elementCount" element but nothing inside it
		proxy.setElementCount(new CountPropertyType());
		
		// The schema requires the "elementType" element.
		// These are the columns of the array.
        proxy.setElementType(createColumnsForProxy());
        
        // These are the rows of the array.
        EncodedValuesPropertyType valueContainer = new EncodedValuesPropertyType();
        valueContainer.getAny().add(createRowsForProxy());
        proxy.setValues(valueContainer);
        
		return proxy;
	}
	
	private DataArrayType.ElementType createColumnsForProxy()
	{
		DataArrayType.ElementType retval = new DataArrayType.ElementType();
		retval.setName("columns"); // Name attribute is required in the XML schema and must have a valid NCName as the value
		
		// Omitting data record if no columns, because an empty
        // data record is not allowed in the XML schema.
        if (m_columns.size() > 0)
        {
        	DataRecordType columnsRecord = new DataRecordType();
        	ObjectFactory factory = new ObjectFactory();
        	
        	for (ArrayColumn col : m_columns)
        	{
        		Field fieldProxy = col.toXmlProxy(factory);
        		columnsRecord.getField().add(fieldProxy);
        	}
        	
        	// Assign the data record
        	retval.setAbstractDataComponent(factory.createDataRecord(columnsRecord));
        }
		
		return retval;
	}
	
	private Element createRowsForProxy()
	{
		ArrayType arrayForMarshal = new ArrayType();
		
		// Setting rows to the array object
		for (Object[] cells : m_rows)
		{
			ArrayRowType rowProxy = new ArrayRowType();
			
			for (int a = 0; a < cells.length; ++a)
			{
				Object cell = cells[a];
				Class<?> type = m_columns.get(a).getDataType();
				String serialised = serialiseAfterType(cell, type);
				rowProxy.getI().add(serialised);
			}
			
			arrayForMarshal.getRow().add(rowProxy);
		}
		
		// Marshalling to XML DOM
		try
		{
			// Get JAXB context
			JAXBContext jaxbContext = XmlHelper.getJaxbContext();
			
			// Create DOM document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document document = db.newDocument();
			
			// Do JAXB marshalling
			Marshaller marshaller = jaxbContext.createMarshaller(); // throws JAXBException
			marshaller.marshal(arrayForMarshal, document); // throws JAXBException, MarshalException
			
			// Return root element
			return document.getDocumentElement();
		}
		catch (Exception e) // Catching all exceptions just in case
		{
			// Use RuntimeException, because it is unchecked and these errors
			// should not appear at runtime if the code is OK.
			throw new RuntimeException("Failed to unmarshal custom array", e);
		}
	}
	
	private String serialiseAfterType(Object value, Class<?> type)
    {
        // Empty values
        if (valueIsEmpty(value))
        {
            return "";
        }
        
        if (typesAreEqual(type, String.class))
        {
            return ((String)value).trim();
        }
        else if (typesAreEqual(type, Boolean.class))
        {
            return XmlHelper.serialiseXmlBoolean((Boolean)value);
        }
        else if (typesAreEqual(type, Item_TimeInstant.class))
        {
        	return ((Item_TimeInstant)value).toXsdDateTime();
        }
        else if (typesAreEqual(type, Long.class))
        {
        	return XmlHelper.serialiseXmlLong((Long)value);
        }
        else if (typesAreEqual(type, Double.class))
        {
            return XmlHelper.serialiseXmlDouble((Double)value);
        }
        else
        {
            throw new IllegalArgumentException("Unexpected cell type " + type.getCanonicalName());
        }
    }
	
	private ArrayList<ArrayColumn> readColumnsFromProxy(DataArrayType proxy) throws InvalidMessageException
	{
		try
		{
			ArrayList<ArrayColumn> retval = new ArrayList<>();
			
			if (proxy.getElementType() != null &&
				proxy.getElementType().getAbstractDataComponent() != null &&
				proxy.getElementType().getAbstractDataComponent().getValue() != null)
			{
				// Expecting a data record of columns
				DataRecordType record = (DataRecordType)proxy.getElementType().getAbstractDataComponent().getValue();
				Object fields = record.getField();
				
				if (fields != null)
				{
					for (Field field : record.getField())
					{
						retval.add(new ArrayColumn(field));
					}
				}
			}
			
			return retval;
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Failed to read columns of array - expected DataRecord nested in elementType", e);
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException("Failed to read columns of array - something missing", e);
		}
	}
	
	private ArrayList<Object[]> readRowsFromProxy(DataArrayType proxy) throws InvalidMessageException
	{
		ArrayList<Object[]> retval = new ArrayList<>();
		
		if (proxy.getValues() == null ||
			proxy.getValues().getAny() == null)
		{
			// No rows
			return retval;
		}
		
		// This semi-manual unmarshalling is necessary, because the "any" type use in the
		// schema maps to XmlNode objects in the proxy.
		ArrayType customArray = unmarshalCustomArray(proxy.getValues().getAny().get(0)); // throws InvalidMessageException
		
		if (customArray.getRow() != null)
		{
			for (ArrayRowType row : customArray.getRow())
			{
				Object[] parsedValuesOfRow = new Object[m_columns.size()];
				
				if (row.getI() == null)
				{
					// Expecting no columns
					if (m_columns.size() != 0)
					{
						throw new InvalidMessageException("No columns defined for array but a row has cells");
					}
				}
				else
				{
					if (row.getI().size() != m_columns.size())
					{
						throw new InvalidMessageException("Inconsistent cell count in rows of array");
					}
					
					for (int a = 0; a < row.getI().size(); ++a)
                    {
                        // Attempting to parse the value after the type of the column
						String raw = row.getI().get(a);
                        Object valueParsed = tryParseAfterType(raw, m_columns.get(a).getDataType()); // throws InvalidMessageException
                        parsedValuesOfRow[a] = valueParsed;
                    }
				}
				
				retval.add(parsedValuesOfRow);
			}
		}
		
		return retval;
	}
	
	private ArrayType unmarshalCustomArray(Node arrayRootNode) throws InvalidMessageException
	{
		try
		{
			// Get JAXB context
			JAXBContext jaxbContext = XmlHelper.getJaxbContext();
			
			// Do JAXB unmarshalling
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); // throws JAXBException
			return (ArrayType)unmarshaller.unmarshal(arrayRootNode); // throws JAXBException
		}
		catch (Exception e) // Catching all exceptions just in case
		{
			throw new InvalidMessageException("Failed to unmarshal custom array", e);
		}
	}
	
	private Object tryParseAfterType(String raw, Class<?> type) throws InvalidMessageException
	{
		if (valueIsEmpty(raw))
		{
			// Empty value
			return null;
		}
		
		try
		{
			if (typesAreEqual(type, Boolean.class))
			{
				return XmlHelper.parseXmlBoolean(raw); // throws IllegalArgumentException
			}
			else if (typesAreEqual(type, Item_TimeInstant.class))
			{
				return new Item_TimeInstant(raw);
			}
			else if (typesAreEqual(type, Double.class))
			{
				return XmlHelper.parseXmlDouble(raw); // throws NumberFormatException
			}
			else if (typesAreEqual(type, Long.class))
			{
				return XmlHelper.parseXmlLong(raw); // throws NumberFormatException
			}
			else if (typesAreEqual(type, String.class))
			{
				// This will condition will match unsupported simple types too
				return raw;
			}
			else
			{
				throw new RuntimeException("Unexpected column type " + type.getCanonicalName().toString());
			}
		}
		catch (IllegalArgumentException e) // will catch NumberFormatException too
		{
			throw new InvalidMessageException("Failed to parse value in array: \"" + raw + "\"", e);
		}
	}
	
	private boolean valueIsEmpty(Object value)
    {
        return
            value == null ||
            value instanceof String && ((String)value).trim().isEmpty();
    }
	
	private static boolean typesAreEqual(Class<?> t1, Class<?> t2)
	{
		String t1str = t1.getCanonicalName();
		String t2str = t2.getCanonicalName();
		
		return t1str.equals(t2str);
	}
	
	
	/**
	 * Represents a column in an array.
	 * 
	 * In this module, the code has been derived from OGC(r) SWE Common Data
	 * Model Encoding Standard 
	 * (OGC 08-094r1; please see the file "ref_and_license_ogc_swecommon.txt").
	 * @author Petri Kannisto
	 */
	public static final class ArrayColumn
	{
		private final String m_name;
		private final Class<?> m_dataType;
		private final String m_unitOfMeasure;
		private final boolean m_dataTypeSupported;
		
		private String m_label = null;
		private String m_description = null;
		
		
		/**
		 * Constructor.
		 * @param name Column name.
		 * @param type Column type.
		 * @throws IllegalArgumentException Thrown if the column type is unsupported.
		 */
		public ArrayColumn(String name, Class<?> type) throws IllegalArgumentException
		{
			this(name, type, "");
		}
		
		/**
		 * Constructor.
		 * @param name Column name.
		 * @param type Column type.
		 * @param uom Unit of measure.
		 * @throws IllegalArgumentException Thrown if an invalid argument is detected.
		 */
		public ArrayColumn(String name, Class<?> type, String uom) throws IllegalArgumentException
		{
			// Checking the name argument. This must be a valid NCName.
			checkIfValidColumnName(name); // throws IllegalArgumentException
			
			type = convertTypeIfNeeded(type);
			
			// Checking if the type is supported
			String typeString = type.getCanonicalName();
			
			if (!getSupportedTypes().contains(typeString))
			{
				throw new IllegalArgumentException("Unsupported column type " + typeString + "; the supported are " + getSupportedTypesString());
			}
			
			// Type check: only measurements can have a unit.
			// Omitting the general check of supported types, because
			// there would be redundancy.
            if (uom != null && !uom.isEmpty() && !typesAreEqual(type, Double.class))
            {
                throw new IllegalArgumentException("Only measurements (doubles) can have a unit");
            }
            
            m_name = name;
			m_dataType = type;
			m_dataTypeSupported = true;
			m_unitOfMeasure = uom;
		}
		
		private void checkIfValidColumnName(String s) throws IllegalArgumentException
		{
			// Checking if invalid
			boolean invalid = 
					s == null ||
					s.isEmpty() ||
					s.contains(":") ||
					stringContainsWhitespaces(s);
					
			if (invalid)
			{
				String errMsg = "Column name is mandatory and must be valid NCName";
				
				if (s != null && !s.isEmpty())
				{
					errMsg = errMsg + ": \"" + s + "\"";
				}
				
				throw new IllegalArgumentException(errMsg);
			}
		}
		
		private boolean stringContainsWhitespaces(String s)
		{
			Pattern wspPattern = Pattern.compile("\\s");
			Matcher wspMatcher = wspPattern.matcher(s);
			return wspMatcher.find();
		}
		
		private Class<?> convertTypeIfNeeded(Class<?> type)
		{
			// Converting primitive classes to object classes, because boxing
			// in the "add" method will force using object types anyway.
			if (typesAreEqual(type, boolean.class))
			{
				return Boolean.class;
			}
			else if (typesAreEqual(type, double.class))
			{
				return Double.class;
			}
			else if (typesAreEqual(type, long.class))
			{
				return Long.class;
			}
			else
			{
				return type;
			}
		}
		
		/**
		 * Constructor.
		 * @param proxy XML proxy.
		 * @throws InvalidMessageException Thrown if an error is encountered.
		 */
		ArrayColumn(Field proxy) throws InvalidMessageException
		{
			try
			{
				String unitOfMeasureTemp = "";
				
				AbstractSimpleComponentType abstractObject = (AbstractSimpleComponentType)proxy.getAbstractDataComponent().getValue();
				
				if (abstractObject instanceof BooleanType)
				{
					m_dataType = Boolean.class;
					m_dataTypeSupported = true;
				}
				else if (abstractObject instanceof CountType)
				{
					m_dataType = Long.class;
					m_dataTypeSupported = true;
				}
				else if (abstractObject instanceof QuantityType)
				{
					m_dataType = Double.class;
					m_dataTypeSupported = true;
					
					// Assign unit of measure
					QuantityType measProxy = (QuantityType)proxy.getAbstractDataComponent().getValue();
					
					if (measProxy.getUom() != null)
					{
						unitOfMeasureTemp = measProxy.getUom().getCode();
					}
				}
				else if (abstractObject instanceof TextType)
				{
					m_dataType = String.class;
					m_dataTypeSupported = true;
				}
				else if (abstractObject instanceof TimeType)
				{
					m_dataType = Item_TimeInstant.class;
					m_dataTypeSupported = true;
				}
				else
				{
					// Unexpected simple type. The values will be treated as strings.
					m_dataType = String.class;
					m_dataTypeSupported = false;
				}
				
				// Assigning the fields common to all types
				m_name = proxy.getName();
				m_unitOfMeasure = unitOfMeasureTemp;
				m_label = abstractObject.getLabel();
				m_description = abstractObject.getDescription();
			}
			catch (ClassCastException e)
			{
				throw new InvalidMessageException("Type mismatch in array column", e);
			}
			catch (NullPointerException e)
			{
				throw new InvalidMessageException("Failed to read array column data - something is missing", e);
			}
		}
		
		private TreeSet<String> getSupportedTypes()
		{
			TreeSet<String> retval = new TreeSet<>();
			retval.add(Boolean.class.getCanonicalName());
			retval.add(Double.class.getCanonicalName());
			retval.add(Item_TimeInstant.class.getCanonicalName());
			retval.add(Long.class.getCanonicalName());
			retval.add(String.class.getCanonicalName());
			
			return retval;
		}
		
		private String getSupportedTypesString()
		{
			return String.join(", ", getSupportedTypes());
		}
		
		/**
		 * The name of the column.
		 * @return The name of the column.
		 */
		public String getName()
		{
			return m_name;
		}
		
		/**
		 * Data type.
		 * @return Data type.
		 */
		public Class<?> getDataType()
		{
			return m_dataType;
		}
		
		/**
		 * Whether the data type is supported. When deserialising XML,
		 * this will be false for unsupported types that map to string.
		 * @return True if supported. Otherwise, false.
		 */
		public boolean getDataTypeSupported()
		{
			return m_dataTypeSupported;
		}
		
		/**
		 * Unit of measure.
		 * @return Unit of measure.
		 */
		public String getUnitOfMeasure()
		{
			return m_unitOfMeasure;
		}
		
		/**
		 * The human-readable label of the column. Leave blank if not needed.
		 * @return The human-readable label of the column. Leave blank if not needed.
		 */
		public String getLabel()
		{
			return m_label;
		}
		
		/**
		 * The human-readable label of the column. Leave blank if not needed.
		 * @return The human-readable label of the column. Leave blank if not needed.
		 */
		public void setLabel(String l)
		{
			m_label = l;
		}
		
		/**
		 * The human-readable description of the column. Leave blank if not needed.
		 * @return The human-readable description of the column. Leave blank if not needed.
		 */
		public String getDescription()
		{
			return m_description;
		}
		
		/**
		 * The human-readable description of the column. Leave blank if not needed.
		 * @return The human-readable description of the column. Leave blank if not needed.
		 */
		public void setDescription(String d)
		{
			m_description = d;
		}
		
		/**
		 * Generates an XML proxy.
		 * @param factory Object factory.
		 * @return Proxy.
		 */
		Field toXmlProxy(ObjectFactory factory)
		{
			Field proxy = new Field();
			
			// Setting type
			JAXBElement<? extends AbstractDataComponentType> typeElement = getFieldForTyping(factory);
			proxy.setAbstractDataComponent(typeElement);
			
			// Setting name. This attribute is mandatory and must be a valid NCName.
			proxy.setName(m_name);
			
			// Setting label if defined
			if (m_label != null && !m_label.isEmpty())
			{
				typeElement.getValue().setLabel(m_label);
			}
			
			// Setting description if defined
			if (m_description != null && !m_description.isEmpty())
			{
				typeElement.getValue().setDescription(m_description);
			}
			
			return proxy;
		}
		
		private JAXBElement<? extends AbstractDataComponentType> getFieldForTyping(ObjectFactory factory)
		{
			if (typesAreEqual(m_dataType, Boolean.class))
			{
				return factory.createBoolean(new BooleanType());
			}
			else if (typesAreEqual(m_dataType, Item_TimeInstant.class))
			{
				TimeType time = new TimeType();
				
				// The XML schema requires this "uom" element here
				// (although this seems to be nonsense with a time value) 
				UnitReference unitRef = new UnitReference();
				time.setUom(unitRef);
				
				return factory.createTime(time);
			}
			else if (typesAreEqual(m_dataType, Double.class))
			{
				QuantityType quantity = new QuantityType();
				
				// Setting unit of measure if defined
				if (m_unitOfMeasure != null && !m_unitOfMeasure.isEmpty())
				{
					UnitReference unitRef = new UnitReference();
					unitRef.setCode(m_unitOfMeasure);
					quantity.setUom(unitRef);
				}
				
				return factory.createQuantity(quantity);
			}
			else if (typesAreEqual(m_dataType, Long.class))
			{
				return factory.createCount(new CountType());
			}
			else if (typesAreEqual(m_dataType, String.class))
			{
				return factory.createText(new TextType());
			}
			else
			{
				throw new RuntimeException("Unexpected column type " + m_dataType.getClass().getCanonicalName());
			}
		}
	}
}
