/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.rapidpm.module.se.commons.reflections.scanners;

import com.google.common.base.Joiner;
import javassist.*;
import javassist.bytecode.MethodInfo;
import javassist.expr.*;
import org.rapidpm.module.se.commons.reflections.ReflectionsException;
import org.rapidpm.module.se.commons.reflections.util.ClasspathHelper;

/**
 * scans methods/constructors/fields usage
 * <p><i> depends on {@link org.rapidpm.module.se.commons.reflections.adapters.JavassistAdapter} configured </i>
 */
@SuppressWarnings("unchecked")
public class MemberUsageScanner extends AbstractScanner {
  private ClassPool classPool;

  @Override
  public void scan(Object cls) {
    try {
      CtClass ctClass = getClassPool().get(getMetadataAdapter().getClassName(cls));
      for (CtBehavior member : ctClass.getDeclaredConstructors()) {
        scanMember(member);
      }
      for (CtBehavior member : ctClass.getDeclaredMethods()) {
        scanMember(member);
      }
      ctClass.detach();
    } catch (Exception e) {
      throw new ReflectionsException("Could not scan method usage for " + getMetadataAdapter().getClassName(cls), e);
    }
  }

  private ClassPool getClassPool() {
    if (classPool == null) {
      synchronized (this) {
        classPool = new ClassPool();
        ClassLoader[] classLoaders = getConfiguration().getClassLoaders();
        if (classLoaders == null) {
          classLoaders = ClasspathHelper.classLoaders();
        }
        for (ClassLoader classLoader : classLoaders) {
          classPool.appendClassPath(new LoaderClassPath(classLoader));
        }
      }
    }
    return classPool;
  }

  void scanMember(CtBehavior member) throws CannotCompileException {
    //key contains this$/val$ means local field/parameter closure
    final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() +
        "(" + parameterNames(member.getMethodInfo()) + ")"; //+ " #" + member.getMethodInfo().getLineNumber(0)
    member.instrument(new ExprEditor() {
      @Override
      public void edit(NewExpr e) throws CannotCompileException {
        try {
          put(e.getConstructor().getDeclaringClass().getName() + "." + "<init>" +
              "(" + parameterNames(e.getConstructor().getMethodInfo()) + ")", e.getLineNumber(), key);
        } catch (NotFoundException e1) {
          throw new ReflectionsException("Could not find new instance usage in " + key, e1);
        }
      }

      @Override
      public void edit(MethodCall m) throws CannotCompileException {
        try {
          put(m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName() +
              "(" + parameterNames(m.getMethod().getMethodInfo()) + ")", m.getLineNumber(), key);
        } catch (NotFoundException e) {
          throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, e);
        }
      }

      @Override
      public void edit(ConstructorCall c) throws CannotCompileException {
        try {
          put(c.getConstructor().getDeclaringClass().getName() + "." + "<init>" +
              "(" + parameterNames(c.getConstructor().getMethodInfo()) + ")", c.getLineNumber(), key);
        } catch (NotFoundException e) {
          throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, e);
        }
      }

      @Override
      public void edit(FieldAccess f) throws CannotCompileException {
        try {
          put(f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), f.getLineNumber(), key);
        } catch (NotFoundException e) {
          throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, e);
        }
      }
    });
  }

  String parameterNames(MethodInfo info) {
    return Joiner.on(", ").join(getMetadataAdapter().getParameterNames(info));
  }

  private void put(String key, int lineNumber, String value) {
    if (acceptResult(key)) {
      getStore().put(key, value + " #" + lineNumber);
    }
  }
}
