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
import javax.xml.datatype.Duration;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.joda.time.Period;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.gml._3.TimePositionType;
import net.opengis.tsml._1.TimeseriesDomainRangeType;
import net.opengis.tsml._1.TimeseriesMetadataExtensionPropertyType;
import net.opengis.tsml._1.TimeseriesMetadataExtensionType;
import net.opengis.tsml._1.TimeseriesMetadataExtensionType.TimeseriesMetadata;
import net.opengis.tsml._1.TimeseriesMetadataType;

/**
 * A class to serialise time series with a constant sampling interval.
 * Please note that this implementation does not support this type in an
 * {@link Item_DataRecord}.
 * 
 * In this module, the code has been derived from TimeseriesML 1.0 - XML Encoding
 * of the Timeseries Profile of Observations and Measurements
 * (OGC 15-042r3; please see the file "ref_and_license_ogc_tsml.txt").
 * @see Item_Array
 * @see Item_TimeSeriesFlexible
 * @author Petri Kannisto
 */
public final class Item_TimeSeriesConstant extends Item_TimeSeries
{
	private Item_TimeInstant m_baseTime = null;
	private Period m_spacing = null;
	
	
	/**
	 * Constructor.
	 * @param uom Unit of measure.
	 * @param baseTime The base time of the time series (i.e., the time of the first sample).
	 * @param spacing The constant time interval between samples.
	 */
	public Item_TimeSeriesConstant(String uom, Item_TimeInstant baseTime, Period spacing)
	{
		super(XmlHelper.TYPEURI_TIMESERIESCONSTANT, uom);
		
		m_baseTime = baseTime;
		m_spacing = spacing;
	}
	
	/**
	 * Constructor. Use this to populate an object from XML.
	 * @param proxy Proxy.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	Item_TimeSeriesConstant(TimeseriesDomainRangeType proxy) throws InvalidMessageException
	{
		super(XmlHelper.TYPEURI_TIMESERIESCONSTANT, proxy);
		
		readDataFromProxy(proxy);
	}

	@Override
	protected void addSubclassDataToProxy(TimeseriesDomainRangeType proxy, String uid)
	{
		// Assuming that the base class has already added tsml:metadata/tsml:TimeseriesMetadataExtension
		TimeseriesMetadataExtensionType timeSerMetadataExtension = getMetadataExtension(proxy);
		
		// Adding the actual metadata elements
		// tsml:metadata/tsml:TimeseriesMetadataExtension/tsml:timeseriesMetadata/tsml:TimeseriesMetadata
		TimeseriesMetadata metadataOut = new TimeseriesMetadata();
		timeSerMetadataExtension.setTimeseriesMetadata(metadataOut);
		TimeseriesMetadataType metadataIn = new TimeseriesMetadataType();
		metadataOut.setTimeseriesMetadata(metadataIn);
		
		// Setting base time
		TimePositionType baseTimePos = new TimePositionType();
		baseTimePos.getValue().add(m_baseTime.toXsdDateTime());
		metadataIn.setBaseTime(baseTimePos);
		
		try
		{
			// Setting spacing
			Duration spacingDuration = DatatypeFactory.newInstance().newDuration(XmlHelper.serialiseXmlPeriod(m_spacing));
			metadataIn.setSpacing(spacingDuration);
		}
		catch (DatatypeConfigurationException e)
		{
			// DatatypeConfigurationException is expected only if something funny happens
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The base time of the time series (i.e., the time of the first sample).
	 * @return The base time of the time series (i.e., the time of the first sample).
	 */
	public Item_TimeInstant getBaseTime()
	{
		return m_baseTime;
	}
	
	/**
	 * The constant time interval between samples.
	 * @return The constant time interval between samples.
	 */
	public Period getSpacing()
	{
		return m_spacing;
	}
	
	/**
	 * Adds a value.
	 * @param v Value.
	 * @param dq Data quality of the value.
	 */
	public void addValue(double v, DataQuality dq)
	{
		addValueBase(v, dq);
	}
	
	/**
	 * Adds a value. A "good" data quality is expected.
	 * @param v Value.
	 */
	public void addValue(double v)
	{
		addValue(v, DataQuality.createGood());
	}
	
	private void readDataFromProxy(TimeseriesDomainRangeType proxy) throws InvalidMessageException
	{
		String errorMsg = "Failed to read the data of constant-interval time series";

        try
        {
        	// Getting metadata extension element
        	// tsml:metadata/tsml:TimeseriesMetadataExtension
    		TimeseriesMetadataExtensionType timeSerMetadataExtension = getMetadataExtension(proxy);
        	
    		// tsml:metadata/tsml:TimeseriesMetadataExtension/tsml:timeseriesMetadata/tsml:TimeseriesMetadata
    		TimeseriesMetadataType actualMetadata = timeSerMetadataExtension.getTimeseriesMetadata().getTimeseriesMetadata();
    		
    		m_baseTime = new Item_TimeInstant(actualMetadata.getBaseTime());
    		m_spacing = XmlHelper.parseXmlPeriod(actualMetadata.getSpacing().toString());
        }
        // Missing fields
        catch (IndexOutOfBoundsException e)
        {
            throw new InvalidMessageException(errorMsg + " (required item missing?)", e);
        }
        // Missing fields
        catch (NullPointerException e)
        {
            throw new InvalidMessageException(errorMsg + " (required item missing?)", e);
        }
        // Datetime values, timespan values
        catch (IllegalArgumentException e)
        {
            throw new InvalidMessageException(errorMsg + " (invalid value formatting?)", e);
        }
	}
	
	private TimeseriesMetadataExtensionType getMetadataExtension(TimeseriesDomainRangeType proxy)
	{
		// Getting metadata extension element
    	// tsml:metadata/tsml:TimeseriesMetadataExtension
		@SuppressWarnings("unchecked")
		JAXBElement<TimeseriesMetadataExtensionPropertyType> tsmlMetadata = (JAXBElement<TimeseriesMetadataExtensionPropertyType>)proxy.getRest().get(0);
		return tsmlMetadata.getValue().getTimeseriesMetadataExtension();
	}
}
