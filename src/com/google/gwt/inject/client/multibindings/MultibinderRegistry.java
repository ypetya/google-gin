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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A registry to keep track of multiple bindings for a type.
 *
 * @param <T> type of key for multi binding
 */
public class MultibinderRegistry<T> {

  private final Set<Provider<T>> providers = new LinkedHashSet<Provider<T>>();

  @SuppressWarnings("unchecked")
  void register(Provider<? extends T> provider) {
    providers.add((Provider<T>) provider);
  }

  Set<Provider<T>> getProviders() {
    return providers;
  }
}
