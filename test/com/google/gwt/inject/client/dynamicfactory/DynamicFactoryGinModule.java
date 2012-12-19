package com.google.gwt.inject.client.dynamicfactory;

import javax.inject.Singleton;

import com.google.gwt.inject.client.AbstractGinModule;

public class DynamicFactoryGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(DynamicFactory.class).in(Singleton.class);

    install(new ResourceGinModule("one", OneResource.class));
    install(new ResourceGinModule("two", TwoResource.class));
  }
}
