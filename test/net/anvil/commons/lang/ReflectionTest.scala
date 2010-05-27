package net.anvil.commons.lang


import org.junit._

import java.lang.reflect.{Constructor, Method}
import java.util.Arrays

import org.junit.runner._
import org.junit.Assert._
import org.easymock.classextension.EasyMock.createControl
import org.easymock.classextension.ConstructorArgs
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
  def findDeclared_ShouldFindAmbigeousOverloadedConstructor_ByNumberOfArguments() {
     val expectedArgumentTypes = List(classOf[SomeClass], classOf[SomeOtherClass])
     val rs = Reflection.findDeclared(constructorIn[MyClass](WithNumberOfArguments(2)))
     
     assertEquals(expectedArgumentTypes, List(rs.getParameterTypes: _*))
  }
    
  @Test
  def findDeclared_ShouldFindAmbigeousOverloadedConstructor_ByArgumentTypes() {
     val expectedArgumentTypes = List(classOf[Boolean])
     val rs = Reflection.findDeclared(constructorIn[MyClass](WithArgumentTypes(classOf[Boolean])))
     
     assertEquals(expectedArgumentTypes, List(rs.getParameterTypes: _*))
  }

  @Test 
  def findDeclared_ShouldFindNonOverloadedMethod_ByName() {
    assertEquals("method1", Reflection.findDeclared(methodIn[MyClass]("method1")).getName)
  }
  
  @Test 
  def findDeclared_ShouldThrowOnFindOverloadedMethod_ByName() {
    assertThrows[IllegalArgumentException] { Reflection.findDeclared(methodIn[MyClass]("method2")) }
  }

  @Test 
  def findDeclared_ShouldFindNonAmbigeousOverloadedMethod_ByNameAndNumberOfArguments() {
    val rs = Reflection.findDeclared(methodIn[MyClass]("method2", WithNumberOfArguments(1)))
    
    assertEquals("method2", rs.getName)
    assertEquals(1, rs.getParameterTypes.size)
  }
  
  @Test 
  def findDeclared_ShouldThrowOnFindAmbigeousOverloadedMethod_ByNameAndNumberOfArguments() {
    assertThrows[IllegalArgumentException] { 
      Reflection.findDeclared(methodIn[MyClass]("method3", WithNumberOfArguments(2))) 
    }
  }

  @Test 
  def findDeclared_ShouldFindAmbigeousOverloadedMethod_ByNameAndArgumentTypes() {
    val expectedArgumentTypes = List(classOf[String], classOf[Boolean])
    val rs = Reflection.findDeclared(methodIn[MyClass]("method3", WithArgumentTypes(expectedArgumentTypes)))
    
    assertEquals("method3", rs.getName)
    assertEquals(expectedArgumentTypes, List(rs.getParameterTypes: _*))
  }
}
