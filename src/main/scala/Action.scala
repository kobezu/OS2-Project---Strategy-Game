sealed trait Action(activeTile: Option[Tile])

case class NoFocus() extends Action(None)
case class TileFocus(activeTile: Tile) extends Action(Some(activeTile))
case class TroopFocus(activeTile: Tile) extends Action(Some(activeTile))
case class Moving(activeTile: Tile) extends Action(Some(activeTile))
case class Attacking(activeTile: Tile) extends Action(Some(activeTile))
case class BuildTroop(activeTile: Tile) extends Action(Some(activeTile))
