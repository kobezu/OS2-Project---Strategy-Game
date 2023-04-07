import scala.collection.mutable.Buffer

trait Troop(val id: String, owner: Player, initialCoords: (Int, Int)):
  var gridCoords = initialCoords
  var imageViewIndex = -1
  val controller = owner
  var hasMoved = false
  var exhausted = false
  val range: Int
  val movement: Int
  var hp: Int
  val attackPower: Int
  val defense: Int
  
  def takeDamage(amount: Int) =
    if amount > 0 then
      hp -= amount

  def attack(target: Troop) =
    target.takeDamage(this.attackPower - target.defense)
    exhaust()

  def move(movePos: (Int, Int)) = 
    gridCoords = movePos
    hasMoved = true

  def exhaust() =
    hasMoved = true
    exhausted = true

  def refresh() =
    hasMoved = false
    exhausted = false
    
  /*
  val resistance: Tile

  def ability(): Unit
  */
end Troop

case class Solider(owner: Player, initialCoords: (Int, Int)) extends Troop("solider", owner, initialCoords):
  val range = 3
  val movement = 5
  var hp = 3
  val attackPower = 3
  val defense = 1
end Solider