import scala.collection.mutable.Buffer

trait Troop(val id: String, initialCoords: (Int, Int)):
  var gridCoords = initialCoords
  var imageViewIndex = -1
  val range: Int
  val movement: Int
/*
  var hp: Int
  val movement: Int
  val attackPower: Int
  val defense: Int
  val resistance: Tile

  def attack(target: Troop) = target.takeDamage(this.attackPower - target.defense)

  def ability(): Unit

  def pass() = ???

  def takeDamage(amount: Int) =
    if amount > 0 then hp -= amount
*/
  def move(movePos: (Int, Int)) = 
    gridCoords = movePos

end Troop

case class Solider(initialCoords: (Int, Int)) extends Troop("solider", initialCoords):
  val range = 3
  val movement = 5
end Solider