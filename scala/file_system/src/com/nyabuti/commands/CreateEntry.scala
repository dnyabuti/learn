package com.nyabuti.commands

import com.nyabuti.files.{DirEntry, Directory}
import com.nyabuti.filesystem.State

abstract class CreateEntry(name: String) extends Command {
  override def apply(state: State): State = {
    val wd = state.wd
    if(wd.hasEntry(name)){
      state.setMessage(s"Entry $name already exists")
    } else if (name.contains(Directory.SEPARATOR)){
      state.setMessage(s"$name must not contain separators (${Directory.SEPARATOR})")
    } else if (checkIllegal(name)){
      state.setMessage(s"$name: illegal entry name")
    } else {
      doCreateEntry(state,name)
    }
  }

  def checkIllegal(name: String): Boolean = {
    name.contains(".")
  }

  def doCreateEntry(state: State, name: String): State = {
    def updateStructure(currentDirectory: Directory, path: List[String], newEntry: DirEntry): Directory = {
      if(path.isEmpty) currentDirectory.addEntry(newEntry)
      else {
        val oldEntry = currentDirectory.findEntry(path.head)
        currentDirectory.replaceEntry(oldEntry.name, updateStructure(oldEntry.asDirectory, path.tail, newEntry))
      }
    }

    val wd = state.wd

    // get all dir in full path
    val allDirsInPath = wd.getAllFoldersInPath

    // create new dir in the wd
    val newEntry: DirEntry = doCreateSpecificEntry(state)
    //val newDir = Directory.empty(wd.path, name)

    // update whole dir structure from the root
    val  newRoot = updateStructure(state.root, allDirsInPath, newEntry)

    // find new wd instance given the wd full path in the new dir structure
    val newWd = newRoot.findDescendant(allDirsInPath)

    State(newRoot, newWd)
  }

  def doCreateSpecificEntry(state: State): DirEntry

}
