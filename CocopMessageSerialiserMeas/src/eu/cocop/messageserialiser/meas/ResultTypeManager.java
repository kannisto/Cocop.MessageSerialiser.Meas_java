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

import java.math.BigInteger;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.gml._3.MeasureType;
import net.opengis.gml._3.ReferenceType;
import net.opengis.gml._3.TimeInstantPropertyType;
import net.opengis.gml._3.TimePeriodPropertyType;
import net.opengis.swe._2.DataArrayType;
import net.opengis.swe._2.DataRecordPropertyType;
import net.opengis.tsml._1.TimeseriesDomainRangeType;

/**
 * A class to manage result typing. It was implemented to facilitate the testing of 
 * Item_* classes, as it also needs this functionality.
 * 
 * This class is tested in Item_* tests.
 *  
 * This functionality could be located in the Item class, but that would make
 * the Item class dependent on its subclasses, which would be bad design
 * (bi-directional dependencies).
 * @author Petri Kannisto
 */
class ResultTypeManager
{
	/**
	 * Builds a result object according to the given result type.
	 * @param obsType Result type.
	 * @param result Raw result object from XML.
	 * @return Result object.
	 * @throws ClassCastException Thrown if a typing conflict occurs.
	 * @throws InvalidMessageException Thrown if a message-related error occurs.
	 */
	static Item buildResultFromXml(String obsType, Object result) throws ClassCastException, InvalidMessageException
	{
		// This function is here, not in Observation class, to facilitate testing
		
		if (obsType.equals(XmlHelper.TYPEURI_TRUTH))
		{
			return new Item_Boolean((boolean)result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_CATEGORY))
		{
			return new Item_Category((ReferenceType)result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_COMPLEX))
		{
			return buildComplex(result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_COUNT))
		{
			return new Item_Count((BigInteger)result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_MEASUREMENT))
		{
			return new Item_Measurement((MeasureType)result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_TIMESERIESCONSTANT))
		{
			return new Item_TimeSeriesConstant((TimeseriesDomainRangeType)result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_TIMESERIESFLEXIBLE))
		{
			return new Item_TimeSeriesFlexible((TimeseriesDomainRangeType)result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_TEMPORAL))
		{
			return buildTemporal(result);
		}
		else if (obsType.equals(XmlHelper.TYPEURI_TEXT))
		{
			return new Item_Text((String)result);
		}
		else
		{
			throw new RuntimeException("No support implemented for type \"" + obsType + "\"");
		}
	}
	
	private static Item buildComplex(Object result) throws InvalidMessageException
	{
		if (result instanceof DataRecordPropertyType)
		{
			return new Item_DataRecord((DataRecordPropertyType)result);
		}
		else if (result instanceof DataArrayType)
		{
			return new Item_Array((DataArrayType)result);
		}
		else
		{
			throw new InvalidMessageException("Unexpected result type in complex observation");
		}
	}
	
	private static Item buildTemporal(Object result) throws InvalidMessageException
	{
		if (result instanceof TimeInstantPropertyType)
		{
			return new Item_TimeInstant((TimeInstantPropertyType)result);
		}
		else if (result instanceof TimePeriodPropertyType)
		{
			return new Item_TimeRange((TimePeriodPropertyType)result);
		}
		else
		{
			throw new InvalidMessageException("Unexpected result type in temporal observation");
		}
	}
}
