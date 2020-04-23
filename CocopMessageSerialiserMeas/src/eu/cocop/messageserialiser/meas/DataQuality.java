//
// Please make sure to read and understand the files README.md and LICENSE.txt.
// 
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
//
// Author: Petri Kannisto, Tampere University, Finland
// File created: 3/2018
// Last modified: 4/2020

package eu.cocop.messageserialiser.meas;

/**
 * Represents observation quality information.
 * @author Petri Kannisto
 */
public final class DataQuality
{
	/**
	 * The URI of good data quality.
	 */
	public static final String GOOD = "good";
	
	/**
	 * The URI of bad data quality.
	 */
	public static final String BAD = "bad";
	
	
	private final String m_value;
	
	
	/**
	 * Constructor.
	 * @param input String that refers to data quality.
	 */
	DataQuality(String input)
	{
		if (!input.startsWith(GOOD) && !input.startsWith(BAD))
		{
			throw new IllegalArgumentException("Cannot interpret data quality value \"" + input + "\"");
		}
		
		m_value = input;
	}
	
	/**
	 * Creates a data quality object with the value "good".
	 * @return New instance.
	 */
	public static DataQuality createGood()
	{
		return new DataQuality(GOOD);
	}
	
	/**
	 * Creates a data quality object with the value "bad".
	 * @return New instance.
	 */
	public static DataQuality createBad()
	{
		return new DataQuality(BAD);
	}
	
	/**
	 * Creates a data quality object with the value "bad" and additional information why the value is bad.
	 * @param reason Reason why the quality is bad. 
	 * @return New instance.
	 */
	public static DataQuality createBad(String reason)
	{
		return new DataQuality(BAD + "/" + reason);
	}
	
	/**
	 * Whether data quality is good.
	 * @return True if good, otherwise false.
	 */
	public boolean isGood()
	{
		return m_value.startsWith(GOOD);
	}
	
	/**
	 * The value that encloses data quality information.
	 * @return Value.
	 */
	public String getValue()
	{
		return m_value;
	}
	
	@Override
	public String toString()
	{
		return getValue();
	}
}
