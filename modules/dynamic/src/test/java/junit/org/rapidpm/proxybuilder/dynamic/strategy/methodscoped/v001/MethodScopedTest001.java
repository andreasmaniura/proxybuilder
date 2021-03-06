/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package junit.org.rapidpm.proxybuilder.dynamic.strategy.methodscoped.v001;

import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.type.dymamic.virtual.CreationStrategy;

public class MethodScopedTest001 {


  static int constructorCounter = 0;


  @Test
  public void test001() throws Exception {

    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, ServiceImpl.class, CreationStrategy.METHOD_SCOPED)
        .build();

    service.doWork("");
    Assert.assertEquals(1, constructorCounter);

    service.doWork("");
    Assert.assertEquals(2, constructorCounter);

    service.doWork("");
    Assert.assertEquals(3, constructorCounter);
  }


  public static interface Service {
    String doWork(String txt);
  }

  public static class ServiceImpl implements Service {

    public ServiceImpl() {
      constructorCounter += 1;
    }

    @Override
    public String doWork(final String txt) {
      return txt + " - impl";
    }
  }
}
