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

package codetroopers.wicket.web.routes.mounts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cgatay
 */
public class MountPathExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MountPathExtractor.class);
    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("([\\$#])\\{([^:]+)(:(.+))?\\}");
    
    final List<MountParameter> parameters;
    String path;

    MountPathExtractor() {
        parameters = new ArrayList<>();
    }

    public MountPathExtractor(String path) {
        this();
        extractParametersFromPath(path);
    }

    public List<MountParameter> getParameters() {
        return parameters;
    }

    public String getPath() {
        return path;
    }

    void extractParametersFromPath(String path){
        final String[] pathChunks = path.split("/");
        StringBuilder builder = new StringBuilder();
        for (String pathChunk : pathChunks) {
            final Matcher matcher = PATH_PARAM_PATTERN.matcher(pathChunk);
            if (matcher.matches()){
                extractMountParameterFromMatch(matcher, builder);
            }else{
                builder.append(pathChunk);
            }
            builder.append("/");
        }
        this.path = builder.substring(0, builder.length() - 1);
    }

    private void extractMountParameterFromMatch(final Matcher matcher, final StringBuilder builder) {
        MountParameter parameter = null;
        int i = matcher.groupCount();
        if (i == 2) {
            parameter = new MountParameter(MountParameter.Type.parse(matcher.group(1)),
                                                        matcher.group(2));
        } else if (i == 4) {
            parameter = new MountParameter(MountParameter.Type.parse(matcher.group(1)),
                                                        matcher.group(2), matcher.group(4));
        } 
        if (parameter == null) {
            LOGGER.error("Malformed pattern detected, try validating it : {}", matcher.group(0));
        }else{
            builder.append(parameter.getPathExpr());
            parameters.add(parameter);
        }

    }
}
