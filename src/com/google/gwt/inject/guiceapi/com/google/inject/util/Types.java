/**
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.inject.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Static methods for working with types.
 *
 * @author crazybob@google.com (Bob Lee)
 * @since 2.0
 */
public final class Types {
  private Types() {}

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to
   * {@code rawType}. The returned type does not have an owner type.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to
   * {@code rawType} and enclosed by {@code ownerType}.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType newParameterizedTypeWithOwner(
      Type ownerType, Type rawType, Type... typeArguments) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns an array type whose elements are all instances of
   * {@code componentType}.
   *
   * @return a {@link java.io.Serializable serializable} generic array type.
   */
  public static GenericArrayType arrayOf(Type componentType) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns a type that represents an unknown type that extends {@code bound}.
   * For example, if {@code bound} is {@code CharSequence.class}, this returns
   * {@code ? extends CharSequence}. If {@code bound} is {@code Object.class},
   * this returns {@code ?}, which is shorthand for {@code ? extends Object}.
   */
  public static WildcardType subtypeOf(Type bound) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns a type that represents an unknown supertype of {@code bound}. For
   * example, if {@code bound} is {@code String.class}, this returns {@code ?
   * super String}.
   */
  public static WildcardType supertypeOf(Type bound) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns a type modelling a {@link List} whose elements are of type
   * {@code elementType}.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType listOf(Type elementType) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns a type modelling a {@link Set} whose elements are of type
   * {@code elementType}.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType setOf(Type elementType) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  /**
   * Returns a type modelling a {@link Map} whose keys are of type
   * {@code keyType} and whose values are of type {@code valueType}.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType mapOf(Type keyType, Type valueType) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }

  // for other custom collections types, use newParameterizedType()

  /**
   * Returns a type modelling a {@link Provider} that provides elements of type
   * {@code elementType}.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType providerOf(Type providedType) {
      throw new UnsupportedOperationException("Should never be called in client code.");
  }
}