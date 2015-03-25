# Introduction #

EasyMock is one of my workhorses at work. Mocks are a great thing - sometimes it is not even possible to test without them in any sensible way, and even if it is, often a test using them runs about 10 times faster than the same one that needs to initialize all the useless infrastructure.

Like most people, I originally started out using EasyMock **without** controls, simply running ` xxx = createMock(XXX.class); ...; replay(xxx); ` on every mock, always cursing the need to add that damned ` replay(..); ` to everything (I even wrote an Eclipse plugin to do that for me). When I learned about controls, it was like a revelation for me.

Scala, which has now become my favorite programming language, offers great support for unit testing and also for mocking in form of the [ScalaTest](http://www.scalatest.org/) library. ScalaTest includes EasyMock and does a great job on adding syntactic suggar to it, but it doesn't seem to have any support for controls. Even worse, you can't just use EasyMock controls without the intermediary ScalaTest because the Scala compiler cannot handle the overloaded ` createMock(..) ` method - you have to add at least one parameter of type ` java.lang.reflect.Method ` for the call to work, but that makes it a **partial** mock, which is not always wanted.

The only solution I found for this problem was to introduce a bridge class written in Java. Continuing on that path, I wrote a library around it that adds syntactic sugar to mock controls.

# Details #

## Using ScalaEasyMock to create controls and mocks ##

### Create a control ###

```
import net.anvil.commons.test._
...
val mocks = MocksControl.create
```

### Create simple mocks ###

```
val myMock = mocks.mock[MyClass]
```

ScalaEasyMock tries to be syntactically close to ScalaTest, so the ` mock ` method doesn't take an argument ` classOf[MyClass] ` as it would in Java. Instead, it uses [Manifests](http://www.scala-blogs.org/2008/10/manifests-reified-types.html) to get the actual class to mock.

You can specify additional parameters to the mock, for example a name:

```
val myMock = mocks.mock[MyClass]("myMock")
```

Please note that ScalaEasyMock doesn't have the same default behavior concerning names as EasyMock itself. If you don't specify a name for an EasyMock mock, it won't have one (which makes it onerous to debug unexpected calls, going only by the method name). Instead, ScalaEasyMock will generate a name from the mocked type if you don't specify one.

### Create a partial mock for a class ###

EasyMock has a very powerful and very difficult to use feature that's called _partial mocking_. If you add one or more instances of ` java.lang.reflect.Method ` to the parameter list of ` createMock(..) `, **only** this methods will actually be mocked, leaving the rest at their default behavior.

However, this results usually in a monster of code, like this:

```
// This is java code:

createMock(
    MyClass.class, 
    MyClass.class.getDeclaredMethod("method1", new Class[] { String.class }), 
    MyClass.class.getDeclaredMethod("method2", new Class[] { String.class }), 
    MyClass.class.getDeclaredMethod("method3", new Class[] { String.class, Boolean.class }));
```

With ScalaEasyMock, this is much simpler:

```
import net.anvil.commons.lang._
import net.anvil.commons.lang.MethodDescriptor._
...

mocks.mockIn[MyClass](
  "method1",
  ("method2", WithNumberOfArguments(1)), 
  ("method3", WithArgumentTypes(classOf[String], classOf[Boolean])))
```

There is an object ` Reflection ` provided with the library that allows this by using case classes and implicit conversion. The rule is that you just need to specify the method name as a String if it is unambiguous (not overloaded). For overloaded methods that can be made unambiguous by their number of arguments, you can specify a tuple of method name and an _arguments descriptor_, ` WithNumberOfArguments(count: Int)`. In case you encounter the rare case that a method is overloaded and has the same number of arguments, you can use a different arguments descriptor ` WithArgumentTypes(types: Class[_]*) `.

### Create a mock for a class and specify some constructor args ###

If you create a mock for a class, per default it will be instantiated using an anonymous default constructor with no arguments, causing all its fields to be ` null`. This is no problem for normal mocks, but partial mocks suffer from it.

EasyMock solves this by allowing to add an additional parameter of type ` ConstructorArgs ` that contains a ` java.lang.reflect.Constructor ` and a list of arguments.

Like with partial mocks, this is quite difficult to do, because you have to look up the constructor using ` Class.getDeclaredConstructor(Class<?>... parameterTypes) `, which again will result in a huge ugly code monster.

Again, ScalaEasyMock provides some convenience functionality for this:

```
  import net.anvil.commons.lang._
  import net.anvil.commons.lang.ConstructorDescriptor._
  ...

  mocks.mock[MyClass](
    "myMock", 
    constructorArgsFactory.createFor(
      constructorIn[MyClass](WithNumberOfArguments(2)), 
      new SomeClass("someClass"), new SomeOtherClass("someOtherClass")))
```

Moreover, you are not required to specify **all** of the required arguments, but only those you need to have a specific value:

```
  import net.anvil.commons.lang._
  import net.anvil.commons.lang.ConstructorDescriptor._
  ...
  val constructorArgsFactory = ConstructorArgsFactory using mocks

  mocks.mock[MyClass](
    "myMock", 
    constructorArgsFactory.createFor(
      constructorIn[MyClass](WithNumberOfArguments(2)), 
      new SomeClass("someClass")))
```

How will that work? The ` ConstructorArgsFactory ` will internally create default values for simple parameter types (numbers and strings) and for complex types, mocks - using the mocks control it was instantiated with - for any argument that was not specified.

### Create non-mock test objects ###

The same facility can also be used to create instances of classes without having to specify all their constructor parameters. This is facilitated by ` ObjectFactory `:

```
  import net.anvil.commons.lang._
  ...

  val objectFactory = ObjectFactory using mocks
  objectFactory.create[MyClass](new SomeClass("someClass"))
```

By default, ObjectFactory will auto-determine the constructor to use - normally, the (first) constructor with the most arguments. You can, of course, also use a constructor descriptor to choose a specific constructor:

```
  objectFactory.create[MyClass](WithNumberOfArguments(1), new SomeClass("someClass"))
```

### Disclaimer ###

This project is quite new and hasn't yet encountered any testing except the unit tests provided with it. Please don't hesitate to file bug reports.