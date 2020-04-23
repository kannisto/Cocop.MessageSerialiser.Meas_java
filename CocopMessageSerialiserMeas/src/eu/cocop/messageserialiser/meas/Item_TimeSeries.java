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
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.XmlHelper;
import net.opengis.gml._3.CodeOrNilReasonListType;
import net.opengis.gml._3.DomainSetType;
import net.opengis.gml._3.MeasureOrNilReasonListType;
import net.opengis.gml._3.RangeSetType;
import net.opengis.gml._3.StringOrRefType;
import net.opengis.swe._2.DataRecordPropertyType;
import net.opengis.tsml._1.AnnotationCoveragePropertyType;
import net.opengis.tsml._1.AnnotationCoverageType;
import net.opengis.tsml._1.ObjectFactory;
import net.opengis.tsml._1.TimeseriesDomainRangeType;
import net.opengis.tsml._1.TimeseriesMetadataExtensionPropertyType;
import net.opengis.tsml._1.TimeseriesMetadataExtensionType;

/**
 * Base class for time series.
 * 
 * In this module, the code has been derived from TimeseriesML 1.0 - XML Encoding
 * of the Timeseries Profile of Observations and Measurements
 * (OGC 15-042r3; please see the file "ref_and_license_ogc_tsml.txt").
 * @see Item_Array
 * @author Petri Kannisto
 */
public abstract class Item_TimeSeries extends Item
{
	private final String m_unitOfMeasure;
	private final ArrayList<Double> m_values = new ArrayList<Double>();
    private final ArrayList<DataQuality> m_dataQualities = new ArrayList<DataQuality>();
    
    private String m_description;
	
	
    /**
     * Constructor.
     * @param typeUri Type URI.
     * @param uom Unit of measure.
     */
	protected Item_TimeSeries(String typeUri, String uom)
	{
		super(typeUri);

		m_unitOfMeasure = uom;
	}
	
	/**
	 * Constructor. Use this to populate the object from XML.
     * @param typeUri Type URI.
	 * @param proxy Proxy object.
	 * @throws InvalidMessageException Thrown if an error is encountered.
	 */
	protected Item_TimeSeries(String typeUri, TimeseriesDomainRangeType proxy) throws InvalidMessageException
	{
		super(typeUri);

		m_unitOfMeasure = readFieldValuesFromXmlDoc(proxy);
	}
	
	
	/// ### Public or protected methods ###
	
	/**
	 * The unit of measure.
	 * @return The unit of measure.
	 */
	public String getUnitOfMeasure()
	{
		return m_unitOfMeasure;
	}
	
	/**
	 * Count of values in the time series.
	 * @return Count of values in the time series.
	 */
	public int getValueCount()
	{
		return m_values.size();
	}
	
	/**
	 * Get the value in the given position.
	 * @param index Position.
	 * @return Value.
	 */
	public double getValue(int index)
	{
		return m_values.get(index);
	}
	
	/**
	 * Description.
	 * @return Description.
	 */
	public String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Set the description.
	 * @param d Description.
	 */
	public void setDescription(String d)
	{
		m_description = d;
	}
	
	/**
	 * Gets the data quality of the value in the given position.
	 * @param index Position.
	 * @return Data quality.
	 */
	public DataQuality getDataQuality(int index)
    {
        return m_dataQualities.get(index);
    }
	
	/**
	 * Adds a value.
	 * @param value Value.
	 * @param dq Data quality.
	 */
	protected void addValueBase(double value, DataQuality dq)
    {
        m_values.add(value);
        m_dataQualities.add(dq);
    }
	
	/**
	 * Adds subclass data to a proxy object. In the implementation, the subclass *must not*
	 * re-instantiate any proxy class members if they already exist, as it would erase
	 * the data set by the superclass.
	 * @param proxy Proxy object.
	 * @param uid Unique ID (for XML IDs).
	 */
	protected abstract void addSubclassDataToProxy(TimeseriesDomainRangeType proxy, String uid);
	
