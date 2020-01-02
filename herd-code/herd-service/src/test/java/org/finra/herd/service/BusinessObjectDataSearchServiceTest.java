package org.finra.herd.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import org.finra.herd.model.api.xml.AttributeValueFilter;
import org.finra.herd.model.api.xml.BusinessObjectData;
import org.finra.herd.model.api.xml.BusinessObjectDataSearchFilter;
import org.finra.herd.model.api.xml.BusinessObjectDataSearchKey;
import org.finra.herd.model.api.xml.BusinessObjectDataSearchRequest;
import org.finra.herd.model.api.xml.PartitionValueFilter;
import org.finra.herd.model.dto.BusinessObjectDataSearchResultPagingInfoDto;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;

public class BusinessObjectDataSearchServiceTest extends AbstractServiceTest
{
    /**
     * The default page number for the business object data search.
     */
    private static final Integer DEFAULT_PAGE_NUMBER = 1;

    /**
     * The default page size for the business object data search.
     */
    private static final Integer DEFAULT_PAGE_SIZE = (Integer) ConfigurationValue.BUSINESS_OBJECT_DATA_SEARCH_MAX_PAGE_SIZE.getDefaultValue();

    /**
     * The page size for the business object data search.
     */
    private static final Integer PAGE_SIZE = 100;

