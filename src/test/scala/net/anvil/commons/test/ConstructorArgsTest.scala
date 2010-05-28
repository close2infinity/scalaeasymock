/*
 * Copyright (C) 2010 the-anvil.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anvil.commons.test

import java.lang.reflect.Method
import org.junit._
import org.junit.runner._
import org.junit.Assert._
import org.easymock.classextension.ConstructorArgs
import org.scalatest.junit._
import net.anvil.commons.lang._

class ConstructorArgsFactoryTest extends JUnitSuite {
  val mocks = MocksControl.create

  @Test
  def createFor_ShouldCreateArgsForConstructorWithMostArguments() {
    val specifiedArgument = new SomeClass("myFirstArgument")
    val rs = (ConstructorArgsFactory using mocks).tryCreateFor[MyClass](specifiedArgument)

    assertTrue(rs.isDefined)
    
    for (constructorArgs <- rs) {
      assertEquals(2, constructorArgs.getConstructor.getParameterTypes.size)
      assertEquals(specifiedArgument, constructorArgs.getInitArgs()(0))
    }
  }
}