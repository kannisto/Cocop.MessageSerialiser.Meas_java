//
// Please make sure to read and understand the files README.md and LICENSE.txt.
// 
// This file was prepared in the research project COCOP (Coordinating
// Optimisation of Complex Industrial Processes).
// https://cocop-spire.eu/
//
// Author: Petri Kannisto, Tampere University, Finland
// File created: 10/2018
// Last modified: 4/2020

package jartest;

import eu.cocop.messageserialiser.meas.InvalidMessageException;
import eu.cocop.messageserialiser.meas.Item_Measurement;
import eu.cocop.messageserialiser.meas.Observation;

/**
 * This application was created to test if the JAR export of Cocop.MessageSerialiser.Meas has succeeded.
 * @author Petri Kannisto
 */
public class JarTestProgram
{
	public static void main(String[] args) throws InvalidMessageException
	{
		// The default transformer factory has caused issues in Matlab.
		// In Matlab, you may have to explicitly set the transformer.
		String propName = "javax.xml.transform.TransformerFactory";
		System.out.println(propName + " is:");
		// If this prints "null", there is no explicit setting
		System.out.println(System.getProperty(propName));
		
		Item_Measurement meas1 = new Item_Measurement("t", 3.6);
		Observation obs1 = new Observation(meas1);
		
		// Serialising and deserialising
		Observation obs2 = new Observation(obs1.toXmlBytes());
		Item_Measurement meas2 = (Item_Measurement)obs2.getResult();
		
		System.out.println(meas2.getValue() + " " + meas2.getUnitOfMeasure());
		System.out.println("Serialisation with the JAR seems to have succeeded as expected. Great!");
	}
}
