package npc

class CoreConfig(val xlen : Int){ 
  def useDPIC : Boolean = true
  def ysyxsoc : Boolean = true
  def npc     : Boolean = false
}

/*case class MemConfig(
  xlen : Int
)*/

object MiniConfig{
  def apply(): CoreConfig = {
    val xlen = 32
    val config = new CoreConfig(xlen)
    config
  }
}