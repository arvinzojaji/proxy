package com.github.arvinzojaji.proxy;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.github.arvinzojaji.proxy.Proxy;
import com.github.arvinzojaji.proxy.Proxy.ProxyContext;
import com.github.arvinzojaji.proxy.Proxy.ProxyHandler;

public class RetroRT {
  static class LambdaProxyHandler implements ProxyHandler {
    private final MethodHandle target;
    private final Class<?>[] capturedTypes;
    
    LambdaProxyHandler(MethodHandle target, Class<?>[] capturedTypes) {
      this.target = target;
      this.capturedTypes = capturedTypes;
    }

    @Override
    public boolean override(Method method) {
      return Modifier.isAbstract(method.getModifiers());
    }
    
    @Override
    public boolean isMutable(int fieldIndex, Class<?> fieldType) {
      return false;
    }
    
    @Override
    public CallSite bootstrap(ProxyContext context) throws Throwable {
      // we can not use MethodBuilder here, it will introduce a cycle when retro-weaving
      Method method = context.method();
      MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
      MethodHandle endPoint = target;
      if (!methodType.equals(target.type())) {
        endPoint = endPoint.asType(methodType.insertParameterTypes(0, capturedTypes));
      }
      return new ConstantCallSite(MethodHandles.dropArguments(endPoint, 0, Object.class));
    }
  }
  
  public static CallSite metafactory(Lookup lookup, String name, MethodType type,
                                     MethodType sig, MethodHandle impl, MethodType reifiedSig) throws Throwable {
    Class<?>[] capturedTypes = type.parameterArray();
    MethodHandle target = impl.asType(reifiedSig.insertParameterTypes(0, capturedTypes)); // apply generic casts
    MethodHandle mh = Proxy.createAnonymousProxyFactory(lookup, type, new LambdaProxyHandler(target, capturedTypes));
    if (type.parameterCount() == 0) { // no capture
      return new ConstantCallSite(MethodHandles.constant(type.returnType(), mh.invoke()));
    }
    return new ConstantCallSite(mh);
  }
}
