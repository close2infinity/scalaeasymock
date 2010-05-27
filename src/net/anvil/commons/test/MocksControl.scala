package net.anvil.commons.test

import collection.mutable.ArrayBuffer
import org.easymock.classextension.{ConstructorArgs, IMocksControl}
import org.easymock.classextension.EasyMock.createControl
import net.anvil.commons.lang._

/**
 * A Scala wrapper around the `IMocksControl` implementor from EasyMock class 
 * extensions.
 *
 * @see org.easymock.classextension.IMocksControl
 */
class MocksControl(val decorated: IMocksControl) {
  import MocksControl._
  
  private val NoConstructorArgs = null
    
  /** 
   * @return a mock of the specified type with a default name.
   */
  def mock[T](implicit manifest: Manifest[T]): T = mock[T]()
  
  /** 
   * @return a mock of the specified type with optionally specified name and 
   *   constructor arguments.
   */
  def mock[T]
    (name: String = nameFrom, constructorArgs: ConstructorArgs = NoConstructorArgs)
    (implicit manifest: Manifest[T]): T =
  {
    if (constructorArgs != NoConstructorArgs) 
      decorated.createMock(name, manifest.erasure, constructorArgs).asInstanceOf[T]
    else MocksControlHelper.createMock(decorated, name, manifest.erasure).asInstanceOf[T]
  }

  /** 
   * @return a partial mock of the specified type with a default name that is
   *   instantiated using the type's default constructor.
   */
  def mockIn[T]
    (firstMockedMethodDescriptor: MethodDescriptor[T], otherMockedMethodDescriptors: MethodDescriptor[T]*)
    (implicit manifest: Manifest[T]): T = 
      mockIn[T]()(firstMockedMethodDescriptor, otherMockedMethodDescriptors: _*)
  
  /**
   * @param maybeName an optional name for the mock. If this is empty, a 
   *   default name will be created from the class name.
   * @param maybeConstructorArgs optional constructor arguments. If this is 
   *   not empty, the specified constructor will be called to instantiate the
   *   mock.
   *   
   * @param mockedMethodDescriptors descriptors for the methods to mock (to 
   *   create a partial mock). If no method is specified, a full mock is 
   *   created.
   * @return a mock of the specified type.
   * @see net.anvil.commons.lang.Reflection#findDeclaredMethod(MethodDescriptor)
   */
  def mockIn[T]
    (name: String = nameFrom, constructorArgs: ConstructorArgs = NoConstructorArgs)
    (firstMockedMethodDescriptor: MethodDescriptor[T], otherMockedMethodDescriptors: MethodDescriptor[T]*)
    (implicit manifest: Manifest[T]): T = 
  {
    val mockedMethods = 
      Reflection.findAllDeclared[T](firstMockedMethodDescriptor, otherMockedMethodDescriptors: _*)
        
    if (constructorArgs != NoConstructorArgs) 
      decorated.createMock(name, manifest.erasure, constructorArgs, mockedMethods: _*).asInstanceOf[T]
    else decorated.createMock(name, manifest.erasure, mockedMethods: _*).asInstanceOf[T]
  }

  /** 
   * Activate/de-activate check that mocks from this control are used 
   * exclusively by a single thread.
   */
  def checkIsUsedByOneThread(shouldBeUsedInOneThread: Boolean) = decorated.checkIsUsedInOneThread(shouldBeUsedInOneThread)

  /**
   * Switches order checking on and off.
   */
  def checkOrder(shouldCheckOrder: Boolean) = decorated.checkOrder(shouldCheckOrder)
  
  /**
   * Makes the mock thread safe.
   */
  def makeThreadSafe(threadSafe: Boolean) = decorated.makeThreadSafe(threadSafe)
          
  /**
   * Switches the control from record mode to replay mode.
   */
  def replay = decorated.replay

  /** 
   * Removes all expectations for the mock objects of this control.
   */
  def reset = decorated.reset

  /** 
   * Removes all expectations for the mock objects of this control and turn them to default mocks.
   */
  def resetToDefault = decorated.resetToDefault
  
  /** 
   * Removes all expectations for the mock objects of this control and turn them to nice mocks.
   */
  def resetToNice = decorated.resetToNice
  
  /** 
   * Removes all expectations for the mock objects of this control and turn them to strict mocks.
   */
  def resetToStrict = decorated.resetToStrict
  
  /**
   * Verifies that all expectations were met. 
   */
  def verify = decorated.verify
}  

object MocksControl {
  def create: MocksControl = new MocksControl(createControl)    

  private[test] def nameFrom[T](implicit manifest: Manifest[T]): String = nameFrom(manifest.erasure)
  private[test] def nameFrom[T](someClass: Class[T]): String = lowerCaseFirstLetter(someClass.getSimpleName)
  private[test] def lowerCaseFirstLetter(s: String) = s(0).toLower + s.drop(1)
}
