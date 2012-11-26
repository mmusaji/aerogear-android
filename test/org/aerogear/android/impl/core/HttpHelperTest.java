/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aerogear.android.impl.core;

import java.util.HashMap;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpException;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpHelperTest {
	private static final byte[] SIMPLE_DATA = {8, 6, 7, 5, 3, 0, 9};
	private static final String SAMPLE_MESSAGE = "SAMPLE_MESSAGE";
	private static final String SAMPLE_HEADER = "SAMPLE_HEADER";
	private static final int NOT_FOUND = 404;

	@Test
	public void testHttpExceptionConstructor() {
		HttpException exception = new HttpException(SIMPLE_DATA, NOT_FOUND);
		HttpException exceptionWithMessage = new HttpException(SIMPLE_DATA,
				NOT_FOUND, SAMPLE_MESSAGE);

		assertNotSame(SIMPLE_DATA, exception.getData());
		assertArrayEquals(SIMPLE_DATA, exception.getData());
		assertEquals(NOT_FOUND, exception.getStatusCode());
		assertEquals(SAMPLE_MESSAGE, exceptionWithMessage.getMessage());

	}

	@Test
	public void testHeaderAndBody() {
		HeaderAndBody headerAndBody = new HeaderAndBody(SIMPLE_DATA,
				new HashMap<String, Object>());
		headerAndBody.setHeader(SAMPLE_HEADER, SAMPLE_MESSAGE);
		assertEquals(SAMPLE_MESSAGE, headerAndBody.getHeader(SAMPLE_HEADER));
	}

}
