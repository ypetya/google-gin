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

import java.util.Iterator;
import java.util.Set;

public class MultibinderTest extends GWTTestCase {

  public interface X {}

  public static class GinModuleWithNoBinding extends AbstractGinModule {
    @Override
    protected void configure() {
      Multibinder.newSetBinder(binder(), X.class);
    }
  }

  @GinModules(GinModuleWithNoBinding.class)
  public interface NoBindingInjector extends Ginjector {
    Set<X> getSet();
  }

  public void testInject_empty() throws Exception {
    NoBindingInjector injector = GWT.create(NoBindingInjector.class);
    Set<X> set = injector.getSet();
    assertTrue(set.isEmpty());
  }

  public static class X1 implements X {}
  public static class X2 implements X {}

  public static class GinModuleWithX extends AbstractGinModule {
    @Override
    protected void configure() {
      Multibinder.newSetBinder(binder(), X.class).addBinding().to(X1.class);
    }
  }

  public static class GinModuleWithMoreX extends AbstractGinModule {
    @Override
    protected void configure() {
      Multibinder<X> setBinder = Multibinder.newSetBinder(binder(), X.class);
      setBinder.addBinding().to(X2.class);
      setBinder.addBinding().to(X1.class);
    }
  }


  @GinModules({GinModuleWithX.class, GinModuleWithMoreX.class})
  public interface XInjector extends Ginjector {
    Set<X> getSetX();
  }


  public void testInjectSet() throws Exception {
    XInjector injector = GWT.create(XInjector.class);

    Set<X> setX = injector.getSetX();
    assertEquals(3, setX.size());

    Iterator<X> iterator = setX.iterator();
    assertTrue(iterator.next() instanceof X1);
    assertTrue(iterator.next() instanceof X2);
    assertTrue(iterator.next() instanceof X1);
  }

  public void testInjectSameSetMultipleTimes() throws Exception {
    XInjector injector = GWT.create(XInjector.class);
    Set<X> set1 = injector.getSetX();
    Set<X> set2 = injector.getSetX();
    assertEquals(set1.size(), set2.size());
    Iterator<X> iterator1 = set1.iterator();
    Iterator<X> iterator2 = set2.iterator();

    X element = iterator2.next();
    assertTrue(element instanceof X1);
    assertNotSame(iterator1.next(), element);

    element = iterator2.next();
    assertTrue(element instanceof X2);
    assertNotSame(iterator1.next(), element);

    element = iterator2.next();
    assertTrue(element instanceof X1);
    assertNotSame(iterator1.next(), element);
  }

  public interface Y {}
  public static class YImpl implements Y {}

  public static class GinModuleWithY extends AbstractGinModule {
    @Override
    protected void configure() {
      Multibinder.newSetBinder(binder(), Y.class).addBinding().to(YImpl.class);
    }
  }

  @GinModules(GinModuleWithY.class)
  public interface XYInjector extends XInjector {
    Set<Y> getSetY();
  }

  public void testInjectMultipleTypes() throws Exception {
    XYInjector injector = GWT.create(XYInjector.class);

    assertEquals(3, injector.getSetX().size());

    Set<Y> setY = injector.getSetY();
    assertEquals(1, setY.size());
    assertTrue(setY.iterator().next() instanceof YImpl);
  }

  public static class GinModuleWithMoreXInPrivateModule extends AbstractGinModule {
    @Override
    protected void configure() {
      install(new GinModuleWithMoreX());
    }
  }

  @GinModules({GinModuleWithX.class, GinModuleWithMoreXInPrivateModule.class})
  public interface XWithPrivateModuleInjector extends Ginjector {
    Set<X> getSetX();
  }

  public void testInjectSetWithBindingsFromPrivateModule() throws Exception {
    XWithPrivateModuleInjector injector = GWT.create(XWithPrivateModuleInjector.class);

    Set<X> setX = injector.getSetX();
    assertEquals(3, setX.size());

    Iterator<X> iterator = setX.iterator();
    assertTrue(iterator.next() instanceof X1);
    assertTrue(iterator.next() instanceof X2);
    assertTrue(iterator.next() instanceof X1);
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.inject.InjectTest";
  }
}
