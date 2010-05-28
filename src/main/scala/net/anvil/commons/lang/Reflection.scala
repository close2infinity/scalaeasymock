package net.anvil.commons.lang

import java.lang.reflect.{ AccessibleObject, Constructor, Method }
import scala.reflect.ClassManifest._

/** 
 * Utilities for reflective operations.
 */
object Reflection {
  
  /**
   * Find a constructor using a constructor descriptor. A constructor 
   * descriptor is a convenient and deterministic way to describe a known 
   * constructor without the cumbersome syntax of
   *  `Class.getDeclaredConstructor(..)`.
   * 
   * *Example* (returns `Date(year: Integer, month: Integer, date: Integer, 
   *   hrs: Integer, min: Integer, sec: Integer)`:
   * {{{
   * Reflection.findDeclared(constructorIn[java.util.Date](WithNumberOfArguments(6)))
   * }}}
   * 
   * @param constructorDescriptor an unambiguous constructor descriptor.  
   * @return a single method matching the descriptor.
   * 
   * @throws NoSuchElementException if the constructor descriptor didn't match 
   *   any constructor.
   * @throws IllegalArgumentException if the constructor descriptor is 
   *   ambiguous and matched multiple constructors. 
   * @see ConstructorDescriptor
   */
  def findDeclared[T](constructorDescriptor: ConstructorDescriptor[T]) (implicit manifest: Manifest[T]): 
    Constructor[T] = singleConstructorMatching(constructorDescriptor, getDeclaredConstructorsOf[T]) 

  /**
   * Find multiple constructors using constructor descriptors.
   * 
   * @return a list of constructor, one for each specified descriptor. 
   * @throws NoSuchElementException if any of the specified constructor 
   *   descriptors didn't match any declared constructor.
   * @throws IllegalArgumentException if any of the specified constructor 
   *   descriptors is ambiguous and matched multiple constructors. 
   * @see #findDeclaredConstructor(ConstructorDescriptor)
   * @see ConstructorDescriptor
   */
  def findAllDeclared[T]
    (firstConstructorDescriptor: ConstructorDescriptor[T], otherConstructorDescriptors: ConstructorDescriptor[T]*) 
    (implicit manifest: Manifest[T]): List[Constructor[T]] = 
  {
    val declaredConstructors = getDeclaredConstructorsOf[T]
    
    for (constructorDescriptor <- firstConstructorDescriptor :: List(otherConstructorDescriptors: _*)) 
      yield singleConstructorMatching[T](constructorDescriptor, declaredConstructors)
  }
    
  private def getDeclaredConstructorsOf[T](implicit manifest: Manifest[T]): List[Constructor[T]] =  
    List(manifest.erasure.getDeclaredConstructors.asInstanceOf[Array[Constructor[T]]]: _*)
  
  private def singleConstructorMatching[T](constructorDescriptor: ConstructorDescriptor[T], 
    constructors: List[Constructor[T]]): Constructor[T] = 
      asSingleMember[Constructor[T], T](allConstructorsMatching(constructorDescriptor, constructors), 
        constructorDescriptor) 
    
  private def allConstructorsMatching[T](constructorDescriptor: ConstructorDescriptor[T], 
    constructors: List[Constructor[T]]): List[Constructor[T]] = constructorDescriptor.filter(constructors)
      
  /**
   * Find a method using a method descriptor. A method descriptor is a 
   * convenient way to describe a known method in a deterministic way without 
   * the cumbersome syntax of `Class.getDeclaredMethod(..)`.
   * 
   * *Example* (returns `String.format(l: Locale, format: String, args: Object*)`:
   * {{{
   * Reflection.findDeclared(methodIn[java.lang.String]("format", WithNumberOfArguments(3)))
   * }}}
   * 
   * @param methodDescriptor an unambiguous method descriptor.  
   * @return a single method matching the descriptor.
   * 
   * @throws NoSuchElementException if the method descriptor didn't match any 
   *   method.
   * @throws IllegalArgumentException if the method descriptor is ambiguous 
   *   and matched multiple methods. 
   * @see MethodDescriptor[T]
   */
  def findDeclared[T](methodDescriptor: MethodDescriptor[T])(implicit manifest: Manifest[T]): Method = 
      singleMethodMatching(methodDescriptor, mapDeclaredMethodsOf[T])

  /**
   * Find multiple methods using method descriptors.
   * 
   * @param methodDescriptors a list of unambiguous method descriptors.
   * @return a list of methods, one for each specified descriptor. 
   * @throws NoSuchElementException if any of the specified method descriptors 
   *   didn't match a method.
   * @throws IllegalArgumentException if any of the specified method 
   *   descriptors is ambiguous and matched multiple methods. 
   * @see #findDeclaredMethod(MethodDescriptor[T])
   * @see MethodDescriptor[T]
   */
  def findAllDeclared[T]
    (firstMethodDescriptor: MethodDescriptor[T], otherMethodDescriptors: MethodDescriptor[T]*)
    (implicit manifest: Manifest[T]): List[Method] = 
  {
    val declaredMethods: Map[String, List[Method]] = mapDeclaredMethodsOf[T]

    for (methodDescriptor <- firstMethodDescriptor :: List[MethodDescriptor[T]](otherMethodDescriptors: _*))
      yield singleMethodMatching(methodDescriptor, declaredMethods)
  }
  
