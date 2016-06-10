/*
 *  Copyright 2016 Aino.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.aino.agents.core.config;

import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.Iterator;

/**
 * Class for reading configuration file from InputStream.
 */
public class InputStreamConfigBuilder implements AgentConfigBuilder {
    private static final Log log = LogFactory.getLog(InputStreamConfigBuilder.class);
    private static final String LOGGER_SCHEMA = "Logger.xsd";

    private static QName CONFIG_ENABLED_ATT_Q = new QName("enabled");
    private static QName CONFIG_LOGGER_SERVICE_Q = new QName("ainoLoggerService");
    private static QName CONFIG_ADDRESS_Q = new QName("address");
    private static QName CONFIG_SEND_Q = new QName("send");
    private static QName CONFIG_URI_ATT_Q = new QName("uri");
    private static QName CONFIG_APIKEY_ATT_Q = new QName("apiKey");
    private static QName CONFIG_INTERVAL_ATT_Q = new QName("interval");
    private static QName CONFIG_SIZE_THRESHOLD_ATT_Q = new QName("sizeThreshold");
    private static QName CONFIG_GZIP_ENABLED_ATT_Q = new QName("gzipEnabled");
    private static QName CONFIG_PROXY_Q = new QName("proxy");
    private static QName CONFIG_HOST_ATT_Q = new QName("host");
    private static QName CONFIG_PORT_ATT_Q = new QName("port");

    private static QName CONFIG_OPERATIONS_Q = new QName("operations");
    private static QName CONFIG_IDTYPES_Q = new QName("idTypes");
    private static QName CONFIG_PAYLOADTYPES_Q = new QName("payloadTypes");
    private static QName CONFIG_APPLICATIONS_Q = new QName("applications");
    private static QName CONFIG_KEY_ATT_Q = new QName("key");
    private static QName CONFIG_NAME_ATT_Q = new QName("name");

    private final InputStream stream;

    /**
     * Constructor.
     *
     * @param stream InputStream to read the configuration from
     */
    public InputStreamConfigBuilder(InputStream stream) {
        if(null == stream){
            throw new NullPointerException("Stream cannot be null");
        }
        this.stream = convertToByteStream(stream);
    }

    @Override
    public AgentConfig build() {
        return configFromInputStream();
    }

    private AgentConfig configFromInputStream() {
        validateSchema(getStream());

        OMElement configElement = parseConfigElement(getStream());
        OMElement serviceElement = configElement.getFirstChildWithName(CONFIG_LOGGER_SERVICE_Q);
        OMElement operationsElement = configElement.getFirstChildWithName(CONFIG_OPERATIONS_Q);
        OMElement applicationsElement = configElement.getFirstChildWithName(CONFIG_APPLICATIONS_Q);
        OMElement idTypesElement = configElement.getFirstChildWithName(CONFIG_IDTYPES_Q);
        OMElement payloadTypesElement = configElement.getFirstChildWithName(CONFIG_PAYLOADTYPES_Q);

        AgentConfig config = new AgentConfig();

        if (isServiceEnabled(serviceElement)) {
            applyServiceSettings(config, serviceElement);
            applyOperationSettings(config, operationsElement);
            applyApplicationSettings(config, applicationsElement);
            applyIdTypeSettings(config, idTypesElement);
            applyPayloadTypeSettings(config, payloadTypesElement);
        }

        config.setEnabled(isServiceEnabled(serviceElement));
        this.closeStream();
        return config;
    }

