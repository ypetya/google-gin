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

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A helper that gets injected by a value provider and registers it to a target
 * registry.
 * <p>
 * In GWT compilation time, Multibinder adds a binding for this class with
 * specific generic types and marks is at eager-singleton, so that GIN generates
 * code that creates the instance of this class when the injector is created. In
 * the runtime, when this class is instantiated, it registers the value provider
 * to the actual provider of the target Set.
 *
 * @param <T> type of value for set
 * @param <V> type of implementation for value
 */
public class MultibinderRegisterer<T, V extends T> {
  @Inject
  public MultibinderRegisterer(MultibinderRegistry<T> registry, Provider<V> valueProvider) {
    registry.register(valueProvider);
  }
}
