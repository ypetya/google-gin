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

import static com.google.gwt.inject.client.multibindings.TypeLiterals.newParameterizedType;

import com.google.gwt.inject.client.PrivateGinModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * A private gin module to bind MultibinderRegisterer.
 *
 * @param <T> type of Set entry
 */
public class MultibinderRegistererModule<T> extends PrivateGinModule {
  private final TypeLiteral<T> type;
  private final TypeLiteral<? extends T> implementation;

  public MultibinderRegistererModule(TypeLiteral<T> type, TypeLiteral<? extends T> implementation) {
    this.type = type;
    this.implementation = implementation;
  }

  @Override
  protected void configure() {
    bind(Key.get(registererOf(type, implementation))).asEagerSingleton();
  }

  private static <T> TypeLiteral<?> registererOf(TypeLiteral<T> type,
      TypeLiteral<? extends T> implementation) {
    return newParameterizedType(MultibinderRegisterer.class, type, implementation);
  }
}
