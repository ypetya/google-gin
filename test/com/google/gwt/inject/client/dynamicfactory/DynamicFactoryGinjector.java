package com.google.gwt.inject.client.dynamicfactory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.google.gwt.inject.client.GinModules;
import com.google.inject.Injector;

@GinModules({ DynamicFactoryGinModule.class })
public interface DynamicFactoryGinjector extends Injector {
  
  public interface Resource {
    String invoke();
  }
  
  public static class OneResource implements Resource {
    @Override
    public String invoke() {
      return "one";
    }
  }
  
  public static class TwoResource implements Resource {
    @Override
    public String invoke() {
      return "two";
    }
  }

  @Singleton
  public static class DynamicFactory {

    private final Map<String, Resource> resources = new HashMap<String, Resource>();

    public Resource get(String code) {
      return resources.get(code);
    }

    protected void register(String code, Resource resource) {
      resources.put(code, resource);
    }
  }

  DynamicFactory getDynamicFactory();
}