  /**
   * Get all declared methods form the class and put them into a map.
   * 
   * @return a map from method name to a list of all methods with that name.
   * @todo Using append (:+) operation may be inefficient.
   */
  private def mapDeclaredMethodsOf[T](implicit manifest: Manifest[T]): Map[String, List[Method]] = 
    (List[Method]() ++ manifest.erasure.getDeclaredMethods).foldLeft(Map[String, List[Method]]()) {
      (tmpDeclaredMethods, method) => tmpDeclaredMethods(method.getName) = 
        (tmpDeclaredMethods.get(method.getName).getOrElse(List[Method]()) :+ method)     
    }
  
  /**
   * @throws NoSuchElementException if the method descriptor didn't match any method.
   * @throws IllegalArgumentException if the method descriptor was ambiguous and matched multiple methods. 
   */
  private def singleMethodMatching[T](
    methodDescriptor: MethodDescriptor[T], 
    declaredMethods: Map[String, List[Method]]): Method = 
      asSingleMember(allMethodsMatching(methodDescriptor, declaredMethods), methodDescriptor) 
  
  private def allMethodsMatching[T](methodDescriptor: MethodDescriptor[T], declaredMethods: Map[String, List[Method]]):
    List[Method] = methodDescriptor.filter(declaredMethods(methodDescriptor.name))
  
  private def asSingleMember[M <: AccessibleObject, T](seqThatShouldContainExactlyOneElement: List[M], 
    expectedMemberDescriptor: ExecutableMemberDescriptor[M, T]): M = seqThatShouldContainExactlyOneElement match 
  {
      case List() => throw new NoSuchElementException(expectedMemberDescriptor.toString)
      case member :: List() => member
      case multipleMembers => throw new IllegalArgumentException(
        String.format("Member Descriptor %s matches multiple members: %s", expectedMemberDescriptor, multipleMembers))
  }
}

/** 
 * Describes the arguments list of a method signature.
 */
sealed abstract class ArgumentsDescriptor

/**
 * Describes a a method signature's arguments list that contains arbitrary 
 * arguments.
 */
case class WithAnyArguments() extends ArgumentsDescriptor

/**
 * Describes a method's or constructor's signature by its number of arguments. 
 * This descriptor should be used to identify overloaded methods in a simple 
 * and convenient manner.
 */
case class WithNumberOfArguments(numberOfArguments: Int) extends ArgumentsDescriptor

/**
 * Describes a method's or constructor's signature by its argument types, very 
 * much alike the syntax of the original `Class.getDeclaredMethod()`. This 
 * descriptor should be used for overloaded methods that have the same number 
 * of arguments.
 */
case class WithArgumentTypes(argumentTypes: List[Class[_]]) extends ArgumentsDescriptor 

object WithArgumentTypes { 
  def apply(firstArgumentType: Class[_], moreArgumentTypes: Class[_]*) = 
    new WithArgumentTypes(firstArgumentType :: List (moreArgumentTypes: _*))  
}

/** 
 * Describes an executable class member (either a constructor or a method).
 */
sealed abstract class ExecutableMemberDescriptor[M <: AccessibleObject, T](val argumentsDescriptor: ArgumentsDescriptor) {
  def filter[T](executableMembers: List[M]): List[M] = argumentsDescriptor match {
    case WithAnyArguments() => executableMembers
    case WithNumberOfArguments(numberOfArguments) => 
      executableMembers.filter((executableMember) => parameterTypesOf(executableMember).size == numberOfArguments)
    case WithArgumentTypes(argumentTypes) => 
      executableMembers.filter((executableMember) => parameterTypesOf(executableMember) == argumentTypes)
    case _ => throw new IllegalArgumentException(argumentsDescriptor.toString)
  }
  
  /**
   * Required to be overridden by any subclass because the Java Reflection API
   * defines no common superclass or interface for executable members. 
   */
  protected def parameterTypesOf(executableMember: M): List[Class[_]]
}

/**
 * Describes a constructor by its declaring class and arguments.
 */
case class ConstructorDescriptor[T](override val argumentsDescriptor: ArgumentsDescriptor) 
  extends ExecutableMemberDescriptor[Constructor[T], T](argumentsDescriptor)
{
  override def parameterTypesOf(constructor: Constructor[T]): List[Class[_]] = List(constructor.getParameterTypes: _*)  
}

object ConstructorDescriptor {
  def constructorIn[T](arguments: ArgumentsDescriptor = WithAnyArguments()) = new ConstructorDescriptor[T](arguments)
}

/**
 * Describes a method by its name and arguments.
 */
case class MethodDescriptor[T](name: String, override val argumentsDescriptor: ArgumentsDescriptor) 
  extends ExecutableMemberDescriptor[Method, T](argumentsDescriptor)
{
  def this(name: String) = this(name, WithAnyArguments())
  override def parameterTypesOf(method: Method): List[Class[_]] = List(method.getParameterTypes: _*)  
}

object MethodDescriptor {
  def methodIn[T](name: String, arguments: ArgumentsDescriptor = WithAnyArguments()) = 
    new MethodDescriptor[T](name, arguments)
}

