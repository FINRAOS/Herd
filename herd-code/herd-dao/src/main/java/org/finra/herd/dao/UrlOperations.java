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

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;

/**
 * URL operations service.
 */
public interface UrlOperations
{
    /**
     * Opens a connection to the specified URL and returns an input stream for reading from that connection.
     *
     * @param url the URL object
     * @param proxy the the Proxy through which this connection will be made
     *
     * @return an input stream for reading from the URL connection
     * @throws IOException if an I/O exception occurs
     */
    public InputStream openStream(URL url, Proxy proxy) throws IOException;
}
