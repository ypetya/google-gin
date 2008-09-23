/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.inject.rebind.adapter;

import com.google.gwt.inject.client.GinModule;
import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Makes a {@link com.google.gwt.inject.client.GinModule}
 * available as a {@link com.google.inject.Module}.
 */
public final class GinModuleAdapter implements Module {
  private final GinModule gmodule;

  public GinModuleAdapter(GinModule ginModule) {
    this.gmodule = ginModule;
  }

  public void configure(Binder binder) {
    gmodule.configure(new BinderAdapter(binder));
  }
}
