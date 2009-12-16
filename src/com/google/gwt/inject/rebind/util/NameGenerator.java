/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.inject.rebind.util;

import com.google.inject.Key;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper to generate various names for members of a {@code Ginjector}
 * implementation.
 */
@Singleton
public class NameGenerator {

  /**
   * "Mangled key name" cache:  Key -> mangled name
   */
  private final Map<Key<?>, String> cache = new HashMap<Key<?>, String>();

  /**
   * Map of known method names.
   */
  private final Set<String> methodNames = new HashSet<String>();

  /**
   * Returns the key's getter method name.  The method with that name can be
   * called to retrieve an instance of the type described by the key.
   *
   * @return getter method name
   */
  public String getGetterMethodName(Key<?> key) {
    return "get_" + mangle(key);
  }

  /**
   * Returns the key's creator method name.  The method with that name can be
   * called to create and retrieve an instance of the type described by the
   * key.
   *
   * @return creator method name
   */
  public String getCreatorMethodName(Key<?> key) {
    return "create_" + mangle(key);
  }

  /**
   * Returns the key's member inject method name.  The method with that name
   * can be called with a single parameter to inject members of that parameter.
   *
   * @return member inject method name
   */
  public String getMemberInjectMethodName(Key<?> key) {
    return "memberInject_" + mangle(key);
  }

  /**
   * Returns the key's singleton field name.
   *
   * @return singleton field name
   */
  public String getSingletonFieldName(Key<?> key) {
    return "singleton_" + mangle(key);
  }

  /**
   * Returns a new valid (i.e. unique) method name based on {@code base}.
   *
   * Note: Method names are considered "used" after being returned by this
   * method, whether they're actually used to write a new method or not.
   *
   * @param base base on which method name gets created
   * @return valid method name
   */
  public String createMethodName(String base) {
    while (methodNames.contains(base)) {

      // TODO(schmitt):  Make more efficient for repeated calls with same base?
      base += "_";
    }
    methodNames.add(base);
    return base;
  }

  /**
   * Reserves the given name to prevent new methods to be created with it.
   *
   * @param name name to be reserved
   */
  public void markAsUsed(String name) throws IllegalArgumentException {
    methodNames.add(name);
  }

  private String mangle(Key<?> key) {
    String cached = cache.get(key);
    if (cached != null) {
      return cached;
    }

    // TODO(bstoler): This algorithm is kinda crazy because the annotation
    // values are of unbounded length. One option
    // is to use mangled(type) + mangled(annotation type) + counter, where
    // counter is used just to distinguish different annotation values.
    String name = key.toString();
    name = convertToValidMemberName(name);

    // TODO(schmitt):  Not necessary for field names?
    name = createMethodName(name);

    cache.put(key, name);
    return name;
  }

  public String convertToValidMemberName(String name) {
    name = name.replaceAll("\\s+", "_");
    name = name.replaceAll("[^\\p{Alnum}_]", "\\$");
    return name;
  }

  // Static for access from enum.
  public static String replaceLast(String source, char toReplace, char with) {
    StringBuilder sb = new StringBuilder(source);
    int index = sb.lastIndexOf(String.valueOf(toReplace));
    if (index != -1) {
      sb.setCharAt(index, with);
    }
    return sb.toString();
  }
}
