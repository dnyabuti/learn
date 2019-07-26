package com.nyabuti.commands

import com.nyabuti.filesystem.State

class UnknownCommand extends Command {
  override def apply(state: State): State = state.setMessage("Command not found")
}
