/*
 * Libanius-Play
 * Copyright (C) 2013-2014 James McCabe <james@oranda.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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