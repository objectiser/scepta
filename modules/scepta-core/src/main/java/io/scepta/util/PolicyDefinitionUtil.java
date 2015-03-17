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
package io.scepta.util;

/**
 * This class provides policy definition utility functions.
 *
 */
public class PolicyDefinitionUtil {

    public static final String ENDPOINT_PREFIX = "endpoint:";

    public static final String PROCESSOR_PREFIX = "processor:";

    /**
     * This method determines whether the supplied element name represents
     * a consumer.
     *
     * @param elemName The element name
     * @return Whether the element is a consumer
     */
    public static final boolean isConsumer(String elemName) {
        return (elemName.equals("from"));
    }

    /**
     * This method determines whether the supplied element name represents
     * a producer.
     *
     * @param elemName The element name
     * @return Whether the element is a producer
     */
    public static final boolean isProducer(String elemName) {
        return (elemName.equals("to") || isOnewayProducer(elemName));
    }

    /**
     * This method determines whether the supplied element name represents
     * a 'oneway' producer.
     *
     * @param elemName The element name
     * @return Whether the element is a producer
     */
    public static final boolean isOnewayProducer(String elemName) {
        return (elemName.equals("inOnly"));
    }

    /**
     * This method determins whether the URI is an endpoint.
     *
     * @param uri The logical URI
     * @return Whether it represents an endpoint
     */
    public static boolean isEndpointURI(String uri) {
        return (uri != null && uri.startsWith(ENDPOINT_PREFIX));
    }

    /**
     * This method returns the endpoint name related to
     * a supplied logical URI.
     *
     * @param uri The logical URI
     * @return The endpoint name, or null if not found
     */
    public static String getEndpointName(String uri) {
        String endpointName=null;

        if (uri != null && uri.startsWith(ENDPOINT_PREFIX)) {
            endpointName = uri.substring(ENDPOINT_PREFIX.length());

            int pos=endpointName.indexOf('?');
            if (pos != -1) {
                endpointName = endpointName.substring(0, pos);
            }
        }

        return (endpointName);
    }

    /**
     * This method returns the query parameters associated with
     * an endpoint URI.
     *
     * @param uri The logical URI
     * @return The endpoint query parameters
     */
    public static java.util.Map<String,String> getEndpointQueryParameters(String uri) {
        java.util.Map<String,String> ret=new java.util.HashMap<String, String>();

        if (uri != null && uri.startsWith(ENDPOINT_PREFIX)) {
            String endpoint = uri.substring(ENDPOINT_PREFIX.length());

            int pos=endpoint.indexOf('?');
            if (pos != -1) {
                String parameters=endpoint.substring(pos+1);

                java.util.StringTokenizer st=new java.util.StringTokenizer(parameters, "&");

                while (st.hasMoreTokens()) {
                    String kvstr=st.nextToken();
                    String[] kv=kvstr.split("=");
                    if (kv.length == 2) {
                        String key=kv[0].trim();
                        String value=kv[1].trim();
                        ret.put(key, value);
                    } else {
                        // TODO: REPORT ERROR
                    }
                }
            }
        }

        return (ret);
    }

    /**
     * This method replaces any parameters from a supplied map in the supplied text and
     * returns the modified version.
     *
     * @param text The original text
     * @param parameters The map of parameters/values
     * @return The updated text, with available parameters replaced
     */
    public static String replaceParameters(String text, java.util.Map<String,String> parameters) {

        for (String key : parameters.keySet()) {
            String value=parameters.get(key);

            text = text.replaceAll("\\$\\{"+key+"(|.*)?}", value);
        }

        return (text);
    }

    /**
     * This method determins whether the URI is a processor.
     *
     * @param uri The logical URI
     * @return Whether it represents a processor
     */
    public static boolean isProcessorURI(String uri) {
        return (uri != null && uri.startsWith(PROCESSOR_PREFIX));
    }

    /**
     * This method returns the processor name related to
     * a supplied logical URI.
     *
     * @param uri The logical URI
     * @return The processor name, or null if not found
     */
    public static String getProcessorName(String uri) {
        String processorName=null;

        if (uri != null && uri.startsWith(PROCESSOR_PREFIX)) {
            processorName = uri.substring(PROCESSOR_PREFIX.length());

            int pos=processorName.indexOf('?');
            if (pos != -1) {
                processorName = processorName.substring(0, pos);
            }
        }

        return (processorName);
    }

}
