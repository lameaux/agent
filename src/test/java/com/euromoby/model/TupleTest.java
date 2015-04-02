package com.euromoby.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.euromoby.model.Tuple;

public class TupleTest {

	@Test
	public void testEmpty() {
		Tuple<String, String> t = Tuple.empty();
		assertNull(t.getFirst());
		assertNull(t.getSecond());		
	}

	@Test
	public void testFull() {
		String f = "f";
		String s = "s";		
		Tuple<String, String> t = Tuple.of(f,s);
		assertEquals(f, t.getFirst());
		assertEquals(s, t.getSecond());		
	}
	
}
