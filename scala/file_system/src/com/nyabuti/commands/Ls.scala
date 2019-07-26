package com.nyabuti.commands

import com.nyabuti.files.DirEntry
import com.nyabuti.filesystem.State

class Ls extends Command {
  override def apply(state: State): State = {
    val contents = state.wd.contents
    val niceOutput = createNiceOutput(contents)
    state.setMessage(niceOutput)
  }

  def createNiceOutput(contents: List[DirEntry]): String = {
    if(contents.isEmpty) ""
    else {
      val entry = contents.head
      s"${entry.name} [${entry.getType}]\n${createNiceOutput(contents.tail)}"
    }
  }
}
