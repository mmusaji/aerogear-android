/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.android.impl.pipeline;

import org.jboss.aerogear.android.impl.pipeline.PipeConfig;
import org.jboss.aerogear.android.impl.pipeline.DefaultPipeFactory;
import org.jboss.aerogear.android.impl.pipeline.RestAdapter;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import java.net.MalformedURLException;
import java.net.URL;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.jboss.aerogear.android.Pipeline;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.impl.AGSecurityAuthenticationConfig;
import org.jboss.aerogear.android.authentication.impl.AGSecurityAuthenticationModule;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import static org.jboss.aerogear.android.impl.pipeline.PipeTypes.REST;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class PipelineTest {

    private URL url;

    @Before
    public void setup() throws MalformedURLException {
        url = new URL("http://server.com/context/");
    }

    @Test
    public void testRegisterPipeFactory() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url, new StubPipeFactory());

        Pipe stubPipe = pipeline.pipe(Data.class, new PipeConfig(url, Data.class));

        assertNotNull("received pipe", stubPipe);
        assertEquals("verifying the given URL", "http://myStubUrl/myStubProject", stubPipe.getUrl().toString());
        assertEquals("verifying the type", "Stub", stubPipe.getType().getName());
    }

    @Test
    public void testAddPipe() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        Pipe newPipe = pipeline.pipe(Data.class);

        assertEquals("verifying the given URL", "http://server.com/context/data", newPipe.getUrl().toString());
        assertEquals("verifying the type", REST, newPipe.getType());
    }

    @Test
    public void testAddPipeWithEndpoint() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("bad name");
        config.setEndpoint("foo");
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the given URL", "http://server.com/context/foo", newPipe.getUrl().toString());
    }

    @Test
    public void testAddPipeWithType() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        config.setType(REST);
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the type", REST, newPipe.getType());
    }

    @Test
    public void testAddPipeWithUrl() throws MalformedURLException {
        URL otherURL = new URL("http://server.com/otherContext/");

        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(otherURL, Data.class);
        config.setName("foo");
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the given URL", "http://server.com/otherContext/data", newPipe.getUrl().toString());
    }

    @Test
    public void testAddPipeWithEndpointAndType() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        config.setEndpoint("bar/");
        config.setType(REST);
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the type", REST, newPipe.getType());
        assertEquals("verifying the given URL", "http://server.com/context/bar/", newPipe.getUrl().toString());
    }

    @Test
    public void testAddPipeWithEndpointAndURL() throws MalformedURLException {
        URL otherURL = new URL("http://server.com/otherContext/");

        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(otherURL, Data.class);
        config.setName("bad name");
        config.setEndpoint("foo/");
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the given URL", "http://server.com/otherContext/foo/", newPipe.getUrl().toString());
    }

    @Test
    public void testAddPipeWithTypeAndUrl() throws MalformedURLException {
        URL otherURL = new URL("http://server.com/otherContext/");

        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(otherURL, Data.class);
        config.setType(REST);
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the type", REST, newPipe.getType());
        assertEquals("verifying the given URL", "http://server.com/otherContext/data", newPipe.getUrl().toString());
    }

    @Test
    public void testGetExistingPipe() {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        pipeline.pipe(Data.class, config);

        Pipe fooPipe = pipeline.get("foo");
        assertNotNull("received pipe", fooPipe);
    }

    @Test
    public void testGetNonExistingPipe() {
        Pipeline pipeline = new Pipeline(url);

        Pipe fooPipe = pipeline.get("Footasks");
        assertNull("Not received pipe", fooPipe);
    }

    @Test
    public void testRemoveExistingPipe() {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        pipeline.pipe(Data.class, config);

        Pipe fooPipe = pipeline.remove("foo");
        assertNotNull("deleted pipe", fooPipe);

        fooPipe = pipeline.get("foo");
        assertNull("Not received pipe", fooPipe);
    }

    @Test
    public void testRemoveNonExistingPipe() {
        Pipeline pipeline = new Pipeline(url);

        Pipe fooPipe = pipeline.remove("foo");
        assertNull("Not deleted pipe", fooPipe);
    }

    @Test
    public void testPipeConfigEndpointIsSetCorrectly() {
        Pipeline pipeline = new Pipeline(url);
        PipeConfig pipeConfig = new PipeConfig(url, Data.class);
        pipeConfig.setEndpoint("EndPoint");
        Pipe<Data> pipe = pipeline.pipe(Data.class, pipeConfig);
        assertEquals("http://server.com/context/EndPoint", pipe.getUrl().toString());

    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultPipeFactoryThrowsIAEOnBadConfigType() {
        DefaultPipeFactory factory = new DefaultPipeFactory();
        PipeConfig config = new PipeConfig(url, Data.class);
        config.setType(null);
        factory.createPipe(Data.class, config);
    }

    @Test
    public void pipeFactoryAddsAuthModule() throws Exception {
        DefaultPipeFactory factory = new DefaultPipeFactory();
        PipeConfig config = new PipeConfig(url, Data.class);
        config.setAuthModule(new AGSecurityAuthenticationModule(url,
                new AGSecurityAuthenticationConfig()));
        RestAdapter<Data> pipe = (RestAdapter<Data>) factory.createPipe(
                Data.class, config);
        Object restRunner = UnitTestUtils.getPrivateField(pipe, "restRunner");

        assertNotNull(UnitTestUtils.getPrivateField(restRunner, "authModule",
                AuthenticationModule.class));

    }

    @Test(expected = IllegalArgumentException.class)
    public void pipelineStringConstructorThrowsExceptionOnBadURL()
            throws Exception {
        new Pipeline("ttp:");
    }

    @Test
    public void pipeFactoryAppendsUrlCorrectly() throws Exception {
        URL altUrl = new URL("http://server.com/context");
        DefaultPipeFactory factory = new DefaultPipeFactory();
        PipeConfig config = new PipeConfig(altUrl, Data.class);
        Pipe<Data> pipe = factory.createPipe(Data.class, config);
        assertEquals("http://server.com/context/data", pipe.getUrl().toString());
    }

    @Test
    public void pipelineStringConstructor() {
        Pipeline pipeline = new Pipeline(url.toString());
        Pipeline pipeline2 = new Pipeline(url);

        Pipe pipe = pipeline.pipe(Data.class);
        Pipe pipe2 = pipeline2.pipe(Data.class);

        assertEquals(pipe.getUrl(), pipe2.getUrl());
    }
}
