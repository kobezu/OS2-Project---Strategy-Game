class Game():
  val generatedMap = Vector.fill(20, 40)(scala.util.Random.nextInt(3))
  val gameLevel = GameLevel(generatedMap)
  val gameState = GameState(this)
  var currentAction: Action = NoFocus()
end Game