	@Override
	Object getObjectForXml_Result(String idPrefix)
	{
		return toXmlProxy(idPrefix);
	}
	
	// TODO: Support for time series as a field of data record
	
	
	/// ### Private methods ###
	
	private String readFieldValuesFromXmlDoc(TimeseriesDomainRangeType proxy) throws InvalidMessageException
    {
        String unitOfMeasure = null;
        String partName = "measurement values";

        try
        {
            // Reading description
            if (proxy.getDescription() != null)
            {
                m_description = proxy.getDescription().getValue();
            }

            JAXBElement<?> valueDataSet = proxy.getRangeSet().getAbstractScalarValueList().get(0);
            MeasureOrNilReasonListType measValues = (MeasureOrNilReasonListType)valueDataSet.getValue();

            // Reading the unit of measure
            unitOfMeasure = measValues.getUom();

            // Getting measurement values
            List<String> valuesRaw = measValues.getValue();
			
			for (String s : valuesRaw)
			{
				double d = XmlHelper.parseXmlDouble(s);
				m_values.add(d);
			}
            
			// Reading data qualities
			partName = "data qualities";
			
            /*
            The OGC example "timeseries-domain-range-example.xml" uses the gmlcov:metadata
            field instead of this. However, this approach is better typed (no extension
            elements used) and has one less of XML element layers.
            */
            // C# path: proxy.metadata1.TimeseriesMetadataExtension.annotation[0].AnnotationCoverage.rangeSet.CategoryList
			@SuppressWarnings("unchecked")
			JAXBElement<TimeseriesMetadataExtensionPropertyType> tsmlMetadata = (JAXBElement<TimeseriesMetadataExtensionPropertyType>)proxy.getRest().get(0);
			TimeseriesMetadataExtensionType timeSerMetadataExtension = tsmlMetadata.getValue().getTimeseriesMetadataExtension();
			AnnotationCoverageType annotationCov = timeSerMetadataExtension.getAnnotation().get(0).getAnnotationCoverage();
			JAXBElement<?> qualityListAlmost = annotationCov.getRangeSet().getAbstractScalarValueList().get(0);
			CodeOrNilReasonListType qualityList = (CodeOrNilReasonListType)qualityListAlmost.getValue();

            List<String> qualitiesRaw = qualityList.getValue();
			
			for (String s : qualitiesRaw)
			{
				m_dataQualities.add(new DataQuality(s));
			}
        }
        // Missing fields
        catch (IndexOutOfBoundsException e)
        {
            throw new InvalidMessageException("Failed to read " + partName + " from time series XML (required item missing?)", e);
        }
        // Missing fields
        catch (NullPointerException e)
        {
            throw new InvalidMessageException("Failed to read " + partName + " from time series XML (required item missing?)", e);
        }

        // Checking that the size of each collection matches
        if (m_dataQualities.size() != m_values.size())
        {
            throw new InvalidMessageException("The sizes of collections do not match in the XML document (something missing or too many)");
        }

        return unitOfMeasure;
    }

