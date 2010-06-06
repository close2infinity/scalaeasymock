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

import java.lang.reflect.{Method, Constructor}
import org.easymock.{ConstructorArgs, IMocksControl}
import net.anvil.commons.lang._

/**
 * Factory that produces instances of 
 * org.easymock.classextension.ConstructorArgs for a specific class.
 */
class ConstructorArgsFactory(mocks: MocksControl) {
 
  /**
   * @see tryCreateFor[T](specifiedArguments: AnyRef*)(implicit manifest: Manifest[T])
   */
  def tryCreateFor[T](implicit manifest: Manifest[T]): Option[ConstructorArgs] = tryCreateFor[T](Nil)

  /**
   * Try to create constructor args for the constructor with the most 
   * arguments (which is normally the main constructor), using the 
   * specified arguments where possible and creating mocks to fill in the 
   * remaining ones.
   *  
   * @param specifiedArguments a sequence of arguments to be used in the 
   *   constructor args. If there are multiple arguments of the same type, 
   *   they will be filled in according to their order in the sequence. 
   * @return an option containing the constructor args if a declared 
   *   constructor was found on the class.
   */
  def tryCreateFor[T](specifiedArguments: AnyRef*)(implicit manifest: Manifest[T]): Option[ConstructorArgs] = 
  {
    val constructorOption = tryFindDeclaredConstructorWithMostArgumentsIn[T]
     
    if (constructorOption.isDefined) Some(createFor(constructorOption.get, specifiedArguments: _*))
    else None 
  }
  
  private def tryFindDeclaredConstructorWithMostArgumentsIn[T](implicit manifest: Manifest[T]): 
    Option[Constructor[T]] = 
  {
    val declaredConstructors = List(manifest.erasure.getConstructors: _*) 
  
    if (declaredConstructors.isEmpty) None 
    else Some(findConstructorWithMostArgumentsInNonEmpty(declaredConstructors).asInstanceOf[Constructor[T]])
  }
  
  private def findConstructorWithMostArgumentsInNonEmpty[T](listOfConstructors: List[Constructor[T]]): 
    Constructor[T] =
  { 
    listOfConstructors.tail.foldLeft(listOfConstructors.head.asInstanceOf[Constructor[T]]) {
      (lastConstructor, currentConstructor) => 
        if (currentConstructor.getParameterTypes.size > lastConstructor.getParameterTypes.size) currentConstructor
        else lastConstructor
    }
  }

  def createFor[T](constructorDescriptor: ConstructorDescriptor[T], specifiedArguments: AnyRef*)
    (implicit manifest: Manifest[T]) = 
  { 
    val constructor = Reflection.findDeclaredConstructorIn[T](constructorDescriptor)
    new ConstructorArgs(constructor, completeConstructorArgsFor(constructor, List(specifiedArguments: _*)): _*)
  }

  /**
   * Create constructor arguments for the specified constructor, using the 
   * specified arguments where possible and creating mocks to fill in the 
   * remaining ones.
   * 
   * @param specifiedArguments a sequence of arguments to be used in the 
   *   constructor args. If there are multiple arguments of the same type, 
   *   they will be filled in according to their order in the sequence. 
   * @return the constructor args.
   */
  def createFor(constructor: Constructor[_], specifiedArguments: AnyRef*) =  
    new ConstructorArgs(constructor, completeConstructorArgsFor(constructor, List(specifiedArguments: _*)): _*)
  
  /**
   * Completes the specified constructor arguments by taking arguments from the
   * specified list or using default values if no fitting one is found.
   * 
   * @param constructor the constructor.
   * @param specifiedArguments a list of arguments to use. This list doesn't 
   *   have to be complete.
   * @return a list of objects corresponding to the arguments list of the 
   *   constructor.
   */
  private def completeConstructorArgsFor(constructor: Constructor[_], specifiedArguments: List[AnyRef]): Array[AnyRef] = 
    for (parameterType <- constructor.getParameterTypes) yield 
      takeOrCreateArgumentOfType(parameterType, specifiedArguments).asInstanceOf[AnyRef]
  
  /**
   * @return an object of the specified type that is either the first object 
   *   of this type in the list
   */
  private def takeOrCreateArgumentOfType[T](someType: Class[T], specifiedArguments: List[AnyRef]): T =
    tryTakeFromListAndReturnRemaining(someType, specifiedArguments)._1.getOrElse(createDefaultValueOfType(someType))

  /**
   * @returns a tuple of 1) the first element in the source list that is of the
   *  specified type (if available) and 2) the remaining source list.
   */
  private def tryTakeFromListAndReturnRemaining[T](someType: Class[T], source: List[AnyRef]): 
    (Option[T], List[AnyRef]) = 
  { 
    source match {
      case List() => (None, List())
      case firstElement :: remainingElementsBeforeRecursion =>
      
        // This could be moved into a separate method for sake of clarity - 
        // however, doing so would break tail recursion:
        if (firstElement.getClass == someType) (Some(firstElement.asInstanceOf[T]), remainingElementsBeforeRecursion)
        else {
            val (maybeFoundElement, remainingElementsAfterRecursion) = 
              tryTakeFromListAndReturnRemaining(someType, remainingElementsBeforeRecursion)
                (maybeFoundElement, firstElement :: remainingElementsAfterRecursion)
        }
    }
  }

  private def createDefaultValueOfType[T](someType: Class[T]): T = {
    if (someType == classOf[Boolean]) false.asInstanceOf[T]
    else if(someType.isPrimitive) 0.asInstanceOf[T]
    else MocksControlHelper.createMock(mocks.decorated, MocksControl.nameFrom(someType), someType)
  }
}

object ConstructorArgsFactory {
  def using(mocks: MocksControl) = new ConstructorArgsFactory(mocks)
}
