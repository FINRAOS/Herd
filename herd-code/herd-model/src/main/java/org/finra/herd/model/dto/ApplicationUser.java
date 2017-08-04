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
package org.finra.herd.model.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.finra.herd.model.api.xml.NamespaceAuthorization;

/**
 * A class to hold the credentials of an application user.
 */
public class ApplicationUser implements Serializable
{
    private static final long serialVersionUID = 9080644815191877789L;

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;
    private String sessionId;
    private Date sessionInitTime;
    private Set<NamespaceAuthorization> namespaceAuthorizations;

    /**
     * The class that built this application user.
     */
    private Class<?> generatedByClass;

    public ApplicationUser(Class<?> generatedByClass)
    {
        this.generatedByClass = generatedByClass;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * Gets the collection of roles.
     *
     * @return the set of roles
     */
    public Set<String> getRoles()
    {
        if (roles == null)
        {
            roles = new HashSet<>();
        }

        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public Date getSessionInitTime()
    {
        return sessionInitTime;
    }

    public void setSessionInitTime(Date sessionInitTime)
    {
        this.sessionInitTime = sessionInitTime;
    }

    public Set<NamespaceAuthorization> getNamespaceAuthorizations()
    {
        return namespaceAuthorizations;
    }

    public void setNamespaceAuthorizations(Set<NamespaceAuthorization> namespaceAuthorizations)
    {
        this.namespaceAuthorizations = namespaceAuthorizations;
    }

    public Class<?> getGeneratedByClass()
    {
        return generatedByClass;
    }

    public String toString()
    {
        return "userId=" + userId + " username=\"" + firstName + " " + lastName + "\" sessionId=" + sessionId + " roles=" + roles + " generatedByClass=" +
            generatedByClass.getCanonicalName();
    }
}
