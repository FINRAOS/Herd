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
package org.finra.herd.dao;

import static org.finra.herd.dao.helper.ElasticsearchHelper.DISPLAY_NAME_SOURCE;
import static org.finra.herd.dao.helper.ElasticsearchHelper.TAG_TYPE_FACET_AGGS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.listeners.CollectCreatedMocks;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.helper.ElasticsearchHelper;
import org.finra.herd.dao.helper.JestClientHelper;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.dao.impl.IndexSearchDaoImpl;
import org.finra.herd.model.api.xml.IndexSearchFilter;
import org.finra.herd.model.api.xml.IndexSearchKey;
import org.finra.herd.model.api.xml.IndexSearchRequest;
import org.finra.herd.model.api.xml.IndexSearchResponse;
import org.finra.herd.model.api.xml.IndexSearchResult;
import org.finra.herd.model.api.xml.IndexSearchResultTypeKey;
import org.finra.herd.model.api.xml.TagKey;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.dto.ElasticsearchResponseDto;
import org.finra.herd.model.dto.IndexSearchHighlightField;
import org.finra.herd.model.dto.IndexSearchHighlightFields;
import org.finra.herd.model.dto.ResultTypeIndexSearchResponseDto;
import org.finra.herd.model.dto.TagIndexSearchResponseDto;
import org.finra.herd.model.dto.TagTypeIndexSearchResponseDto;

/**
 * IndexSearchDaoTest
 */
public class IndexSearchDaoTest extends AbstractDaoTest
{
    private static final String NAMESPACE = "namespace";

    private static final String TAG_TYPE = "tagType";

    private List<Object> createdMocks;

    @InjectMocks
    private IndexSearchDaoImpl indexSearchDao;

    @Mock
    private ConfigurationHelper configurationHelper;

    @Mock
    private ElasticsearchHelper elasticsearchHelper;

    @Mock
    private JestClientHelper jestClientHelper;

