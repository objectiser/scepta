/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.scepta.generator;

import static org.junit.Assert.*;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.scepta.model.Endpoint;
import io.scepta.model.Policy;
import io.scepta.model.PolicyGroup;
import io.scepta.model.Processor;
import io.scepta.server.GeneratedResult;
import io.scepta.server.PolicyGroupInterchange;
import io.scepta.util.DOMUtil;
import io.scepta.util.PolicyDefinitionUtil;

import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultGeneratorTest {

    private static final String TEST_PROCESSOR_CLASS = "my.test.Processor";
    private static final String TEST_PROCESSOR_NAME = "testBean";
    private static final String OTHER_PARAM = "?other=param";
    private static final String TO_ELEMENT = "to";
    private static final String FROM_ELEMENT = "from";
    private static final String ACTIVEMQ_QUEUE_TEST = "activemq:queue:test";
    private static final String TEST_ENDPOINT = "test";
    private static final ObjectMapper MAPPER=new ObjectMapper();

    @BeforeClass
    public static void setup() {
        System.setProperty("scepta.version", "0.1.0-SNAPSHOT");
    }

    @Test
    public void testProcessEndpointURIConsumer() {
        String uri=PolicyDefinitionUtil.ENDPOINT_PREFIX+TEST_ENDPOINT;

        PolicyGroup group=new PolicyGroup();

        Endpoint endpoint=new Endpoint()
                .setName(TEST_ENDPOINT)
                .setURI(ACTIVEMQ_QUEUE_TEST);

        endpoint.getConsumerOptions().put("op1", "val1");
        endpoint.getConsumerOptions().put("op2", "val2");

        group.getEndpoints().add(endpoint);

        try {
            String newuri=DefaultGenerator.processEndpointURI(group, FROM_ELEMENT, uri);

            assertNotNull(newuri);

            assertEquals(ACTIVEMQ_QUEUE_TEST+"?op1=val1&op2=val2", newuri);
        } catch (Exception e) {
            fail("Failed to process endpoint uri: "+e);
        }
    }

    @Test
    public void testProcessEndpointURIConsumerWithSceptaQueryString() {
        String uri=PolicyDefinitionUtil.ENDPOINT_PREFIX+TEST_ENDPOINT+OTHER_PARAM;

        PolicyGroup group=new PolicyGroup();

        Endpoint endpoint=new Endpoint()
                .setName(TEST_ENDPOINT)
                .setURI(ACTIVEMQ_QUEUE_TEST);

        endpoint.getConsumerOptions().put("op1", "val1");
        endpoint.getConsumerOptions().put("op2", "val2");

        group.getEndpoints().add(endpoint);

        try {
            String newuri=DefaultGenerator.processEndpointURI(group, FROM_ELEMENT, uri);

            assertNotNull(newuri);

            assertEquals(ACTIVEMQ_QUEUE_TEST+OTHER_PARAM+"&op1=val1&op2=val2", newuri);
        } catch (Exception e) {
            fail("Failed to process endpoint uri: "+e);
        }
    }

    @Test
    public void testProcessEndpointURIConsumerWithExistingOptions() {
        String uri=PolicyDefinitionUtil.ENDPOINT_PREFIX+TEST_ENDPOINT;

        PolicyGroup group=new PolicyGroup();

        Endpoint endpoint=new Endpoint()
                .setName(TEST_ENDPOINT)
                .setURI(ACTIVEMQ_QUEUE_TEST+"?existing=value");

        endpoint.getConsumerOptions().put("op1", "val1");
        endpoint.getConsumerOptions().put("op2", "val2");

        group.getEndpoints().add(endpoint);

        try {
            String newuri=DefaultGenerator.processEndpointURI(group, FROM_ELEMENT, uri);

            assertNotNull(newuri);

            assertEquals(ACTIVEMQ_QUEUE_TEST+"?existing=value&op1=val1&op2=val2", newuri);
        } catch (Exception e) {
            fail("Failed to process endpoint uri: "+e);
        }
    }

    @Test
    public void testProcessEndpointURIProducer() {
        String uri=PolicyDefinitionUtil.ENDPOINT_PREFIX+TEST_ENDPOINT;

        PolicyGroup group=new PolicyGroup();

        Endpoint endpoint=new Endpoint()
                .setName(TEST_ENDPOINT)
                .setURI(ACTIVEMQ_QUEUE_TEST);

        endpoint.getProducerOptions().put("op1", "val1");
        endpoint.getProducerOptions().put("op2", "val2");

        group.getEndpoints().add(endpoint);

        try {
            String newuri=DefaultGenerator.processEndpointURI(group, TO_ELEMENT, uri);

            assertNotNull(newuri);

            assertEquals(ACTIVEMQ_QUEUE_TEST+"?op1=val1&op2=val2", newuri);
        } catch (Exception e) {
            fail("Failed to process endpoint uri: "+e);
        }
    }

    @Test
    public void testProcessEndpointURIProducerWithExistingOptions() {
        String uri=PolicyDefinitionUtil.ENDPOINT_PREFIX+TEST_ENDPOINT;

        PolicyGroup group=new PolicyGroup();

        Endpoint endpoint=new Endpoint()
                .setName(TEST_ENDPOINT)
                .setURI(ACTIVEMQ_QUEUE_TEST+"?existing=value");

        endpoint.getProducerOptions().put("op1", "val1");
        endpoint.getProducerOptions().put("op2", "val2");

        group.getEndpoints().add(endpoint);

        try {
            String newuri=DefaultGenerator.processEndpointURI(group, TO_ELEMENT, uri);

            assertNotNull(newuri);

            assertEquals(ACTIVEMQ_QUEUE_TEST+"?existing=value&op1=val1&op2=val2", newuri);
        } catch (Exception e) {
            fail("Failed to process endpoint uri: "+e);
        }
    }

    @Test
    public void testGenerate() {
        PolicyGroupInterchange group=getPolicyGroupInterchange("RESTProducer");

        DefaultGenerator generator=new DefaultGenerator();

        GeneratedResult result=generator.generate(group);

        assertTrue(result.getGenerated().containsKey("ActivityServer"));

        WebArchive war=result.getGenerated().get("ActivityServer");

        try {
            java.io.File f=java.io.File.createTempFile(war.getName(), ".war");
            f.deleteOnExit();

            war.as(ZipExporter.class).exportTo(f, true);

            JarFile jar=new JarFile(f);
            java.util.Enumeration<JarEntry> iter=jar.entries();

            while (iter.hasMoreElements()) {
                JarEntry entry=iter.nextElement();

                if (entry.getName().endsWith(".xml")) {
                    // Check xml is parsable
                    java.io.InputStream is=jar.getInputStream(entry);
                    DOMUtil.textToDoc(is);
                }
            }

            jar.close();

        } catch (Exception e) {
            fail("Failed to export war: "+e);
        }

        // Check zip
        try {
            java.io.File f=java.io.File.createTempFile(group.getGroupDetails().getName(), ".zip");
            f.deleteOnExit();

            java.io.FileOutputStream fos=new java.io.FileOutputStream(f);

            result.asZip(fos);

            // Check zip file can be loaded
            ZipFile zipFile=new ZipFile(f);

            java.util.Enumeration<? extends ZipEntry> iter=zipFile.entries();

            while (iter.hasMoreElements()) {
                ZipEntry entry=iter.nextElement();
                java.io.InputStream is=zipFile.getInputStream(entry);

                // TODO: Any checks?

                is.close();
            }

            zipFile.close();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to export zip: "+e);
        }
    }

    @Test
    public void testGeneratePolicyDefinitionProcessorOnGroup() {
        PolicyGroup group=new PolicyGroup();
        Processor processor=new Processor();
        processor.setName(TEST_PROCESSOR_NAME);
        processor.setClassName(TEST_PROCESSOR_CLASS);
        group.getProcessors().add(processor);

        Endpoint ep=new Endpoint();
        ep.setName("activityunits");
        ep.setURI("activemq:queue:activityunits");
        group.getEndpoints().add(ep);

        Policy policy=new Policy();

        String policyDefn="<route id=\"TestPolicy\">\n      <from uri=\"endpoint:activityunits\"/>\n"
                + "      <to uri=\"processor:"+TEST_PROCESSOR_NAME+"\"/>\n    </route>\n";

        String camelConfig=DefaultGenerator.generatePolicyDefinition(group, policy, policyDefn, null);

        assertNotNull(camelConfig);

        org.w3c.dom.Document doc=null;

        try {
            doc = DOMUtil.textToDoc(camelConfig);
        } catch (Exception e) {
            fail("Failed to parse config: "+e);
        }

        if (doc != null) {

            // Check for processor URI
            org.w3c.dom.NodeList tos=doc.getElementsByTagName("to");

            if (tos.getLength() != 1) {
                fail("Expecting 1 'to': "+tos.getLength());
            }

            org.w3c.dom.Element to=(org.w3c.dom.Element)tos.item(0);

            assertEquals(to.getAttribute("uri"), TEST_PROCESSOR_NAME);

            // Check for bean definitions
            org.w3c.dom.NodeList beans=doc.getElementsByTagName("bean");

            if (beans.getLength() != 1) {
                fail("Expecting 1 bean: "+beans.getLength());
            }

            org.w3c.dom.Element bean=(org.w3c.dom.Element)beans.item(0);

            assertEquals(bean.getAttribute("id"), TEST_PROCESSOR_NAME);
            assertEquals(bean.getAttribute("class"), TEST_PROCESSOR_CLASS);

        } else {
            fail("Could not find camel-config.xml");
        }
    }

    @Test
    public void testGeneratePolicyDefinitionProcessorOnPolicy() {
        PolicyGroup group=new PolicyGroup();

        Endpoint ep=new Endpoint();
        ep.setName("activityunits");
        ep.setURI("activemq:queue:activityunits");
        group.getEndpoints().add(ep);

        Policy policy=new Policy();

        Processor processor=new Processor();
        processor.setName(TEST_PROCESSOR_NAME);
        processor.setClassName(TEST_PROCESSOR_CLASS);
        policy.getProcessors().add(processor);

        String policyDefn="<route id=\"TestPolicy\">\n      <from uri=\"endpoint:activityunits\"/>\n"
                + "      <to uri=\"processor:"+TEST_PROCESSOR_NAME+"\"/>\n    </route>\n";

        String camelConfig=DefaultGenerator.generatePolicyDefinition(group, policy, policyDefn, null);

        assertNotNull(camelConfig);

        org.w3c.dom.Document doc=null;

        try {
            doc = DOMUtil.textToDoc(camelConfig);
        } catch (Exception e) {
            fail("Failed to parse config: "+e);
        }

        if (doc != null) {

            // Check for processor URI
            org.w3c.dom.NodeList tos=doc.getElementsByTagName("to");

            if (tos.getLength() != 1) {
                fail("Expecting 1 'to': "+tos.getLength());
            }

            org.w3c.dom.Element to=(org.w3c.dom.Element)tos.item(0);

            assertEquals(to.getAttribute("uri"), TEST_PROCESSOR_NAME);

            // Check for bean definitions
            org.w3c.dom.NodeList beans=doc.getElementsByTagName("bean");

            if (beans.getLength() != 1) {
                fail("Expecting 1 bean: "+beans.getLength());
            }

            org.w3c.dom.Element bean=(org.w3c.dom.Element)beans.item(0);

            assertEquals(bean.getAttribute("id"), TEST_PROCESSOR_NAME);
            assertEquals(bean.getAttribute("class"), TEST_PROCESSOR_CLASS);

        } else {
            fail("Could not find camel-config.xml");
        }
    }

    @Test
    public void testGeneratePolicyDefinitionProcessorOnPolicyDuplicate() {
        PolicyGroup group=new PolicyGroup();

        Endpoint ep=new Endpoint();
        ep.setName("activityunits");
        ep.setURI("activemq:queue:activityunits");
        group.getEndpoints().add(ep);

        Policy policy=new Policy();

        Processor processor=new Processor();
        processor.setName(TEST_PROCESSOR_NAME);
        processor.setClassName(TEST_PROCESSOR_CLASS);
        policy.getProcessors().add(processor);

        String policyDefn="<route id=\"TestPolicy\">\n      <from uri=\"endpoint:activityunits\"/>\n"
                + "      <to uri=\"processor:"+TEST_PROCESSOR_NAME+"\"/>\n"
                + "      <to uri=\"processor:"+TEST_PROCESSOR_NAME+"\"/>\n    </route>\n";

        String camelConfig=DefaultGenerator.generatePolicyDefinition(group, policy, policyDefn, null);

        assertNotNull(camelConfig);

        org.w3c.dom.Document doc=null;

        try {
            doc = DOMUtil.textToDoc(camelConfig);
        } catch (Exception e) {
            fail("Failed to parse config: "+e);
        }

        if (doc != null) {

            // Check for processor URI - should be 2
            org.w3c.dom.NodeList tos=doc.getElementsByTagName("to");

            if (tos.getLength() != 2) {
                fail("Expecting 2 'to': "+tos.getLength());
            }

            org.w3c.dom.Element to1=(org.w3c.dom.Element)tos.item(0);
            org.w3c.dom.Element to2=(org.w3c.dom.Element)tos.item(1);

            assertEquals(to1.getAttribute("uri"), TEST_PROCESSOR_NAME);
            assertEquals(to2.getAttribute("uri"), TEST_PROCESSOR_NAME);

            // Check for bean definitions - should still only be defined once, even
            // though used twice
            org.w3c.dom.NodeList beans=doc.getElementsByTagName("bean");

            if (beans.getLength() != 1) {
                fail("Expecting 1 bean: "+beans.getLength());
            }

            org.w3c.dom.Element bean=(org.w3c.dom.Element)beans.item(0);

            assertEquals(bean.getAttribute("id"), TEST_PROCESSOR_NAME);
            assertEquals(bean.getAttribute("class"), TEST_PROCESSOR_CLASS);

        } else {
            fail("Could not find camel-config.xml");
        }
    }

    protected PolicyGroupInterchange getPolicyGroupInterchange(String name) {
        PolicyGroupInterchange ret=null;

        try {
            java.io.InputStream is=DefaultGeneratorTest.class.getResourceAsStream("/groups/"+name+".json");

            if (is != null) {
                byte[] b=new byte[is.available()];

                is.read(b);

                is.close();

                ret = MAPPER.readValue(b, PolicyGroupInterchange.class);
            }

        } catch (Exception e) {
            fail("Failed to load policy group '"+name+"'"+e);
        }

        return (ret);
    }
}
