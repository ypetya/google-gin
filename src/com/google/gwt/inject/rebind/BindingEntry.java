/*
 * Copyright 2010 Google Inc.
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

import com.google.gwt.inject.rebind.binding.Binding;
import com.google.gwt.inject.rebind.binding.BindingContext;

/**
 * A single entry in the table of bindings maintained by
 * {@link BindingProcessor}.
 */
class BindingEntry {

  private final Binding binding;
  private final BindingContext bindingContext;

  public BindingEntry(Binding binding, BindingContext bindingContext) {
    this.binding = binding;
    this.bindingContext = bindingContext;
  }

  public Binding getBinding() {
    return binding;
  }

  public BindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public String toString() {
    return binding.toString();
  }
}
