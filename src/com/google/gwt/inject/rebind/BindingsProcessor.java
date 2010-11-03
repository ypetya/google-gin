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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModule;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.assistedinject.FactoryModule;
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.gwt.inject.rebind.adapter.GwtDotCreateProvider;
import com.google.gwt.inject.rebind.binding.AsyncProviderBinding;
import com.google.gwt.inject.rebind.binding.BindClassBinding;
import com.google.gwt.inject.rebind.binding.BindConstantBinding;
import com.google.gwt.inject.rebind.binding.BindProviderBinding;
import com.google.gwt.inject.rebind.binding.Binding;
import com.google.gwt.inject.rebind.binding.BindingContext;
import com.google.gwt.inject.rebind.binding.BindingIndex;
import com.google.gwt.inject.rebind.binding.CallConstructorBinding;
import com.google.gwt.inject.rebind.binding.CallGwtDotCreateBinding;
import com.google.gwt.inject.rebind.binding.FactoryBinding;
import com.google.gwt.inject.rebind.binding.GinjectorBinding;
import com.google.gwt.inject.rebind.binding.ImplicitProviderBinding;
import com.google.gwt.inject.rebind.binding.ProviderMethodBinding;
import com.google.gwt.inject.rebind.binding.RemoteServiceProxyBinding;
import com.google.gwt.inject.rebind.binding.RequiredKeys;
import com.google.gwt.inject.rebind.util.KeyUtil;
import com.google.gwt.inject.rebind.util.MemberCollector;
import com.google.gwt.inject.rebind.util.NameGenerator;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvidedBy;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.internal.ProviderMethod;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.Message;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.UntargettedBinding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

/**
 * Builds up the bindings and scopes for this {@code Ginjector}.
 */
@Singleton
class BindingsProcessor implements BindingIndex {

  /**
   * Type array representing zero arguments for a method.
   */
  private static final JType[] ZERO_ARGS = new JType[0];

  private final TreeLogger logger;

  /**
   * Generates names for code we produce to resolve injection requests.
   */
  private final NameGenerator nameGenerator;

  /**
   * Map from key to binding for all types we already have a binding for.
   */
  private final Map<Key<?>, BindingEntry> bindings = new HashMap<Key<?>, BindingEntry>();

  /**
   * Map from key to scope for all types we have a binding for.
   */
  private final Map<Key<?>, GinScope> scopes = new HashMap<Key<?>, GinScope>();

  /**
   * Set of keys for classes that we still need to resolve. Every time a
   * binding is added to {@code bindings}, the key is removed from this set.
   * When this set and {@code unresolvedOptional} becomes empty, we know we've
   * satisfied all dependencies.
   */
  private final Set<Key<?>> unresolved = new HashSet<Key<?>>();

  /**
   * Set of keys for classes that we still need to resolve but that are
   * optionally bound. Every time a binding is added to {@code bindings},
   * the key is removed from this set. When this set and {@code unresolved}
   * becomes empty, we know we've satisfied all dependencies.
   */
  private final Set<Key<?>> unresolvedOptional = new HashSet<Key<?>>();

  /**
   * Collection of keys for which the ginjector interface provides member
   * inject methods. If a regular binding is defined for the same key, no
   * special member inject handling is required - a member inject method will
   * be created as part of a regular binding.
   */
  private final Set<Key<?>> memberInjectRequests = new HashSet<Key<?>>();

  /**
   * All types for which static injection has been requested.
   */
  private final Set<Class<?>> staticInjectionRequests = new HashSet<Class<?>>();

  /**
   * Collector that gathers all methods from an injector.
   */
  private final MemberCollector completeCollector;

  private final Provider<CallGwtDotCreateBinding> callGwtDotCreateBindingProvider;
  private final Provider<RemoteServiceProxyBinding> remoteServiceProxyBindingProvider;
  private final Provider<CallConstructorBinding> callConstructorBinding;
  private final Provider<BindClassBinding> bindClassBindingProvider;
  private final Provider<BindProviderBinding> bindProviderBindingProvider;
  private final Provider<ImplicitProviderBinding> implicitProviderBindingProvider;
  private final Provider<AsyncProviderBinding> asyncProviderBindingProvider;
  private final Provider<ProviderMethodBinding> providerMethodBindingProvider;
  private final Provider<BindConstantBinding> bindConstantBindingProvider;
  private final Provider<GinjectorBinding> ginjectorBindingProvider;
  private final Provider<FactoryBinding> factoryBindingProvider;

