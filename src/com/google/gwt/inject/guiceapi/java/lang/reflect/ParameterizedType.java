/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */

package java.lang.reflect;


/**
 * ParameterizedType represents a parameterized type such as
 * Collection&lt;String&gt;.
 *
 * <p>A parameterized type is created the first time it is needed by a
 * reflective method, as specified in this package. When a
 * parameterized type p is created, the generic type declaration that
 * p instantiates is resolved, and all type arguments of p are created
 * recursively. See {@link java.lang.reflect.TypeVariable
 * TypeVariable} for details on the creation process for type
 * variables. Repeated creation of a parameterized type has no effect.
 *
 * <p>Instances of classes that implement this interface must implement
 * an equals() method that equates any two instances that share the
 * same generic type declaration and have equal type parameters.
 *
 * @since 1.5
 */
public interface ParameterizedType extends Type {

}
