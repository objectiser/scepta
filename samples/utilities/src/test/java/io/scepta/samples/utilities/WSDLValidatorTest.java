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
package io.scepta.samples.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

public class WSDLValidatorTest {

    @Test
    public void testFailNullWSDL() {
        WSDLValidator validator=new WSDLValidator();

        java.util.List<String> issues=validator.validate(null);

        assertNotNull(issues);

        assertTrue(issues.size() == 1);
    }

    @Test
    public void testFailEmptyWSDL() {
        WSDLValidator validator=new WSDLValidator();

        java.util.List<String> issues=validator.validate("");

        assertNotNull(issues);

        assertTrue(issues.size() == 1);
    }

    @Test
    public void testValidWSDLTopElement() {
        WSDLValidator validator=new WSDLValidator();

        java.util.List<String> issues=validator.validate(
                "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" />");

        assertNotNull(issues);

        assertTrue(issues.size() == 0);
    }

}
