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