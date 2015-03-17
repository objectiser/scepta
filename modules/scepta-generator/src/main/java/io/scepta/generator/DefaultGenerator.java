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

import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.scepta.generator.dependency.ConfiguredDependencyResolver;
import io.scepta.model.Characteristic;
import io.scepta.model.Dependency;
import io.scepta.model.Endpoint;
import io.scepta.model.Policy;
import io.scepta.model.PolicyGroup;
import io.scepta.model.Processor;
import io.scepta.model.Resource;
import io.scepta.server.CharacteristicType;
import io.scepta.server.GeneratedResult;
import io.scepta.server.Generator;
import io.scepta.server.PolicyGroupInterchange;
import io.scepta.util.DOMUtil;
import io.scepta.util.PolicyDefinitionUtil;

/**
 * This class represents the default implementation of the Generator.
 *
 */
public class DefaultGenerator implements Generator {

    private static final String WEB_XML = "web.xml";

    private static final java.util.Set<Dependency> SPRING_DEPENDENCIES=new java.util.HashSet<Dependency>();

    private static final String CAMEL_CONTEXT_HEADER;
    private static final String CAMEL_CONTEXT_FOOTER;

    static {
        // TODO: Find way to retrieve Spring version
        SPRING_DEPENDENCIES.add(new Dependency()
                .setGroupId("org.springframework")
                .setArtifactId("spring-web")
                .setVersion("4.1.1.RELEASE"));
        SPRING_DEPENDENCIES.add(new Dependency()
                .setGroupId("org.apache.camel")
                .setArtifactId("camel-spring")
                .setVersion("2.16-SNAPSHOT"));

        // TODO: See if possible to identify dependencies based on camel xml element names
        SPRING_DEPENDENCIES.add(new Dependency()
                .setGroupId("org.apache.camel")
                .setArtifactId("camel-jsonpath")
                .setVersion("2.16-SNAPSHOT"));
        SPRING_DEPENDENCIES.add(new Dependency()
                .setGroupId("org.apache.camel")
                .setArtifactId("camel-mvel")
                .setVersion("2.16-SNAPSHOT"));

        // Load header and footer definitions
        java.io.InputStream is=ConfiguredDependencyResolver.class.getResourceAsStream("/CamelContextHeader");

        String header=null;
        try {
            byte[] b=new byte[is.available()];
            is.read(b);

            header = new String(b);

        } catch (Exception e) {
            // TODO: ERROR
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // TODO: REPORT ERROR
                e.printStackTrace();
            }
        }

        CAMEL_CONTEXT_HEADER = header;

        is = ConfiguredDependencyResolver.class.getResourceAsStream("/CamelContextFooter");

        String footer = null;