  private final KeyUtil keyUtil;

  /**
   * Collection of all factory modules configured in gin modules.
   */
  private final Set<FactoryModule<?>> factoryModules = new HashSet<FactoryModule<?>>();

  /**
   * Interface of the injector that this class is implementing.
   */
  private final JClassType ginjectorInterface;

  /**
   * Module used to pretend to Guice about the source of all generated binding
   * targets.
   */
  private final LieToGuiceModule lieToGuiceModule;

  /**
   * Keeps track of whether we've found an error so we can eventually throw
   * an {@link UnableToCompleteException}. We do this instead of throwing
   * immediately so that we can find more than one error per compilation cycle.
   */
  private boolean foundError = false;

  @Inject
  BindingsProcessor(NameGenerator nameGenerator, TreeLogger logger,
      Provider<MemberCollector> collectorProvider,
      Provider<CallGwtDotCreateBinding> callGwtDotCreateBindingProvider,
      Provider<CallConstructorBinding> callConstructorBinding,
      KeyUtil keyUtil,
      Provider<BindClassBinding> bindClassBindingProvider,
      Provider<BindProviderBinding> bindProviderBindingProvider,
      Provider<ImplicitProviderBinding> implicitProviderBindingProvider,
      Provider<AsyncProviderBinding> asyncProviderBindingProvider,
      @GinjectorInterfaceType JClassType ginjectorInterface,
      LieToGuiceModule lieToGuiceModule,
      Provider<BindConstantBinding> bindConstantBindingProvider,
      Provider<RemoteServiceProxyBinding> remoteServiceProxyBindingProvider,
      Provider<ProviderMethodBinding> providerMethodBindingProvider,
      Provider<GinjectorBinding> ginjectorBindingProvider,
      Provider<FactoryBinding> factoryBindingProvider) {
    this.nameGenerator = nameGenerator;
    this.logger = logger;
    this.callGwtDotCreateBindingProvider = callGwtDotCreateBindingProvider;
    this.callConstructorBinding = callConstructorBinding;
    this.bindClassBindingProvider = bindClassBindingProvider;
    this.implicitProviderBindingProvider = implicitProviderBindingProvider;
    this.asyncProviderBindingProvider = asyncProviderBindingProvider;
    this.bindProviderBindingProvider = bindProviderBindingProvider;
    this.keyUtil = keyUtil;
    this.ginjectorInterface = ginjectorInterface;
    this.lieToGuiceModule = lieToGuiceModule;
    this.remoteServiceProxyBindingProvider = remoteServiceProxyBindingProvider;
    this.bindConstantBindingProvider = bindConstantBindingProvider;
    this.providerMethodBindingProvider = providerMethodBindingProvider;
    this.ginjectorBindingProvider = ginjectorBindingProvider;
    this.factoryBindingProvider = factoryBindingProvider;

    completeCollector = collectorProvider.get();
    completeCollector.setMethodFilter(MemberCollector.ALL_METHOD_FILTER);
  }

  public void process() throws UnableToCompleteException {
    validateMethods();
    addUnresolvedEntriesForInjectorInterface();

    List<Module> modules = createModules();

    createBindingsForModules(modules);
    createBindingsForFactories();
    createImplicitBindingsForUnresolved();
    validateModulesUsingGuice(modules);
  }

  private void createBindingsForFactories() throws UnableToCompleteException {
    for (final FactoryModule<?> factoryModule : factoryModules) {
      lieToGuiceModule.registerImplicitBinding(factoryModule.getFactoryType());

      FactoryBinding binding = factoryBindingProvider.get();
      try {
        binding.setKeyAndCollector(factoryModule.getFactoryType(), factoryModule.getBindings());
      } catch (ConfigurationException e) {
        logError("Factory " + factoryModule.getFactoryType() + " could not be created: ", e);
        continue;
      } catch (NotFoundException e) {
        logError("Factory " + factoryModule.getFactoryType() + " could not be created.", e);
        continue;
      }

      // TODO(dburrows): store appropriate contextual information for the
      // factory and use it here.
      addBinding(factoryModule.getFactoryType(), new BindingEntry(binding,
          BindingContext.forText("Bound using factory " + factoryModule.getFactoryType())));

      // All implementations that are created by the factory are also member-
      // injected. To ensure that implementations created by multiple factories
      // result only in one member inject method they are added to this central
      // list.
      memberInjectRequests.addAll(binding.getImplementations());
    }

    checkForError();
  }

