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