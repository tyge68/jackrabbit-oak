/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.security.authorization.composite;

import java.security.Principal;
import java.util.Collections;

import javax.jcr.RepositoryException;
import javax.jcr.security.AccessControlManager;

import org.apache.jackrabbit.oak.AbstractSecurityTest;
import org.apache.jackrabbit.oak.namepath.NamePathMapper;
import org.apache.jackrabbit.oak.security.authorization.AuthorizationConfigurationImpl;
import org.apache.jackrabbit.oak.spi.security.authorization.AuthorizationConfiguration;
import org.apache.jackrabbit.oak.spi.security.authorization.OpenAuthorizationConfiguration;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.EmptyPermissionProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.PermissionProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.restriction.CompositeRestrictionProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.restriction.RestrictionProvider;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CompositeAuthorizationConfigurationTest extends AbstractSecurityTest {

    private CompositeAuthorizationConfiguration getCompositeConfiguration(AuthorizationConfiguration... entries) {
        CompositeAuthorizationConfiguration compositeConfiguration = new CompositeAuthorizationConfiguration(getSecurityProvider());
        for (AuthorizationConfiguration ac : entries) {
            compositeConfiguration.addConfiguration(ac);
        }
        return compositeConfiguration;
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyGetAccessControlManager() {
        getCompositeConfiguration().getAccessControlManager(root, NamePathMapper.DEFAULT);
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyGetPermissionProvider() {
        getCompositeConfiguration().getPermissionProvider(root, adminSession.getWorkspaceName(), Collections.<Principal>emptySet());
    }

    @Test
    public void testEmptyGetRestrictionProvider() {
        assertSame(RestrictionProvider.EMPTY, getCompositeConfiguration().getRestrictionProvider());
    }

    @Test
    public void testSingleGetAccessControlManager() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(new AuthorizationConfigurationImpl(getSecurityProvider()));

        AccessControlManager accessControlManager = cc.getAccessControlManager(root, NamePathMapper.DEFAULT);
        assertFalse(accessControlManager instanceof CompositeAccessControlManager);
    }

    @Test
    public void testSingleGetPermissionProvider() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(new AuthorizationConfigurationImpl(getSecurityProvider()));

        PermissionProvider pp = cc.getPermissionProvider(root, root.getContentSession().getWorkspaceName(), Collections.<Principal>emptySet());
        assertFalse(pp instanceof CompositePermissionProvider);
    }

    @Test
    public void testSlingRestrictionProvider() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(new AuthorizationConfigurationImpl(getSecurityProvider()));

        RestrictionProvider rp = cc.getRestrictionProvider();
        assertFalse(rp instanceof CompositeRestrictionProvider);
    }

    @Test
    public void testMultipleGetAccessControlManager() throws RepositoryException {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(
                new AuthorizationConfigurationImpl(getSecurityProvider()),
                new AuthorizationConfigurationImpl(getSecurityProvider()));

        AccessControlManager accessControlManager = cc.getAccessControlManager(root, NamePathMapper.DEFAULT);
        assertTrue(accessControlManager instanceof CompositeAccessControlManager);
    }

    @Test
    public void testMultipleGetPermissionProvider() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(
                new OpenAuthorizationConfiguration(),
                new AuthorizationConfigurationImpl(getSecurityProvider()));

        PermissionProvider pp = cc.getPermissionProvider(root, root.getContentSession().getWorkspaceName(), Collections.<Principal>emptySet());
        assertFalse(pp instanceof CompositePermissionProvider);
    }

    @Test
    public void testMultipleGetPermissionProvider2() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(
                new AuthorizationConfigurationImpl(getSecurityProvider()),
                new AuthorizationConfigurationImpl(getSecurityProvider()));

        PermissionProvider pp = cc.getPermissionProvider(root, root.getContentSession().getWorkspaceName(), Collections.<Principal>emptySet());
        assertTrue(pp instanceof CompositePermissionProvider);
    }

    @Test
    public void testMultipleGetPermissionProvider3() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(
                new OpenAuthorizationConfiguration(),
                new OpenAuthorizationConfiguration());

        PermissionProvider pp = cc.getPermissionProvider(root, root.getContentSession().getWorkspaceName(), Collections.<Principal>emptySet());
        assertFalse(pp instanceof CompositePermissionProvider);
        assertSame(EmptyPermissionProvider.getInstance(), pp);
    }

    @Test
    public void testMultipleRestrictionProvider() {
        CompositeAuthorizationConfiguration cc = getCompositeConfiguration(
                new AuthorizationConfigurationImpl(getSecurityProvider()),
                new AuthorizationConfigurationImpl(getSecurityProvider()));

        RestrictionProvider rp = cc.getRestrictionProvider();
        assertTrue(rp instanceof CompositeRestrictionProvider);
    }
}