  private void createImplicitBindingsForUnresolved() throws UnableToCompleteException {
    while (!unresolved.isEmpty() || !unresolvedOptional.isEmpty()) {
      // Iterate through copies because we will modify sets during iteration
      for (Key<?> key : new ArrayList<Key<?>>(unresolved)) {
        createImplicitBindingForUnresolved(key, false);
      }

      for (Key<?> key : new ArrayList<Key<?>>(unresolvedOptional)) {
        createImplicitBindingForUnresolved(key, true);
      }

      checkForError();
    }
  }

  private void createImplicitBindingForUnresolved(final Key<?> key, boolean optional) {
    Binding binding = createImplicitBinding(key, optional);

    if (binding != null) {
      logger.log(TreeLogger.TRACE, "Implicit binding for " + key + ": " + binding);
      if (binding instanceof CallGwtDotCreateBinding || binding instanceof GinjectorBinding
          || binding instanceof AsyncProviderBinding){
        // Need to lie to Guice about any implicit GWT.create bindings and
        // ginjector bindings we install that Guice would otherwise not see.
        // http://code.google.com/p/google-gin/issues/detail?id=13
        lieToGuiceModule.registerImplicitBinding(key);
      }

      // TODO(dburrows): provide a summary of why the unresolved binding was
      // created in the first place in its context.
      addBinding(key, new BindingEntry(binding,
          BindingContext.forText("Implicit binding for " + key)));
    } else if (optional) {
      unresolvedOptional.remove(key);
    }
  }

  private void checkForError() throws UnableToCompleteException {
    if (foundError) {
      throw new UnableToCompleteException();
    }
  }

  public Map<Key<?>, BindingEntry> getBindings() {
    return bindings;
  }

  public Map<Key<?>, GinScope> getScopes() {
    return scopes;
  }

  public Set<Class<?>> getStaticInjectionRequests() {
    return staticInjectionRequests;
  }

  public Set<Key<?>> getMemberInjectRequests() {
    return memberInjectRequests;
  }

  public GinScope determineScope(Key<?> key) {
    GinScope scope = getScopes().get(key);
    if (scope == null) {
      Class<?> raw = keyUtil.getRawType(key);
      if (raw.getAnnotation(Singleton.class) != null
          || raw.getAnnotation(javax.inject.Singleton.class) != null) {
        // Look for scope annotation as a fallback
        scope = GinScope.SINGLETON;
      } else if (RemoteServiceProxyBinding.isRemoteServiceProxy(keyUtil.getRawClassType(key))) {
        // Special case for remote services
        scope = GinScope.SINGLETON;
      } else {
        scope = GinScope.NO_SCOPE;
      }
    }

    logger.log(TreeLogger.TRACE, "scope for " + key + ": " + scope);
    return scope;
  }

  public boolean isBound(Key<?> key) {
    return bindings.containsKey(key);
  }

  private void validateMethods() throws UnableToCompleteException {
    for (JMethod method : completeCollector.getMethods(ginjectorInterface)) {
      if (method.getParameters().length > 1) {
        logError("Injector methods cannot have more than one parameter, "
            + " found: " + method.getReadableDeclaration());
      }

      if (method.getParameters().length == 1) {
        // Member inject method.
        if (method.getParameters()[0].getType().isClassOrInterface() == null) {
          logError("Injector method parameter types must be a class or "
              + "interface, found: " + method.getReadableDeclaration());
        }

        if (method.getReturnType() != JPrimitiveType.VOID) {
          logError("Injector methods with a parameter must have a void "
              + "return type, found: " + method.getReadableDeclaration());
        }
      } else if (method.getReturnType() == JPrimitiveType.VOID) {
        // Constructor injection.
        logError("Injector methods with no parameters cannot return void");
      }
    }

    checkForError();
  }

