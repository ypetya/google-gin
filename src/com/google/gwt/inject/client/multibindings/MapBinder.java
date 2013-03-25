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

import static com.google.gwt.inject.client.multibindings.TypeLiterals.mapOf;
import static com.google.gwt.inject.client.multibindings.TypeLiterals.newParameterizedType;

import com.google.gwt.inject.client.GinModule;
import com.google.gwt.inject.client.binder.GinBinder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import java.util.Map;

/**
 * A utility that mimics the behavior of MapBinder for GIN.
 *
 * <p>Example usage:
 * <pre>
 *   interface X {};
 *
 *   class X1Impl implements X {};
 *
 *   class X2Impl implements X {};
 *
 *   MapBinder.newMapBinder(binder(), String.class, X.class)
 *      .addBinding("id1").to(X1Impl.class);
 *      .addBinding("id2").to(X2Impl.class);
 * </pre>
 * <p> Also a provider could be used to bind non-constant keys.
 *
 * <p> By default Guice does not permit duplicates items. However as this
 * implementation doesn't have any check for duplicates, default behavior is
 * same as permitting duplicates. So if you rely on this behavior, calling
 * {@link #permitDuplicates()} is going to protect your code from being broken
 * when these checks are implemented.
 *
 * @param <K> type of key for map
 * @param <V> type of value for map
 */
public final class MapBinder<K, V> implements GinModule {

  /**
   * Returns a new mapbinder that collects entries of {@code keyType}/{@code
   * valueType} in a {@link Map} that is itself bound with no binding
   * annotation.
   */
  public static <K, V> MapBinder<K, V> newMapBinder(
      GinBinder binder, Class<K> keyType, Class<V> valueType) {
    return newMapBinder(binder, TypeLiteral.get(keyType), TypeLiteral.get(valueType));
  }

  /**
   * Returns a new mapbinder that collects entries of {@code keyType}/{@code
   * valueType} in a {@link Map} that is itself bound with no binding
   * annotation.
   */
  public static <K, V> MapBinder<K, V> newMapBinder(
      GinBinder binder, TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
    MapBinder<K, V> mapBinder = new MapBinder<K, V>(binder, keyType, valueType);
    binder.install(mapBinder);
    return mapBinder;
  }

  private final GinBinder ginBinder;
  private final TypeLiteral<K> keyType;
  private final TypeLiteral<V> valueType;
  private final Key<MapBinderRegistry<K, V>> keyForMapBinder;

  private MapBinder(GinBinder ginBinder, TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
    this.ginBinder = ginBinder;
    this.keyType = keyType;
    this.valueType = valueType;
    this.keyForMapBinder = Key.get(registryOf(keyType, valueType));
  }

  @Override
  public void configure(GinBinder binder) {
    ginBinder.bind(keyForMapBinder).in(Singleton.class);
    ginBinder.bind(mapOf(keyType, valueType)).toProvider(
        Key.get(providerForMapOf(keyType, valueType)));
  }

  public MapBinder<K, V> permitDuplicates() {
    //TODO: Fix not permitting duplicates by default
    return this;
  }

  @Override
  public int hashCode() {
    return keyForMapBinder.hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    return obj instanceof MapBinder && ((MapBinder) obj).keyForMapBinder.equals(keyForMapBinder);
  }

  public interface BindingBuilder<V> {
    void to(Class<? extends V> implementation);
    void to(TypeLiteral<? extends V> implementation);
  }

  public BindingBuilder<V> addBinding(final K key) {
    return new BindingBuilder<V>() {
      @Override public void to(Class<? extends V> implementation) {
        to(TypeLiteral.get(implementation));
      }
      @Override public void to(TypeLiteral<? extends V> implementation) {
        ginBinder.install(
            new MapBinderRegistererModule<K, V>(keyType, valueType, key, implementation));
      }
    };
  }

  public BindingBuilder<V> addBinding(final Class<? extends Provider<? extends K>> keyProvider) {
    return new BindingBuilder<V>() {
      @Override public void to(Class<? extends V> implementation) {
        to(TypeLiteral.get(implementation));
      }
      @Override public void to(TypeLiteral<? extends V> implementation) {
        ginBinder.install(
            new MapBinderRegistererModule<K, V>(keyType, valueType, keyProvider, implementation));
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <K, V> TypeLiteral<MapBinderRegistry<K, V>> registryOf(TypeLiteral<K> keyType,
      TypeLiteral<V> valueType) {
    return newParameterizedType(MapBinderRegistry.class, keyType, valueType);
  }

  @SuppressWarnings("unchecked")
  private static <K, V> TypeLiteral<ProviderForMap<K, V>> providerForMapOf(TypeLiteral<K> keyType,
      TypeLiteral<V> valueType) {
    return newParameterizedType(ProviderForMap.class, keyType, valueType);
  }
}
