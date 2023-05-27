import static com.github.arvinzojaji.proxy.MethodBuilder.methodBuilder;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;

import com.github.arvinzojaji.proxy.Proxy;
import com.github.arvinzojaji.proxy.Proxy.ProxyContext;
import com.github.arvinzojaji.proxy.Proxy.ProxyFactory;
import com.github.arvinzojaji.proxy.Proxy.ProxyHandler;

public class Main {
  private static int COUNTER;
  private static MyIntUnaryOp op;
  
  public interface MyIntUnaryOp {
    public int call(int value);
  }
  
  private static void foo() {
    COUNTER += op.call(3);
  }
  
  public static void main(String[] args) throws Throwable {
    ProxyFactory<MyIntUnaryOp> factory =
      Proxy.createAnonymousProxyFactory(MyIntUnaryOp.class, 
        new ProxyHandler.Default() {
          @Override
          public CallSite bootstrap(ProxyContext context) throws Throwable {
            System.out.println("require implementation of " + context.method());
            
            MethodHandle target = methodBuilder(context.type())
                .dropFirst()  // drop the proxy object
                .callIdentity();   // then return the value taken as argument
            return new ConstantCallSite(target);
          }
        });
    MyIntUnaryOp op = factory.create();
    System.out.println(op.call(3));
    
    Main.op = op;
    for(int i = 0; i < 1_000_000; i++) {
      foo();
    }
    System.out.println(COUNTER);
  }
}