  private void addUnresolvedEntriesForInjectorInterface() {
    for (JMethod method : completeCollector.getMethods(ginjectorInterface)) {
      nameGenerator.markAsUsed(method.getName());
      Key<?> key = keyUtil.getKey(method);
      logger.log(TreeLogger.TRACE, "Add unresolved key from injector interface: " + key);

      // Member inject types do not need to be gin-creatable themselves but we
      // need to provide all dependencies.
      if (keyUtil.isMemberInject(method)) {
        if (!unresolved.contains(key)) {
          memberInjectRequests.add(key);
          RequiredKeys requiredKeys = keyUtil.getRequiredKeys(keyUtil.getClassType(key));
          unresolved.addAll(requiredKeys.getRequiredKeys());
          unresolvedOptional.addAll(requiredKeys.getOptionalKeys());
        }
      } else {
        unresolved.add(key);
      }
    }
  }

  private void createBindingsForModules(List<Module> modules) throws UnableToCompleteException {
    List<Element> elements = Elements.getElements(modules);
    for (Element element : elements) {
      GuiceElementVisitor visitor = new GuiceElementVisitor();
      element.acceptVisitor(visitor);

      // Capture any binding errors, any of which we treat as fatal.
      List<Message> messages = visitor.getMessages();
      if (!messages.isEmpty()) {
        for (Message message : messages) {
          // tostring has both source and message so use that
          logError(message.toString(), message.getCause());
        }
      }
    }

    checkForError();
  }

  private List<Module> createModules() {
    List<Module> modules = new ArrayList<Module>();
    populateModulesFromInjectorInterface(ginjectorInterface, modules,
        new HashSet<Class<? extends GinModule>>());
    return modules;
  }

  private void validateModulesUsingGuice(List<Module> modules) throws UnableToCompleteException {
    // Validate module consistency using Guice.
    try {
      List<Module> modulesForGuice = new ArrayList<Module>(modules.size() + 1);
      modulesForGuice.add(lieToGuiceModule);
      modulesForGuice.addAll(modules);
      Guice.createInjector(Stage.TOOL, modulesForGuice);
    } catch (Exception e) {
      logError("Errors from Guice: " + e.getMessage(), e);
      throw new UnableToCompleteException();
    }
  }

  private void populateModulesFromInjectorInterface(JClassType iface, List<Module> modules,
      Set<Class<? extends GinModule>> added) {
    GinModules gmodules = iface.getAnnotation(GinModules.class);
    if (gmodules != null) {
      for (Class<? extends GinModule> moduleClass : gmodules.value()) {
        if (added.contains(moduleClass)) {
          continue;
        }
        
        Module module = instantiateGModuleClass(moduleClass);
        if (module != null) {
          modules.add(module);
          added.add(moduleClass);
        }
      }
    }

    for (JClassType superIface : iface.getImplementedInterfaces()) {
      populateModulesFromInjectorInterface(superIface, modules, added);
    }
  }

  private Module instantiateGModuleClass(Class<? extends GinModule> moduleClassName) {
    try {
      Constructor<? extends GinModule> constructor = moduleClassName.getDeclaredConstructor();
      try {
        constructor.setAccessible(true);
        return new GinModuleAdapter(constructor.newInstance(), factoryModules);
      } finally {
        constructor.setAccessible(false);
      }
    } catch (IllegalAccessException e) {
      logError("Error creating module: " + moduleClassName, e);
    } catch (InstantiationException e) {
      logError("Error creating module: " + moduleClassName, e);
    } catch (NoSuchMethodException e) {
      logError("Error creating module: " + moduleClassName, e);
    } catch (InvocationTargetException e) {
      logError("Error creating module: " + moduleClassName, e);
    }

    return null;
  }