    /**
     * Validates the config file against XSD schema.
     *
     * @param stream InputStream to config file.
     */
    private void validateSchema(InputStream stream) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream xsdStream = this.getClass().getClassLoader().getResourceAsStream(LOGGER_SCHEMA);
            Schema schema = factory.newSchema(new StreamSource(xsdStream));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(stream));
        } catch (SAXException e) {
            throw new InvalidAgentConfigException("Failed to validate logger config.", e);
        } catch (IOException e) {
            throw new InvalidAgentConfigException("Failed to validate logger config.", e);
        }
    }

    private boolean isServiceEnabled(OMElement serviceElement){
        if(null == serviceElement){
            return false;
        }
        return Boolean.valueOf(serviceElement.getAttributeValue(CONFIG_ENABLED_ATT_Q));
    }

    private void applyServiceSettings(AgentConfig config, OMElement serviceElement) {

        OMElement addressElement = serviceElement.getFirstChildWithName(CONFIG_ADDRESS_Q);
        OMElement proxyElement = serviceElement.getFirstChildWithName(CONFIG_PROXY_Q);
        OMElement sendElement = serviceElement.getFirstChildWithName(CONFIG_SEND_Q);

        if (null == addressElement || null == sendElement) {
            throw new InvalidAgentConfigException("The logger config does not contain all of the required elements for the logger service configuration.");
        }

        config.setLogServiceUri(addressElement.getAttributeValue(CONFIG_URI_ATT_Q));
        config.setApiKey(addressElement.getAttributeValue(CONFIG_APIKEY_ATT_Q));
        config.setSendInterval(Integer.parseInt(sendElement.getAttributeValue(CONFIG_INTERVAL_ATT_Q)));
        config.setSizeThreshold(Integer.parseInt(sendElement.getAttributeValue(CONFIG_SIZE_THRESHOLD_ATT_Q)));
        config.setGzipEnabled(Boolean.parseBoolean(sendElement.getAttributeValue(CONFIG_GZIP_ENABLED_ATT_Q)));

        if(null != proxyElement){
            config.setProxyHost(proxyElement.getAttributeValue(CONFIG_HOST_ATT_Q));
            config.setProxyPort(Integer.parseInt(proxyElement.getAttributeValue(CONFIG_PORT_ATT_Q)));
        }
    }

    private void applyKeyNameElementSettings(AgentConfig config, OMElement elementList, AgentConfig.KeyNameElementType type) {
        Iterator i = elementList.getChildElements();
        while(i.hasNext()){
            OMElement element = (OMElement) i.next();
            String key = element.getAttributeValue(CONFIG_KEY_ATT_Q);
            String name = element.getAttributeValue(CONFIG_NAME_ATT_Q);

            if(config.get(type).entryExists(key)){
                throw new InvalidAgentConfigException("Duplicate key: " + key + " for type: " + type.name());
            }
            config.get(type).addEntry(key, name);
        }
    }

    private void applyOperationSettings(AgentConfig config, OMElement operationsElement) {
        applyKeyNameElementSettings(config, operationsElement, AgentConfig.KeyNameElementType.OPERATIONS);
    }

    private void applyPayloadTypeSettings(AgentConfig config, OMElement payloadTypeElement) {
        applyKeyNameElementSettings(config, payloadTypeElement, AgentConfig.KeyNameElementType.PAYLOADTYPES);
    }

    private void applyApplicationSettings(AgentConfig config, OMElement applicationsElement) {
        applyKeyNameElementSettings(config, applicationsElement, AgentConfig.KeyNameElementType.APPLICATIONS);
    }

    private void applyIdTypeSettings(AgentConfig config, OMElement idTypesElement) {
        applyKeyNameElementSettings(config, idTypesElement, AgentConfig.KeyNameElementType.IDTYPES);
    }

    private OMElement parseConfigElement(InputStream stream) {
        try {
             return new StAXOMBuilder(stream).getDocumentElement();
        } catch (XMLStreamException e) {
            throw new InvalidAgentConfigException("Unable to read logger config.", e);
        }
    }

    private ByteArrayInputStream convertToByteStream(InputStream stream) {
        try {
            //This is done in order to ensure the stream has all required capabilities such as reset
            ByteArrayInputStream bais =  new ByteArrayInputStream(IOUtils.getStreamAsByteArray(stream));
            stream.close();
            return bais;
        } catch(IOException e) {
            throw new InvalidAgentConfigException("Could not convert logger config input stream to ByteArrayInputStream.", e);
        }
    }

    private InputStream getStream() {
        try {
            stream.reset();
            return stream;
        } catch (IOException e) {
            throw new InvalidAgentConfigException("Failed to reset() stream.", e);
        }
    }

    private void closeStream() {
        try {
            this.getStream().close();
        } catch (IOException e) {
            //This should happen just about never
            throw new InvalidAgentConfigException("Could not close internal ByteArrayInputStream.", e);
        }
    }
}
