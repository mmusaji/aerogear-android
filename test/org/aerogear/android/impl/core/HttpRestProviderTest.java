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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aerogear.android.Provider;
import org.aerogear.android.authentication.impl.AuthenticatorTest;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpException;
import static org.aerogear.android.impl.helper.TestUtil.*;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class HttpRestProviderTest {

	private static final URL SIMPLE_URL;
	private static final String HEADER_KEY1_NAME = "KEY1";
	private static final String HEADER_KEY2_NAME = "KEY2";
	private static final byte[] RESPONSE_DATA = "12345".getBytes();/*Not real data*/
	private static final String REQUEST_DATA = "12345";/*Not real data*/
	private static final Map<String, List<String>> RESPONSE_HEADERS;
	private static final String HEADER_VALUE = "VALUE";

	static {
		try {
			SIMPLE_URL = new URL("http", "localhost", 80, "/");
		} catch (MalformedURLException ex) {
			Logger.getLogger(AuthenticatorTest.class.getName()).log(
					Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
		}

		RESPONSE_HEADERS = new HashMap<String, List<String>>(2);
		RESPONSE_HEADERS.put(HEADER_KEY1_NAME, new ArrayList<String>(1));
		RESPONSE_HEADERS.put(HEADER_KEY2_NAME, new ArrayList<String>(1));
		RESPONSE_HEADERS.get(HEADER_KEY1_NAME).add(HEADER_VALUE);
		RESPONSE_HEADERS.get(HEADER_KEY2_NAME).add(HEADER_VALUE);

	}

	@Test(expected = HttpException.class)
	public void testGetFailsWith404() throws Exception {
		HttpURLConnection connection404 = mock(HttpURLConnection.class);

		doReturn(404).when(connection404).getResponseCode();
		when(connection404.getErrorStream()).thenReturn(
				new ByteArrayInputStream(RESPONSE_DATA));

		HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
		setPrivateField(provider, "connectionPreparer",
				new HttpUrlConnectionProvider(connection404));

		try {
			provider.get();
		} catch (HttpException exception) {
			assertArrayEquals(RESPONSE_DATA, exception.getData());
			assertEquals(404, exception.getStatusCode());
			throw exception;
		}
	}

	@Test
	public void testGet() throws Exception {
		HttpURLConnection connection = mock(HttpURLConnection.class);
		HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
		setPrivateField(provider, "connectionPreparer",
				new HttpUrlConnectionProvider(connection));

		doReturn(200).when(connection).getResponseCode();
		when(connection.getInputStream()).thenReturn(
				new ByteArrayInputStream(RESPONSE_DATA));
		when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);

		HeaderAndBody result = provider.get();
		assertArrayEquals(RESPONSE_DATA, result.getBody());
		assertNotNull(result.getHeader(HEADER_KEY1_NAME));
		assertNotNull(result.getHeader(HEADER_KEY2_NAME));
		assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));

	}

	@Test
	public void testPost() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				RESPONSE_DATA.length);
		HttpURLConnection connection = mock(HttpURLConnection.class);
		HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
		setPrivateField(provider, "connectionPreparer",
				new HttpUrlConnectionProvider(connection));

		doReturn(200).when(connection).getResponseCode();
		when(connection.getInputStream()).thenReturn(
				new ByteArrayInputStream(RESPONSE_DATA));
		when(connection.getOutputStream()).thenReturn(outputStream);
		when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);
		doCallRealMethod().when(connection).setRequestMethod(anyString());
		when(connection.getRequestMethod()).thenCallRealMethod();

		HeaderAndBody result = provider.post(REQUEST_DATA);
		assertEquals("POST", connection.getRequestMethod());
		assertArrayEquals(RESPONSE_DATA, result.getBody());
		assertNotNull(result.getHeader(HEADER_KEY1_NAME));
		assertNotNull(result.getHeader(HEADER_KEY2_NAME));
		assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));
		assertArrayEquals(RESPONSE_DATA, outputStream.toByteArray());

	}

	@Test
	public void testPut() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				RESPONSE_DATA.length);
		HttpURLConnection connection = mock(HttpURLConnection.class);
		HttpUrlConnectionProvider providerProvider = new HttpUrlConnectionProvider(
				connection);
		final String id = "1";

		doReturn(200).when(connection).getResponseCode();
		when(connection.getInputStream()).thenReturn(
				new ByteArrayInputStream(RESPONSE_DATA));
		when(connection.getOutputStream()).thenReturn(outputStream);
		when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);
		doCallRealMethod().when(connection).setRequestMethod(anyString());
		when(connection.getRequestMethod()).thenCallRealMethod();

		HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
		setPrivateField(provider, "connectionPreparer", providerProvider);

		HeaderAndBody result = provider.put(id, REQUEST_DATA);
		assertEquals("PUT", connection.getRequestMethod());
		assertArrayEquals(RESPONSE_DATA, result.getBody());
		assertNotNull(result.getHeader(HEADER_KEY1_NAME));
		assertNotNull(result.getHeader(HEADER_KEY2_NAME));
		assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));
		assertArrayEquals(RESPONSE_DATA, outputStream.toByteArray());
		assertEquals(id, providerProvider.id);
	}

	@Test
	public void testDelete() throws Exception {
		HttpURLConnection connection = mock(HttpURLConnection.class);
		HttpUrlConnectionProvider providerProvider = new HttpUrlConnectionProvider(
				connection);
		final String id = "1";

		doReturn(200).when(connection).getResponseCode();
		when(connection.getInputStream()).thenReturn(
				new ByteArrayInputStream(RESPONSE_DATA));
		when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);
		doCallRealMethod().when(connection).setRequestMethod(anyString());
		when(connection.getRequestMethod()).thenCallRealMethod();

		HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
		setPrivateField(provider, "connectionPreparer", providerProvider);

		HeaderAndBody result = provider.delete(id);

		assertArrayEquals(RESPONSE_DATA, result.getBody());
		assertEquals("DELETE", connection.getRequestMethod());
		assertNotNull(result.getHeader(HEADER_KEY1_NAME));
		assertNotNull(result.getHeader(HEADER_KEY2_NAME));
		assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));
		assertEquals(id, providerProvider.id);
	}

	static class HttpUrlConnectionProvider
			implements
				Provider<HttpURLConnection> {

		public HttpURLConnection connection;
		public String id;

		public HttpUrlConnectionProvider(HttpURLConnection connection) {
			this.connection = connection;
		}

		@Override
		public HttpURLConnection get(Object... in) {
			if (in != null && in.length > 0) {
				id = (String) in[0];
			}
			return connection;
		}
	}

}