    private TimeseriesDomainRangeType toXmlProxy(String idPrefix)
    {
    	String myUniqueId = idPrefix + "TimeSer";
    	
    	// Initialising objects
		ObjectFactory objectFactoryTs = new ObjectFactory();
		net.opengis.gml._3.ObjectFactory objectFactoryGml = new net.opengis.gml._3.ObjectFactory();
		TimeseriesDomainRangeType timeseriesToMarshal = new TimeseriesDomainRangeType();
		timeseriesToMarshal.setId(myUniqueId);
		
		// Setting description (if set)
		if (m_description != null && m_description.length() > 0)
		{
			StringOrRefType desc = new StringOrRefType();
			desc.setValue(m_description);
			timeseriesToMarshal.setDescription(desc);
		}
		
		// Adding an empty domain set (required by the schema)
		DomainSetType domainSet = new DomainSetType();
		JAXBElement<DomainSetType> domainSetEl = objectFactoryGml.createDomainSet(domainSet);
		timeseriesToMarshal.setDomainSet(domainSetEl);
		
		// Adding an empty rangeType element as it is required
		DataRecordPropertyType emptyDataRecordProp = new DataRecordPropertyType();
		timeseriesToMarshal.setRangeType(emptyDataRecordProp);
		
		// 1) Adding values
		// rangeSet/QuantityList
		MeasureOrNilReasonListType measValues = new MeasureOrNilReasonListType();
		measValues.setUom(m_unitOfMeasure);
		
		for (double d : m_values)
		{
			String valueString = XmlHelper.serialiseXmlDouble(d);
			measValues.getValue().add(valueString);
		}
		
		// rangeSet
		RangeSetType rangeSetValues = new RangeSetType();
		JAXBElement<?> measValueEl = objectFactoryGml.createQuantityList(measValues);
		rangeSetValues.getAbstractScalarValueList().add(measValueEl);
		timeseriesToMarshal.setRangeSet(rangeSetValues);
		
		// 2) Adding data quality information
		// Codespace attr is required by the XML schema?
		CodeOrNilReasonListType qualityList = new CodeOrNilReasonListType();
		qualityList.setCodeSpace("http://cocop");
		
		for (DataQuality dq : m_dataQualities)
		{
			qualityList.getValue().add(dq.getValue());
		}
		
		// tsml:metadata/tsml:TimeseriesMetadataExtension/tsml:annotation/tsml:AnnotationCoverage/gml:rangeSet (/gml:CategoryList)
		JAXBElement<?> qualValueEl = objectFactoryGml.createCategoryList(qualityList);
		RangeSetType rangeSetQual = new RangeSetType();
		rangeSetQual.getAbstractScalarValueList().add(qualValueEl);
		
		// tsml:metadata/tsml:TimeseriesMetadataExtension/tsml:annotation/tsml:AnnotationCoverage
		AnnotationCoverageType annotationCoverage = new AnnotationCoverageType();
		annotationCoverage.setId(myUniqueId + "_qualCov"); // Required by the schema
		DomainSetType domainSetAnnotationCoverage = new DomainSetType();
		JAXBElement<DomainSetType> domainSetAnnotationCoverageEl = objectFactoryGml.createDomainSet(domainSetAnnotationCoverage);
		annotationCoverage.setDomainSet(domainSetAnnotationCoverageEl); // Required by the schema
		annotationCoverage.setRangeSet(rangeSetQual);
		DataRecordPropertyType emptyDataRecordProp_qual = new DataRecordPropertyType();
		annotationCoverage.setRangeType(emptyDataRecordProp_qual); // Required by the schema
		AnnotationCoveragePropertyType annotationCoverageProp = new AnnotationCoveragePropertyType();
		annotationCoverageProp.setAnnotationCoverage(annotationCoverage);
		
		// tsml:metadata/tsml:TimeseriesMetadataExtension
		TimeseriesMetadataExtensionType tsmlMetadataExt = new TimeseriesMetadataExtensionType();
		tsmlMetadataExt.getAnnotation().add(annotationCoverageProp);
		
		// tsml:metadata
		TimeseriesMetadataExtensionPropertyType timeseriesMetadataExtensionProp = new TimeseriesMetadataExtensionPropertyType();
		JAXBElement<TimeseriesMetadataExtensionPropertyType> metadataExtProp = objectFactoryTs.createTimeseriesDomainRangeTypeMetadata(timeseriesMetadataExtensionProp);
		metadataExtProp.getValue().setTimeseriesMetadataExtension(tsmlMetadataExt);
		timeseriesToMarshal.getRest().add(metadataExtProp);
		
		// Adding subclass data
		addSubclassDataToProxy(timeseriesToMarshal, myUniqueId);
		
		return timeseriesToMarshal;
    }
}
