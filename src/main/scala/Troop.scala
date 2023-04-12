import scala.collection.mutable.Buffer
import Stat.*
import TroopType.*
import scala.collection.mutable.Map

enum TroopType:
  case Human, Vehicle, Flying

enum Stat:
  case Hp, Atk, Def, Mov, Rng

trait Troop(val id: String, owner: Player, initialCoords: (Int, Int)):
  var gridCoords = initialCoords
  var imageViewIndex = -1
  val controller = owner
  var hasMoved = false
  var exhausted = false
  val range: Int
  val movement: Int
  val hp: Int
  val attackPower: Int
  val defense: Int
  val troopType: TroopType
  val cost: Int

  //can't be negative and are used to determine troop stats
  val stats = Map[Stat, Int](Hp -> 0, Atk -> 0, Def -> 0, Mov -> 0, Rng -> 0)
  //can be negative and are used only in counting purposes
  private val realStats = Map[Stat, Int](Hp -> 0, Atk -> 0, Def -> 0, Mov -> 0, Rng -> 0)

  def modifyStat(stat: Stat, amount: Int) =
    val modifiedValue = realStats(stat) + amount
    realStats(stat) = modifiedValue
    if modifiedValue < 0 then
      stats(stat) = 0
    else stats(stat) = modifiedValue

  //get base stat of given stat
  def baseStat(stat: Stat) =
    stat match
      case Hp => hp
      case Atk => attackPower
      case Def => defense
      case Mov => movement
      case Rng => range

  def initializeStats() =
    for stat <- Stat.values do
      modifyStat(stat, baseStat(stat))

  def takeDamage(amount: Int) =
    if amount > 0 then
      modifyStat(Hp, -amount)

  def extraDamage(tile: Tile): Int

  def attack(tile: Tile) =
    val target = tile.troop.get
    target.takeDamage(this.attackPower + extraDamage(tile) - target.defense)
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

  //def ability(): Unit
end Troop

case class Solider(owner: Player, initialCoords: (Int, Int)) extends Troop("solider", owner, initialCoords):
  val range = 4
  val movement = 5
  val hp = 3
  val attackPower = 3
  val defense = 1
  val troopType = Human
  val cost = 1

  def extraDamage(tile: Tile) = if tile.distanceTo(gridCoords) < 3 then 2 else 0
end Solider

case class Tank(owner: Player, initialCoords: (Int, Int)) extends Troop("tank", owner, initialCoords):
  val range = 4
  val movement = 7
  val hp = 6
  val attackPower = 4
  val defense = 3
  val troopType = Vehicle
  val cost = 4

  def extraDamage(tile: Tile) = tile.troop match
    case Some(troop) => if troop.troopType == Vehicle then 2 else 0
    case None => 0
end Tank

case class Artillery(owner: Player, initialCoords: (Int, Int)) extends Troop("artillery", owner, initialCoords):
  val range = 7
  val movement = 4
  val hp = 4
  val attackPower = 2
  val defense = 2
  val troopType = Vehicle
  val cost = 4

  def extraDamage(tile: Tile) = if tile.distanceTo(gridCoords) > 4 then 3 else 0
end Artillery

case class Sniper(owner: Player, initialCoords: (Int, Int)) extends Troop("sniper", owner, initialCoords):
  val range = 6
  val movement = 3
  val hp = 3
  val attackPower = 3
  val defense = 1
  val troopType = Human
  val cost = 3

  def extraDamage(tile: Tile) = tile.troop match
    case Some(troop) => if troop.troopType == Flying then 2 else 0
    case None => 0
end Sniper

case class Apache(owner: Player, initialCoords: (Int, Int)) extends Troop("apache", owner, initialCoords):
  val range = 4
  val movement = 5
  val hp = 4
  val attackPower = 4
  val defense = 2
  val troopType = Flying
  val cost = 6

  def extraDamage(tile: Tile) = 0
end Apache

/*
case class Fighter(owner: Player, initialCoords: (Int, Int)) extends Troop("fighter", owner, initialCoords):
  val range = 4
  val movement = 6
  val hp = 4
  val attackPower = 4
  val defense = 3
  val troopType = Flying
end Fighter

case class Medic(owner: Player, initialCoords: (Int, Int)) extends Troop("medic", owner, initialCoords):
  val range = 0
  val movement = 4
  val hp = 4
  val attackPower = 0
  val defense = 0
  val troopType = Human
end Medic

case class Mechanic(owner: Player, initialCoords: (Int, Int)) extends Troop("mechanic", owner, initialCoords):
  val range = 0
  val movement = 4
  val hp = 4
  val attackPower = 0
  val defense = 0
  val troopType = Human
end Mechanic

case class Transport(owner: Player, initialCoords: (Int, Int)) extends Troop("transport", owner, initialCoords):
  val range = 0
  val movement = 6
  val hp = 5
  val attackPower = 0
  val defense = 2
  val troopType = Vehicle
end Transport

case class Pontoon(owner: Player, initialCoords: (Int, Int)) extends Troop("pontoon", owner, initialCoords):
  val range = 0
  val movement = 4
  val hp = 3
  val attackPower = 0
  val defense = 2
  val troopType = Vehicle
end Pontoon
*/
