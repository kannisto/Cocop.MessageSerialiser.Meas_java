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

import javax.xml.bind.JAXBElement;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.gml._3.DomainSetType;
import net.opengis.tsml._1.ObjectFactory;
import net.opengis.tsml._1.TimePositionListType;
import net.opengis.tsml._1.TimeseriesDomainRangeType;

/**
 * A time series of measurement values. In this time series type, there 
 * is no requirement for  constant time intervals between measurement points. 
 * Please note that this implementation does not support this type in an
 * {@link Item_DataRecord}.
 * 
 * In this module, the code has been derived from TimeseriesML 1.0 - XML Encoding
 * of the Timeseries Profile of Observations and Measurements
 * (OGC 15-042r3; please see the file "ref_and_license_ogc_tsml.txt").
 * @see Item_Array
 * @see Item_TimeSeriesConstant
 * @author Petri Kannisto
 */
public final class Item_TimeSeriesFlexible extends Item_TimeSeries
{
	private ArrayList<Item_TimeInstant> m_timestamps = new ArrayList<>();
	
	
	/**
	 * Constructor. Use this for manual population.
	 * @param uom Unit of measure.
	 */
	public Item_TimeSeriesFlexible(String uom)
	{
		super(XmlHelper.TYPEURI_TIMESERIESFLEXIBLE, uom);
		
		// Empty ctor body
	}
	
	/**
	 * Constructor. Use to populate from XML proxy.
	 * @param proxy Proxy.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeSeriesFlexible(TimeseriesDomainRangeType proxy) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_TIMESERIESFLEXIBLE, proxy);
		
		readFieldValuesFromXmlDoc(proxy);
	}

	@Override
	protected void addSubclassDataToProxy(TimeseriesDomainRangeType proxy, String uid)
	{
		// Initialising objects
		ObjectFactory objectFactoryTs = new ObjectFactory();
		net.opengis.gml._3.ObjectFactory objectFactoryGml = new net.opengis.gml._3.ObjectFactory();
		
		// Checking in case the base class has already added the domain set element
		if (proxy.getDomainSet() == null)
		{
			// domainSet
			DomainSetType domainSet = new DomainSetType();
			JAXBElement<DomainSetType> domainSetEl = objectFactoryGml.createDomainSet(domainSet);
			proxy.setDomainSet(domainSetEl);
		}
		
		// 1) Adding time positions
		// domainSet/TimePositionList
		TimePositionListType timePositionList = new TimePositionListType();
		timePositionList.setId("timestamps"); // Required by the schema
		
		for (Item_TimeInstant dt : m_timestamps)
		{
			timePositionList.getTimePositionList().add(dt.toXsdDateTime());
		}
		
		JAXBElement<TimePositionListType> timePositionListEl = objectFactoryTs.createTimePositionList(timePositionList);
		proxy.getDomainSet().getValue().setAbstractTimeObject(timePositionListEl);
	}
	
	/**
	 * Adds a value to the time series.
	 * @param dt Timestamp.
	 * @param value Value.
	 * @param dq Data quality of the value.
	 */
	public void addValue(Item_TimeInstant dt, double value, DataQuality dq)
	{
		super.addValueBase(value, dq);
		m_timestamps.add(dt);
	}
	
	/**
	 * Adds a value to the time series. This method assumes that 
	 * the quality of the value is "good".
	 * @param dt Timestamp.
	 * @param value Value.
	 */
	public void addValue(Item_TimeInstant dt, double value)
	{
		addValue(dt, value, DataQuality.createGood());
	}
	
	/**
	 * Gets a timestamp.
	 * @param index Timestamp index.
	 * @return Timestamp.
	 */
	public Item_TimeInstant getTimestamp(int index)
	{
		return m_timestamps.get(index);
	}
	
	private void readFieldValuesFromXmlDoc(TimeseriesDomainRangeType proxy) throws InvalidMessageException
	{
		String phase = "measurement values";
		
		try
		{
			// Reading timestamps
			phase = "timestamps";
			DomainSetType domainSet = proxy.getDomainSet().getValue();
			TimePositionListType timePositionList = (TimePositionListType)domainSet.getAbstractTimeObject().getValue();
			
			for (String xmlDateTime : timePositionList.getTimePositionList())
			{
				Item_TimeInstant instant = new Item_TimeInstant(xmlDateTime);
				m_timestamps.add(instant);
			}
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException("Failed to read " + phase +  " from time series", e);
		}
		
		// Checking that the size of each collection matches
		if (m_timestamps.size() != getValueCount())
		{
			throw new InvalidMessageException("The sizes of series do not match");
		}
	}
}
