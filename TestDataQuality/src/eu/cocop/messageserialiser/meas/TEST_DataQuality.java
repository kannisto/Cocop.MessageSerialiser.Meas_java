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

import static org.junit.Assert.*;

import org.junit.Test;

public class TEST_DataQuality
{
	@Test
	public void dq_10_BasicGoodness()
	{
		// Test basic good and bad qualities
		
		DataQuality good = DataQuality.createGood();
		DataQuality bad = DataQuality.createBad();
		
		assertTrue(good.isGood());
		assertFalse(bad.isGood());
	}
	
	@Test
	public void dq_20_CustomBad() throws Exception
	{
		// Test bad qualities with a custom reason
		
		DataQuality customBad1 = DataQuality.createBad("myreason");
		DataQuality customBad2 = DataQuality.createBad("myreason/subreason");
		
		assertFalse(customBad1.isGood());
		assertFalse(customBad2.isGood());
	}
	
	@Test
	public void dq_30_CreateFromString()
	{
		// Test creation for a raw URI
		
		String value1 = "good";
		String value2 = "bad";
		String value3 = "bad/justreason";
		String unexpected1 = "baad/myreason";
		
		DataQuality fromUri1 = new DataQuality(value1);
		DataQuality fromUri2 = new DataQuality(value2);
		DataQuality fromUri3 = new DataQuality(value3);
		
		assertTrue(fromUri1.isGood());
		assertFalse(fromUri2.isGood());
		assertFalse(fromUri3.isGood());
		
		// Unexpected URI
		try {
			new DataQuality(unexpected1);
			fail("Expected an exception");
		} catch (Exception ignore) {}
	}
}
