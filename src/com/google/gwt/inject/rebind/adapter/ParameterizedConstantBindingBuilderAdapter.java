package com.google.gwt.inject.rebind.adapter;

import java.lang.reflect.Type;

import com.google.gwt.inject.client.ConstantProvider;
import com.google.gwt.inject.client.binder.GinConstantBindingBuilder;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

public class ParameterizedConstantBindingBuilderAdapter implements GinConstantBindingBuilder {
  private Binder binder;
  private TypeLiteral<?> parameter;

  public ParameterizedConstantBindingBuilderAdapter(Binder binder, TypeLiteral<?> parameter) {
    this.binder = binder;
    this.parameter = parameter;
  }

  @Override
  public void to(String s) {
    to(String.class, s);
  }

  @Override
  public void to(int i) {
    to(Integer.class, i);
  }

  @Override
  public void to(long l) {
    to(Long.class, l);
  }

  @Override
  public void to(boolean b) {
    to(Boolean.class, b);
  }

  @Override
  public void to(double v) {
    to(Double.class, v);
  }

  @Override
  public void to(float v) {
    to(Float.class, v);
  }

  @Override
  public void to(short i) {
    to(Short.class, i);
  }

  @Override
  public void to(char c) {
    to(Character.class, c);
  }

  @Override
  public void to(Class<?> aClass) {
    to(Class.class, aClass);
  }

  @Override
  public <E extends Enum<E>> void to(E e) {
    to((Class) e.getClass(), e);
  }

  private <T> void to(Class<T> clazz, final T instance) {
    Type providerType = Types.newParameterizedType(ConstantProvider.class, parameter.getType(), clazz);
    Key<ConstantProvider<?, T>> key = (Key)Key.get(providerType);
    binder.bind(key).toInstance(new ConstantProvider<Object, T>() {
      public T get() {
        return instance;
      }
    });
  }
}
