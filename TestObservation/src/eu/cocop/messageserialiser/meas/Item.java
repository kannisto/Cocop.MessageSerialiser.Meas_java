//
// Please make sure to read and understand the files README.md and LICENSE.txt.
// 
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
//
// Author: Petri Kannisto, Tampere University, Finland
// File created: 2019
// Last modified: 4/2020

package eu.cocop.messageserialiser.meas;

import javax.xml.bind.JAXBElement;

import net.opengis.swe._2.AbstractDataComponentType;

public abstract class Item
{
	// Stub class
	
	private final String m_typeUri;
	
	
	public Item(String typeUri)
	{
		m_typeUri = typeUri;
	}
	
	public String getObservationTypeUri()
	{
		return m_typeUri;
	}
	
	boolean supportsDataQualityInDataRecord()
	{
		return true;
	}
	
	Object getObjectForXml_Result(String s)
	{
		throw new RuntimeException("Not implemented");
	}
	
	JAXBElement<? extends AbstractDataComponentType> getObjectForXml_DataRecordField(net.opengis.swe._2.ObjectFactory fact)
	{
		throw new RuntimeException("Not implemented");
	}
}
