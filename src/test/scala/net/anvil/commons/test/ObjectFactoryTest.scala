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
import org.easymock.ConstructorArgs
import org.scalatest.junit._
import net.anvil.commons.lang._

class ObjectFactoryTest extends JUnitSuite {
  val mocks = MocksControl.create
  val sut = ObjectFactory using mocks
  
  @Test
  def create_ShouldCreateObjectUsingDefaultConstructor() {
    val yetSomeOtherClass = sut.create[YetSomeOtherClass]
    assertTrue(yetSomeOtherClass.isInstanceOf[YetSomeOtherClass])
  }
  
  @Test
  def create_ShouldCreateObjectUsingConstructorWithMostArguments() {
    val arg0 = new SomeClass("test1")  
    val arg1 = new SomeOtherClass("test2")
    val myClass = sut.create[MyClass](arg0, arg1)

    assertTrue(myClass.isInstanceOf[MyClass])
    assertEquals(arg0, myClass.arg0)
    assertEquals(arg1, myClass.arg1)
  }

}