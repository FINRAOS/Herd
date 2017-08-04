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


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import io.searchbox.client.JestResult;
import io.searchbox.core.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.listeners.CollectCreatedMocks;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import org.finra.herd.dao.helper.HerdStringHelper;
import org.finra.herd.dao.helper.JestClientHelper;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.dao.impl.IndexFunctionsDaoImpl;

public class IndexFunctionsDaoTest
{

    private List<Object> createdMocks;

    @InjectMocks
    private IndexFunctionsDaoImpl indexFunctionsDao;

    @Mock
    private HerdStringHelper herdStringHelper;

    @Mock
    private JsonHelper jsonHelper;

    @Mock
    private JestClientHelper jestClientHelper;


    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        createdMocks = new LinkedList<>();
        final MockingProgress progress = new ThreadSafeMockingProgress();
        progress.setListener(new CollectCreatedMocks(createdMocks));
    }

    @Test
    public void testIndexFunction()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        // Call the method under test
        indexFunctionsDao.createIndexDocument("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        // Verify the calls to external methods
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testValidateFunctionIndex()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn("");
        indexFunctionsDao.validateDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        // Verify the calls to external methods
        verify(jestClientHelper, times(2)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testValidateFunctionUpdate()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn("JSON_UPDATE");
        indexFunctionsDao.validateDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        // Verify the calls to external methods
        verify(jestClientHelper, times(2)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testValidateFunctionNoActionRequiredValidDocument()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn("JSON");
        indexFunctionsDao.validateDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        // Verify the calls to external methods
        verify(jestClientHelper, times(1)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testIsValidFunction()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn("JSON");

        // Call the method under test
        boolean isValid = indexFunctionsDao.isValidDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        assertThat("IsValid is false when it should have been true.", isValid, is(true));

        verify(jestClientHelper, times(1)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testIsValidFunctionEmpty()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn("");

        // Call the method under test
        boolean isValid = indexFunctionsDao.isValidDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        assertThat("IsValid is true when it should have been false.", isValid, is(false));

        verify(jestClientHelper, times(1)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verifyNoMoreInteractions(createdMocks.toArray());

    }

    @Test
    public void testIsValidFunctionNull()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn(null);

        // Call the method under test
        boolean isValid = indexFunctionsDao.isValidDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        assertThat("IsValid is true when it should have been false.", isValid, is(false));

        verify(jestClientHelper, times(1)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testIsValidFunctionNotEqual()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsString()).thenReturn("JSON_NOT_EQUAL");

        // Call the method under test
        boolean isValid = indexFunctionsDao.isValidDocumentIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");
        assertThat("IsValid is true when it should have been false.", isValid, is(false));

        verify(jestClientHelper, times(1)).executeAction(any());
        verify(jestResult).getSourceAsString();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testIndexExistsFunction()
    {
        JestResult jestResult = mock(JestResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        indexFunctionsDao.isIndexExists("Index");
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testDeleteIndexFunction()
    {
        JestResult jestResult = mock(JestResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        indexFunctionsDao.deleteIndex("Index");
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testCreateIndexFunction()
    {
        JestResult jestResult = mock(JestResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        //indexFunctionsDao.createIndex("Index", "Document_Type", "Mapping", "Settings");


       // verifyNoMoreInteractions(createdMocks.toArray());
    }



    @Test
    public void testCreateIndexDocumentsFunction()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(true);
        Map<String, String> documentMap = new HashMap<>();
        documentMap.put("1", "JSON");
        indexFunctionsDao.createIndexDocuments("INDEX_NAME", "DOCUMENT_TYPE", documentMap);

        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testCreateIndexDocumentsFunctionWithFailures()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(false);
        Map<String, String> documentMap = new HashMap<>();
        documentMap.put("1", "JSON");
        indexFunctionsDao.createIndexDocuments("INDEX_NAME", "DOCUMENT_TYPE", documentMap);

        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verify(jestResult).getErrorMessage();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testDeleteDocumentByIdFunction()
    {
        JestResult jestResult = mock(JestResult.class);
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(true);

        indexFunctionsDao.deleteDocumentById("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testDeleteIndexDocumentsFunction()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(true);
        // Call the method under test
        List<Integer> businessObjectDefinitionIds = new ArrayList<>();
        businessObjectDefinitionIds.add(1);
        indexFunctionsDao.deleteIndexDocuments("INDEX_NAME", "DOCUMENT_TYPE", businessObjectDefinitionIds);

        // Verify the calls to external methods
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testDeleteIndexDocumentsFunctionWithFailures()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(false);
        // Call the method under test
        List<Integer> businessObjectDefinitionIds = new ArrayList<>();
        businessObjectDefinitionIds.add(1);
        indexFunctionsDao.deleteIndexDocuments("INDEX_NAME", "DOCUMENT_TYPE", businessObjectDefinitionIds);

        // Verify the calls to external methods
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verify(jestResult).getErrorMessage();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testNumberOfTypesInIndexFunction()
    {
        JestResult searchResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(searchResult);
        when(searchResult.getSourceAsString()).thenReturn("100");
        indexFunctionsDao.getNumberOfTypesInIndex("INDEX_NAME", "DOCUMENT_TYPE");
        // Verify the calls to external methods
        verify(jestClientHelper).executeAction(any());
        verify(searchResult).getSourceAsString();

        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testIdsInIndexFunction()
    {
        JestResult jestResult = mock(JestResult.class);
        SearchResult searchResult = mock(SearchResult.class);
        List<String> idList = Arrays.asList("{id:1}");
        List<String> emptyList = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_scroll_id", "100");
        when(jestClientHelper.searchExecute(any())).thenReturn(searchResult);
        when(searchResult.getSourceAsStringList()).thenReturn(idList);
        when(searchResult.getJsonObject()).thenReturn(jsonObject);
        when(jestClientHelper.searchScrollExecute(any())).thenReturn(jestResult);
        when(jestResult.getSourceAsStringList()).thenReturn(emptyList);
        indexFunctionsDao.getIdsInIndex("INDEX_NAME", "DOCUMENT_TYPE");
        verify(jestClientHelper).searchExecute(any());
        verify(searchResult, times(2)).getSourceAsStringList();
        verify(searchResult).getJsonObject();
        verify(jestClientHelper).searchScrollExecute(any());
        verify(jestResult).getSourceAsStringList();
    }


    @Test
    public void testUpdateIndexDocumentsFunction()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(true);

        // Call the method under test
        Map<String, String> documentMap = new HashMap<>();
        documentMap.put("1", "JSON");
        indexFunctionsDao.updateIndexDocuments("INDEX_NAME", "DOCUMENT_TYPE", documentMap);

        // Verify the calls to external methods
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verifyNoMoreInteractions(createdMocks.toArray());
    }

    @Test
    public void testUpdateIndexDocumentsFunctionWithFailures()
    {
        SearchResult jestResult = mock(SearchResult.class);
        // Build mocks
        when(jestClientHelper.executeAction(any())).thenReturn(jestResult);
        when(jestResult.isSucceeded()).thenReturn(false);

        // Call the method under test
        Map<String, String> documentMap = new HashMap<>();
        documentMap.put("1", "JSON");
        indexFunctionsDao.updateIndexDocuments("INDEX_NAME", "DOCUMENT_TYPE", documentMap);

        // Verify the calls to external methods
        verify(jestClientHelper).executeAction(any());
        verify(jestResult).isSucceeded();
        verify(jestResult).getErrorMessage();
        verifyNoMoreInteractions(createdMocks.toArray());
    }
}
