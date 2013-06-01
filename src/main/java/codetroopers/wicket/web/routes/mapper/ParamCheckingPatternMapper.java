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

package codetroopers.wicket.web.routes.mapper;

import codetroopers.wicket.web.routes.MountParameter;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.ClassProvider;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Base extracted from fiftyfive.wicket.mapper.PatternMountedMapper
 * 
 * An improved version of Wicket's standard {@link MountedMapper} that additionally allows
 * regular expressions inside placeholders. This feature is inspired by the pattern matching
 * behavior of the JAX-RS {@code @Path} annotation.
 * <pre class="example">
 * mount(new PatternMountedMapper("people/${personId:\\d+}", PersonPage.class));</pre>
 * This will map URLs like {@code people/12345} but yield a 404 not found for something like
 * {@code people/abc} since {@code abc} doesn't match the {@code \d+} regular expression.
 *
 * @author 55minutes.com
 * @author cgatay
 */
public class ParamCheckingPatternMapper extends MountedMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamCheckingPatternMapper.class);

    protected List<PatternPlaceholder> patternPlaceholders;

    private int numSegments;
    private boolean exact = false;
    private boolean versionInUrl = true;
    private boolean ignoreIncorrectParameters = true;

    public ParamCheckingPatternMapper(final String mountPath,
                                      final Class<? extends IRequestablePage> pageClass,
                                      final List<MountParameter> mountParameters) {
        this(mountPath, pageClass, new PageParametersEncoder(), mountParameters);
    }

    public ParamCheckingPatternMapper(final String mountPath,
                                      final ClassProvider<? extends IRequestablePage> pageClassProvider) {
        this(mountPath, pageClassProvider, new PageParametersEncoder(), null);
    }

    public ParamCheckingPatternMapper(final String mountPath,
                                      final Class<? extends IRequestablePage> pageClass,
                                      final IPageParametersEncoder pageParametersEncoder,
                                      final List<MountParameter> mountParameters) {
        this(mountPath, ClassProvider.of(pageClass), pageParametersEncoder, mountParameters);
    }

    public ParamCheckingPatternMapper(final String mountPath,
                                      final ClassProvider<? extends IRequestablePage> pageClassProvider,
                                      final IPageParametersEncoder pageParametersEncoder,
                                      final List<MountParameter> mountParameters) {
        super(mountPath, pageClassProvider, pageParametersEncoder);
        this.numSegments = 0;
        this.patternPlaceholders = buildPatternPlaceholdersList(mountParameters);
    }

    /**
     * Allows to build the list of parameters expected for this mapper
     * @param mountParameters parameters
     * @return patterns
     */
    protected List<PatternPlaceholder> buildPatternPlaceholdersList(final List<MountParameter> mountParameters) {
        List<PatternPlaceholder> list = new ArrayList<PatternPlaceholder>();
        if (mountParameters != null) {
            for (MountParameter parameter : mountParameters) {
                final PatternPlaceholder patternPlaceholder = new PatternPlaceholder(parameter);
                if (!patternPlaceholder.optional) {
                    this.numSegments++;
                }
                list.add(patternPlaceholder);
            }
        }
        return list;
    }


    /**
     * Set to {@code true}, to force this mapper to strictly match URLs by disallowing any extra
     * path elements that come after the matched pattern.
     * <pre class="example">
     * PatternMountedMapper m = new PatternMountedMapper(MyPage.class, "page/${id:\\d+}");
     * // These will always be matched: "page/1", "page/2", "page/30", etc.
     * // By default, these will be matched as well: "page/1/whatever/foo/bar", "page/2/baz"
     * m.setExact(true);
     * // Now these will not be matched: "page/1/whatever/foo/bar", "page/2/baz"</pre>
     *
     * In other words, if {@code exact} is set to {@code false}, extra path elements after the
     * specified pattern will be allowed. The default is {@code false}, to match the default
     * behavior of Wicket's {@link org.apache.wicket.core.request.mapper.MountedMapper}.
     *
     * @return {@code this} to allow chaining
     */
    public ParamCheckingPatternMapper setExact(boolean exact) {
        this.exact = exact;
        return this;
    }

    /**
     * Allows to ignore incorrect parameters.
     * If set to ${@code true} incorrect parameters will be stripped from the request.
     * If set to ${@code false} any incorrect parameter will make the Mapper respond as it doesn't match
     * @param ignore ignore invalid request parameter
     * @return ${@code this} to allow chaining
     */
    public ParamCheckingPatternMapper ignoreIncorrectParameters(final boolean ignore) {
        this.ignoreIncorrectParameters = ignore;
        return this;
    }

    /**
     * Allows to bypass the version string in url if set to ${code false}. Default behavior is ${code true}
     * The main benefit of doing this is the possibility of having "clean" URL even on stateful pages
     * The drawback is that the built-in browser back button support will likely not work as expected
     * @param versionInUrl whether to display the version in url
     * @return ${code this} to allow chaining
     */
    public ParamCheckingPatternMapper displayVersionInUrl(final boolean versionInUrl) {
        this.versionInUrl = versionInUrl;
        return this;
    }

    /**
     * First delegate to the superclass to parse the request as normal, then additionally
     * verify that all regular expressions specified in the placeholders match.
     */
    @Override
    protected UrlInfo parseRequest(Request request) {
        // Parse the request normally. If the standard impl can't parse it, we won't either.
        UrlInfo info = super.parseRequest(request);
        if (null == info || null == info.getPageParameters()) {
            return info;
        }

        // If exact matching, reject URLs that have more than expected number of segments
        if (exact) {
            int requestNumSegments = request.getUrl().getSegments().size();
            if (requestNumSegments > this.numSegments) {
                return null;
            }
        }

        // Loop through each placeholder and verify that the regex of the placeholder matches
        // the value that was provided in the request url. If any of the values don't match,
        // immediately return null signifying that the url is not matched by this mapper.
        PageParameters params = info.getPageParameters();
        for (PatternPlaceholder pp : getPatternPlaceholders()) {
            List<StringValue> values = params.getValues(pp.getName());
            if (null == values || values.size() == 0) {
                values = Arrays.asList(StringValue.valueOf(""));
            }
            for (StringValue val : values) {
                if (!pp.matches(val.toString())) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format(
                                "Parameter \"%s\" did not match pattern placeholder %s", val, pp));
                    }
                    if (ignoreIncorrectParameters) {
                        params.remove(pp.getName(), val.toString());
                    } else {
                        return null;
                    }
                }
            }
        }
        return info;
    }

    @Override
    protected void encodePageComponentInfo(final Url url, final PageComponentInfo info) {
        //we are generating an url to a behavior, we keep all information, otherwise we consider we don't want version in url
        if (versionInUrl || info.getComponentInfo() != null) {
            super.encodePageComponentInfo(url, info);
        } else {
            super.encodePageComponentInfo(url, null);
        }
    }

    /**
     * The list of placeholders (in other words, the <code>${name:regex}</code> components of the
     * mount path).
     */
    protected List<PatternPlaceholder> getPatternPlaceholders() {
        return this.patternPlaceholders;
    }

    /**
     * Represents a placeholder that optionally contains a regular expression.
     */
    protected static class PatternPlaceholder {
        private boolean optional;
        private final Pattern pattern;
        private final String name;

        public PatternPlaceholder(final MountParameter parameter) {
            this.name = parameter.value();
            final String regex = parameter.regex();
            this.pattern = regex == null ? null : Pattern.compile(regex);
            this.optional = MountParameter.Type.OPTIONAL.equals(parameter.type());
        }

        /**
         * Return {@code true} if this placeholder has a regex pattern and that pattern matches
         * the specified value. 
         * If this placeholder doesn't have a regex, always return {@code true}
         */
        public boolean matches(CharSequence value) {
            if (optional && (value == null || "".equals(value))) {
                return true;
            }
            return this.pattern == null || this.pattern.matcher(value).matches();
        }

        /**
         * The name of this placeholder with the {@code :regex} portion removed.
         */
        public String getName() {
            return this.name;
        }

        /**
         * For debugging.
         */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("PatternPlaceholder");
            sb.append("{optional=").append(optional);
            sb.append(", pattern='").append(pattern).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

}
