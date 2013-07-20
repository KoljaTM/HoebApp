package de.vanmar.android.hoebapp.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class HtmlMockAnswer implements Answer<InputStream> {

	@Override
	public InputStream answer(final InvocationOnMock invocation)
			throws Throwable {
		final String filename = (String) invocation.getArguments()[0];
		final File file = new File("../HoebApp-test/assets/" + filename);
		assertTrue("Mock HTML File " + filename + " not found", file.exists());
		return new FileInputStream(file);
	}

}
