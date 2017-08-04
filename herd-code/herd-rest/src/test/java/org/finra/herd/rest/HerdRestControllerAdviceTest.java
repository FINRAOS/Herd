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
package org.finra.herd.rest;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * Test the herd REST Controller Advice. There is very little functionality to test since the majority of functionality has been moved into the base class and
 * is tested in a different JUnit for that class. The only thing specific to the REST controller advice is that logging should be enabled.
 */
public class HerdRestControllerAdviceTest extends AbstractRestTest
{
    @Mock
    private DateTimeEditor dateTimeEditor;

    @Mock
    private DelimitedFieldValuesEditor delimitedFieldValuesEditor;

    @InjectMocks
    private HerdRestControllerAdvice herdRestControllerAdvice;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitBinder()
    {
        // Mock HTTP servlet request.
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock servlet request data binder.
        ServletRequestDataBinder binder = mock(ServletRequestDataBinder.class);

        // Call the method under test.
        herdRestControllerAdvice.initBinder(request, binder);

        // Verify the external calls.
        verify(binder).registerCustomEditor(DelimitedFieldValues.class, delimitedFieldValuesEditor);
        verify(binder).registerCustomEditor(DateTime.class, dateTimeEditor);
        verifyNoMoreInteractions(binder);
        verifyNoMoreInteractionsHelper();
    }

    @Test
    public void testIsLoggingEnabled() throws Exception
    {
        herdRestControllerAdvice.afterPropertiesSet();
        assertTrue(herdRestControllerAdvice.isLoggingEnabled());
    }

    /**
     * Checks if any of the mocks has any interaction.
     */
    private void verifyNoMoreInteractionsHelper()
    {
        verifyNoMoreInteractions(dateTimeEditor, delimitedFieldValuesEditor);
    }
}
