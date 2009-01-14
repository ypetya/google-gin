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
package com.google.gwt.inject.rebind.binding;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.inject.rebind.util.SourceWriteUtil;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.inject.Inject;
import com.google.inject.Key;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 * Binding for a constant value.
 */
public class BindConstantBinding implements Binding {
  private String valueToOutput;

  /**
   * Returns true if the provided key is a valid constant key, i.e. if a
   * constant binding can be legally created for it.
   *
   * @param key key to check
   * @return true if constant key
   */
  public static boolean isConstantKey(Key<?> key) {
    Type type = key.getTypeLiteral().getType();

    if (!(type instanceof Class)) {
      return false;
    }

    Class clazz = (Class) type;
    return clazz == String.class || clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)
        || Character.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
        || clazz.isEnum();
  }

  private final SourceWriteUtil sourceWriteUtil;

  @Inject
  public BindConstantBinding(SourceWriteUtil sourceWriteUtil) {
    this.sourceWriteUtil = sourceWriteUtil;
  }

  /**
   * Sets this binding's key and instance.  Must be called before
   * writeCreatorMethod is invoked.
   *
   * @param key key to bind to
   * @param instance value to bind  to
   */
  public <T> void setKeyAndInstance(Key<T> key, T instance) {
    Type type = key.getTypeLiteral().getType();

    if (type == String.class) {
      valueToOutput = "\"" + Generator.escape(instance.toString()) + "\"";
    } else if (type == Character.class) {
      // TODO(bstoler): Need to escape to work in all cases
      valueToOutput = "'" + instance.toString() + "'";
    } else if (instance instanceof Number || instance instanceof Boolean) {
      // TODO(bstoler): May need type qualifier on numbers
      valueToOutput = instance.toString();
    } else if (instance instanceof Enum) {
      valueToOutput = instance.getClass().getName() + "." + ((Enum) instance).name();
    } else {
      throw new IllegalArgumentException("Attempted to create a constant binding with a "
          + "non-constant type: " + type);
    }
  }

  public void writeCreatorMethods(SourceWriter writer, String creatorMethodSignature) {
    assert valueToOutput != null;

    sourceWriteUtil.writeMethod(writer, creatorMethodSignature, "return " + valueToOutput + ";");
  }

  public Set<Key<?>> getRequiredKeys() {
    return Collections.emptySet();
  }
}
