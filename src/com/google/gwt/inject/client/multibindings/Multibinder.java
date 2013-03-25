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
import static com.google.gwt.inject.client.multibindings.TypeLiterals.setOf;

import com.google.gwt.inject.client.GinModule;
import com.google.gwt.inject.client.binder.GinBinder;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import java.util.Set;

/**
 * A utility that mimics the behavior of MultiBinder for GIN.
 *
 * <p>Example usage:
 * <pre>
 *   interface X {};
 *
 *   class X1Impl implements X {};
 *
 *   class X2Impl implements X {};
 *
 *   Multibinder.newSetBinder(binder(), X.class)
 *      .addBinding().to(X1Impl.class);
 *      .addBinding().to(X2Impl.class);
 * </pre>
 *
 * By default Guice does not permit duplicates keys. However as this
 * implementation doesn't have any check for duplicates, default behavior is
 * same as permitting duplicates. So if you rely on this behavior, calling
 * {@link #permitDuplicates()} is going to protect your code from being broken
 * when these checks are implemented.
 *
 * @param <T> type of value for Set
 */
public final class Multibinder<T> implements GinModule {

  /**
   * Returns a new multibinder that collects instances of {@code type} in a
   * {@link Set} that is itself bound with no binding annotation.
   */
  public static <T> Multibinder<T> newSetBinder(GinBinder binder, TypeLiteral<T> type) {
    Multibinder<T> result = new Multibinder<T>(binder, type);
    binder.install(result);
    return result;
  }

  /**
   * Returns a new multibinder that collects instances of {@code type} in a
   * {@link Set} that is itself bound with no binding annotation.
   */
  public static <T> Multibinder<T> newSetBinder(GinBinder binder, Class<T> type) {
    return newSetBinder(binder, TypeLiteral.get(type));
  }

  private final GinBinder ginBinder;
  private final TypeLiteral<T> type;
  private final Key<MultibinderRegistry<T>> keyForMultibinder;

  private Multibinder(GinBinder ginBinder, TypeLiteral<T> type) {
    this.ginBinder = ginBinder;
    this.type = type;
    this.keyForMultibinder = Key.get(registryOf(type));
  }

  @Override
  public void configure(GinBinder binder) {
    ginBinder.bind(keyForMultibinder).in(Singleton.class);
    ginBinder.bind(setOf(type)).toProvider(Key.get(providerForSetOf(type)));
  }

  public Multibinder<T> permitDuplicates() {
    //TODO: Fix not permitting duplicates by default
    return this;
  }

  @Override
  public int hashCode() {
    return keyForMultibinder.hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Multibinder
        && ((Multibinder) obj).keyForMultibinder.equals(keyForMultibinder);
  }

  public interface BindingBuilder<T> {
    void to(Class<? extends T> implementation);
    void to(TypeLiteral<? extends T> implementation);
  }

  public BindingBuilder<T> addBinding() {
    return new BindingBuilder<T>() {
      @Override public void to(Class<? extends T> implementation) {
        to(TypeLiteral.get(implementation));
      }
      @Override public void to(TypeLiteral<? extends T> implementation) {
        ginBinder.install(new MultibinderRegistererModule<T>(type, implementation));
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <T> TypeLiteral<MultibinderRegistry<T>> registryOf(TypeLiteral<T> type) {
    return newParameterizedType(MultibinderRegistry.class, type);
  }

  @SuppressWarnings("unchecked")
  private static <T> TypeLiteral<ProviderForSet<T>> providerForSetOf(TypeLiteral<T> type) {
    return newParameterizedType(ProviderForSet.class, type);
  }
}
