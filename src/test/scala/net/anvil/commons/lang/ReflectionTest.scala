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
package net.anvil.commons.lang

import org.junit._

import java.lang.reflect.{Constructor, Method}
import java.util.Arrays

import org.junit.runner._
import org.junit.Assert._
import org.easymock.EasyMock.createControl
import org.easymock.ConstructorArgs
import org.scalatest.junit._

import net.anvil.commons.lang._
import net.anvil.commons.test.Assert._
import net.anvil.commons.lang.ConstructorDescriptor._
import net.anvil.commons.lang.MethodDescriptor._

/**
 * Test for the `Reflection` object.
 * 
 * @see net.anvil.commons.lang.Reflection
 */
class ReflectionTest extends JUnitSuite {

  @Test
  def findDeclaredConstructorIn_ShouldFindAmbigeousOverloadedConstructor_ByNumberOfArguments() {
     val expectedArgumentTypes = List(classOf[SomeClass], classOf[SomeOtherClass])
     val rs = Reflection.findDeclaredConstructorIn[MyClass](WithNumberOfArguments(2))
     
     assertEquals(expectedArgumentTypes, List(rs.getParameterTypes: _*))
  }
    
  @Test
  def findDeclaredConstructorIn_ShouldFindAmbigeousOverloadedConstructor_ByArgumentTypes() {
     val rs = Reflection.findDeclaredConstructorIn[MyClass](WithArgumentTypes(classOf[Boolean]))
     
     assertEquals(List(classOf[Boolean]), List(rs.getParameterTypes: _*))
  }

  @Test
  def findAllDeclaredConstructorsIn_ShouldFindMultipleConstructors() {
     val rs = Reflection.findAllDeclaredConstructorsIn[MyClass](
       WithNumberOfArguments(0), 
       WithArgumentTypes(classOf[Boolean]))
     
     assertEquals(List[Class[_]](), List(rs(0).getParameterTypes: _*))
     assertEquals(List(classOf[Boolean]), List(rs(1).getParameterTypes: _*))
  }

  @Test 
  def findDeclaredMethodIn_ShouldFindNonOverloadedMethod_ByName() {
    assertEquals("method1", Reflection.findDeclaredMethodIn[MyClass]("method1").getName)
  }
  
  @Test 
  def findDeclaredMethodIn_ShouldThrowOnFindOverloadedMethod_ByName() {
    assertThrows[IllegalArgumentException] { Reflection.findDeclaredMethodIn[MyClass]("method2") }
  }

  @Test 
  def findDeclaredMethodIn_ShouldFindNonAmbigeousOverloadedMethod_ByNameAndNumberOfArguments() {
    val rs = Reflection.findDeclaredMethodIn[MyClass]("method2", WithNumberOfArguments(1))
    
    assertEquals("method2", rs.getName)
    assertEquals(1, rs.getParameterTypes.size)
  }
  
  @Test 
  def findDeclaredMethodIn_ShouldThrowOnFindAmbigeousOverloadedMethod_ByNameAndNumberOfArguments() {
    assertThrows[IllegalArgumentException] { 
      Reflection.findDeclaredMethodIn[MyClass]("method3", WithNumberOfArguments(2)) 
    }
  }

  @Test 
  def findDeclaredMethodIn_ShouldFindAmbigeousOverloadedMethod_ByNameAndArgumentTypes() {
    val expectedArgumentTypes = List(classOf[String], classOf[Boolean])
    val rs = Reflection.findDeclaredMethodIn[MyClass]("method3", WithArgumentTypes(expectedArgumentTypes))
    
    assertEquals("method3", rs.getName)
    assertEquals(expectedArgumentTypes, List(rs.getParameterTypes: _*))
  }
  
  @Test 
  def findAllDeclaredMethodsIn_ShouldFindMultipleMethods() {
    val rs = Reflection.findAllDeclaredMethodsIn[MyClass](
      "method1", 
      ("method2", WithNumberOfArguments(1)), 
      ("method3", WithArgumentTypes(classOf[String], classOf[Boolean])))
    
    assertEquals(("method1", List(classOf[String])), (rs(0).getName, List(rs(0).getParameterTypes: _*)))
    assertEquals(("method2", List(classOf[String])), (rs(1).getName, List(rs(1).getParameterTypes: _*)))
    assertEquals(("method3", List(classOf[String], classOf[Boolean])), (rs(2).getName, List(rs(2).getParameterTypes: _*)))
  }

}
