import scala.collection.mutable.Buffer
import Stat.*
import TroopType.*
import com.sun.prism.impl.Disposer.Target

import scala.collection.mutable.Map

enum TroopType:
  case Human, Vehicle, Flying

enum Stat:
  case Hp, Atk, Def, Mov, Rng

trait Troop(val id: String, owner: Player, initialTile: Tile, n: Int):
  var currentTile = initialTile
  var imageViewIndex = -1
  val controller = owner
  var hasMoved = false
  var exhausted = false
  var isDead = false
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

  //lower Hp and return remaining Hp
  def takeDamage(amount: Int): Boolean =
    if amount > 0 then
      modifyStat(Hp, -amount)
    if realStats(Hp) <= 0 then
      isDead = true
    isDead
  
  def extraDamage(tile: Tile): Int

  def attackDamage(target: Troop): Int = this.stats(Atk) + extraDamage(target.currentTile) - target.stats(Def)

  def attack(target: Troop): Boolean =
    exhaust()
    target.takeDamage(attackDamage(target))

  def move(tile: Tile) =
    currentTile.removeTroop()
    currentTile = tile
    tile.moveTo(this)
    hasMoved = true

  def exhaust() =
    hasMoved = true
    exhausted = true

  def refresh() =
    hasMoved = false
    exhausted = false

  def gridCoords = currentTile.coords
  //def ability(): Unit

  //used by AI
  //checks if this troop dies
  def dies(damage: Int): Boolean = stats(Hp) - damage <= 0
  //check how much damage target troop takes over its health
  def wastedAttackDamage(target: Troop) =
    attackDamage(target) - target.stats(Hp)
end Troop

case class Solider(owner: Player, initialTile: Tile, n: Int) extends Troop("solider", owner, initialTile, n):
  val range = 4
  val movement = 5
  val hp = 3
  val attackPower = 3
  val defense = 1
  val troopType = Human
  val cost = 1

  def extraDamage(tile: Tile) = if tile.distanceTo(currentTile.coords) < 3 then 2 else 0
end Solider

case class Tank(owner: Player, initialTile: Tile, n: Int) extends Troop("tank", owner, initialTile, n):
  val range = 4
  val movement = 7
  val hp = 5
  val attackPower = 4
  val defense = 3
  val troopType = Vehicle
  val cost = 5

  def extraDamage(tile: Tile) = tile.troop match
    case Some(troop) => if troop.troopType == Vehicle then 2 else 0
    case None => 0
end Tank

case class Artillery(owner: Player, initialTile: Tile, n: Int) extends Troop("artillery", owner, initialTile, n):
  val range = 7
  val movement = 4
  val hp = 4
  val attackPower = 2
  val defense = 2
  val troopType = Vehicle
  val cost = 4

  def extraDamage(tile: Tile) = if tile.distanceTo(tile.coords) > 4 then 3 else 0
end Artillery

case class Sniper(owner: Player, initialTile: Tile, n: Int) extends Troop("sniper", owner, initialTile, n):
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

case class Apache(owner: Player, initialTile: Tile, n: Int) extends Troop("apache", owner, initialTile, n):
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
case class Fighter(owner: Player, initialTile: (Int, Int)) extends Troop("fighter", owner, initialTile):
  val range = 4
  val movement = 6
  val hp = 4
  val attackPower = 4
  val defense = 3
  val troopType = Flying
end Fighter

case class Medic(owner: Player, initialTile: (Int, Int)) extends Troop("medic", owner, initialTile):
  val range = 0
  val movement = 4
  val hp = 4
  val attackPower = 0
  val defense = 0
  val troopType = Human
end Medic

case class Mechanic(owner: Player, initialTile: (Int, Int)) extends Troop("mechanic", owner, initialTile):
  val range = 0
  val movement = 4
  val hp = 4
  val attackPower = 0
  val defense = 0
  val troopType = Human
end Mechanic

case class Transport(owner: Player, initialTile: (Int, Int)) extends Troop("transport", owner, initialTile):
  val range = 0
  val movement = 6
  val hp = 5
  val attackPower = 0
  val defense = 2
  val troopType = Vehicle
end Transport

case class Pontoon(owner: Player, initialTile: (Int, Int)) extends Troop("pontoon", owner, initialTile):
  val range = 0
  val movement = 4
  val hp = 3
  val attackPower = 0
  val defense = 2
  val troopType = Vehicle
end Pontoon
*/
