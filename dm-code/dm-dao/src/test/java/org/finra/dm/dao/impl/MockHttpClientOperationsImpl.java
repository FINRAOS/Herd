/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.dm.dao.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.finra.dm.core.helper.ConfigurationHelper;
import org.finra.dm.dao.HttpClientOperations;
import org.finra.dm.dao.helper.XmlHelper;
import org.finra.dm.model.api.xml.Attribute;
import org.finra.dm.model.api.xml.BusinessObjectData;
import org.finra.dm.model.api.xml.S3KeyPrefixInformation;
import org.finra.dm.model.api.xml.Storage;
import org.finra.dm.model.api.xml.StorageFile;
import org.finra.dm.model.api.xml.StorageUnit;
import org.finra.dm.model.dto.ConfigurationValue;
import org.finra.dm.model.jpa.BusinessObjectDataStatusEntity;
import org.finra.dm.model.jpa.StorageEntity;
import org.finra.dm.model.jpa.StoragePlatformEntity;

/**
 * Mock implementation of HTTP client operations.
 */
public class MockHttpClientOperationsImpl implements HttpClientOperations
{
    private static final Logger LOGGER = Logger.getLogger(MockHttpClientOperationsImpl.class);

    public static final String HOSTNAME_THROW_IO_EXCEPTION_DURING_POST = "testThrowIoExceptionDuringPost";
    public static final String HOSTNAME_THROW_IO_EXCEPTION_DURING_GET_STORAGES = "testThrowIoExceptionDuringGetStorages";

    @Autowired
    private XmlHelper xmlHelper;

    @Autowired
    protected ConfigurationHelper configurationHelper;

    @Override
    public CloseableHttpResponse execute(CloseableHttpClient httpClient, HttpUriRequest request) throws IOException, JAXBException
    {
        LOGGER.debug("request = " + request);

        ProtocolVersion protocolVersion = new ProtocolVersion("http", 1, 1);
        StatusLine statusLine = new BasicStatusLine(protocolVersion, HttpStatus.SC_OK, "Success");
        MockCloseableHttpResponse response = new MockCloseableHttpResponse(statusLine);

        // Find out which API's are being called and build an appropriate response.
        if (request instanceof HttpGet)
        {
            URI uri = request.getURI();
            if (uri.getPath().startsWith("/dm-app/rest/businessObjectData/"))
            {
                if (uri.getPath().endsWith("s3KeyPrefix"))
                {
                    buildGetS3KeyPrefixResponse(response, uri);
                }
                else
                {
                    buildGetBusinessObjectDataResponse(response, uri);
                }
            }
            else if (uri.getPath().startsWith("/dm-app/rest/storages/"))
            {
                checkHostname(request, HOSTNAME_THROW_IO_EXCEPTION_DURING_GET_STORAGES);
                buildGetStorageResponse(response, uri);
            }
        }
        else if (request instanceof HttpPost)
        {
            checkHostname(request, HOSTNAME_THROW_IO_EXCEPTION_DURING_POST);
        }

        LOGGER.debug("response = " + response);
        return response;
    }

    /**
     * Check the hostname to see if we should throw an exception.
     *
     * @param request the HTTP request.
     * @param hostnameToThrowException the hostname that will cause an exception to be thrown.
     *
     * @throws IOException if the hostname suggests that we should thrown this exception.
     */
    private void checkHostname(HttpUriRequest request, String hostnameToThrowException) throws IOException
    {
        // We don't have mocking for HttpPost operations yet (e.g. business object data registration) - just exception throwing as needed.
        String hostname = request.getURI().getHost();
        if (hostname != null)
        {
            if (hostname.contains(hostnameToThrowException))
            {
                throw new IOException(hostnameToThrowException);
            }
        }
    }

    /**
     * Builds a Get S3 Key Prefix response.
     *
     * @param response the response.
     * @param uri the URI of the incoming request.
     *
     * @throws JAXBException if a JAXB error occurred.
     */
    private void buildGetS3KeyPrefixResponse(MockCloseableHttpResponse response, URI uri) throws JAXBException
    {
        Pattern pattern = Pattern.compile("/dm-app/rest/businessObjectData(/namespaces/(?<namespace>.*?))?" +
            "/businessObjectDefinitionNames/(?<businessObjectDefinitionName>.*?)/businessObjectFormatUsages/(?<businessObjectFormatUsage>.*?)" +
            "/businessObjectFormatFileTypes/(?<businessObjectFormatFileType>.*?)/businessObjectFormatVersions/(?<businessObjectFormatVersion>.*?)" +
            "/s3KeyPrefix");
        Matcher matcher = pattern.matcher(uri.getPath());
        if (matcher.find())
        {
            S3KeyPrefixInformation s3KeyPrefixInformation = new S3KeyPrefixInformation();
            String namespace = getGroup(matcher, "namespace");
            namespace = namespace == null ? "testNamespace" : namespace;
            String businessObjectFormatUsage = getGroup(matcher, "businessObjectFormatUsage");
            String businessObjectFormatType = getGroup(matcher, "businessObjectFormatFileType");
            String businessObjectDefinitionName = getGroup(matcher, "businessObjectDefinitionName");
            String businessObjectFormatVersion = getGroup(matcher, "businessObjectFormatVersion");
            s3KeyPrefixInformation
                .setS3KeyPrefix(namespace.toLowerCase().replace('_', '-') + "/exchange-a/" + businessObjectFormatUsage.toLowerCase().replace('_', '-') + "/" +
                    businessObjectFormatType.toLowerCase().replace('_', '-') + "/" + businessObjectDefinitionName.toLowerCase().replace('_', '-') + "/frmt-v" +
                    businessObjectFormatVersion + "/data-v0/process-date=2014-01-31");

            response.setEntity(getHttpEntity(s3KeyPrefixInformation));
        }
    }

