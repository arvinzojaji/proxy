import static com.github.arvinzojaji.proxy.MethodBuilder.methodBuilder;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.github.arvinzojaji.proxy.Proxy;
import com.github.arvinzojaji.proxy.Proxy.ProxyContext;
import com.github.arvinzojaji.proxy.Proxy.ProxyFactory;
import com.github.arvinzojaji.proxy.Proxy.ProxyHandler;

public class Throw {
  public static void main(String[] args) throws Throwable {
    ProxyFactory<Runnable> factory = Proxy.createAnonymousProxyFactory(
      Runnable.class,                        // the proxy type
      new Class<?>[] { Runnable.class },     // type of each field inside the proxy
      new ProxyHandler.Default() {
        @Override
        public boolean override(Method method) {
          return Modifier.isAbstract(method.getModifiers());
        }

        @Override
        public CallSite bootstrap(ProxyContext context) throws Throwable {
          Method method = context.method();
          MethodHandle target = methodBuilder(context.type())        // configure the builder
              .dropFirst()                                  // remove the proxy object
              .unreflect(MethodHandles.publicLookup(), method);       // call the method
          return new ConstantCallSite(target);
        }
      });
    
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        throw null;
      }
    };
    
    Runnable proxy = factory.create(runnable);
    //Runnable proxy = factory.create((Runnable)null);
    proxy.run();
  }
}
