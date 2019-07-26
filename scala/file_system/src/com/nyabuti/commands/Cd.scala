package com.nyabuti.commands

import com.nyabuti.files.{DirEntry, Directory}
import com.nyabuti.filesystem.State

import scala.annotation.tailrec

class Cd(dir: String) extends Command {

  override def apply(state: State): State = {
    // cd /a/b/c (absolute path
    // cd b/c (relative path)

    // find root
    val root = state.root
    val wd = state.wd

    // find absolute path of the directory i want to cd to
    val absolutePath =
      if (dir.startsWith(Directory.SEPARATOR)) dir
      else if (wd.isRoot) wd.path + dir
      else wd.path + Directory.SEPARATOR + dir

    // find directory to cd to given path
    val destinationDirectory = doFindEntry(root, absolutePath)

    // change state given the new directory
    if (destinationDirectory == null || !destinationDirectory.isDirectory)
      state.setMessage(s"$dir: No such directory")
    else
      State(root, destinationDirectory.asDirectory)
  }

  def doFindEntry(root: Directory, path: String): DirEntry = {
    @tailrec
    def findEntryHelper(currentDirectory: Directory, path: List[String]): DirEntry = {
      if(path.isEmpty || path.head.isEmpty) currentDirectory
      else if(path.tail.isEmpty) currentDirectory.findEntry(path.head)
      else {
        val nextDir = currentDirectory.findEntry(path.head)
        if (nextDir == null || !nextDir.isDirectory) null
        else findEntryHelper(nextDir.asDirectory, path.tail)
      }
    }

    @tailrec
    def collapseRelativeTokens(path: List[String], result: List[String]): List[String] = {
      if (path.isEmpty) result
      else if (".".equals(path.head)) collapseRelativeTokens(path.tail, result)
      else if ("..".equals(path.head)) {
        if (result.isEmpty) null
        else collapseRelativeTokens(path.tail, result.init)
      }
      else collapseRelativeTokens(path.tail, result :+ path.head)
    }

    // tokens
    val tokens: List[String] = path.substring(1).split(Directory.SEPARATOR).toList

    // eliminate relative tokens
    /*
      /a/. => ["a", "."] => ["a"]
      /a/../ => ["a", ".."] => []
      /a/b/.. => ["a", "b", ".."] => ["a"]
     */
    val newTokens = collapseRelativeTokens(tokens, List())
    if(newTokens == null) null
    else findEntryHelper(root, newTokens)
  }
}