    @Test
    public void testSearchBusinessObjectDataAttributeValueFilters()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        businessObjectDataAttributeDaoTestHelper
            .createBusinessObjectDataAttributeEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null,
                DATA_VERSION, ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1);

        businessObjectDataAttributeDaoTestHelper
            .createBusinessObjectDataAttributeEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null,
                DATA_VERSION, ATTRIBUTE_NAME_2_MIXED_CASE, ATTRIBUTE_VALUE_2);

        businessObjectDataAttributeDaoTestHelper
            .createBusinessObjectDataAttributeEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null,
                DATA_VERSION, ATTRIBUTE_NAME_3_MIXED_CASE, ATTRIBUTE_VALUE_3);

        businessObjectDataAttributeDaoTestHelper
            .createBusinessObjectDataAttributeEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE_2, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null,
                DATA_VERSION, ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1);

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(BDEF_NAME);

        List<AttributeValueFilter> attributeValueFilters = new ArrayList<>();
        attributeValueFilters.add(new AttributeValueFilter(ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1));
        attributeValueFilters.add(new AttributeValueFilter(ATTRIBUTE_NAME_2_MIXED_CASE, null));

        key.setAttributeValueFilters(attributeValueFilters);
        businessObjectDataSearchKeys.add(key);

        BusinessObjectDataSearchFilter filter = new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys);
        filters.add(filter);
        request.setBusinessObjectDataSearchFilters(filters);

        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
        List<BusinessObjectData> resultList = result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements();
        assertEquals(1, resultList.size());

        for (BusinessObjectData data : resultList)
        {
            assertEquals(NAMESPACE, data.getNamespace());
            assertEquals(BDEF_NAME, data.getBusinessObjectDefinitionName());

            assertEquals(2, data.getAttributes().size());
            boolean foundCase1 = false, foundCase2 = false;
            for (int i = 0; i < data.getAttributes().size(); i++)
            {
                if (ATTRIBUTE_NAME_1_MIXED_CASE.equals(data.getAttributes().get(i).getName()))
                {
                    assertEquals(ATTRIBUTE_VALUE_1, data.getAttributes().get(i).getValue());
                    foundCase1 = true;
                }
                if (ATTRIBUTE_NAME_2_MIXED_CASE.equals(data.getAttributes().get(i).getName()))
                {
                    assertEquals(ATTRIBUTE_VALUE_2, data.getAttributes().get(i).getValue());
                    foundCase2 = true;
                }
            }
            assertTrue(foundCase1 && foundCase2);
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataAttributeValueFiltersMissingRequiredParameters()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey businessObjectDataSearchKey = new BusinessObjectDataSearchKey();
        businessObjectDataSearchKey.setNamespace(NAMESPACE);
        businessObjectDataSearchKey.setBusinessObjectDefinitionName(BDEF_NAME);
        businessObjectDataSearchKeys.add(businessObjectDataSearchKey);
        BusinessObjectDataSearchFilter filter = new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys);
        filters.add(filter);
        request.setBusinessObjectDataSearchFilters(filters);

        // Try to search with a null attribute name and a null attribute value.
        List<AttributeValueFilter> attributeValueFilters = new ArrayList<>();
        attributeValueFilters.add(new AttributeValueFilter(null, null));
        businessObjectDataSearchKey.setAttributeValueFilters(attributeValueFilters);
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Either attribute name or attribute value filter must be specified.", ex.getMessage());
        }

        // Try to search with an empty attribute name and a null attribute value.
        attributeValueFilters = new ArrayList<>();
        attributeValueFilters.add(new AttributeValueFilter(" ", null));
        businessObjectDataSearchKey.setAttributeValueFilters(attributeValueFilters);
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Either attribute name or attribute value filter must be specified.", ex.getMessage());
        }

        // Try to search with an empty attribute name and empty attribute value.
        attributeValueFilters = new ArrayList<>();
        attributeValueFilters.add(new AttributeValueFilter(" ", ""));
        businessObjectDataSearchKey.setAttributeValueFilters(attributeValueFilters);
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Either attribute name or attribute value filter must be specified.", ex.getMessage());
        }
    }

    @Test
    public void testSearchBusinessObjectDataAttributeValueFiltersSingleFilter()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        businessObjectDataAttributeDaoTestHelper
            .createBusinessObjectDataAttributeEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null,
                DATA_VERSION, ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1);

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(BDEF_NAME);

        List<AttributeValueFilter> attributeValueFilters = new ArrayList<>();
        attributeValueFilters.add(new AttributeValueFilter(ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1));

        key.setAttributeValueFilters(attributeValueFilters);
        businessObjectDataSearchKeys.add(key);

        BusinessObjectDataSearchFilter filter = new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys);
        filters.add(filter);
        request.setBusinessObjectDataSearchFilters(filters);

        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
        List<BusinessObjectData> resultList = result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements();
        assertEquals(1, resultList.size());

        for (BusinessObjectData data : resultList)
        {
            assertEquals(NAMESPACE, data.getNamespace());
            assertEquals(BDEF_NAME, data.getBusinessObjectDefinitionName());
            assertEquals(ATTRIBUTE_NAME_1_MIXED_CASE, data.getAttributes().get(0).getName());
            assertEquals(ATTRIBUTE_VALUE_1, data.getAttributes().get(0).getValue());
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataAttributeValueFiltersTrimAttributeName()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        businessObjectDataAttributeDaoTestHelper
            .createBusinessObjectDataAttributeEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null,
                DATA_VERSION, ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1);

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(BDEF_NAME);

        List<AttributeValueFilter> attributeValueFilters = new ArrayList<>();
        attributeValueFilters.add(new AttributeValueFilter(addWhitespace(ATTRIBUTE_NAME_1_MIXED_CASE), ATTRIBUTE_VALUE_1));

        key.setAttributeValueFilters(attributeValueFilters);
        businessObjectDataSearchKeys.add(key);

        BusinessObjectDataSearchFilter filter = new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys);
        filters.add(filter);
        request.setBusinessObjectDataSearchFilters(filters);

        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
        List<BusinessObjectData> resultList = result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements();
        assertEquals(1, resultList.size());

        for (BusinessObjectData data : resultList)
        {
            assertEquals(NAMESPACE, data.getNamespace());
            assertEquals(BDEF_NAME, data.getBusinessObjectDefinitionName());
            assertEquals(ATTRIBUTE_NAME_1_MIXED_CASE, data.getAttributes().get(0).getName());
            assertEquals(ATTRIBUTE_VALUE_1, data.getAttributes().get(0).getValue());
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataLatestValidFilterPagingTraverseAllPages()
    {
        // Create test data.
        List<BusinessObjectDataEntity> expectedBusinessObjectDataEntities =
            businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchWithLatestValidFilterTesting();
        assertEquals(3, CollectionUtils.size(expectedBusinessObjectDataEntities));

        // Create a simple search request with the latest valid filter.
        BusinessObjectDataSearchRequest request = businessObjectDataServiceTestHelper.createSimpleBusinessObjectDataSearchRequest(NAMESPACE, BDEF_NAME);
        request.getBusinessObjectDataSearchFilters().get(0).getBusinessObjectDataSearchKeys().get(0)
            .setFilterOnLatestValidVersion(FILTER_ON_LATEST_VALID_VERSION);

        // Go through all expected entities - one page with a single search result at a time.
        int pageNum = 0;
        for (BusinessObjectDataEntity expectedBusinessObjectDataEntity : expectedBusinessObjectDataEntities)
        {
            // Get the relative page with page size set to a single response.
            BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(++pageNum, 1, request);

            // Validate the search results.
            assertNotNull(result);
            assertNotNull(result.getBusinessObjectDataSearchResult());
            assertEquals(1, CollectionUtils.size(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements()));
            assertEquals(new BusinessObjectData(expectedBusinessObjectDataEntity.getId(), NAMESPACE, BDEF_NAME,
                    expectedBusinessObjectDataEntity.getBusinessObjectFormat().getUsage(),
                    expectedBusinessObjectDataEntity.getBusinessObjectFormat().getFileType().getCode(),
                    expectedBusinessObjectDataEntity.getBusinessObjectFormat().getBusinessObjectFormatVersion(),
                    expectedBusinessObjectDataEntity.getBusinessObjectFormat().getPartitionKey(), expectedBusinessObjectDataEntity.getPartitionValue(),
                    NULL_AS_SUBPARTITION_VALUES, expectedBusinessObjectDataEntity.getVersion(), expectedBusinessObjectDataEntity.getLatestVersion(),
                    expectedBusinessObjectDataEntity.getStatus().getCode(), NULL_AS_STORAGE_UNITS, NULL_AS_ATTRIBUTES, NULL_AS_BUSINESS_OBJECT_DATA_PARENTS,
                    NULL_AS_BUSINESS_OBJECT_DATA_CHILDREN, NULL_AS_BUSINESS_OBJECT_DATA_STATUS_HISTORY, NO_RETENTION_EXPIRATION_DATE),
                result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(0));

            // Validate the paging information.
            assertEquals(Long.valueOf(pageNum), result.getPageNum());
            assertEquals(Long.valueOf(1), result.getPageSize());
            assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getPageCount());
            assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
            assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getTotalRecordCount());
            assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
        }

        // Get the first page with page size set to 2.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(1, 2, request);

        // Validate the search results.
        assertNotNull(result);
        assertNotNull(result.getBusinessObjectDataSearchResult());
        assertEquals(2, CollectionUtils.size(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements()));
        assertEquals(Long.valueOf(expectedBusinessObjectDataEntities.get(0).getId()),
            Long.valueOf(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(0).getId()));
        assertEquals(Long.valueOf(expectedBusinessObjectDataEntities.get(1).getId()),
            Long.valueOf(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(1).getId()));

        // Validate the paging information.
        assertEquals(Long.valueOf(1), result.getPageNum());
        assertEquals(Long.valueOf(2), result.getPageSize());
        assertEquals(Long.valueOf(2), result.getPageCount());
        assertEquals(Long.valueOf(2), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(3), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Get the second page with page size set to 2.
        result = businessObjectDataService.searchBusinessObjectData(2, 2, request);

        // Validate the search results.
        assertNotNull(result);
        assertNotNull(result.getBusinessObjectDataSearchResult());
        assertEquals(1, CollectionUtils.size(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements()));
        assertEquals(Long.valueOf(expectedBusinessObjectDataEntities.get(2).getId()),
            Long.valueOf(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(0).getId()));

        // Validate the paging information.
        assertEquals(Long.valueOf(2), result.getPageNum());
        assertEquals(Long.valueOf(2), result.getPageSize());
        assertEquals(Long.valueOf(2), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(3), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Test getting a larger page than there are expected search results.
        result = businessObjectDataService.searchBusinessObjectData(1, CollectionUtils.size(expectedBusinessObjectDataEntities) + 2, request);
        assertEquals(CollectionUtils.size(expectedBusinessObjectDataEntities),
            result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(1), result.getPageNum());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities) + 2), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Test getting a page that does not exist.
        // This is a case when number of records to be skipped due to specified page number and page size is equal to the total record count.
        result = businessObjectDataService.searchBusinessObjectData(4, 1, request);
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(4), result.getPageNum());
        assertEquals(Long.valueOf(1), result.getPageSize());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getPageCount());
        assertEquals(Long.valueOf(0), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Test getting a page that does not exist.
        // This is a case when number of records to be skipped due to specified page number and page size is greater than the total record count.
        result = businessObjectDataService.searchBusinessObjectData(5, 1, request);
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(5), result.getPageNum());
        assertEquals(Long.valueOf(1), result.getPageSize());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getPageCount());
        assertEquals(Long.valueOf(0), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(CollectionUtils.size(expectedBusinessObjectDataEntities)), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataLatestValidFilterRawSearchResultsGreaterThanSearchQueryPaginationSize() throws Exception
    {
        // Create test data.
        List<BusinessObjectDataEntity> expectedBusinessObjectDataEntities =
            businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchWithLatestValidFilterTesting();
        assertEquals(3, CollectionUtils.size(expectedBusinessObjectDataEntities));

        // Create a simple search request with the latest valid filter.
        BusinessObjectDataSearchRequest request = businessObjectDataServiceTestHelper.createSimpleBusinessObjectDataSearchRequest(NAMESPACE, BDEF_NAME);
        request.getBusinessObjectDataSearchFilters().get(0).getBusinessObjectDataSearchKeys().get(0)
            .setFilterOnLatestValidVersion(FILTER_ON_LATEST_VALID_VERSION);

        // Override configuration for business object data search query pagination size to be small enough to require multiple calls to the database to get all
        // raw business object data search results.
        int maxBusinessObjectDataSearchQueryPaginationSize = 2;
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.BUSINESS_OBJECT_DATA_SEARCH_QUERY_PAGINATION_SIZE.getKey(), maxBusinessObjectDataSearchQueryPaginationSize);
        modifyPropertySourceInEnvironment(overrideMap);

        try
        {
            // Get the first page with page size set to 2.
            BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(1, 2, request);

            // Validate the search results.
            assertNotNull(result);
            assertNotNull(result.getBusinessObjectDataSearchResult());
            assertEquals(2, CollectionUtils.size(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements()));
            assertEquals(Long.valueOf(expectedBusinessObjectDataEntities.get(0).getId()),
                Long.valueOf(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(0).getId()));
            assertEquals(Long.valueOf(expectedBusinessObjectDataEntities.get(1).getId()),
                Long.valueOf(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(1).getId()));

            // Validate the paging information.
            assertEquals(Long.valueOf(1), result.getPageNum());
            assertEquals(Long.valueOf(2), result.getPageSize());
            assertEquals(Long.valueOf(2), result.getPageCount());
            assertEquals(Long.valueOf(2), result.getTotalRecordsOnPage());
            assertEquals(Long.valueOf(3), result.getTotalRecordCount());
            assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

            // Get the second page with page size set to 2.
            result = businessObjectDataService.searchBusinessObjectData(2, 2, request);

            // Validate the search results.
            assertNotNull(result);
            assertNotNull(result.getBusinessObjectDataSearchResult());
            assertEquals(1, CollectionUtils.size(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements()));
            assertEquals(Long.valueOf(expectedBusinessObjectDataEntities.get(2).getId()),
                Long.valueOf(result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().get(0).getId()));

            // Validate the paging information.
            assertEquals(Long.valueOf(2), result.getPageNum());
            assertEquals(Long.valueOf(2), result.getPageSize());
            assertEquals(Long.valueOf(2), result.getPageCount());
            assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
            assertEquals(Long.valueOf(3), result.getTotalRecordCount());
            assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testSearchBusinessObjectDataNoFilters()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying all business object data search key parameters, except for filters.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
        for (BusinessObjectData businessObjectData : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, businessObjectData.getNamespace());
            assertEquals(BDEF_NAME, businessObjectData.getBusinessObjectDefinitionName());
            assertEquals(FORMAT_USAGE_CODE, businessObjectData.getBusinessObjectFormatUsage());
            assertEquals(FORMAT_FILE_TYPE_CODE, businessObjectData.getBusinessObjectFormatFileType());
            assertEquals(FORMAT_VERSION, Integer.valueOf(businessObjectData.getBusinessObjectFormatVersion()));
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersLowerCaseParameters()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying all business object data search key parameters in lowercase.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE.toLowerCase(), BDEF_NAME.toLowerCase(), FORMAT_USAGE_CODE.toLowerCase(),
                    FORMAT_FILE_TYPE_CODE.toLowerCase(), FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS, NO_REGISTRATION_DATE_RANGE_FILTER,
                    NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
        for (BusinessObjectData businessObjectData : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, businessObjectData.getNamespace());
            assertEquals(BDEF_NAME, businessObjectData.getBusinessObjectDefinitionName());
            assertEquals(FORMAT_USAGE_CODE, businessObjectData.getBusinessObjectFormatUsage());
            assertEquals(FORMAT_FILE_TYPE_CODE, businessObjectData.getBusinessObjectFormatFileType());
            assertEquals(FORMAT_VERSION, Integer.valueOf(businessObjectData.getBusinessObjectFormatVersion()));
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersMissingOptionalParametersPassedAsNulls()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying only parameters that are required for a business object data search key.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, NO_FORMAT_USAGE_CODE, NO_FORMAT_FILE_TYPE_CODE, NO_FORMAT_VERSION,
                    NO_PARTITION_VALUE_FILTERS, NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION,
                    NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(2, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
        for (BusinessObjectData businessObjectData : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, businessObjectData.getNamespace());
            assertEquals(BDEF_NAME, businessObjectData.getBusinessObjectDefinitionName());
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(2), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersMissingOptionalParametersPassedAsWhitespace()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying only parameters that are required for a business object data search key.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, BLANK_TEXT, BLANK_TEXT, NO_FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(2, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
        for (BusinessObjectData businessObjectData : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, businessObjectData.getNamespace());
            assertEquals(BDEF_NAME, businessObjectData.getBusinessObjectDefinitionName());
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(2), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersMissingRequiredParameters()
    {
        // Try to search business object data without specifying a namespace.
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections.singletonList(
                new BusinessObjectDataSearchFilter(Collections.singletonList(
                    new BusinessObjectDataSearchKey(BLANK_TEXT, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                        NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION,
                        NO_FILTER_ON_RETENTION_EXPIRATION))))));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A namespace must be specified.", e.getMessage());
        }

        // Try to search business object data without specifying a business object definition name.
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections.singletonList(
                new BusinessObjectDataSearchFilter(Collections.singletonList(
                    new BusinessObjectDataSearchKey(BDEF_NAMESPACE, BLANK_TEXT, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION,
                        NO_PARTITION_VALUE_FILTERS, NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION,
                        NO_FILTER_ON_RETENTION_EXPIRATION))))));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A business object definition name must be specified.", e.getMessage());
        }
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersPagingMaxRecordsExceeded() throws Exception
    {
        businessObjectDataDaoTestHelper
            .createBusinessObjectDataEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null, DATA_VERSION,
                true, "VALID");
        businessObjectDataDaoTestHelper
            .createBusinessObjectDataEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, null, DATA_VERSION,
                true, "INVALID");

        businessObjectDataDaoTestHelper
            .createBusinessObjectDataEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE_2, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION_2, PARTITION_VALUE, null,
                DATA_VERSION, true, "INVALID");

        businessObjectDataDaoTestHelper
            .createBusinessObjectDataEntity(NAMESPACE, BDEF_NAME_2, FORMAT_USAGE_CODE_2, FORMAT_FILE_TYPE_CODE_2, FORMAT_VERSION_2, PARTITION_VALUE, null,
                DATA_VERSION, true, "VALID");

        // Override configuration.
        int maxBusinessObjectDataSearchResultCount = 2;
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.BUSINESS_OBJECT_DATA_SEARCH_MAX_RESULT_COUNT.getKey(), maxBusinessObjectDataSearchResultCount);
        modifyPropertySourceInEnvironment(overrideMap);

        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections
                .singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                    new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, NO_FORMAT_USAGE_CODE, NO_FORMAT_FILE_TYPE_CODE, NO_FORMAT_VERSION,
                        NO_PARTITION_VALUE_FILTERS, NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION,
                        NO_FILTER_ON_RETENTION_EXPIRATION))))));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Result limit of %d exceeded. Modify filters to further limit results.", maxBusinessObjectDataSearchResultCount),
                e.getMessage());
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersPagingPageSizeGreaterThanMaximumPageSize()
    {
        // Get the maximum page size configured in the system.
        int maxPageSize = configurationHelper.getProperty(ConfigurationValue.BUSINESS_OBJECT_DATA_SEARCH_MAX_PAGE_SIZE, Integer.class);

        // Try to search business object data.
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, maxPageSize + 1,
                businessObjectDataServiceTestHelper.createSimpleBusinessObjectDataSearchRequest(NAMESPACE, BDEF_NAME));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("A pageSize less than %d must be specified.", maxPageSize), e.getMessage());
        }
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersPagingTraverseAllPages()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        BusinessObjectDataSearchRequest request = businessObjectDataServiceTestHelper.createSimpleBusinessObjectDataSearchRequest(NAMESPACE, BDEF_NAME);

        // Test getting the first page.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(1, 1, request);

        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        for (BusinessObjectData data : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, data.getNamespace());
            assertEquals(BDEF_NAME, data.getBusinessObjectDefinitionName());
            assertEquals(FORMAT_FILE_TYPE_CODE, data.getBusinessObjectFormatFileType());
            assertEquals(FORMAT_USAGE_CODE, data.getBusinessObjectFormatUsage());
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(1), result.getPageNum());
        assertEquals(Long.valueOf(1), result.getPageSize());
        assertEquals(Long.valueOf(2), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Test getting the second page.
        result = businessObjectDataService.searchBusinessObjectData(2, 1, request);

        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        for (BusinessObjectData data : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, data.getNamespace());
            assertEquals(BDEF_NAME, data.getBusinessObjectDefinitionName());
            assertEquals(FORMAT_FILE_TYPE_CODE, data.getBusinessObjectFormatFileType());
            assertEquals(FORMAT_USAGE_CODE_2, data.getBusinessObjectFormatUsage());
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(2), result.getPageNum());
        assertEquals(Long.valueOf(1), result.getPageSize());
        assertEquals(Long.valueOf(2), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Test getting a larger page than there are results remaining
        result = businessObjectDataService.searchBusinessObjectData(1, 3, request);

        assertEquals(2, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(1), result.getPageNum());
        assertEquals(Long.valueOf(3), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(2), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());

        // Test getting a page that does not exist.
        result = businessObjectDataService.searchBusinessObjectData(3, 1, request);

        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(3), result.getPageNum());
        assertEquals(Long.valueOf(1), result.getPageSize());
        assertEquals(Long.valueOf(2), result.getPageCount());
        assertEquals(Long.valueOf(0), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersRelativeEntitiesNoExist()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying all business object data search key parameters, except for filters.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Search business object data by specifying a non-existing namespace.
        result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections
            .singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(I_DO_NOT_EXIST, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Search business object data by specifying a non-existing business object definition name.
        result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections
            .singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, I_DO_NOT_EXIST, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Search business object data by specifying a non-existing business object format usage.
        result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections
            .singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, I_DO_NOT_EXIST, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Search business object data by specifying a non-existing business object format file type.
        result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections
            .singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, I_DO_NOT_EXIST, FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Search business object data by specifying a non-existing business object format version.
        result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections
            .singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION_2, NO_PARTITION_VALUE_FILTERS,
                    NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersTrimParameters()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying all business object data search key string parameters with leading and trailing empty spaces.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(addWhitespace(NAMESPACE), addWhitespace(BDEF_NAME), addWhitespace(FORMAT_USAGE_CODE),
                    addWhitespace(FORMAT_FILE_TYPE_CODE), FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS, NO_REGISTRATION_DATE_RANGE_FILTER,
                    NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
        for (BusinessObjectData businessObjectData : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, businessObjectData.getNamespace());
            assertEquals(BDEF_NAME, businessObjectData.getBusinessObjectDefinitionName());
            assertEquals(FORMAT_USAGE_CODE, businessObjectData.getBusinessObjectFormatUsage());
            assertEquals(FORMAT_FILE_TYPE_CODE, businessObjectData.getBusinessObjectFormatFileType());
            assertEquals(FORMAT_VERSION, Integer.valueOf(businessObjectData.getBusinessObjectFormatVersion()));
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataNoFiltersUpperCaseParameters()
    {
        // Create business object data entities required for testing.
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        // Search business object data by specifying all business object data search key parameters in uppercase.
        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE,
            new BusinessObjectDataSearchRequest(Collections.singletonList(new BusinessObjectDataSearchFilter(Collections.singletonList(
                new BusinessObjectDataSearchKey(NAMESPACE.toUpperCase(), BDEF_NAME.toUpperCase(), FORMAT_USAGE_CODE.toUpperCase(),
                    FORMAT_FILE_TYPE_CODE.toUpperCase(), FORMAT_VERSION, NO_PARTITION_VALUE_FILTERS, NO_REGISTRATION_DATE_RANGE_FILTER,
                    NO_ATTRIBUTE_VALUE_FILTERS, NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));

        // Validate the results.
        assertEquals(1, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());
        for (BusinessObjectData businessObjectData : result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements())
        {
            assertEquals(NAMESPACE, businessObjectData.getNamespace());
            assertEquals(BDEF_NAME, businessObjectData.getBusinessObjectDefinitionName());
            assertEquals(FORMAT_USAGE_CODE, businessObjectData.getBusinessObjectFormatUsage());
            assertEquals(FORMAT_FILE_TYPE_CODE, businessObjectData.getBusinessObjectFormatFileType());
            assertEquals(FORMAT_VERSION, Integer.valueOf(businessObjectData.getBusinessObjectFormatVersion()));
        }

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(1), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(1), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataPartitionValueFilters()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(BDEF_NAME);

        List<PartitionValueFilter> partitionValueFilters = new ArrayList<>();
        key.setPartitionValueFilters(partitionValueFilters);
        PartitionValueFilter partitionValueFilter = new PartitionValueFilter();
        partitionValueFilters.add(partitionValueFilter);
        partitionValueFilter.setPartitionKey(PARTITION_KEY);
        partitionValueFilter.setPartitionValues(Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2));

        businessObjectDataSearchKeys.add(key);
        filters.add(new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys));
        request.setBusinessObjectDataSearchFilters(filters);

        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);

        // The search results expect to contain two business object data instances.
        assertEquals(2, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(1), result.getPageCount());
        assertEquals(Long.valueOf(2), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(2), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataPartitionValueFiltersBusinessObjectDefinitionNoExists()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(I_DO_NOT_EXIST);

        List<PartitionValueFilter> partitionValueFilters = new ArrayList<>();
        key.setPartitionValueFilters(partitionValueFilters);
        PartitionValueFilter partitionValueFilter = new PartitionValueFilter();
        partitionValueFilters.add(partitionValueFilter);
        partitionValueFilter.setPartitionKey(PARTITION_KEY);
        partitionValueFilter.setPartitionValues(Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2));

        businessObjectDataSearchKeys.add(key);
        filters.add(new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys));
        request.setBusinessObjectDataSearchFilters(filters);

        BusinessObjectDataSearchResultPagingInfoDto result = businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);

        // The result list should be empty.
        assertEquals(0, result.getBusinessObjectDataSearchResult().getBusinessObjectDataElements().size());

        // Validate the paging information.
        assertEquals(Long.valueOf(DEFAULT_PAGE_NUMBER), result.getPageNum());
        assertEquals(Long.valueOf(PAGE_SIZE), result.getPageSize());
        assertEquals(Long.valueOf(0), result.getPageCount());
        assertEquals(Long.valueOf(0), result.getTotalRecordsOnPage());
        assertEquals(Long.valueOf(0), result.getTotalRecordCount());
        assertEquals(Long.valueOf(DEFAULT_PAGE_SIZE), result.getMaxResultsPerPage());
    }

    @Test
    public void testSearchBusinessObjectDataPartitionValueFiltersInvalidPartitionKey()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(BDEF_NAME);
        key.setBusinessObjectFormatUsage(FORMAT_USAGE_CODE);
        key.setBusinessObjectFormatFileType(FORMAT_FILE_TYPE_CODE);
        key.setBusinessObjectFormatVersion(FORMAT_VERSION);

        List<PartitionValueFilter> partitionValueFilters = new ArrayList<>();
        key.setPartitionValueFilters(partitionValueFilters);
        PartitionValueFilter partitionValueFilterA = new PartitionValueFilter();
        partitionValueFilters.add(partitionValueFilterA);
        partitionValueFilterA.setPartitionKey(INVALID_VALUE);
        partitionValueFilterA.setPartitionValues(Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2));
        PartitionValueFilter partitionValueFilterB = new PartitionValueFilter();
        partitionValueFilters.add(partitionValueFilterB);
        partitionValueFilterB.setPartitionKey(INVALID_VALUE_2);
        partitionValueFilterB.setPartitionValues(Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2));

        businessObjectDataSearchKeys.add(key);
        filters.add(new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys));
        request.setBusinessObjectDataSearchFilters(filters);

        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("There are no registered business object formats with \"%s\" namespace, \"%s\" business object definition name, " +
                    "\"%s\" business object format usage, \"%s\" business object format file type, \"%s\" business object format version " +
                    "that have schema with partition columns matching \"%s, %s\" partition key(s).", NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE,
                FORMAT_VERSION, INVALID_VALUE, INVALID_VALUE_2), e.getMessage());
        }
    }

    @Test
    public void testSearchBusinessObjectDataPartitionValueFiltersInvalidPartitionKeyMissingOptionalParameters()
    {
        businessObjectDataServiceTestHelper.createDatabaseEntitiesForBusinessObjectDataSearchTesting();

        BusinessObjectDataSearchRequest request = new BusinessObjectDataSearchRequest();
        List<BusinessObjectDataSearchFilter> filters = new ArrayList<>();
        List<BusinessObjectDataSearchKey> businessObjectDataSearchKeys = new ArrayList<>();
        BusinessObjectDataSearchKey key = new BusinessObjectDataSearchKey();
        key.setNamespace(NAMESPACE);
        key.setBusinessObjectDefinitionName(BDEF_NAME);

        List<PartitionValueFilter> partitionValueFilters = new ArrayList<>();
        key.setPartitionValueFilters(partitionValueFilters);
        PartitionValueFilter partitionValueFilterA = new PartitionValueFilter();
        partitionValueFilters.add(partitionValueFilterA);
        partitionValueFilterA.setPartitionKey(INVALID_VALUE);
        partitionValueFilterA.setPartitionValues(Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2));
        PartitionValueFilter partitionValueFilterB = new PartitionValueFilter();
        partitionValueFilters.add(partitionValueFilterB);
        partitionValueFilterB.setPartitionKey(INVALID_VALUE_2);
        partitionValueFilterB.setPartitionValues(Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2));

        businessObjectDataSearchKeys.add(key);
        filters.add(new BusinessObjectDataSearchFilter(businessObjectDataSearchKeys));
        request.setBusinessObjectDataSearchFilters(filters);

        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, request);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("There are no registered business object formats with \"%s\" namespace, \"%s\" business object definition name " +
                    "that have schema with partition columns matching \"%s, %s\" partition key(s).", NAMESPACE, BDEF_NAME, INVALID_VALUE, INVALID_VALUE_2),
                e.getMessage());
        }
    }

    @Test
    public void testSearchBusinessObjectDataPartitionValueFiltersMissingRequiredParameters()
    {
        try
        {
            businessObjectDataService.searchBusinessObjectData(DEFAULT_PAGE_NUMBER, PAGE_SIZE, new BusinessObjectDataSearchRequest(Collections.singletonList(
                new BusinessObjectDataSearchFilter(Collections.singletonList(
                    new BusinessObjectDataSearchKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, Collections
                        .singletonList(
                            new PartitionValueFilter(NO_PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                                NO_LATEST_AFTER_PARTITION_VALUE)), NO_REGISTRATION_DATE_RANGE_FILTER, NO_ATTRIBUTE_VALUE_FILTERS,
                        NO_FILTER_ON_LATEST_VALID_VERSION, NO_FILTER_ON_RETENTION_EXPIRATION))))));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition key must be specified.", e.getMessage());
        }
    }
}
