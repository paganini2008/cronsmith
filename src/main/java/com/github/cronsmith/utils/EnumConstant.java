/*
 * Copyright 2026 Fred Feng
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
package com.github.cronsmith.utils;

/**
 * 
 * A persistable enum: one whose stored representation is decided by the enum itself rather than by
 * its ordinal, so that reordering the constants never silently changes what is already in the
 * database.
 * 
 * @Description: EnumConstant
 * @Author: Fred Feng
 * @Date: 13/04/2025
 * @Version 1.0.0
 */
public interface EnumConstant {

    String DEFAULT_GROUP = "DEFAULT";

    /**
     * The value written to and read back from external storage.
     */
    Object getValue();

    /**
     * A human readable form of this constant.
     */
    String getRepr();

    /**
     * Constants of one enum type may belong to several groups, so that a subset can be looked up
     * without splitting the type.
     */
    default String getGroup() {
        return DEFAULT_GROUP;
    }
}
