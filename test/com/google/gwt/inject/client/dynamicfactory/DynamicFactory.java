package com.google.gwt.inject.client.dynamicfactory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class DynamicFactory {

  private final Map<String, Resource> resources = new HashMap<String, Resource>();

  public Resource get(String code) {
    return resources.get(code);
  }

  protected void register(String code, Resource resource) {
    resources.put(code, resource);
  }
}