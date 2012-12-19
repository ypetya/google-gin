package com.google.gwt.inject.client.dynamicfactory;



import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules({ DynamicFactoryGinModule.class })
public interface DynamicFactoryGinjector extends Ginjector {
  
  DynamicFactory getDynamicFactory();
}
