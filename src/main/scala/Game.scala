class Game():
  val generatedMap = Vector.fill(18, 32)(scala.util.Random.nextInt(3))
  val gameLevel = GameLevel(generatedMap)
  
  var currentAction: Action = NoFocus()
end Game

