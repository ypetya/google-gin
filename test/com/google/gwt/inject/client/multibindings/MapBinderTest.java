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

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import java.util.Map;

public class MapBinderTest extends GWTTestCase {

  public interface X {}

  public static class GinModuleWithNoBinding extends AbstractGinModule {
    @Override
    protected void configure() {
      MapBinder.newMapBinder(binder(), String.class, X.class);
    }
  }

  @GinModules(GinModuleWithNoBinding.class)
  public interface NoBindingInjector extends Ginjector {
    Map<String, X> getMap();
  }

  public void testInjectEmptyMap() throws Exception {
    NoBindingInjector injector = GWT.create(NoBindingInjector.class);
    Map<String, X> map = injector.getMap();
    assertTrue(map.isEmpty());
  }

  public static class X1 implements X {}
  public static class X2 implements X {}

  public static class GinModuleWithX extends AbstractGinModule {
    @Override
    protected void configure() {
      MapBinder<String, X> mapBinder = MapBinder.newMapBinder(binder(), String.class, X.class);
      mapBinder.addBinding("1").to(X1.class);
      mapBinder.addBinding("2").to(X2.class);
    }
  }

  public static class X3<T> implements X {
    private final T object;
    @Inject
    public X3(T object) {
      this.object = object;
    }
  }

  public static class GinModuleWithMoreX extends AbstractGinModule {
    @Override
    protected void configure() {
      MapBinder<String, X> mapBinder = MapBinder.newMapBinder(binder(), String.class, X.class);
      mapBinder.addBinding("3").to(X1.class);
      TypeLiteral<X3<YImpl>> x3 = new TypeLiteral<X3<YImpl>>() {};
      mapBinder.addBinding("4").to(x3);
    }
  }

  @GinModules({GinModuleWithX.class, GinModuleWithMoreX.class})
  public interface XInjector extends Ginjector {
    Map<String, X> getMapX();
  }

  public void testInjectMap() throws Exception {
    XInjector injector = GWT.create(XInjector.class);

    Map<String, X> mapX = injector.getMapX();
    assertEquals(4, mapX.size());
    assertTrue(mapX.get("1") instanceof X1);
    assertTrue(mapX.get("2") instanceof X2);
    assertTrue(mapX.get("3") instanceof X1);
    assertTrue(mapX.get("4") instanceof X3<?>);
    assertTrue(((X3<?>) mapX.get("4")).object instanceof YImpl);
  }

  public void testInjectSameMapMultipleTimes() throws Exception {
    XInjector injector = GWT.create(XInjector.class);

    Map<String, X> map1 = injector.getMapX();
    Map<String, X> map2 = injector.getMapX();
    assertEquals(map1.size(), map2.size());

    assertTrue(map2.get("1") instanceof X1);
    assertNotSame(map1.get("1"), map2.get("1"));

    assertTrue(map2.get("2") instanceof X2);
    assertNotSame(map1.get("2"), map2.get("2"));
  }


  public interface Y {}
  public static class YImpl implements Y {}

  public static class GinModuleWithY extends AbstractGinModule {
    @Override
    protected void configure() {
      MapBinder.newMapBinder(binder(), String.class, Y.class).addBinding("1").to(YImpl.class);
    }
  }

  @GinModules(GinModuleWithY.class)
  public interface XYInjector extends XInjector {
    Map<String, Y> getMapY();
  }

  public void testInjectMultipleTypes() throws Exception {
    XYInjector injector = GWT.create(XYInjector.class);

    assertEquals(4, injector.getMapX().size());

    Map<String, Y> mapY = injector.getMapY();
    assertEquals(1, mapY.size());
    assertTrue(mapY.get("1") instanceof YImpl);
  }


  public static class GinModuleWithMoreXInPrivateModule extends AbstractGinModule {
    @Override
    protected void configure() {
      install(new GinModuleWithMoreX());
    }
  }

  @GinModules({GinModuleWithX.class, GinModuleWithMoreXInPrivateModule.class})
  public interface XWithPrivateModuleInjector extends Ginjector {
    Map<String, X> getMapX();
  }

  public void testInjectMapWithBindingsFromPrivateModule() throws Exception {
    XWithPrivateModuleInjector injector = GWT.create(XWithPrivateModuleInjector.class);

    Map<String, X> mapX = injector.getMapX();
    assertEquals(4, mapX.size());
    assertTrue(mapX.get("1") instanceof X1);
    assertTrue(mapX.get("2") instanceof X2);
    assertTrue(mapX.get("3") instanceof X1);
    assertTrue(mapX.get("4") instanceof X3<?>);
    assertTrue(((X3<?>) mapX.get("4")).object instanceof YImpl);
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.inject.InjectTest";
  }
}
