package com.nyabuti.commands

import com.nyabuti.filesystem.State

class Pwd extends Command {
  override def apply(state: State): State = {
    state.setMessage(state.wd.path)
  }
}
