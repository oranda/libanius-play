/*
 * Copyright 2013 James McCabe <james@oranda.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oranda.libanius.mobile.dependencies

import play.api.Logger
import com.oranda.libanius.dependencies.{Logger => LibaniusLogger}

class LoggerPlay extends LibaniusLogger {

  override def logImpl(message: String, module: String = "Libanius", t: Option[Throwable] = None) {
    Logger("application").error("LoggerPlay logImpl called")
    t match {
      case Some(t) => Logger("application").error(module + ": " + message, t)
      case _ => Logger("application").debug(module + ": " + message)
    }
  }
}