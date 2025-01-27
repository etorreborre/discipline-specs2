/*
 * Copyright 2018-2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.discipline
package specs2.mutable

import org.specs2.mutable.SpecificationLike
import org.specs2.ScalaCheck
import org.specs2.scalacheck.Parameters
import org.specs2.specification.core.Fragments

trait Discipline extends ScalaCheck { self: SpecificationLike =>

  def checkAll(name: String, ruleSet: Laws#RuleSet)(implicit p: Parameters) = {
    s"""${ruleSet.name} laws must hold for $name""".txt
    br
    t
    Fragments.foreach(ruleSet.all.properties.toList.zipWithIndex) { case ((id, prop), n) =>
      addFragment(fragmentFactory.example(id, check(prop, p, defaultFreqMapPretty)))
      if (n != ruleSet.all.properties.toList.size - 1) br
      else bt
    }
  }

}
