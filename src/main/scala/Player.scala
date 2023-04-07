sealed abstract class Player:
  //val base =
  var settlements = 0
  var resources = 10
  override def toString: String
end Player

object Red extends Player:
  override def toString = "Red"
  
object Blue extends Player:
  override def toString = "Blue"