        try {
            byte[] b=new byte[is.available()];
            is.read(b);

            footer = new String(b);

        } catch (Exception e) {
            // TODO: ERROR
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // TODO: REPORT ERROR
                e.printStackTrace();
            }
        }

        CAMEL_CONTEXT_FOOTER = footer;
    }

    @Override
    public GeneratedResult generate(PolicyGroupInterchange group) {
        GeneratedResult ret=new GeneratedResult(group);

        // Process each policy
        for (Policy policy : group.getPolicyDetails()) {

            WebArchive war=ShrinkWrap.create(WebArchive.class,
                    group.getGroupDetails().getName()+"-"+policy.getName());

            // Process the policy definition
            generatePolicyDefinition(group.getGroupDetails(), policy,
                    group.getPolicyDefinitions().get(policy.getName()), war);

            // Process the 'resources'
            for (Resource resource : policy.getResources()) {
                generateResource(group.getGroupDetails(), policy, resource,
                        group.getResourceDefinitions().get(resource.getName()), war);
            }

            // Add web.xml
            addWebXML(war);

            ret.getGenerated().put(policy.getName(), war);
        }

        return ret;
    }

    /**
     * This method adds a web.xml file to the archive.
     *
     * @param war The web archive
     */
    protected static void addWebXML(WebArchive war) {
        java.net.URL url=DefaultGenerator.class.getResource("/templates/camel-web-xml");

        if (url != null) {
            // Check if web.xml already exists (added as a resource)
            if (war.contains("WEB-INF/web.xml")) {
                java.io.InputStream is1=null;
                java.io.InputStream is2=null;

                try {
                    is1=url.openStream();

                    org.w3c.dom.Document doc1=DOMUtil.textToDoc(is1);

                    org.jboss.shrinkwrap.api.Node node=war.get("WEB-INF/web.xml");

                    is2 = node.getAsset().openStream();

                    org.w3c.dom.Document doc2=DOMUtil.textToDoc(is2);

                    org.w3c.dom.NodeList nl=doc1.getDocumentElement().getChildNodes();

                    for (int i=0; i < nl.getLength(); i++) {
                        Node n=nl.item(i);
                        if (n instanceof Element) {
                            doc2.getDocumentElement().appendChild(doc2.importNode(n, true));
                        }
                    }

                    String merged=DOMUtil.docToText(doc2);

                    war.setWebXML(new StringAsset(merged));

                } catch (Exception e) {
                    // TODO: REPORT ERROR
                    e.printStackTrace();
                } finally {
                    try {
                        is1.close();
                        is2.close();
                    } catch (Exception e) {
                        // TODO: REPORT ERROR
                        e.printStackTrace();
                    }
                }
            } else {
                war.setWebXML(url);
            }
        } else {
            // TODO: REPORT ERROR
            System.err.println("FAILED TO ADD CAMEL WEB.XML");
        }
    }

    /**
     * This method converts the supplied policy definition, which references logical endpoints
     * with characteristics, into real physical endpoints using patterns associated with the
     * characteristics.
     *
     * @param group The policy group
     * @param policy The policy
     * @param policyDefn The policy definition
     * @param war The optional war archive
     * @return The updated policy definition, or null if failed
     */
    protected static String generatePolicyDefinition(PolicyGroup group, Policy policy,
                        String policyDefn, WebArchive war) {
        String ret=null;

        // Add header and footer to policy definition
        policyDefn = getPolicyDefinitionHeader()+policyDefn+getPolicyDefinitionFooter();

        // Scan for 'from', 'inOnly' and 'to' elements - if 'scepta' prefix, then locate
        // endpoint definition - then apply actual uri, as well as relevant consumer/producer
        // options and process characteristics

        try {
            // Convert to DOM representation
            org.w3c.dom.Document doc=DOMUtil.textToDoc(policyDefn);

            // Replace the logical URIs with physical details
            processFromTo("from", group, policy, doc, war);
            processFromTo("to", group, policy, doc, war);
            processFromTo("inOnly", group, policy, doc, war);

            // Convert back to text
            ret = DOMUtil.docToText(doc);

            if (war != null) {
                if (ret != null) {
                    war.addAsWebInfResource(new StringAsset(ret), "classes/camel-config.xml");

                    // Add spring dependency
                    addDependencies(war, SPRING_DEPENDENCIES);
                }

                // Add policy dependencies
                addDependencies(war, policy.getDependencies());
            }

        } catch (Exception e) {
            // TODO: LOG EXCEPTION
            e.printStackTrace();
        }

        return (ret);
    }

    protected static boolean processFromTo(String elemName, PolicyGroup group, Policy policy, org.w3c.dom.Document doc,
                            WebArchive war) throws Exception {
        boolean f_changed=false;

        do {
            f_changed = false;

            org.w3c.dom.NodeList nl=doc.getElementsByTagName(elemName);

            for (int i=0; i < nl.getLength(); i++) {
                Node node=nl.item(i);

                if (node instanceof Element) {
                    String uri=((Element)node).getAttribute("uri");

                    if (PolicyDefinitionUtil.isEndpointURI(uri)) {
                        f_changed = processEndpoint(uri, group, (Element)node, war);
                    } else if (PolicyDefinitionUtil.isProcessorURI(uri)) {
                        f_changed = processProcessor(uri, group, policy, (Element)node, war);
                    }

                    if (f_changed) {
                        continue;
                    }
                }
            }
        } while (f_changed);

        return (f_changed);
    }

    protected static String getPolicyDefinitionHeader() {
        return (CAMEL_CONTEXT_HEADER);
    }

    protected static String getPolicyDefinitionFooter() {
        return (CAMEL_CONTEXT_FOOTER);
    }

    /**
     * This method processes the endpoint. If the element represents a SCEPTA endpoint,
     * then it will be configured with the actual physical URI and any relevant options.
     *
     * @param uri The URI
     * @param group The policy group
     * @param elem The DOM element representing the input/output element
     * @param war The optional war archive
     * @return Whether this element has been modified
     * @throws Exception Failed to process endpoint
     */
    protected static boolean processEndpoint(String uri, PolicyGroup group, Element elem,
                            WebArchive war) throws Exception {
        String endpointName=PolicyDefinitionUtil.getEndpointName(uri);

        if (endpointName != null) {
            String newuri=processEndpointURI(group, elem.getNodeName(), uri);

            if (newuri != null) {
                elem.setAttribute("uri", newuri);

                // Lookup endpoint
                Endpoint endpoint=group.getEndpoint(endpointName);

                if (endpoint == null) {
                    // TODO: ERROR
                    throw new Exception("Unable to find endpoint '"+endpointName+"'");
                }

                // Apply characteristics to the policy definition endpoint
                for (Characteristic characteristic : endpoint.getCharacteristics()) {
                    CharacteristicProcessor cp=CharacteristicProcessorFactory.get(characteristic);

                    if (cp == null) {
                        // TODO: ERROR?

                    } else {
                        cp.process(group, endpoint, characteristic, elem);

                        // Add dependencies
                        if (war != null) {
                            addDependencies(war, cp.getDependencies());
                        }
                    }
                }

                // Apply dependencies on endpoint
                if (war != null) {
                    addDependencies(war, endpoint.getDependencies());

                    // Check if dependencies associated with URI
                    addDependencies(war, DependencyResolverManager.getDependencies(newuri));
                }

                return (true);
            }
        } else {
            // Add dependencies related to the physical URI specified
            addDependencies(war, DependencyResolverManager.getDependencies(uri));
        }

        return (false);
    }

    /**
     * This method processes the endpoint. If the URI represents a SCEPTA endpoint,
     * then it will be configured with the actual physical URI and any relevant options.
     *
     * @param group The policy group
     * @param elemName The element name
     * @param uri The original URI
     * @return The new URI, or null if not relevant
     * @throws Exception Failed to process endpoint
     */
    protected static String processEndpointURI(PolicyGroup group, String elemName, String uri)
                                throws Exception {
        String newuri=null;

        String endpointName=PolicyDefinitionUtil.getEndpointName(uri);

        if (endpointName != null) {

            // Lookup endpoint
            Endpoint endpoint=group.getEndpoint(endpointName);

            if (endpoint == null) {
                // TODO: ERROR
                throw new Exception("Unable to find endpoint '"+endpointName+"'");
            }

            newuri = endpoint.getURI();

            // Check if policy URI had query parameters
            java.util.Map<String,String> endpointQueryParams=
                        PolicyDefinitionUtil.getEndpointQueryParameters(uri);

            // Check if endpoint URI needs to replace parameters
            newuri = PolicyDefinitionUtil.replaceParameters(newuri, endpointQueryParams);

            // Add consumer or producer options, if defined
            if (PolicyDefinitionUtil.isConsumer(elemName)) {

                boolean firstOption=(newuri.indexOf('?')==-1);

                // Sort the option keys to make the end result reproducible
                java.util.List<String> list=
                        new java.util.ArrayList<String>(endpoint.getConsumerOptions().keySet());
                Collections.sort(list);

                for (String key : list) {
                    String value=endpoint.getConsumerOptions().get(key);

                    value = PolicyDefinitionUtil.replaceParameters(value, endpointQueryParams);

                    newuri += (firstOption?'?':'&') + key + '=' + value;

                    firstOption = false;
                }

            } else if (PolicyDefinitionUtil.isProducer(elemName)) {

                boolean firstOption=(newuri.indexOf('?')==-1);

                // Sort the option keys to make the end result reproducible
                java.util.List<String> list=
                        new java.util.ArrayList<String>(endpoint.getProducerOptions().keySet());
                Collections.sort(list);

                for (String key : list) {
                    String value=endpoint.getProducerOptions().get(key);

                    value = PolicyDefinitionUtil.replaceParameters(value, endpointQueryParams);

                    newuri += (firstOption?'?':'&') + key + '=' + value;

                    firstOption = false;
                }
            } else {
                // TODO: ERROR
                throw new Exception("Invalid element '"+elemName+"'");
            }

        } else if (uri == null) {
            // TODO: ERROR
            throw new Exception("Missing 'uri' attribute");
        }

        return (newuri);
    }

    /**
     * This method adds the supplied resource and its dependencies to the war archive.
     *
     * @param group The policy group
     * @param policy The policy
     * @param resource The resource
     * @param definition The resource definition
     * @param war The war archive
     */
    protected static void generateResource(PolicyGroup group, Policy policy, Resource resource,
                                    String definition, WebArchive war) {
        if (resource.getName().equals(WEB_XML)) {
            war.setWebXML(new StringAsset(definition));
        } else {
            war.addAsWebInfResource(new StringAsset(definition), "classes/"+resource.getName());
        }

        // Add resource dependencies
        addDependencies(war, resource.getDependencies());
    }

    /**
     * This method processes the 'processor'. If the element represents a SCEPTA processor,
     * then it will be configured with the actual physical URI with associated dependencies.
     *
     * @param uri The URI
     * @param group The policy group
     * @param policy The policy
     * @param elem The DOM element representing the input/output element
     * @param war The optional war archive
     * @return Whether this element has been modified
     * @throws Exception Failed to process processor
     */
    protected static boolean processProcessor(String uri, PolicyGroup group, Policy policy, Element elem,
                            WebArchive war) throws Exception {
        String processorName=PolicyDefinitionUtil.getProcessorName(uri);

        if (processorName != null) {

            // Locate the processor definition
            Processor processor=policy.getProcessor(processorName);

            if (processor == null) {
                processor=group.getProcessor(processorName);
            }

            if (processor == null) {
                // TODO: ERROR
                throw new Exception("Unable to find processor '"+processorName+"'");
            }

            elem.setAttribute("uri", processorName);

            // Check if bean has already been configured
            org.w3c.dom.NodeList beans=elem.getOwnerDocument().getDocumentElement().getElementsByTagName("bean");
            boolean f_found=false;

            for (int i=0; !f_found && i < beans.getLength(); i++) {
                org.w3c.dom.Element bean=(org.w3c.dom.Element)beans.item(i);

                f_found = bean.getAttribute("id").equals(processorName);
            }

            if (!f_found) {
                org.w3c.dom.Element bean=elem.getOwnerDocument().createElement("bean");

                bean.setAttribute("id", processorName);
                bean.setAttribute("class", processor.getClassName());

                elem.getOwnerDocument().getDocumentElement().appendChild(bean);
            }

            // Add dependencies
            if (war != null) {
                addDependencies(war, processor.getDependencies());
            }

            return (true);
        }

        return (false);
    }

    /**
     * This method adds a set of maven dependencies to the supplied war archive.
     *
     * @param war The war achieve
     * @param dependencies The set of dependencies
     */
    protected static void addDependencies(WebArchive war, java.util.Set<Dependency> dependencies) {
        for (Dependency d : dependencies) {
            war.addAsLibraries(Maven.resolver().resolve(
                    d.getGroupId()+":"+d.getArtifactId()+":"+d.getVersion())
                    .withTransitivity().asFile());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CharacteristicType> getSupportedCharacteristicTypes() {
        return (CharacteristicProcessorFactory.getCharacteristicTypes());
    }
}
