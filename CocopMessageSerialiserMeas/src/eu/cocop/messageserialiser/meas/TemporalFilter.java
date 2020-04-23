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
import net.opengis.fes._2.BinaryTemporalOpType;
import net.opengis.fes._2.ObjectFactory;
import net.opengis.gml._3.AbstractTimeObjectType;
import net.opengis.gml._3.TimeInstantType;
import net.opengis.gml._3.TimePeriodType;

/**
 * A temporal filter to be utilised in requests.
 * 
 * In this module, the code has been derived from OGC Filter Encoding 2.0
 * Encoding Standard - With Corrigendum (OGC 09-026r2; please see the file
 * "ref_and_license_ogc_filter.txt") and OGC(r) Sensor Observation Service
 * Interface Standard (OGC 12-006; please see the file
 * "ref_and_license_ogc_sos.txt").
 * @author Petri Kannisto
 */
public final class TemporalFilter
{
	private static final String VALUEREF_PHENOMENONTIME = "phenomenonTime";
	private static final String VALUEREF_RESULTTIME = "resultTime";
	
	
	/**
	 * Indicates which value is being referred to.
	 * @author Petri Kannisto
	 */
	public enum ValueReferenceType
    {
		PhenomenonTime,
        ResultTime
    }
	
	/**
	 * Operators.
	 * @author Petri Kannisto
	 */
	public enum OperatorType
	{
		After,
		Before,
		During
	}
	
	private ValueReferenceType m_valueReference;
	private OperatorType m_operator;
	private Item m_time;
	
	
	/**
	 * Constructor.
	 * @param valRef Reference to the value that this filter refers to.
	 * @param op The operator to be applied.
	 * @param time The definition of time. Depending on the operator, 
	 * this must be either a time instant or a time range.
	 */
	public TemporalFilter(ValueReferenceType valRef, OperatorType op, Item time)
	{
		checkConsistency(op, time);
		
		m_valueReference = valRef;
        m_operator = op;
        m_time = time;
	}
	
	TemporalFilter(net.opengis.sos._2.GetObservationType.TemporalFilter proxy) throws InvalidMessageException
	{
		try
		{
			BinaryTemporalOpType binaryOp = (BinaryTemporalOpType)proxy.getTemporalOps().getValue();
			
			// Recognising the operator
			String operatorRaw = proxy.getTemporalOps().getName().getLocalPart();
			m_operator = OperatorType.valueOf(operatorRaw);
			
			// Getting value reference
			@SuppressWarnings("unchecked")
			JAXBElement<String> valueRefNode = (JAXBElement<String>)binaryOp.getExpressionOrAny().get(0);
			String valueRefRaw = (String)valueRefNode.getValue();
			m_valueReference = parseValueReference(valueRefRaw);
			
			@SuppressWarnings("unchecked")
			JAXBElement<Object> timeObjectNode = (JAXBElement<Object>)binaryOp.getExpressionOrAny().get(1);
			
			// Getting time object; this will cause an exception if the time object has an invalid type
			m_time = getTimeObjectFromProxy(m_operator, timeObjectNode.getValue());
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Unexpected contents in temporal filter", e);
		}
		catch (IllegalArgumentException e)
		{
			// At least Enum.valueOf may throw this
			throw new InvalidMessageException("Unexpected contents in temporal filter", e);
		}
		catch (NullPointerException e)
		{
			throw new InvalidMessageException("Something missing from temporal filter", e);
		}
	}
	
	/**
	 * Reference to the value that this filter refers to.
	 * @return Reference to the value that this filter refers to.
	 */
	public ValueReferenceType getValueReference()
	{
		return m_valueReference;
	}
	
	/**
	 * The operator to be applied.
	 * @return The operator to be applied.
	 */
	public OperatorType getOperator()
	{
		return m_operator;
	}
	
	/**
	 * The definition of time. Depending on the operator, this must 
	 * be either a time instant or a time range.
	 * @return Time.
	 */
	public Item getTime()
	{
		return m_time;
	}

