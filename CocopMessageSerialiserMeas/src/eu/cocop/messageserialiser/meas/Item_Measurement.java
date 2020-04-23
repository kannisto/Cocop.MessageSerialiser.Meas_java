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

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.gml._3.MeasureType;
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.QuantityType;
import net.opengis.swe._2.UnitReference;


/**
 * A numeric measurement value that has a unit of measure.
 * 
 * In this module, the code has been derived from OpenGIS(r)
 * Geography Markup Language (GML) Encoding Standard 
 * (OGC 07-036; please see the file "ref_and_license_ogc_gml.txt") and
 * OGC(r) SWE Common Data Model Encoding Standard
 * (OGC 08-094r1; please see the file "ref_and_license_ogc_swecommon.txt").
 * @see Item_Count
 * @author Petri Kannisto
 */
public final class Item_Measurement extends Item
{
	private final String m_unitOfMeasure;
	private final double m_value;
	
	
	/**
	 * Constructor.
	 * @param u Unit of measure.
	 * @param v Value.
	 */
	public Item_Measurement(String u, double v)
	{
		super(XmlHelper.TYPEURI_MEASUREMENT);
		
		m_unitOfMeasure = u;
		m_value = v;
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (observation result).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_Measurement(MeasureType el) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_MEASUREMENT);
		
		try
		{
			m_unitOfMeasure = el.getUom();
			m_value = el.getValue();
		}
		catch (Exception e)
		{
			throw new InvalidMessageException(e.getMessage(), e);
		}
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (data record field).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_Measurement(QuantityType el) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_MEASUREMENT);
		
		try
		{
			m_unitOfMeasure = el.getUom().getCode();
			m_value = el.getValue();
		}
		catch (Exception e)
		{
			throw new InvalidMessageException(e.getMessage(), e);
		}
	}
	

	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		MeasureType retval = new MeasureType();
		retval.setUom(m_unitOfMeasure);
		retval.setValue(m_value);
		
		return retval;
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		QuantityType quantity = new QuantityType();
		
		// Unit
		UnitReference unitRef = new UnitReference();
		unitRef.setCode(m_unitOfMeasure);
		quantity.setUom(unitRef);
		
		// Value
		quantity.setValue(m_value);
		
		return fact.createQuantity(quantity);
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
	 * Measurement value.
	 * @return Measurement value.
	 */
	public double getValue()
	{
		return m_value;
	}
}
