abstract class Area(val tiles: Vector[Tile]):
  var controller: Option[Player]
  var strength: Int

  //if control changes updates score accordingly
  def updateScore(newController: Option[Player]): Unit

  def updateControl() =
    var redCount = 0
    var blueCount = 0
    var newController: Option[Player] = None
    //check who has most troops in the area
    for tile <- tiles do
      tile.troop match
        case Some(troop: Troop) =>
          troop.controller match
            case RedPlayer => redCount += 1
            case BluePlayer => blueCount += 1
        case None =>
    if redCount > blueCount then
      newController = Some(RedPlayer)
    else if blueCount > redCount then
      newController = Some(BluePlayer)
    val strengthDifference = math.abs(redCount-blueCount)
    //update strength
    newController match
      case Some(player: Player) =>
        if newController != controller then
          if strength > 0 then
            strength -= strengthDifference
            if strength <= 0 then
              strength = 0
              updateScore(None)
          else
            updateScore(newController)
        else if strength < 3 then
          strength += strengthDifference
          if strength > 3 then
            strength = 3
      case None =>
        if controller.isEmpty then
          strength = 1
end Area

class Settlement(tiles: Vector[Tile]) extends Area(tiles):
  var controller = None
  var strength = 1

  def updateScore(newController: Option[Player]) =
    newController match
      case Some(player: Player) =>
        if newController != controller then
          player.settlements += 1
          controller.foreach(_.settlements -= 1)
          controller = newController
      case None =>
        controller.foreach(_.settlements -= 1)


class Base(tiles: Vector[Tile], owner: Player) extends Area(tiles):
  var controller = Some(owner)
  var strength = 3

  def updateScore(newController: Option[Player]) = 
    controller.foreach(_.baseCaptured = true)
    controller = None