  private Binding createImplicitBinding(Key<?> key, boolean optional) {
    // All steps per:
    // http://code.google.com/p/google-guice/wiki/BindingResolution

    JClassType rawClassType = keyUtil.getRawClassType(key);

    // 1. Explicit binding - already finished at this point.

    // This is really an explicit binding, we add it here.
    // TODO(schmitt): Can we just add a binding to the module?
    if (rawClassType.equals(ginjectorInterface)) {
      return ginjectorBindingProvider.get();
    }

    // 2. Ask parent injector.
    // TODO(schmitt): Implement parent/child injectors.

    // 3. Ask child injector.
    // TODO(schmitt): Implement parent/child injectors.

    // 4. Provider injections.
    if (isProviderKey(key)) {
      ImplicitProviderBinding binding = implicitProviderBindingProvider.get();
      binding.setProviderKey(key);

      if (optional) {
        // We have to take special measures for optional implicit providers
        // since they are only created/injected if their provided type can be
        // bound.
        return checkOptionalBindingAvailability(binding);
      }

      return binding;

      // TODO(bstoler): Scope the provider binding like the thing being provided?
    }
    
    // 4b. AsyncProvider injections.
    if (isAsyncProviderKey(key)) {
      AsyncProviderBinding binding = asyncProviderBindingProvider.get();
      binding.setProviderKey(key);
      
      if (optional) {
        // We have to take special measures for optional implicit providers
        // since they are only created/injected if their provided type can be
        // bound.
        return checkOptionalBindingAvailability(binding);
      }
      
      return binding;
    }

    // 5. Convert constants.
    // Already covered by resolving explicit bindings.
    if (BindConstantBinding.isConstantKey(key)) {
      if (!optional) {
        logError("Binding requested for constant key " + key
            + " but no explicit binding was found.");
      }

      return null;
    }

    // 6. If the dependency has a binding annotation, give up.
    if (key.getAnnotation() != null || key.getAnnotationType() != null) {
      if (!optional) {
        logError("No implementation bound for \"" + key
            + "\" and an implicit binding cannot be created because the type is annotated.");
      }

      return null;
    }

    // 7. If the dependency is an array or enum, give up.
    // Covered by step 5 (enum) and 11 (array).

    // 8. Handle TypeLiteral injections.
    // TODO(schmitt): Implement TypeLiteral injections.

    // 9. Use resolution annotations (@ImplementedBy, @ProvidedBy)
    ImplementedBy implementedBy = rawClassType.getAnnotation(ImplementedBy.class);
    if (implementedBy != null) {
      return createImplementedByBinding(key, implementedBy, optional);
    }

    ProvidedBy providedBy = rawClassType.getAnnotation(ProvidedBy.class);
    if (providedBy != null) {
      return createProvidedByBinding(key, providedBy, optional);
    }

    // 10. If the dependency is abstract or a non-static inner class, give up.
    // Abstract classes are handled by GWT.create.
    // TODO(schmitt): Introduce check.

    // 11. Use a single @Inject or public no-arguments constructor.
    JClassType classType = keyUtil.getClassType(key);
    if (classType != null) {
      return createImplicitBindingForClass(classType, optional, key);
    } else if (!optional) {
      logError("Class not found: " + key);
    }

    return null;
  }

  private Binding createImplicitBindingForClass(JClassType classType, boolean optional,
      Key<?> key) {
    // Either call the @Inject constructor or use GWT.create
    JConstructor injectConstructor = getInjectConstructor(classType);

    if (injectConstructor != null) {
      CallConstructorBinding binding = callConstructorBinding.get();
      binding.setConstructor(injectConstructor, key);
      return binding;
    }

    if (hasAccessibleZeroArgConstructor(classType)) {
      if (RemoteServiceProxyBinding.isRemoteServiceProxy(classType)) {
        RemoteServiceProxyBinding binding = remoteServiceProxyBindingProvider.get();
        binding.setClassType(classType, key);
        return binding;
      } else {
        CallGwtDotCreateBinding binding = callGwtDotCreateBindingProvider.get();
        binding.setClassType(classType, key);
        return binding;
      }
    }

    if (!optional) {
      logError("No @Inject or default constructor found for " + classType);
    }

    return null;
  }

  /**
   * Returns true iff the passed type has a constructor with zero arguments
   * (default constructors included) and that constructor is non-private,
   * excepting constructors for private classes where the constructor may be of
   * any visibility.
   *
   * @param classType type to be checked for matching constructor
   * @return true if a matching constructor is present on the passed type
   */
  private boolean hasAccessibleZeroArgConstructor(JClassType classType) {
    if (classType.isInterface() != null) {
      return true;
    }

    // This will return one constructor on any class that doesn't have any
    // constructors specified:  The JDT compiler (internally used by GWT) adds
    // a synthetic default constructor to every class with the class's
    // visibility that gets picked up by GWT as a regular constructor.
    //
    // See also:
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3514
    // http://code.google.com/p/google-guice/wiki/Injections
    JConstructor constructor = classType.findConstructor(ZERO_ARGS);
    return constructor != null && (!constructor.isPrivate() || classType.isPrivate());
  }

