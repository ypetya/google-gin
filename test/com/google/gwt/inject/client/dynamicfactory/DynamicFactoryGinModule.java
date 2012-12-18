package com.google.gwt.inject.client.dynamicfactory;

import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.ConstantProvider;
import com.google.gwt.inject.client.dynamicfactory.DynamicFactoryGinjector.DynamicFactory;
import com.google.gwt.inject.client.dynamicfactory.DynamicFactoryGinjector.OneResource;
import com.google.gwt.inject.client.dynamicfactory.DynamicFactoryGinjector.Resource;
import com.google.gwt.inject.client.dynamicfactory.DynamicFactoryGinjector.TwoResource;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

public class DynamicFactoryGinModule extends AbstractGinModule {

  public static class Registrator<T extends Resource> {
    @Inject
    public Registrator(DynamicFactory factory, ConstantProvider<T, String> provider, T resource) {
      factory.register(provider.get(), resource);
    }
  }

  public static class ResourceGinModule extends AbstractGinModule {
    private final String code;
    private final Class<? extends Resource> resource;

    public ResourceGinModule(String code, Class<? extends Resource> resource) {
      this.code = code;
      this.resource = resource;
    }

    @Override
    protected void configure() {
      bind(resource);
      bindInstanceToParameter(TypeLiteral.get(resource), code);

      Type registratorType = Types.newParameterizedType(Registrator.class, TypeLiteral.get(resource).getType());
      bind(TypeLiteral.get(registratorType)).asEagerSingleton();
    }
  }

  @Override
  protected void configure() {
    bind(DynamicFactory.class);

    install(new ResourceGinModule("one", OneResource.class));
    install(new ResourceGinModule("two", TwoResource.class));
  }
}