    private String getGroup(Matcher matcher, String groupName)
    {
        try
        {
            return matcher.group(groupName);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            return null;
        }
    }

    /**
     * Builds a business object data response.
     *
     * @param response the response.
     * @param uri the URI of the incoming request.
     *
     * @throws JAXBException if a JAXB error occurred.
     */
    private void buildGetBusinessObjectDataResponse(MockCloseableHttpResponse response, URI uri) throws JAXBException
    {
        Pattern pattern = Pattern.compile("/dm-app/rest/businessObjectData/namespaces/(.*)/businessObjectDefinitionNames/(.*)/businessObjectFormatUsages/(.*)" +
            "/businessObjectFormatFileTypes/(.*).*");
        Matcher matcher = pattern.matcher(uri.getPath());
        if (matcher.find())
        {
            BusinessObjectData businessObjectData = new BusinessObjectData();
            businessObjectData.setNamespace(matcher.group(1));
            businessObjectData.setBusinessObjectDefinitionName(matcher.group(2));
            businessObjectData.setBusinessObjectFormatUsage(matcher.group(3));
            businessObjectData.setBusinessObjectFormatFileType(matcher.group(4));
            businessObjectData.setPartitionValue("2014-01-31");
            businessObjectData.setPartitionKey("PROCESS_DATE");
            businessObjectData.setAttributes(new ArrayList<Attribute>());
            businessObjectData.setBusinessObjectFormatVersion(0);
            businessObjectData.setLatestVersion(true);
            businessObjectData.setStatus(BusinessObjectDataStatusEntity.VALID);

            List<StorageUnit> storageUnits = new ArrayList<>();
            businessObjectData.setStorageUnits(storageUnits);

            StorageUnit storageUnit = new StorageUnit();
            storageUnits.add(storageUnit);

            storageUnit.setStorage(getNewStorage(StorageEntity.MANAGED_STORAGE));

            List<StorageFile> storageFiles = new ArrayList<>();
            storageUnit.setStorageFiles(storageFiles);

            List<String> localFiles = Arrays.asList("foo1.dat", "Foo2.dat", "FOO3.DAT", "folder/foo3.dat", "folder/foo2.dat", "folder/foo1.dat");
            for (String filename : localFiles)
            {
                StorageFile storageFile = new StorageFile();
                storageFiles.add(storageFile);
                storageFile.setFilePath(businessObjectData.getNamespace().toLowerCase().replace('_', '-') + "/exchange-a/" +
                    businessObjectData.getBusinessObjectFormatUsage().toLowerCase().replace('_', '-') + "/" +
                    businessObjectData.getBusinessObjectFormatFileType().toLowerCase().replace('_', '-') + "/" +
                    businessObjectData.getBusinessObjectDefinitionName().toLowerCase().replace('_', '-') + "/frmt-v" +
                    businessObjectData.getBusinessObjectFormatVersion() + "/data-v" + businessObjectData.getVersion() + "/" +
                    businessObjectData.getPartitionKey().toLowerCase().replace('_', '-') +
                    "=" + businessObjectData.getPartitionValue() + "/" + filename);
                storageFile.setFileSizeBytes(1024L);
                storageFile.setRowCount(10L);
            }

            businessObjectData.setSubPartitionValues(new ArrayList<String>());
            businessObjectData.setId(1234);
            businessObjectData.setVersion(0);

            response.setEntity(getHttpEntity(businessObjectData));
        }
    }

    /**
     * Builds a Get Storage response.
     *
     * @param response the response.
     * @param uri the URI of the incoming request.
     *
     * @throws JAXBException if a JAXB error occurred.
     */
    private void buildGetStorageResponse(MockCloseableHttpResponse response, URI uri) throws JAXBException
    {
        Pattern pattern = Pattern.compile("/dm-app/rest/storages/(.*)");
        Matcher matcher = pattern.matcher(uri.getPath());
        if (matcher.find())
        {
            Storage storage = getNewStorage(matcher.group(1));
            response.setEntity(getHttpEntity(storage));
        }
    }

    /**
     * Gets a new storage object with the specified information.
     *
     * @param storageName the storage name.
     *
     * @return the newly created storage.
     */
    private Storage getNewStorage(String storageName)
    {
        Storage storage = new Storage();
        storage.setName(storageName);
        storage.setStoragePlatformName(StoragePlatformEntity.S3);

        List<Attribute> attributes = new ArrayList<>();
        Attribute attribute = new Attribute(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME), "testBucket");
        attributes.add(attribute);
        storage.setAttributes(attributes);

        return storage;
    }

    private HttpEntity getHttpEntity(Object content) throws UnsupportedCharsetException, JAXBException
    {
        String xml = xmlHelper.objectToXml(content);
        LOGGER.debug("xml = " + xml);
        ContentType contentType = ContentType.APPLICATION_XML.withCharset(StandardCharsets.UTF_8);
        return new StringEntity(xml, contentType);
    }
}
