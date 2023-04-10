import TroopType.*

trait Tile(val id: String, gridCoords: (Int, Int)):
  val coords = gridCoords
  var troop: Option[Troop] = None
  var isPassable = true
  val statModifier: Int
  val movementCost: Map[TroopType, Int]

  def moveTo(movingTroop: Troop): Unit =
    troop = Some(movingTroop)
    //addModifier(movingTroop)
    isPassable = false

  def removeTroop(): Unit =
    //removeModifier(troop.get)
    troop = None
    isPassable = true

  def addModifier(troop: Troop): Unit
  def removeModifier(troop: Troop): Unit
end Tile


case class Plain(gridCoords: (Int, Int)) extends Tile("plain", gridCoords):
  val statModifier = 0
  val movementCost = Map[TroopType, Int](Human -> 1, Vehicle -> 2, Flying -> 1)
  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Plain


case class Forest(gridCoords: (Int, Int)) extends Tile("forest", gridCoords):
  val statModifier = 2
  val movementCost = Map[TroopType, Int](Human -> 2, Vehicle -> 3, Flying -> 1)
  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Forest


case class Water(gridCoords: (Int, Int)) extends Tile("water", gridCoords):
  val statModifier = 3
  val movementCost = Map[TroopType, Int](Human -> 3, Vehicle -> 999, Flying -> 1)

  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Water

case class Marsh(gridCoords: (Int, Int)) extends Tile("marsh", gridCoords):
  val statModifier = 3
  val movementCost = Map[TroopType, Int](Human -> 3, Vehicle -> 4, Flying -> 1)

  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Marsh

case class Road(gridCoords: (Int, Int)) extends Tile("road", gridCoords):
  val statModifier = 3
  val movementCost = Map[TroopType, Int](Human -> 1, Vehicle -> 1, Flying -> 1)

  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Road
