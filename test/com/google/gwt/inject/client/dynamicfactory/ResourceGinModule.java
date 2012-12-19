package com.google.gwt.inject.client.dynamicfactory;

import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.ConstantProvider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

public class ResourceGinModule extends AbstractGinModule {
  public static class Registrator<T extends Resource> {
    @Inject
    public Registrator(DynamicFactory factory, ConstantProvider<T, String> provider, T resource) {
      factory.register(provider.get(), resource);
    }
  }

  private final String code;
  private final Class<? extends Resource> resource;

  public ResourceGinModule(String code, Class<? extends Resource> resource) {
    this.code = code;
    this.resource = resource;
  }

  @Override
  protected void configure() {
    bind(resource);
    bindConstant().parameterizedWith(TypeLiteral.get(resource)).to(code);

    Type registratorType = Types.newParameterizedTypeWithOwner(Registrator.class, Registrator.class, TypeLiteral.get(resource).getType());
    bind(TypeLiteral.get(registratorType)).asEagerSingleton();
  }
}