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

import javax.xml.bind.JAXBElement;

import net.opengis.swe._2.AbstractDataComponentType;

/**
 * Abstract base class for items.
 * @author Petri Kannisto
 */
public abstract class Item
{
	private final String m_observationTypeUri;
	
	/**
	 * Constructor.
	 * @param typ The URI of the associated observation type. 
	 */
	protected Item(String typ)
	{
		m_observationTypeUri = typ;
	}
	
	
	/**
	 * The URI of the associated observation type.
	 * @return Type URI.
	 */
	String getObservationTypeUri()
	{
		return m_observationTypeUri;
	}
	
	/**
	 * Whether the item type supports data quality in a data record.
	 * The default is true; override this function if the type does not support data quality in a data record.
	 * The property exists to enable the discovery of conflicts as early as possible.
	 * Otherwise, the error of associating data quality would appear only just before serialisation.
	 * @return True if supported, otherwise false.
	 */
	protected boolean supportsDataQualityInDataRecord()
	{
		return true;
	}
	
	/**
	 * Returns the object for result marshalling. The base class has no proper implementation,
	 * but this method shall be overridden in sub-classes as needed.
	 * @param idPrefix String to be utilised to create IDs that are unique within XML documents.
	 * @return Object.
	 */
	Object getObjectForXml_Result(String idPrefix)
	{
		throw new RuntimeException("The type does not support serialisation as an observation result");
	}
	
	/**
	 * Returns the object for data record marshalling. The base class has no proper implementation,
	 * but this method shall be overridden in sub-classes as needed.
	 * @return Object.
	 */
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		throw new RuntimeException("The type does not support serialisation as a data record field");
	}
}
