import TroopType.*
import Stat.*
import scala.collection.mutable.Buffer

trait Tile(val id: String, gridCoords: (Int, Int)):
  val coords = gridCoords
  var troop: Option[Troop] = None
  var isPassable = true
  val statModifier: Map[TroopType, Map[Stat, Int]]
  val movementCost: Map[TroopType, Int]
  var adjacentTiles = Vector[Tile]()

  def moveTo(movingTroop: Troop): Unit =
    //gets correct modifiers for the troop
    statModifier.get(movingTroop.troopType) match
      case Some(modifiers) =>
        //updates troop's stats
        for stat <- modifiers.keySet do
          movingTroop.modifyStat(stat, modifiers(stat))
      case None =>
    troop = Some(movingTroop)
    isPassable = false

  def removeTroop(): Unit =
    troop match
      case Some(leavingTroop) =>
        statModifier.get(leavingTroop.troopType) match
          case Some(modifiers) =>
            for stat <- modifiers.keySet do
              leavingTroop.modifyStat(stat, -modifiers(stat))
          case None =>
        troop = None
        isPassable = true
      case None =>
  
  def distanceTo(coords: (Int, Int)) =
    (gridCoords._1 - coords._1).abs + (gridCoords._2 - coords._2).abs
end Tile


case class Plain(gridCoords: (Int, Int)) extends Tile("plain", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]]()
  val movementCost = Map[TroopType, Int](Human -> 1, Vehicle -> 2, Flying -> 1)
end Plain


case class Forest(gridCoords: (Int, Int)) extends Tile("forest", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(Def -> 1, Rng -> -1), Vehicle -> Map(Rng -> -1))
  val movementCost = Map[TroopType, Int](Human -> 2, Vehicle -> 3, Flying -> 1)
end Forest


case class Water(gridCoords: (Int, Int)) extends Tile("water", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(Atk -> -1, Rng -> -1), Vehicle -> Map(Atk -> -1, Rng -> -3))
  val movementCost = Map[TroopType, Int](Human -> 3, Vehicle -> 999, Flying -> 1)
end Water

case class Marsh(gridCoords: (Int, Int)) extends Tile("marsh", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]](Human -> Map(Def -> -1), Vehicle -> Map(Def -> -1))
  val movementCost = Map[TroopType, Int](Human -> 3, Vehicle -> 4, Flying -> 1)
end Marsh

case class Road(gridCoords: (Int, Int)) extends Tile("road", gridCoords):
  val statModifier = Map[TroopType, Map[Stat, Int]]()
  val movementCost = Map[TroopType, Int](Human -> 1, Vehicle -> 1, Flying -> 1)
end Road
