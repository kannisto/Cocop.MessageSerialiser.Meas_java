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
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.BooleanType;


/**
 * Represents a boolean value.
 * 
 * In this module, the code has been derived from OGC(r) SWE Common Data Model
 * Encoding Standard (OGC 08-094r1; please see the file
 * "ref_and_license_ogc_swecommon.txt").
 * @see Item_Category
 * @author Petri Kannisto
 */
public final class Item_Boolean extends Item
{
	private final boolean m_value;
	
	
	/**
	 * Constructor.
	 * @param v Value.
	 */
	public Item_Boolean(boolean v)
	{
		super(XmlHelper.TYPEURI_TRUTH);
		
		m_value = v;
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (data record field).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_Boolean(BooleanType el) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_TRUTH);
		
		m_value = el.isValue();
	}
	
	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		return m_value;
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		BooleanType booleanItem = new BooleanType();
		booleanItem.setValue(m_value);
		
		return fact.createBoolean(booleanItem);
	}
	
	
	/**
	 * Gets the truth value.
	 * @return Truth value.
	 */
	public boolean getValue()
	{
		return m_value;
	}
}
