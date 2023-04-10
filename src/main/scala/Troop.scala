import scala.collection.mutable.Buffer

enum TroopType:
  case Human, Vehicle, Flying

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

  val troopType: TroopType

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
  val troopType = TroopType.Human
  
end Solider

case class Tank(owner: Player, initialCoords: (Int, Int)) extends Troop("tank", owner, initialCoords):
  val range = 4
  val movement = 7
  var hp = 6
  val attackPower = 5
  val defense = 3
  val troopType = TroopType.Vehicle
end Tank

case class Artillery(owner: Player, initialCoords: (Int, Int)) extends Troop("artillery", owner, initialCoords):
  val range = 7
  val movement = 4
  var hp = 4
  val attackPower = 4
  val defense = 2
  val troopType = TroopType.Vehicle
end Artillery

case class Sniper(owner: Player, initialCoords: (Int, Int)) extends Troop("sniper", owner, initialCoords):
  val range = 6
  val movement = 3
  var hp = 3
  val attackPower = 4
  val defense = 1
  val troopType = TroopType.Human
end Sniper

case class Apache(owner: Player, initialCoords: (Int, Int)) extends Troop("apache", owner, initialCoords):
  val range = 4
  val movement = 5
  var hp = 4
  val attackPower = 4
  val defense = 2
  val troopType = TroopType.Flying
end Apache

/*
case class Fighter(owner: Player, initialCoords: (Int, Int)) extends Troop("fighter", owner, initialCoords):
  val range = 4
  val movement = 6
  var hp = 4
  val attackPower = 4
  val defense = 3
  val troopType = TroopType.Flying
end Fighter

case class Medic(owner: Player, initialCoords: (Int, Int)) extends Troop("medic", owner, initialCoords):
  val range = 0
  val movement = 4
  var hp = 4
  val attackPower = 0
  val defense = 0
  val troopType = TroopType.Human
end Medic

case class Mechanic(owner: Player, initialCoords: (Int, Int)) extends Troop("mechanic", owner, initialCoords):
  val range = 0
  val movement = 4
  var hp = 4
  val attackPower = 0
  val defense = 0
  val troopType = TroopType.Human
end Mechanic

case class Transport(owner: Player, initialCoords: (Int, Int)) extends Troop("transport", owner, initialCoords):
  val range = 0
  val movement = 6
  var hp = 5
  val attackPower = 0
  val defense = 2
  val troopType = TroopType.Vehicle
end Transport

case class Pontoon(owner: Player, initialCoords: (Int, Int)) extends Troop("pontoon", owner, initialCoords):
  val range = 0
  val movement = 4
  var hp = 3
  val attackPower = 0
  val defense = 2
  val troopType = TroopType.Vehicle
end Pontoon
*/
