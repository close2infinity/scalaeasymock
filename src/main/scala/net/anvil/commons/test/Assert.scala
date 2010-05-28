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

import org.junit.Assert._

object Assert {
  
  /**
   * Assert that an operation throws the expected exception. This is a 
   * replacement for JUnit's expected exceptions defined on the test 
   * annotation, which has the disadvantage it is impossible to do further 
   * validation on the fixture after the exception has been thrown.
   *
   * @param T the throwable expected to be thrown.
   * @param operation the code that's supposed to throw the exception.
   */
  def assertThrows[T <: Throwable](operation: => Any)(implicit manifest: Manifest[T]) {
    try {
      operation
      fail("Expected operation to throw " + manifest.erasure.getName + ".")
    }
    catch {
      case e: Throwable if (e.getClass.isAssignableFrom(manifest.erasure)) => ()
      case e: Any => throw new AssertionError("Unexpected throwable of type " + e.getClass.getName + ".", e)
    }
  }
}
