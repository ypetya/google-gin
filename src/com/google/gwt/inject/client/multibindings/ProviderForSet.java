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
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A provider for the set of multi-binded values.
 *
 * @param <T> type of key for multi binding
 */
@Singleton
public class ProviderForSet<T> implements Provider<Set<T>> {

  private final MultibinderRegistry<T> registry;

  @Inject
  public ProviderForSet(MultibinderRegistry<T> registry) {
    this.registry = registry;
  }

  @Override
  public Set<T> get() {
    Set<T> set = new LinkedHashSet<T>();
    for (Provider<? extends T> provider : registry.getProviders()) {
      set.add(provider.get());
    }
    return Collections.unmodifiableSet(set);
  }
}
