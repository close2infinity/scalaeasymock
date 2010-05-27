package net.anvil.commons.lang;
  
class MyClass(val arg0: SomeClass, val arg1: SomeOtherClass) {
  def this() = this(new SomeClass(""), new SomeOtherClass(""))
  def this(arg0: String) = this(new SomeClass(""), new SomeOtherClass(""))
  def this(arg0: Boolean) = this(new SomeClass(""), new SomeOtherClass(""))
  
  def method1(arg0: String): Unit = ()
  def method2(arg0: String): Unit = ()
  def method2(arg0: String, arg1: String): Unit = ()
  def method3(arg0: String, arg1: String): Unit = ()
  def method3(arg0: String, arg1: Boolean): Unit = ()
}

class SomeClass(val s: String)
class SomeOtherClass(val s: String)
class YetSomeOtherClass