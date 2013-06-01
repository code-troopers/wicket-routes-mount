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

/**
 * @author cgatay
 */
public class MountParameter {
    private Type type;
    private String value;
    private String regex;

    public MountParameter(final Type type, final String value) {
        this.type = type;
        this.value = value;
    }

    public MountParameter(final Type type, final String value, final String regex) {
        this.type = type;
        this.value = value;
        this.regex = regex;
    }

    public enum Type {
        OPTIONAL('#'),
        REQUIRED('$');
        private final char marker;

        Type(final char marker) {
            this.marker = marker;
        }

        public char marker() {
            return marker;
        }

        public static Type parse(String s) {
            if (s != null && s.length() > 0) {
                final char firstChar = s.charAt(0);
                if (OPTIONAL.marker == firstChar) {
                    return OPTIONAL;
                }
                if (REQUIRED.marker == firstChar) {
                    return REQUIRED;
                }
            }
            return null;
        }
    }

    public CharSequence getPathExpr(){
        return new StringBuilder().append(type().marker()).append("{").append(value).append("}");
    }

    public String value() {
        return value;
    }

    public Type type() {
        return type;
    }

    public String regex(){
        return regex;
    }

}