  private void addBinding(Key<?> key, BindingEntry bindingEntry) {
    if (bindings.containsKey(key)) {
      BindingEntry keyEntry = bindings.get(key);
      logError("Double-bound: " + key + ". " + keyEntry.getBindingContext() + ", "
          + bindingEntry.getBindingContext());
      return;
    }

    JClassType classType = keyUtil.getRawClassType(key);
    if (classType != null && !isClassAccessibleFromGinjector(classType)) {
      logError("Can not inject an instance of an inaccessible class. Key=" + key);
      return;
    }

    bindings.put(key, bindingEntry);
    unresolved.remove(key);
    unresolvedOptional.remove(key);
    memberInjectRequests.remove(key);

    addRequiredKeys(key, bindingEntry.getBinding().getRequiredKeys());

    logger.log(TreeLogger.TRACE, "bound " + key + " to " + bindingEntry);
  }

  private void addRequiredKeys(Key<?> key, RequiredKeys requiredKeys) {
    // Resolve optional keys.
    // Clone the returned set so we can safely mutate it
    Set<Key<?>> optionalKeys = new HashSet<Key<?>>(requiredKeys.getOptionalKeys());
    optionalKeys.removeAll(bindings.keySet());
    if (!optionalKeys.isEmpty()) {
      logger.log(TreeLogger.TRACE, "Add optional unresolved as dep from binding to "
          + key + ": " + optionalKeys);
      unresolvedOptional.addAll(optionalKeys);
    }

    // Resolve required keys.
    // Clone the returned set so we can safely mutate it
    Set<Key<?>> nowUnresolved = new HashSet<Key<?>>(requiredKeys.getRequiredKeys());
    nowUnresolved.removeAll(bindings.keySet());
    if (!nowUnresolved.isEmpty()) {
      logger.log(TreeLogger.TRACE, "Add unresolved as dep from binding to "
          + key + ": " + nowUnresolved);
      unresolved.addAll(nowUnresolved);
    }
  }

  private <T extends Binding> T checkOptionalBindingAvailability(T binding) {
    RequiredKeys requiredKeys = binding.getRequiredKeys();

    assert(requiredKeys.getOptionalKeys().isEmpty());

    // Find out whether all requirements of this provider can be satisfied.
    Set<Key<?>> unresolved = new HashSet<Key<?>>(requiredKeys.getRequiredKeys());
    unresolved.removeAll(bindings.keySet());
    for (Key<?> requiredKey : unresolved) {

      // Note: This call doesn't cause a binding to be registered.
      if (createImplicitBinding(requiredKey, true) == null) {

        // A dependency cannot be constructed, this binding is not available.
        return null;
      }
    }

    return binding;
  }

  private BindClassBinding createImplementedByBinding(Key<?> key, ImplementedBy implementedBy,
      boolean optional) {
    Class<?> rawType = key.getTypeLiteral().getRawType();
    Class<?> implementationType = implementedBy.value();

    if (implementationType == rawType) {
      logError("@ImplementedBy points to the same class it annotates: " + rawType);
      return null;
    }

    if (!rawType.isAssignableFrom(implementationType)) {
      logError(implementationType + " doesn't extend " + rawType
          + " (while resolving @ImplementedBy)");
      return null;
    }

    BindClassBinding implementedByBinding = bindClassBindingProvider.get();
    implementedByBinding.setBoundClassKey(Key.get(implementationType));

    if (optional) {
      return checkOptionalBindingAvailability(implementedByBinding);
    }

    return implementedByBinding;
  }

  private BindProviderBinding createProvidedByBinding(Key<?> key, ProvidedBy providedBy,
      boolean optional) {
    Class<?> rawType = key.getTypeLiteral().getRawType();
    Class<? extends Provider<?>> providerType = providedBy.value();

    if (providerType == rawType) {
      logError("@ProvidedBy points to the same class it annotates: " + rawType);
      return null;
    }

    BindProviderBinding implementedByBinding = bindProviderBindingProvider.get();
    implementedByBinding.setProviderKey(Key.get(providerType));

    if (optional) {
      return checkOptionalBindingAvailability(implementedByBinding);
    }

    return implementedByBinding;
  }

  private boolean isProviderKey(Key<?> key) {
    Type keyType = key.getTypeLiteral().getType();
    return keyType instanceof ParameterizedType &&
        (((ParameterizedType) keyType).getRawType() == Provider.class
            || ((ParameterizedType) keyType).getRawType() == com.google.inject.Provider.class);
  }

