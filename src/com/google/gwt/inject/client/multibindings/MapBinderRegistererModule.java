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
import com.google.gwt.inject.client.binder.GinConstantBindingBuilder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A private gin module to bind MapProviderRegisterer.
 *
 * @param <K> type of map key
 * @param <V> type of map value
 */
public class MapBinderRegistererModule<K, V> extends PrivateGinModule {

  @BindingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER})
  public static @interface MapKey {}

  private final TypeLiteral<K> keyType;
  private final TypeLiteral<V> valueType;
  private final K key;
  private final Class<? extends Provider<? extends K>> keyProvider;
  private final TypeLiteral<? extends V> value;

  public MapBinderRegistererModule(
      TypeLiteral<K> keyType, TypeLiteral<V> valueType, K key, TypeLiteral<? extends V> value) {
    this.keyType = keyType;
    this.valueType = valueType;
    this.key = key;
    this.keyProvider = null;
    this.value = value;
  }

  public MapBinderRegistererModule(TypeLiteral<K> keyType, TypeLiteral<V> valueType,
      Class<? extends Provider<? extends K>> keyProvider, TypeLiteral<? extends V> value) {
    this.keyType = keyType;
    this.valueType = valueType;
    this.key = null;
    this.keyProvider = keyProvider;
    this.value = value;
  }

  @Override
  protected void configure() {
    if (key != null) {
      bindToContant(bindConstant().annotatedWith(MapKey.class), key);
    } else {
      bind(keyType).annotatedWith(MapKey.class).toProvider(keyProvider);
    }
    bind(Key.get(registererOf(keyType, valueType, value))).asEagerSingleton();
  }

  @SuppressWarnings("unchecked")
  private static void bindToContant(GinConstantBindingBuilder bindKey, Object key) {
    if (key instanceof String) {
      bindKey.to((String) key);
    } else if (key instanceof Enum<?>) {
      bindKey.to((Enum) key);
    } else if (key instanceof Integer) {
      bindKey.to((Integer) key);
    } else if (key instanceof Long) {
      bindKey.to((Long) key);
    } else if (key instanceof Float) {
      bindKey.to((Float) key);
    } else if (key instanceof Double) {
      bindKey.to((Double) key);
    } else if (key instanceof Short) {
      bindKey.to((Short) key);
    } else if (key instanceof Boolean) {
      bindKey.to((Boolean) key);
    } else if (key instanceof Character) {
      bindKey.to((Character) key);
    } else if (key instanceof Class<?>) {
      bindKey.to((Class<?>) key);
    } else {
      throw new AssertionError("A non-constant type bind as a key");
    }
  }

  private static <K, T> TypeLiteral<?> registererOf(TypeLiteral<K> keyType,
      TypeLiteral<T> valueType, TypeLiteral<? extends T> valueImplType) {
    return newParameterizedType(MapBinderRegisterer.class, keyType, valueType, valueImplType);
  }
}
