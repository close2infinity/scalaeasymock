ScalaEasyMock is a wrapper around the [EasyMock](http://easymock.org) library that focuses on mock controls and EasyMock Class Extension.

It provides a convenient, easy to use DSL for mock controls that makes heavy use of Scala manifests. Additionally, it contains helper classes that make it easy to create test objects with constructor arguments and partial mocks.

Please see the [Tutorial](Tutorial.md) on how to use, along with the [API Docs](http://scalaeasymock.googlecode.com/hg/docs/api/index.html).

ScalaEasyMock is based on EasyMock 3.0 and written using Scala 2.8 RC3. It is **not** compatible with Scala 2.7.x.

**Note**: There is a snapshot version of ScalaEasyMock 1.0.0 available that works with EasyMock 2.5.2.


# News #

**2010-07-06**
  * Switched internally to the new IMockBuilder and got rid of the Java support class