	/**
	 * Generates an XML proxy.
	 * @param idPrefix ID prefix to enable the generation of unique IDs within the document.
	 * @return Proxy.
	 */
	net.opengis.sos._2.GetObservationType.TemporalFilter toXmlProxy(String idPrefix)
	{
		net.opengis.sos._2.GetObservationType.TemporalFilter proxy = new net.opengis.sos._2.GetObservationType.TemporalFilter();
        String idPrefixAll = idPrefix + "TempF_";
        ObjectFactory objectFactoryFes = new ObjectFactory();
        net.opengis.gml._3.ObjectFactory objectFactoryGml = new net.opengis.gml._3.ObjectFactory();
        
        // Creating temporal operator element
        BinaryTemporalOpType temporalOpElement = new BinaryTemporalOpType();
        
        // Adding value reference
        String valueRefString = valueReferenceToString(m_valueReference);
        JAXBElement<String> valueReferenceElement = objectFactoryFes.createValueReference(valueRefString);
        temporalOpElement.getExpressionOrAny().add(valueReferenceElement);
        
        // Adding time object
        JAXBElement<? extends AbstractTimeObjectType> timeObjElement = getTimeProxyFromItem(m_time, idPrefixAll, objectFactoryGml);
        temporalOpElement.getExpressionOrAny().add(timeObjElement);
        
        // Creating element for the temporal operator
        JAXBElement<BinaryTemporalOpType> conditionElement = null;
        
        switch (m_operator)
        {
            case After:
            	conditionElement = objectFactoryFes.createAfter(temporalOpElement);
            	break;

            case Before:
            	conditionElement = objectFactoryFes.createBefore(temporalOpElement);
            	break;

            case During:
            	conditionElement = objectFactoryFes.createDuring(temporalOpElement);
            	break;

            default:
                throw new IllegalArgumentException("Unsupported operator " + m_operator.toString());
        }
        
        proxy.setTemporalOps(conditionElement);

        return proxy;
	}
	
	private JAXBElement<? extends AbstractTimeObjectType> getTimeProxyFromItem(Item item, String idPrefix, net.opengis.gml._3.ObjectFactory objectFactoryGml)
	{
		if (item instanceof Item_TimeInstant)
		{
			Item_TimeInstant instant = (Item_TimeInstant)item;
			TimeInstantType proxy = instant.toXmlProxy(idPrefix);
			return objectFactoryGml.createTimeInstant(proxy);
		}
		else if (item instanceof Item_TimeRange)
		{
			Item_TimeRange range = (Item_TimeRange)item;
			TimePeriodType proxy = range.toXmlProxy(idPrefix);
			return objectFactoryGml.createTimePeriod(proxy);
		}
		else
		{
			throw new IllegalArgumentException("Unexpected type of time item in filter: " + item.getClass().getName());
		}
	}
	
	private ValueReferenceType parseValueReference(String s)
	{
		if (s.equals(VALUEREF_PHENOMENONTIME))
			return ValueReferenceType.PhenomenonTime;
		
		else if (s.equals(VALUEREF_RESULTTIME))
			return ValueReferenceType.ResultTime;
		
		else
			throw new IllegalArgumentException("Unexpected value reference \"" + s + "\"");
	}
	
	private String valueReferenceToString(ValueReferenceType valRef)
	{
		switch (valRef)
		{
		case PhenomenonTime:
			return VALUEREF_PHENOMENONTIME;
		case ResultTime:
			return VALUEREF_RESULTTIME;
		default:
			throw new IllegalArgumentException("Unexpected value reference " + valRef.toString());
		}
	}
	
	private Item getTimeObjectFromProxy(OperatorType op, Object timeObject) throws InvalidMessageException
	{
		try
		{
			switch (op)
	        {
	            case After:
	            case Before:
	            	return new Item_TimeInstant((TimeInstantType)timeObject);
	            	
	            case During:
	            	return new Item_TimeRange((TimePeriodType)timeObject);
	            	
	        	default:
	        		throw new IllegalArgumentException("Unexpected operator " + op.toString());
	        }
		}
		catch (ClassCastException e)
		{
			throw new InvalidMessageException("Unexpected type of time object in temporal filter", e);
		}
	}
	
	private void checkConsistency(OperatorType op, Item time)
    {
        // Checking the type of the time item
        switch (op)
        {
            case After:
            case Before:

                if (!(time instanceof Item_TimeInstant))
                {
                    throw new IllegalArgumentException("Only time instants are compatible with operator " + op.toString());
                }

                break;

            case During:

                if (!(time instanceof Item_TimeRange))
                {
                    throw new IllegalArgumentException("Only time ranges are compatible with operator " + op.toString());
                }

                break;

            default:
                throw new IllegalArgumentException("There is no support for operator " + op.toString());
        }
    }
}
