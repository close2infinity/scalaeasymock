package net.anvil.commons.test

import java.lang.reflect.{Method, Constructor}
import org.easymock.classextension.{ConstructorArgs, IMocksControl}
import net.anvil.commons.lang._

/** 
 * A utility class that helps to create instances of classes that require many 
 * constructor arguments (e.g. in a dependency injection environment).
 */
class ObjectFactory(mocks: MocksControl) {
  private val constructorArgsFactory = ConstructorArgsFactory.using(mocks)  

  /**
   * Creates an instance of the specified class using the constructor that 
   * requires the most arguments, filling in all arguments by generating 
   * appropriate mock objects.
   * 
   * @param T the class to instantiate.
   */
  def create[T](implicit manifest: Manifest[T]): T = create[T]()

  /**
   * Creates an instance of the specified class using the constructor that 
   * requires the most arguments, filling in any arguments not specified by 
   * generating appropriate mock objects.
   * 
   * @param T the class to instantiate.
   * @param specifiedArguments some arguments.
   * @see ConstructorArgsFactory.tryCreateFor[T](specifiedArguments: AnyRef*)(implicit manifest: Manifest[T])
   */
  def create[T](specifiedArguments: AnyRef*)(implicit manifest: Manifest[T]): T = {
    val maybeConstructorArgs = constructorArgsFactory.tryCreateFor[T](specifiedArguments: _*)
    
    if (maybeConstructorArgs.isDefined) createInstanceUsing(maybeConstructorArgs.get)
    else manifest.erasure.newInstance.asInstanceOf[T] ensuring (specifiedArguments.size == 0)
  }
  
  /**
   * Creates an instance of the specified class using the constructor that
   * matches the specified descriptor, filling in any arguments not specified 
   * by generating appropriate mock objects.
   * 
   * @param T the class to instantiate.
   * @param constructorDescriptor a descriptor for a constructor.
   * @param specifiedArguments some arguments.
   * @see ConstructorArgsFactory.tryCreateFor[T](specifiedArguments: AnyRef*)(implicit manifest: Manifest[T])
   * @see Reflection.findDeclaredConstructor[T](constructorDescriptor: ConstructorDescriptor[T]) (implicit manifest: Manifest[T])
   */
  def create[T](constructorDescriptor: ConstructorDescriptor[T], specifiedArguments: AnyRef*)
    (implicit manifest: Manifest[T]): T = 
    createInstanceUsing(constructorArgsFactory.createFor[T](constructorDescriptor, specifiedArguments))    

  private def createInstanceUsing[T](constructorArgs: ConstructorArgs) = {
    assert(constructorArgs.getConstructor.getParameterTypes.size == constructorArgs.getInitArgs.length)
    constructorArgs.getConstructor.newInstance(constructorArgs.getInitArgs: _*).asInstanceOf[T]
  }
}

object ObjectFactory {

    /**
     * Creates a new object factory that uses the specified mocks control to
     * fill in required arguments. 
     */
    def using(mocks: MocksControl) = new ObjectFactory(mocks)  
}