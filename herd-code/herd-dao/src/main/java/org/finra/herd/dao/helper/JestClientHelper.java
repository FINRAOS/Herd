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
package org.finra.herd.dao.helper;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.searchbox.action.Action;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchScroll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import org.finra.herd.dao.JestClientFactory;

/**
 * JestClientHelper
 */
@Component
public class JestClientHelper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JestClientHelper.class);

    @Autowired
    private JestClientFactory jestClientFactory;

    /**
     * Method to use the JEST client to search against Elasticsearch.
     *
     * @param search JEST Search object
     *
     * @return a search result
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification="cast is safe in this situation")
    @Retryable(maxAttempts = 3, value = RestClientException.class, backoff = @Backoff(delay = 5000, multiplier = 2))
    public SearchResult searchExecute(final Search search)
    {
        SearchResult searchResult = null;
        try
        {
            searchResult = jestClientFactory.getJestClient().execute(search);
        }
        catch (final IOException ioException)
        {
            LOGGER.error("Failed to execute JEST client search.", ioException);
            //throw a runtime exception so that the client needs not catch
            throw new RestClientException(ioException.getMessage()); //NOPMD
        }

        return searchResult;
    }

    /**
     * Method to use the JEST client to search against Elasticsearch.
     *
     * @param searchScroll search scroll request
     *
     * @return a jest search result
     */
    @Retryable(maxAttempts = 3, value = RestClientException.class, backoff = @Backoff(delay = 5000, multiplier = 2))
    public JestResult searchScrollExecute(final SearchScroll searchScroll)
    {
        JestResult searchResult = null;
        try
        {
            searchResult =
                jestClientFactory.getJestClient().execute(searchScroll);
        }
        catch (final IOException ioException)
        {
            LOGGER.error("Failed to execute JEST client search.", ioException);
            //throw a runtime exception so that the client needs not catch
            throw new RestClientException(ioException.getMessage()); //NOPMD
        }

        return searchResult;
    }

    /**
     * execute action
     * @param action action
     * @return JestResult
     */
    @Retryable(maxAttempts = 3, value = RestClientException.class, backoff = @Backoff(delay = 5000, multiplier = 2))
    public JestResult executeAction(Action action)
    {
        JestResult searchResult = null;
        try
        {
            searchResult =
                jestClientFactory.getJestClient().execute(action);
        }
        catch (final IOException ioException)
        {
            LOGGER.error("Failed to execute JEST client action.", ioException);
            //throw a runtime exception so that the client needs not catch
            throw new RestClientException(ioException.getMessage()); //NOPMD
        }

        return searchResult;
    }
}
