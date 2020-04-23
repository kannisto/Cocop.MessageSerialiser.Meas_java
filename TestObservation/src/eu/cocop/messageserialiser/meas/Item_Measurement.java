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

import net.opengis.gml._3.MeasureType;

public class Item_Measurement extends Item
{
	// Stub class
	
	public Item_Measurement(Object o)
	{
		super(XmlHelper.TYPEURI_MEASUREMENT);
	}
	
	@Override
	Object getObjectForXml_Result(String s)
	{
		MeasureType retval = new MeasureType();
		retval.setUom("Cel");
		retval.setValue(45.1);
		
		return retval;
	}
}
