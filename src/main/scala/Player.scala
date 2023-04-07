import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*

sealed abstract class Player:
  //val base =
  val color: Color
  var baseCaptured = false
  var settlements = 0
  var resources = 10
  override def toString: String
end Player

object RedPlayer extends Player:
  val color = Red
  override def toString = "Red"
  
object BluePlayer extends Player:
  val color = Blue
  override def toString = "Blue"