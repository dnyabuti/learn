package com.nyabuti.commands

import com.nyabuti.files.{DirEntry, Directory}
import com.nyabuti.filesystem.State

class Mkdir(name: String) extends CreateEntry(name) {
  override def doCreateSpecificEntry(state: State): DirEntry = {
    Directory.empty(state.wd.path, name)
  }
}
