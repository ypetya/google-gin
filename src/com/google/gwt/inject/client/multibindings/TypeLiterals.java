// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.gwt.inject.client.multibindings;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import java.util.Map;
import java.util.Set;

/**
 * Utilities for creating TypeLiteral instances.
 */
class TypeLiterals {

  private TypeLiterals() {}

  @SuppressWarnings("unchecked")
  public static <K, V> TypeLiteral<Map<K, V>> mapOf(TypeLiteral<K> key, TypeLiteral<V> value) {
    return newParameterizedType(Map.class, key, value);
  }

  @SuppressWarnings("unchecked")
  public static <V> TypeLiteral<Set<V>> setOf(TypeLiteral<V> type) {
    return newParameterizedType(Set.class, type);
  }

  @SuppressWarnings("unchecked")
  public static <V> TypeLiteral<Provider<V>> providerOf(TypeLiteral<V> type) {
    return newParameterizedType(Provider.class, type);
  }

  public static TypeLiteral newParameterizedType(Class<?> baseClass, TypeLiteral<?> t1) {
    return TypeLiteral.get(Types.newParameterizedType(baseClass, t1.getType()));
  }

  public static TypeLiteral newParameterizedType(Class<?> baseClass, TypeLiteral<?> t1,
      TypeLiteral<?> t2) {
    return TypeLiteral.get(Types.newParameterizedType(baseClass, t1.getType(), t2.getType()));
  }

  public static TypeLiteral newParameterizedType(Class<?> baseClass, TypeLiteral<?> t1,
      TypeLiteral<?> t2, TypeLiteral<?> t3) {
    return TypeLiteral.get(Types.newParameterizedType(baseClass, t1.getType(), t2.getType(),
        t3.getType()));
  }

}
