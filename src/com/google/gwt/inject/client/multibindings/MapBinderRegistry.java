/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.inject.client.multibindings;

import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A registry to keep track of bindings for a key&value binding pair.
 *
 * @param <K> type of key for map binding
 * @param <V> type of value for map binding
 */
@Singleton
public class MapBinderRegistry<K, V> {

  private final Map<K, Set<Provider<V>>> providers = new LinkedHashMap<K, Set<Provider<V>>>();

  @SuppressWarnings("unchecked")
  void register(K key, Provider<? extends V> provider) {
    Set<Provider<V>> set = providers.get(key);
    if (set == null) {
      set = new LinkedHashSet<Provider<V>>();
      providers.put(key, set);
    }
    set.add((Provider<V>) provider);
  }

  Set<Entry<K, Set<Provider<V>>>> getEntries() {
    return providers.entrySet();
  }

  static <V> Provider<V> getLast(Set<Provider<V>> value) {
    Iterator<Provider<V>> iterator = value.iterator();
    while (true) {
      Provider<V> current = iterator.next();
      if (!iterator.hasNext()) {
        return current;
      }
    }
  }
}
