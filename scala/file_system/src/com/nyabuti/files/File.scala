package com.nyabuti.files

import com.nyabuti.filesystem.FileSystemException

class File(override val parentPath: String, override val name: String, val contents: String)
  extends DirEntry(parentPath, name) {

  def asDirectory: Directory = throw new FileSystemException("A file cannot be converted to a directory")

  def getType: String = "File"

  def asFile: File = this

  override def isDirectory: Boolean = false

  override def isFile: Boolean = true

  def setContents(newContents: String): File = {
    new File(parentPath, name, newContents)
  }

  def appendContents(newContents: String): File = {
    setContents(contents +"\n"+ newContents)
  }
}

object File {
  def empty(parentPath: String, name: String): File = {
    new File(parentPath, name, "")
  }
}
