import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*

sealed abstract class Player:
  var cpu: Option[AI] = None
  val color: Color
  var baseCaptured = false
  var resources = 10
  var settlements = 0
  override def toString: String
  
  def isCPU = cpu.nonEmpty
end Player

object RedPlayer extends Player:
  val color = Red
  override def toString = "Red"
  
object BluePlayer extends Player:
  val color = Blue
  override def toString = "Blue"