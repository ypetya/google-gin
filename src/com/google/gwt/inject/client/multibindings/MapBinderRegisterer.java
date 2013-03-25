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

import com.google.gwt.inject.client.multibindings.MapBinderRegistererModule.MapKey;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A helper that gets injected by a key provider/ value provider pair and
 * registers it to a target registry.
 * <p>In GWT compilation time, MapBinder adds a binding for this class with
 * specific generic types and marks is at eager-singleton, so that GIN generates
 * code that creates the instance of this class when the injector is created. In
 * the runtime, as this class is instantiated, it registers the key and the
 * value provider to the actual provider of the target Map.
 *
 * @param <K> type of key for map
 * @param <V> type of value for map
 * @param <T> type of implementation for value
 */
public class MapBinderRegisterer<K, V, T extends V> {

  @Inject
  public MapBinderRegisterer(MapBinderRegistry<K, V> registry, @MapKey K key,
      Provider<T> valueProvider) {
    registry.register(key, valueProvider);
  }
}
