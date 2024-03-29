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

import org.junit._
import java.lang.reflect.Method

import org.junit.runner._
import org.junit.Assert._
import org.easymock.EasyMock.createControl
import org.easymock.ConstructorArgs
import org.scalatest.junit._
import net.anvil.commons.lang._
import net.anvil.commons.lang.ConstructorDescriptor._
import net.anvil.commons.lang.MethodDescriptor._
import net.anvil.commons.test.Assert._

class MocksControlTest extends JUnitSuite {
  val mocks = MocksControl.create
  val constructorArgsFactory = ConstructorArgsFactory using mocks
  
  @Test 
  def mock_ShouldCreateMockOfSpecifiedType() {
    val rs = mocks.mock[MyClass]
    assertTrue(rs.isInstanceOf[MyClass])
  }
  
  @Test 
  def mock_ShouldCreateNamedMockOfSpecifiedType() {
    val rs = mocks.mock[MyClass]("myMockName")
    assertTrue(rs.isInstanceOf[MyClass])
  }

  @Test 
  def mock_ShouldCreateMockOfSpecifiedTypeWithConstructorArgs() {
    val someClass = new SomeClass("someClass")
    val someOtherClass = new SomeOtherClass("someOtherClass")
    val rs = mocks.mock[MyClass](
      "test",
      constructorArgsFactory.createFor(constructorIn[MyClass](WithNumberOfArguments(2)), someClass, someOtherClass))
    
    assertTrue(rs.isInstanceOf[MyClass])
    assertEquals(someClass, rs.arg0)
    assertEquals(someOtherClass, rs.arg1)
  }

  @Test 
  def mockIn_ShouldCreateMockForMethodWithMethodNameDescriptor() {
    
    // --- Call SUT:  
    val rs = mocks.mockIn[MyClass](methodIn[MyClass]("method1"))
    mocks.replay

    // --- Verify: 
    assertThrows[AssertionError] { rs.method1("test") }
    rs.method2("test")
    rs.method2("test", "test")
    rs.method3("test", "test")
    rs.method3("test", true)
  }
  
  @Test 
  def mockIn_ShouldCreateMockForMethodWithArgumentCountDescriptor() {

    // --- Call SUT:  
    val rs = mocks.mockIn[MyClass](("method2", WithNumberOfArguments(2)))
    mocks.replay
    
    // --- Verify: 
    rs.method1("test")
    rs.method2("test")
    assertThrows[AssertionError] { rs.method2("test", "test") }
    rs.method3("test", "test")
    rs.method3("test", true)
  }

  @Test 
  def mock_ShouldCreateMockForMultipleMethodsWithArgumentCountAndTypesDescriptor() {

    // --- Call SUT:  
    val rs = mocks.mockIn[MyClass](
      ("method2", WithNumberOfArguments(1)), 
      ("method3", WithArgumentTypes(classOf[String], classOf[Boolean])))
    mocks.replay

    // --- Verify: 
    rs.method1("test")
    assertThrows[AssertionError] { rs.method2("test") }
    rs.method2("test", "test")    
    rs.method3("test", "test")
    assertThrows[AssertionError] { rs.method3("test", true) }
  }
}