  private boolean isAsyncProviderKey(Key<?> key) {
      Type keyType = key.getTypeLiteral().getType();
      return keyType instanceof ParameterizedType &&
          ((ParameterizedType) keyType).getRawType() == AsyncProvider.class;
  }
  
  private boolean isClassAccessibleFromGinjector(JClassType classType) {
    if (classType.isPublic()) {
      return true;
    }

    // Null class package could be if it's not an object type
    JPackage classPackage = classType.getPackage();
    if (classPackage == null) {
      return false;
    }

    JPackage ginjectorPackage = ginjectorInterface.getPackage();
    return (ginjectorPackage.isDefault() && classPackage.isDefault())
        || classPackage.getName().equals(ginjectorPackage.getName());
  }

  private void logError(String message) {
    logError(message, null);
  }

  private void logError(String message, Throwable t) {
    logger.log(TreeLogger.ERROR, message, t);
    foundError = true;
  }

  private JConstructor getInjectConstructor(JClassType classType) {
    JConstructor[] constructors = classType.getConstructors();

    JConstructor injectConstructor = null;
    for (JConstructor constructor : constructors) {
      if (constructor.getAnnotation(Inject.class) != null
          || constructor.getAnnotation(javax.inject.Inject.class) != null) {
        if (injectConstructor == null) {
          injectConstructor = constructor;
        } else {
          logError("More than one @Inject constructor found for "
              + classType + "; " + injectConstructor + ", " + constructor);
          return null;
        }
      }
    }

    return injectConstructor;
  }

  private class GuiceElementVisitor extends DefaultElementVisitor<Void> {
    private final List<Message> messages = new ArrayList<Message>();

    @Override
    public <T> Void visit(com.google.inject.Binding<T> command) {
      GuiceBindingVisitor<T> bindingVisitor = new GuiceBindingVisitor<T>(command.getKey(),
          messages);
      command.acceptTargetVisitor(bindingVisitor);
      command.acceptScopingVisitor(bindingVisitor);
      return null;
    }

    @Override
    public Void visit(Message message) {
      messages.add(message);
      return null;
    }

    @Override
    public <T> Void visit(ProviderLookup<T> providerLookup) {
      // Ignore provider lookups for now
      // TODO(bstoler): I guess we should error if you try to lookup a provider
      // that is not bound?
      return null;
    }

    @Override
    protected Void visitOther(Element element) {
      visit(new Message(element.getSource(),
          "Ignoring unsupported Module element: " + element));
      return null;
    }

    @Override
    public Void visit(StaticInjectionRequest staticInjectionRequest) {
      addStaticInjectionRequest(staticInjectionRequest);
      return null;
    }

    public List<Message> getMessages() {
      return messages;
    }

    private void addStaticInjectionRequest(StaticInjectionRequest staticInjectionRequest) {
      Class<?> type = staticInjectionRequest.getType();
      staticInjectionRequests.add(type);

      // Calculate required bindings and add to unresolved.
      Set<Key<?>> unresolved = new HashSet<Key<?>>();
      Set<Key<?>> unresolvedOptional = new HashSet<Key<?>>();
      for (InjectionPoint injectionPoint : InjectionPoint.forStaticMethodsAndFields(type)) {
        Member member = injectionPoint.getMember();
        if (member instanceof Method) {
          JMethod method = null;
          try {
            method = keyUtil.javaToGwtMethod((Method) member);
          } catch (NotFoundException e) {
            messages.add(
                new Message(new ArrayList<Object>(), "Could not resolve GWT method: " + member, e));
            return;
          }
          RequiredKeys keys = keyUtil.getRequiredKeys(method);
          unresolved.addAll(keys.getRequiredKeys());
          unresolvedOptional.addAll(keys.getOptionalKeys());
        } else if (member instanceof Field) {
          JField field = keyUtil.javaToGwtField((Field) member);
          Key<?> key = keyUtil.getKey(field);
          if (keyUtil.isOptional(field)) {
            unresolvedOptional.add(key);
          } else {
            unresolved.add(key);
          }
        }
      }
      addRequiredKeys(keyUtil.getKey(type), new RequiredKeys(unresolved, unresolvedOptional));
    }
  }

