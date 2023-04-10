class Game(val gameLevel: GameLevel):
  val gameState = GameState(this)
  var currentAction: Action = NoFocus()
end Game

