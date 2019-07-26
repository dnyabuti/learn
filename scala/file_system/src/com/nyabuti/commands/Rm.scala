package com.nyabuti.commands

import com.nyabuti.files.Directory
import com.nyabuti.filesystem.State

class Rm(name: String) extends Command {
  override def apply(state: State): State = {
    // get wd
    val wd = state.wd

    // get abs path
    val absolutePath = {
      if (name.startsWith(Directory.SEPARATOR)) name
      else if (wd.isRoot) wd.path + name
      else wd.path + Directory.SEPARATOR + name
    }

    // do some checks
    if (Directory.ROOT_PATH.equals(absolutePath))
      state.setMessage("Operation not supported")
    else
      doRm(state, absolutePath)
  }

  def doRm(state: State, path: String): State = {
    // find the entry to remove
    // update structure

    def rmHelper(currentDirectory: Directory, path: List[String]): Directory = {
      // TODO implement findDescendant in Directory
      if (path.isEmpty) currentDirectory
      else if (path.tail.isEmpty) currentDirectory.removeEntry(path.head)
      else {
        val nextDirectory = currentDirectory.findEntry(path.head)
        if  (!nextDirectory.isDirectory) currentDirectory
        else {
          val newNextDirectory = rmHelper(nextDirectory.asDirectory, path.tail)
          if (newNextDirectory == nextDirectory) currentDirectory
          currentDirectory.replaceEntry(path.head, newNextDirectory)
        }
      }
    }
    val tokens = path.substring(1).split(Directory.SEPARATOR).toList
    val newRoot: Directory = rmHelper(state.root, tokens)

    if(newRoot == state.root)
      state.setMessage(s"$path: no such file or directory")
    else
      State(newRoot, newRoot.findDescendant(state.wd.path.substring(1)))
  }

}