    @Mock
    private JsonHelper jsonHelper;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        createdMocks = new LinkedList<>();
        final MockingProgress progress = new ThreadSafeMockingProgress();
        progress.setListener(new CollectCreatedMocks(createdMocks));
    }

    @Test
    public void indexSearchTest() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = Sets.newHashSet(DISPLAY_NAME_FIELD, SHORT_DESCRIPTION_FIELD);
        testIndexSearch(fields, null, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithExceptions() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = Sets.newHashSet(DISPLAY_NAME_FIELD, SHORT_DESCRIPTION_FIELD);
        testIndexSearch(fields, null, null, HIT_HIGHLIGHTING_DISABLED, true);
    }

    @Test
    public void indexSearchTestWithNoFields() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();
        testIndexSearch(fields, null, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithFacets() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();
        //tag and result set facet
        testIndexSearch(fields, null, Arrays.asList(ElasticsearchHelper.RESULT_TYPE_FACET, ElasticsearchHelper.TAG_FACET), HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithTagFacet() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        //tag facet only
        testIndexSearch(fields, null, Collections.singletonList(ElasticsearchHelper.TAG_FACET), HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithResultTypeFacet() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        //result type facet only
        testIndexSearch(fields, null, Collections.singletonList(ElasticsearchHelper.RESULT_TYPE_FACET), HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithEmptyFilters() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        // Create a new filters list
        List<IndexSearchFilter> searchFilters = new ArrayList<>();

        //result type facet only
        testIndexSearch(fields, searchFilters, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithTagKeyFilter() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        // Create an index search key
        final IndexSearchKey indexSearchKey = new IndexSearchKey();

        // Create a tag key
        final TagKey tagKey = new TagKey(TAG_TYPE_CODE, TAG_CODE);
        indexSearchKey.setTagKey(tagKey);

        // Create an index search keys list and add the previously defined key to it
        final List<IndexSearchKey> indexSearchKeys = Collections.singletonList(indexSearchKey);

        // Create an index search filter with the keys previously defined
        final IndexSearchFilter indexSearchFilter = new IndexSearchFilter(NO_EXCLUSION_SEARCH_FILTER, indexSearchKeys);

        List<IndexSearchFilter> indexSearchFilters = Collections.singletonList(indexSearchFilter);

        //result type facet only
        testIndexSearch(fields, indexSearchFilters, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithResultTypeFilter() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        // Create an index search key
        final IndexSearchKey indexSearchKey = new IndexSearchKey();

        // Create a result type key
        final IndexSearchResultTypeKey resultTypeKey = new IndexSearchResultTypeKey(BUSINESS_OBJECT_DEFINITION_INDEX);
        indexSearchKey.setIndexSearchResultTypeKey(resultTypeKey);

        // Create an index search keys list and add the previously defined key to it
        final List<IndexSearchKey> indexSearchKeys = Collections.singletonList(indexSearchKey);

        // Create an index search filter with the keys previously defined
        final IndexSearchFilter indexSearchFilter = new IndexSearchFilter(NO_EXCLUSION_SEARCH_FILTER, indexSearchKeys);

        List<IndexSearchFilter> indexSearchFilters = Collections.singletonList(indexSearchFilter);

        //result type facet only
        testIndexSearch(fields, indexSearchFilters, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithTagKeyFilterAndExcludeFlagSet() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        // Create an index search key
        final IndexSearchKey indexSearchKey = new IndexSearchKey();

        // Create a tag key
        final TagKey tagKey = new TagKey(TAG_TYPE_CODE, TAG_CODE);
        indexSearchKey.setTagKey(tagKey);

        // Create an index search keys list and add the previously defined key to it
        final List<IndexSearchKey> indexSearchKeys = Collections.singletonList(indexSearchKey);

        // Create an index search filter with the keys previously defined
        final IndexSearchFilter indexSearchFilter = new IndexSearchFilter(NO_EXCLUSION_SEARCH_FILTER, indexSearchKeys);

        // Set exclude filter flag to true
        indexSearchFilter.setIsExclusionSearchFilter(true);

        List<IndexSearchFilter> indexSearchFilters = Collections.singletonList(indexSearchFilter);

        //result type facet only
        testIndexSearch(fields, indexSearchFilters, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchTestWithResultTypeFilterAndExcludeFlagSet() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        // Create an index search key
        final IndexSearchKey indexSearchKey = new IndexSearchKey();

        // Create a result type key
        final IndexSearchResultTypeKey resultTypeKey = new IndexSearchResultTypeKey(BUSINESS_OBJECT_DEFINITION_INDEX);
        indexSearchKey.setIndexSearchResultTypeKey(resultTypeKey);

        // Create an index search keys list and add the previously defined key to it
        final List<IndexSearchKey> indexSearchKeys = Collections.singletonList(indexSearchKey);

        // Create an index search filter with the keys previously defined
        final IndexSearchFilter indexSearchFilter = new IndexSearchFilter(NO_EXCLUSION_SEARCH_FILTER, indexSearchKeys);

        // Set exclude flag to true
        indexSearchFilter.setIsExclusionSearchFilter(true);

        List<IndexSearchFilter> indexSearchFilters = Collections.singletonList(indexSearchFilter);

        //result type facet only
        testIndexSearch(fields, indexSearchFilters, null, HIT_HIGHLIGHTING_DISABLED, false);
    }

    @Test
    public void indexSearchWithResultTypeExcludeFilterAndHitHighlighting() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        // Create an index search key
        final IndexSearchKey indexSearchKey = new IndexSearchKey();

        // Create a result type key
        final IndexSearchResultTypeKey resultTypeKey = new IndexSearchResultTypeKey(BUSINESS_OBJECT_DEFINITION_INDEX);
        indexSearchKey.setIndexSearchResultTypeKey(resultTypeKey);

        // Create an index search keys list and add the previously defined key to it
        final List<IndexSearchKey> indexSearchKeys = Collections.singletonList(indexSearchKey);

        // Create an index search filter with the keys previously defined
        final IndexSearchFilter indexSearchFilter = new IndexSearchFilter(NO_EXCLUSION_SEARCH_FILTER, indexSearchKeys);

        // Set exclude flag to true
        indexSearchFilter.setIsExclusionSearchFilter(true);

        List<IndexSearchFilter> indexSearchFilters = Collections.singletonList(indexSearchFilter);

        //result type facet only
        testIndexSearch(fields, indexSearchFilters, null, HIT_HIGHLIGHTING_ENABLED, false);
    }

    @Test
    public void indexSearchWithHighlightingEnabled() throws IOException
    {
        // Create a new fields set that will be used when testing the index search method
        final Set<String> fields = new HashSet<>();

        testIndexSearch(fields, null, null, HIT_HIGHLIGHTING_ENABLED, false);
    }

    private void testIndexSearch(Set<String> fields, List<IndexSearchFilter> searchFilters, List<String> facetList, boolean isHitHighlightingEnabled,
        boolean testExceptions) throws IOException
    {
        // Build the mocks
        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithSource = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithSize = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithSorting = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithHighlighting = mock(SearchRequestBuilder.class);

        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit searchHit1 = mock(SearchHit.class);
        SearchHit searchHit2 = mock(SearchHit.class);
        SearchShardTarget searchShardTarget1 = mock(SearchShardTarget.class);
        SearchShardTarget searchShardTarget2 = mock(SearchShardTarget.class);
        SearchHit[] searchHitArray = new SearchHit[2];
        searchHitArray[0] = searchHit1;
        searchHitArray[1] = searchHit2;

        HighlightField highlightField = mock(HighlightField.class);
        when(highlightField.getName()).thenReturn("displayName");

        Text[] value = {new Text("match <hlt>fragment</hlt class=\"highlight\">"), new Text("<hlt class=\"highlight\">match</hlt>")};
        when(highlightField.getFragments()).thenReturn(value);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<SearchResponse> listenableActionFuture = mock(ListenableActionFuture.class);

        final String highlightFieldsConfigValue =
            "{\"highlightFields\":[{\"fieldName\":\"displayName\",\"fragmentSize\":100,\"matchedFields\":[\"displayName\",\"displayName.stemmed\",\"displayName.ngrams\"],\"numOfFragments\":5}]}";

        // Mock the call to external methods
        when(configurationHelper.getProperty(ConfigurationValue.TAG_SHORT_DESCRIPTION_LENGTH, Integer.class)).thenReturn(300);
        when(configurationHelper.getProperty(ConfigurationValue.BUSINESS_OBJECT_DEFINITION_SHORT_DESCRIPTION_LENGTH, Integer.class)).thenReturn(300);
        when(configurationHelper.getProperty(ConfigurationValue.ELASTICSEARCH_SEARCHABLE_FIELDS_NGRAMS)).thenReturn("{\"displayName\":\"1.0\"}");
        when(configurationHelper.getProperty(ConfigurationValue.ELASTICSEARCH_SEARCHABLE_FIELDS_STEMMED)).thenReturn("{\"displayName\":\"1.0\"}");
        when(configurationHelper.getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_PRETAGS)).thenReturn("<hlt class=\"highlight\">");
        when(configurationHelper.getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_POSTTAGS)).thenReturn("</hlt>");
        when(configurationHelper.getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_FIELDS)).thenReturn(highlightFieldsConfigValue);

        Map<String, String> fieldsBoostMap = new HashMap<>();
        fieldsBoostMap.put("displayName", "1.0");

        if (testExceptions)
        {
            when(jsonHelper.unmarshallJsonToObject(Map.class, "{\"displayName\":\"1.0\"}")).thenThrow(new IOException());
        }
        else
        {
            when(jsonHelper.unmarshallJsonToObject(Map.class, "{\"displayName\":\"1.0\"}")).thenReturn(fieldsBoostMap);
        }

        IndexSearchHighlightField indexSearchHighlightField =
            new IndexSearchHighlightField("displayName", 100, Arrays.asList("displayName", "displayName.stemmed", "displayName.ngrams"), 5);

        IndexSearchHighlightFields highlightFields = new IndexSearchHighlightFields(Collections.singletonList(indexSearchHighlightField));

        if (testExceptions)
        {
            when(jsonHelper.unmarshallJsonToObject(IndexSearchHighlightFields.class, highlightFieldsConfigValue)).thenThrow(new IOException());
        }
        else
        {
            when(jsonHelper.unmarshallJsonToObject(IndexSearchHighlightFields.class, highlightFieldsConfigValue)).thenReturn(highlightFields);
        }

        when(searchRequestBuilder.setSource(any())).thenReturn(searchRequestBuilderWithSource);
        when(searchRequestBuilderWithSource.setSize(SEARCH_RESULT_SIZE)).thenReturn(searchRequestBuilderWithSize);
        when(searchRequestBuilderWithSize.addSort(any())).thenReturn(searchRequestBuilderWithSorting);
        when(searchRequestBuilderWithSorting.highlighter(any(HighlightBuilder.class))).thenReturn(searchRequestBuilderWithHighlighting);

        when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.hits()).thenReturn(searchHitArray);
        Map<String, Object> sourceMap1 = new HashMap<>();
        Map<String, Object> tagTypeMap = new HashMap<>();
        tagTypeMap.put(CODE, TAG_TYPE_CODE);
        sourceMap1.put(TAG_TYPE, tagTypeMap);
        when(searchHit1.sourceAsMap()).thenReturn(sourceMap1);
        Map<String, Object> sourceMap2 = new HashMap<>();
        Map<String, Object> businessObjectDefinitionMap = new HashMap<>();
        businessObjectDefinitionMap.put(CODE, NAMESPACE_CODE);
        sourceMap2.put(NAMESPACE, businessObjectDefinitionMap);
        when(searchHit2.sourceAsMap()).thenReturn(sourceMap2);
        when(searchHit1.getShard()).thenReturn(searchShardTarget1);
        when(searchHit2.getShard()).thenReturn(searchShardTarget2);
        when(searchShardTarget1.getIndex()).thenReturn(TAG_INDEX);
        when(searchShardTarget2.getIndex()).thenReturn(BUSINESS_OBJECT_DEFINITION_INDEX);
        when(searchHits.getTotalHits()).thenReturn(200L);

        Map<String, HighlightField> highlightFieldMap = new HashMap<>();
        highlightFieldMap.put("displayName", highlightField);

        when(searchHit1.getHighlightFields()).thenReturn(highlightFieldMap);
        when(searchHit2.getHighlightFields()).thenReturn(highlightFieldMap);

        // Create index search request
        final IndexSearchRequest indexSearchRequest = new IndexSearchRequest(SEARCH_TERM, searchFilters, facetList, isHitHighlightingEnabled);

        List<TagTypeIndexSearchResponseDto> tagTypeIndexSearchResponseDtos = Collections
            .singletonList(new TagTypeIndexSearchResponseDto("code", 1, Collections.singletonList(new TagIndexSearchResponseDto("tag1", 1, null)), null));
        List<ResultTypeIndexSearchResponseDto> resultTypeIndexSearchResponseDto =
            Collections.singletonList(new ResultTypeIndexSearchResponseDto("type", 1, null));

        when(elasticsearchHelper.getNestedTagTagIndexSearchResponseDto(searchResponse)).thenReturn(tagTypeIndexSearchResponseDtos);
        when(elasticsearchHelper.getResultTypeIndexSearchResponseDto(searchResponse)).thenReturn(resultTypeIndexSearchResponseDto);
        when(elasticsearchHelper.getFacetsResponse(any(ElasticsearchResponseDto.class), any(Boolean.class))).thenCallRealMethod();
        when(elasticsearchHelper.addIndexSearchFilterBooleanClause(any(List.class))).thenCallRealMethod();
        when(elasticsearchHelper.addFacetFieldAggregations(any(Set.class), any(SearchRequestBuilder.class))).thenReturn(searchRequestBuilder);

        SearchResult searchResult = mock(SearchResult.class);
        when(jestClientHelper.searchExecute(any())).thenReturn(searchResult);

        List<SearchResult.Hit<Map, Void>> searchHitList = new ArrayList<>();
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map.put(DISPLAY_NAME_SOURCE, "Display Name");
        Map<String, Object> codeMap = new HashMap<>();
        codeMap.put(CODE, "Code");
        map.put(TAG_TYPE, codeMap);
        map.put(NAMESPACE, codeMap);
        JsonElement element = gson.toJsonTree(map);
        List<String> highlightList = new ArrayList<>();
        highlightList.add("Highlight 1");
        Map<String, List<String>> highlightMap = new HashMap<>();
        highlightMap.put("field", highlightList);

        SearchResult.Hit<Map, Void> hit1 =
            new SearchResult(gson).new Hit(HashMap.class, element, HashMap.class, null, highlightMap, null, TAG_INDEX, TAG_INDEX, "type", 1.0);
        SearchResult.Hit<Map, Void> hit2 =
            new SearchResult(gson).new Hit(HashMap.class, element, HashMap.class, null, highlightMap, null, BUSINESS_OBJECT_DEFINITION_INDEX,
                BUSINESS_OBJECT_DEFINITION_INDEX, "type", 2.0);
        searchHitList.add(hit1);
        searchHitList.add(hit2);

        when(searchResult.getHits(Map.class)).thenReturn(searchHitList);
        when(searchResult.getTotal()).thenReturn(200);

        MetricAggregation metricAggregation = mock(MetricAggregation.class);
        TermsAggregation termsAggregation = mock(TermsAggregation.class);
        when(searchResult.getAggregations()).thenReturn(metricAggregation);
        when(metricAggregation.getTermsAggregation(TAG_TYPE_FACET_AGGS)).thenReturn(termsAggregation);
        List<TermsAggregation.Entry> buckets = new ArrayList<>();
        TermsAggregation.Entry entry1 = mock(TermsAggregation.Entry.class);
        TermsAggregation.Entry entry2 = mock(TermsAggregation.Entry.class);
        buckets.add(entry1);
        buckets.add(entry2);
        when(termsAggregation.getBuckets()).thenReturn(buckets);

        // Call the method under test
        IndexSearchResponse indexSearchResponse = indexSearchDao.indexSearch(indexSearchRequest, fields);
        List<IndexSearchResult> indexSearchResults = indexSearchResponse.getIndexSearchResults();

        assertThat("Index search results list is null.", indexSearchResults, not(nullValue()));
        assertThat(indexSearchResponse.getTotalIndexSearchResults(), is(200L));

        // Verify the calls to external methods
        verify(configurationHelper, times(1)).getProperty(ConfigurationValue.TAG_SHORT_DESCRIPTION_LENGTH, Integer.class);
        verify(configurationHelper, times(1)).getProperty(ConfigurationValue.BUSINESS_OBJECT_DEFINITION_SHORT_DESCRIPTION_LENGTH, Integer.class);

        if (indexSearchRequest.isEnableHitHighlighting() != null)
        {
            verifyHitHighlightingInteractions(searchRequestBuilder, indexSearchRequest.isEnableHitHighlighting());
        }

        verify(jestClientHelper).searchExecute(any());
        verify(searchResult).getTotal();
        verify(searchResult).getHits(Map.class);
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    private void verifyHitHighlightingInteractions(SearchRequestBuilder searchRequestBuilder, boolean isHitHighlightingEnabled) throws IOException
    {
        if (isHitHighlightingEnabled)
        {
            // verify interactions with the helpers which is required to fetch highlighting config
            verify(jsonHelper, times(2)).unmarshallJsonToObject(eq(Map.class), any(String.class));
            verify(jsonHelper, times(1)).unmarshallJsonToObject(eq(IndexSearchHighlightFields.class), any(String.class));
            verify(configurationHelper, times(1)).getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_POSTTAGS);
            verify(configurationHelper, times(1)).getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_PRETAGS);
            verify(configurationHelper, times(1)).getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_FIELDS);
        }
        else
        {
            // verify highlighting is not added to the query if not highlighting is disabled
            verify(searchRequestBuilder, times(0)).highlighter(any(HighlightBuilder.class));

            // verify highlighting-specific configuration values are not fetched if highlighting is disabled
            verify(configurationHelper, times(0)).getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_POSTTAGS);
            verify(configurationHelper, times(0)).getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_PRETAGS);
            verify(configurationHelper, times(0)).getProperty(ConfigurationValue.ELASTICSEARCH_HIGHLIGHT_FIELDS);
        }
    }
}
