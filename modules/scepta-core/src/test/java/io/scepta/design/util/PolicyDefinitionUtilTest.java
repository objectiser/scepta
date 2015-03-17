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
package io.scepta.design.util;

import static org.junit.Assert.*;
import io.scepta.util.PolicyDefinitionUtil;

import org.junit.Test;

public class PolicyDefinitionUtilTest {

    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value 2";
    private static final String PARAM1 = "param1";
    private static final String PARAM2 = "param2";
    private static final String TEST_ENDPOINT = "test";

    @Test
    public void testGetEndpointName() {
        assertEquals(PolicyDefinitionUtil.getEndpointName(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test"), TEST_ENDPOINT);
        assertEquals(PolicyDefinitionUtil.getEndpointName(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test?op1=val1"), TEST_ENDPOINT);
        assertNull(PolicyDefinitionUtil.getEndpointName("jms:test"));
    }

    @Test
    public void testGetEndpointQueryParametersEmpty() {
        java.util.Map<String,String> params=PolicyDefinitionUtil.getEndpointQueryParameters(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test");

        assertNotNull(params);

        assertEquals(params.size(), 0);
    }

    @Test
    public void testGetEndpointQueryParametersSingle() {
        java.util.Map<String,String> params=PolicyDefinitionUtil.getEndpointQueryParameters(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test?"+PARAM1+"="+VALUE1);

        assertNotNull(params);

        assertEquals(params.size(), 1);

        assertTrue(params.containsKey(PARAM1));

        assertEquals(params.get(PARAM1), VALUE1);
    }

    @Test
    public void testGetEndpointQueryParametersSingleWithPaddingSpaces() {
        java.util.Map<String,String> params=PolicyDefinitionUtil.getEndpointQueryParameters(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test? "+PARAM1+" = "+VALUE1+" ");

        assertNotNull(params);

        assertEquals(params.size(), 1);

        assertTrue(params.containsKey(PARAM1));

        assertEquals(params.get(PARAM1), VALUE1);
    }

    @Test
    public void testGetEndpointQueryParametersSingleWithSpaceInValue() {
        java.util.Map<String,String> params=PolicyDefinitionUtil.getEndpointQueryParameters(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test? "+PARAM2+" = "+VALUE2);

        assertNotNull(params);

        assertEquals(params.size(), 1);

        assertTrue(params.containsKey(PARAM2));

        assertEquals(params.get(PARAM2), VALUE2);
    }

    @Test
    public void testGetEndpointQueryParametersSingleWithQuotes() {
        java.util.Map<String,String> params=PolicyDefinitionUtil.getEndpointQueryParameters(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test?"+PARAM1+"=\""+VALUE1+"\"");

        assertNotNull(params);

        assertEquals(params.size(), 1);

        assertTrue(params.containsKey(PARAM1));

        assertEquals(params.get(PARAM1), "\""+VALUE1+"\"");
    }

    @Test
    public void testGetEndpointQueryParametersMultiple() {
        java.util.Map<String,String> params=PolicyDefinitionUtil.getEndpointQueryParameters(
                PolicyDefinitionUtil.ENDPOINT_PREFIX+"test?"+PARAM1+"="+VALUE1+"&"+PARAM2+"="+VALUE2);

        assertNotNull(params);

        assertEquals(params.size(), 2);

        assertTrue(params.containsKey(PARAM1));
        assertTrue(params.containsKey(PARAM2));

        assertEquals(params.get(PARAM1), VALUE1);
        assertEquals(params.get(PARAM2), VALUE2);
    }

    @Test
    public void testReplaceParametersWithDefinedValues() {
        String text="Mary had a ${size|medium,little} lamb";
        String expected="Mary had a little lamb";

        java.util.Map<String,String> parameters=new java.util.HashMap<String, String>();
        parameters.put("size", "little");

        String actual=PolicyDefinitionUtil.replaceParameters(text, parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void testReplaceParametersMultipleInstances() {
        String text="Mary had a ${size} lamb who was very ${size}";
        String expected="Mary had a little lamb who was very little";

        java.util.Map<String,String> parameters=new java.util.HashMap<String, String>();
        parameters.put("size", "little");

        String actual=PolicyDefinitionUtil.replaceParameters(text, parameters);

        assertEquals(expected, actual);
    }
}