  private class GuiceBindingVisitor<T> extends DefaultBindingTargetVisitor<T, Void>
      implements BindingScopingVisitor<Void> {
    private final Key<T> targetKey;
    private final List<Message> messages;

    public GuiceBindingVisitor(Key<T> targetKey, List<Message> messages) {
      this.targetKey = targetKey;
      this.messages = messages;
    }

    @Override
    public Void visit(ProviderKeyBinding<? extends T> providerKeyBinding) {
      BindProviderBinding binding = bindProviderBindingProvider.get();
      binding.setProviderKey(providerKeyBinding.getProviderKey());
      addBinding(targetKey,
          new BindingEntry(binding, BindingContext.forElement(providerKeyBinding)));

      return null;
    }

    @Override
    public Void visit(ProviderInstanceBinding<? extends T> providerInstanceBinding) {
      // Detect provider methods and handle them
      // TODO(bstoler): Update this when the SPI explicitly has a case for provider methods
      Provider<? extends T> provider = providerInstanceBinding.getProviderInstance();
      if (provider instanceof ProviderMethod) {
        ProviderMethodBinding binding = providerMethodBindingProvider.get();
        try {
          binding.setProviderMethod((ProviderMethod) provider);
          addBinding(targetKey,
              new BindingEntry(binding, BindingContext.forElement(providerInstanceBinding)));
        } catch (UnableToCompleteException e) {
          messages.add(new Message(providerInstanceBinding.getSource(),
              "Error processing provider method"));
        }
        return null;
      }

      if (provider instanceof GwtDotCreateProvider) {
        addImplicitBinding(providerInstanceBinding);
        return null;
      }

      // OTt, use the normal default handler (and error)
      return super.visit(providerInstanceBinding);
    }

    @Override
    public Void visit(LinkedKeyBinding<? extends T> linkedKeyBinding) {
      BindClassBinding binding = bindClassBindingProvider.get();
      binding.setBoundClassKey(linkedKeyBinding.getLinkedKey());
      addBinding(targetKey,
          new BindingEntry(binding, BindingContext.forElement(linkedKeyBinding)));
      return null;
    }

    @Override
    public Void visit(InstanceBinding<? extends T> instanceBinding) {
      T instance = instanceBinding.getInstance();
      if (BindConstantBinding.isConstantKey(targetKey)) {
        BindConstantBinding binding = bindConstantBindingProvider.get();
        binding.setKeyAndInstance(targetKey, instance);
        addBinding(targetKey,
            new BindingEntry(binding, BindingContext.forElement(instanceBinding)));
      } else {
        messages.add(new Message(instanceBinding.getSource(),
            "Instance binding not supported; key=" + targetKey + " inst=" + instance));
      }

      return null;
    }

    @Override
    public Void visit(UntargettedBinding<? extends T> untargettedBinding) {
      addImplicitBinding(untargettedBinding);

      return null;
    }

    private void addImplicitBinding(Element sourceElement) {
      // Register a Gin binding for the default-case binding that
      // Guice saw. We need to register this to avoid later adding
      // this key to the Guice-lies module, which would make it
      // double bound. If binding was null, an error was already logged.
      Binding binding = createImplicitBinding(targetKey, false);
      if (binding != null) {
        logger.log(TreeLogger.TRACE, "Implicit binding for " + targetKey + ": " + binding);
        addBinding(targetKey,
            new BindingEntry(binding, BindingContext.forElement(sourceElement)));
      }
    }

    @Override
    protected Void visitOther(com.google.inject.Binding<? extends T> binding) {
      messages.add(new Message(binding.getSource(),
          "Unsupported binding provided for key: " + targetKey + ": " + binding));
      return null;
    }

    public Void visitEagerSingleton() {
      scopes.put(targetKey, GinScope.EAGER_SINGLETON);
      return null;
    }

    // TODO(schmitt): We don't support this right now in any case, but it's
    // strange to be using the Guice Scope instead of javax.inject.Scope
    public Void visitScope(Scope scope) {
      messages.add(new Message("Explicit scope unsupported: key=" + targetKey
          + " scope=" + scope));
      return null;
    }

    public Void visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
      if (scopeAnnotation == Singleton.class || scopeAnnotation == javax.inject.Singleton.class) {
        scopes.put(targetKey, GinScope.SINGLETON);
      } else {
        messages.add(new Message("Unsupported scope annoation: key=" + targetKey
            + " scope=" + scopeAnnotation));
      }
      return null;
    }

    public Void visitNoScoping() {
      scopes.put(targetKey, GinScope.NO_SCOPE);
      return null;
    }
  }
}
