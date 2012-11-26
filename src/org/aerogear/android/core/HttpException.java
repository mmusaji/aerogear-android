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
package org.aerogear.android.core;

import java.util.Arrays;

/**
 * If an HTTP Request does not return status code 200 then this will
 * be thrown.
 */
public class HttpException extends RuntimeException {

    /**
     * The body of the http response.
     */
    private byte[] data;

    /**
     * The returned status code
     */
    private int statusCode;

	public HttpException(byte[] data, int statusCode) {
		this(data, statusCode, null);
	}

    public HttpException(byte[] data, int statusCode, String message) {
        super(message);
        this.data = data;
        this.statusCode = statusCode;
    }

	public byte[] getData() {
		return Arrays.copyOf(data, data.length);
	}

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    public int getStatusCode() {
        return statusCode;
    }

}
