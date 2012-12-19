package com.google.gwt.inject.client.dynamicfactory;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;

public class DynamicFactoryTest extends GWTTestCase {
  
  public void testOne() {
    DynamicFactoryGinjector injector = GWT.create(DynamicFactoryGinjector.class);
    DynamicFactory dynamicFactory = injector.getDynamicFactory();
    Resource resource = dynamicFactory.get("one");
    assertNotNull(resource);
    assertEquals("one", resource.invoke());
  }

  public void testTwo() {
    DynamicFactoryGinjector injector = GWT.create(DynamicFactoryGinjector.class);
    DynamicFactory dynamicFactory = injector.getDynamicFactory();
    Resource resource = dynamicFactory.get("two");
    assertNotNull(resource);
    assertEquals("two", resource.invoke());
  }

  public String getModuleName() {
    return "com.google.gwt.inject.InjectTest";
  }
}
