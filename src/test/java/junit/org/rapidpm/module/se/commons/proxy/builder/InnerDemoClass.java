package junit.org.rapidpm.module.se.commons.proxy.builder;

/**
 * Created by sven on 28.04.15.
 */
public class InnerDemoClass implements InnerDemoInterface {

  public InnerDemoClass() {
    System.out.println("InnerDemoClass = init");
  }

  @Override
  public String doWork() {
    return "InnerDemoClass.doWork()";
  }
}
