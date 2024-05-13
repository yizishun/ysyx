package npc

case class Config(
  core : CoreConfig,
)

case class CoreConfig(
  xlen : Int
)

/*case class MemConfig(
  xlen : Int
)*/

object MiniConfig{
  def apply(): Config = {
    val xlen = 32
    Config(
      core = CoreConfig(
        xlen = xlen
      )
    )
  }
}