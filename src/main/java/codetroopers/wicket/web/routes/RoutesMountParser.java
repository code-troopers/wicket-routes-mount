/*
 * Copyright 2013 Code-troopers.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package codetroopers.wicket.web.routes;

import codetroopers.wicket.web.routes.mapper.ParamCheckingPatternMapper;
import codetroopers.wicket.web.routes.mounts.MountPathExtractor;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cgatay
 */
public class RoutesMountParser {
    static Pattern routePattern = Pattern.compile("^([^\\t\\s]*)[\\t\\s]*([^\\t\\s]*)[\\t\\s]*([^\\t\\s]*)[\\t\\s]*$");
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesMountParser.class);

    public static void mount(WebApplication application) {
        try {
            final List<URLPageMapping> mappings = new RoutesMountParser().parse();
            for (URLPageMapping mapping : mappings) {
                mapping.mount(application);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to mount pages : {}", e.getMessage());
        }

    }

    RoutesMountParser() {
    }

    List<URLPageMapping> parseFile(final String fileName) throws IOException {
        final InputStream resourceAsStream = getClass().getResourceAsStream(fileName);
        if (resourceAsStream == null) {
            throw new IllegalArgumentException("Cannot open file " + fileName);
        }
        List<URLPageMapping> mapping = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && line.trim().length() > 0) {
                    final Matcher matcher = routePattern.matcher(line);
                    if (matcher.matches()) {
                        try {
                            final URLPageMapping pageMapping = new URLPageMapping(matcher.group(1), matcher.group(2));
                            if (matcher.groupCount() == 3) {
                                pageMapping.addRoles(matcher.group(3));
                            }
                            mapping.add(pageMapping);
                        } catch (ClassNotFoundException e) {
                            LOGGER.error("Unable to parse line (ClassNotFound) : {}", line);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Unable to mount page : '{}'", e.getMessage());
                        }
                    }
                }
            }
        }
        return mapping;
    }

    private List<URLPageMapping> parse() throws IOException {
        return parseFile("/routes.conf");
    }


    @SuppressWarnings("unchecked")
    static class URLPageMapping {
        private final List<String> roles;
        private final String mountPoint;
        private final Class<? extends Page> clazz;

        public URLPageMapping(final String mountPoint, final String clazzName) throws ClassNotFoundException {
            this.mountPoint = mountPoint;
            final Class<?> aClass = Class.forName(clazzName);
            if (!Page.class.isAssignableFrom(aClass)) {
                throw new IllegalArgumentException(clazzName + " is not a wicket Page !");
            } else {
                this.clazz = (Class<? extends Page>) aClass;
            }
            this.roles = new ArrayList<>();
        }

        public void mount(WebApplication application) {
            final MountPathExtractor extractor = new MountPathExtractor(mountPoint);
            application.mount(new ParamCheckingPatternMapper(extractor.getPath(), 
                                                             clazz, 
                                                             extractor.getParameters()));
            for (String role : roles) {
                MetaDataRoleAuthorizationStrategy.authorize(clazz, role);
            }
        }

        public void addRoles(final String commaSeparatedRoles) {
            if (!Strings.isEmpty(commaSeparatedRoles)) {
                final String[] roles = Strings.split(commaSeparatedRoles, ',');
                for (String role : roles) {
                    this.roles.add(role);
                }
            }

        }

        List<String> getRoles() {
            return roles;
        }
    }
}
