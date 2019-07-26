package com.nyabuti.commands

import com.nyabuti.files.{DirEntry, File}
import com.nyabuti.filesystem.State

class Touch(name: String) extends CreateEntry(name) {
  override def doCreateSpecificEntry(state: State): DirEntry = {
    File.empty(state.wd.path, name)
  }
}
