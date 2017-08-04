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
package org.finra.herd.core;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

/**
 * HerdStringUtils
 */
public class HerdStringUtils
{
    /**
     * Truncates the description field to a configurable value thereby producing a 'short description'
     *
     * @param description the specified description
     * @param shortDescMaxLength the short description maximum length
     *
     * @return truncated (short) description
     */
    public static String getShortDescription(String description, Integer shortDescMaxLength)
    {
        // Parse out only html tags, truncate and return
        // Do a partial HTML parse just in case there are some elements that don't have ending tags or the like
        String toParse = description != null ? description : "";
        return StringUtils.left(Jsoup.parseBodyFragment(toParse).body().text(), shortDescMaxLength);
    }

    /**
     * Strips HTML tags from a given input String, allows some tags to be retained via a whitelist
     *
     * @param fragment the specified String
     * @param whitelistTags the specified whitelist tags
     *
     * @return cleaned String with allowed tags
     */
    public static String stripHtml(String fragment, String... whitelistTags)
    {

        // Parse out html tags except those from a given list of whitelist tags
        Document dirty = Jsoup.parseBodyFragment(fragment);

        Whitelist whitelist = new Whitelist();

        for (String whitelistTag : whitelistTags)
        {
            // Get the actual tag name from the whitelist tag
            // this is vulnerable in general to complex tags but will suffice for our simple needs
            whitelistTag = StringUtils.removePattern(whitelistTag, "[^\\{IsAlphabetic}]");

            // Add all specified tags to the whitelist while preserving inline css
            whitelist.addTags(whitelistTag).addAttributes(whitelistTag, "class");
        }

        Cleaner cleaner = new Cleaner(whitelist);
        Document clean = cleaner.clean(dirty);
        clean.outputSettings()
            .escapeMode(Entities.EscapeMode.base)
             // Set character encoding to UTF-8
            .charset(CharEncoding.UTF_8)
             // Make sure no line-breaks are added
            .prettyPrint(false);

        // return 'cleaned' html body
        return clean.body().html();
    }
}
