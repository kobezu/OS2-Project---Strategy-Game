trait Tile(val id: String, gridCoords: (Int, Int)):
  val coords = gridCoords
  var troop: Option[Troop] = None
  var isPassable = true
  val statModifier: Int
  val movementCost: Int

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
  val movementCost = 1
  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Plain


case class Forest(gridCoords: (Int, Int)) extends Tile("forest", gridCoords):
  val statModifier = 2
  val movementCost = 2

  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Forest


case class Water(gridCoords: (Int, Int)) extends Tile("water", gridCoords):
  val statModifier = 3
  val movementCost = 3

  def addModifier(troop: Troop): Unit = ???
  def removeModifier(troop: Troop): Unit = ???
end Water