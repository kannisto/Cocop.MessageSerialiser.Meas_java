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
import net.opengis.gml._3.ReferenceType;
import net.opengis.swe._2.AbstractDataComponentType;
import net.opengis.swe._2.CategoryType;

/**
 * Represents a category value.
 * 
 * In this module, the code has been derived from OpenGIS(r)
 * Geography Markup Language (GML) Encoding Standard 
 * (OGC 07-036; please see the file "ref_and_license_ogc_gml.txt") and
 * OGC(r) SWE Common Data Model Encoding Standard
 * (OGC 08-094r1; please see the file "ref_and_license_ogc_swecommon.txt").
 * @see Item_Boolean
 * @see Item_Text
 * @author Petri Kannisto
 */
public final class Item_Category extends Item
{
	private final String m_categoryReference;
	
	
	/**
	 * Constructor.
	 * @param categ Category.
	 */
	public Item_Category(String categ)
	{
		super(XmlHelper.TYPEURI_CATEGORY);
		
		m_categoryReference = categ;
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (observation result).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_Category(ReferenceType el) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_CATEGORY);
		
		m_categoryReference = el.getTitleAttr();
	}
	
	/**
	 * Constructor. Use this to instantiate an item from XML (data record field).
	 * @param el XML data.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_Category(CategoryType el) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_CATEGORY);
		
		m_categoryReference = el.getValue();
	}

	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		ReferenceType refObj = new ReferenceType();
		refObj.setTitleAttr(m_categoryReference);
		return refObj;
	}
	
	@Override
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		CategoryType ctgItem = new CategoryType();
		ctgItem.setValue(m_categoryReference);
		
		return fact.createCategory(ctgItem);
	}
	
	
	/**
	 * The category value.
	 * @return The category value.
	 */
	public String getValue()
	{
		return m_categoryReference;
	}
}
