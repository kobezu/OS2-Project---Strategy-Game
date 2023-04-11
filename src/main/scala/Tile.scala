import TroopType.*
import Stat.*

trait Tile(val id: String, gridCoords: (Int, Int)):
  val coords = gridCoords
  var troop: Option[Troop] = None
  var isPassable = true
  val statModifier: Map[TroopType, Map[Stat, Int]]
  val movementCost: Map[TroopType, Int]

  def moveTo(movingTroop: Troop): Unit =
    troop = Some(movingTroop)
    addModifiers(movingTroop)
    isPassable = false

  def removeTroop(): Unit =
    removeModifiers(troop.get)
    troop = None
    isPassable = true

  def addModifiers(troop: Troop) =
    //gets correct modifiers for the troop
    val modifiers = statModifier(troop.troopType)
    //updates troop's stats
    for stat <- modifiers.keySet do
      troop.modifyStat(stat, modifiers(stat))

  def removeModifiers(troop: Troop) =
    val modifiers = statModifier(troop.troopType)
    for stat <- modifiers.keySet do
      troop.modifyStat(stat, -modifiers(stat))

end Tile


case class Plain(gridCoords: (Int, Int)) extends Tile("plain", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(), Vehicle -> Map(), Flying -> Map())
  val movementCost = Map[TroopType, Int](Human -> 1, Vehicle -> 2, Flying -> 1)
end Plain


case class Forest(gridCoords: (Int, Int)) extends Tile("forest", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(Def -> 1, Rng -> -1), Vehicle -> Map(Rng -> -1), Flying -> Map())
  val movementCost = Map[TroopType, Int](Human -> 2, Vehicle -> 3, Flying -> 1)
end Forest


case class Water(gridCoords: (Int, Int)) extends Tile("water", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(Atk -> -1, Rng -> -1), Vehicle -> Map(Atk -> -1, Rng -> -3), Flying -> Map())
  val movementCost = Map[TroopType, Int](Human -> 3, Vehicle -> 999, Flying -> 1)
end Water

case class Marsh(gridCoords: (Int, Int)) extends Tile("marsh", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(Def -> -1), Vehicle -> Map(Def -> -1), Flying -> Map())
  val movementCost = Map[TroopType, Int](Human -> 3, Vehicle -> 4, Flying -> 1)
end Marsh

case class Road(gridCoords: (Int, Int)) extends Tile("road", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(), Vehicle -> Map(), Flying -> Map())
  val movementCost = Map[TroopType, Int](Human -> 1, Vehicle -> 1, Flying -> 1)
end Road
