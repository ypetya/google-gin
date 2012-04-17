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
package com.google.gwt.inject.rebind;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createNiceControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.inject.rebind.binding.BindingFactory;
import com.google.gwt.inject.rebind.binding.BindingFactoryImpl;
import com.google.gwt.inject.rebind.binding.Context;
import com.google.gwt.inject.rebind.resolution.BindingResolver;
import com.google.gwt.inject.rebind.util.GuiceUtil;
import com.google.gwt.inject.rebind.util.MemberCollector;
import com.google.gwt.inject.rebind.util.MethodCallUtil;
import com.google.gwt.inject.rebind.util.NameGenerator;
import com.google.inject.Key;
import com.google.inject.Provider;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;


public class GinjectorBindingsTest extends TestCase {

  private IMocksControl control;
  private IMocksControl niceControl;
  private BindingFactory bindingFactory;

  // Mocks:
  private NameGenerator nameGenerator;
  private TreeLogger logger;
  private GuiceUtil guiceUtil;
  private Provider<GinjectorBindings> ginjectorBindingsProvider;
  private MemberCollector collector;
  private ErrorManager errorManager;
  private BindingResolver bindingResolver;

  private MethodCallUtil methodCallUtil;

  private final Context context = Context.forText("");

  @Override
  public void setUp() {
    control = createControl();
    niceControl = createNiceControl();

    nameGenerator = control.createMock("nameGenerator", NameGenerator.class);
    logger = niceControl.createMock("logger", TreeLogger.class);
    guiceUtil = control.createMock("guiceUtil", GuiceUtil.class);

    {
      @SuppressWarnings("unchecked")
      Provider<GinjectorBindings> tmpProvider =
          control.createMock("ginjectorBindingsProvider", Provider.class);
      ginjectorBindingsProvider = tmpProvider;
    }
    collector = control.createMock("collector", MemberCollector.class);
    errorManager = control.createMock("errorManager", ErrorManager.class);
    bindingResolver = control.createMock("bindingResolver", BindingResolver.class);

    methodCallUtil = control.createMock("methodCallUtil", MethodCallUtil.class);

    bindingFactory = new BindingFactoryImpl(
        errorManager, guiceUtil, DummyInjectorInterface.class, methodCallUtil);
  }

  /** Replay all mocks. */
  private void replay() {
    control.replay();
    niceControl.replay();
  }

  /** Verify all mocks and reset them. */
  private void verifyAndReset() {
    control.verify();
    niceControl.verify();

    control.reset();
    niceControl.reset();
  }

  public void testGetChildWhichBindsLocally() {
    GinjectorBindings topBindings = createBindings();
    GinjectorBindings childBindings = createBindings();

    expect(ginjectorBindingsProvider.get()).andReturn(childBindings);

    replay();

    topBindings.createChildGinjectorBindings(GinjectorBindingsTest.class);

    Key<?> fromKey = Key.get(GinjectorBindingsTest.class);
    Key<?> toKey = Key.get(Long.class);

    childBindings.addBinding(fromKey, bindingFactory.getBindClassBinding(fromKey, toKey, context));

    verifyAndReset();
    assertEquals(childBindings, topBindings.getChildWhichBindsLocally(fromKey));
  }

  public void testIsBoundLocallyInChild() {
    GinjectorBindings topBindings = createBindings();
    GinjectorBindings childBindings = createBindings();

    expect(ginjectorBindingsProvider.get()).andReturn(childBindings);

    replay();

    topBindings.createChildGinjectorBindings(GinjectorBindingsTest.class);

    Key<?> fromKey = Key.get(GinjectorBindingsTest.class);
    Key<?> toKey = Key.get(Long.class);

    childBindings.addBinding(fromKey, bindingFactory.getBindClassBinding(fromKey, toKey, context));

    verifyAndReset();
    assertTrue(topBindings.isBoundLocallyInChild(fromKey));
  }

  public void testIsBoundLocallyInChild_ignoresParentBindings() {
    GinjectorBindings topBindings = createBindings();
    GinjectorBindings childBindings = createBindings();

    expect(ginjectorBindingsProvider.get()).andReturn(childBindings);

    replay();

    topBindings.createChildGinjectorBindings(GinjectorBindingsTest.class);

    Key<?> key = Key.get(GinjectorBindingsTest.class);

    childBindings.addBinding(key, bindingFactory.getParentBinding(key, topBindings, context));

    verifyAndReset();
    assertFalse(topBindings.isBoundLocallyInChild(key));
  }

  public void testisBoundLocallyInChild_ignoresParentBindingsButKeepsOthers() {
    GinjectorBindings topBindings = createBindings();
    GinjectorBindings childBindings1 = createBindings();
    GinjectorBindings childBindings2 = createBindings();
    GinjectorBindings childBindings3 = createBindings();

    expect(ginjectorBindingsProvider.get()).andReturn(childBindings1);
    expect(ginjectorBindingsProvider.get()).andReturn(childBindings2);
    expect(ginjectorBindingsProvider.get()).andReturn(childBindings3);

    replay();

    topBindings.createChildGinjectorBindings(GinjectorBindingsTest.class);
    topBindings.createChildGinjectorBindings(GinjectorBindingsTest.class);
    topBindings.createChildGinjectorBindings(GinjectorBindingsTest.class);

    Key<?> key = Key.get(GinjectorBindingsTest.class);
    Key<?> toKey = Key.get(Long.class);

    childBindings1.addBinding(key, bindingFactory.getParentBinding(key, topBindings, context));
    childBindings2.addBinding(key, bindingFactory.getBindClassBinding(key, toKey, context));
    childBindings3.addBinding(key, bindingFactory.getParentBinding(key, topBindings, context));

    verifyAndReset();
    assertTrue(topBindings.isBoundLocallyInChild(key));
  }

  private GinjectorBindings createBindings() {
    collector.setMethodFilter(EasyMock.<MemberCollector.MethodFilter>anyObject());

    replay();

    GinjectorBindings result = new GinjectorBindings(
        nameGenerator, logger, guiceUtil, DummyInjectorInterface.class, ginjectorBindingsProvider,
        collector, errorManager, bindingResolver);

    verifyAndReset();

    return result;
  }

  private interface DummyInjectorInterface extends Ginjector {
  }
}
